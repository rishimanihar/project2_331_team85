import java.io.FileInputStream;
import java.util.Properties;

/*
CSCE 331
Database setup example
Purpose: Loading credentials from a .env file to keep them private.
*/
public final class dbSetup {
    public String user;
    public String pswd;

    public dbSetup() {
        Properties props = new Properties();
        try {
            // Looks for the .env file in the current directory
            props.load(new FileInputStream("login.env"));
            
            // Assigns the values to the variables your other files use
            this.user = props.getProperty("DB_USER");
            this.pswd = props.getProperty("DB_PASS");
            
        } catch (Exception e) {
            System.out.println("[ERROR] Could not load .env file");
            e.printStackTrace();
        }
    }
}