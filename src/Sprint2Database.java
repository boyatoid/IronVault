/**********************************************************
 *Filename:			Sprint2Database.java
 *Package:			ironvault (currently default :( )
 *Project:			IronVault Group Project
 *Author:			Josh Still
 *Section:			HCDD 311
 *Assignment:		Group Project
 *Description:		Logic for pushing data to the database
 *Date Created:		2/21/2026
 *Date Modified:	1/22/2026
 *Modifier:			Andrew Boyer
 *Changes:			made logic to accept UserRecord (authService.java) object information
 *					changed addUser, updateUser
 *					added loadUser, DoesUserExist, clear_resources
 *					removed close connection finally statements from addUser and updateUser, 
 *					added runtime shutdown condition to clear resource to keep database open
*********************************************************/
import java.sql.*;
import java.util.Base64;

import javax.swing.JOptionPane;

public class Sprint2Database {
	static final String DATABASE_URL = "jdbc:ucanaccess:///Users/thepwn3r/Desktop/IronVault/Sprint2Prototype.accdb";
	Connection connection = null;
	Statement statement = null;
	ResultSet resultSet = null;
	PreparedStatement insertNewUser = null;
	
	public Sprint2Database() {
		try {
			System.out.println("Starting Database Connection");
			connection = DriverManager.getConnection(DATABASE_URL);
			System.out.println("Create Statement");
			statement = connection.createStatement();
			String strSQL = "Select ID, UserName, Password, AuthSalt, AuthHash, KeySalt, WrappedKey from PLocker";
			System.out.println(strSQL);
			resultSet = statement.executeQuery(strSQL);
			System.out.println("resultSet returned");
			//Show the Data
			while(resultSet.next()) {
	//			JOptionPane.showMessageDialog(null, resultSet.getString(1));
	//			JOptionPane.showMessageDialog(null,resultSet.getString("LastName"));
			}
		}
		catch(SQLException sqlex) {
			JOptionPane.showMessageDialog(null,sqlex.getMessage(),"Database Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	
	public int addUser(String uName, authService.UserRecord record) {
		int result = 0;
		
		try {
			insertNewUser = connection.prepareStatement(
	                "INSERT INTO PLocker (UserName, AuthSalt, AuthHash, KeySalt, WrappedKey) VALUES (?, ?, ?, ?, ?)",
	                Statement.RETURN_GENERATED_KEYS);
			insertNewUser.setString(1, uName);
			insertNewUser.setString(2, Base64.getEncoder().encodeToString(record.authSalt));
			insertNewUser.setString(3, record.authHash);
			insertNewUser.setString(4, Base64.getEncoder().encodeToString(record.keySalt));
			insertNewUser.setString(5, record.wrappedVaultKey);
			
			result = insertNewUser.executeUpdate();
			
			if(result == 1) {
				JOptionPane.showMessageDialog(null, "Credential Insert Successful", uName, JOptionPane.INFORMATION_MESSAGE);
			}
			else {
				JOptionPane.showMessageDialog(null, "Credential Insert Failed", uName, JOptionPane.ERROR_MESSAGE);
			}
		}
		
		catch (SQLException sqlex){
			JOptionPane.showMessageDialog(null, sqlex.getMessage(), "Database Insert Failed", JOptionPane.ERROR_MESSAGE);
			result = 0;
		}
		
		return result;
	}
	
	public int updateUser(String uName, authService.UserRecord record, int id) {
		int result = 0;
		
		PreparedStatement updateUser = null;
		
		try {
			updateUser = connection.prepareStatement("UPDATE PLocker SET UserName = ?, AuthSalt = ?, AuthHash = ?, KeySalt = ?, WrappedKey = ? WHERE ID = ?");
			
			updateUser.setString(1, uName);
			updateUser.setString(2, Base64.getEncoder().encodeToString(record.authSalt));
			updateUser.setString(3, record.authHash);
			updateUser.setString(4, Base64.getEncoder().encodeToString(record.keySalt));
			updateUser.setString(5, record.wrappedVaultKey);
			updateUser.setInt(6, id);
			
			result = updateUser.executeUpdate();
			
			if(result == 1) {
				JOptionPane.showMessageDialog(null, "Credentail Update Successful", uName, JOptionPane.INFORMATION_MESSAGE);
			}
			else {
				JOptionPane.showMessageDialog(null, "Credential Update Failed", uName, JOptionPane.ERROR_MESSAGE);
			}
		}
		
		catch (SQLException sqlex){
			JOptionPane.showMessageDialog(null, sqlex.getMessage(), "Database Update Failed", JOptionPane.ERROR_MESSAGE);
			result = 0;
		}
		
		return result;
	}
	
	public authService.UserRecord loadUser(String uName) {
		try {
			PreparedStatement loadUser = connection.prepareStatement(
					"SELECT AuthSalt, AuthHash, KeySalt, WrappedKey FROM PLocker WHERE UserName = ?");
			loadUser.setString(1, uName);
			ResultSet rs = loadUser.executeQuery();
			if (!rs.next()) {
				JOptionPane.showMessageDialog(null, "User not found: " + uName, "Login Error", JOptionPane.ERROR_MESSAGE);
				return null;}
			
			return new authService.UserRecord(
	                Base64.getDecoder().decode(rs.getString("AuthSalt")),
	                rs.getString("AuthHash"),
	                Base64.getDecoder().decode(rs.getString("KeySalt")),
	                rs.getString("WrappedKey")
	            );

		} catch (SQLException sqlex) {
			JOptionPane.showMessageDialog(null, sqlex.getMessage(), "Database error in loadUser method", JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}
	
	public boolean DoesUserExist(String uName) {
		try {
			PreparedStatement doesUserExist = connection.prepareStatement("SELECT 1 FROM PLocker WHERE UserName = ?");
			doesUserExist.setString(1, uName);
			return doesUserExist.executeQuery().next();
		} catch (SQLException sqlex) {
			JOptionPane.showMessageDialog(null, sqlex.getMessage(), "Database error in DoesUserExist method", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}
	
	public void clear_resources() {
		try {
			System.out.println("Clearing resources...");
			if (statement != null) {statement.close();}
			if (connection != null) {connection.close();}
			System.out.println("Resources cleared..!");
		} catch (Exception sqlex) {
			JOptionPane.showMessageDialog(null, sqlex.getMessage(), "Database error in clear_resources method", JOptionPane.ERROR_MESSAGE);
		}
	}
}

