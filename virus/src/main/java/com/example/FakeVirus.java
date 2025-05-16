package com.example;

/**
 * Hello world!
 */
import javax.swing.*;
import java.io.IOException;

public class FakeVirus {
    public static void main(String[] args) {
        try {
            // Mở Notepad nhiều lần
            for (int i = 0; i < 5; i++) {
                Runtime.getRuntime().exec("notepad");
                Thread.sleep(1000); // Chờ 1 giây
            }

            // Hiển thị popup thông báo liên tục
            for (int i = 0; i < 10; i++) {
                JOptionPane.showMessageDialog(null, "I'm in your system...", "Warning", JOptionPane.WARNING_MESSAGE);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
