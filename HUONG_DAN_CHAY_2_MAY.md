# 🏦 HƯỚNG DẪN CHẠY RMI BANKING TRÊN 2 MÁY KHÁC NHAU

## 📋 Yêu cầu
- Cả 2 máy phải cùng mạng LAN/WiFi
- Java JDK đã được cài đặt trên cả 2 máy

---

## 🖥️ MÁY 1 - CHẠY SERVER + CLIENT

### Bước 1: Compile tất cả file Java
```bash
cd c:\Users\nguye\IdeaProjects\RMIBanking
javac -d out src/*.java
```

### Bước 2: Chạy Server
```bash
cd c:\Users\nguye\IdeaProjects\RMIBanking
java -cp out BankingServer
```

Server sẽ hiển thị địa chỉ IP của máy, ví dụ:
```
================================================
   🏦 BANKING SERVER ĐÃ SẴN SÀNG!
================================================
📍 Địa chỉ IP: 192.168.1.100
📍 Port: 1099
...
```

**GHI NHỚ địa chỉ IP này để cấu hình cho máy 2!**

### Bước 3: Chạy Client (cửa sổ terminal khác)
```bash
cd c:\Users\nguye\IdeaProjects\RMIBanking
java -cp out LoginFrame
```

---

## 💻 MÁY 2 - CHỈ CHẠY CLIENT

### Bước 1: Copy toàn bộ thư mục project sang máy 2
- Copy folder `RMIBanking` sang máy của bạn

### Bước 2: Sửa file `config.properties`
Mở file `config.properties` và thay đổi:
```properties
# Thay localhost bằng IP của máy chạy server
server.host=192.168.1.100
server.port=1099
service.name=BankingService
```

### Bước 3: Compile (nếu chưa có folder out)
```bash
cd [đường dẫn đến RMIBanking]
javac -d out src/*.java
```

### Bước 4: Chạy Client
```bash
java -cp out LoginFrame
```

---

## ⚠️ XỬ LÝ LỖI THƯỜNG GẶP

### Lỗi 1: "Connection refused"
**Nguyên nhân:** Firewall chặn kết nối
**Giải pháp:**
1. **Trên máy Server (Windows):**
   - Mở Windows Defender Firewall
   - Chọn "Allow an app through firewall"
   - Thêm Java (java.exe và javaw.exe)
   - Hoặc tạm thời tắt Firewall để test

2. **Mở port 1099:**
   ```powershell
   # Chạy PowerShell với quyền Admin
   New-NetFirewallRule -DisplayName "RMI Server" -Direction Inbound -LocalPort 1099 -Protocol TCP -Action Allow
   ```

### Lỗi 2: "Host unreachable"
**Nguyên nhân:** 2 máy không cùng mạng
**Giải pháp:**
- Kiểm tra cả 2 máy có cùng mạng WiFi/LAN không
- Thử ping từ máy client đến máy server:
  ```bash
  ping 192.168.1.100
  ```

### Lỗi 3: "Class not found"
**Nguyên nhân:** Chưa compile hoặc thiếu file
**Giải pháp:** Compile lại tất cả file Java

---

## 🔧 KIỂM TRA KẾT NỐI MẠNG

### Trên máy Client, test kết nối đến Server:
```powershell
# Test ping
ping [IP_SERVER]

# Test port 1099
Test-NetConnection -ComputerName [IP_SERVER] -Port 1099
```

---

## 📂 CẤU TRÚC FILE CẦN COPY SANG MÁY 2

```
RMIBanking/
├── config.properties    ← SỬA FILE NÀY
├── out/                 ← Các file .class đã compile
│   ├── Account.class
│   ├── BankingCallback.class
│   ├── BankingClient.class
│   ├── BankingClientGUI.class
│   ├── BankingServer.class
│   ├── BankingService.class
│   ├── BankingServiceImpl.class
│   ├── LoginFrame.class
│   ├── LoginFrame$*.class
│   ├── BankingClientGUI$*.class
│   ├── Main.class
│   ├── Transaction.class
│   └── User.class
└── src/                 ← (Tùy chọn) Source code
```

**Lưu ý:** Nếu copy cả folder `out`, bạn không cần compile lại trên máy 2.

---

## 🎯 TÓM TẮT NHANH

| Máy | Làm gì | Cấu hình |
|-----|--------|----------|
| **Máy 1 (Server)** | Chạy `BankingServer` + `LoginFrame` | Mặc định |
| **Máy 2 (Client)** | Chỉ chạy `LoginFrame` | Sửa `config.properties` với IP của máy 1 |

---

## 📞 Liên hệ hỗ trợ
Nếu gặp vấn đề, hãy kiểm tra:
1. Server có đang chạy không?
2. IP trong config.properties có đúng không?
3. Firewall đã được cấu hình chưa?
4. 2 máy có cùng mạng không?
