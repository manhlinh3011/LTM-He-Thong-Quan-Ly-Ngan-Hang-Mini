package btl;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.border.TitledBorder;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;

public class UngDungKhachHang extends JFrame {
    private Socket socket; private BufferedReader br; private PrintWriter pw;

    private JTextField tfUser = new JTextField("admin");
    private JPasswordField tfPass = new JPasswordField("admin123");
    private CardLayout card = new CardLayout();
    private JPanel root = new JPanel(card);

    // Tabs
    private JTabbedPane tabs = new JTabbedPane();
    private JTable tbKhach = new JTable(new DefaultTableModel(new Object[]{"ID","Họ tên","Điện thoại","Ngày sinh","Địa chỉ","Số giấy tờ"},0));
    private JTextField tfTen = new JTextField();
    private JTextField tfDob = new JTextField("2000-01-01");
    private JTextField tfDc = new JTextField();
    private JTextField tfDt = new JTextField();
    private JTextField tfGt = new JTextField();

    private JTextField tfKhId = new JTextField();
    private JTable tbTk = new JTable(new DefaultTableModel(new Object[]{"Số TK","Loại","Số dư","Trạng thái","Họ tên KH","Điện thoại","Ngày sinh","Địa chỉ","Số giấy tờ"},0));
    private JComboBox<String> cbLoaiTk = new JComboBox<>(new String[]{"THANH_TOAN","TIET_KIEM"});
    private JTextField tfSoDuBd = new JTextField("0");
    private JButton btnKhoaMo = new JButton("Khóa/Mở khóa TK");
    private JTextField tfSoTkChon = new JTextField();
    // Form sửa TK giống tab Khách hàng
    private JTextField tfTkEdit = new JTextField();
    private JComboBox<String> cbLoaiEdit = new JComboBox<>(new String[]{"THANH_TOAN","TIET_KIEM"});
    private JTextField tfSoDuEdit = new JTextField();
    private JTextField tfSoTkMoi = new JTextField();
    private JCheckBox chkKhoaEdit = new JCheckBox("Khóa tài khoản");

    private JTextField tfTkNap = new JTextField();
    private JTextField tfTkRut = new JTextField();
    private JTextField tfTkFrom = new JTextField();
    private JTextField tfTkTo = new JTextField();
    private JTextField tfTienNap = new JTextField();
    private JTextField tfTienRut = new JTextField();
    private JTextField tfTienChuyen = new JTextField();

    private JTextField tfTkSaoKe = new JTextField();
    private JTable tbSaoKe = new JTable(new DefaultTableModel(new Object[]{"ID","Loại","Số tiền","Ngày","Mô tả"},0));

    public UngDungKhachHang(){
        super("Mini Bank Client (TCP)");
        setSize(1000, 650); setLocationRelativeTo(null); setDefaultCloseOperation(EXIT_ON_CLOSE);
        ketNoiServer();
        add(root);
        taoManHinhDangNhap();
        taoManHinhChinh();
        card.show(root, "login");
    }

    private void ketNoiServer(){
        try{
            int port = MayChuNganHang.PORT;
            try{ String sp = System.getProperty("server.port"); if(sp!=null) port = Integer.parseInt(sp); }catch(Exception ignored){}
            try{ String ev = System.getenv("BANK_PORT"); if(ev!=null) port = Integer.parseInt(ev); }catch(Exception ignored){}
            socket = new Socket("localhost", port);
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            pw = new PrintWriter(socket.getOutputStream(), true);
        }catch(Exception e){ JOptionPane.showMessageDialog(this, "Không kết nối được server: "+e.getMessage()); System.exit(0);} }

    private Map<String,String> goi(Map<String,String> req){
        try{ pw.println(TienIch.toKv(req)); String res = br.readLine(); return TienIch.parseKv(res);}catch(Exception e){ JOptionPane.showMessageDialog(this, "Lỗi kết nối: "+e.getMessage()); return Collections.emptyMap(); }
    }

    private void taoManHinhDangNhap(){
        JPanel p = new JPanel(new GridBagLayout()); root.add(p, "login");
        GridBagConstraints c = new GridBagConstraints(); c.insets=new Insets(8,8,8,8); c.fill=GridBagConstraints.HORIZONTAL;
        c.gridx=0;c.gridy=0; p.add(new JLabel("Tên đăng nhập:"), c);
        c.gridx=1; p.add(tfUser, c);
        c.gridx=0;c.gridy=1; p.add(new JLabel("Mật khẩu:"), c);
        c.gridx=1; p.add(tfPass, c);
        JButton btn = new JButton("Đăng nhập"); c.gridx=1;c.gridy=2; p.add(btn,c);
        btn.addActionListener(e -> dangNhap());
    }

    private void dangNhap(){
        Map<String,String> req = new LinkedHashMap<>(); req.put("cmd","login"); req.put("u", tfUser.getText()); req.put("p", new String(tfPass.getPassword()));
        Map<String,String> res = goi(req);
        if("true".equals(res.get("ok"))){
            setTitle("Mini Bank — Vai trò: "+res.get("role"));
            napBangKhach();
            card.show(root, "main");
        }else JOptionPane.showMessageDialog(this, res.get("err"));
    }

