package com.yaoxin.appbase.utils;

import android.util.Base64;

import com.yaoxin.appbase.net.Constant;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 描述 :
 * 作者 :  TLJ
 * 时间 :  2019-08-26 09:44
 */
public class AESUtil {
    //-- 算法/模式/填充
    private static final String CipherMode = "AES/ECB/PKCS5Padding";//

    /**
     * AES加密,返回BASE64编码后的加密字符串
     *
     * @param sSrc           -- 待加密内容
//     * @param encodingFormat -- 字符串编码方式
//     * @param algorithm      -- 使用的算法 算法/模式/补码方式, 目前支持ECB和CBC模式
     * @return Base64编码后的字符串
     * @throws Exception
     */
    public static String aesEncrypt(String sSrc) throws Exception {
        Cipher cipher = Cipher.getInstance(CipherMode);
        byte[] raw = Constant.ENCODE_KEY.getBytes("UTF-8");
        SecretKeySpec skeySpec = new SecretKeySpec(raw, CipherMode);
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(sSrc.getBytes("UTF-8"));
        return new String(Base64.encode(encrypted, Base64.DEFAULT));
    }

    public static String aseDecrypt(String strToDecrypt) throws Exception {
        Cipher cipher = Cipher.getInstance(CipherMode);
        byte[] raw = Constant.ENCODE_KEY.getBytes("UTF-8");
        SecretKeySpec skeySpec = new SecretKeySpec(raw, CipherMode);
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decodedBytes = Base64.decode(strToDecrypt, Base64.DEFAULT);
        byte[] decrypted = cipher.doFinal(decodedBytes);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    public static String msgAesEncrypt(String sSrc) throws Exception {
        Cipher cipher = Cipher.getInstance(CipherMode);
        byte[] raw = Constant.MSG_ENCODE_KEY.getBytes("UTF-8");
        SecretKeySpec skeySpec = new SecretKeySpec(raw, CipherMode);
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(sSrc.getBytes("UTF-8"));
        return new String(Base64.encode(encrypted, Base64.DEFAULT));
    }

    public static String msgAseDecrypt(String strToDecrypt) throws Exception {
        if (containsChineseCharacters(strToDecrypt)) {
            return strToDecrypt;
        }

        Cipher cipher = Cipher.getInstance(CipherMode);
        byte[] raw = Constant.MSG_ENCODE_KEY.getBytes("UTF-8");
        SecretKeySpec skeySpec = new SecretKeySpec(raw, CipherMode);
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decodedBytes = Base64.decode(strToDecrypt, Base64.DEFAULT);
        byte[] decrypted = cipher.doFinal(decodedBytes);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    /** 聊天消息解密，失败时返回原文 */
    public static String safeMsgDecrypt(String text) {
        if (text == null || text.isEmpty()) {
            return text == null ? "" : text;
        }
        try {
            return msgAseDecrypt(text);
        } catch (Exception e) {
            return text;
        }
    }

    public static boolean isChineseCharacter(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT;
    }

    public static boolean containsChineseCharacters(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        for (char c : str.toCharArray()) {
            if (isChineseCharacter(c)) {
                return true;
            }
        }
        return false;
    }

}
