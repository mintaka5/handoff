package org.white5moke.handoff;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class Utilities {
    public static String randomID(int bytLength) {
        SecureRandom r = null;
        try {
            r = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        byte[] rBs = r.generateSeed(bytLength);

        return Hex.encodeHexString(rBs);
    }

    public static enum KeyWrapType {
        PUBLIC_RSA("RSA PUBLIC KEY"),
        PRIVATE_RSA("RSA PRIVATE KEY"),
        PRIVATE_EC("EC PRIVATE KEY"),
        PUBLIC_EC("PUBLIC KEY");

        private String type;

        private KeyWrapType(String t) {
            this.type = t;
        }

        @Override
        public String toString() {
            return type;
        }
    }

    public static String wrapKey(byte[] encBs, KeyWrapType t) {
        return String.format(
          "%s%s%s",
          StringUtils.repeat("-", 5) + "BEGIN " + t + StringUtils.repeat("-", 5) + "\r\n",
                Base64.getMimeEncoder().encodeToString(encBs),
                "\r\n" + StringUtils.repeat("-", 5) + "END " + t + StringUtils.repeat("-", 5)
        );
    }
}
