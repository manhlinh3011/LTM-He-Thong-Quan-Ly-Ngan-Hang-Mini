package btl;

import java.io.*;
import java.net.*;
import java.util.*;

public class MayChuNganHang {
    public static final int PORT = 5556;
    public static void main(String[] args) throws Exception {
        DichVuNganHang service = new DichVuNganHang();
        service.seedAdmin();
        int port = PORT;
        try{ String sp = System.getProperty("server.port"); if(sp!=null) port = Integer.parseInt(sp); }catch(Exception ignored){}
        try{ String ev = System.getenv("BANK_PORT"); if(ev!=null) port = Integer.parseInt(ev); }catch(Exception ignored){}
        try{ if(args!=null && args.length>0) port = Integer.parseInt(args[0]); }catch(Exception ignored){}
        try(ServerSocket ss = new ServerSocket(port)){
            System.out.println("Máy chủ ngân hàng đang lắng nghe cổng "+port);
            while(true){
                Socket s = ss.accept();
                new XuLyClient(s, service).start();
            }
        }catch(BindException be){
            System.err.println("Không thể mở cổng "+port+" (đang được sử dụng). Hãy dừng server cũ hoặc chạy với cổng khác, ví dụ: java -cp bin btl.MayChuNganHang 5557");
            return;
        }
    }
}

