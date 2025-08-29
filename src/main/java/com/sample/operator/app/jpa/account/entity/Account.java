package com.sample.operator.app.jpa.account.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Entity // jpa 사용
@EntityListeners(AuditingEntityListener.class) // createdDate 용
@Table(name="Account", schema = "test")
public class Account implements UserDetails
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int accountNo;

    @NotNull(message = "계정명은 비워둘 수 없습니다")
    @Column(columnDefinition = "varchar(255) character set utf8 collate utf8_bin")
    String accountName;
    

    @NotNull(message = "패스워드는 비워둘 수 없습니다")
    @Column(columnDefinition = "varchar(255) character set utf8 collate utf8_bin")
    String password;

    @CreatedDate
    LocalDateTime registeredAt;

    @Column(columnDefinition = "varchar(255) character set utf8 collate utf8_bin")
    String status;

    @Embedded
    MemberInfo memberInfo;

    @ToString.Exclude
    @OneToMany(mappedBy = "account", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    List<Authority> authorityList;

    // 권한 추가
    public void addAuthority(Role role)
    {
        if(Optional.ofNullable(authorityList).isEmpty())
        {
            authorityList = new ArrayList<>();
        }

        Authority auth = Authority.builder().account(this).role(role).build();
        authorityList.add(auth);
    }

    @Override
    public String getUsername() {
        return accountName;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorityList.stream().map(auth -> new SimpleGrantedAuthority(auth.getRole().getRoleName())).collect(Collectors.toSet());
    }
}
