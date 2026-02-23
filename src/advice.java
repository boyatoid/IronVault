
public class advice {
	
	public String adviceForChar;
	public String adviceForNum;
	public String adviceForSymbols;
	
	// private String newPassword;
	
	 public void giveAdvice(String password) {
	        System.out.println("Your password: "+password+" isn't strong enough.");
	        System.out.println("Here are some tips to make it better.");
	        System.out.println("-Make the length of your password at least 16 characters.");
	        System.out.println("-Add numbers and symbols into your password to make it more complex.");
	        System.out.println("-Capitalize some letters to strengthen your password.");
	    }
	public void passwordDetails(String password, String newPassword) {
	        System.out.println("Original Password: " + password);
	        System.out.println("New Password: "+newPassword);
	    }
}

