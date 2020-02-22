package me.izhong.shop.util;

import me.izhong.common.util.MD5Util;
import org.apache.commons.lang3.RandomStringUtils;

public class PasswordUtils {
    private static final String DICT = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890abcdefghijklmnopqrstuvwxyz";
    public static String encrypt(String password, String salt) {
        return MD5Util.hash(password + salt);
    }

    public static String generateSalt(int num) {
        return RandomStringUtils.random(num, DICT);
    }
}


