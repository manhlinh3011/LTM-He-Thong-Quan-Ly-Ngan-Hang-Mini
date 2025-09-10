package btl;

import java.sql.*;
import java.util.*;

class KhachHangDAO {
    public KhachHang tao(String hoTen, String ngaySinh, String diaChi, String dienThoai, String soGiayTo) throws SQLException{
        try(Connection c = CauHinh.ketNoi(); PreparedStatement ps = c.prepareStatement(
                "INSERT INTO Customers(name,dob,address,phone,id_number) VALUES(?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)){
            ps.setString(1, hoTen); ps.setString(2, ngaySinh); ps.setString(3, diaChi); ps.setString(4, dienThoai); ps.setString(5, soGiayTo);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            KhachHang k = new KhachHang();
            if(rs.next()) k.id = rs.getLong(1);
            k.hoTen=hoTen; k.ngaySinh=ngaySinh; k.diaChi=diaChi; k.dienThoai=dienThoai; k.soGiayTo=soGiayTo;
            return k;
        }
    }
    public List<KhachHang> tatCa() throws SQLException{
        List<KhachHang> list = new ArrayList<>();
        try(Connection c = CauHinh.ketNoi(); Statement st = c.createStatement()){
            ResultSet rs = st.executeQuery("SELECT * FROM Customers ORDER BY customer_id DESC");
            while(rs.next()){
                KhachHang k = new KhachHang();
                k.id = rs.getLong("customer_id"); k.hoTen=rs.getString("name"); k.ngaySinh=rs.getString("dob");
                k.diaChi=rs.getString("address"); k.dienThoai=rs.getString("phone"); k.soGiayTo=rs.getString("id_number");
                list.add(k);
            }
        }
        return list;
    }
    public void capNhat(KhachHang k) throws SQLException{
        try(Connection c = CauHinh.ketNoi(); PreparedStatement ps = c.prepareStatement(
                "UPDATE Customers SET name=?, dob=?, address=?, phone=?, id_number=? WHERE customer_id=?")){
            ps.setString(1, k.hoTen); ps.setString(2, k.ngaySinh); ps.setString(3, k.diaChi); ps.setString(4, k.dienThoai); ps.setString(5, k.soGiayTo); ps.setLong(6, k.id);
            ps.executeUpdate();
        }
    }
    public void xoa(long id) throws SQLException{
        try(Connection c = CauHinh.ketNoi(); PreparedStatement ps = c.prepareStatement("DELETE FROM Customers WHERE customer_id=?")){
            ps.setLong(1, id); ps.executeUpdate();
        }
    }
}

class TaiKhoanDAO {
    public TaiKhoan tao(long khId, LoaiTaiKhoan loai, long soDu) throws SQLException{
        try(Connection c = CauHinh.ketNoi(); PreparedStatement ps = c.prepareStatement(
                "INSERT INTO Accounts(customer_id,balance,type,status) VALUES(?,?,?,?)", Statement.RETURN_GENERATED_KEYS)){
            ps.setLong(1, khId); ps.setLong(2, soDu); ps.setString(3, loai.name()); ps.setString(4, TrangThaiTaiKhoan.HOAT_DONG.name());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            TaiKhoan t = new TaiKhoan();
            if(rs.next()) t.id = rs.getLong(1);
            t.khachHangId = khId; t.loai = loai; t.soDu = soDu; t.trangThai = TrangThaiTaiKhoan.HOAT_DONG;
            return t;
        }
    }
    // Tạo tài khoản với số tài khoản do người dùng chọn (chèn trực tiếp account_id)
    public TaiKhoan taoVoiId(long accountId, long khId, LoaiTaiKhoan loai, long soDu) throws SQLException{
        try(Connection c = CauHinh.ketNoi(); PreparedStatement ps = c.prepareStatement(
                "INSERT INTO Accounts(account_id,customer_id,balance,type,status) VALUES(?,?,?,?,?)")){
            ps.setLong(1, accountId); ps.setLong(2, khId); ps.setLong(3, soDu); ps.setString(4, loai.name()); ps.setString(5, TrangThaiTaiKhoan.HOAT_DONG.name());
            ps.executeUpdate();
            TaiKhoan t = new TaiKhoan();
            t.id = accountId; t.khachHangId = khId; t.loai = loai; t.soDu = soDu; t.trangThai = TrangThaiTaiKhoan.HOAT_DONG;
            return t;
        }
    }
    public List<TaiKhoan> theoKhach(long khId) throws SQLException{
        List<TaiKhoan> list = new ArrayList<>();
        try(Connection c = CauHinh.ketNoi(); PreparedStatement ps = c.prepareStatement("SELECT * FROM Accounts WHERE customer_id=?")){
            ps.setLong(1, khId); ResultSet rs = ps.executeQuery();
            while(rs.next()){
                TaiKhoan t = new TaiKhoan(); t.id = rs.getLong("account_id"); t.khachHangId = rs.getLong("customer_id");
                t.soDu = rs.getLong("balance"); t.loai = LoaiTaiKhoan.valueOf(rs.getString("type")); t.trangThai = TrangThaiTaiKhoan.valueOf(rs.getString("status"));
                list.add(t);
            }
        }
        return list;
    }
    public TaiKhoan tim(long id) throws SQLException{
        try(Connection c = CauHinh.ketNoi(); PreparedStatement ps = c.prepareStatement("SELECT * FROM Accounts WHERE account_id=?")){
            ps.setLong(1, id); ResultSet rs = ps.executeQuery();
            if(rs.next()){
                TaiKhoan t = new TaiKhoan(); t.id = rs.getLong("account_id"); t.khachHangId = rs.getLong("customer_id");
                t.soDu = rs.getLong("balance"); t.loai = LoaiTaiKhoan.valueOf(rs.getString("type")); t.trangThai = TrangThaiTaiKhoan.valueOf(rs.getString("status"));
                return t;
            }
            return null;
        }
    }
    public void capNhatSoDu(long id, long soDu) throws SQLException{
        try(Connection c = CauHinh.ketNoi(); PreparedStatement ps = c.prepareStatement("UPDATE Accounts SET balance=? WHERE account_id=?")){
            ps.setLong(1, soDu); ps.setLong(2, id); ps.executeUpdate();
        }
    }
    public void capNhatTrangThai(long id, TrangThaiTaiKhoan st) throws SQLException{
        try(Connection c = CauHinh.ketNoi(); PreparedStatement ps = c.prepareStatement("UPDATE Accounts SET status=? WHERE account_id=?")){
            ps.setString(1, st.name()); ps.setLong(2, id); ps.executeUpdate();
        }
    }
}

class GiaoDichDAO {
    public void ghi(long taiKhoanId, LoaiGiaoDich loai, long soTien, String moTa) throws SQLException{
        try(Connection c = CauHinh.ketNoi(); PreparedStatement ps = c.prepareStatement(
                "INSERT INTO Transactions(account_id,type,amount,date,description) VALUES(?,?,?,?,?)")){
            ps.setLong(1, taiKhoanId); ps.setString(2, loai.name()); ps.setLong(3, soTien); ps.setTimestamp(4, new Timestamp(System.currentTimeMillis())); ps.setString(5, moTa);
            ps.executeUpdate();
        }
    }
    public List<GiaoDich> saoKe(long taiKhoanId) throws SQLException{
        List<GiaoDich> list = new ArrayList<>();
        try(Connection c = CauHinh.ketNoi(); PreparedStatement ps = c.prepareStatement("SELECT * FROM Transactions WHERE account_id=? ORDER BY date DESC")){
            ps.setLong(1, taiKhoanId); ResultSet rs = ps.executeQuery();
            while(rs.next()){
                GiaoDich g = new GiaoDich(); g.id = rs.getLong("trans_id"); g.taiKhoanId = rs.getLong("account_id");
                g.loai = LoaiGiaoDich.valueOf(rs.getString("type")); g.soTien = rs.getLong("amount"); g.ngay = rs.getTimestamp("date"); g.moTa = rs.getString("description");
                list.add(g);
            }
        }
        return list;
    }
}

class NguoiDungDAO {
    public void tao(String user, String passHash, VaiTro role) throws SQLException{
        try(Connection c = CauHinh.ketNoi(); PreparedStatement ps = c.prepareStatement("INSERT INTO Users(username,password,role) VALUES(?,?,?)")){
            ps.setString(1, user); ps.setString(2, passHash); ps.setString(3, role.name()); ps.executeUpdate();
        }
    }
    public NguoiDung tim(String user) throws SQLException{
        try(Connection c = CauHinh.ketNoi(); PreparedStatement ps = c.prepareStatement("SELECT * FROM Users WHERE username=?")){
            ps.setString(1, user); ResultSet rs = ps.executeQuery();
            if(rs.next()){
                NguoiDung n = new NguoiDung(); n.tenDangNhap = rs.getString("username"); n.matKhauHash = rs.getString("password"); n.vaiTro = VaiTro.valueOf(rs.getString("role"));
                return n;
            }
            return null;
        }
    }
}
