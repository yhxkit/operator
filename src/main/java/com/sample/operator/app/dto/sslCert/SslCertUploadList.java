package com.sample.operator.app.dto.sslCert;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SslCertUploadList
{
    List<SslCertUploadDto> list;
}
