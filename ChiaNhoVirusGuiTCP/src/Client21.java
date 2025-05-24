import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.Scanner;

public class Client {
    private static final String OUTPUT_FILE = "received.exe";
    
    public static void main(String[] args) {
        new Client().startClient();
    }
    
    public void startClient() {
        try (Scanner scanner = new Scanner(System.in)) {
            // Nhập thông tin kết nối
            ConnectionInfo connectionInfo = getConnectionInfo(scanner);
            
            // Kết nối và nhận file
            connectAndReceiveFile(connectionInfo);
            
        } catch (Exception e) {
            System.err.println("Lỗi client: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private ConnectionInfo getConnectionInfo(Scanner scanner) {
        System.out.print("Nhập địa chỉ IP server (mặc định localhost): ");
        String host = scanner.nextLine().trim();
        if (host.isEmpty()) {
            host = "localhost";
        }
        
        System.out.print("Nhập cổng server (mặc định 1234): ");
        String portInput = scanner.nextLine().trim();
        int port = portInput.isEmpty() ? 1234 : Integer.parseInt(portInput);
        
        return new ConnectionInfo(host, port);
    }
    
    private void connectAndReceiveFile(ConnectionInfo connectionInfo) {
        try (Socket socket = new Socket(connectionInfo.host(), connectionInfo.port());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {
            
            System.out.println("Đã kết nối tới server " + connectionInfo.host() + ":" + connectionInfo.port());
            
            receiveFileData(in);
            
            System.out.println("Nhận file hoàn tất!");
            
            // Hỏi người dùng có muốn chạy file không
            promptToRunFile();
            
        } catch (IOException e) {
            System.err.println("Lỗi kết nối: " + e.getMessage());
        }
    }
    
    private void receiveFileData(DataInputStream in) throws IOException {
        Path outputPath = Paths.get(OUTPUT_FILE);
        
        try (FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {
            int partNumber = 1;
            long totalBytesReceived = 0;
            
            while (true) {
                // Đọc tin nhắn
                String message = in.readUTF();
                
                // Nếu đã kết thúc
                if ("MSG:DONE".equals(message)) {
                    System.out.println("Đã nhận xong toàn bộ file.");
                    break;
                }
                
                // Hiển thị tin nhắn (bỏ prefix "MSG:")
                if (message.startsWith("MSG:")) {
                    String actualMessage = message.substring(4);
                    System.out.println("Phần " + partNumber + " - Tin nhắn: " + actualMessage);
                }
                
                // Nhận dữ liệu và ghi file
                int length = in.readInt();
                if (length > 0) {
                    byte[] buffer = new byte[length];
                    in.readFully(buffer);
                    fos.write(buffer);
                    totalBytesReceived += length;
                    
                    System.out.println("Đã nhận phần " + partNumber + " (" + length + " bytes)");
                    partNumber++;
                }
            }
            
            System.out.println("Tổng dung lượng nhận được: " + totalBytesReceived + " bytes");
            
        } catch (IOException e) {
            System.err.println("Lỗi khi ghi file: " + e.getMessage());
            throw e;
        }
    }
    
    private void promptToRunFile() {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Bạn có muốn chạy file đã nhận không? (y/N): ");
            String response = scanner.nextLine().trim().toLowerCase();
            
            if ("y".equals(response) || "yes".equals(response)) {
                runReceivedFile();
            } else {
                System.out.println("File đã được lưu tại: " + OUTPUT_FILE);
            }
        }
    }
    
    private void runReceivedFile() {
        Path filePath = Paths.get(OUTPUT_FILE);
        
        if (!Files.exists(filePath)) {
            System.err.println("File không tồn tại: " + OUTPUT_FILE);
            return;
        }
        
        try {
            System.out.println("Đang chạy file: " + OUTPUT_FILE);
            
            ProcessBuilder processBuilder = new ProcessBuilder();
            
            // Xác định OS và thiết lập lệnh phù hợp
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                processBuilder.command(OUTPUT_FILE);
            } else {
                // Cho Linux/Mac, cần chmod +x trước khi chạy
                processBuilder.command("chmod", "+x", OUTPUT_FILE);
                Process chmodProcess = processBuilder.start();
                chmodProcess.waitFor();
                
                processBuilder.command("./" + OUTPUT_FILE);
            }
            
            processBuilder.inheritIO();
            Process process = processBuilder.start();
            
            System.out.println("File đã được khởi chạy với PID: " + process.pid());
            
        } catch (IOException | InterruptedException e) {
            System.err.println("Lỗi khi chạy file: " + e.getMessage());
        }
    }
    
    // Record để lưu thông tin kết nối (Java 14+)
    private record ConnectionInfo(String host, int port) {}
}
