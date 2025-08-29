package com.sample.operator.app.dto.sslCert;

import lombok.Builder;
import lombok.Data;

import java.math.BigInteger;

@Data
@Builder
public class SslCertInfoModel
{
    int certIdx;

    String cn;
    String expire;
    String issuer;
    BigInteger serialNumber;

    String certType;
    String subSvc;

    String uploadedAt;
    String uploadedBy;

    String svcGroupName;

    public String getSerialNumber() {
        byte[] bt = serialNumber.toByteArray();

        StringBuilder sb = new StringBuilder();
        for(byte b : bt) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
