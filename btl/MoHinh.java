package btl;

import java.util.Date;

enum LoaiTaiKhoan {
    THANH_TOAN, TIET_KIEM
}

enum TrangThaiTaiKhoan {
    HOAT_DONG, KHOA
}

enum LoaiGiaoDich {
    NAP, RUT, CHUYEN_DI, CHUYEN_DEN
}

enum VaiTro {
    ADMIN, GIAO_DICH_VIEN
}

class KhachHang {
    long id;
    String hoTen;
    String ngaySinh;
    String diaChi;
    String dienThoai;
    String soGiayTo;
}

class TaiKhoan {
    long id;
    long khachHangId;
    LoaiTaiKhoan loai;
    long soDu;
    TrangThaiTaiKhoan trangThai;
}

class GiaoDich {
    long id;
    long taiKhoanId;
    LoaiGiaoDich loai;
    long soTien;
    java.util.Date ngay;
    String moTa;
}

class NguoiDung {
    String tenDangNhap;
    String matKhauHash;
    VaiTro vaiTro;
}
