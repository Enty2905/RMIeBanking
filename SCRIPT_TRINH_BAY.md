# 📜 SCRIPT TRÌNH BÀY DỰ ÁN RMI E-BANKING

## ⏱️ Thời gian dự kiến: 10-15 phút

---

# PHẦN 1: GIỚI THIỆU DỰ ÁN (2-3 phút)

## 1.1. Mở đầu

> "Xin chào thầy/cô và các bạn. Hôm nay em/nhóm em sẽ trình bày đề tài **Ứng dụng Ngân hàng điện tử sử dụng công nghệ Java RMI**."

## 1.2. Giới thiệu tổng quan

> "Đây là một ứng dụng **E-Banking** - Ngân hàng điện tử, cho phép người dùng thực hiện các giao dịch ngân hàng cơ bản như:
> - Đăng ký tài khoản mới
> - Đăng nhập vào hệ thống
> - Vấn tin số dư
> - Nạp tiền vào tài khoản
> - Rút tiền từ tài khoản
> - Chuyển khoản cho người khác
> - Xem lịch sử giao dịch
> - Nhận thông báo real-time khi có tiền chuyển đến"

## 1.3. Công nghệ sử dụng

> "Dự án sử dụng các công nghệ sau:"

| Công nghệ | Mục đích sử dụng |
|-----------|------------------|
| **Java SE** | Ngôn ngữ lập trình chính |
| **Java RMI (Remote Method Invocation)** | Công nghệ gọi phương thức từ xa, cho phép client gọi method trên server qua mạng |
| **Java Swing** | Xây dựng giao diện người dùng đồ họa (GUI) |
| **JSON** | Lưu trữ dữ liệu (users, accounts, transactions) |
| **Callback Pattern** | Thông báo real-time khi có giao dịch mới |

## 1.4. Tại sao chọn Java RMI?

> "Java RMI - Remote Method Invocation là một công nghệ cho phép:
> - **Gọi phương thức từ xa**: Client có thể gọi method trên Server như gọi method local
> - **Phân tán xử lý**: Server xử lý logic nghiệp vụ, Client chỉ hiển thị giao diện
> - **Trong suốt với lập trình viên**: Không cần quan tâm đến chi tiết truyền dữ liệu qua mạng
> - **Phù hợp với mô hình Client-Server**: Nhiều client có thể kết nối đến 1 server"

---

# PHẦN 2: KIẾN TRÚC HỆ THỐNG (2-3 phút)

## 2.1. Mô hình kiến trúc

> "Hệ thống được xây dựng theo mô hình **Client-Server** với kiến trúc 3 tầng:"

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT LAYER                              │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │
│  │  Client 1   │  │  Client 2   │  │  Client 3   │    ...       │
│  │ (LoginFrame │  │ (Máy khác)  │  │ (Máy khác)  │              │
│  │  + GUI)     │  │             │  │             │              │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘              │
└─────────┼────────────────┼────────────────┼─────────────────────┘
          │                │                │
          │    RMI (Remote Method Invocation)
          │                │                │
┌─────────▼────────────────▼────────────────▼─────────────────────┐
│                       SERVER LAYER                               │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │              BankingServer + BankingServiceImpl          │    │
│  │  - Xử lý đăng ký, đăng nhập                             │    │
│  │  - Xử lý giao dịch (nạp, rút, chuyển khoản)             │    │
│  │  - Quản lý callback cho thông báo real-time             │    │
│  └─────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
          │
