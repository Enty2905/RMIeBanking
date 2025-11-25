/**
 * Main class - Chọn chạy Server hoặc Client
 * 
 * Để chạy Server: java BankingServer
 * Để chạy Client: java BankingClient
 */
public class Main {
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("server")) {
            BankingServer.main(args);
        } else {
            BankingClient.main(args);
        }
    }
}