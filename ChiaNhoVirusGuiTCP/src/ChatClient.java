import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatClient {
    private static FileOutputStream fos;
    private static String fileName = "OpenNotepadFiveTimes.class";
    private static boolean fileReceivingComplete = false;
    private static int partsReceived = 0;

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Nhap dia chi IP server: ");
        String host = br.readLine();
        System.out.print("Nhap cong server: ");
        int port = Integer.parseInt(br.readLine());

        // Ket noi toi server
        Socket socket = new Socket(host, port);
        System.out.println("Da ket noi toi server!");

        // Tao stream de gui va nhan du lieu
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        DataInputStream in = new DataInputStream(socket.getInputStream());

        // Khoi tao file de nhan
        File outFile = new File(fileName);
        fos = new FileOutputStream(outFile);

        System.out.println("=== BAT DAU CHAT ===");
        // System.out.println("(Se tu dong nhan file khi server gui)");

        // Thread nhan tin nhan va file tu server
        Thread receiveThread = new Thread(() -> {
            try {
                while (true) {
                    String serverMessage = in.readUTF();

                    if (serverMessage.startsWith("CHAT:")) {
                        System.out.println("Server: " + serverMessage.substring(5));
                    } else if (serverMessage.equals("FILE_PART_START")) {
                        receiveFilePart(in);
                    } else if (serverMessage.equals("FILE_COMPLETE")) {
                        handleFileComplete();
                    } else if (serverMessage.equals("SERVER_DISCONNECT")) {
                        System.out.println("Server da ngat ket noi!");
                        break;
                    }
                }
            } catch (IOException e) {
                System.out.println("Mat ket noi voi server!");
            }
        });
        receiveThread.start();

        // Gui tin nhan
        Scanner scanner = new Scanner(System.in);
        String message;
        while (true) {
            message = scanner.nextLine();

            if (message.equalsIgnoreCase("quit")) {
                out.writeUTF("CLIENT_DISCONNECT");
                break;
            }

            out.writeUTF("CHAT:" + message);
        }

        // Dong cac ket noi
        fos.close();
        out.close();
        in.close();
        socket.close();
        br.close();
        scanner.close();

        System.out.println("Client da dong ket noi.");
    }

    private static void receiveFilePart(DataInputStream in) throws IOException {
        // Nhan do dai du lieu
        int length = in.readInt();
        if (length > 0) {
            byte[] buffer = new byte[length];
            in.readFully(buffer);
            fos.write(buffer);
            partsReceived++;
            System.out.println(">>> Da nhan phan " + partsReceived + " (" + length + " bytes)");
        }
    }

    private static void handleFileComplete() throws IOException {
        if (!fileReceivingComplete) {
            fileReceivingComplete = true;
            fos.close();

            System.out.println(">>> DA NHAN XONG TOAN BO FILE!");
            System.out.println(">>> File da duoc luu tai: " + new File(fileName).getAbsolutePath());

            // Tu dong chay file .class
            if (fileName.endsWith(".class")) {
                try {
                    System.out.println(">>> DANG TU DONG CHAY FILE...");
                    String className = fileName.replace(".class", "");
                    ProcessBuilder pb = new ProcessBuilder("java", className);
                    pb.inheritIO();
                    Process process = pb.start();

                    // Chay file trong thread rieng de khong block chat
                    Thread executeThread = new Thread(() -> {
                        try {
                            process.waitFor();
                            System.out.println(">>> File da chay xong!");
                        } catch (InterruptedException e) {
                            System.out.println(">>> Loi khi chay file: " + e.getMessage());
                        }
                    });
                    executeThread.start();

                } catch (IOException e) {
                    System.out.println(">>> Loi khi chay file: " + e.getMessage());
                }
            }
        }
    }
}
