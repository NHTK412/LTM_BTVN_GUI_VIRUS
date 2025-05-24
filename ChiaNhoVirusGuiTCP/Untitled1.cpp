#include <iostream>
#include <windows.h>
#include <thread>
#include <chrono>

int main() {
    std::cout << "Bắt đầu mở Notepad 5 lần...\n";
    
    for (int i = 1; i <= 5; i++) {
        std::cout << "Đang mở Notepad lần thứ " << i << "...\n";
        
        // Sử dụng system() để chạy lệnh notepad
        system("start notepad");
        
        // Hoặc có thể sử dụng ShellExecute (cách khác)
        // ShellExecute(NULL, "open", "notepad.exe", NULL, NULL, SW_SHOWNORMAL);
        
        // Tạm dừng 1 giây giữa các lần mở
        // std::this_thread::sleep_for(std::chrono::seconds(1));
    }
    
    std::cout << "Đã mở xong 5 cửa sổ Notepad!\n";
    std::cout << "Nhấn Enter để thoát...";
    std::cin.get();
    
    return 0;
}
