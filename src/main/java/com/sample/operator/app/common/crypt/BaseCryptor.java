package com.sample.operator.app.common.crypt;

public interface BaseCryptor {

    String encrypt(String data);
    String decrypt(String data);
}
