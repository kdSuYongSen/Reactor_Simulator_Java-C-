// =============================================================================
//  Dev-Cpp / TDM-GCC 综合自检
//
//  这个文件用于一次性验证 C++ 工具链的各项能力：
//    - C++17 标准库（iostream / vector / string / algorithm / memory）
//    - 模板、lambda、auto 类型推导
//    - 结构体与类
//    - 异常处理
//    - 文件读写
//    - 多线程支持
//  编译：tools\dev\run-gcc.cmd  cpp\toolchain_check.cpp
// =============================================================================

#include <algorithm>
#include <exception>
#include <fstream>
#include <iostream>
#include <map>
#include <memory>
#include <numeric>
#include <sstream>
#include <stdexcept>
#include <string>
#include <thread>
#include <vector>

namespace {

/// 模板 + 容器：验证泛型与标准库算法
template <typename T>
T sum_of(const std::vector<T>& values) {
    return std::accumulate(values.begin(), values.end(), T{0});
}

/// 结构体 + 运算符重载
struct Reactor {
    double power;
    double temperature;

    std::string to_string() const {
        std::ostringstream os;
        os << "P=" << power << "MW, T=" << temperature << "C";
        return os.str();
    }
};

/// lambda + auto 推导 + 智能指针
auto make_gauge(const std::string& label) -> std::unique_ptr<std::string> {
    auto prefixed = std::make_unique<std::string>(label + "-GAUGE");
    return prefixed;
}

/// 自定义异常
class SafetyError : public std::runtime_error {
public:
    explicit SafetyError(const std::string& what) : std::runtime_error(what) {}
};

}  // namespace

int main() {
    std::cout << "=== Dev-Cpp / TDM-GCC toolchain check ===\n";
    std::cout << "  __cplusplus = " << __cplusplus << " (201703 = C++17)\n";
#ifdef __MINGW64__
    std::cout << "  target      = MinGW-w64 / x86_64\n";
#endif
    std::cout << "\n";

    // 1) 模板与标准库算法
    const std::vector<int> powers = {210, 180, 240, 195, 225};
    std::vector<int> sorted = powers;
    std::sort(sorted.begin(), sorted.end());
    std::cout << "[1] template + algorithm\n";
    std::cout << "    sorted powers : ";
    for (int p : sorted) std::cout << p << " ";
    std::cout << "\n    sum           : " << sum_of(powers) << " MW\n\n";

    // 2) 结构体与字符串化
    std::cout << "[2] struct + ostringstream\n";
    const Reactor core{225.0, 312.5};
    std::cout << "    " << core.to_string() << "\n\n";

    // 3) lambda / auto / 智能指针
    std::cout << "[3] lambda + auto + unique_ptr\n";
    auto adder = [](int a, int b) { return a + b; };
    auto gauge = make_gauge("MTK-1");
    std::cout << "    adder(2, 3)   = " << adder(2, 3) << "\n";
    std::cout << "    gauge         = " << *gauge << "\n\n";

    // 4) 关联容器
    std::cout << "[4] map\n";
    std::map<std::string, int> systems{{"ECCS", 1}, {"TDA", 1}, {"MSK", 3}};
    for (const auto& [name, count] : systems) {
        std::cout << "    " << name << " x" << count << "\n";
    }
    std::cout << "\n";

    // 5) 异常处理
    std::cout << "[5] exception handling\n";
    try {
        throw SafetyError("AZ-5: reactivity margin exceeded");
    } catch (const SafetyError& e) {
        std::cout << "    caught: " << e.what() << "\n";
    } catch (const std::exception&) {
        std::cout << "    caught: generic std::exception\n";
    }
    std::cout << "\n";

    // 6) 多线程
    std::cout << "[6] std::thread\n";
    std::string worker_result;
    std::thread worker([&worker_result] { worker_result = "worker thread finished"; });
    worker.join();
    std::cout << "    " << worker_result << "\n\n";

    // 7) 文件读写
    std::cout << "[7] file i/o\n";
    const std::string tmp = "bin/gcc/toolchain_check.txt";
    {
        std::ofstream out(tmp);
        out << "RBMK-1500 toolchain check\n";
    }
    {
        std::ifstream in(tmp);
        std::string first_line;
        std::getline(in, first_line);
        std::cout << "    wrote+read back: \"" << first_line << "\"\n";
    }
    std::remove(tmp.c_str());

    std::cout << "\n=== ALL CHECKS PASSED ===\n";
    return 0;
}