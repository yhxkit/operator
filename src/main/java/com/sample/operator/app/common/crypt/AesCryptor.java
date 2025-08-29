package com.sample.operator.app.common.crypt;

import com.sample.operator.app.common.crypt.dto.AesDto;
import com.sample.operator.app.common.crypt.spec.AesCryptSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class AesCryptor implements BaseCryptor{

    private final AesCryptSpec aesCryptSpec;

    @Override
    public String encrypt(String data) {
        return "";
    }

    @Override
    public String decrypt(String data) {
        return "";
    }

    public String encrypt(String data, AesDto aesDto) throws InvalidAlgorithmParameterException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException {
        SecretKey secretKey = new SecretKeySpec(Base64.getUrlDecoder().decode(aesDto.getAes256Key()), aesCryptSpec.getAlgorithm());
        Cipher cipher =Cipher.getInstance(aesCryptSpec.getCipherFormat());
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(Base64.getUrlDecoder().decode(aesDto.getAes256Iv())));
        byte[] encrypted = cipher.doFinal(data.getBytes());
        return new String(Base64.getUrlEncoder().encode(encrypted));
    }

    public String decrypt(String data, AesDto aesDto) throws InvalidAlgorithmParameterException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException {
        SecretKey secretKey = new SecretKeySpec(Base64.getUrlDecoder().decode(aesDto.getAes256Key()), aesCryptSpec.getAlgorithm());
        Cipher cipher =Cipher.getInstance(aesCryptSpec.getCipherFormat());
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(Base64.getUrlDecoder().decode(aesDto.getAes256Iv())));
        byte[] btArr = cipher.doFinal(Base64.getUrlDecoder().decode(data));
        return new String(btArr);
    }
}
