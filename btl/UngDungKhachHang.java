package btl;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.DefaultTableCellRenderer;
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

    // Màu chủ đạo
    private static final Color PRIMARY = new Color(33,150,243);
    private static final Color PRIMARY_DARK = new Color(25,118,210);
    private static final Color BACKGROUND = new Color(0xF3,0xF7,0xFD);
    private static final Color STRIPE = new Color(248,250,252);
    private static final Color BTN_TEXT = Color.WHITE;

    private JTextField tfUser = new JTextField("admin");
    private JPasswordField tfPass = new JPasswordField("admin123");
    private CardLayout card = new CardLayout();
    private JPanel root = new JPanel(card);

    // Tabs
    private JTabbedPane tabs = new JTabbedPane();
    private JTable tbKhach = new JTable(new DefaultTableModel(new Object[]{"ID","Họ tên","Điện thoại","Ngày sinh","Địa chỉ","Số giấy tờ"},0));
    private JTextField tfTen = new JTextField();
    private JTextField tfDob = new JTextField("01-01-2000");
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

    // Giao dịch: chọn khách và tài khoản để tránh nhầm lẫn
    private JComboBox<Object> cbKhGd = new JComboBox<>();
    private JComboBox<Object> cbKhNap = new JComboBox<>();
    private JComboBox<Object> cbKhRut = new JComboBox<>();
    private JComboBox<Object> cbKhFrom = new JComboBox<>();
    private JComboBox<Object> cbKhTo = new JComboBox<>();
    private JComboBox<Object> cbTkNapSel = new JComboBox<>();
    private JComboBox<Object> cbTkRutSel = new JComboBox<>();
    private JComboBox<Object> cbTkFromSel = new JComboBox<>();
    private JComboBox<Object> cbTkToSel = new JComboBox<>();

    private static class ComboItem {
        final long id; final String label; final Long balance; // balance optional (null for KH)
        ComboItem(long id, String label){ this(id, label, null); }
        ComboItem(long id, String label, Long balance){ this.id = id; this.label = label; this.balance = balance; }
        @Override public String toString(){ return label; }
    }

    // Nút thao tác tải dữ liệu cho phần giao dịch
    private JButton btnTaiKhGd = new JButton("Tải DS Khách");
    private JButton btnTaiTkTheoKhGd = new JButton("Tải TK theo KH (Nạp/Rút)");

    // Nhãn số dư hiển thị theo lựa chọn
    private JLabel lbNapBal = new JLabel("Số dư: -");
    private JLabel lbRutBal = new JLabel("Số dư: -");
    private JLabel lbFromBal = new JLabel("Số dư: -");
    private JLabel lbToBal = new JLabel("Số dư: -");

    // Gợi ý tìm kiếm khách (autocomplete) cho khu Tài khoản
    private java.util.List<ComboItem> khCache = new java.util.ArrayList<>();
    private JComboBox<Object> cbKhSuggest = new JComboBox<>();
    private JButton btnChonKhGoiY = new JButton("Chọn");
    private JTextField tfSearchPhone = new JTextField();
    private JTextField tfSearchIdNum = new JTextField();
    private JButton btnTimKh = new JButton("Tìm");

    public UngDungKhachHang(){
        super("Mini Bank Client (TCP)");
        caiDatGiaoDien();
        setSize(1000, 650); setLocationRelativeTo(null); setDefaultCloseOperation(EXIT_ON_CLOSE);
        ketNoiServer();
        add(root);
        getContentPane().setBackground(BACKGROUND);
        root.setBackground(BACKGROUND);
        taoManHinhDangNhap();
        taoManHinhChinh();
        apDungMauNut(root);
        card.show(root, "login");
    }
    //TCP
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
        JPanel bg = new GradientPanel();
        bg.setLayout(new GridBagLayout());
        root.add(bg, "login");

        JPanel cardPanel = new JPanel(new GridBagLayout());
        cardPanel.setOpaque(true);
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(225,230,240)),
            BorderFactory.createEmptyBorder(24,32,24,32)
        ));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10,10,10,10);
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("User Login");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridx=0; c.gridy=0; c.gridwidth=2; c.weightx=1; cardPanel.add(title, c);

        // Fields
        tfUser.setColumns(20);
        tfUser.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220,224,236)),
            BorderFactory.createEmptyBorder(10,12,10,12)));
        tfUser.setBackground(new Color(245,247,250));
        c.gridy=1; c.gridwidth=2; cardPanel.add(tfUser, c);

        tfPass.setColumns(20);
        tfPass.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220,224,236)),
            BorderFactory.createEmptyBorder(10,12,10,12)));
        tfPass.setBackground(new Color(245,247,250));
        c.gridy=2; cardPanel.add(tfPass, c);

        RoundButton btnLogin = new RoundButton("Login");
        btnLogin.setPreferredSize(new Dimension(220,36));
        btnLogin.addActionListener(e -> dangNhap());
        c.gridy=3; c.gridwidth=2; cardPanel.add(btnLogin, c);

        // Place the card in center of gradient
        GridBagConstraints wrap = new GridBagConstraints();
        wrap.gridx=0; wrap.gridy=0; bg.add(cardPanel, wrap);
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
        JPanel pnl = new SoftGradientPanel(); pnl.setLayout(new BorderLayout()); pnl.add(tabs);
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
        tbAdd.setTitleColor(PRIMARY);
        form.setBorder(tbAdd);
        int r = 0;
        gf.gridy = r; gf.gridx = 0; form.add(new JLabel("Họ tên"), gf); gf.gridx = 1; gf.weightx = 1; form.add(tfTen, gf);
        r++; gf.gridy = r; gf.gridx = 0; gf.weightx = 0; form.add(new JLabel("Ngày sinh(dd-MM-yyyy)"), gf); gf.gridx = 1; gf.weightx = 1; form.add(tfDob, gf);
        r++; gf.gridy = r; gf.gridx = 0; gf.weightx = 0; form.add(new JLabel("Địa chỉ"), gf); gf.gridx = 1; gf.weightx = 1; form.add(tfDc, gf);
        r++; gf.gridy = r; gf.gridx = 0; gf.weightx = 0; form.add(new JLabel("Điện thoại"), gf); gf.gridx = 1; gf.weightx = 1; form.add(tfDt, gf);
        r++; gf.gridy = r; gf.gridx = 0; gf.weightx = 0; form.add(new JLabel("Số giấy tờ"), gf); gf.gridx = 1; gf.weightx = 1; form.add(tfGt, gf);
        r++; gf.gridy = r; gf.gridx = 0; gf.gridwidth = 1; JButton btnThem = new JButton("Thêm"); form.add(btnThem, gf); gf.gridx = 1; JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); JButton btnSua = new JButton("Sửa KH đã chọn"); JButton btnXoa = new JButton("Xóa KH đã chọn"); actions.add(btnSua); actions.add(btnXoa); form.add(actions, gf);
        btnThem.addActionListener(e -> themKhach());
        btnSua.addActionListener(e -> suaKhach());
        btnXoa.addActionListener(e -> xoaKhach());
        a.add(form, BorderLayout.SOUTH);
        tabs.addTab("Khách hàng", a);
        lamDepBang(tbKhach);
        // Khi chọn khách ở bảng, tự điền ID vào ô tìm tài khoản
        tbKhach.getSelectionModel().addListSelectionListener(e -> {
            if(e.getValueIsAdjusting()) return;
            int row = tbKhach.getSelectedRow();
            if(row >= 0){
                Object v = tbKhach.getValueAt(row, 0);
                if(v!=null) tfKhId.setText(String.valueOf(v));
                // Đổ danh sách TK theo khách sang các combobox giao dịch
                try{
                    long kh = Long.parseLong(String.valueOf(v));
                    napDanhSachTkChoGiaoDich(kh);
                }catch(Exception ignored){}
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

        // Khôi phục chọn từ tab Khách hàng
        btnChonKh.addActionListener(e -> {
            int row = tbKhach.getSelectedRow();
            if(row<0){ JOptionPane.showMessageDialog(this, "Hãy chọn một khách ở tab Khách hàng"); return; }
            Object v = tbKhach.getValueAt(row,0);
            if(v!=null){ tfKhId.setText(String.valueOf(v)); taiTkTheoKhach(); }
        });
        btnTai.addActionListener(e -> taiTkTheoKhach());
        // Nút tải khách cho giao dịch & tải TK theo khách giao dịch
        btnTaiKhGd.addActionListener(e -> napDanhSachKhachChoGiaoDich());
        // Mỗi selector KH điều khiển riêng combobox TK của phần đó
        cbKhNap.addActionListener(e -> { Object it=cbKhNap.getSelectedItem(); if(it instanceof ComboItem) napDanhSachTkCho(cbTkNapSel, ((ComboItem)it).id); });
        cbKhRut.addActionListener(e -> { Object it=cbKhRut.getSelectedItem(); if(it instanceof ComboItem) napDanhSachTkCho(cbTkRutSel, ((ComboItem)it).id); });
        cbKhFrom.addActionListener(e -> { Object it=cbKhFrom.getSelectedItem(); if(it instanceof ComboItem) napDanhSachTkCho(cbTkFromSel, ((ComboItem)it).id); });
        cbKhTo.addActionListener(e -> { Object it=cbKhTo.getSelectedItem(); if(it instanceof ComboItem) napDanhSachTkCho(cbTkToSel, ((ComboItem)it).id); });
        // Hiển thị số dư khi chọn TK
        cbTkNapSel.addActionListener(e -> capNhatSoDuLabel(cbTkNapSel, lbNapBal));
        cbTkRutSel.addActionListener(e -> capNhatSoDuLabel(cbTkRutSel, lbRutBal));
        cbTkFromSel.addActionListener(e -> { capNhatSoDuLabel(cbTkFromSel, lbFromBal); capNhatDanhSachToTheoFrom(); });
        cbTkToSel.addActionListener(e -> capNhatSoDuLabel(cbTkToSel, lbToBal));
        btnTai.addActionListener(e -> taiTkTheoKhach());
        btnGoiY.addActionListener(e -> goiYSoTk());
        btnTaoTk.addActionListener(e -> taoTk());
        // Thanh tìm kiếm TK (khôi phục đơn giản)
        JPanel searchTk = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField tfSearchTk = new JTextField(); tfSearchTk.setColumns(24);
        searchTk.add(new JLabel("Tìm TK/Loại/Trạng thái:")); searchTk.add(tfSearchTk);
        JButton btnExportTk = new JButton("Xuất CSV"); searchTk.add(btnExportTk);
        // (bỏ gợi ý KH ở khu này để giống như cũ)

        // Bật sắp xếp + sorter cho bảng TK
        TableRowSorter<DefaultTableModel> sorterTk = new TableRowSorter<>((DefaultTableModel) tbTk.getModel());
        tbTk.setRowSorter(sorterTk);
        // Form sửa tài khoản: căn đều tiêu đề và ô nhập theo cột
        JPanel formTk = new JPanel(new GridBagLayout());
        TitledBorder tbEdit = BorderFactory.createTitledBorder("Sửa tài khoản");
        tbEdit.setTitleJustification(TitledBorder.CENTER);
        tbEdit.setTitlePosition(TitledBorder.TOP);
        tbEdit.setTitleColor(PRIMARY);
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
        lamDepBang(tbTk);
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
        JPanel c = new JPanel(new GridLayout(4,1));
        // Hàng chọn khách độc lập cho từng chức năng
        JPanel choose = new JPanel(new GridBagLayout());
        choose.setBorder(BorderFactory.createTitledBorder("Chọn khách cho giao dịch"));
        GridBagConstraints cg = new GridBagConstraints(); cg.insets=new Insets(6,6,6,6); cg.fill=GridBagConstraints.HORIZONTAL;
        // Nạp
        cg.gridx=0; cg.gridy=0; choose.add(new JLabel("KH Nạp:"), cg);
        cg.gridx=1; cg.weightx=1; choose.add(cbKhNap, cg);
        // Rút
        cg.gridx=2; cg.weightx=0; choose.add(new JLabel("KH Rút:"), cg);
        cg.gridx=3; cg.weightx=1; choose.add(cbKhRut, cg);
        // Chuyển
        cg.gridx=0; cg.gridy=1; cg.weightx=0; choose.add(new JLabel("KH From:"), cg);
        cg.gridx=1; cg.weightx=1; choose.add(cbKhFrom, cg);
        cg.gridx=2; cg.weightx=0; choose.add(new JLabel("KH To:"), cg);
        cg.gridx=3; cg.weightx=1; choose.add(cbKhTo, cg);
        // Nút tải 1 lần
        cg.gridx=0; cg.gridy=2; cg.gridwidth=4; choose.add(btnTaiKhGd, cg);
        c.add(choose);

        JPanel nap = new JPanel(new FlowLayout(FlowLayout.LEFT));
        TitledBorder tbNap = BorderFactory.createTitledBorder("Nạp tiền");
        tbNap.setTitleJustification(TitledBorder.CENTER);
        tbNap.setTitlePosition(TitledBorder.TOP);
        tbNap.setTitleColor(PRIMARY);
        nap.setBorder(tbNap);
        tfTkNap.setColumns(12); tfTienNap.setColumns(10);
        nap.add(new JLabel("KH:")); nap.add(cbKhNap);
        nap.add(new JLabel("Tài khoản:")); nap.add(cbTkNapSel); nap.add(lbNapBal); nap.add(new JLabel("Số tiền:")); nap.add(tfTienNap); JButton btnNap = new JButton("Nạp"); nap.add(btnNap);
        btnNap.addActionListener(e -> napTien());
        JPanel rut = new JPanel(new FlowLayout(FlowLayout.LEFT));
        TitledBorder tbRut = BorderFactory.createTitledBorder("Rút tiền");
        tbRut.setTitleJustification(TitledBorder.CENTER);
        tbRut.setTitlePosition(TitledBorder.TOP);
        tbRut.setTitleColor(PRIMARY);
        rut.setBorder(tbRut);
        tfTkRut.setColumns(12); tfTienRut.setColumns(10);
        rut.add(new JLabel("KH:")); rut.add(cbKhRut);
        rut.add(new JLabel("Tài khoản:")); rut.add(cbTkRutSel); rut.add(lbRutBal); rut.add(new JLabel("Số tiền:")); rut.add(tfTienRut); JButton btnRut = new JButton("Rút"); rut.add(btnRut);
        btnRut.addActionListener(e -> rutTien());
        JPanel chuyen = new JPanel(new FlowLayout(FlowLayout.LEFT));
        TitledBorder tbChuyen = BorderFactory.createTitledBorder("Chuyển khoản");
        tbChuyen.setTitleJustification(TitledBorder.CENTER);
        tbChuyen.setTitlePosition(TitledBorder.TOP);
        tbChuyen.setTitleColor(PRIMARY);
        chuyen.setBorder(tbChuyen);
        tfTkFrom.setColumns(12); tfTkTo.setColumns(12); tfTienChuyen.setColumns(10);
        chuyen.add(new JLabel("KH From:")); chuyen.add(cbKhFrom);
        chuyen.add(new JLabel("Từ TK:")); chuyen.add(cbTkFromSel); chuyen.add(lbFromBal);
        chuyen.add(new JLabel("KH To:")); chuyen.add(cbKhTo);
        chuyen.add(new JLabel("Đến TK:")); chuyen.add(cbTkToSel); chuyen.add(lbToBal);
        chuyen.add(new JLabel("Số tiền:")); chuyen.add(tfTienChuyen); JButton btnChuyen = new JButton("Chuyển"); chuyen.add(btnChuyen);
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
        phongToTabs();
        lamDepBang(tbSaoKe);

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
            String dob = p.length>3?toDisplayDate(p[3]):"";
            m.addRow(new Object[]{p[0], p[1], p[2], dob, p.length>4?p[4]:"", p.length>5?p[5]:""});
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
        req.put("dob", toServerDate(tfDob.getText()));
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
                dob = toDisplayDate(resKh.getOrDefault("dob","")); dc = resKh.getOrDefault("dc",""); gt = resKh.getOrDefault("gt","");
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

    // Chuyển đổi ngày giữa server (yyyy-MM-dd) và hiển thị (dd-MM-yyyy)
    private static String toDisplayDate(String server){
        try{
            if(server==null || server.trim().isEmpty()) return "";
            java.time.LocalDate d = java.time.LocalDate.parse(server);
            return d.format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        }catch(Exception e){ return server; }
    }
    private static String toServerDate(String display){
        try{
            if(display==null || display.trim().isEmpty()) return "";
            java.time.LocalDate d = java.time.LocalDate.parse(display, java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            return d.toString();
        }catch(Exception e){ return display; }
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
        if(!(cbTkNapSel.getSelectedItem() instanceof ComboItem)){ JOptionPane.showMessageDialog(this, "Chọn tài khoản để nạp"); return; }
        tfTkNap.setText(String.valueOf(((ComboItem)cbTkNapSel.getSelectedItem()).id));
        if(!isPositiveNumber(tfTienNap.getText())){ JOptionPane.showMessageDialog(this, "Số tiền nạp phải là số dương"); return; }
        Map<String,String> req = new LinkedHashMap<>(); req.put("cmd","nap"); req.put("tk", tfTkNap.getText().trim()); req.put("tien", tfTienNap.getText().trim()); req.put("mota","Nap tien");
        Map<String,String> res = goi(req);
        if("true".equals(res.get("ok"))){
            JOptionPane.showMessageDialog(this, "Nạp thành công");
            capNhatSauGiaoDich(cbTkNapSel, lbNapBal);
        } else JOptionPane.showMessageDialog(this, res.get("err"));
    }
    private void rutTien(){
        if(!(cbTkRutSel.getSelectedItem() instanceof ComboItem)){ JOptionPane.showMessageDialog(this, "Chọn tài khoản để rút"); return; }
        tfTkRut.setText(String.valueOf(((ComboItem)cbTkRutSel.getSelectedItem()).id));
        if(!isPositiveNumber(tfTienRut.getText())){ JOptionPane.showMessageDialog(this, "Số tiền rút phải là số dương"); return; }
        Map<String,String> req = new LinkedHashMap<>(); req.put("cmd","rut"); req.put("tk", tfTkRut.getText().trim()); req.put("tien", tfTienRut.getText().trim()); req.put("mota","Rut tien");
        Map<String,String> res = goi(req);
        if("true".equals(res.get("ok"))){
            JOptionPane.showMessageDialog(this, "Rút thành công");
            capNhatSauGiaoDich(cbTkRutSel, lbRutBal);
        } else JOptionPane.showMessageDialog(this, res.get("err"));
    }
    private void chuyenKhoan(){
        if(!(cbTkFromSel.getSelectedItem() instanceof ComboItem) || !(cbTkToSel.getSelectedItem() instanceof ComboItem)){
            JOptionPane.showMessageDialog(this, "Chọn tài khoản nguồn và đích"); return;
        }
        tfTkFrom.setText(String.valueOf(((ComboItem)cbTkFromSel.getSelectedItem()).id));
        tfTkTo.setText(String.valueOf(((ComboItem)cbTkToSel.getSelectedItem()).id));
        if(!isPositiveNumber(tfTkFrom.getText()) || !isPositiveNumber(tfTkTo.getText())){ JOptionPane.showMessageDialog(this, "Số TK không hợp lệ"); return; }
        if(tfTkFrom.getText().trim().equals(tfTkTo.getText().trim())){ JOptionPane.showMessageDialog(this, "Không thể chuyển giữa cùng một tài khoản"); return; }
        if(!isPositiveNumber(tfTienChuyen.getText())){ JOptionPane.showMessageDialog(this, "Số tiền chuyển phải là số dương"); return; }
        Map<String,String> req = new LinkedHashMap<>(); req.put("cmd","chuyen"); req.put("from", tfTkFrom.getText().trim()); req.put("to", tfTkTo.getText().trim()); req.put("tien", tfTienChuyen.getText().trim()); req.put("mota","Chuyen khoan");
        Map<String,String> res = goi(req);
        if("true".equals(res.get("ok"))){
            JOptionPane.showMessageDialog(this, "Chuyển thành công");
            capNhatSauGiaoDich(cbTkFromSel, lbFromBal);
            capNhatSauGiaoDich(cbTkToSel, lbToBal);
        } else JOptionPane.showMessageDialog(this, res.get("err"));
    }
    private void xemSaoKe(){
        if(!isPositiveNumber(tfTkSaoKe.getText())){ JOptionPane.showMessageDialog(this, "Số TK xem sao kê không hợp lệ"); return; }
        DefaultTableModel m = (DefaultTableModel) tbSaoKe.getModel(); m.setRowCount(0);
        Map<String,String> req = new LinkedHashMap<>(); req.put("cmd","saoke"); req.put("tk", tfTkSaoKe.getText()); Map<String,String> res = goi(req);
        if(!"true".equals(res.get("ok"))){ JOptionPane.showMessageDialog(this, res.get("err")); return; }
        String data = res.get("data"); if(data==null||data.isEmpty()) return;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        for(String row: data.split(";")){
            if(row.trim().isEmpty()) continue; String[] p = row.split(",",5);
            long ts = Long.parseLong(p[3]);
            m.addRow(new Object[]{p[0], p[1], TienIch.dinhDangVND(Long.parseLong(p[2])), sdf.format(new java.util.Date(ts)), p.length>=5?p[4]:""});
        }
    }

    private void napDanhSachKhachChoGiaoDich(){
        try{
            // Xóa toàn bộ combobox
            cbKhNap.removeAllItems(); cbKhRut.removeAllItems(); cbKhFrom.removeAllItems(); cbKhTo.removeAllItems();
            cbTkNapSel.removeAllItems(); cbTkRutSel.removeAllItems(); cbTkFromSel.removeAllItems(); cbTkToSel.removeAllItems();

            // Tải tất cả khách hàng
            Map<String,String> req = new LinkedHashMap<>(); req.put("cmd","kh_ds"); Map<String,String> res = goi(req);
            if(!"true".equals(res.get("ok"))){ JOptionPane.showMessageDialog(this, res.get("err")); return; }
            String data = res.get("data"); if(data==null||data.isEmpty()) return;

            // Lưu tạm id->name để dán nhãn tài khoản
            java.util.List<ComboItem> khList = new java.util.ArrayList<>(); khCache.clear();
            for(String row: data.split(";")){
                if(row.trim().isEmpty()) continue; String[] p = row.split(",");
                long id = Long.parseLong(p[0]); String name = (p.length>1?p[1]:"");
                String khLabel = p[0]+" - "+name;
                ComboItem item = new ComboItem(id, khLabel);
                cbKhNap.addItem(item); cbKhRut.addItem(item); cbKhFrom.addItem(item); cbKhTo.addItem(item);
                khList.add(new ComboItem(id, name)); khCache.add(new ComboItem(id, name));
            }

            // Với mỗi KH, tải tất cả tài khoản và đưa vào combobox, nhãn gồm số TK + loại + trạng thái + tên KH
            for(ComboItem kh : khList){
                Map<String,String> reqTk = new LinkedHashMap<>(); reqTk.put("cmd","tk_theokh"); reqTk.put("kh", String.valueOf(kh.id));
                Map<String,String> resTk = goi(reqTk);
                if(!"true".equals(resTk.get("ok"))) continue;
                String d = resTk.get("data"); if(d==null||d.isEmpty()) continue;
                for(String r: d.split(";")){
                    if(r.trim().isEmpty()) continue; String[] p = r.split(",");
                    long tkId = Long.parseLong(p[0]); String loai = p[1]; String trangThai = p[3];
                    String label = tkId+" - "+loai+" - "+trangThai+" - "+kh.label;
                    ComboItem tkItem = new ComboItem(tkId, label);
                    cbTkNapSel.addItem(tkItem); cbTkRutSel.addItem(tkItem); cbTkFromSel.addItem(tkItem); cbTkToSel.addItem(tkItem);
                }
            }
        }catch(Exception ex){ JOptionPane.showMessageDialog(this, "Tải DS khách/TK lỗi: "+ex.getMessage()); }
    }

    private void napDanhSachTkChoGiaoDich(long khId){
        try{
            cbTkNapSel.removeAllItems(); cbTkRutSel.removeAllItems(); cbTkFromSel.removeAllItems(); cbTkToSel.removeAllItems();
            Map<String,String> req = new LinkedHashMap<>(); req.put("cmd","tk_theokh"); req.put("kh", String.valueOf(khId)); Map<String,String> res = goi(req);
            if(!"true".equals(res.get("ok"))){ JOptionPane.showMessageDialog(this, res.get("err")); return; }
            String data = res.get("data"); if(data==null||data.isEmpty()) return;
            for(String row: data.split(";")){
                if(row.trim().isEmpty()) continue; String[] p = row.split(",");
                long id = Long.parseLong(p[0]); long balance = Long.parseLong(p[2]); String label = p[0]+" - "+p[1]+" - "+TienIch.dinhDangVND(balance)+" - "+p[3];
                ComboItem item = new ComboItem(id, label, balance);
                cbTkNapSel.addItem(item); cbTkRutSel.addItem(item); cbTkFromSel.addItem(item); cbTkToSel.addItem(item);
            }
        }catch(Exception ex){ JOptionPane.showMessageDialog(this, "Tải DS tài khoản lỗi: "+ex.getMessage()); }
    }

    private void napDanhSachTkCho(JComboBox<Object> cb, long khId){
        try{
            cb.removeAllItems();
            Map<String,String> req = new LinkedHashMap<>(); req.put("cmd","tk_theokh"); req.put("kh", String.valueOf(khId)); Map<String,String> res = goi(req);
            if(!"true".equals(res.get("ok"))){ JOptionPane.showMessageDialog(this, res.get("err")); return; }
            String data = res.get("data"); if(data==null||data.isEmpty()) return;
            for(String row: data.split(";")){
                if(row.trim().isEmpty()) continue; String[] p = row.split(",");
                long id = Long.parseLong(p[0]); long balance = Long.parseLong(p[2]); String label = p[0]+" - "+p[1]+" - "+TienIch.dinhDangVND(balance)+" - "+p[3];
                cb.addItem(new ComboItem(id, label, balance));
            }
        }catch(Exception ex){ JOptionPane.showMessageDialog(this, "Tải DS tài khoản lỗi: "+ex.getMessage()); }
    }

    // Khi người dùng chọn "Từ TK", nếu chọn một tài khoản cụ thể, phần "Đến TK" sẽ hiển thị tất cả tài khoản khác để dễ chọn chuyển
    private void capNhatDanhSachToTheoFrom(){
        Object it = cbTkFromSel.getSelectedItem();
        if(!(it instanceof ComboItem)) return;
        long fromId = ((ComboItem) it).id;
        // Nạp toàn bộ các tài khoản (từ DS khách đã load) rồi loại bỏ fromId
        try{
            cbTkToSel.removeAllItems();
            // Nếu đã chọn KH cho Nạp/Rút, ưu tiên load theo KH; nếu chưa, load tất cả KH
            if(cbKhGd.getItemCount()>0 && cbKhGd.getSelectedItem() instanceof ComboItem){
                ComboItem kh = (ComboItem) cbKhGd.getSelectedItem();
                napDanhSachTkCho(cbTkToSel, kh.id);
            } else {
                // Load tất cả KH và đưa toàn bộ TK vào "Đến TK"
                Map<String,String> req = new LinkedHashMap<>(); req.put("cmd","kh_ds"); Map<String,String> res = goi(req);
                if("true".equals(res.get("ok"))){
                    String data = res.get("data"); if(data!=null){
                        for(String row: data.split(";")){
                            if(row.trim().isEmpty()) continue; String[] p = row.split(",");
                            long khId = Long.parseLong(p[0]);
                            napDanhSachTkCho(cbTkToSel, khId);
                        }
                    }
                }
            }
            // Loại bỏ tài khoản đang chọn làm nguồn
            for(int i=cbTkToSel.getItemCount()-1;i>=0;i--){
                Object x = cbTkToSel.getItemAt(i);
                if(x instanceof ComboItem && ((ComboItem)x).id == fromId){ cbTkToSel.removeItemAt(i); }
            }
        }catch(Exception ignored){}
    }

    private void capNhatSoDuLabel(JComboBox<Object> cb, JLabel lb){
        Object it = cb.getSelectedItem();
        if(it instanceof ComboItem && ((ComboItem) it).balance != null){
            lb.setText("Số dư: "+TienIch.dinhDangVND(((ComboItem) it).balance));
        }else{
            lb.setText("Số dư: -");
        }
    }

    private void capNhatSauGiaoDich(JComboBox<Object> cbTk, JLabel lb){
        try{
            if(!(cbTk.getSelectedItem() instanceof ComboItem)) return;
            long tkId = ((ComboItem) cbTk.getSelectedItem()).id;
            // Gọi lại API tk_theokh theo KH chứa TK này để lấy số dư mới
            // Đầu tiên tìm KH id từ các combobox KH (nạp/rút/from/to) có chứa TK đang chọn
            long khId = -1;
            ComboItem[] khCbs = new ComboItem[]{ (ComboItem) cbKhNap.getSelectedItem(), (ComboItem) cbKhRut.getSelectedItem(), (ComboItem) cbKhFrom.getSelectedItem(), (ComboItem) cbKhTo.getSelectedItem() };
            for(ComboItem kh : khCbs){
                if(kh==null) continue;
                Map<String,String> req = new LinkedHashMap<>(); req.put("cmd","tk_theokh"); req.put("kh", String.valueOf(kh.id)); Map<String,String> res = goi(req);
                if(!"true".equals(res.get("ok"))) continue;
                String data = res.get("data"); if(data==null) continue;
                for(String row: data.split(";")){
                    if(row.trim().isEmpty()) continue; String[] p = row.split(",");
                    if(Long.parseLong(p[0])==tkId){ khId = kh.id; break; }
                }
                if(khId!=-1) break;
            }
            if(khId==-1) return;
            // Lấy lại danh sách TK theo KH đó và cập nhật item đang chọn (số dư mới)
            cbTk.removeAllItems();
            napDanhSachTkCho(cbTk, khId);
            // Đặt lại selection về tkId
            for(int i=0;i<cbTk.getItemCount();i++){
                Object it = cbTk.getItemAt(i);
                if(it instanceof ComboItem && ((ComboItem)it).id==tkId){ cbTk.setSelectedIndex(i); break; }
            }
            capNhatSoDuLabel(cbTk, lb);
        }catch(Exception ignored){}
    }

    // Gợi ý KH theo tên/điện thoại/giấy tờ → lọc cục bộ từ khCache, nếu rỗng thì gọi server
    private void capNhatGoiYKKhach(String q){
        try{
            String qq = q==null?"":q.trim().toLowerCase();
            cbKhSuggest.removeAllItems();
            if(qq.isEmpty()) return;
            java.util.List<ComboItem> src = new java.util.ArrayList<>(khCache);
            if(src.isEmpty()){
                Map<String,String> req = new LinkedHashMap<>(); req.put("cmd","kh_ds"); Map<String,String> res = goi(req);
                if("true".equals(res.get("ok"))){
                    String data = res.get("data"); if(data!=null){
                        for(String row: data.split(";")){
                            if(row.trim().isEmpty()) continue; String[] p = row.split(",");
                            long id = Long.parseLong(p[0]); String name = p.length>1?p[1]:"";
                            khCache.add(new ComboItem(id, name));
                        }
                        src = new java.util.ArrayList<>(khCache);
                    }
                }
            }
            for(ComboItem it : src){
                if(it.label!=null && it.label.toLowerCase().contains(qq)){
                    cbKhSuggest.addItem(new ComboItem(it.id, it.id+" - "+it.label));
                }
            }
        }catch(Exception ignored){}
    }

    private void chonKhTheoGoiY(){
        Object it = cbKhSuggest.getSelectedItem();
        if(!(it instanceof ComboItem)) return;
        long id = ((ComboItem) it).id;
        // Sau khi chọn, đổ vào các combobox KH để bạn có thể thao tác tiếp
        ComboItem chosen = new ComboItem(id, ((ComboItem) it).label);
        cbKhNap.setSelectedItem(chosen);
        cbKhRut.setSelectedItem(chosen);
        cbKhFrom.setSelectedItem(chosen);
    }

    private void locTheoThongTin(){
        try{
            String phone = tfSearchPhone.getText()==null?"":tfSearchPhone.getText().trim().toLowerCase();
            String idn = tfSearchIdNum.getText()==null?"":tfSearchIdNum.getText().trim().toLowerCase();
            if(khCache.isEmpty()) capNhatGoiYKKhach("*");
            cbKhSuggest.removeAllItems();
            for(ComboItem it : khCache){
                // Vì không có sẵn sđt/giấy tờ ở cache, hỏi server chi tiết từng KH
                boolean match = false;
                if(it.label!=null && !it.label.isEmpty()){
                    String nm = it.label.toLowerCase();
                    if(!phone.isEmpty() || !idn.isEmpty()){
                        Map<String,String> req = new LinkedHashMap<>(); req.put("cmd","kh_get"); req.put("id", String.valueOf(it.id)); Map<String,String> res = goi(req);
                        if("true".equals(res.get("ok"))){
                            String sdt = res.getOrDefault("dt", "").toLowerCase();
                            String gt = res.getOrDefault("gt", "").toLowerCase();
                            match = (phone.isEmpty() || sdt.contains(phone)) && (idn.isEmpty() || gt.contains(idn));
                        }
                    }
                    if(phone.isEmpty() && idn.isEmpty()){ match = true; }
                    if(match){ cbKhSuggest.addItem(new ComboItem(it.id, it.id+" - "+it.label)); }
                }
            }
        }catch(Exception ignored){}
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

    // Nền gradient cho màn hình đăng nhập
    private static class GradientPanel extends JPanel{
        @Override protected void paintComponent(Graphics g){
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            int w = getWidth(), h = getHeight();
            GradientPaint gp = new GradientPaint(0,0,new Color(63,81,181), w, h, new Color(233,30,99));
            g2.setPaint(gp);
            g2.fillRect(0,0,w,h);
            g2.dispose();
        }
    }

    // Nút bo tròn dùng cho Login
    private class RoundButton extends JButton{
        RoundButton(String text){ super(text); setForeground(Color.WHITE); setBackground(new Color(76,175,80)); setFocusPainted(false); setBorderPainted(false); }
        @Override protected void paintComponent(Graphics g){
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0,0,getWidth(),getHeight(),28,28);
            g2.dispose();
            super.paintComponent(g);
            setContentAreaFilled(false);
        }
    }

    // Nền gradient rất nhạt cho giao diện sau đăng nhập
    private static class SoftGradientPanel extends JPanel{
        @Override protected void paintComponent(Graphics g){
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            int w = getWidth(), h = getHeight();
            Color c1 = new Color(219,229,255); // nhạt hơn nhưng đậm hơn một chút xanh lam
            Color c2 = new Color(243,218,245); // nhạt hơn nhưng đậm hơn một chút hồng tím
            GradientPaint gp = new GradientPaint(0,0,c1, w, h, c2);
            g2.setPaint(gp);
            g2.fillRect(0,0,w,h);
            g2.dispose();
        }
    }

    private void caiDatGiaoDien(){
        try{
            for(UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()){
                if("Nimbus".equals(info.getName())){ UIManager.setLookAndFeel(info.getClassName()); break; }
            }
        }catch(Exception ignored){}
        // Màu chủ đạo
        UIManager.put("nimbusBase", PRIMARY_DARK);
        UIManager.put("nimbusBlueGrey", new Color(232, 240, 254));
        UIManager.put("control", BACKGROUND);
    }

    private void lamDepBang(JTable table){
        table.setRowHeight(26);
        table.setSelectionBackground(new Color(227,242,253));
        table.setSelectionForeground(Color.BLACK);
        JTableHeader header = table.getTableHeader();
        header.setBackground(PRIMARY);
        header.setForeground(Color.WHITE);
        header.setFont(header.getFont().deriveFont(Font.BOLD));
        // sọc dòng nhẹ
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int r,int c){
                Component comp = super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                if(!sel){ comp.setBackground((r%2==0)?Color.WHITE:STRIPE); }
                return comp;
            }
        });
    }

    private void apDungMauNut(Container parent){
        for(Component comp : parent.getComponents()){
            if(comp instanceof JButton){
                JButton b = (JButton) comp;
                b.setBackground(PRIMARY);
                b.setForeground(BTN_TEXT);
                b.setFocusPainted(false);
                b.setBorder(BorderFactory.createEmptyBorder(6,12,6,12));
            } else if (comp instanceof JPanel){
                ((JPanel)comp).setOpaque(true); ((JPanel)comp).setBackground(BACKGROUND);
            }
            if(comp instanceof Container){ apDungMauNut((Container) comp); }
        }
    }

    private void phongToTabs(){
        int count = tabs.getTabCount();
        for(int i=0;i<count;i++){
            String title = tabs.getTitleAt(i);
            JLabel lbl = new JLabel(" "+title+" ");
            lbl.setBorder(BorderFactory.createEmptyBorder(6,12,6,12));
            lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
            tabs.setTabComponentAt(i, lbl);
        }
        capNhatTabStyle();
        tabs.addChangeListener(e -> capNhatTabStyle());
    }

    private void capNhatTabStyle(){
        int sel = tabs.getSelectedIndex();
        int count = tabs.getTabCount();
        for(int i=0;i<count;i++){
            Component c = tabs.getTabComponentAt(i);
            if(!(c instanceof JLabel)) continue;
            JLabel lbl = (JLabel)c;
            if(i==sel){
                lbl.setOpaque(true);
                lbl.setBackground(PRIMARY);
                lbl.setForeground(BTN_TEXT);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0,0,2,0, PRIMARY_DARK),
                    BorderFactory.createEmptyBorder(6,12,6,12)
                ));
            }else{
                lbl.setOpaque(true);
                lbl.setBackground(new Color(236, 242, 249));
                lbl.setForeground(new Color(51,51,51));
                lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0,0,1,0, new Color(220,225,230)),
                    BorderFactory.createEmptyBorder(6,12,6,12)
                ));
            }
        }
    }

    public static void main(String[] args){ SwingUtilities.invokeLater(() -> new UngDungKhachHang().setVisible(true)); }
}
