import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) throws Exception {
        // Ket noi toi server
        Socket socket = new Socket("localhost", 1234);
        System.out.println("Da ket noi toi server!");

        // Tao stream de nhan du lieu
        DataInputStream in = new DataInputStream(socket.getInputStream());

        // Tao file de ghi file nhan duoc
        File outFile = new File("received.exe");
        FileOutputStream fos = new FileOutputStream(outFile);

        while (true) {
            // Doc tin nhan
            String message = in.readUTF();

            // Neu da ket thuc
            if (message.equals("MSG:DONE")) {
                System.out.println("Da nhan xong toan bo file.");
                break;
            }

            // Hien thi tin nhan
            System.out.println("Tin nhan: " + message.substring(4));

            // Nhan du lieu va ghi file
            int length = in.readInt();
            if (length > 0) {
                byte[] buffer = new byte[length];
                in.readFully(buffer);
                fos.write(buffer);
            }
        }

        // Dong stream
        fos.close();
        in.close();
        socket.close();

        // Thu chay file da nhan
        try {
            System.out.println("Dang chay file...");
            ProcessBuilder pb = new ProcessBuilder("received.exe");
            pb.inheritIO();
            pb.start();
        } catch (IOException e) {
            System.out.println("Loi khi chay file: " + e.getMessage());
        }
    }
}
