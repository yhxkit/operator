package com.sample.operator.app.jpa.account.repository;

import com.sample.operator.app.jpa.account.entity.Account;
import com.sample.operator.app.jpa.account.entity.Authority;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorityRepository extends JpaRepository<Authority, Integer> {
    int deleteAllByAccount(Account account);
}
