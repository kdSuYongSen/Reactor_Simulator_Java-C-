// GDB 调试能力验证：断点/单步/变量观察
#include <iostream>
#include <vector>
#include <string>

int compute(int n) {
    int sum = 0;
    for (int i = 1; i <= n; ++i) sum += i;
    return sum;
}

int main() {
    std::vector<std::string> items = {"core", "turbine", "condenser"};
    int total = compute(10);
    std::cout << "items=" << items.size() << " total=" << total << std::endl;
    for (const auto& s : items) std::cout << "  - " << s << "\n";
    return 0;
}
