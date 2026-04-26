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
import javax.swing.table.*;

import org.hsqldb.error.Error;

import java.awt.*;
import java.sql.ResultSet;
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
				authService.UserRecord UserRecord = db.loadUser(username);
				vaultKey = authService.login(password, UserRecord);
				System.out.println("[?] GUI Server {debug}: Authenticated User, wrapped vault key: " + UserRecord.wrappedVaultKey);
				System.out.println("[?] GUI Server {debug}: User password hash (Base64 encoded): " + UserRecord.authHash);
				password = authService.clearPassword(password);
				frame.dispose();
				showVaultWindow(username, UserRecord, password);
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
				password = authService.clearPassword(password);
				
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
	
	// main UI logic
	private void showVaultWindow(String username, authService.UserRecord record, char[] loginPassword) {
		JFrame frame = new JFrame("IronVault Manager - " + username);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(700, 500);
		
		// define top bar, logout button and "+" to add passwords 
		JPanel topBar = new JPanel(new BorderLayout());
		JButton logOutButton = new JButton("logout");
		JButton addPassButton = new JButton("+");
		addPassButton.setFont(new Font("Arial", Font.BOLD, 18));
		addPassButton.setPreferredSize(new Dimension(45, 30));
	    topBar.add(logOutButton, BorderLayout.WEST);
	    topBar.add(new JLabel("IronVault - " + username, SwingConstants.CENTER), BorderLayout.CENTER);
	    topBar.add(addPassButton, BorderLayout.EAST);
	    topBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		
	    // Password Grid (this was much harder to do than I previously thought)
	    String[] columns = {"Site", "Username", "Password"};
	    DefaultTableModel TableModel = new DefaultTableModel(columns, 0) {
	    	public boolean isEditable(int row, int col) {return false;}
	    };
	    JTable table = new JTable(TableModel);
	    table.setRowHeight(28);
	    table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
	        public Component getTableCellRendererComponent(JTable t, Object val,
	                boolean sel, boolean foc, int row, int col) {
	            super.getTableCellRendererComponent(t, val, sel, foc, row, col);
	            setText("••••••••"); 
	            return this;
	    	}
	    });
	    
	    // status label incase of errors
	    JScrollPane scrollPane = new JScrollPane(table);
	    JLabel statusLabel = new JLabel(" ", SwingConstants.CENTER);
	    statusLabel.setForeground(Color.RED);
	    
	    frame.setLayout(new BorderLayout());
	    frame.add(topBar, BorderLayout.NORTH);
	    frame.add(scrollPane, BorderLayout.CENTER);
	    frame.add(statusLabel, BorderLayout.SOUTH);
		
	    int userID = db.getUserID(username);
	    loadVaultEntries(TableModel, userID, statusLabel);
	    
	    // on logout, clear vaultKey and push back to login screen
	    logOutButton.addActionListener(e -> {
	    	 System.out.println("[?] GUI Server: Vault Key set null, returning to login..");
	         vaultKey = null;
	         frame.dispose();
	         showLoginWindow();
	    });
		
	    addPassButton.addActionListener(e -> showAddEntryDialog(frame, TableModel, userID, statusLabel, vaultKey));
	    
	    table.addMouseListener(new java.awt.event.MouseAdapter() {
	    	public void mouseClicked(java.awt.event.MouseEvent e) {
			   if (e.getClickCount() == 2) {
				   int row = table.getSelectedRow();
				   if (row == -1) {return;}
				   String encPass = (String) TableModel.getValueAt(row, 2);
				   try {
					   String decPass = authService.decryptPassword(encPass, vaultKey);
					   JOptionPane.showMessageDialog(frame, 
							   						"Password: " + decPass, 
							   						(String) TableModel.getValueAt(row, 0),
							   						JOptionPane.INFORMATION_MESSAGE);
				   } catch (Exception err) {statusLabel.setText("Failed to decrypt password: " + err.getMessage());}
			   }
	    	}
	    });
	    
	    frame.getRootPane().setDefaultButton(logOutButton);
	    frame.setVisible(true);
	}
	
	// load all added passwords into the table model
	public void loadVaultEntries(DefaultTableModel tableModel, int userID, JLabel statusLabel) {
		tableModel.setRowCount(0);
		try {
			ResultSet rs = db.getVaultEntries(userID);
			if (rs == null) {statusLabel.setText("Failed to load vault entries."); return;}
			while (rs.next()) {
				tableModel.addRow(new Object[] {
						rs.getString("SiteName"),
						rs.getString("SiteUser"),
						rs.getString("SitePass"),
						rs.getInt("ID")
				});
			}
		} catch (Exception ex) {
			statusLabel.setText("Failed to get vault entries: " + ex.getMessage());
		}
	}
	
	public void showAddEntryDialog(JFrame parent, DefaultTableModel tableModel, int userID, JLabel statusLabel, SecretKey vaultKey) {
		JDialog dialog = new JDialog(parent, "Add Password", true);
		dialog.setSize(350, 220);
	    dialog.setLocationRelativeTo(parent);
	    dialog.setLayout(new GridBagLayout());

	    GridBagConstraints gbc = new GridBagConstraints();
	    gbc.insets = new Insets(5, 10, 5, 10);
	    gbc.fill = GridBagConstraints.HORIZONTAL;
	    
	    // site name field
	    gbc.gridx = 0; gbc.gridy = 0;
	    dialog.add(new JLabel("Site:"), gbc);
	    gbc.gridx = 1;
	    JTextField siteField = new JTextField(15);
	    dialog.add(siteField, gbc);
	    
	    // username field
	    gbc.gridx = 0; gbc.gridy = 1;
	    dialog.add(new JLabel("Username:"), gbc);
	    gbc.gridx = 1;
	    JTextField siteUserField = new JTextField(15);
	    dialog.add(siteUserField, gbc);
	    
	    // pass field
	    gbc.gridx = 0; gbc.gridy = 2;
	    dialog.add(new JLabel("Password:"), gbc);
	    gbc.gridx = 1;
	    JPasswordField sitePassField = new JPasswordField(15);
	    dialog.add(sitePassField, gbc);
	    
	    // status indicator 
	    gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
	    JLabel dialogStatus = new JLabel(" ", SwingConstants.CENTER);
	    dialogStatus.setForeground(Color.RED);
	    dialog.add(dialogStatus, gbc);
	    
	    // save info
	    gbc.gridy = 4;
	    JButton saveButton = new JButton("Save");
	    dialog.add(saveButton, gbc);
	    saveButton.addActionListener(e -> {
	    	String site = siteField.getText().trim();
	    	String username = siteUserField.getText().trim();
	    	char[] Pass = sitePassField.getPassword();
	    	// check that all fields are filled in
	    	if (site.isEmpty() || username.isEmpty() || Pass.length == 0) {
	    		dialogStatus.setText("All fields are required");
	    		return;}
	    	try {
	    		String encPass = authService.encryptPassword(new String(Pass), vaultKey);
	    		db.addSiteAccount(userID, site, username, encPass);
	    		
	    		// refresh vault entries
	    		loadVaultEntries(tableModel, userID, statusLabel);
	    		statusLabel.setForeground(Color.GREEN);
	    		statusLabel.setText("Added entry for: " + site);
	    		dialog.dispose();
	    	} catch (Exception err) {dialogStatus.setText("Failed to save: " + err.getMessage());}
	    });
	    
	    dialog.getRootPane().setDefaultButton(saveButton);
	    dialog.setVisible(true);
	}
	
}
