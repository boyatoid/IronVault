/**********************************************************
*Filename: PasswordValidator.java
*Package: ironvault
*Project: IronVault
*Imports: java.util.Scanner
*Author: Joshua Still
*Section: HCDD 311
*Description: Takes input for a password, 
*then checks if the input meets conditions to be added to database
*Date Created: 4/16/2026
*
*********************************************************/
import java.util.Scanner;

public class PasswordValidator {
	
	    
	    private static boolean isValidPassword(String password) { //Tests input to see if it is valid for test
	        
	    	if (password.length() < 12) {
	            return false;
	        }

	        boolean hasUppercase = false;
	        boolean hasNumber = false;
	        boolean hasSymbol = false;

	        
	        for (int i = 0; i < password.length(); i++) {
	            char c = password.charAt(i);      //checks each character index in the input. 
	            								  //"i" is the current position being returned.

	            if (Character.isUpperCase(c)) {
	                hasUppercase = true;
	            } 
	            
	            else if (Character.isDigit(c)) {
	                hasNumber = true;
	            } 
	            
	            else if (!Character.isLetterOrDigit(c)) {
	                hasSymbol = true;
	            }
	        }												//if statement makes sure every object
			   												//is true before returning

	        return hasUppercase && hasNumber && hasSymbol; 
	    }
	    public static void promptForValidPassword() { 	//Called in main
	        Scanner scanner = new Scanner(System.in);   //Takes input from user
	        String password;

	        while (true) {								//If the password is valid
	            System.out.print("Enter a password: ");
	            password = scanner.nextLine();

	            if (isValidPassword(password)) { 		//calls isValidPassword function to test the input
	                System.out.println("Password is valid!");
	                break;
	            } 
	            else {								
	                System.out.println("Invalid password. Requirements:");
	                System.out.println("- At least 12 characters");
	                System.out.println("- At least one uppercase letter");
	                System.out.println("- At least one number");
	                System.out.println("- At least one symbol");
	                System.out.println();
	            }
	        }

	        scanner.close();
	    }
	}