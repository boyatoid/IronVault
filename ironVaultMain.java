
public class ironVaultMain {

	public static void main(String[] args) {
		
		Password pass = new Password(); 
		advice view = new advice();
		passwordModifier controller = new passwordModifier(pass, view);
		controller.updateView();
	}
	

}
