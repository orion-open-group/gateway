package com.orion.mobile.gateway.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.security.SecureRandom;

/**
 * @Description TODO
 * @Author potsmart
 * @Date 2019/7/25 10:25
 * @Version 1.0.0
 */
public class GatewayEncrypt {
    static String key = "abcid12348";
    public static String encrypt(String content) {
        try {
            SecureRandom random = new SecureRandom();
            DESKeySpec desKey = new DESKeySpec(key.getBytes());
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey securekey = keyFactory.generateSecret(desKey);
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE, securekey, random);
            byte[] result = cipher.doFinal(content.getBytes());
            Base64 base64 = new Base64();
            String s = base64.encodeAsString(result);
            return s;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解密
     *
     * @param content 待解密内容
     * @return
     */
    public static String decrypt(String content) {
        try {
            byte[] decode = new Base64().decode(content);
            SecureRandom random = new SecureRandom();
            DESKeySpec desKey = new DESKeySpec(key.getBytes());
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey securekey = keyFactory.generateSecret(desKey);
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.DECRYPT_MODE, securekey, random);
            byte[] result = cipher.doFinal(decode);
            return new String(result);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        String databaseUrl="jdbc:mysql://120.53.118.185:3306/gateway?serverTimezone=GMT%2B8&useUnicode=true&characterEncoding=utf-8";
        String userName="root";
        String password="ai@2020sp";

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("url",databaseUrl);
        jsonObject.put("user",userName);
        jsonObject.put("pwd",password);
        String encrypt = encrypt(jsonObject.toJSONString());
        System.out.println(encrypt);
        String decrypt = decrypt("QXvGICkPhVZvy6M1e4zGX+iDbliDSjlvBPgiL17iAuxMoMHkx0lckTeqvc7aDUHvumR/uL1pSW2mFBhA9y/Ht50uueV2aumNJLGd00M6w7F5suvPYaVrP3xgiYz18JM6xV5Lmz0GiPhbaAxJzNt6AmLaJcwDLQDqtdBO6oKs8SCyhqBkqvPOHkQ4sMLEhFs9D08S3GhsX10=\n");
        System.out.println(decrypt);

    }
}
