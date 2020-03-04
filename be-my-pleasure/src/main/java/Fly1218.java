import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.RandomUtil;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class Fly1218 {

    public static void main(String[] args) {

        Long ll = System.currentTimeMillis();
        String seed = System.currentTimeMillis()/100%1000000 +  RandomUtil.randomString(2);
        System.out.println(seed);
        String s = encryptDES("1", seed);
        System.out.println(s);
        System.out.println(System.currentTimeMillis() - ll);
        String mix = mix(s, RandomUtil.randomString(8) + seed);
        System.out.println(mix);
        String[] strings = deMix(mix);
        System.out.println(strings[0]);
        System.out.println(strings[1].substring(8));
        System.out.println(decryptDES(strings[0], strings[1].substring(8)));

        System.out.println(System.currentTimeMillis() - ll);

    }

    private static String mix(String cipher, String seed) {
        char[] cs = new char[32];
        for (int i = 0; i < 16; i ++) {
            cs[i*2] = seed.charAt(i);
            cs[i*2 + 1] = cipher.charAt(i);
        }
        return new String(cs);
    }

    private static String[] deMix(String cipher) {
        char[] originCipher = new char[16];
        char[] seed = new char[16];
        for (int i = 0; i < 16; i ++) {
            seed[i] = cipher.charAt(i*2);
            originCipher[i] = cipher.charAt(i*2 + 1);
        }
        return new String[]{new String(originCipher), new String(seed)};
    }

    private static byte[] iv = {1, 2, 3, 4, 5, 6, 7, 8};

    public static String encryptDES(String encryptString, String encryptKey) {
        try {
            IvParameterSpec zeroIv = new IvParameterSpec(iv);
            SecretKeySpec key = new SecretKeySpec(encryptKey.getBytes(StandardCharsets.UTF_8), "DES");
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key, zeroIv);
            byte[] encryptedData = cipher.doFinal(encryptString.getBytes(StandardCharsets.UTF_8));
            return HexUtil.encodeHexStr(encryptedData);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

    }

    /**
     * 解密
     */
    public static String decryptDES(String decryptString, String decryptKey) {
        try {
            byte[] byteMi = HexUtil.decodeHex(decryptString);
            IvParameterSpec zeroIv = new IvParameterSpec(iv);
            SecretKeySpec key = new SecretKeySpec(decryptKey.getBytes(), "DES");
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, zeroIv);
            byte decryptedData[] = cipher.doFinal(byteMi);

            return new String(decryptedData);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

}
