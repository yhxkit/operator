package com.sample.operator.app.jpa.account.repository;

import com.sample.operator.app.jpa.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Integer> {
    Account findAccountByAccountName(String accountName);
    Account findAccountByAccountNameAndPassword(String accountName, String password);
    boolean existsAccountByAccountName(String accountName);
}
