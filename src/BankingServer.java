import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Inet4Address;
import java.util.Enumeration;
import java.io.FileInputStream;
import java.util.Properties;

public class BankingServer {
    
    // Lấy IP thực của máy (bỏ qua VirtualBox, VMware, Loopback)
    private static String getRealIP() {
        try {
            // Thử đọc từ config trước
            try {
                Properties props = new Properties();
                props.load(new FileInputStream("config.properties"));
                String configIP = props.getProperty("server.host");
                if (configIP != null && !configIP.equals("localhost") && !configIP.isEmpty()) {
                    return configIP;
                }
            } catch (Exception e) {
                // Không có config, tiếp tục tìm IP tự động
            }
            
            // Tự động tìm IP thực
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                
                // Bỏ qua interface không hoạt động hoặc loopback
                if (!iface.isUp() || iface.isLoopback()) continue;
                
                // Bỏ qua VirtualBox, VMware, Docker
                String name = iface.getDisplayName().toLowerCase();
                if (name.contains("virtual") || name.contains("vmware") || 
                    name.contains("vbox") || name.contains("docker")) continue;
                
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    // Chỉ lấy IPv4
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        String ip = addr.getHostAddress();
                        // Ưu tiên IP 192.168.x.x hoặc 10.x.x.x (mạng LAN)
                        if (ip.startsWith("192.168.") || ip.startsWith("10.")) {
                            return ip;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Fallback
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "localhost";
        }
    }
    
    public static void main(String[] args) {
        try {
            // Lấy địa chỉ IP thực của máy
            String hostIP = getRealIP();
            String hostName = InetAddress.getLocalHost().getHostName();
            
            // QUAN TRỌNG: Cấu hình RMI server hostname
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
            try {
                Naming.rebind("rmi://localhost/BankingService", bankingService);
                System.out.println("✓ Đã bind service với localhost");
            } catch (Exception e) {
                System.err.println("⚠ Lỗi bind với localhost: " + e.getMessage());
            }
            
            try {
                Naming.rebind("rmi://" + hostIP + "/BankingService", bankingService);
                System.out.println("✓ Đã bind service với IP: " + hostIP);
            } catch (Exception e) {
                System.err.println("⚠ Lỗi bind với IP " + hostIP + ": " + e.getMessage());
            }
            
            System.out.println("================================================");
            System.out.println("   BANKING SERVER DA SAN SANG!");
            System.out.println("================================================");
            System.out.println("Ten may: " + hostName);
            System.out.println("Dia chi IP: " + hostIP);
            System.out.println("Port: 1099");
            System.out.println("------------------------------------------------");
            System.out.println("URL ket noi cho CLIENT cung may:");
            System.out.println("   rmi://localhost/BankingService");
            System.out.println("");
            System.out.println("URL ket noi cho CLIENT may khac:");
            System.out.println("   rmi://" + hostIP + "/BankingService");
            System.out.println("------------------------------------------------");
            System.out.println("Tren may CLIENT, sua file config.properties:");
            System.out.println("   server.host=" + hostIP);
            System.out.println("================================================");
            System.out.println("Nhan Ctrl+C de dung server");
        } catch (Exception e) {
            System.err.println("Loi khoi dong server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}

