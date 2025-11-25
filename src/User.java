import java.io.Serializable;

public class User implements Serializable {
    private String username;
    private String password;
    private String accountNumber;
    private String fullName;
    
    public User() {
    }
    
    public User(String username, String password, String accountNumber, String fullName) {
        this.username = username;
        this.password = password;
        this.accountNumber = accountNumber;
        this.fullName = fullName;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}

