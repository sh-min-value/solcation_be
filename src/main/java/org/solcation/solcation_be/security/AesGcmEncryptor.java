package org.solcation.solcation_be.security;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

public class AesGcmEncryptor {
    private static final String CIPHER = "AES/GCM/NoPadding";
    private static final String KDF = "PBKDF2WithHmacSHA256"; //대칭 키 생성 함수
    private static final int TAG_LENGTH_BIT = 128; // GCM 태그 길이
    private static final String VERSION = "v1"; //버전

    private final AesGcmProperties props;
    private final SecureRandom secureRandom = new SecureRandom(); //iv/salt 생성 시 사용

    public AesGcmEncryptor(AesGcmProperties props) {
        this.props = props;

        if (props.passphrase() == null || props.passphrase().isBlank()) {
            throw new IllegalArgumentException("AES-GCM passphrase is empty. Provide via yml/env.");
        }
    }

    /**
     * 암호화
     * @param plain
     * @param aad(optional)
     * @return
     *
     * salt: 무작위로 생성(16B)
     * iv: 무작위로 생성(12B)
     * 키: passphrase + salt + iterations
     * 암호화: AES/GCM
     * 저장 형식: v1$B64(salt)$B64(iv)$B64(ciphertext+tag)
     */
    public String encrypt(String plain, String aad) {
        if(plain==null) return null;

        //salt 생성
        byte[] salt = randomBytes(props.saltLength());

        //PBKDF2(passphrase + salt + iterations)로 AES 비밀 키 생성
        SecretKey key = deriveKey(props.passphrase(), salt, props.iterations(), props.keyLength());

        //GCM용 IV 생성
        byte[] iv = randomBytes(props.ivLength());
        GCMParameterSpec gcm = new GCMParameterSpec(TAG_LENGTH_BIT, iv);

        try {
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, key, gcm);

            //AAD로 컨텍스트 결합 -> 복호 시 동일 aad 존재 필요 (optional)
            if (aad != null) cipher.updateAAD(aad.getBytes(StandardCharsets.UTF_8));

            //암호화
            byte[] ciphertext = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));


            //저장 포캣으로 직렬화
            String sSalt = b64(salt);
            String sIv   = b64(iv);
            String sData = b64(ciphertext);

            return VERSION + "$" + sSalt + "$" + sIv + "$" + sData;
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("AES-GCM encrypt failed", e);
        }
    }

    /**
     * AAD 없이 암호화
     * @param plain
     * @return
     */
    public String encrypt(String plain) {
        return encrypt(plain, null);
    }

    /**
     * 복호화
     * @param token
     * @param aad(optional)
     * @return
     *
     * 토큰 분해 -> salt/iv/data 추출
     * 동일 passphrase + salt로 키 재생성
     * 복호화: AES/GCM
     */
    public String decrypt(String token, String aad) {
        if (token == null) return null;

        //$기준 분해
        String[] parts = token.split("\\$");
        if (parts.length != 4 || !VERSION.equals(parts[0])) {
            throw new IllegalArgumentException("Invalid token format");
        }
        byte[] salt = db64(parts[1]);
        byte[] iv = db64(parts[2]);
        byte[] data = db64(parts[3]);

        //passphrase + salt로 키 재생성
        SecretKey key = deriveKey(props.passphrase(), salt, props.iterations(), props.keyLength());
        GCMParameterSpec gcm = new GCMParameterSpec(TAG_LENGTH_BIT, iv);

        try {
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, key, gcm);

            //aad 검증(optional)
            if (aad != null) cipher.updateAAD(aad.getBytes(StandardCharsets.UTF_8));

            //복호화
            byte[] plain = cipher.doFinal(data);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (AEADBadTagException badTag) {
            // AAD 불일치나 위/변조 시 여기로 옴
            throw new SecurityException("Ciphertext authentication failed", badTag);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("AES-GCM decrypt failed", e);
        }
    }

    /**
     * AAD 없이 복호화
     * @param token
     * @return
     */
    public String decrypt(String token) {
        return decrypt(token, null);
    }


    /**
     * PBKDF2WithHmacSHA256으로 pssphrase에서 AES 키 유도
     * @param passphrase
     * @param salt
     * @param iter
     * @param keyLenBit
     * @return
     */
    private SecretKey deriveKey(String passphrase, byte[] salt, int iter, int keyLenBit) {
        try {
            KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), salt, iter, keyLenBit);
            SecretKeyFactory f = SecretKeyFactory.getInstance(KDF);
            byte[] keyBytes = f.generateSecret(spec).getEncoded();
            return new SecretKeySpec(keyBytes, "AES");
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 랜덤 바이트 생성
     */
    private byte[] randomBytes(int n) {
        byte[] out = new byte[n];
        secureRandom.nextBytes(out);

        return out;
    }

    /**
     * Base64 인코딩/디코딩
     */
    private static String b64(byte[] b) { return Base64.getEncoder().encodeToString(b); }
    private static byte[] db64(String s) { return Base64.getDecoder().decode(s); }
}
