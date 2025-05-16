import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Server {
    public static void main(String[] args) throws Exception {
        // Tao server socket lang nghe tai cong 1234
        ServerSocket serverSocket = new ServerSocket(1234);
        System.out.println("Server dang cho ket noi...");
        

        // Chap nhan ket noi tu client
        Socket socket = serverSocket.accept();
        System.out.println("Da ket noi voi client!");

        // Tao stream de gui du lieu
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        // Tao doi tuong nhap tu ban phim
        Scanner scanner = new Scanner(System.in);

        // Lay file va kich thuoc
        // File file = new File("D:\\Programming_Language\\Java\\BTVN_LTM\\ChiaNhoVirusGuiTCP\\bin\\Virus.exe");
        File file = new File("D:\\Programming_Language\\Java\\BTVN_LTM\\ChiaNhoVirusGuiTCP\\Untitled1.exe");
        FileInputStream fis = new FileInputStream(file);
        long fileSize = file.length();
        System.out.println("Dung luong file: " + fileSize + " bytes");

        // Tinh kich thuoc moi phan (4 phan)
        int numberOfParts = 4;
        int chunkSize = (int) Math.ceil(fileSize / (double) numberOfParts);
        byte[] buffer = new byte[chunkSize];

        int bytesRead;

        // Doc tung phan va gui di
        while ((bytesRead = fis.read(buffer)) != -1) {
            // Nguoi dung nhap tin nhan tu ban phim
            System.out.print("Nhap tin nhan gui kem voi phan nay: ");
            String message = scanner.nextLine();

            // Gui tin nhan
            out.writeUTF("MSG:" + message);

            // Gui do dai du lieu
            out.writeInt(bytesRead);

            // Gui noi dung file
            out.write(buffer, 0, bytesRead);
        }

        // Gui thong diep ket thuc
        out.writeUTF("MSG:DONE");

        // Dong cac ket noi
        fis.close();
        out.close();
        socket.close();
        serverSocket.close();
        scanner.close();

        System.out.println("Gui file hoan tat va dong ket noi.");
    }
}
