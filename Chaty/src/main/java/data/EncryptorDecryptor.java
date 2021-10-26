package data;

import lombok.Data;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Properties;

/**
 * Class which helps with encrypting decrypting values
 */
@Data
public class EncryptorDecryptor {
    private static final String APPLICATION_PROPERTIES="application.properties";
    private static final byte[] IV = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    /**
     * Encrypts the given value
     *
     * @param valueToEncrypt value to encrypt
     * @return returns an encrypted value
     */
    public String encrypt(String valueToEncrypt) {
        try {
            // load application properties to retrieve key and salt
            InputStream steam = getClass().getClassLoader().getResourceAsStream(APPLICATION_PROPERTIES);
            Properties prop = new Properties();
            prop.load(steam);

            IvParameterSpec ivSpec = new IvParameterSpec(IV);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(prop.getProperty("key").toCharArray(), prop.getProperty("iv").getBytes(), 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
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
            InputStream steam = getClass().getClassLoader().getResourceAsStream(APPLICATION_PROPERTIES);
            Properties prop = new Properties();
            prop.load(steam);

            IvParameterSpec ivSpec = new IvParameterSpec(IV);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(prop.getProperty("key").toCharArray(), prop.getProperty("iv").getBytes(), 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            return new String(cipher.doFinal(Base64.getDecoder().decode(valueToDecrypt)));
        } catch (Exception e) {
            System.out.println("Failed to decrypt " + e.getStackTrace());
        }
        return null;
    }

}
