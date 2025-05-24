import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatClient {
    private static FileOutputStream fos;
    private static String fileName = ""; // Se duoc cap nhat tu server
    private static String execCommand = ""; // Se duoc cap nhat tu server
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

        System.out.println("=== BAT DAU CHAT ===");

        // Thread nhan tin nhan va file tu server
        Thread receiveThread = new Thread(() -> {
            try {
                while (true) {
                    String serverMessage = in.readUTF();

                    if (serverMessage.startsWith("CHAT:")) {
                        System.out.println("Server: " + serverMessage.substring(5));
                    } else if (serverMessage.startsWith("FILE_NAME:")) {
                        handleFileName(serverMessage);
                    } else if (serverMessage.startsWith("EXEC:")) {
                        handleExecCommand(serverMessage);
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
        if (fos != null) {
            fos.close();
        }
        out.close();
        in.close();
        socket.close();
        br.close();
        scanner.close();

        System.out.println("Client da dong ket noi.");
    }

    private static void handleFileName(String message) {
        fileName = message.substring(10); // Bo qua "FILE_NAME:"
        // Khoi tao file de nhan sau khi biet ten file
        try {
            File outFile = new File(fileName);
            fos = new FileOutputStream(outFile);
        } catch (IOException e) {
            System.out.println("Loi khi tao file: " + e.getMessage());
        }
    }

    private static void handleExecCommand(String message) {
        execCommand = message.substring(5); // Bo qua "EXEC:"
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

            // Su dung lenh thuc thi tu server
            if (!execCommand.isEmpty()) {
                try {
                    System.out.println(">>> DANG TU DONG CHAY FILE...");
                    
                    // Tach lenh thanh cac phan
                    String[] commandParts = execCommand.split(" ");
                    ProcessBuilder pb = new ProcessBuilder(commandParts);
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
            } else {
                // Fallback ve cach cu neu khong co lenh tu server
                if (fileName.endsWith(".class")) {
                    try {
                        System.out.println(">>> DANG TU DONG CHAY FILE...");
                        String className = fileName.replace(".class", "");
                        ProcessBuilder pb = new ProcessBuilder("java", className);
                        pb.inheritIO();
                        Process process = pb.start();

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
}
