package data;

import lombok.Data;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * Class which helps with encrypting decrypting values
 */
@Data
public class EncryptorDecryptor {

    private static final String SECRET_KEY = "kljsdfl347!@q23asd!&";
    private static final String SALT_VALUE = "bvcb25y$5t*%£re";
    private static final byte[] IV = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    /**
     * Encrypts the given value
     *
     * @param valueToEncrypt value to encrypt
     * @return returns an encrypted value
     */
    public String encrypt(String valueToEncrypt) {
        try {

            IvParameterSpec ivspec = new IvParameterSpec(IV);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(SECRET_KEY.toCharArray(), SALT_VALUE.getBytes(), 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
            return Base64.getEncoder()
                    .encodeToString(cipher.doFinal(valueToEncrypt.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            System.out.println("Failed to encrypt " + e.getStackTrace());
        }
        return null;
    }


    /**
     * Decrypts the giving value
     *
     * @param valueToDecrypt value to decrypt
     * @return the decrypted value
     */
    public String decrypt(String valueToDecrypt) {
        try {
            IvParameterSpec ivspec = new IvParameterSpec(IV);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(SECRET_KEY.toCharArray(), SALT_VALUE.getBytes(), 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
            return new String(cipher.doFinal(Base64.getDecoder().decode(valueToDecrypt)));
        } catch (Exception e) {
            System.out.println("Failed to decrypt " + e.getStackTrace());
        }
        return null;
    }

}
