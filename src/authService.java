/**********************************************************
 *Filename:			authService.java
 *Package:			ironvault (currently default :( )
 *Project:			IronVault Group Project
 *Author:			Andrew Boyer
 *Section:			HCDD 311
 *Assignment:		Group Project
 *Description:		Encryption logic for password storage and auth
 *Date Created:		2/21/2026
 *Date Modified:	1/22/2026
 *Modifier:			Andrew Boyer
 *Changes:				
 *
*********************************************************/
import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;
/*
 * TODO
 * ===========
 * Create method to clear used auth creds from memory (done ?)
 * 
 * */
public class authService {
	// encryption constants
	private static final int PBKDF2_ITERATIONS = 310_000; // OWASP min, makes passwords much harder to crack 
    private static final int KEY_LENGTH = 256; // length of private key
    private static final int SALT_LENGTH = 16; // length of salt on password hashes
    private static final int iv_length = 12; // iv length
 
    // creates a UserRecord object, attempting to create an simple way to keep user secretes together
    public static class UserRecord {
        public final byte[] authSalt; // random salt used for hashing the master password for login
        public final String authHash; // hashed master password used to compare and login
        public final byte[] keySalt; // salt used to generate the KeK (key encryption key) used to encrypt the master password
        public final String wrappedVaultKey; // the encrypted (via the KeK) master password
        
        public UserRecord(byte[] authSalt, String authHash, byte[] keySalt, String wrappedVaultKey) {
        	this.authSalt = authSalt;
        	this.authHash = authHash;
        	this.keySalt = keySalt;
        	this.wrappedVaultKey = wrappedVaultKey;
        }

		public UserRecord() {
			this.authSalt = null;
			this.authHash = null;
			this.keySalt = null;
			this.wrappedVaultKey = null;
		}
    	
    }
    
    
    // internals of encryption start
    private static String deriveAuthHash(char[] password, byte[] salt) throws Exception { // creating the hash used for comparison to login
    	SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
    	KeySpec spec = new PBEKeySpec(password, salt, PBKDF2_ITERATIONS, KEY_LENGTH);
    	byte[] hash = factory.generateSecret(spec).getEncoded();
    	return Base64.getEncoder().encodeToString(hash);
    }
    
    private static SecretKey deriveKey(char[] password, byte[] salt) throws Exception { // used to create the KeK for encrypting the mater password
    	SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
    	KeySpec spec = new PBEKeySpec(password, salt, PBKDF2_ITERATIONS, KEY_LENGTH);
    	return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }
    
    private static SecretKey generateVaultKey() throws Exception { // creates a random encryption key to encrypt password stored in vault
    	KeyGenerator keygen = KeyGenerator.getInstance("AES");
    	keygen.init(KEY_LENGTH);
    	return keygen.generateKey();
    }
    
    private static byte[] genSalt() { // used for salt generation (hardens the hashes)
    	byte[] salt = new byte[SALT_LENGTH];
    	new SecureRandom().nextBytes(salt);
    	return salt;
    }
    
    private static String wrapKey(SecretKey keyToWrap, SecretKey kek) throws Exception { // used to encrypt (wrap) the key used for password encryption
    	byte[] iv = new byte[iv_length];
    	new SecureRandom().nextBytes(iv);
    	Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
    	cipher.init(Cipher.WRAP_MODE, kek, new GCMParameterSpec(128, iv));
    	byte[] wrapped = cipher.wrap(keyToWrap);
    	byte[] combined = new byte[iv.length + wrapped.length];
    	System.arraycopy(iv, 0, combined, 0, iv.length);
    	System.arraycopy(wrapped, 0, combined, iv.length, wrapped.length);
    	return Base64.getEncoder().encodeToString(combined);
    }
    
