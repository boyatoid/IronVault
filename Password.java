public class Password {
	private String pass;
	public int charRequirment;
	public boolean CheckPass;
	
	Password() {
		pass = null;
		charRequirment = 16;
	}
	
	Password(String Passw) {
		pass = Passw;
		charRequirment = 16;
	}
	
	public void setPass(String newPass) {this.pass = newPass;}
	public String getPass() {return pass;}
	public void setCheckFlag(boolean flag) {CheckPass = flag;}
	public boolean getCheckFlag() {return CheckPass;}
}