┌─────────▼───────────────────────────────────────────────────────┐
│                        DATA LAYER                                │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐           │
│  │  users.json  │  │accounts.json │  │transactions  │           │
│  │              │  │              │  │    .json     │           │
│  └──────────────┘  └──────────────┘  └──────────────┘           │
└─────────────────────────────────────────────────────────────────┘
```

## 2.2. Giải thích các thành phần

> "Giải thích từng thành phần:"

| Thành phần | File | Chức năng |
|------------|------|-----------|
| **BankingService** | `BankingService.java` | Interface định nghĩa các phương thức remote |
| **BankingServiceImpl** | `BankingServiceImpl.java` | Implement các phương thức xử lý nghiệp vụ |
| **BankingServer** | `BankingServer.java` | Khởi động server, đăng ký RMI service |
| **BankingCallback** | `BankingCallback.java` | Interface callback để thông báo real-time |
| **LoginFrame** | `LoginFrame.java` | Giao diện đăng nhập/đăng ký |
| **BankingClientGUI** | `BankingClientGUI.java` | Giao diện chính sau khi đăng nhập |
| **User, Account, Transaction** | `*.java` | Các class model lưu trữ dữ liệu |

---

# PHẦN 3: DEMO ỨNG DỤNG (5-7 phút)

## 3.1. Chuẩn bị Demo

> "Bây giờ em sẽ demo ứng dụng. Em đã chuẩn bị 2 máy tính:"
> - **Máy 1**: Chạy Server + 1 Client (người dùng A)
> - **Máy 2**: Chỉ chạy Client (người dùng B)

## 3.2. Khởi động Server

> "Đầu tiên, em khởi động Server trên máy 1"

**[Thao tác: Chạy BankingServer]**

> "Khi server khởi động, nó sẽ:
> 1. Tạo RMI Registry trên port 1099
> 2. Đăng ký BankingService
> 3. Hiển thị địa chỉ IP để các client khác kết nối"

## 3.3. Demo Đăng ký tài khoản

> "Tiếp theo, em demo chức năng **đăng ký tài khoản mới**"

**[Thao tác: Mở LoginFrame, click "Đăng ký ngay"]**

> "Người dùng cần nhập:
> - Họ và tên
> - Tên đăng nhập
> - Mật khẩu
> 
> Khi đăng ký thành công, hệ thống sẽ tự động tạo một **số tài khoản ngân hàng** duy nhất cho người dùng."

**[Thao tác: Nhập thông tin và đăng ký]**

## 3.4. Demo Đăng nhập

> "Sau khi đăng ký, em đăng nhập vào hệ thống"

**[Thao tác: Nhập username/password và đăng nhập]**

> "Giao diện chính hiển thị:
> - Thông tin chào mừng người dùng
> - Số tài khoản
> - Số dư hiện tại
> - Các nút chức năng giao dịch
> - Lịch sử giao dịch"

## 3.5. Demo Nạp tiền

> "Em demo chức năng **nạp tiền** - giả lập việc gửi tiền vào tài khoản"

**[Thao tác: Nhập số tiền → Click "Nạp tiền"]**

> "Số dư được cập nhật ngay lập tức và giao dịch được ghi vào lịch sử"

## 3.6. Demo Rút tiền

> "Tiếp theo là chức năng **rút tiền**"

**[Thao tác: Nhập số tiền → Click "Rút tiền"]**

> "Hệ thống sẽ kiểm tra số dư trước khi cho phép rút. Nếu số dư không đủ, sẽ báo lỗi."

## 3.7. Demo Chuyển khoản (QUAN TRỌNG - Highlight RMI)

> "Đây là phần quan trọng nhất - **Chuyển khoản giữa 2 người dùng trên 2 máy khác nhau**"

**[Thao tác trên máy 2: Mở client, đăng nhập với tài khoản B]**

> "Bây giờ em có 2 người dùng đang online trên 2 máy khác nhau:
> - Máy 1: Người dùng A
> - Máy 2: Người dùng B"

**[Thao tác trên máy 1:]**
> "Người dùng A sẽ chuyển tiền cho người dùng B"

**[Nhập số tài khoản B → Nhập số tiền → Nhập nội dung → Click "Chuyển khoản"]**

> "Và bây giờ, điều đặc biệt xảy ra..."

**[Chỉ vào màn hình máy 2]**

> "Người dùng B nhận được **thông báo real-time** ngay lập tức! 
> Đây là nhờ công nghệ **Callback trong RMI** - server gọi ngược lại client để thông báo khi có giao dịch mới."

## 3.8. Demo Vấn tin và Lịch sử giao dịch

> "Cuối cùng, người dùng có thể:
> - **Vấn tin**: Xem số dư hiện tại
> - **Xem lịch sử giao dịch**: Tất cả các giao dịch nạp, rút, chuyển khoản"

**[Thao tác: Click "Vấn tin" và xem lịch sử]**

---

# PHẦN 4: GIẢI THÍCH CODE QUAN TRỌNG (2-3 phút)

## 4.1. Interface RMI - BankingService

> "Đây là interface định nghĩa các phương thức mà client có thể gọi từ xa:"

```java
public interface BankingService extends Remote {
    // Đăng ký, đăng nhập
    String register(String username, String password, String fullName);
    String login(String username, String password);
    
    // Giao dịch
    String queryAccount(String accountNumber);
    String deposit(String accountNumber, double amount);
    String withdraw(String accountNumber, double amount);
    String transfer(String from, String to, double amount, String content);
    
    // Callback để nhận thông báo real-time
    void registerCallback(String accountNumber, BankingCallback callback);
    void unregisterCallback(String accountNumber);
}
```

## 4.2. Cơ chế Callback

> "Để thông báo real-time, em sử dụng **Callback Pattern**:"

```java
// Interface callback - client implement
public interface BankingCallback extends Remote {
    void notifyTransferReceived(String fromAccount, double amount, 
                                 String content, double newBalance);
}

// Server gọi callback khi có chuyển khoản
private void notifyRecipient(String accountNumber, String fromAccount, 
                             double amount, String content, double newBalance) {
    BankingCallback callback = clientCallbacks.get(accountNumber);
    if (callback != null) {
        callback.notifyTransferReceived(fromAccount, amount, content, newBalance);
    }
}
```

## 4.3. Kết nối từ xa

> "Client kết nối đến server thông qua RMI Naming:"

```java
// Trên Server - đăng ký service
Naming.rebind("rmi://192.168.1.69:1099/BankingService", bankingService);

