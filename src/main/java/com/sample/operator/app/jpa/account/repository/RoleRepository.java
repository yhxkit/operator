package com.sample.operator.app.jpa.account.repository;

import com.sample.operator.app.jpa.account.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, String> {
}
