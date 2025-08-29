package com.sample.operator.app.common.crypt.dto;

import lombok.Builder;
import lombok.Data;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;

import java.io.ByteArrayInputStream;
import java.util.Base64;

@Data
@Builder
public class PgpDto
{
    String strKeyRing;
    boolean isPublicKey;

    public PGPPublicKeyRingCollection getPublicKeyCollection()
    {
        try {
            if (isPublicKey) {
                JcaKeyFingerprintCalculator calculator = new JcaKeyFingerprintCalculator();
                byte[] btArr = Base64.getDecoder().decode(strKeyRing);
                return new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(new ByteArrayInputStream(btArr)), calculator);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    public PGPSecretKeyRingCollection getSecretKeyCollection()
    {
        try{
            if(!isPublicKey)
            {
                JcaKeyFingerprintCalculator calculator = new JcaKeyFingerprintCalculator();
                byte[] btArr = Base64.getDecoder().decode(strKeyRing);
                return new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(new ByteArrayInputStream(btArr)), calculator);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}

