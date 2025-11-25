import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Transaction implements Serializable {
    private String transactionId;
    private String accountNumber;
    private String transactionType; // DEPOSIT, WITHDRAW, TRANSFER_IN, TRANSFER_OUT
    private double amount;
    private String description;
    private String relatedAccount; // For transfers
    private Date timestamp;
    
    public Transaction() {
        this.timestamp = new Date();
    }
    
    public Transaction(String accountNumber, String transactionType, double amount, String description) {
        this();
        this.transactionId = generateTransactionId();
        this.accountNumber = accountNumber;
        this.transactionType = transactionType;
        this.amount = amount;
        this.description = description;
    }
    
    public Transaction(String accountNumber, String transactionType, double amount, String description, String relatedAccount) {
        this(accountNumber, transactionType, amount, description);
        this.relatedAccount = relatedAccount;
    }
    
    private String generateTransactionId() {
        return "TXN" + System.currentTimeMillis();
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    public String getTransactionType() {
        return transactionType;
    }
    
    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getRelatedAccount() {
        return relatedAccount;
    }
    
    public void setRelatedAccount(String relatedAccount) {
        this.relatedAccount = relatedAccount;
    }
    
    public Date getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getFormattedTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return sdf.format(timestamp);
    }
    
    @Override
    public String toString() {
        String typeName = "";
        switch (transactionType) {
            case "DEPOSIT": typeName = "Nạp tiền"; break;
            case "WITHDRAW": typeName = "Rút tiền"; break;
            case "TRANSFER_IN": typeName = "Nhận chuyển khoản"; break;
            case "TRANSFER_OUT": typeName = "Chuyển khoản"; break;
        }
        
        String result = String.format("[%s] %s: %.2f VNĐ", getFormattedTimestamp(), typeName, amount);
        if (relatedAccount != null) {
            result += " - Tài khoản: " + relatedAccount;
        }
        if (description != null && !description.isEmpty()) {
            result += " - " + description;
        }
        return result;
    }
}

