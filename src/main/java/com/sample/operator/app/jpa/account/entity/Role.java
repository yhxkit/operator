package com.sample.operator.app.jpa.account.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Entity // jpa 사용
@Table(name="Role", schema = "test")
public class Role {
    @Id
    @Column(columnDefinition = "varchar(255) character set utf8 collate utf8_bin", nullable = false)
    String roleName;
}
