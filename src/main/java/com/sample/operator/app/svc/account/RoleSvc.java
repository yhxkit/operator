package com.sample.operator.app.svc.account;

import com.sample.operator.app.jpa.account.entity.Role;
import com.sample.operator.app.jpa.account.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleSvc {

    private final RoleRepository roleRepository;

    public boolean addRole(String roleName)
    {
        Role role = roleRepository.findById(roleName).orElseGet(() -> roleRepository.save(new Role(roleName.toUpperCase())));
        return Optional.ofNullable(role).isPresent();
    }

    public boolean removeRole(String roleName)
    {
        roleRepository.deleteById(roleName);
        return true;
    }

    public List<Role> getRoles()
    {
        return roleRepository.findAll();
    }

}
