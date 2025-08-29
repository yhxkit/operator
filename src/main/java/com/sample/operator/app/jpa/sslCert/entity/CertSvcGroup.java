package com.sample.operator.app.jpa.sslCert.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "CertSvcGroup", schema = "test")
public class CertSvcGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int svcGroupIdx;

    @Column(columnDefinition = "varchar(255) character set utf8 collate utf8_bin")
    String svcGroupName;

    @Column(columnDefinition = "varchar(255) character set utf8 collate utf8_bin")
    String subSvcName;

    @Column(columnDefinition = "varchar(255) character set utf8 collate utf8_bin")
    String certPurpose;

    @ToString.Exclude //@Data 사용시 순환 참조 stackOverFlow 방지
    @OneToMany(mappedBy = "svcGroup", cascade = CascadeType.MERGE) // 영속성 persist 의 기준을 certList가 가지도록 함 
    List<SslCert> certList;

}
