package btl;

class LoiNganHang extends Exception { public LoiNganHang(String m){ super(m);} }

class DichVuNganHang {
    private final KhachHangDAO khDao = new KhachHangDAO();
    private final TaiKhoanDAO tkDao = new TaiKhoanDAO();
    private final GiaoDichDAO gdDao = new GiaoDichDAO();
    private final NguoiDungDAO ndDao = new NguoiDungDAO();

    // Đăng nhập
    public VaiTro dangNhap(String u, String p) throws Exception{
        NguoiDung nd = ndDao.tim(u);
        if(nd==null) throw new LoiNganHang("Sai tài khoản");
        if(!nd.matKhauHash.equals(TienIch.sha256(p))) throw new LoiNganHang("Sai mật khẩu");
        return nd.vaiTro;
    }
    public void seedAdmin() throws Exception{
        if(ndDao.tim("admin")==null){ ndDao.tao("admin", TienIch.sha256("admin123"), VaiTro.ADMIN); }
    }

    // Khách hàng
    public KhachHang taoKhach(String hoTen, String dob, String diaChi, String dt, String cccd) throws Exception{
        return khDao.tao(hoTen, dob, diaChi, dt, cccd);
    }
    public java.util.List<KhachHang> dsKhach() throws Exception{ return khDao.tatCa(); }
    public void capNhatKhach(KhachHang k) throws Exception{ khDao.capNhat(k); }
    public void xoaKhach(long id) throws Exception{ khDao.xoa(id); }

    // Tài khoản
    public TaiKhoan taoTaiKhoan(long khId, LoaiTaiKhoan loai, long soDuBanDau) throws Exception{
        return tkDao.tao(khId, loai, soDuBanDau);
    }
    public TaiKhoan taoTaiKhoanVoiId(long soTk, long khId, LoaiTaiKhoan loai, long soDuBanDau) throws Exception{
        if(tkDao.tim(soTk)!=null) throw new LoiNganHang("Số tài khoản đã tồn tại: "+soTk);
        return tkDao.taoVoiId(soTk, khId, loai, soDuBanDau);
    }
    public java.util.List<TaiKhoan> dsTaiKhoanTheoKhach(long khId) throws Exception{ return tkDao.theoKhach(khId); }
    public void khoaTaiKhoan(long tkId, boolean khoa) throws Exception{ tkDao.capNhatTrangThai(tkId, khoa?TrangThaiTaiKhoan.KHOA:TrangThaiTaiKhoan.HOAT_DONG); }
    public void suaLoaiTaiKhoan(long tkId, LoaiTaiKhoan loai) throws Exception{
        try(java.sql.Connection c = CauHinh.ketNoi(); java.sql.PreparedStatement ps = c.prepareStatement("UPDATE Accounts SET type=? WHERE account_id=?")){
            ps.setString(1, loai.name()); ps.setLong(2, tkId); ps.executeUpdate();
        }
    }
    public void suaSoDuTaiKhoan(long tkId, long soDuMoi) throws Exception{
        TaiKhoan t = tkDao.tim(tkId);
        if(t==null) throw new LoiNganHang("Không tìm thấy tài khoản");
        tkDao.capNhatSoDu(tkId, soDuMoi);
    }
    public void doiSoTaiKhoan(long tkIdCu, long tkIdMoi) throws Exception{
        if(tkDao.tim(tkIdCu)==null) throw new LoiNganHang("Tài khoản hiện tại không tồn tại");
        if(tkDao.tim(tkIdMoi)!=null) throw new LoiNganHang("Số tài khoản mới đã tồn tại");
        try(java.sql.Connection c = CauHinh.ketNoi()){
            c.setAutoCommit(false);
            try(
                java.sql.PreparedStatement psCopy = c.prepareStatement(
                    "INSERT INTO Accounts(account_id,customer_id,balance,type,status) " +
                    "SELECT ?, customer_id, balance, type, status FROM Accounts WHERE account_id=?");
                java.sql.PreparedStatement psMoveTx = c.prepareStatement(
                    "UPDATE Transactions SET account_id=? WHERE account_id=?");
                java.sql.PreparedStatement psDelOld = c.prepareStatement(
                    "DELETE FROM Accounts WHERE account_id=?");
            ){
                // 1) Tạo record tài khoản mới (để thỏa FK)
                psCopy.setLong(1, tkIdMoi); psCopy.setLong(2, tkIdCu); psCopy.executeUpdate();
                // 2) Chuyển toàn bộ giao dịch sang số mới
                psMoveTx.setLong(1, tkIdMoi); psMoveTx.setLong(2, tkIdCu); psMoveTx.executeUpdate();
                // 3) Xóa tài khoản cũ
                psDelOld.setLong(1, tkIdCu); psDelOld.executeUpdate();
                c.commit();
            }catch(Exception ex){ c.rollback(); throw ex; }
        }
    }

