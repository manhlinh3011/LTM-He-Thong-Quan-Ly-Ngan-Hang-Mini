package HoangManhLinh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.StringTokenizer;

public class Server {

	public static void main(String[] args) {
		// Port
		int port = 5000;
		if (args.length > 0) {
			try {
				port = Integer.parseInt(args[0]);
			} catch (NumberFormatException ignored) {
			}
		}

		System.out.println("[Server] Bắt đầu từ cổng " + port + "...");
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			while (true) {
				Socket client = serverSocket.accept();
				Thread handler = new Thread(new ClientHandler(client));
				handler.setDaemon(true);
				handler.start();
			}
		} catch (IOException e) {
			System.err.println("[Server] Lỗi: " + e.getMessage());
		}
	}

	private static class ClientHandler implements Runnable {
		private final Socket clientSocket;

		ClientHandler(Socket socket) {
			this.clientSocket = socket;
		}

		@Override
		public void run() {
			String remote = clientSocket.getRemoteSocketAddress().toString();
			System.out.println("[Server] Connected: " + remote);
			try (BufferedReader in = new BufferedReader(
					new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
					PrintWriter out = new PrintWriter(
							new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true)) {
				String line = in.readLine();
				if (line == null) {
					return;
				}
				line = line.trim();
				try {
					double result = evaluateExpression(line);
					String formatted = formatNumber(result);
					out.println("OK " + formatted);
					System.out.println("[Server] " + remote + " -> " + line + " = " + formatted);
				} catch (IllegalArgumentException ex) {
					out.println("ERR " + ex.getMessage());
					System.out.println("[Server] " + remote + " -> " + line + " -> ERR " + ex.getMessage());
				}
			} catch (IOException e) {
				System.err.println("[Server] I/O error: " + e.getMessage());
			} finally {
				try {
					clientSocket.close();
				} catch (IOException ignored) {
				}
				System.out.println("[Server] Disconnected: " + remote);
			}
		}

		private static double evaluateExpression(String expression) {
			StringTokenizer tokenizer = new StringTokenizer(expression);
			if (tokenizer.countTokens() != 3) {
				throw new IllegalArgumentException("Yêu cầu không hợp lệ. Sử dụng: <a> <op> <b>");
			}

			String aToken = tokenizer.nextToken();
			String op = tokenizer.nextToken();
			String bToken = tokenizer.nextToken();

			double a;
			double b;
			try {
				a = Double.parseDouble(aToken);
				b = Double.parseDouble(bToken);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Phải là số");
			}
			// 4 toán tử
			switch (op) {
			case "+":
				return a + b;
			case "-":
				return a - b;
			case "*":
				return a * b;
			case "/":
				if (b == 0.0) {
					throw new IllegalArgumentException("Chia cho số 0");
				}
				return a / b;
			default:
				throw new IllegalArgumentException("Toán tử không được hỗ trợ: " + op);
			}
		}

		// Định dạng số
		private static String formatNumber(double value) {
			DecimalFormat df = new DecimalFormat("0.##########");
			return df.format(value);
		}
	}
}