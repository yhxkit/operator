package com.sample.operator.app.common.crypt.spec;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@Getter
public class AesCryptSpec {

    String algorithm = "AES";
    String cipherFormat = "AES/ECB/PKCS5Padding";
}
