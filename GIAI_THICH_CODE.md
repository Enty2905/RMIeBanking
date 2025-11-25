# 📚 GIẢI THÍCH CHI TIẾT CODE DỰ ÁN RMI E-BANKING

## 📁 CẤU TRÚC DỰ ÁN

```
RMIBanking/
├── src/
│   ├── BankingService.java      ← Interface RMI (định nghĩa các method remote)
│   ├── BankingServiceImpl.java  ← Implement logic nghiệp vụ (SERVER)
│   ├── BankingServer.java       ← Khởi động server RMI
│   ├── BankingCallback.java     ← Interface callback (thông báo real-time)
│   ├── BankingClient.java       ← Client console (test)
│   ├── BankingClientGUI.java    ← Giao diện chính sau đăng nhập
│   ├── LoginFrame.java          ← Giao diện đăng nhập/đăng ký
│   ├── User.java                ← Model người dùng
│   ├── Account.java             ← Model tài khoản
│   ├── Transaction.java         ← Model giao dịch
│   └── Main.java                ← Entry point
├── config.properties            ← File cấu hình server
├── users.json                   ← Dữ liệu người dùng
├── accounts.json                ← Dữ liệu tài khoản
└── transactions.json            ← Lịch sử giao dịch
```

---

# 🔵 PHẦN 1: CÁC INTERFACE RMI

## 1.1. BankingService.java - Interface chính

```java
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface BankingService extends Remote {
    // ... các method
}
```

### 📌 Giải thích:

| Thành phần | Ý nghĩa |
|------------|---------|
| `extends Remote` | **BẮT BUỘC** - Đánh dấu interface này là Remote interface, có thể gọi từ xa qua mạng |
| `throws RemoteException` | **BẮT BUỘC** - Mọi method trong Remote interface phải throw RemoteException để xử lý lỗi mạng |

### 📌 Các method được định nghĩa:

```java
// 1. ĐĂNG KÝ TÀI KHOẢN MỚI
String register(String username, String password, String fullName) throws RemoteException;
// → Trả về "SUCCESS:số_tài_khoản" hoặc "ERROR:lý_do"

// 2. ĐĂNG NHẬP
String login(String username, String password) throws RemoteException;
// → Trả về "SUCCESS:số_tài_khoản:họ_tên" hoặc "ERROR:lý_do"

// 3. VẤN TIN SỐ DƯ
String queryAccount(String accountNumber) throws RemoteException;
// → Trả về thông tin số dư

// 4. NẠP TIỀN
String deposit(String accountNumber, double amount) throws RemoteException;
// → Cộng tiền vào tài khoản

// 5. RÚT TIỀN
String withdraw(String accountNumber, double amount) throws RemoteException;
// → Trừ tiền từ tài khoản (kiểm tra số dư)

// 6. CHUYỂN KHOẢN
String transfer(String fromAccount, String toAccount, double amount, String content) throws RemoteException;
// → Chuyển tiền giữa 2 tài khoản

// 7. LỊCH SỬ GIAO DỊCH
List<Transaction> getTransactionHistory(String accountNumber) throws RemoteException;
// → Lấy danh sách giao dịch

// 8. ĐĂNG KÝ CALLBACK (nhận thông báo real-time)
void registerCallback(String accountNumber, BankingCallback callback) throws RemoteException;

// 9. HỦY ĐĂNG KÝ CALLBACK
void unregisterCallback(String accountNumber) throws RemoteException;
```

---

## 1.2. BankingCallback.java - Interface Callback

```java
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BankingCallback extends Remote {
    void notifyTransferReceived(String fromAccount, double amount, 
                                String content, double newBalance) throws RemoteException;
}
```

### 📌 Giải thích:

**Callback Pattern trong RMI:**
- Thông thường: Client gọi Server (một chiều)
- Với Callback: Server có thể gọi ngược lại Client (hai chiều)

**Luồng hoạt động:**
```
1. Client A đăng nhập → Đăng ký callback với Server
2. Client B chuyển tiền cho A
3. Server xử lý chuyển khoản
4. Server GỌI NGƯỢC callback của A → "Bạn vừa nhận được tiền!"
5. Client A hiển thị popup thông báo
```

---

# 🟢 PHẦN 2: SERVER IMPLEMENTATION

## 2.1. BankingServiceImpl.java - Logic nghiệp vụ

```java
public class BankingServiceImpl extends UnicastRemoteObject implements BankingService {
    // ...
}
```

### 📌 Giải thích `UnicastRemoteObject`:

