import java.io.IOException;
import java.util.Scanner;

public class OpenNotepadFiveTimes {
    public static void main(String[] args) {
        System.out.println("Bắt đầu mở Notepad 5 lần...");

        for (int i = 1; i <= 5; i++) {
            System.out.println("Đang mở Notepad lần thứ " + i + "...");

            try {
                // Sử dụng Runtime để mở Notepad
                Runtime.getRuntime().exec("notepad");
                
                // Tạm dừng 1 giây giữa các lần mở
                Thread.sleep(1000);
            } catch (IOException e) {
                System.out.println("Lỗi khi mở Notepad: " + e.getMessage());
            } catch (InterruptedException e) {
                System.out.println("Thread bị gián đoạn: " + e.getMessage());
            }
        }

        System.out.println("Đã mở xong 5 cửa sổ Notepad!");
        System.out.println("Nhấn Enter để thoát...");
        new Scanner(System.in).nextLine();
    }
}
