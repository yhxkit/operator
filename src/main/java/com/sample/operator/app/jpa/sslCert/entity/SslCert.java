package com.sample.operator.app.jpa.sslCert.entity;


import com.sample.operator.app.dto.sslCert.SslCertInfoModel;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.security.Principal;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "SslCert", schema = "test")
public class SslCert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int certIdx;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "svcGroupIdx")
    CertSvcGroup svcGroup;

    @Column(columnDefinition = "varbinary(30000)")
    X509Certificate cert;

    @CreatedDate
    LocalDateTime registered;

    @Column(columnDefinition = "varchar(255) character set utf8 collate utf8_bin")
    String uploaderId;

    // 인증서 용도 가져오기
    public String getPurpose()
    {
        return this.svcGroup.getCertPurpose();
    }

    public SslCertInfoModel convertToSslCertModel()
    {
        X509Certificate x509 = this.cert;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String expire = sdf.format(x509.getNotAfter());

        Principal subjectDn = x509.getSubjectX500Principal();

        String cn = Arrays.stream(subjectDn.getName().split(",")).filter(str -> str.startsWith("CN=")).map(d -> d.substring(3)).findFirst().orElse(subjectDn.getName());
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        SslCertInfoModel cert = SslCertInfoModel.builder()
                .cn(cn)
                .expire(expire)
                .serialNumber(x509.getSerialNumber())
                .certType(getPurpose())
                .certIdx(getCertIdx())
                .svcGroupName(getSvcGroup().getSvcGroupName())
                .subSvc(getSvcGroup().getSubSvcName())
                .uploadedBy(uploaderId)
                .uploadedAt(registered.format(fmt))
                .build();

        return cert;
    }

}
