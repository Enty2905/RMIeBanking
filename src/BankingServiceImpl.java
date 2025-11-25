import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.stream.Collectors;

public class BankingServiceImpl extends UnicastRemoteObject implements BankingService {
    private Map<String, Account> accounts;
    private Map<String, User> users; // username -> User
    private List<Transaction> transactions;
    private Map<String, BankingCallback> callbacks; // accountNumber -> Callback
    private String accountsFile = "accounts.json";
    private String usersFile = "users.json";
    private String transactionsFile = "transactions.json";
    private int accountCounter = 10000;
    
    public BankingServiceImpl() throws RemoteException {
        super();
        accounts = new HashMap<>();
        users = new HashMap<>();
        transactions = new ArrayList<>();
        callbacks = new HashMap<>();
        loadAccounts();
        loadUsers();
        loadTransactions();
        
        // Tạo tài khoản mẫu nếu chưa có
        if (accounts.isEmpty()) {
            accounts.put("01234", new Account("01234", 5000));
            accounts.put("12345", new Account("12345", 10000));
            saveAccounts();
        }
    }
    
    @Override
    public String register(String username, String password, String fullName) throws RemoteException {
        if (username == null || username.trim().isEmpty()) {
            return "ERROR:Tên đăng nhập không được để trống";
        }
        if (password == null || password.trim().isEmpty()) {
            return "ERROR:Mật khẩu không được để trống";
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            return "ERROR:Họ tên không được để trống";
        }
        
        if (users.containsKey(username)) {
            return "ERROR:Tên đăng nhập đã tồn tại";
        }
        
        // Tạo số tài khoản mới
        String accountNumber = String.format("%05d", accountCounter++);
        while (accounts.containsKey(accountNumber)) {
            accountNumber = String.format("%05d", accountCounter++);
        }
        
        // Tạo user và account mới
        User user = new User(username, password, accountNumber, fullName);
        users.put(username, user);
        accounts.put(accountNumber, new Account(accountNumber, 0));
        
        saveUsers();
        saveAccounts();
        
        return "SUCCESS:" + accountNumber;
    }
    
    @Override
    public String login(String username, String password) throws RemoteException {
        if (username == null || password == null) {
            return "ERROR:Tên đăng nhập hoặc mật khẩu không đúng";
        }
        
        User user = users.get(username);
        if (user == null || !user.getPassword().equals(password)) {
            return "ERROR:Tên đăng nhập hoặc mật khẩu không đúng";
        }
        
        return "SUCCESS:" + user.getAccountNumber() + ":" + user.getFullName();
    }
    
    @Override
    public String queryAccount(String accountNumber) throws RemoteException {
        Account account = accounts.get(accountNumber);
        if (account == null) {
            return "Không tìm thấy tài khoản: " + accountNumber;
        }
        return "Số dư hiện tại: " + account.getBalance();
    }
    
    @Override
    public String deposit(String accountNumber, double amount) throws RemoteException {
        if (amount <= 0) {
            return "Số tiền không hợp lệ";
        }
        
        Account account = accounts.get(accountNumber);
        if (account == null) {
            return "Không tìm thấy tài khoản: " + accountNumber;
        }
        
        account.setBalance(account.getBalance() + amount);
        saveAccounts();
        
        // Lưu transaction
        Transaction transaction = new Transaction(accountNumber, "DEPOSIT", amount, "Nạp tiền vào tài khoản");
        transactions.add(transaction);
        saveTransactions();
        
        return "Đã nạp: " + amount + " Số dư hiện tại: " + account.getBalance();
    }
    
    @Override
    public String withdraw(String accountNumber, double amount) throws RemoteException {
        if (amount <= 0) {
            return "Số tiền không hợp lệ";
        }
        
        Account account = accounts.get(accountNumber);
        if (account == null) {
            return "Không tìm thấy tài khoản: " + accountNumber;
        }
        
        if (account.getBalance() < amount) {
            return "Số dư không đủ";
        }
        
        account.setBalance(account.getBalance() - amount);
        saveAccounts();
        
        // Lưu transaction
        Transaction transaction = new Transaction(accountNumber, "WITHDRAW", amount, "Rút tiền từ tài khoản");
        transactions.add(transaction);
        saveTransactions();
        
        return "Đã rút: " + amount + " Số dư hiện tại: " + account.getBalance();
    }
    
