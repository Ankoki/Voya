package us.byeol.voya.auth;

import android.os.Build;

import androidx.annotation.RequiresApi;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiresApi(api = Build.VERSION_CODES.O)
public final class PasswordHasher {

    private static final int COST = 16, KEY_LENGTH = 128;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA1";
    private static final String IDENTIFIER = "$31$";
    private static final Pattern LAYOUT = Pattern.compile("\\$31\\$(\\d\\d?)\\$(.{43})");

    /**
     * Hashes a password against its salt using PBKDF2.
     *
     * @param password the password to hash.
     * @param salt the salt to use.
     * @param iterations the iteration count.
     * @return the hash.
     * @throws GeneralSecurityException if the algorithm is not found or the key specification is invalid.
     */
    private static byte[] hash0(char[] password, byte[] salt, int iterations) throws GeneralSecurityException {
        KeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
        return factory.generateSecret(spec).getEncoded();
    }

    private final SecureRandom random = new SecureRandom();

    /**
     * Hashes a password for storage.
     *
     * @param password the password to hash.
     * @return a secure authentication token.
     * @throws GeneralSecurityException if the algorithm is not found or the key specification is invalid.
     */
    public String hash(String password) throws GeneralSecurityException {
        return this.hash(password.toCharArray());
    }

    /**
     * Hashes a password for storage.
     *
     * @param password the password to hash.
     * @return a secure authentication token.
     * @throws GeneralSecurityException if the algorithm is not found or the key specification is invalid.
     */
    public String hash(char[] password) throws GeneralSecurityException {
        byte[] salt = new byte[KEY_LENGTH / 8];
        random.nextBytes(salt);
        byte[] dk = PasswordHasher.hash0(password, salt, 1 << COST);
        byte[] hash = new byte[salt.length + dk.length];
        System.arraycopy(salt, 0, hash, 0, salt.length);
        System.arraycopy(dk, 0, hash, salt.length, dk.length);
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        return IDENTIFIER + COST + '$' + encoder.encodeToString(hash);
    }

    /**
     * Checks if a given password matches a given token.
     *
     * @param password the local password to check.
     * @param token the token to check against.
     * @return true if they are the same.
     * @throws GeneralSecurityException if the algorithm is not found or the key specification is invalid.
     * @throws IllegalArgumentException if the token has an invalid format.
     */
    public boolean compare(String password, String token) throws GeneralSecurityException, IllegalArgumentException {
        return this.compare(password.toCharArray(), token);
    }

    /**
     * Checks if a given password matches a given token.
     *
     * @param password the local password to check.
     * @param token the token to check against.
     * @return true if they are the same.
     * @throws GeneralSecurityException if the algorithm is not found or the key specification is invalid.
     * @throws IllegalArgumentException if the token has an invalid format.
     */
    public boolean compare(char[] password, String token) throws GeneralSecurityException, IllegalArgumentException {
        Matcher matcher = LAYOUT.matcher(token);
        if (!matcher.matches())
            throw new IllegalArgumentException("The token '" + token + "' has an invalid format.");
        int iterations = 1 << (Integer.parseInt(matcher.group(1)));
        byte[] hash = Base64.getUrlDecoder().decode(matcher.group(2));
        byte[] salt = Arrays.copyOfRange(hash, 0, KEY_LENGTH / 8);
        byte[] check = PasswordHasher.hash0(password, salt, iterations);
        int zero = 0;
        for (int id = 0; id < check.length; id++)
            zero |= hash[salt.length + id] ^ check[id];
        return zero == 0;
    }

}
