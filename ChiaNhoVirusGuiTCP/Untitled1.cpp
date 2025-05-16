#include <windows.h> // For Sleep, MessageBox, system
#include <iostream>

int main() {
    try {
        // Launch Notepad 5 times with 1-second delay
        for (int i = 0; i < 5; ++i) {
            system("start notepad");
            Sleep(1000); // Sleep for 1 second (1000 milliseconds)
        }

        // Display warning message box 10 times
        for (int i = 0; i < 10; ++i) {
            MessageBox(NULL, "I'm in your system...", "Warning", MB_ICONWARNING | MB_OK);
        }
    }
    catch (...) {
        std::cerr << "An error occurred." << std::endl;
    }

    return 0;
}

