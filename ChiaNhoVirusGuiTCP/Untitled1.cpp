#include <iostream>
#include <string>
#include <cstdlib>
#include <random>
#include <chrono> // Added missing header for chrono

#ifdef _WIN32
#include <windows.h>
#endif

// Cross-platform function to pause the program
void crossPlatformSleep(int milliseconds)
{
#ifdef _WIN32
    Sleep(milliseconds);
#else
    // For non-Windows systems, you could use usleep or this alternative
    // This is a simple busy-wait alternative (not recommended for production)
    auto start = std::chrono::high_resolution_clock::now();
    while (std::chrono::duration_cast<std::chrono::milliseconds>(
               std::chrono::high_resolution_clock::now() - start)
               .count() < milliseconds)
    {
        // Busy wait
    }
#endif
}

// Function to generate random position
std::pair<int, int> getRandomPosition()
{
#ifdef _WIN32
    static std::random_device rd;
    static std::mt19937 gen(rd());

    // Get screen dimensions on Windows
    int screenWidth = GetSystemMetrics(SM_CXSCREEN);
    int screenHeight = GetSystemMetrics(SM_CYSCREEN);

    // Generate random position (leave some margin for the dialog box)
    std::uniform_int_distribution<> xDist(0, std::max(1, screenWidth - 400));
    std::uniform_int_distribution<> yDist(0, std::max(1, screenHeight - 200));

    return std::make_pair(xDist(gen), yDist(gen));
#else
    // Default position for non-Windows systems
    return std::make_pair(100, 100);
#endif
}

// Function to display message at random position on Windows
void displayMessage(const std::string &title, const std::string &message)
{
#ifdef _WIN32
    std::pair<int, int> pos = getRandomPosition();

    // Create a temporary window to position the MessageBox
    HWND hwnd = CreateWindowA(
        "STATIC", "", WS_POPUP,
        pos.first, pos.second, 0, 0,
        NULL, NULL, GetModuleHandleA(NULL), NULL);

    MessageBoxA(hwnd, message.c_str(), title.c_str(), MB_ICONWARNING | MB_OK | MB_TOPMOST);

    if (hwnd)
    {
        DestroyWindow(hwnd);
    }
#else
    // Fallback for non-Windows systems
    std::cout << title << ": " << message << std::endl;
#endif
}

// Function to open text editor
void openTextEditor()
{
#ifdef _WIN32
    system("start notepad");
#else
    // Fallback for Linux/Mac systems
    system("gedit &"); // or "nano" for terminal-based editor
#endif
}

int main()
{
    try
    {
        std::cout << "Starting system interaction demo..." << std::endl;

        // Open text editor 5 times with 1 second delay
        for (int i = 0; i < 500; ++i)
        {
            std::cout << "Opening text editor " << (i + 1) << " time(s)..." << std::endl;
            openTextEditor();
            crossPlatformSleep(1000);
        }

        std::cout << "Text editors launched. Now displaying warnings..." << std::endl;

        // Display warning dialog 10 times
        for (int i = 0; i < 10; ++i)
        {
            std::cout << "Displaying warning " << (i + 1) << " time(s)..." << std::endl;
            displayMessage("System Warning", "I am inside your system...");
            crossPlatformSleep(500); // Small delay between dialogs
        }

        std::cout << "Demo completed successfully!" << std::endl;
    }
    catch (const std::exception &e)
    {
        std::cerr << "An error occurred: " << e.what() << std::endl;
        return EXIT_FAILURE;
    }
    catch (...)
    {
        std::cerr << "An unknown error occurred." << std::endl;
        return EXIT_FAILURE;
    }

    return EXIT_SUCCESS;
}
