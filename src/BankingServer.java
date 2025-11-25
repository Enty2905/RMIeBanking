import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.net.InetAddress;

public class BankingServer {
    public static void main(String[] args) {
        try {
            // Lấy địa chỉ IP của máy
            String hostIP = InetAddress.getLocalHost().getHostAddress();
            String hostName = InetAddress.getLocalHost().getHostName();
            
            // Cấu hình để RMI server có thể nhận kết nối từ xa
            System.setProperty("java.rmi.server.hostname", hostIP);
            
            // Kiểm tra xem registry đã tồn tại chưa
            Registry registry;
            try {
                registry = LocateRegistry.getRegistry(1099);
                registry.list(); // Test connection
                System.out.println("RMI Registry đã tồn tại trên port 1099");
            } catch (Exception e) {
                // Tạo RMI registry mới trên port 1099
                registry = LocateRegistry.createRegistry(1099);
                System.out.println("RMI Registry đã được tạo mới trên port 1099");
            }
            
            // Tạo và đăng ký service
            BankingService bankingService = new BankingServiceImpl();
            
            // Bind với cả localhost và IP thực
            Naming.rebind("rmi://localhost/BankingService", bankingService);
            Naming.rebind("rmi://" + hostIP + "/BankingService", bankingService);
            
            System.out.println("================================================");
            System.out.println("   🏦 BANKING SERVER ĐÃ SẴN SÀNG!");
            System.out.println("================================================");
            System.out.println("📍 Tên máy: " + hostName);
            System.out.println("📍 Địa chỉ IP: " + hostIP);
            System.out.println("📍 Port: 1099");
            System.out.println("------------------------------------------------");
            System.out.println("📌 URL kết nối cho CLIENT cùng máy:");
            System.out.println("   rmi://localhost/BankingService");
            System.out.println("");
            System.out.println("📌 URL kết nối cho CLIENT máy khác:");
            System.out.println("   rmi://" + hostIP + "/BankingService");
            System.out.println("------------------------------------------------");
            System.out.println("⚠️  Để máy khác kết nối được, hãy:");
            System.out.println("   1. Tắt Firewall hoặc mở port 1099");
            System.out.println("   2. Đảm bảo 2 máy cùng mạng LAN/WiFi");
            System.out.println("   3. Trên máy client, sửa file config.properties:");
            System.out.println("      server.host=" + hostIP);
            System.out.println("================================================");
            System.out.println("Nhấn Ctrl+C để dừng server");
        } catch (Exception e) {
            System.err.println("Lỗi khởi động server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}

