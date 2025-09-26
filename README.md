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
## 1. Giới thiệu hệ thống
Hệ thống Quản lý Ngân hàng Mini gồm 2 tiến trình: máy chủ (Server) xử lý nghiệp vụ và ứng dụng khách (Client) giao diện Swing. Hỗ trợ quản lý khách hàng, tài khoản, giao dịch (nạp/rút/chuyển), sao kê, phân quyền người dùng, tìm kiếm/sắp xếp, chỉnh sửa thông tin tài khoản. Giao tiếp Client ↔ Server qua TCP bằng giao thức key=value.  

Các đặc điểm nổi bật:
- Tạo tài khoản với số tự chọn (kiểm tra trùng), có gợi ý số TK trống.  
- Đổi số tài khoản an toàn ràng buộc khóa ngoại (transaction copy→move→delete).  
- Tìm kiếm + sắp xếp dữ liệu bảng với TableRowSorter/RowFilter.  
- Thông báo lỗi rõ ràng từ server (ví dụ: “Số tài khoản đã tồn tại: <số>”).  

---

## 2. Ngôn ngữ & Công nghệ sử dụng
<div align="center">

[![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Swing](https://img.shields.io/badge/Java%20Swing-5382a1?style=for-the-badge&logo=java&logoColor=white)]()
[![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![JDK](https://img.shields.io/badge/JDK-8%2B-green?style=for-the-badge)]()

</div>

---

## 3. Hình ảnh các chức năng
<p align="center">
  <img src="images/login.png" alt="Đăng nhập" width="1000"/>
  <br/>
  <em>Hình 1. 🖥️ Giao diện chức năng đăng nhập</em><br/>
</p>
---
<p align="center">
  <img src="images/khachhang.png" alt="Thông tin khách hàng" width="1000"/>
  <br/>
  <em>Hình 2. 🖥️ Giao diện chức năng quản lý thông tin khách hàng (thêm, sửa, xóa, xuất file csv)</em><br/>
</p>
---
<p align="center">
  <img src="images/taikhoan.png" alt="Tài khoản khách hàng" width="1000"/>
  <br/>
  <em>Hình 3. 🖥️ Giao diện chức năng quản lý thông tin tài khoản khách hàng (thêm, sửa, xóa, xuất file csv)</em><br/>
</p>
---
<p align="center">
  <img src="images/naptien.png" alt="Nạp tiền" width="1000"/>
  <br/>
  <em>Hình 4. 🖥️ Giao diện chức năng nạp tiền</em><br/>
</p>
---
<p align="center">
  <img src="images/ruttien.png" alt="Rút tiền" width="1000"/>
  <br/>
  <em>Hình 5. 🖥️ Giao diện chức năng rút tiền</em><br/>
</p>
---
<p align="center">
  <img src="images/chuyenkhoan.png" alt="Chuyển khoản" width="1000"/>
  <br/>
  <em>Hình 6. 🖥️ Giao diện chức năng chuyển khoản</em><br/>
</p>
---
<p align="center">
  <img src="images/saoke.png" alt="Sao kê" width="1000"/>
  <br/>
  <em>Hình 7. 🖥️ Giao diện chức năng sao kê lịch sử (nạp, rút, chuyển)</em><br/>
</p>

## 4. Các bước cài đặt
1. **Cài đặt môi trường**  
   - JDK 8 trở lên  
   - MySQL 5.7+ hoặc 8.0+  
   - IDE khuyến nghị: IntelliJ IDEA / Eclipse / NetBeans  

2. **Tạo cơ sở dữ liệu**  
   - Tạo database `bankmini` trong MySQL  
   - Chạy file `bankmini.sql` (DDL) để tạo bảng: Customers, Accounts, Transactions, Users  

3. **Cấu hình kết nối MySQL**  
   Mở file `src/btl/CauHinh.java` và chỉnh lại thông tin:  
   ```java
   public static final String DB_URL = "jdbc:mysql://localhost:3306/bankmini?useSSL=false&serverTimezone=UTC";
   public static final String DB_USER = "<user>";
   public static final String DB_PASS = "<pass>";

## 5. Thông tin liên hệ  
Họ tên: Hoàng Mạnh Linh.  
Lớp: CNTT 16-03.  
Email: linhmanhhoang03@gmail.com.

© 2025 AIoTLab, Faculty of Information Technology, DaiNam University. All rights reserved.

---
