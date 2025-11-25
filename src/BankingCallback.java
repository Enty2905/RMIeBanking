import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface callback để client nhận thông báo từ server
 */
public interface BankingCallback extends Remote {
    /**
     * Nhận thông báo khi có tiền chuyển đến
     * @param fromAccount Số tài khoản người chuyển
     * @param amount Số tiền
     * @param content Nội dung chuyển khoản
     * @param newBalance Số dư mới sau khi nhận
     */
    void notifyTransferReceived(String fromAccount, double amount, String content, double newBalance) throws RemoteException;
}