    @Override
    public String transfer(String fromAccount, String toAccount, double amount, String content) throws RemoteException {
        if (amount <= 0) {
            return "Số tiền không hợp lệ";
        }
        
        Account from = accounts.get(fromAccount);
        Account to = accounts.get(toAccount);
        
        if (from == null) {
            return "Không tìm thấy tài khoản gốc: " + fromAccount;
        }
        
        if (to == null) {
            return "Không tìm thấy tài khoản nhận: " + toAccount;
        }
        
        if (from.getBalance() < amount) {
            return "Số dư không đủ";
        }
        
        from.setBalance(from.getBalance() - amount);
        to.setBalance(to.getBalance() + amount);
        saveAccounts();
        
        // Lưu transactions cho cả 2 tài khoản
        Transaction transactionOut = new Transaction(fromAccount, "TRANSFER_OUT", amount, content, toAccount);
        Transaction transactionIn = new Transaction(toAccount, "TRANSFER_IN", amount, content, fromAccount);
        transactions.add(transactionOut);
        transactions.add(transactionIn);
        saveTransactions();
        
        // Gửi thông báo callback cho tài khoản nhận
        notifyTransferReceived(toAccount, fromAccount, amount, content, to.getBalance());
        
        return "Đã chuyển: " + amount + " đến tài khoản: " + toAccount + 
               " Số dư hiện tại: " + from.getBalance() + 
               " Nội dung: " + content;
    }
    
    /**
     * Gửi thông báo cho client khi nhận được chuyển khoản
     */
    private void notifyTransferReceived(String toAccount, String fromAccount, double amount, String content, double newBalance) {
        BankingCallback callback = callbacks.get(toAccount);
        if (callback != null) {
            try {
                callback.notifyTransferReceived(fromAccount, amount, content, newBalance);
            } catch (RemoteException e) {
                // Client có thể đã ngắt kết nối, xóa callback
                System.err.println("Lỗi gửi thông báo cho tài khoản " + toAccount + ": " + e.getMessage());
                callbacks.remove(toAccount);
            }
        }
    }
    
    @Override
    public void registerCallback(String accountNumber, BankingCallback callback) throws RemoteException {
        if (accountNumber != null && callback != null) {
            callbacks.put(accountNumber, callback);
            System.out.println("Đã đăng ký callback cho tài khoản: " + accountNumber);
        }
    }
    
    @Override
    public void unregisterCallback(String accountNumber) throws RemoteException {
        if (accountNumber != null) {
            callbacks.remove(accountNumber);
            System.out.println("Đã hủy đăng ký callback cho tài khoản: " + accountNumber);
        }
    }
    
    @Override
    public List<Transaction> getTransactionHistory(String accountNumber) throws RemoteException {
        return transactions.stream()
                .filter(t -> t.getAccountNumber().equals(accountNumber))
                .sorted((t1, t2) -> t2.getTimestamp().compareTo(t1.getTimestamp()))
                .collect(Collectors.toList());
    }
    
