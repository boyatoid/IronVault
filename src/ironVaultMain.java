import javax.swing.SwingUtilities;
import javax.crypto.*;

public class ironVaultMain {

	public static void main(String[] args) throws Exception {	

		SwingUtilities.invokeLater(() -> {
			try {
				new GUIserver();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

//=========Encryption login, register, and change password testing========
		/*
		char[] masterPass = "DummyPassword".toCharArray();
		authService.UserRecord record = authService.register(masterPass);
		authService.login(masterPass, record);
		System.out.println(record.wrappedVaultKey);
		System.out.println(record.keySalt);
		System.out.println(record.authHash);
		System.out.println(record.authSalt);
		Sprint2Database db = new Sprint2Database();
		String pass = "password";
		SecretKey masterKey = authService.unwrapKey(record.wrappedVaultKey, authService.deriveKey(masterPass, record.keySalt));
		String encpass = authService.encryptPassword(pass, masterKey);
		db.addSiteAccount(3, "bing", "username", encpass);
		System.out.print("[!] password added to database");
		*/
//=========================================================================
		/*
		System.out.println();
		
		char[] newPass = "NewPassword".toCharArray();
		authService.UserRecord newRecord = authService.changePassword(masterPass, newPass, record);
		authService.login(newPass, newRecord);
		System.out.println(newRecord.wrappedVaultKey);
		System.out.println(newRecord.keySalt);
		System.out.println(newRecord.authHash);
		System.out.println(newRecord.authSalt);
		*/
//=========================================================================
		/*
//=========View and controller testing logic========
		Password pass = new Password(); 
		advice view = new advice();
		passwordModifier controller = new passwordModifier(pass, view);
		controller.updateView();
//===================================================
		*/
	}
	

}
