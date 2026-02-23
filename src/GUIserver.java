/**********************************************************
 *Filename:			GUIserver.java
 *Package:			ironvault (currently default :( )
 *Project:			IronVault Group Project
 *Author:			Andrew Boyer
 *Section:			HCDD 311
 *Assignment:		Group Project
 *Description:		Logic for the GUI
 *Date Created:		2/21/2026
 *Date Modified:	1/22/2026
 *Modifier:			Andrew Boyer
 *Changes:				
 *
*********************************************************/
import javax.crypto.SecretKey;
import javax.swing.*;
import java.awt.*;
import java.awt.Event.*;
/*
 * TODO
 * ===========
 * Develop main UI window, to display and add passwords for storage
 * Create a window to change login password (will need to purge old UserRecord)
 * 
 * */
public class GUIserver {
	private Sprint2Database db; 
	private SecretKey vaultKey;
	
	public GUIserver() throws Exception {
		db = new Sprint2Database();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> db.clear_resources()));
		showLoginWindow();
	}
	
	private void showLoginWindow() {
		JFrame frame = new JFrame("Iron Vault - Login");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(350, 200);
		frame.setResizable(true);
		
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 10, 5, 10);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		
		// Username Field
		gbc.gridx = 0; gbc.gridy = 0;
		panel.add(new JLabel("Username:"), gbc);
		gbc.gridx = 1;
		JTextField usernameField = new JTextField(20);
		panel.add(usernameField, gbc);
		
		// Password Field
		gbc.gridx = 0; gbc.gridy = 1;
		panel.add(new JLabel("Password:"), gbc);
		gbc.gridx = 1;
		JPasswordField passwordField = new JPasswordField(16);
		panel.add(passwordField, gbc);
		
		// Status (pretty text that is green or red depending on what is happening pretty much)
		gbc.gridx = 0; gbc.gridy = 2;
		gbc.gridwidth = 2;
		JLabel statusLabel = new JLabel(" ", SwingConstants.CENTER);
		statusLabel.setForeground(Color.red);
		panel.add(statusLabel, gbc);
		
		// buttons
		gbc.gridy = 3;
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
		JButton loginButton = new JButton("Login");
		JButton registerButton = new JButton("Register");
		buttonPanel.add(loginButton);
		buttonPanel.add(registerButton);
		panel.add(buttonPanel, gbc);
		
		// login button logic
		loginButton.addActionListener(e -> {
			String username = usernameField.getText().trim();
			char[] password = passwordField.getPassword();
			if (username.isEmpty() || password.length == 0) {statusLabel.setText("Please Fill in all field."); return;}
			try {
				authService.UserRecord record = db.loadUser(username);
				vaultKey = authService.login(password, record);
				// authService.clearPassword(); -> not implemented yet, method would clear password from memory after use
				frame.dispose();
				showVaultWindow(username);
			} catch (Exception x) { // exception is x because ActionListener is e 
				statusLabel.setForeground(Color.red);
				statusLabel.setText("Username or password incorrect, login failed..");
			}});
		
		//register button logic
		registerButton.addActionListener(e -> {
			String username = usernameField.getText().trim();
			char[] password = passwordField.getPassword();
			if (username.isEmpty() || password.length == 0) {statusLabel.setText("Please Fill in all field."); return;}
			try {
				if (db.DoesUserExist(username)) {statusLabel.setForeground(Color.red); 
												 statusLabel.setText("User already exists"); return;}
				authService.UserRecord record = authService.register(password);
				// should add method to clear pass from memory --> authService.clearPassword();
				db.addUser(username, record);
				statusLabel.setForeground(Color.GREEN);
				statusLabel.setText("Registered successfully! Please login!");
			} catch (Exception x ) { // exception is x because ActionListener is e 
				statusLabel.setForeground(Color.red);
				statusLabel.setText("Registration failed: " + x.getMessage());
			}});
		
		frame.getRootPane().setDefaultButton(loginButton);
		frame.add(panel);
		frame.setVisible(true);
	}
	
	// main UI logic, this still has yet to be fully implemented
	private void showVaultWindow(String username) {
		JFrame frame = new JFrame("IronVault Manager - " + username);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(600, 400);
		frame.add(new JLabel("Welcome, " + username + "!", SwingConstants.CENTER));
		frame.setVisible(true);
	}
	
}
