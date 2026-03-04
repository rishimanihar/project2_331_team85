import java.io.FileInputStream;
import java.util.Properties;

/**
 * CSCE 331
 * Database setup configuration class.
 * Purpose: Handles the loading of database credentials from a configuration file
 * to ensure sensitive information like usernames and passwords are not hardcoded.
 */
public final class dbSetup {
    // The database username loaded from the environment file
    public String user;
    
    // The database password loaded from the environment file
    public String pswd;

    /**
     * Constructor for dbSetup.
     * Initializes the Properties object and attempts to load credentials
     * from 'login.env'.
     */
    public dbSetup() {
        Properties props = new Properties();
        try {
            // Attempt to load the properties file (login.env) from the file system
            props.load(new FileInputStream("login.env"));
            
            // Retrieve the DB_USER and DB_PASS properties and assign them to public fields
            this.user = props.getProperty("DB_USER");
            this.pswd = props.getProperty("DB_PASS");
            
        } catch (Exception e) {
            // Log an error message if the file cannot be found or read
            System.out.println("[ERROR] Could not load .env file");
            e.printStackTrace();
        }
    }
}