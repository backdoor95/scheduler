package com.fastcampus.minischeduler.core.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class AES256Utils {

    @Value("${my-env.aes256.key}")
    private String key; // 32byte
    @Value("${my-env.aes256.iv}")
    private String iv; // 16byte

    public static String alg;
    @Value("${my-env.aes256.alg}")
    public void setAlg(String alg) {
        AES256Utils.alg = alg;
    }

    // 암호화
    public String encryptAES256(String text) throws Exception {

        Cipher cipher = Cipher.getInstance(alg); // Cipher 객체 인스턴스화(Java에서는 PKCS#5 = PKCS#7이랑 동일)
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES"); // 비밀키
        IvParameterSpec ivParamSpec = new IvParameterSpec(iv.getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParamSpec); // Cipher 객체 초기화(암호화)

        byte[] encrypted = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(encrypted); // Base64 인코딩 3f
//        return Base64Utils.encodeToUrlSafeString(encrypted); // -16
    }

    // 복호화
    public String decryptAES256(String cipherText) throws Exception {

        Cipher cipher = Cipher.getInstance(alg); // Cipher 객체 인스턴스화(Java에서는 PKCS#5 = PKCS#7이랑 동일)
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES"); // 비밀키
        IvParameterSpec ivParamSpec = new IvParameterSpec(iv.getBytes());
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParamSpec); // Cipher 객체 초기화(복호화)

        byte[] decodedBytes = Base64.getDecoder().decode(cipherText); // Base64 디코딩 3f
//        byte[] decodedBytes = Base64Utils.decodeFromUrlSafeString(cipherText); // -16
        byte[] decrypted = cipher.doFinal(decodedBytes);

        return new String(decrypted, StandardCharsets.UTF_8);
    }
}
