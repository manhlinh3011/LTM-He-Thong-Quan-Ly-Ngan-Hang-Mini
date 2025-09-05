<h2 align="center">
    <a href="https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin">
    🎓 Faculty of Information Technology (DaiNam University)
    </a>
</h2>
<h2 align="center">
    HỆ THỐNG QUẢN LÝ NGÂN HÀNG MINI
</h2>
<div align="center">
    <p align="center">
        <img alt="AIoTLab Logo" width="170" src="https://github.com/user-attachments/assets/711a2cd8-7eb4-4dae-9d90-12c0a0a208a2" />
        <img alt="AIoTLab Logo" width="180" src="https://github.com/user-attachments/assets/dc2ef2b8-9a70-4cfa-9b4b-f6c2f25f1660" />
        <img alt="DaiNam University Logo" width="200" src="https://github.com/user-attachments/assets/77fe0fd1-2e55-4032-be3c-b1a705a1b574" />
    </p>

[![AIoTLab](https://img.shields.io/badge/AIoTLab-green?style=for-the-badge)](https://www.facebook.com/DNUAIoTLab)
[![Faculty of Information Technology](https://img.shields.io/badge/Faculty%20of%20Information%20Technology-blue?style=for-the-badge)](https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin)
[![DaiNam University](https://img.shields.io/badge/DaiNam%20University-orange?style=for-the-badge)](https://dainam.edu.vn)

</div>

---
# Mini Bank — Hệ thống quản lý ngân hàng mini (Java Swing + TCP + MySQL)

## Mục lục
- [1. Giới thiệu hệ thống](#1-giới-thiệu-hệ-thống)
- [2. Ngôn ngữ & Công nghệ chính](#2-ngôn-ngữ--công-nghệ-chính)
- [3. Hình ảnh các chức năng](#3-hình-ảnh-các-chức-năng)
- [4. Kiến trúc & Cấu trúc mã nguồn](#4-kiến-trúc--cấu-trúc-mã-nguồn)
- [5. Cơ sở dữ liệu](#5-cơ-sở-dữ-liệu)
- [6. Giao thức & Lệnh server](#6-giao-thức--lệnh-server)
- [7. Phân quyền](#7-phân-quyền)
- [8. Các bước cài đặt](#8-các-bước-cài-đặt)
- [9. Hướng dẫn sử dụng nhanh](#9-hướng-dẫn-sử-dụng-nhanh)
- [10. Khắc phục sự cố](#10-khắc-phục-sự-cố)
- [11. Ghi chú triển khai & bảo mật](#11-ghi-chú-triển-khai--bảo-mật)
- [12. Tài khoản mẫu](#12-tài-khoản-mẫu)
- [13. Đóng góp](#13-đóng-góp)

---

## 1. Giới thiệu hệ thống
Hệ thống Quản lý Ngân hàng Mini gồm 2 tiến trình: máy chủ (Server) xử lý nghiệp vụ và ứng dụng khách (Client) giao diện Swing. Hỗ trợ quản lý khách hàng, tài khoản, giao dịch (nạp/rút/chuyển), sao kê, phân quyền người dùng, tìm kiếm/sắp xếp, chỉnh sửa thông tin tài khoản. Giao tiếp Client ↔ Server qua TCP bằng giao thức key=value.

Các đặc điểm nổi bật:
- Tạo tài khoản với số tự chọn (kiểm tra trùng), có gợi ý số TK trống.
- Đổi số tài khoản an toàn ràng buộc khóa ngoại (transaction copy→move→delete).
- Tìm kiếm + sắp xếp dữ liệu bảng với TableRowSorter/RowFilter.
- Thông báo lỗi rõ ràng từ server (ví dụ: “Số tài khoản đã tồn tại: <số>”).

---

## 2. Ngôn ngữ & Công nghệ chính
<div align="center">

[![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Swing](https://img.shields.io/badge/Java%20Swing-5382a1?style=for-the-badge&logo=java&logoColor=white)]()

### Cơ sở dữ liệu
[![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)

### Môi trường chạy
[![JDK](https://img.shields.io/badge/JDK-8%2B-green?style=for-the-badge)]()

</div>

---

## 3. Hình ảnh các chức năng


---

## 4. Kiến trúc & Cấu trúc mã nguồn
- Giao tiếp: TCP (giao thức KV `key=value` phân tách bởi `|`).
- Thư mục `src/btl` (các tệp chính):
  - `MayChuNganHang.java`: ứng dụng Server (lắng nghe cổng 5555), lớp con `XuLyClient` xử lý lệnh
  - `UngDungKhachHang.java`: ứng dụng Client (Java Swing)
  - `DichVuNganHang.java`: lớp dịch vụ nghiệp vụ, điều phối DAO, kiểm tra/transaction
  - `DAO.java`: `KhachHangDAO`, `TaiKhoanDAO`, `GiaoDichDAO`, `NguoiDungDAO`
  - `MoHinh.java`: model/enum (`VaiTro`, `LoaiTaiKhoan`, `TrangThaiTaiKhoan`, ...)
  - `CauHinh.java`: cấu hình MySQL
  - `TienIch.java`: SHA‑256, format VND, log, parse/serialize KV

Ví dụ cấu trúc (rút gọn):
```
src/btl/
 ├─ MayChuNganHang.java
 ├─ UngDungKhachHang.java
 ├─ DichVuNganHang.java
 ├─ DAO.java
 ├─ MoHinh.java
 ├─ CauHinh.java
 └─ TienIch.java
```

---

## 5. Cơ sở dữ liệu
Các bảng (khớp với DAO):
- `Customers(customer_id PK, name, dob, address, phone, id_number)`
- `Accounts(account_id PK, customer_id FK→Customers, balance, type, status)`
- `Transactions(trans_id PK, account_id FK→Accounts, type, amount, date, description)`
- `Users(username PK, password, role)`

DDL tham khảo:
```sql
CREATE TABLE Customers (
  customer_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name       VARCHAR(255) NOT NULL,
  dob        DATE,
  address    VARCHAR(255),
  phone      VARCHAR(50),
  id_number  VARCHAR(50)
) ENGINE=InnoDB;

CREATE TABLE Accounts (
  account_id BIGINT PRIMARY KEY,
  customer_id BIGINT NOT NULL,
  balance     BIGINT NOT NULL DEFAULT 0,
  type        VARCHAR(20) NOT NULL,
  status      VARCHAR(20) NOT NULL,
  CONSTRAINT fk_accounts_customers FOREIGN KEY (customer_id)
    REFERENCES Customers(customer_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE Transactions (
  trans_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id  BIGINT NOT NULL,
  type        VARCHAR(20) NOT NULL,
  amount      BIGINT NOT NULL,
  date        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  description TEXT,
  CONSTRAINT fk_tx_accounts FOREIGN KEY (account_id)
    REFERENCES Accounts(account_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE Users (
  username VARCHAR(50) PRIMARY KEY,
  password VARCHAR(255) NOT NULL,
  role     VARCHAR(30)  NOT NULL
) ENGINE=InnoDB;
```

---

## 6. Giao thức & Lệnh server
- Mỗi yêu cầu là một dòng TCP: `key=value|key=value|...`  
- Phản hồi: `ok=true|...` hoặc `ok=false|err=Thông báo lỗi`

Ví dụ:
```
login:   cmd=login|u=admin|p=admin123
resp:    ok=true|role=ADMIN

kh_ds:   cmd=kh_ds
resp:    ok=true|data=1,Nguyen A,0909...;2,Tran B,0912...;
```

Danh sách lệnh (rút gọn):
- Đăng nhập: `login(u,p)` → `role`
- Khách hàng: 
  - `kh_tao(ten,dob,dc,dt,gt)` → `id`
  - `kh_ds()` → `data`
  - `kh_sua(id,ten,dob,dc,dt,gt)`
  - `kh_xoa(id)`
  - `kh_get(id)` → `id,ten,dt,dob,dc,gt`
- Tài khoản:
  - `tk_tao(kh,loai,sodu[,sotk])` → `id` (nếu `sotk` trùng sẽ trả lỗi)
  - `tk_theokh(kh)` → `data`
  - `tk_khoa(id,khoa=true|false)`
  - `tk_sua(id,loai)`
  - `tk_sua_sodu(id,sodu)`
  - `tk_doi_so(cu,moi)` (transaction copy→move→delete an toàn FK)
  - `tk_xoa(id)`
  - `tk_goiy()` → `val` (gợi ý số TK trống)
- Giao dịch: `nap(tk,tien,mota)`, `rut(tk,tien,mota)`, `chuyen(from,to,tien,mota)`
- Sao kê: `saoke(tk)` → `data`

---

## 7. Phân quyền
- `ADMIN`: toàn quyền các lệnh.
- `GIAO_DICH_VIEN`: nghiệp vụ thường ngày (KH/TK/GD/SAOKÊ). Có thể giới hạn thêm theo nhu cầu.

---

## 8. Các bước cài đặt
1. Cài JDK 8+ và MySQL; tạo DB `bankmini` và chạy DDL ở trên.
2. Sửa kết nối DB trong `src/btl/CauHinh.java`:
```java
public static final String DB_URL = "jdbc:mysql://localhost:3306/bankmini?useSSL=false&serverTimezone=UTC";
public static final String DB_USER = "<user>";
public static final String DB_PASS = "<pass>";
```
3. Chạy hệ thống
   - Server: chạy `MayChuNganHang.main` (lắng nghe cổng 5555, seed `admin/admin123` nếu chưa có)
   - Client: chạy `UngDungKhachHang.main` → đăng nhập `admin/admin123`

---

## 9. Hướng dẫn sử dụng nhanh
- Tab Khách hàng: thêm/chọn KH (double‑click để mở tab Tài khoản)
- Tab Tài khoản: bấm “Chọn từ KH đã chọn” → “Tải danh sách TK” → Tạo/Sửa/Khóa/Mở/Xóa/Đổi số TK → Xem sao kê
- Tab Giao dịch: nạp/rút/chuyển; bảng TK tự refresh sau thao tác
- Tab Sao kê: nhập số TK hoặc double‑click dòng TK để mở nhanh

---

## 10. Khắc phục sự cố
- Không kết nối được DB: kiểm tra `CauHinh.java`, user/password, quyền, MySQL đang chạy.
- Cổng 5555 bận: đổi `MayChuNganHang.PORT` hoặc giải phóng cổng.
- “Số tài khoản đã tồn tại”: chọn số khác, hoặc dùng `Gợi ý số TK`, hoặc dùng chức năng `Đổi số TK`.
- Lỗi FK khi đổi số TK: đã xử lý bằng transaction an toàn; nếu vẫn lỗi, kiểm tra quyền user DB.

---

## 11. Ghi chú triển khai & bảo mật
- Mật khẩu user được băm SHA‑256 (`TienIch.sha256`).
- Khuyên dùng tài khoản DB riêng cho ứng dụng, giới hạn quyền.
- Log lưu ở `bank.log`.

---

## 12. Tài khoản mẫu
- ADMIN: `admin / admin123`

---

## 13. Đóng góp

📌 *Lưu ý: Có thể tùy chỉnh tên database, tài khoản admin, giao diện theo nhu cầu.*