// Trên Client - lookup và sử dụng service
BankingService service = (BankingService) Naming.lookup("rmi://192.168.1.69:1099/BankingService");
service.deposit("123456", 1000000); // Gọi method như local!
```

---

# PHẦN 5: ƯU ĐIỂM VÀ HẠN CHẾ (1-2 phút)

## 5.1. Ưu điểm

> "Ưu điểm của hệ thống:"

| STT | Ưu điểm |
|-----|---------|
| 1 | **Phân tán xử lý**: Nhiều client có thể cùng sử dụng 1 server |
| 2 | **Real-time**: Thông báo tức thì khi có giao dịch nhờ Callback |
| 3 | **Trong suốt**: Gọi method từ xa như gọi local, dễ lập trình |
| 4 | **Bảo mật cơ bản**: Mã hóa mật khẩu, kiểm tra số dư |
| 5 | **Giao diện thân thiện**: Modern UI với Java Swing |
| 6 | **Cấu hình linh hoạt**: Dễ dàng thay đổi IP server qua config file |

## 5.2. Hạn chế và hướng phát triển

> "Hạn chế và hướng phát triển:"

| Hạn chế | Hướng phát triển |
|---------|------------------|
| Dữ liệu lưu JSON (không phù hợp production) | Sử dụng Database (MySQL, PostgreSQL) |
| Chưa có mã hóa SSL/TLS | Thêm bảo mật kết nối |
| Chưa có xác thực 2 yếu tố (2FA) | Thêm OTP qua SMS/Email |
| Chưa có quản lý session | Thêm timeout, logout tự động |
| Giao diện desktop | Phát triển thêm Web/Mobile |

---

# PHẦN 6: KẾT LUẬN (1 phút)

> "Tóm lại, dự án **RMI E-Banking** đã hoàn thành các mục tiêu:
> 
> ✅ Xây dựng thành công hệ thống ngân hàng điện tử với đầy đủ chức năng cơ bản
> 
> ✅ Áp dụng công nghệ **Java RMI** để triển khai mô hình Client-Server
> 
> ✅ Sử dụng **Callback Pattern** để thông báo real-time
> 
> ✅ Thiết kế giao diện người dùng trực quan với **Java Swing**
> 
> ✅ Demo thành công việc chạy trên **nhiều máy tính khác nhau** trong cùng mạng LAN
> 
> Dự án giúp em/nhóm em hiểu sâu hơn về **lập trình phân tán** và **công nghệ RMI** trong Java."

> "Em/Nhóm em xin cảm ơn thầy/cô và các bạn đã lắng nghe. Em/Nhóm em sẵn sàng trả lời các câu hỏi."

---

# 📝 CÂU HỎI THƯỜNG GẶP VÀ TRẢ LỜI

## Q1: Tại sao chọn RMI thay vì REST API hay Socket?

> "RMI cho phép gọi method từ xa một cách **trong suốt** - lập trình viên không cần quan tâm đến việc serialize/deserialize dữ liệu hay quản lý kết nối socket. Code đơn giản và dễ bảo trì hơn cho ứng dụng Java-to-Java."

## Q2: Callback hoạt động như thế nào?

> "Khi client đăng nhập, nó đăng ký một **callback object** với server. Callback object này cũng là một remote object. Khi có giao dịch, server sẽ **gọi ngược** method trên callback object của client để thông báo."

## Q3: Làm sao để chạy trên 2 máy khác nhau?

> "Hai máy phải cùng mạng LAN. Trên máy server, chạy BankingServer - nó sẽ hiển thị IP. Trên máy client, sửa file config.properties với IP của server, rồi chạy LoginFrame."

## Q4: Dữ liệu được lưu ở đâu?

> "Dữ liệu được lưu trong 3 file JSON: users.json (thông tin đăng nhập), accounts.json (số dư tài khoản), transactions.json (lịch sử giao dịch). Trong thực tế sẽ dùng database."

## Q5: RMI Registry là gì?

> "RMI Registry giống như một **danh bạ** - nó lưu trữ tên service và vị trí của chúng. Client tra cứu tên 'BankingService' trong registry để lấy được reference đến service thực sự trên server."

---

# 🎯 CHECKLIST TRƯỚC KHI DEMO

- [ ] Server đã chạy và hiển thị IP
- [ ] Client trên máy server kết nối được (config: localhost)
- [ ] Client trên máy khác kết nối được (config: IP server)
- [ ] Đã tạo sẵn 2 tài khoản để demo chuyển khoản
- [ ] Firewall đã được cấu hình/tắt
- [ ] Cả 2 máy cùng mạng WiFi/LAN

---

**Chúc bạn trình bày thành công! 🎉**