class XuLyClient extends Thread {
    private final Socket socket; private final DichVuNganHang svc;
    public XuLyClient(Socket s, DichVuNganHang svc){ this.socket=s; this.svc=svc; }
    @Override public void run(){
        try(BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter pw = new PrintWriter(socket.getOutputStream(), true)){
            String line;
            while((line=br.readLine())!=null){
                Map<String,String> req = TienIch.parseKv(line);
                String cmd = req.get("cmd");
                Map<String,String> res = new LinkedHashMap<>();
                res.put("ok","true");
                try{
                    switch(cmd){
                        case "login":{
                            String u=req.get("u"), p=req.get("p");
                            VaiTro r = svc.dangNhap(u,p); res.put("role", r.name()); break; }
                        case "kh_tao":{
                            KhachHang k = svc.taoKhach(req.get("ten"), req.get("dob"), req.get("dc"), req.get("dt"), req.get("gt"));
                            res.put("id", String.valueOf(k.id)); break; }
                        case "kh_sua":{
                            KhachHang k = new KhachHang();
                            k.id = Long.parseLong(req.get("id"));
                            k.hoTen = req.get("ten"); k.ngaySinh = req.get("dob"); k.diaChi = req.get("dc");
                            k.dienThoai = req.get("dt"); k.soGiayTo = req.get("gt");
                            svc.capNhatKhach(k); break; }
                        case "kh_xoa":{
                            long id = Long.parseLong(req.get("id")); svc.xoaKhach(id); break; }
                        case "kh_ds":{
                            StringBuilder sb=new StringBuilder();
                            for(KhachHang k: svc.dsKhach()){
                                // Trả đủ: id, họ tên, điện thoại, ngày sinh, địa chỉ, số giấy tờ
                                // Loại bỏ dấu "," và ";" tránh vỡ định dạng
                                String hoTen = k.hoTen==null?"":k.hoTen.replace(";"," ").replace(","," ");
                                String dienThoai = k.dienThoai==null?"":k.dienThoai.replace(";"," ").replace(","," ");
                                String ngaySinh = k.ngaySinh==null?"":k.ngaySinh.replace(";"," ").replace(","," ");
                                String diaChi = k.diaChi==null?"":k.diaChi.replace(";"," ").replace(","," ");
                                String soGiayTo = k.soGiayTo==null?"":k.soGiayTo.replace(";"," ").replace(","," ");
                                sb.append(k.id).append(",").append(hoTen).append(",").append(dienThoai)
                                  .append(",").append(ngaySinh).append(",").append(diaChi).append(",").append(soGiayTo)
                                  .append(";");
                            }
                            res.put("data", sb.toString()); break; }
                        case "kh_get":{
                            long id = Long.parseLong(req.get("id"));
                            KhachHang k = null; for(KhachHang x: svc.dsKhach()){ if(x.id==id){ k=x; break; } }
                            if(k==null) throw new LoiNganHang("Không tìm thấy khách");
                            Map<String,String> d = new LinkedHashMap<>();
                            d.put("id", String.valueOf(k.id)); d.put("ten", k.hoTen); d.put("dt", k.dienThoai);
                            d.put("dob", k.ngaySinh); d.put("dc", k.diaChi); d.put("gt", k.soGiayTo);
                            res.putAll(d); break; }
                        case "tk_tao":{
                            long khId=Long.parseLong(req.get("kh")); LoaiTaiKhoan loai=LoaiTaiKhoan.valueOf(req.get("loai")); long sodu=Long.parseLong(req.get("sodu"));
                            String soTkStr = req.get("sotk");
                            TaiKhoan t = (soTkStr!=null && !soTkStr.trim().isEmpty())
                                ? svc.taoTaiKhoanVoiId(Long.parseLong(soTkStr.trim()), khId, loai, sodu)
                                : svc.taoTaiKhoan(khId, loai, sodu);
                            res.put("id", String.valueOf(t.id)); break; }
                        case "tk_khoa":{
                            long tkId = Long.parseLong(req.get("id")); boolean khoa = Boolean.parseBoolean(req.get("khoa"));
                            svc.khoaTaiKhoan(tkId, khoa); break; }
                        case "tk_sua":{
                            long tkId = Long.parseLong(req.get("id")); LoaiTaiKhoan loai=LoaiTaiKhoan.valueOf(req.get("loai"));
                            svc.suaLoaiTaiKhoan(tkId, loai); break; }
                        case "tk_sua_sodu":{
                            long tkId = Long.parseLong(req.get("id")); long sodu = Long.parseLong(req.get("sodu"));
                            svc.suaSoDuTaiKhoan(tkId, sodu); break; }
                        case "tk_doi_so":{
                            long cu = Long.parseLong(req.get("cu")); long moi = Long.parseLong(req.get("moi"));
                            svc.doiSoTaiKhoan(cu, moi); break; }
                        case "tk_xoa":{
                            long tkId = Long.parseLong(req.get("id")); svc.xoaTaiKhoan(tkId); break; }
                        case "tk_theokh":{
                            long khId=Long.parseLong(req.get("kh")); StringBuilder sb=new StringBuilder();
                            for(TaiKhoan t: svc.dsTaiKhoanTheoKhach(khId)){
                                sb.append(t.id).append(",").append(t.loai.name()).append(",").append(t.soDu).append(",").append(t.trangThai.name()).append(";");
                            }
                            res.put("data", sb.toString()); break; }
                        case "nap":{
                            svc.napTien(Long.parseLong(req.get("tk")), Long.parseLong(req.get("tien")), req.get("mota")); break; }
                        case "rut":{
                            svc.rutTien(Long.parseLong(req.get("tk")), Long.parseLong(req.get("tien")), req.get("mota")); break; }
                        case "chuyen":{
                            svc.chuyenKhoan(Long.parseLong(req.get("from")), Long.parseLong(req.get("to")), Long.parseLong(req.get("tien")), req.get("mota")); break; }
                        case "saoke":{
                            long tk=Long.parseLong(req.get("tk")); StringBuilder sb=new StringBuilder();
                            for(GiaoDich g: svc.saoKe(tk)){
                                sb.append(g.id).append(",").append(g.loai.name()).append(",").append(g.soTien).append(",").append(g.ngay.getTime()).append(",").append(g.moTa==null?"":g.moTa.replace(";"," ")).append(";");
                            }
                            res.put("data", sb.toString()); break; }
                        case "tk_goiy":{
                            long v = svc.goiYSoTaiKhoan(); res.put("val", String.valueOf(v)); break; }
                        default: throw new LoiNganHang("Lệnh không hỗ trợ: "+cmd);
                    }
                }catch(Exception ex){ res.put("ok","false"); res.put("err", ex.getMessage()); TienIch.ghiLog("ERR "+ex.getMessage()); }
                pw.println(TienIch.toKv(res));
            }
        }catch(Exception e){ TienIch.ghiLog("Client closed: "+e.getMessage()); }
    }
}
