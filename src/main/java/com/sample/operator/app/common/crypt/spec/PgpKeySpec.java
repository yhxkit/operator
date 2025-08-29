package com.sample.operator.app.common.crypt.spec;

import lombok.Getter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.springframework.stereotype.Component;

@Component
@Getter
public class PgpKeySpec {

    BouncyCastleProvider bcProvider = new BouncyCastleProvider();
    JcaKeyFingerprintCalculator calculator = new JcaKeyFingerprintCalculator();

    String identity = "sample_operator";
    char[] passPhrase = "password".toCharArray();

    int keySize = 3072;
    String generatorAlgorithm = "RSA";

    long oneDaySec = 86400L;
    long expiryPeriod = oneDaySec * 365;
}
