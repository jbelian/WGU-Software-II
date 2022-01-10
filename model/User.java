package model;

/**
 *
 * @author Joseph
 */
public class User {
    private String username;
    private String password;
    private String locale;
    
    public void setUsername(String username) {
        this.username = username;
    }
    public String getUsername() {
        return username;
    }

    
    public void setPassword(String password) {
        this.password = password;
    }    
    public String getPassword() {
        return password;
    }
    

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }
}
