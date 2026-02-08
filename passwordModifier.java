
public class passwordModifier {
	
	public int length;
	public String listOfWordsForPassword;
	public Password pass;
	public advice view;
	
	public passwordModifier(Password passW, advice view) {
		this.pass = passW;
		this.view = view;
	}
	
	public int getPasswordLength() {
		return pass.getPass().length();
	}
	
	public static void insertModifierToPassword() {
		
	}
	
	public void updateView() {
		view.giveAdvice(pass.getPass());
	}
}
