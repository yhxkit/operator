package com.sample.operator.app.dto.pgp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;

import java.io.ByteArrayInputStream;
import java.util.Base64;

@Data
@AllArgsConstructor
public class PgpCollectionPair {

    PGPPublicKeyRingCollection publicKeyRingCollection;
    PGPSecretKeyRingCollection secretKeyRingCollection;

    PgpCollectionPair(String pubStr, String secStr)
    {
        try
        {
            JcaKeyFingerprintCalculator calculator = new JcaKeyFingerprintCalculator();
            byte[] pubBtArr = Base64.getDecoder().decode(pubStr);
            byte[] secBtArr = Base64.getDecoder().decode(secStr);

            PGPPublicKeyRingCollection pubRing = new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(new ByteArrayInputStream(pubBtArr)), calculator);
            PGPSecretKeyRingCollection secRing = new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(new ByteArrayInputStream(secBtArr)), calculator);

            this.publicKeyRingCollection = pubRing;
            this.secretKeyRingCollection = secRing;
        }catch (Exception e)
        {}
    }

}
