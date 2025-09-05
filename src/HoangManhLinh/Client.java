package HoangManhLinh;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.FlowLayout;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Client extends JFrame {
	private final JTextField fieldA = new JTextField(8);
	private final JTextField fieldB = new JTextField(8);
	private final JTextField fieldResult = new JTextField(16);

	private final String serverHost;
	private final int serverPort;

	public Client(String host, int port) {
		super("Giao diện phép tính");
		this.serverHost = host;
		this.serverPort = port;

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));

		add(new JLabel("Số 1:"));
		add(fieldA);
		add(new JLabel("Số 2:"));
		add(fieldB);

		JButton btnAdd = new JButton("Cộng");
		JButton btnSub = new JButton("Trừ");
		JButton btnMul = new JButton("Nhân");
		JButton btnDiv = new JButton("Chia");

		btnAdd.addActionListener(e -> onOperationClicked("+"));
		btnSub.addActionListener(e -> onOperationClicked("-"));
		btnMul.addActionListener(e -> onOperationClicked("*"));
		btnDiv.addActionListener(e -> onOperationClicked("/"));

		add(btnAdd);
		add(btnSub);
		add(btnMul);
		add(btnDiv);

		add(new JLabel("Kết quả:"));
		fieldResult.setEditable(false);
		add(fieldResult);

		setSize(800, 150);
		setLocationRelativeTo(null);
	}

	private void onOperationClicked(String operator) {
		String a = fieldA.getText().trim();
		String b = fieldB.getText().trim();

		if (a.isEmpty() || b.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ 2 số.");
			return;
		}

		String request = a + " " + operator + " " + b;
		try {
			String response = sendOnce(request);
			if (response.startsWith("OK ")) {
				fieldResult.setText(response.substring(3).trim());
			} else if (response.startsWith("ERR ")) {
				fieldResult.setText("");
				JOptionPane.showMessageDialog(this, response.substring(4).trim(), "Lỗi", JOptionPane.ERROR_MESSAGE);
			} else {
				fieldResult.setText("");
				JOptionPane.showMessageDialog(this, "Phản hồi không hợp lệ từ server.");

			}
		} catch (IOException ex) {
			fieldResult.setText("");
			JOptionPane.showMessageDialog(this, "Không thể kết nối server: " + ex.getMessage(), "Lỗi",
					JOptionPane.ERROR_MESSAGE);

		}
	}

	private String sendOnce(String message) throws IOException {
		try (Socket socket = new Socket(serverHost, serverPort);
				PrintWriter out = new PrintWriter(
						new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
				BufferedReader in = new BufferedReader(
						new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
			out.println(message);
			return in.readLine();
		}
	}

	public static void main(String[] args) {
		String host = args.length > 0 ? args[0] : "127.0.0.1";
		int port = 5000;
		if (args.length > 1) {
			try {
				port = Integer.parseInt(args[1]);
			} catch (NumberFormatException ignored) {
			}
		}

		String finalHost = host;
		int finalPort = port;
		SwingUtilities.invokeLater(() -> new Client(finalHost, finalPort).setVisible(true));
	}
}