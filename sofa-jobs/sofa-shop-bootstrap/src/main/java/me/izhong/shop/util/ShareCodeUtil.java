package me.izhong.shop.util;

import me.izhong.common.exception.BusinessException;

public class ShareCodeUtil {
    private static char[] USER_CODE_DIC = new char[]{'A','B','C','D','E','X','Y','Z','2','3','F','J','K','M','N','P','Q',
            'R','S','T','U','V','W','4','G','H','5','6','7','8','9'};

    public static String generateUserCode(Long id) {
        final int dicLength = USER_CODE_DIC.length;
        char[] buf = new char[6];
        int i = 6;
        while ((id / dicLength) > 0 && i>0) {
            int mode = (int) (id % dicLength);
            buf[--i] = USER_CODE_DIC[mode];
            id /= dicLength;
        }
        buf[--i] = USER_CODE_DIC[(int) (id % dicLength)];
        while (i>0) {
            buf[--i] = USER_CODE_DIC[0];
        }
        return new String(buf).toUpperCase();
    }

    public static Long decodeUserCode(String userCode) {
        Long id = 0L;
        final int dicLength = USER_CODE_DIC.length;
        char[] codes = userCode.toUpperCase().toCharArray();
        if (codes.length != 6) {
            return null;
        }
        for (int i = 0; i < codes.length; i++) {
            int ind = 0;
            for (int j = 0; j < dicLength; j++) {
                if (codes[i] == USER_CODE_DIC[j]) {
                    ind = j;
                    break;
                }
            }

            if (i > 0) {
                id = id * dicLength + ind;
            } else {
                id = Long.valueOf(ind);
            }
        }
        return id;
    }
}
