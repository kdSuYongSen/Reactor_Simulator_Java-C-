// C++ 工具链冒烟测试：验证 cl.exe 能编译并运行
#include <iostream>
#include <vector>
#include <string>
#include <algorithm>

int main() {
    std::vector<std::string> langs = {"C++", "Java", "Python"};
    std::sort(langs.begin(), langs.end());
    std::cout << "C++ toolchain OK, std=" << __cplusplus / 100 % 100 << "\n";
    for (const auto& l : langs) std::cout << "  - " << l << "\n";
    return 0;
}