    private void taoManHinhChinh(){
        JPanel pnl = new JPanel(new BorderLayout()); pnl.add(tabs);
        root.add(pnl, "main");

        // Tab Khách hàng
        JPanel a = new JPanel(new BorderLayout());
        // Thanh tìm kiếm Khách hàng
        JPanel searchKh = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField tfSearchKh = new JTextField(); tfSearchKh.setColumns(20);
        searchKh.add(new JLabel("Tìm KH:")); searchKh.add(tfSearchKh);
        JButton btnExportKh = new JButton("Xuất CSV"); searchKh.add(btnExportKh);
        a.add(searchKh, BorderLayout.NORTH);
        // Bật sắp xếp + chuẩn bị sorter để lọc
        TableRowSorter<DefaultTableModel> sorterKh = new TableRowSorter<>((DefaultTableModel) tbKhach.getModel());
        tbKhach.setRowSorter(sorterKh);
        a.add(new JScrollPane(tbKhach), BorderLayout.CENTER);
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gf = new GridBagConstraints();
        gf.insets = new Insets(6,6,6,6);
        gf.fill = GridBagConstraints.HORIZONTAL;
        TitledBorder tbAdd = BorderFactory.createTitledBorder("Thêm khách hàng");
        tbAdd.setTitleJustification(TitledBorder.CENTER);
        tbAdd.setTitlePosition(TitledBorder.TOP);
        form.setBorder(tbAdd);
        int r = 0;
        gf.gridy = r; gf.gridx = 0; form.add(new JLabel("Họ tên"), gf); gf.gridx = 1; gf.weightx = 1; form.add(tfTen, gf);
        r++; gf.gridy = r; gf.gridx = 0; gf.weightx = 0; form.add(new JLabel("Ngày sinh(yyyy-MM-dd)"), gf); gf.gridx = 1; gf.weightx = 1; form.add(tfDob, gf);
        r++; gf.gridy = r; gf.gridx = 0; gf.weightx = 0; form.add(new JLabel("Địa chỉ"), gf); gf.gridx = 1; gf.weightx = 1; form.add(tfDc, gf);
        r++; gf.gridy = r; gf.gridx = 0; gf.weightx = 0; form.add(new JLabel("Điện thoại"), gf); gf.gridx = 1; gf.weightx = 1; form.add(tfDt, gf);
        r++; gf.gridy = r; gf.gridx = 0; gf.weightx = 0; form.add(new JLabel("Số giấy tờ"), gf); gf.gridx = 1; gf.weightx = 1; form.add(tfGt, gf);
        r++; gf.gridy = r; gf.gridx = 0; gf.gridwidth = 1; JButton btnThem = new JButton("Thêm"); form.add(btnThem, gf); gf.gridx = 1; JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); JButton btnSua = new JButton("Sửa KH đã chọn"); JButton btnXoa = new JButton("Xóa KH đã chọn"); actions.add(btnSua); actions.add(btnXoa); form.add(actions, gf);
        btnThem.addActionListener(e -> themKhach());
        btnSua.addActionListener(e -> suaKhach());
        btnXoa.addActionListener(e -> xoaKhach());
        a.add(form, BorderLayout.SOUTH);
        tabs.addTab("Khách hàng", a);
        // Khi chọn khách ở bảng, tự điền ID vào ô tìm tài khoản
        tbKhach.getSelectionModel().addListSelectionListener(e -> {
            if(e.getValueIsAdjusting()) return;
            int row = tbKhach.getSelectedRow();
            if(row >= 0){
                Object v = tbKhach.getValueAt(row, 0);
                if(v!=null) tfKhId.setText(String.valueOf(v));
                // Đổ dữ liệu lên form để sửa nhanh
                tfTen.setText(val(tbKhach, row, 1));
                tfDt.setText(val(tbKhach, row, 2));
                tfDob.setText(val(tbKhach, row, 3));
                tfDc.setText(val(tbKhach, row, 4));
                tfGt.setText(val(tbKhach, row, 5));
            }
        });
        // Double click để chuyển sang tab Tài khoản và tải danh sách
        tbKhach.addMouseListener(new MouseAdapter(){
            @Override public void mouseClicked(MouseEvent e){
                if(e.getClickCount()==2){ taiTkTheoKhach(); tabs.setSelectedIndex(1); }
            }
        });

