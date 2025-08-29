package com.sample.operator.app.svc.account;

import com.sample.operator.app.dto.account.EnabledRoleModel;
import com.sample.operator.app.jpa.account.entity.Account;
import com.sample.operator.app.jpa.account.entity.Authority;
import com.sample.operator.app.jpa.account.entity.Role;
import com.sample.operator.app.jpa.account.repository.AccountRepository;
import com.sample.operator.app.jpa.account.repository.AuthorityRepository;
import com.sample.operator.app.jpa.account.repository.RoleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountSvc implements UserDetailsService {
    
    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;
    private final AuthorityRepository authorityRepository;
    private final RoleRepository roleRepository;

    @Override
    public UserDetails loadUserByUsername(String accountName) throws UsernameNotFoundException {
        Account account = accountRepository.findAccountByAccountName(accountName);
        
        if(Optional.ofNullable(account).isEmpty()) 
        {
            System.out.println(accountName + " 계정 없음");
            throw new UsernameNotFoundException(accountName + " 계정 없음");
        }
        
        if(Optional.ofNullable(account.getStatus()).isPresent() && account.getStatus().equals("비정상"))
        {
            System.out.println("비활성화 또는 삭제 또는 정지 된 계정");
            throw new InsufficientAuthenticationException("비활성화 삭제 정지 계정");
        }

        // DaoAuthenticationProvider의 additionalAuthenticationChecks 에서 자동으로 패스워드까지 검증
        return new User(account.getAccountName(), account.getPassword(), account.getAuthorities());
    }


    @Transactional
    public Account signUp(Account account)
    {
        // 중복 확인
        if( checkDuplicatedAccount(account.getAccountName()))
        {
            System.out.println("중복 계정");
            return null;
        }
        else
        {
            account.setPassword(passwordEncoder.encode(account.getPassword()));
            account.setStatus("정상");

            //  디폴트 계정 생성
            Account saved = accountRepository.save(account);

            // 계정 생성 시 디폴트로 가지는 권한 = MEMBER
            Role defaultRole =Role.builder().roleName("MEMBER").build();

            // MEMBER는 없으면 생성해서라도 부여
            roleRepository.findById("MEMBER").ifPresentOrElse(saved::addAuthority, () -> saved.addAuthority(roleRepository.save(defaultRole)));

            // 권한 추가
            saved.getAuthorityList().stream().forEach(auth -> {
                auth.setAccount(saved);
                authorityRepository.save(auth);
            });
            return saved;
        }
    }

    @Transactional
    public boolean modAccountAuth(String accountName, List<String> roleList)
    {
        Account account = accountRepository.findAccountByAccountName(accountName);

        if(Optional.ofNullable(account).isPresent())
        {
            //기존 권한 삭제
            int removed = authorityRepository.deleteAllByAccount(account);

            System.out.println("기존 권한 "+removed + "개가 삭제 되었습니다");

            // Role이 존재하는 경우메나 권한으로 추가
            roleList.forEach(roleName -> {
                roleRepository.findById(roleName).ifPresent(r -> {
                    Authority auth = Authority.builder().account(account).role(r).build();
                    authorityRepository.save(auth);
                });
            });
            return true;
        }
        else
        {
            System.out.println("없는 계정입니다" + accountName);
            return false;
        }
    }


    public Account modAccount(Account account, boolean isAdm)
    {
        Account mod = accountRepository.findAccountByAccountName(account.getAccountName());
        Account saved;

        // 일반 유저는 반드시 패스워드 필요
        if(!isAdm && account.getPassword().isEmpty())
        {
            System.out.println("패스워드는 비워둘 수 없습니다");
            return null;
        }

        if(Optional.ofNullable(mod).isEmpty())
        {
            System.out.println("없는 계정입니다 " + account.getAccountName());
            return null;
        }

        if(isAdm)
        {
            // 관리자는 이름 사번 상태 수정가능
            mod.setMemberInfo(account.getMemberInfo());
            mod.setStatus(account.getStatus());
            saved = accountRepository.saveAndFlush(mod);
        }
        else
        {
            // 관리자가 아니면 이름 사번 패스워드 변경 가능
            mod.setMemberInfo(account.getMemberInfo());
            mod.setPassword(passwordEncoder.encode(account.getPassword()));
            saved = accountRepository.saveAndFlush(mod);
        }

        return saved;

    }


    // 전체 Role 에서 유저의 권한만 True 값을 가지는 view 노출용 객체
    public List<EnabledRoleModel> getAllAuthoritiesOfAccount(Account account)
    {
        List<Role> userRole = account.getAuthorityList().stream().map(auth -> auth.getRole()).toList();
        List<Role> allRole = roleRepository.findAll();

        List<EnabledRoleModel> enabledRoles = allRole.stream().map(baseRole -> {
            boolean enabled = userRole.contains(baseRole);
            return EnabledRoleModel.builder().role(baseRole).enabled(enabled).build();
        }).collect(Collectors.toList());

        return enabledRoles;
    }


    public boolean checkDuplicatedAccount(String accountName)
    {
        return accountRepository.existsAccountByAccountName(accountName);
    }


    public Account getAccount(String accountName)
    {
        return accountRepository.findAccountByAccountName(accountName);
    }

    public List<Account> getAllAccount()
    {
        //페이저블 적용해야 함
        return accountRepository.findAll();
    }
    
    // 어드민 계정 생성 
    public Account makeAdmin(Account account)
    {
        if(checkDuplicatedAccount(account.getAccountName()))
        {
            System.out.println("중복 계정 " + account.getAccountName());
            return null;
        }
        else 
        {
            account.setPassword(passwordEncoder.encode(account.getPassword()));
            account.setStatus("정상");

            // 디폴트 계쩡 생성
            Account saved = accountRepository.save(account);

            // 계정 생성 시 디폴트 권한
            Role defaultRole1 = Role.builder().roleName("MEMBER").build();
            Role defaultRole2 = Role.builder().roleName("ADMIN").build();
            Role defaultRole3 = Role.builder().roleName("OPERATION").build();

            // MEMBER 권한은 없으면 생성해서라도 부여해야함
            roleRepository.findById("MEMBER").ifPresentOrElse(saved::addAuthority, () -> saved.addAuthority(roleRepository.save(defaultRole1)));
            roleRepository.findById("ADMIN").ifPresentOrElse(saved::addAuthority, () -> saved.addAuthority(roleRepository.save(defaultRole2)));
            roleRepository.findById("OPERATION").ifPresentOrElse(saved::addAuthority, () -> saved.addAuthority(roleRepository.save(defaultRole3)));

            // 권한 추가
            saved.getAuthorityList().stream().forEach(auth -> {
                auth.setAccount(saved);
                authorityRepository.save(auth);
            });

            return saved;
        }
    }
}