| Thành phần | Ý nghĩa |
|------------|---------|
| `extends UnicastRemoteObject` | Làm cho object này có thể được gọi từ xa qua RMI |
| Constructor phải gọi `super()` | Khởi tạo cơ chế remote cho object |

### 📌 Các biến thành viên quan trọng:

```java
private Map<String, Account> accounts;           // accountNumber → Account
private Map<String, User> users;                 // username → User  
private List<Transaction> transactions;          // Danh sách tất cả giao dịch
private Map<String, BankingCallback> callbacks;  // accountNumber → Callback

// ⭐ callbacks: Lưu trữ callback của các client đang online
// Khi có chuyển khoản, server sẽ tìm callback của người nhận và gọi nó
```

### 📌 Method ĐĂNG KÝ - Giải thích chi tiết:

```java
@Override
public String register(String username, String password, String fullName) throws RemoteException {
    // 1. VALIDATE INPUT - Kiểm tra dữ liệu đầu vào
    if (username == null || username.trim().isEmpty()) {
        return "ERROR:Tên đăng nhập không được để trống";
    }
    if (password == null || password.trim().isEmpty()) {
        return "ERROR:Mật khẩu không được để trống";
    }
    if (fullName == null || fullName.trim().isEmpty()) {
        return "ERROR:Họ tên không được để trống";
    }
    
    // 2. CHECK DUPLICATE - Kiểm tra username đã tồn tại chưa
    if (users.containsKey(username)) {
        return "ERROR:Tên đăng nhập đã tồn tại";
    }
    
    // 3. GENERATE ACCOUNT NUMBER - Tạo số tài khoản mới (5 chữ số)
    String accountNumber = String.format("%05d", accountCounter++);
    // String.format("%05d", 123) → "00123" (thêm số 0 ở đầu cho đủ 5 chữ số)
    
    // 4. CREATE USER & ACCOUNT - Tạo user và account mới
    User user = new User(username, password, accountNumber, fullName);
    users.put(username, user);
    accounts.put(accountNumber, new Account(accountNumber, 0)); // Số dư ban đầu = 0
    
    // 5. SAVE TO FILE - Lưu vào file JSON
    saveUsers();
    saveAccounts();
    
    return "SUCCESS:" + accountNumber;
}
```

### 📌 Method CHUYỂN KHOẢN - Quan trọng nhất:

```java
@Override
public String transfer(String fromAccount, String toAccount, double amount, String content) 
        throws RemoteException {
    
    // 1. VALIDATE - Kiểm tra số tiền
    if (amount <= 0) {
        return "Số tiền không hợp lệ";
    }
    
    // 2. GET ACCOUNTS - Lấy thông tin 2 tài khoản
    Account from = accounts.get(fromAccount);
    Account to = accounts.get(toAccount);
    
    // 3. CHECK EXISTS - Kiểm tra tài khoản tồn tại
    if (from == null) {
        return "Không tìm thấy tài khoản gốc: " + fromAccount;
    }
    if (to == null) {
        return "Không tìm thấy tài khoản nhận: " + toAccount;
    }
    
    // 4. CHECK BALANCE - Kiểm tra số dư
    if (from.getBalance() < amount) {
        return "Số dư không đủ";
    }
    
    // 5. TRANSFER - Thực hiện chuyển khoản
    from.setBalance(from.getBalance() - amount);  // Trừ tiền người gửi
    to.setBalance(to.getBalance() + amount);      // Cộng tiền người nhận
    saveAccounts();
    
    // 6. LOG TRANSACTIONS - Lưu lịch sử cho CẢ 2 tài khoản
    Transaction transactionOut = new Transaction(fromAccount, "TRANSFER_OUT", amount, content, toAccount);
    Transaction transactionIn = new Transaction(toAccount, "TRANSFER_IN", amount, content, fromAccount);
    transactions.add(transactionOut);
    transactions.add(transactionIn);
    saveTransactions();
    
    // 7. ⭐ NOTIFY RECIPIENT - Thông báo cho người nhận (CALLBACK)
    notifyTransferReceived(toAccount, fromAccount, amount, content, to.getBalance());
    
    return "Đã chuyển: " + amount + " đến tài khoản: " + toAccount;
}
```

### 📌 Method CALLBACK - Thông báo real-time:

```java
private void notifyTransferReceived(String toAccount, String fromAccount, 
                                    double amount, String content, double newBalance) {
    // Tìm callback của người nhận trong Map
    BankingCallback callback = callbacks.get(toAccount);
    
    if (callback != null) {
        try {
            // ⭐ GỌI NGƯỢC VỀ CLIENT - Server gọi method trên Client!
            callback.notifyTransferReceived(fromAccount, amount, content, newBalance);
        } catch (RemoteException e) {
            // Client có thể đã đóng app hoặc mất kết nối
            System.err.println("Lỗi gửi thông báo: " + e.getMessage());
            callbacks.remove(toAccount); // Xóa callback không còn hoạt động
        }
    }
}
```

