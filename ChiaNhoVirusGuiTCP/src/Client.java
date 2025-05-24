import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Nhap dia chi IP server: ");
        String host = br.readLine();
        System.out.print("Nhap cong server: ");
        int port = Integer.parseInt(br.readLine());

        // Ket noi toi server
        Socket socket = new Socket(host, port);
        System.out.println("Da ket noi toi server!");

        // Tao stream de nhan du lieu
        DataInputStream in = new DataInputStream(socket.getInputStream());

        // Ten file class duoc gui tu server
        String fileName = in.readUTF();  // VD: Hello.class
        File outFile = new File(fileName);
        FileOutputStream fos = new FileOutputStream(outFile);

        while (true) {
            String message = in.readUTF();

            if (message.equals("MSG:DONE")) {
                System.out.println("Da nhan xong toan bo file.");
                break;
            }

            System.out.println("Tin nhan: " + message.substring(4));

            int length = in.readInt();
            if (length > 0) {
                byte[] buffer = new byte[length];
                in.readFully(buffer);
                fos.write(buffer);
            }
        }

        fos.close();
        in.close();
        socket.close();

        // Thu chay file .class da nhan
        try {
            System.out.println("Dang chay file...");
            // Tách tên lớp (không có phần mở rộng .class)
            String className = outFile.getName().replace(".class", "");
            ProcessBuilder pb = new ProcessBuilder("java", className);
            pb.inheritIO();
            pb.start();
        } catch (IOException e) {
            System.out.println("Loi khi chay file: " + e.getMessage());
        }
    }
}
