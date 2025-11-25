# HƯỚNG DẪN SETUP SERVER VÀ CLIENT

## 📌 Tình huống: Server chạy trên máy khác

### BƯỚC 1: Trên máy SERVER (IP: 192.168.1.69)

1. **Chạy Server:**
   ```bash
   java BankingServer
   ```

2. **Kiểm tra thông tin hiển thị:**
   - Server sẽ hiển thị IP thực của máy
   - Ví dụ: `Dia chi IP: 192.168.1.69`
   - Ghi lại IP này

3. **Kiểm tra Firewall:**
   - Mở Windows Firewall
   - Cho phép port **1099** (TCP)
   - Hoặc tạm thời tắt firewall để test

### BƯỚC 2: Trên máy CLIENT

1. **Tạo/sửa file `config.properties`** (trong thư mục gốc project):
   ```properties
   server.host=192.168.1.69
   server.port=1099
   service.name=BankingService
   ```
   ⚠️ **QUAN TRỌNG:** Thay `192.168.1.69` bằng IP thực của máy SERVER

2. **Chạy Client:**
   ```bash
   java BankingClient
   ```

### BƯỚC 3: Kiểm tra kết nối

1. **Ping từ Client đến Server:**
   ```bash
   ping 192.168.1.69
   ```
   Nếu ping thành công → Mạng OK

2. **Kiểm tra port 1099:**
   ```bash
   telnet 192.168.1.69 1099
   ```
   Nếu kết nối được → Port mở

## 🔧 KHẮC PHỤC LỖI

### Lỗi: "Connection refused" hoặc "Connection timed out"

**Nguyên nhân có thể:**
1. Server chưa chạy
2. IP trong config.properties sai
3. Firewall chặn port 1099
4. Server không bind đúng IP

**Cách khắc phục:**

1. **Kiểm tra Server đã chạy:**
   - Trên máy SERVER, chạy `java BankingServer`
   - Xem IP hiển thị có đúng không

2. **Kiểm tra config.properties:**
   - Mở file `config.properties` trên máy CLIENT
   - Đảm bảo `server.host` = IP của máy SERVER
   - Không dùng `localhost` nếu server ở máy khác

3. **Mở Firewall:**
   - Windows: Control Panel → Windows Defender Firewall → Advanced Settings
   - Tạo Inbound Rule mới cho port 1099 (TCP)

4. **Kiểm tra mạng:**
   - Cả 2 máy phải cùng mạng LAN
   - Ping được đến nhau
   - Không bị router chặn

## 📝 LƯU Ý

- **Nếu chạy cùng máy:** Dùng `server.host=localhost`
- **Nếu chạy khác máy:** Dùng IP thực của máy SERVER
- **Port mặc định:** 1099 (có thể đổi trong config.properties)
- **Sau khi sửa config.properties:** Không cần restart, client sẽ tự load lại

## 🆘 VẪN LỖI?

1. Kiểm tra console của Server xem có lỗi gì không
2. Kiểm tra console của Client xem URL kết nối có đúng không
3. Thử tắt tạm thời Antivirus/Firewall để test
4. Đảm bảo cả 2 máy cùng mạng (cùng WiFi hoặc cùng LAN)

