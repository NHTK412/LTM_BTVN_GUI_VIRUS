import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.Scanner;

public class Server {
    private static final int PORT = 1234;
    private static final int NUMBER_OF_PARTS = 4;
    private static final String FILE_PATH = "D:\\Programming_Language\\Java\\BTVN_LTM\\ChiaNhoVirusGuiTCP\\Untitled1.exe";
    
    public static void main(String[] args) {
        new Server().startServer();
    }
    
    public void startServer() {
        // Sử dụng try-with-resources để tự động đóng tài nguyên
        try (ServerSocket serverSocket = new ServerSocket(PORT);
             Scanner scanner = new Scanner(System.in)) {
            
            System.out.println("Server đang chờ kết nối trên cổng " + PORT + "...");
            
            // Chấp nhận kết nối từ client
            try (Socket socket = serverSocket.accept();
                 DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {
                
                System.out.println("Đã kết nối với client!");
                
                sendFileInChunks(out, scanner);
                
                System.out.println("Gửi file hoàn tất và đóng kết nối.");
            }
            
        } catch (IOException e) {
            System.err.println("Lỗi server: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void sendFileInChunks(DataOutputStream out, Scanner scanner) throws IOException {
        Path filePath = Paths.get(FILE_PATH);
        
        // Kiểm tra file tồn tại
        if (!Files.exists(filePath)) {
            System.err.println("File không tồn tại: " + FILE_PATH);
            return;
        }
        
        long fileSize = Files.size(filePath);
        System.out.println("Dung lượng file: " + fileSize + " bytes");
        
        // Tính kích thước mỗi phần
        int chunkSize = (int) Math.ceil(fileSize / (double) NUMBER_OF_PARTS);
        
        try (FileInputStream fis = new FileInputStream(filePath.toFile())) {
            byte[] buffer = new byte[chunkSize];
            int bytesRead;
            int partNumber = 1;
            
            // Đọc từng phần và gửi đi
            while ((bytesRead = fis.read(buffer)) != -1) {
                // Người dùng nhập tin nhắn từ bàn phím
                System.out.printf("Nhập tin nhắn gửi kèm với phần %d/%d: ", 
                                partNumber++, NUMBER_OF_PARTS);
                String message = scanner.nextLine();
                
                // Gửi tin nhắn
                out.writeUTF("MSG:" + message);
                
                // Gửi độ dài dữ liệu
                out.writeInt(bytesRead);
                
                // Gửi nội dung file
                out.write(buffer, 0, bytesRead);
                out.flush(); // Đảm bảo dữ liệu được gửi ngay lập tức
                
                System.out.println("Đã gửi phần " + (partNumber - 1) + " (" + bytesRead + " bytes)");
            }
            
            // Gửi thông điệp kết thúc
            out.writeUTF("MSG:DONE");
            out.flush();
            
        } catch (IOException e) {
            System.err.println("Lỗi khi đọc/gửi file: " + e.getMessage());
            throw e;
        }
    }
}