        // Tab Tài khoản
        JPanel b = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,6,6,6);
        g.fill = GridBagConstraints.HORIZONTAL;

        tfKhId.setEditable(false);
        tfKhId.setColumns(10);
        tfSoDuBd.setColumns(8);
        tfSoTkChon.setColumns(12);

        JButton btnChonKh = new JButton("Chọn từ KH đã chọn");
        JButton btnTai = new JButton("Tải danh sách TK");
        JButton btnGoiY = new JButton("Gợi ý số TK");
        JButton btnTaoTk = new JButton("Tạo TK");

        java.awt.Dimension btnSize = new java.awt.Dimension(140, 28);
        btnChonKh.setPreferredSize(btnSize);
        btnTai.setPreferredSize(btnSize);
        btnGoiY.setPreferredSize(btnSize);
        btnTaoTk.setPreferredSize(btnSize);

        int col = 0;
        // Row 0: ID Khách, chọn, tải
        g.gridy = 0; g.gridx = col++; top.add(new JLabel("ID Khách:"), g);
        g.gridx = col++; g.weightx = 0.2; top.add(tfKhId, g);
        g.gridx = col++; g.weightx = 0; top.add(btnChonKh, g);
        g.gridx = col++; top.add(btnTai, g);

        // Row 1: Loại, Số dư ban đầu, Số TK tự chọn, Gợi ý, Tạo TK
        col = 0; g.gridy = 1;
        g.gridx = col++; top.add(new JLabel("Loại:"), g);
        g.gridx = col++; g.weightx = 0.2; top.add(cbLoaiTk, g);
        g.gridx = col++; g.weightx = 0; top.add(new JLabel("Số dư ban đầu:"), g);
        g.gridx = col++; g.weightx = 0.2; top.add(tfSoDuBd, g);
        g.gridx = col++; g.weightx = 0; top.add(new JLabel("Số TK (tự chọn):"), g);
        g.gridx = col++; g.weightx = 0.2; top.add(tfSoTkChon, g);
        g.gridx = col++; g.weightx = 0; top.add(btnGoiY, g);
        g.gridx = col++; top.add(btnTaoTk, g);

        btnChonKh.addActionListener(e -> {
            int row = tbKhach.getSelectedRow();
            if(row<0){ JOptionPane.showMessageDialog(this, "Hãy chọn một khách ở tab Khách hàng"); return; }
            Object v = tbKhach.getValueAt(row,0);
            if(v!=null){ tfKhId.setText(String.valueOf(v)); taiTkTheoKhach(); }
        });
        btnTai.addActionListener(e -> taiTkTheoKhach());
        btnGoiY.addActionListener(e -> goiYSoTk());
        btnTaoTk.addActionListener(e -> taoTk());
        // Thanh tìm kiếm TK
        JPanel searchTk = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField tfSearchTk = new JTextField(); tfSearchTk.setColumns(24);
        searchTk.add(new JLabel("Tìm TK/Loại/Trạng thái:")); searchTk.add(tfSearchTk);
        JButton btnExportTk = new JButton("Xuất CSV"); searchTk.add(btnExportTk);
        // Bật sắp xếp + sorter cho bảng TK
        TableRowSorter<DefaultTableModel> sorterTk = new TableRowSorter<>((DefaultTableModel) tbTk.getModel());
        tbTk.setRowSorter(sorterTk);
        // Form sửa tài khoản: căn đều tiêu đề và ô nhập theo cột
        JPanel formTk = new JPanel(new GridBagLayout());
        TitledBorder tbEdit = BorderFactory.createTitledBorder("Sửa tài khoản");
        tbEdit.setTitleJustification(TitledBorder.CENTER);
        tbEdit.setTitlePosition(TitledBorder.TOP);
        formTk.setBorder(tbEdit);

        GridBagConstraints ge = new GridBagConstraints();
        ge.insets = new Insets(6,6,6,6);
        ge.fill = GridBagConstraints.HORIZONTAL;

        tfTkEdit.setColumns(12); tfTkEdit.setEditable(false);
        tfSoDuEdit.setColumns(12); tfSoTkMoi.setColumns(12);

        java.awt.Dimension actBtn = new java.awt.Dimension(120, 28);
        JButton btnCapNhatTk = new JButton("Cập nhật"); btnCapNhatTk.setPreferredSize(actBtn);
        JButton btnThemTkMoi = new JButton("Thêm TK mới"); btnThemTkMoi.setPreferredSize(actBtn);
        JButton btnXoaTkForm = new JButton("Xóa TK"); btnXoaTkForm.setPreferredSize(actBtn);

        int rowLbl = 0;
        int colx = 0;
        ge.gridy = rowLbl; ge.gridx = colx++; ge.weightx = 0; formTk.add(new JLabel("Số TK"), ge);
        ge.gridx = colx++; formTk.add(new JLabel("Loại"), ge);
        ge.gridx = colx++; formTk.add(new JLabel("Số dư mới"), ge);
        ge.gridx = colx++; formTk.add(new JLabel("Số TK mới"), ge);
        ge.gridx = colx++; formTk.add(new JLabel("Trạng thái"), ge);
        // chừa cột cho các nút
        ge.gridx = colx++; formTk.add(new JLabel(""), ge);
        ge.gridx = colx++; formTk.add(new JLabel(""), ge);
        ge.gridx = colx++; formTk.add(new JLabel(""), ge);

        int rowInp = 1; colx = 0;
        ge.gridy = rowInp; ge.gridx = colx++; ge.weightx = 0.2; formTk.add(tfTkEdit, ge);
        ge.gridx = colx++; ge.weightx = 0.2; formTk.add(cbLoaiEdit, ge);
        ge.gridx = colx++; ge.weightx = 0.2; formTk.add(tfSoDuEdit, ge);
        ge.gridx = colx++; ge.weightx = 0.2; formTk.add(tfSoTkMoi, ge);
        ge.gridx = colx++; ge.weightx = 0; formTk.add(chkKhoaEdit, ge);
        ge.gridx = colx++; ge.weightx = 0; formTk.add(btnCapNhatTk, ge);
        ge.gridx = colx++; formTk.add(btnThemTkMoi, ge);
        ge.gridx = colx++; formTk.add(btnXoaTkForm, ge);

        btnCapNhatTk.addActionListener(e -> capNhatTkTuForm());
        btnThemTkMoi.addActionListener(e -> taoTkMoiFromForm());
        btnXoaTkForm.addActionListener(e -> xoaTk());
        // Gom các thành phần phía trên vào vùng NORTH theo cột dọc
        JPanel north = new JPanel(); north.setLayout(new BoxLayout(north, javax.swing.BoxLayout.Y_AXIS));
        north.add(top); north.add(formTk); north.add(searchTk);
        b.add(north, BorderLayout.NORTH);
        b.add(new JScrollPane(tbTk), BorderLayout.CENTER);
        tabs.addTab("Tài khoản", b);
        // Khi chọn tài khoản, tự điền số TK vào các ô giao dịch và cập nhật nhãn nút khóa
        tbTk.getSelectionModel().addListSelectionListener(e -> {
            if(e.getValueIsAdjusting()) return;
            int row = tbTk.getSelectedRow();
            if(row >= 0){
                Object id = tbTk.getValueAt(row, 0);
                if(id!=null){
                    String s = String.valueOf(id);
                    tfTkNap.setText(s);
                    tfTkRut.setText(s);
                    if(tfTkFrom.getText()==null || tfTkFrom.getText().trim().isEmpty()) tfTkFrom.setText(s);
                    tfTkSaoKe.setText(s);
                    // Đổ vào form sửa TK
                    tfTkEdit.setText(s);
                    cbLoaiEdit.setSelectedItem(String.valueOf(tbTk.getValueAt(row,1)));
                    String st = String.valueOf(tbTk.getValueAt(row,3));
                    chkKhoaEdit.setSelected("KHOA".equals(st));
                    tfSoDuEdit.setText(boDinhDangVND(String.valueOf(tbTk.getValueAt(row,2))));
                    tfSoTkMoi.setText("");
                }
                capNhatNhanNutKhoa();
            }
        });
        // Double click để mở sao kê cho tài khoản được chọn
        tbTk.addMouseListener(new MouseAdapter(){
            @Override public void mouseClicked(MouseEvent e){
                if(e.getClickCount()==2){ moSaoKeTheoDongTk(); }
            }
        });

        // Tab Giao dịch
        JPanel c = new JPanel(new GridLayout(3,1));
        JPanel nap = new JPanel(new FlowLayout(FlowLayout.LEFT));
        TitledBorder tbNap = BorderFactory.createTitledBorder("Nạp tiền");
        tbNap.setTitleJustification(TitledBorder.CENTER);
        tbNap.setTitlePosition(TitledBorder.TOP);
        nap.setBorder(tbNap);
        tfTkNap.setColumns(12); tfTkRut.setColumns(12); tfTkFrom.setColumns(12); tfTkTo.setColumns(12); tfTienNap.setColumns(10); tfTienRut.setColumns(10); tfTienChuyen.setColumns(10);
        nap.add(new JLabel("Số TK:")); nap.add(tfTkNap); nap.add(new JLabel("Số tiền:")); nap.add(tfTienNap); JButton btnNap = new JButton("Nạp"); nap.add(btnNap);
        btnNap.addActionListener(e -> napTien());
        JPanel rut = new JPanel(new FlowLayout(FlowLayout.LEFT));
        TitledBorder tbRut = BorderFactory.createTitledBorder("Rút tiền");
        tbRut.setTitleJustification(TitledBorder.CENTER);
        tbRut.setTitlePosition(TitledBorder.TOP);
        rut.setBorder(tbRut);
        rut.add(new JLabel("Số TK:")); rut.add(tfTkRut); rut.add(new JLabel("Số tiền:")); rut.add(tfTienRut); JButton btnRut = new JButton("Rút"); rut.add(btnRut);
        btnRut.addActionListener(e -> rutTien());
        JPanel chuyen = new JPanel(new FlowLayout(FlowLayout.LEFT));
        TitledBorder tbChuyen = BorderFactory.createTitledBorder("Chuyển khoản");
        tbChuyen.setTitleJustification(TitledBorder.CENTER);
        tbChuyen.setTitlePosition(TitledBorder.TOP);
        chuyen.setBorder(tbChuyen);
        chuyen.add(new JLabel("Từ TK:")); chuyen.add(tfTkFrom); chuyen.add(new JLabel("Đến TK:")); chuyen.add(tfTkTo); chuyen.add(new JLabel("Số tiền:")); chuyen.add(tfTienChuyen); JButton btnChuyen = new JButton("Chuyển"); chuyen.add(btnChuyen);
        btnChuyen.addActionListener(e -> chuyenKhoan());
        c.add(nap); c.add(rut); c.add(chuyen);
        tabs.addTab("Giao dịch", c);

        // Tab Sao kê
        JPanel d = new JPanel(new BorderLayout());
        JPanel skTop = new JPanel(new GridBagLayout());
        GridBagConstraints gs = new GridBagConstraints(); gs.insets = new Insets(6,6,6,6); gs.fill = GridBagConstraints.HORIZONTAL;
        gs.gridx=0; gs.gridy=0; skTop.add(new JLabel("Số TK:"), gs);
        gs.gridx=1; gs.weightx=1; tfTkSaoKe.setColumns(14); skTop.add(tfTkSaoKe, gs);
        gs.gridx=2; gs.weightx=0; JButton btnSk = new JButton("Xem sao kê"); skTop.add(btnSk, gs);
        btnSk.addActionListener(e -> xemSaoKe());
        d.add(skTop, BorderLayout.NORTH); d.add(new JScrollPane(tbSaoKe), BorderLayout.CENTER);
        tabs.addTab("Sao kê", d);

        // Lọc dữ liệu theo nhập liệu (client-side) sử dụng RowFilter an toàn
        tfSearchKh.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
            private void filter(){
                String q = tfSearchKh.getText().trim();
                if(q.isEmpty()){ sorterKh.setRowFilter(null); return; }
                sorterKh.setRowFilter(RowFilter.regexFilter("(?i)"+java.util.regex.Pattern.quote(q)));
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e){ filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e){ filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e){ filter(); }
        });

        tfSearchTk.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
            private void filter(){
                String q = tfSearchTk.getText().trim();
                if(q.isEmpty()){ sorterTk.setRowFilter(null); return; }
                sorterTk.setRowFilter(RowFilter.regexFilter("(?i)"+java.util.regex.Pattern.quote(q)));
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e){ filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e){ filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e){ filter(); }
        });

        // Hành động xuất CSV
        btnExportKh.addActionListener(e -> xuatCsv(tbKhach, "khach_hang.csv"));
        btnExportTk.addActionListener(e -> xuatCsv(tbTk, "tai_khoan.csv"));
    }

    private void napBangKhach(){
        DefaultTableModel m = (DefaultTableModel) tbKhach.getModel(); m.setRowCount(0);
        Map<String,String> req = new LinkedHashMap<>(); req.put("cmd","kh_ds"); Map<String,String> res = goi(req);
        if(!"true".equals(res.get("ok"))){ JOptionPane.showMessageDialog(this, res.get("err")); return; }
        String data = res.get("data"); if(data==null||data.isEmpty()) return;
        for(String row: data.split(";")){
            if(row.trim().isEmpty()) continue; String[] p = row.split(",");
            m.addRow(new Object[]{p[0], p[1], p[2], p.length>3?p[3]:"", p.length>4?p[4]:"", p.length>5?p[5]:""});
        }
    }

    private void themKhach(){
        Map<String,String> req = new LinkedHashMap<>(); req.put("cmd","kh_tao"); req.put("ten", tfTen.getText()); req.put("dob", tfDob.getText()); req.put("dc", tfDc.getText()); req.put("dt", tfDt.getText()); req.put("gt", tfGt.getText());
        Map<String,String> res = goi(req);
        if("true".equals(res.get("ok"))){ JOptionPane.showMessageDialog(this, "Đã thêm khách: ID="+res.get("id")); napBangKhach(); }
        else JOptionPane.showMessageDialog(this, res.get("err"));
    }

    private void suaKhach(){
        int row = tbKhach.getSelectedRow();
        if(row<0){ JOptionPane.showMessageDialog(this, "Chọn một khách hàng để sửa"); return; }
        String id = String.valueOf(tbKhach.getValueAt(row,0));
        Map<String,String> req = new LinkedHashMap<>();
        req.put("cmd","kh_sua");
        req.put("id", id);
        req.put("ten", tfTen.getText());
        req.put("dob", tfDob.getText());
        req.put("dc", tfDc.getText());
        req.put("dt", tfDt.getText());
        req.put("gt", tfGt.getText());
        Map<String,String> res = goi(req);
        if("true".equals(res.get("ok"))){ JOptionPane.showMessageDialog(this, "Đã cập nhật khách hàng"); napBangKhach(); }
        else JOptionPane.showMessageDialog(this, res.get("err"));
    }

    private void xoaKhach(){
        int row = tbKhach.getSelectedRow();
        if(row<0){ JOptionPane.showMessageDialog(this, "Chọn một khách hàng để xóa"); return; }
        String id = String.valueOf(tbKhach.getValueAt(row,0));
        int cf = JOptionPane.showConfirmDialog(this, "Xóa khách ID="+id+"?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if(cf!=JOptionPane.YES_OPTION) return;
        Map<String,String> req = new LinkedHashMap<>(); req.put("cmd","kh_xoa"); req.put("id", id);
        Map<String,String> res = goi(req);
        if("true".equals(res.get("ok"))){ JOptionPane.showMessageDialog(this, "Đã xóa"); napBangKhach(); }
        else JOptionPane.showMessageDialog(this, res.get("err"));
    }

    private void taiTkTheoKhach(){
        DefaultTableModel m = (DefaultTableModel) tbTk.getModel(); m.setRowCount(0);
        Map<String,String> req = new LinkedHashMap<>(); req.put("cmd","tk_theokh"); req.put("kh", tfKhId.getText()); Map<String,String> res = goi(req);
        if(!"true".equals(res.get("ok"))){ JOptionPane.showMessageDialog(this, res.get("err")); return; }
        String data = res.get("data"); if(data==null||data.isEmpty()) return;
        // Lấy thông tin KH một lần
        String ten="", dt="", dob="", dc="", gt="";
        try{
            Map<String,String> reqKh = new LinkedHashMap<>(); reqKh.put("cmd","kh_get"); reqKh.put("id", tfKhId.getText());
            Map<String,String> resKh = goi(reqKh);
            if("true".equals(resKh.get("ok"))){
                ten = resKh.getOrDefault("ten",""); dt = resKh.getOrDefault("dt","");
                dob = resKh.getOrDefault("dob",""); dc = resKh.getOrDefault("dc",""); gt = resKh.getOrDefault("gt","");
            }
        }catch(Exception ignored){}
        for(String row: data.split(";")){
            if(row.trim().isEmpty()) continue; String[] p = row.split(",");
            m.addRow(new Object[]{p[0], p[1], TienIch.dinhDangVND(Long.parseLong(p[2])), p[3], ten, dt, dob, dc, gt});
        }
    }

    private void taoTk(){
        if(!isPositiveNumber(tfKhId.getText())){ JOptionPane.showMessageDialog(this, "ID Khách không hợp lệ"); return; }
        if(!isNonNegativeNumber(tfSoDuBd.getText())){ JOptionPane.showMessageDialog(this, "Số dư ban đầu phải là số không âm"); return; }
        String kh = tfKhId.getText().trim();
        String loai = String.valueOf(cbLoaiTk.getSelectedItem());
        String sodu = tfSoDuBd.getText().trim().isEmpty()?"0":tfSoDuBd.getText().trim();
        Map<String,String> req = new LinkedHashMap<>(); req.put("cmd","tk_tao"); req.put("kh", kh); req.put("loai", loai); req.put("sodu", sodu);
        String soTk = tfSoTkChon.getText().trim();
        if(!soTk.isEmpty()){
            if(!isPositiveNumber(soTk) || soTk.length()<6){ JOptionPane.showMessageDialog(this, "Số TK tự chọn phải là số dương, tối thiểu 6 chữ số"); return; }
            req.put("sotk", soTk);
        }
        Map<String,String> res = goi(req);
        if("true".equals(res.get("ok"))){ JOptionPane.showMessageDialog(this, "Đã tạo tài khoản: "+res.get("id")); taiTkTheoKhach(); }
        else JOptionPane.showMessageDialog(this, res.get("err"));
    }

    private void khoaMoKhoaTk(){
        int row = tbTk.getSelectedRow();
        if(row<0){ JOptionPane.showMessageDialog(this, "Chọn một tài khoản trong bảng để khóa/mở khóa"); return; }
        String id = String.valueOf(tbTk.getValueAt(row,0));
        String st = String.valueOf(tbTk.getValueAt(row,3));
        boolean seKhoa = "HOAT_DONG".equals(st);
        Map<String,String> req = new LinkedHashMap<>(); req.put("cmd","tk_khoa"); req.put("id", id); req.put("khoa", String.valueOf(seKhoa));
        Map<String,String> res = goi(req);
        if("true".equals(res.get("ok"))){ JOptionPane.showMessageDialog(this, (seKhoa?"Đã khóa":"Đã mở khóa")); taiTkTheoKhach(); }
        else JOptionPane.showMessageDialog(this, res.get("err"));
    }

    private void xoaTk(){
        int row = tbTk.getSelectedRow();
        if(row<0){ JOptionPane.showMessageDialog(this, "Chọn một tài khoản trong bảng để xóa"); return; }
        String id = String.valueOf(tbTk.getValueAt(row,0));
        int cf = JOptionPane.showConfirmDialog(this, "Xóa tài khoản "+id+"?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if(cf!=JOptionPane.YES_OPTION) return;
        Map<String,String> req = new LinkedHashMap<>(); req.put("cmd","tk_xoa"); req.put("id", id);
        Map<String,String> res = goi(req);
        if("true".equals(res.get("ok"))){ JOptionPane.showMessageDialog(this, "Đã xóa tài khoản"); taiTkTheoKhach(); }
        else JOptionPane.showMessageDialog(this, res.get("err"));
    }

    private void suaTk(){
        int row = tbTk.getSelectedRow();
        if(row<0){ JOptionPane.showMessageDialog(this, "Chọn một tài khoản trong bảng để sửa"); return; }
        String id = String.valueOf(tbTk.getValueAt(row,0));
        String loai = String.valueOf(cbLoaiTk.getSelectedItem());
        Map<String,String> req = new LinkedHashMap<>(); req.put("cmd","tk_sua"); req.put("id", id); req.put("loai", loai);
        Map<String,String> res = goi(req);
        if("true".equals(res.get("ok"))){ JOptionPane.showMessageDialog(this, "Đã cập nhật loại tài khoản"); taiTkTheoKhach(); }
        else JOptionPane.showMessageDialog(this, res.get("err"));
    }

    private void suaSoDu(String soDuMoi){
        int row = tbTk.getSelectedRow();
        if(row<0){ JOptionPane.showMessageDialog(this, "Chọn một tài khoản trong bảng để sửa số dư"); return; }
        if(!isNonNegativeNumber(soDuMoi)){ JOptionPane.showMessageDialog(this, "Số dư mới phải là số không âm"); return; }
        String id = String.valueOf(tbTk.getValueAt(row,0));
        Map<String,String> req = new LinkedHashMap<>(); req.put("cmd","tk_sua_sodu"); req.put("id", id); req.put("sodu", soDuMoi.trim());
        Map<String,String> res = goi(req);
        if("true".equals(res.get("ok"))){ JOptionPane.showMessageDialog(this, "Đã cập nhật số dư"); taiTkTheoKhach(); }
        else JOptionPane.showMessageDialog(this, res.get("err"));
    }

    private void doiSoTk(String soMoi){
        int row = tbTk.getSelectedRow();
        if(row<0){ JOptionPane.showMessageDialog(this, "Chọn một tài khoản trong bảng để đổi số"); return; }
        if(!isPositiveNumber(soMoi) || soMoi.length()<6){ JOptionPane.showMessageDialog(this, "Số TK mới phải là số dương tối thiểu 6 chữ số"); return; }
        String idCu = String.valueOf(tbTk.getValueAt(row,0));
        Map<String,String> req = new LinkedHashMap<>(); req.put("cmd","tk_doi_so"); req.put("cu", idCu); req.put("moi", soMoi.trim());
        Map<String,String> res = goi(req);
        if("true".equals(res.get("ok"))){ JOptionPane.showMessageDialog(this, "Đã đổi số tài khoản"); taiTkTheoKhach(); }
        else JOptionPane.showMessageDialog(this, res.get("err"));
    }

    private String boDinhDangVND(String s){
        if(s==null) return "";
        return s.replace(" ", "").replace("đ", "").replace(",", "");
    }

    private void capNhatTkTuForm(){
        String id = tfTkEdit.getText().trim(); if(!isPositiveNumber(id)){ JOptionPane.showMessageDialog(this, "Chưa chọn tài khoản"); return; }
        String loai = String.valueOf(cbLoaiEdit.getSelectedItem());
        boolean khoa = chkKhoaEdit.isSelected();
        // 1) Sửa loại
        Map<String,String> req1 = new LinkedHashMap<>(); req1.put("cmd","tk_sua"); req1.put("id", id); req1.put("loai", loai); Map<String,String> r1 = goi(req1);
        if(!"true".equals(r1.get("ok"))){ JOptionPane.showMessageDialog(this, r1.get("err")); return; }
        // 2) Sửa trạng thái
        Map<String,String> req2 = new LinkedHashMap<>(); req2.put("cmd","tk_khoa"); req2.put("id", id); req2.put("khoa", String.valueOf(khoa)); Map<String,String> r2 = goi(req2);
        if(!"true".equals(r2.get("ok"))){ JOptionPane.showMessageDialog(this, r2.get("err")); return; }
        // 3) Sửa số dư nếu có
        String soDuMoi = boDinhDangVND(tfSoDuEdit.getText());
        if(!soDuMoi.trim().isEmpty()){
            if(!isNonNegativeNumber(soDuMoi)){ JOptionPane.showMessageDialog(this, "Số dư mới không hợp lệ"); return; }
            Map<String,String> req3 = new LinkedHashMap<>(); req3.put("cmd","tk_sua_sodu"); req3.put("id", id); req3.put("sodu", soDuMoi.trim()); Map<String,String> r3 = goi(req3);
            if(!"true".equals(r3.get("ok"))){ JOptionPane.showMessageDialog(this, r3.get("err")); return; }
        }
        // 4) Đổi số TK nếu nhập
        String soMoi = tfSoTkMoi.getText().trim();
        if(!soMoi.isEmpty()){
            if(!isPositiveNumber(soMoi) || soMoi.length()<6){ JOptionPane.showMessageDialog(this, "Số TK mới phải là số dương tối thiểu 6 chữ số"); return; }
            Map<String,String> req4 = new LinkedHashMap<>(); req4.put("cmd","tk_doi_so"); req4.put("cu", id); req4.put("moi", soMoi); Map<String,String> r4 = goi(req4);
            if(!"true".equals(r4.get("ok"))){ JOptionPane.showMessageDialog(this, r4.get("err")); return; }
        }
        JOptionPane.showMessageDialog(this, "Đã cập nhật tài khoản");
        taiTkTheoKhach();
    }

    private void taoTkMoiFromForm(){
        if(!isPositiveNumber(tfKhId.getText())){ JOptionPane.showMessageDialog(this, "Chưa chọn khách hàng"); return; }
        String kh = tfKhId.getText().trim();
        String loai = String.valueOf(cbLoaiEdit.getSelectedItem());
        // Ưu tiên lấy ở form; nếu trống, dùng ô trên cùng (Số dư ban đầu/Số TK tự chọn)
        String soDu = boDinhDangVND(tfSoDuEdit.getText());
        if(soDu.isEmpty()) soDu = boDinhDangVND(tfSoDuBd.getText());
        if(soDu.isEmpty()) soDu = "0";
        if(!isNonNegativeNumber(soDu)){ JOptionPane.showMessageDialog(this, "Số dư ban đầu không hợp lệ"); return; }
        String soTk = tfSoTkMoi.getText().trim();
        if(soTk.isEmpty()) soTk = tfSoTkChon.getText().trim();
        Map<String,String> req = new LinkedHashMap<>(); req.put("cmd","tk_tao"); req.put("kh", kh); req.put("loai", loai); req.put("sodu", soDu);
        if(!soTk.isEmpty()){
            if(!isPositiveNumber(soTk) || soTk.length()<6){ JOptionPane.showMessageDialog(this, "Số TK tự chọn phải là số dương, tối thiểu 6 chữ số"); return; }
            req.put("sotk", soTk);
        }
        Map<String,String> res = goi(req);
        if("true".equals(res.get("ok"))){ JOptionPane.showMessageDialog(this, "Đã thêm tài khoản: "+res.get("id")); taiTkTheoKhach(); }
        else JOptionPane.showMessageDialog(this, res.get("err"));
    }

    private void napTien(){
        if(!isPositiveNumber(tfTkNap.getText())){ JOptionPane.showMessageDialog(this, "Số TK nạp không hợp lệ"); return; }
        if(!isPositiveNumber(tfTienNap.getText())){ JOptionPane.showMessageDialog(this, "Số tiền nạp phải là số dương"); return; }
        Map<String,String> req = new LinkedHashMap<>(); req.put("cmd","nap"); req.put("tk", tfTkNap.getText().trim()); req.put("tien", tfTienNap.getText().trim()); req.put("mota","Nap tien");
        Map<String,String> res = goi(req);
        if("true".equals(res.get("ok"))){
            JOptionPane.showMessageDialog(this, "Nạp thành công");
            capNhatDanhSachTkNeuCo();
        } else JOptionPane.showMessageDialog(this, res.get("err"));
    }
    private void rutTien(){
        if(!isPositiveNumber(tfTkRut.getText())){ JOptionPane.showMessageDialog(this, "Số TK rút không hợp lệ"); return; }
        if(!isPositiveNumber(tfTienRut.getText())){ JOptionPane.showMessageDialog(this, "Số tiền rút phải là số dương"); return; }
        Map<String,String> req = new LinkedHashMap<>(); req.put("cmd","rut"); req.put("tk", tfTkRut.getText().trim()); req.put("tien", tfTienRut.getText().trim()); req.put("mota","Rut tien");
        Map<String,String> res = goi(req);
        if("true".equals(res.get("ok"))){
            JOptionPane.showMessageDialog(this, "Rút thành công");
            capNhatDanhSachTkNeuCo();
        } else JOptionPane.showMessageDialog(this, res.get("err"));
    }
    private void chuyenKhoan(){
        if(!isPositiveNumber(tfTkFrom.getText()) || !isPositiveNumber(tfTkTo.getText())){ JOptionPane.showMessageDialog(this, "Số TK không hợp lệ"); return; }
        if(tfTkFrom.getText().trim().equals(tfTkTo.getText().trim())){ JOptionPane.showMessageDialog(this, "Không thể chuyển giữa cùng một tài khoản"); return; }
        if(!isPositiveNumber(tfTienChuyen.getText())){ JOptionPane.showMessageDialog(this, "Số tiền chuyển phải là số dương"); return; }
        Map<String,String> req = new LinkedHashMap<>(); req.put("cmd","chuyen"); req.put("from", tfTkFrom.getText().trim()); req.put("to", tfTkTo.getText().trim()); req.put("tien", tfTienChuyen.getText().trim()); req.put("mota","Chuyen khoan");
        Map<String,String> res = goi(req);
        if("true".equals(res.get("ok"))){
            JOptionPane.showMessageDialog(this, "Chuyển thành công");
            capNhatDanhSachTkNeuCo();
        } else JOptionPane.showMessageDialog(this, res.get("err"));
    }
    private void xemSaoKe(){
        if(!isPositiveNumber(tfTkSaoKe.getText())){ JOptionPane.showMessageDialog(this, "Số TK xem sao kê không hợp lệ"); return; }
        DefaultTableModel m = (DefaultTableModel) tbSaoKe.getModel(); m.setRowCount(0);
        Map<String,String> req = new LinkedHashMap<>(); req.put("cmd","saoke"); req.put("tk", tfTkSaoKe.getText()); Map<String,String> res = goi(req);
        if(!"true".equals(res.get("ok"))){ JOptionPane.showMessageDialog(this, res.get("err")); return; }
        String data = res.get("data"); if(data==null||data.isEmpty()) return;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for(String row: data.split(";")){
            if(row.trim().isEmpty()) continue; String[] p = row.split(",",5);
            long ts = Long.parseLong(p[3]);
            m.addRow(new Object[]{p[0], p[1], TienIch.dinhDangVND(Long.parseLong(p[2])), sdf.format(new java.util.Date(ts)), p.length>=5?p[4]:""});
        }
    }

    private void xuatCsv(JTable table, String defaultName){
        try{
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new java.io.File(defaultName));
            if(fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
            java.io.File f = fc.getSelectedFile();
            StringBuilder sb = new StringBuilder();
            // Header
            for(int c=0;c<table.getColumnCount();c++){
                if(c>0) sb.append(',');
                sb.append(escapeCsv(String.valueOf(table.getColumnName(c))));
            }
            sb.append('\n');
            // Rows
            for(int r=0;r<table.getRowCount();r++){
                for(int c=0;c<table.getColumnCount();c++){
                    if(c>0) sb.append(',');
                    Object v = table.getValueAt(r,c);
                    sb.append(escapeCsv(v==null?"":String.valueOf(v)));
                }
                sb.append('\n');
            }
            Files.write(f.toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));
            JOptionPane.showMessageDialog(this, "Đã xuất: "+f.getAbsolutePath());
        }catch(Exception ex){ JOptionPane.showMessageDialog(this, "Xuất CSV lỗi: "+ex.getMessage()); }
    }

    private static String escapeCsv(String s){
        boolean needQuote = s.contains(",") || s.contains("\n") || s.contains("\r") || s.contains("\"");
        String t = s.replace("\"", "\"\"");
        return needQuote ? '"'+t+'"' : t;
    }

    private void moSaoKeTheoDongTk(){
        int row = tbTk.getSelectedRow();
        if(row<0){ JOptionPane.showMessageDialog(this, "Chọn một tài khoản để xem sao kê"); return; }
        String id = String.valueOf(tbTk.getValueAt(row,0));
        tfTkSaoKe.setText(id);
        tabs.setSelectedIndex(3); // chuyển sang tab Sao kê
        xemSaoKe();
    }

    // Helpers
    private static String val(JTable t, int row, int col){ Object v = t.getValueAt(row,col); return v==null?"":String.valueOf(v); }
    private static boolean isPositiveNumber(String s){ if(s==null) return false; try{ return Long.parseLong(s.trim())>0; }catch(Exception e){ return false; } }
    private static boolean isNonNegativeNumber(String s){ if(s==null) return false; try{ return Long.parseLong(s.trim())>=0; }catch(Exception e){ return false; } }

    private void capNhatNhanNutKhoa(){
        int row = tbTk.getSelectedRow();
        if(row>=0){
            String st = String.valueOf(tbTk.getValueAt(row,3));
            btnKhoaMo.setText("HOAT_DONG".equals(st)?"Khóa TK":"Mở khóa TK");
        } else {
            btnKhoaMo.setText("Khóa/Mở khóa TK");
        }
    }

    private void goiYSoTk(){
        Map<String,String> req = new LinkedHashMap<>(); req.put("cmd","tk_goiy");
        Map<String,String> res = goi(req);
        if("true".equals(res.get("ok"))){
            String v = res.get("val");
            tfSoTkMoi.setText(v); tfSoTkChon.setText(v);
        }else JOptionPane.showMessageDialog(this, res.get("err"));
    }

    private void capNhatDanhSachTkNeuCo(){
        if(tfKhId.getText()!=null && !tfKhId.getText().trim().isEmpty()){
            taiTkTheoKhach();
        }
    }

    public static void main(String[] args){ SwingUtilities.invokeLater(() -> new UngDungKhachHang().setVisible(true)); }
}
