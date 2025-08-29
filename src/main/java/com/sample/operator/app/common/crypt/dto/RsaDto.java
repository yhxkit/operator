package com.sample.operator.app.common.crypt.dto;

import lombok.Builder;
import lombok.Data;

import java.security.PrivateKey;

@Data
@Builder
public class RsaDto {
    public PrivateKey privateKey;
}
