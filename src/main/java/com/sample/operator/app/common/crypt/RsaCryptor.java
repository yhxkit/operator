package com.sample.operator.app.common.crypt;

import com.sample.operator.app.common.crypt.spec.RsaCryptSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.security.PrivateKey;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class RsaCryptor implements BaseCryptor
{

    private final RsaCryptSpec rsaCryptSpec;

    @Override
    public String encrypt(String data) {
        return "";
    }

    @Override
    public String decrypt(String data) {
        return "";
    }


    public byte[] encrypt(String data, PrivateKey privateKey) {
        try{
            Cipher cipher = Cipher.getInstance(rsaCryptSpec.getCipherTransformation());
            cipher.init(Cipher.ENCRYPT_MODE, privateKey, rsaCryptSpec.getParamSpec());
            return cipher.doFinal(Base64.getUrlDecoder().decode(data));
        }catch (Exception e) {
            return null;
        }
    }

    public byte[] decrypt(String data, PrivateKey privateKey) {
        try{
            Cipher cipher = Cipher.getInstance(rsaCryptSpec.getCipherTransformation());
            cipher.init(Cipher.DECRYPT_MODE, privateKey, rsaCryptSpec.getParamSpec());
            return cipher.doFinal(Base64.getUrlDecoder().decode(data));
        }catch (Exception e) {
            return null;
        }
    }
}
