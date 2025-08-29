package com.sample.operator.app.dto.sslCert;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SslCertUploadDto
{
    MultipartFile certFile;

    String svcName;
    String subSvc;
    String certType; // 고정 SS
}