    private void loadAccounts() {
        try {
            File file = new File(accountsFile);
            if (!file.exists()) {
                return;
            }
            
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
            reader.close();
            
            if (json.length() == 0) {
                return;
            }
            
            String jsonStr = json.toString().trim();
            if (jsonStr.startsWith("{") && jsonStr.endsWith("}")) {
                jsonStr = jsonStr.substring(1, jsonStr.length() - 1);
                String[] entries = jsonStr.split("},");
                
                for (String entry : entries) {
                    entry = entry.trim();
                    if (entry.endsWith("}")) {
                        entry = entry.substring(0, entry.length() - 1);
                    }
                    
                    String accountNum = extractValue(entry, "accountNumber");
                    String balanceStr = extractValue(entry, "balance");
                    
                    if (accountNum != null && balanceStr != null) {
                        try {
                            double balance = Double.parseDouble(balanceStr);
                            accounts.put(accountNum, new Account(accountNum, balance));
                            // Cập nhật accountCounter
                            try {
                                int accNum = Integer.parseInt(accountNum);
                                if (accNum >= accountCounter) {
                                    accountCounter = accNum + 1;
                                }
                            } catch (NumberFormatException e) {
                                // Ignore
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("Lỗi parse balance: " + balanceStr);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Lỗi đọc file accounts: " + e.getMessage());
        }
    }
    
    private void saveAccounts() {
        try {
            FileWriter writer = new FileWriter(accountsFile);
            writer.write("{\n");
            int count = 0;
            for (Account account : accounts.values()) {
                if (count > 0) {
                    writer.write(",\n");
                }
                writer.write("  \"" + account.getAccountNumber() + "\": {\n");
                writer.write("    \"accountNumber\": \"" + account.getAccountNumber() + "\",\n");
                writer.write("    \"balance\": " + account.getBalance() + "\n");
                writer.write("  }");
                count++;
            }
            writer.write("\n}");
            writer.close();
        } catch (IOException e) {
            System.err.println("Lỗi ghi file accounts: " + e.getMessage());
        }
    }
    
    private void loadUsers() {
        try {
            File file = new File(usersFile);
            if (!file.exists()) {
                return;
            }
            
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
            reader.close();
            
            if (json.length() == 0) {
                return;
            }
            
            String jsonStr = json.toString().trim();
            if (jsonStr.startsWith("{") && jsonStr.endsWith("}")) {
                jsonStr = jsonStr.substring(1, jsonStr.length() - 1);
                String[] entries = jsonStr.split("},");
                
                for (String entry : entries) {
                    entry = entry.trim();
                    if (entry.endsWith("}")) {
                        entry = entry.substring(0, entry.length() - 1);
                    }
                    
                    String username = extractValue(entry, "username");
                    String password = extractValue(entry, "password");
                    String accountNumber = extractValue(entry, "accountNumber");
                    String fullName = extractValue(entry, "fullName");
                    
                    if (username != null && password != null && accountNumber != null) {
                        users.put(username, new User(username, password, accountNumber, fullName != null ? fullName : ""));
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Lỗi đọc file users: " + e.getMessage());
        }
    }
    
    private void saveUsers() {
        try {
            FileWriter writer = new FileWriter(usersFile);
            writer.write("{\n");
            int count = 0;
            for (User user : users.values()) {
                if (count > 0) {
                    writer.write(",\n");
                }
                writer.write("  \"" + user.getUsername() + "\": {\n");
                writer.write("    \"username\": \"" + user.getUsername() + "\",\n");
                writer.write("    \"password\": \"" + user.getPassword() + "\",\n");
                writer.write("    \"accountNumber\": \"" + user.getAccountNumber() + "\",\n");
                writer.write("    \"fullName\": \"" + user.getFullName() + "\"\n");
                writer.write("  }");
                count++;
            }
            writer.write("\n}");
            writer.close();
        } catch (IOException e) {
            System.err.println("Lỗi ghi file users: " + e.getMessage());
        }
    }
    
    private void loadTransactions() {
        try {
            File file = new File(transactionsFile);
            if (!file.exists()) {
                return;
            }
            
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
            reader.close();
            
            if (json.length() == 0) {
                return;
            }
            
            // Parse transactions từ JSON đơn giản
            // Format: {"transactions": [{"transactionId": "...", ...}, ...]}
            String jsonStr = json.toString().trim();
            if (jsonStr.contains("\"transactions\"")) {
                // Tìm mảng transactions
                int arrayStart = jsonStr.indexOf('[');
                int arrayEnd = jsonStr.lastIndexOf(']');
                if (arrayStart != -1 && arrayEnd != -1) {
                    String arrayContent = jsonStr.substring(arrayStart + 1, arrayEnd);
                    // Parse từng transaction
                    String[] transactionStrings = arrayContent.split("\\},\\s*\\{");
                    for (String transStr : transactionStrings) {
                        transStr = transStr.trim();
                        if (transStr.startsWith("{")) {
                            transStr = transStr.substring(1);
                        }
                        if (transStr.endsWith("}")) {
                            transStr = transStr.substring(0, transStr.length() - 1);
                        }
                        
                        String transactionId = extractValue(transStr, "transactionId");
                        String accountNumber = extractValue(transStr, "accountNumber");
                        String transactionType = extractValue(transStr, "transactionType");
                        String amountStr = extractValue(transStr, "amount");
                        String description = extractValue(transStr, "description");
                        String relatedAccount = extractValue(transStr, "relatedAccount");
                        String timestampStr = extractValue(transStr, "timestamp");
                        
                        if (accountNumber != null && transactionType != null && amountStr != null) {
                            try {
                                double amount = Double.parseDouble(amountStr);
                                Transaction transaction = new Transaction(accountNumber, transactionType, amount, 
                                    description != null ? description : "");
                                if (relatedAccount != null && !relatedAccount.isEmpty()) {
                                    transaction.setRelatedAccount(relatedAccount);
                                }
                                if (timestampStr != null && !timestampStr.isEmpty()) {
                                    try {
                                        long timestamp = Long.parseLong(timestampStr);
                                        transaction.setTimestamp(new Date(timestamp));
                                    } catch (NumberFormatException e) {
                                        // Ignore
                                    }
                                }
                                if (transactionId != null) {
                                    transaction.setTransactionId(transactionId);
                                }
                                transactions.add(transaction);
                            } catch (NumberFormatException e) {
                                // Ignore invalid amount
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Lỗi đọc file transactions: " + e.getMessage());
        }
    }
    
    private void saveTransactions() {
        try {
            FileWriter writer = new FileWriter(transactionsFile);
            writer.write("{\n  \"transactions\": [\n");
            for (int i = 0; i < transactions.size(); i++) {
                Transaction t = transactions.get(i);
                writer.write("    {\n");
                writer.write("      \"transactionId\": \"" + t.getTransactionId() + "\",\n");
                writer.write("      \"accountNumber\": \"" + t.getAccountNumber() + "\",\n");
                writer.write("      \"transactionType\": \"" + t.getTransactionType() + "\",\n");
                writer.write("      \"amount\": " + t.getAmount() + ",\n");
                writer.write("      \"description\": \"" + (t.getDescription() != null ? t.getDescription() : "") + "\",\n");
                writer.write("      \"relatedAccount\": \"" + (t.getRelatedAccount() != null ? t.getRelatedAccount() : "") + "\",\n");
                writer.write("      \"timestamp\": " + t.getTimestamp().getTime() + "\n");
                writer.write("    }");
                if (i < transactions.size() - 1) {
                    writer.write(",");
                }
                writer.write("\n");
            }
            writer.write("  ]\n}");
            writer.close();
        } catch (IOException e) {
            System.err.println("Lỗi ghi file transactions: " + e.getMessage());
        }
    }
    
    private String extractValue(String json, String key) {
        String searchKey = "\"" + key + "\"";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) {
            return null;
        }
        
        int colonIndex = json.indexOf(":", keyIndex);
        if (colonIndex == -1) {
            return null;
        }
        
        int startIndex = colonIndex + 1;
        while (startIndex < json.length() && (json.charAt(startIndex) == ' ' || json.charAt(startIndex) == '"')) {
            startIndex++;
        }
        
        int endIndex = startIndex;
        if (startIndex < json.length() && json.charAt(startIndex - 1) == '"') {
            while (endIndex < json.length() && json.charAt(endIndex) != '"') {
                endIndex++;
            }
        } else {
            while (endIndex < json.length() && 
                   json.charAt(endIndex) != ',' && 
                   json.charAt(endIndex) != '}' &&
                   json.charAt(endIndex) != '\n') {
                endIndex++;
            }
        }
        
        String value = json.substring(startIndex, endIndex);
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }
        return value.trim();
    }
}