---

## 2.2. BankingServer.java - Khởi động Server

```java
public static void main(String[] args) {
    try {
        // 1. LẤY IP THỰC - Bỏ qua VirtualBox, VMware
        String hostIP = getRealIP();
        
        // 2. ⭐ CẤU HÌNH RMI HOSTNAME - QUAN TRỌNG!
        System.setProperty("java.rmi.server.hostname", hostIP);
        // → Đây là IP mà Server sẽ trả về cho Client
        // → Nếu không set, có thể trả về IP sai (VirtualBox)
        
        // 3. TẠO RMI REGISTRY - "Danh bạ" để Client tra cứu
        Registry registry;
        try {
            registry = LocateRegistry.getRegistry(1099);
            registry.list(); // Test xem registry có hoạt động không
        } catch (Exception e) {
            // Chưa có registry → Tạo mới
            registry = LocateRegistry.createRegistry(1099);
        }
        
        // 4. TẠO SERVICE OBJECT
        BankingService bankingService = new BankingServiceImpl();
        
        // 5. ĐĂNG KÝ SERVICE VÀO REGISTRY
        Naming.rebind("rmi://localhost/BankingService", bankingService);
        Naming.rebind("rmi://" + hostIP + "/BankingService", bankingService);
        // → Đăng ký với tên "BankingService"
        // → Client sẽ tra cứu tên này để lấy reference đến service
        
        System.out.println("Server đã sẵn sàng tại: " + hostIP + ":1099");
        
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

### 📌 Giải thích `Naming.rebind()`:

```
┌─────────────────────────────────────────────────────────────┐
│                    RMI REGISTRY (Port 1099)                  │
│  ┌────────────────────┬──────────────────────────────────┐  │
│  │      Tên           │         Reference                 │  │
│  ├────────────────────┼──────────────────────────────────┤  │
│  │  "BankingService"  │  → BankingServiceImpl object     │  │
│  └────────────────────┴──────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
        ↑
        │ Naming.rebind("rmi://localhost/BankingService", service)
        │
    Server đăng ký service
```

---

# 🟡 PHẦN 3: CLIENT IMPLEMENTATION

## 3.1. LoginFrame.java - Kết nối đến Server

### 📌 Load cấu hình từ file:

```java
private static void loadConfig() {
    Properties props = new Properties();
    try {
        FileInputStream fis = new FileInputStream("config.properties");
        props.load(fis);
        fis.close();
        
        // Đọc các giá trị từ file
        SERVER_HOST = props.getProperty("server.host", "localhost");
        SERVER_PORT = Integer.parseInt(props.getProperty("server.port", "1099"));
        SERVICE_NAME = props.getProperty("service.name", "BankingService");
        
    } catch (IOException e) {
        // Nếu không có file, dùng giá trị mặc định
    }
}

// Tạo URL kết nối
public static String getServerURL() {
    return "rmi://" + SERVER_HOST + ":" + SERVER_PORT + "/" + SERVICE_NAME;
    // Ví dụ: "rmi://192.168.1.69:1099/BankingService"
}
```

### 📌 Kết nối đến Server:

```java
private void connectToServer() {
    new Thread(() -> {  // Chạy trong thread riêng để không block UI
        int retries = 0;
        String serverURL = getServerURL();
        
        while (retries < 5) {  // Thử 5 lần
            try {
                // ⭐ LOOKUP SERVICE - Tra cứu service từ Registry
                bankingService = (BankingService) Naming.lookup(serverURL);
                
                // Sau khi có bankingService, có thể gọi các method:
                // bankingService.login(...)
                // bankingService.deposit(...)
                // → Giống như gọi method local!
                
                System.out.println("Kết nối thành công!");
                return;
                
            } catch (Exception e) {
                retries++;
                Thread.sleep(2000);  // Đợi 2 giây rồi thử lại
            }
        }
    }).start();
}
```

### 📌 Giải thích `Naming.lookup()`:

```
┌───────────────────────────────────────────────────────────────┐
│  CLIENT                                                        │
│                                                                │
│  BankingService service = (BankingService)                     │
│      Naming.lookup("rmi://192.168.1.69:1099/BankingService");  │
│                          │                                     │
└──────────────────────────┼─────────────────────────────────────┘
                           │
                           │ 1. Tra cứu trong Registry
                           ↓