    private static SecretKey unwrapKey(String wrappedKey, SecretKey kek) throws Exception { // used to decrypt (unwrap) the key used for password encryption
    	byte[] combined = Base64.getDecoder().decode(wrappedKey);
    	byte[] iv = new byte[iv_length];
    	byte[] wrapped = new byte[combined.length - iv_length];
    	System.arraycopy(combined, 0, iv, 0, iv_length);
    	System.arraycopy(combined, iv_length, wrapped, 0, wrapped.length);
    	Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
    	cipher.init(Cipher.UNWRAP_MODE, kek, new GCMParameterSpec(128, iv));
    	return (SecretKey) cipher.unwrap(wrapped, "AES", cipher.SECRET_KEY);
    }
    
    // register a new user
    public static UserRecord register(char[] masterPassword) throws Exception {
    	System.out.println("[!] Auth Service: Registering User...");
    	byte[] authSalt = genSalt();
    	String authHash = deriveAuthHash(masterPassword, authSalt);
    	SecretKey vaultKey = generateVaultKey();
    	byte[] keySalt = genSalt();
    	SecretKey kek = deriveKey(masterPassword, keySalt);
    	String wrappedVault = wrapKey(vaultKey, kek);
    	return new UserRecord(authSalt, authHash, keySalt, wrappedVault);
    }
    
    // login with existing user, throws exception if login failed
    public static SecretKey login(char[] masterPassword, UserRecord record) throws Exception {
    	System.out.println("[!] Auth Service: Login Running...");
    	String attemptedPass = deriveAuthHash(masterPassword, record.authSalt);
    	if (!MessageDigest.isEqual(Base64.getDecoder().decode(attemptedPass), 
    			Base64.getDecoder().decode(record.authHash))) {
    		throw new SecurityException("Invalid password, login failed...");
    		}
    	SecretKey kek = deriveKey(masterPassword, record.keySalt);
    	System.out.println("[!] Auth Service: User logged in..!");
    	return unwrapKey(record.wrappedVaultKey, kek);
    }
    
    public static String encryptPassword(String password, SecretKey masterPassword) throws Exception { // used to encrypt a password entering into the vault with the master key
    	byte[] iv = new byte[iv_length];
    	new SecureRandom().nextBytes(iv);
    	
    	Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
    	cipher.init(Cipher.ENCRYPT_MODE, masterPassword, new GCMParameterSpec(128, iv));
    	byte[] encryptedPass = cipher.doFinal(password.getBytes("UTF-8"));
    	
    	byte[] combined = new byte[iv_length + encryptedPass.length];
    	System.arraycopy(iv, 0, combined, iv_length, encryptedPass.length);
    	System.arraycopy(encryptedPass, 0, combined, iv.length, encryptedPass.length);
    	
    	return Base64.getEncoder().encodeToString(combined);
    }
    
    public static String decryptPassword(String encryptedPass, SecretKey masterPass) throws Exception { // used to decrypt a password "leaving" the vault
    	byte[] combined = Base64.getDecoder().decode(encryptedPass);
    	
    	byte[] iv = new byte[iv_length];
    	byte[] encrypted = new byte[combined.length - iv_length];
    	System.arraycopy(combined, 0, iv, 0, iv_length);
    	System.arraycopy(combined, iv_length, encrypted, 0, encrypted.length);
    	
    	Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
    	cipher.init(Cipher.DECRYPT_MODE, masterPass, new GCMParameterSpec(128, iv));
    	
    	return new String(cipher.doFinal(encrypted), "UTF-8");
    }
    
    // change login password without re-encrypting all passwords in vault
    public static UserRecord changePassword(char[] oldPass, char[] newPass, UserRecord record) throws Exception {
    	System.out.println("Fetching Vault Key");
    	SecretKey vaultKey = login(oldPass, record);
    	byte[] newKeySalt = genSalt();
    	SecretKey newKek = deriveKey(newPass, newKeySalt);
    	String newWrappedKey = wrapKey(vaultKey, newKek);
    	byte[] newAuthSalt = genSalt();
    	String newAuthHash = deriveAuthHash(newPass, newAuthSalt);
    	return new UserRecord(newAuthSalt, newAuthHash, newKeySalt, newWrappedKey);
    }
    
    // nullify used password variables and passes it back
    public static char[] clearPassword(char[] password) {
    	password = null;
    	System.out.println("[!] Auth Service: used password var nullifed.. returning..");
    	return password;
    }
    
}