    public long goiYSoTaiKhoan() throws Exception{
        java.util.Random rnd = new java.util.Random();
        for(int i=0;i<1000;i++){
            long v = 1_000_000L + (Math.abs(rnd.nextLong()) % 900_000_000L);
            if(tkDao.tim(v)==null) return v;
        }
        throw new LoiNganHang("Không thể gợi ý số tài khoản, thử lại");
    }
    public void xoaTaiKhoan(long tkId) throws Exception{ if(tkDao.tim(tkId)==null) throw new LoiNganHang("Không tìm thấy tài khoản");
        try(java.sql.Connection c = CauHinh.ketNoi(); java.sql.PreparedStatement ps = c.prepareStatement("DELETE FROM Accounts WHERE account_id=?")){
            ps.setLong(1, tkId); ps.executeUpdate();
        }
    }

    // Giao dịch
    public synchronized void napTien(long tkId, long soTien, String moTa) throws Exception{
        TaiKhoan t = tkDao.tim(tkId); if(t==null) throw new LoiNganHang("Không tìm thấy tài khoản");
        if(t.trangThai==TrangThaiTaiKhoan.KHOA) throw new LoiNganHang("Tài khoản đã khóa");
        t.soDu += soTien; tkDao.capNhatSoDu(tkId, t.soDu); gdDao.ghi(tkId, LoaiGiaoDich.NAP, soTien, moTa);
    }
    public synchronized void rutTien(long tkId, long soTien, String moTa) throws Exception{
        TaiKhoan t = tkDao.tim(tkId); if(t==null) throw new LoiNganHang("Không tìm thấy tài khoản");
        if(t.trangThai==TrangThaiTaiKhoan.KHOA) throw new LoiNganHang("Tài khoản đã khóa");
        if(t.soDu < soTien) throw new LoiNganHang("Số dư không đủ");
        t.soDu -= soTien; tkDao.capNhatSoDu(tkId, t.soDu); gdDao.ghi(tkId, LoaiGiaoDich.RUT, soTien, moTa);
    }
    public synchronized void chuyenKhoan(long tkNguon, long tkDich, long soTien, String moTa) throws Exception{
        if(tkNguon==tkDich) throw new LoiNganHang("Không thể chuyển cùng một tài khoản");
        TaiKhoan a = tkDao.tim(tkNguon); TaiKhoan b = tkDao.tim(tkDich);
        if(a==null||b==null) throw new LoiNganHang("Tài khoản không tồn tại");
        if(a.trangThai==TrangThaiTaiKhoan.KHOA||b.trangThai==TrangThaiTaiKhoan.KHOA) throw new LoiNganHang("Một trong hai tài khoản bị khóa");
        if(a.soDu < soTien) throw new LoiNganHang("Số dư không đủ");
        a.soDu -= soTien; b.soDu += soTien;
        tkDao.capNhatSoDu(a.id, a.soDu); tkDao.capNhatSoDu(b.id, b.soDu);
        gdDao.ghi(a.id, LoaiGiaoDich.CHUYEN_DI, soTien, moTa+" -> "+b.id);
        gdDao.ghi(b.id, LoaiGiaoDich.CHUYEN_DEN, soTien, moTa+" <- "+a.id);
    }
    public java.util.List<GiaoDich> saoKe(long tkId) throws Exception{ return gdDao.saoKe(tkId); }
}
