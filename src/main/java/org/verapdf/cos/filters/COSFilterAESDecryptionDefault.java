package org.verapdf.cos.filters;

import org.verapdf.as.filters.io.ASBufferingInFilter;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.COSKey;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * This filter decrypts data using AES cipher.
 *
 * @author Sergey Shemyakov
 */
public class COSFilterAESDecryptionDefault extends ASBufferingInFilter {

    private static final byte[] SALT_BYTES = new byte[]{0x73, 0x41, 0x6C, 0x54};

    private Cipher aes;

    /**
     * Constructor.
     *
     * @param stream        is stream with encrypted data.
     * @param objectKey     contains object and generation numbers from object
     *                      identifier for object that is being decrypted. If it is
     *                      direct object, objectKey is taken from indirect object
     *                      that contains it.
     * @param encryptionKey is encryption key that is calculated from user
     *                      password and encryption dictionary.
     */
    public COSFilterAESDecryptionDefault(ASInputStream stream, COSKey objectKey,
                                         byte[] encryptionKey)
            throws IOException, GeneralSecurityException {
        super(stream);
        initAES(objectKey, encryptionKey);
    }

    @Override
    public int read(byte[] buffer, int size) throws IOException {
        if (this.bufferSize() == 0) {
            int bytesFed = (int) this.feedBuffer(getBufferCapacity());
            if (bytesFed == -1) {
                return -1;
            }
        }
        byte[] encData = new byte[BF_BUFFER_SIZE];
        int encDataLength = this.bufferPopArray(encData, size);
        byte[] res = this.aes.update(encData, 0, encDataLength);
        System.arraycopy(res, 0, buffer, 0, encDataLength);
        return encDataLength;
    }

    private void initAES(COSKey objectKey, byte[] encryptionKey)
            throws IOException, GeneralSecurityException {
        byte[] objectKeyDigest =
                COSFilterRC4DecryptionDefault.getObjectKeyDigest(objectKey);
        byte[] encWithObjectKey = concatenate(encryptionKey, encryptionKey.length,
                objectKeyDigest, objectKeyDigest.length);
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(concatenate(encWithObjectKey, encWithObjectKey.length,
                SALT_BYTES, SALT_BYTES.length));
        byte[] resultEncryptionKey = md5.digest();
        int keyLength = Math.min(COSFilterRC4DecryptionDefault.MAXIMAL_KEY_LENGTH,
                resultEncryptionKey.length);
        SecretKey key = new SecretKeySpec(
                Arrays.copyOf(resultEncryptionKey, keyLength), "AES");
        IvParameterSpec initializingVector = new
                IvParameterSpec(getAESInitializingVector());
        this.aes = Cipher.getInstance("AES/CBC/PKCS5Padding");
        this.aes.init(Cipher.DECRYPT_MODE, key, initializingVector);
    }

    private byte[] getAESInitializingVector() throws IOException {
        byte[] initVector = new byte[16];
        if (this.getInputStream().read(initVector, 16) != 16) {
            throw new IOException("Can't initialize AES cipher: AES initializing" +
                    " vector is not fully read.");
        }
        return initVector;
    }
}