┌───────────────────────────────────────────────────────────────┐
│  RMI REGISTRY (192.168.1.69:1099)                             │
│                                                                │
│  "BankingService" → [Reference đến BankingServiceImpl]        │
│                           │                                    │
└───────────────────────────┼────────────────────────────────────┘
                           │
                           │ 2. Trả về Stub (proxy)
                           ↓
┌───────────────────────────────────────────────────────────────┐
│  CLIENT nhận được STUB                                         │
│                                                                │
│  service.deposit("12345", 1000);                               │
│      │                                                         │
│      │ 3. Stub serialize và gửi qua mạng                       │
│      ↓                                                         │
└───────────────────────────────────────────────────────────────┘
                           │
                           │ Network (TCP/IP)
                           ↓
┌───────────────────────────────────────────────────────────────┐
│  SERVER                                                        │
│                                                                │
│  BankingServiceImpl thực hiện deposit() thật sự               │
│      │                                                         │
│      │ 4. Trả kết quả về Client                                │
│      ↓                                                         │
└───────────────────────────────────────────────────────────────┘
```

---

## 3.2. BankingClientGUI.java - Implement Callback

### 📌 Tạo Callback Object:

```java
// Inner class implement BankingCallback
private class BankingCallbackImpl extends UnicastRemoteObject implements BankingCallback {
    
    public BankingCallbackImpl() throws RemoteException {
        super();  // ⭐ Export object để có thể nhận gọi từ xa
    }
    
    @Override
    public void notifyTransferReceived(String fromAccount, double amount, 
                                       String content, double newBalance) throws RemoteException {
        // ⭐ Method này được SERVER GỌI khi có người chuyển tiền đến
        
        SwingUtilities.invokeLater(() -> {  // Cập nhật UI trên EDT thread
            // Hiển thị popup thông báo
            String message = String.format(
                "Bạn đã nhận được %.2f VNĐ từ tài khoản %s\n" +
                "Nội dung: %s\n" +
                "Số dư mới: %.2f VNĐ",
                amount, fromAccount, content, newBalance
            );
            
            JOptionPane.showMessageDialog(
                BankingClientGUI.this,
                message,
                "Thông báo nhận tiền",
                JOptionPane.INFORMATION_MESSAGE
            );
            
            // Cập nhật số dư trên giao diện
            balanceLabel.setText(String.format("%.2f VNĐ", newBalance));
            
            // Làm mới lịch sử giao dịch
            loadTransactionHistory();
        });
    }
}
```

### 📌 Đăng ký Callback với Server:

```java
private void registerCallback() {
    try {
        // Tạo callback object
        callback = new BankingCallbackImpl();
        
        // ⭐ Đăng ký với server
        bankingService.registerCallback(currentAccount, callback);
        // → Server sẽ lưu callback này vào Map
        // → Khi có chuyển khoản đến, server sẽ gọi callback.notifyTransferReceived()
        
    } catch (RemoteException e) {
        System.err.println("Lỗi đăng ký callback: " + e.getMessage());
    }
}

// Hủy đăng ký khi đóng app
private void unregisterCallback() {
    try {
        bankingService.unregisterCallback(currentAccount);
    } catch (RemoteException e) {
        // Ignore
    }
}
```

---

# 🟣 PHẦN 4: MODEL CLASSES

## 4.1. User.java

```java
public class User implements Serializable {
    private String username;      // Tên đăng nhập
    private String password;      // Mật khẩu
    private String accountNumber; // Số tài khoản ngân hàng
    private String fullName;      // Họ tên đầy đủ
    
    // Constructor, getters, setters...
}
```

### 📌 Tại sao implement `Serializable`?

```
Serializable cho phép object được:
1. Truyền qua mạng (RMI gửi object từ Server → Client)
2. Lưu vào file
3. Clone object

Khi Client gọi: List<Transaction> history = service.getTransactionHistory("12345");
→ Server tạo List<Transaction>
→ RMI serialize thành bytes
→ Gửi qua mạng
→ Client deserialize thành List<Transaction>
```

## 4.2. Account.java

```java
public class Account implements Serializable {
    private String accountNumber;  // Số tài khoản (VD: "12345")
    private double balance;        // Số dư (VD: 5000000.0)
    
    // Constructor, getters, setters...
}
```

## 4.3. Transaction.java

```java
public class Transaction implements Serializable {
    private String transactionId;    // Mã giao dịch (VD: "TXN1732521234567")
    private String accountNumber;    // Tài khoản thực hiện
    private String transactionType;  // Loại: DEPOSIT, WITHDRAW, TRANSFER_IN, TRANSFER_OUT
    private double amount;           // Số tiền
    private String description;      // Nội dung
    private String relatedAccount;   // Tài khoản liên quan (với chuyển khoản)
    private Date timestamp;          // Thời gian giao dịch
    
