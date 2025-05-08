package fi.tuni.secprog.passwordmanager;

import javax.crypto.spec.SecretKeySpec;

public class AESKeyHolder {
    private static SecretKeySpec aesKey;

    public static void storeKey(SecretKeySpec key) {
        aesKey = key;
    }

    public static SecretKeySpec getKey() {
        return aesKey;
    }
    
    public static void clearKey() {
        if (aesKey != null) {
            byte[] keyData = aesKey.getEncoded();
            if (keyData != null) {
                for (int i = 0; i < keyData.length; i++) {
                    keyData[i] = 0;
                }
            }
        }
        aesKey = null;
    }
}
