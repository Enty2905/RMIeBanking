import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface BankingService extends Remote {
    /**
     * Đăng ký tài khoản mới
     */
    String register(String username, String password, String fullName) throws RemoteException;
    
    /**
     * Đăng nhập
     */
    String login(String username, String password) throws RemoteException;
    
    /**
     * Vấn tin tài khoản - Query account information
     */
    String queryAccount(String accountNumber) throws RemoteException;
    
    /**
     * Nạp tiền - Deposit money
     */
    String deposit(String accountNumber, double amount) throws RemoteException;
    
    /**
     * Rút tiền - Withdraw money
     */
    String withdraw(String accountNumber, double amount) throws RemoteException;
    
    /**
     * Chuyển khoản - Transfer money
     */
    String transfer(String fromAccount, String toAccount, double amount, String content) throws RemoteException;
    
    /**
     * Lấy lịch sử giao dịch
     */
    List<Transaction> getTransactionHistory(String accountNumber) throws RemoteException;
    
    /**
     * Đăng ký callback để nhận thông báo
     */
    void registerCallback(String accountNumber, BankingCallback callback) throws RemoteException;
    
    /**
     * Hủy đăng ký callback
     */
    void unregisterCallback(String accountNumber) throws RemoteException;
}