    // Tạo mã giao dịch tự động
    private String generateTransactionId() {
        return "TXN" + System.currentTimeMillis();
        // VD: "TXN1732521234567"
    }
}
```

---

# 🔴 PHẦN 5: LUỒNG HOẠT ĐỘNG CHÍNH

## 5.1. Luồng ĐĂNG NHẬP:

```
┌─────────────────────────────────────────────────────────────────┐
│ 1. User nhập username, password trên LoginFrame                 │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ 2. Client gọi: bankingService.login(username, password)         │
│    → RMI gửi request qua mạng đến Server                        │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ 3. Server (BankingServiceImpl.login()):                         │
│    - Tìm user trong Map                                         │
│    - Kiểm tra password                                          │
│    - Trả về "SUCCESS:accountNumber:fullName" hoặc "ERROR:..."   │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ 4. Client nhận kết quả:                                         │
│    - Nếu SUCCESS: Mở BankingClientGUI, đăng ký callback         │
│    - Nếu ERROR: Hiển thị thông báo lỗi                          │
└─────────────────────────────────────────────────────────────────┘
```

## 5.2. Luồng CHUYỂN KHOẢN (với Callback):

```
┌────────────────┐                              ┌────────────────┐
│   CLIENT A     │                              │   CLIENT B     │
│  (Người gửi)   │                              │  (Người nhận)  │
└───────┬────────┘                              └───────┬────────┘
        │                                               │
        │ 1. Đã đăng nhập                               │ 1. Đã đăng nhập
        │    và đăng ký callback                        │    và đăng ký callback
        │                                               │
        │ 2. Nhập: toAccount=B                          │
        │         amount=500000                         │
        │         content="Trả tiền cơm"                │
        │                                               │
        │ 3. Gọi service.transfer(A, B, 500000, ...)    │
        │                    │                          │
        │                    ↓                          │
        │    ┌───────────────────────────────┐          │
        │    │           SERVER              │          │
        │    │                               │          │
        │    │ 4. Trừ tiền A: A.balance -= 500000       │
        │    │ 5. Cộng tiền B: B.balance += 500000      │
        │    │ 6. Lưu transaction            │          │
        │    │ 7. Tìm callback của B         │          │
        │    │ 8. Gọi: callbackB.notifyTransferReceived()
        │    │         (fromAccount=A,       │──────────┼───→ 9. B nhận thông báo
        │    │          amount=500000,       │          │      "Bạn nhận được
        │    │          content="Trả tiền")  │          │       500000 từ A"
        │    │                               │          │
        │    └───────────────────────────────┘          │
        │                    │                          │
        │←───────────────────┘                          │
        │ 10. A nhận kết quả:                           │
        │     "Chuyển thành công"                       │
        │                                               │
```

---

# 📊 PHẦN 6: TÓM TẮT CÔNG NGHỆ

| Công nghệ | File sử dụng | Mục đích |
|-----------|--------------|----------|
| **Remote Interface** | `BankingService.java`, `BankingCallback.java` | Định nghĩa method có thể gọi từ xa |
| **UnicastRemoteObject** | `BankingServiceImpl.java`, `BankingCallbackImpl` | Cho phép object được gọi từ xa |
| **Naming.rebind()** | `BankingServer.java` | Đăng ký service vào Registry |
| **Naming.lookup()** | `LoginFrame.java` | Tra cứu service từ Registry |
| **Serializable** | `User.java`, `Account.java`, `Transaction.java` | Cho phép truyền object qua mạng |
| **Callback Pattern** | `BankingCallback.java`, `BankingClientGUI.java` | Thông báo real-time từ Server → Client |
| **Swing** | `LoginFrame.java`, `BankingClientGUI.java` | Giao diện đồ họa |
| **Properties** | `LoginFrame.java`, `BankingServer.java` | Đọc file cấu hình |

---

# ✅ ĐIỂM NỔI BẬT CỦA PROJECT

1. **RMI Remote Method Invocation**: Gọi method từ xa như gọi local
2. **Callback Pattern**: Server gọi ngược Client để thông báo real-time
3. **Multi-client**: Nhiều client có thể kết nối cùng lúc
4. **Persistent Storage**: Dữ liệu được lưu vào file JSON
5. **Modern UI**: Giao diện đẹp với gradient, rounded corners
6. **Configurable**: Dễ dàng thay đổi IP server qua config file
7. **Error Handling**: Xử lý lỗi kết nối, validation input

---

**Đây là toàn bộ giải thích code của dự án RMI E-Banking! 🎉**
