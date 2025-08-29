package com.sample.operator.app.common.crypt.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AesDto
{
    String aes256Iv;
    String aes256Key;
}
