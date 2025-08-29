package com.sample.operator.app.jpa.account.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Entity // jpa 사용
@EntityListeners(AuditingEntityListener.class) // createdDate 용
@Table(name="Authority", schema = "test")
public class Authority {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int authIdx;

    @ManyToOne(targetEntity = Account.class, optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name="account", nullable = false)
    Account account;


    @ManyToOne(targetEntity = Role.class, optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name="role", nullable = false)
    Role role;

    @CreatedDate
    LocalDateTime createdAt;
}
