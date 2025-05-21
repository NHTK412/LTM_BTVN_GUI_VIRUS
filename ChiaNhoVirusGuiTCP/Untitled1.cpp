#include <iostream>
#include <string> // Cần thiết cho std::string
#include <cstdlib> // Cần thiết cho system() và EXIT_SUCCESS, EXIT_FAILURE

#ifdef _WIN32
#include <windows.h> // Cho Sleep và MessageBox
#else
#include <unistd.h> // Cho sleep (Lưu ý: sleep ở đây tính bằng giây)
// Đối với MessageBox trên Linux, chúng ta sẽ sử dụng một giải pháp dòng lệnh đơn giản
// hoặc bạn có thể tích hợp một thư viện GUI như GTK+ hoặc Qt nếu cần giao diện đồ họa phức tạp.
// Trong ví dụ này, chúng tôi sẽ mô phỏng một thông báo đơn giản trên console
// hoặc sử dụng một công cụ dòng lệnh nếu có (ví dụ: zenity).
#endif

// Hàm đa nền tảng để tạm dừng chương trình
void crossPlatformSleep(int milliseconds) {
#ifdef _WIN32
    Sleep(milliseconds);
#else
    sleep(milliseconds / 1000); // Chuyển milliseconds sang giây
#endif
}

// Hàm đa nền tảng để hiển thị thông báo
// Trên Linux, hàm này sẽ in ra console hoặc cố gắng sử dụng zenity nếu có.
void displayMessage(const std::string& title, const std::string& message) {
#ifdef _WIN32
    MessageBox(NULL, message.c_str(), title.c_str(), MB_ICONWARNING | MB_OK);
#else
    // Giải pháp đơn giản cho Linux: In ra console
    std::cout << "[" << title << "] " << message << std::endl;
    // Hoặc cố gắng sử dụng zenity (cần cài đặt zenity trên hệ thống Linux)
    // std::string command = "zenity --warning --title=\"" + title + "\" --text=\"" + message + "\"";
    // int result = system(command.c_str());
    // if (result != 0) {
    //     std::cerr << "Lỗi khi thực thi zenity hoặc zenity không được cài đặt." << std::endl;
    //     std::cout << "[Thông báo thay thế][" << title << "] " << message << std::endl;
    // }
#endif
}

// Hàm đa nền tảng để mở trình soạn thảo văn bản
void openTextEditor() {
#ifdef _WIN32
    system("start notepad");
#else
    // Thử mở các trình soạn thảo văn bản phổ biến trên Linux
    // Bạn có thể điều chỉnh danh sách này hoặc chọn một trình soạn thảo cụ thể
    if (system("xdg-open '' > /dev/null 2>&1 && (gedit > /dev/null 2>&1 || xed > /dev/null 2>&1 || kate > /dev/null 2>&1 || pluma > /dev/null 2>&1 || mousepad > /dev/null 2>&1 || leafpad > /dev/null 2>&1 || nano) &") != 0) {
        // Nếu xdg-open không thành công hoặc không có trình soạn thảo đồ họa nào được tìm thấy,
        // thử mở một trình soạn thảo dòng lệnh cơ bản như nano hoặc vi.
        // Lưu ý: Việc mở trình soạn thảo dòng lệnh sẽ chặn chương trình cho đến khi nó được đóng.
        // Để tránh điều này, bạn có thể cần các kỹ thuật phức tạp hơn (ví dụ: forking).
        // Trong ví dụ này, chúng tôi sẽ ưu tiên các trình soạn thảo GUI không chặn.
        // Nếu bạn muốn mở trình soạn thảo dòng lệnh và để chương trình tiếp tục,
        // bạn có thể cần loại bỏ dấu '&' và xử lý việc này một cách khác.
        if (system("nano") != 0) { // Thử nano
            if (system("vi") != 0) { // Thử vi nếu nano không có
                 std::cerr << "Không thể mở trình soạn thảo văn bản." << std::endl;
            }
        }
    }
#endif
}

int main() {
    try {
        // Mở trình soạn thảo văn bản 5 lần với độ trễ 1 giây
        for (int i = 0; i < 5; ++i) {
            std::cout << "Đang mở trình soạn thảo lần thứ " << i + 1 << "..." << std::endl;
            openTextEditor();
            crossPlatformSleep(1000); // Tạm dừng 1 giây
        }

        // Hiển thị hộp thoại cảnh báo 10 lần
        for (int i = 0; i < 10; ++i) {
            std::cout << "Hiển thị cảnh báo lần thứ " << i + 1 << "..." << std::endl;
            displayMessage("Cảnh báo", "Tôi đang ở trong hệ thống của bạn...");
        }
    }
    catch (const std::exception& e) { // Bắt các ngoại lệ chuẩn cụ thể
        std::cerr << "Đã xảy ra lỗi: " << e.what() << std::endl;
        return EXIT_FAILURE;
    }
    catch (...) { // Bắt tất cả các loại ngoại lệ khác
        std::cerr << "Đã xảy ra một lỗi không xác định." << std::endl;
        return EXIT_FAILURE;
    }

    return EXIT_SUCCESS;
}