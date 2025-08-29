package com.sample.operator.app.jpa.pgp.entity;

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
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "PgpKeyRing", schema = "test")
public class PgpKeyRing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int pgpIdx;

    @Column(columnDefinition = "varbinary(30000)")
    byte[] pgpPubKeyRing;

    @Column(columnDefinition = "varbinary(30000)")
    byte[] pgpPrivKeyRing;


    @Column(columnDefinition = "varchar(255) character set utf8 collate utf8_bin")
    String comment;

    @Column(columnDefinition = "varchar(255) character set utf8 collate utf8_bin")
    String uploaderId;

    @CreatedDate
    LocalDateTime registered;
}
