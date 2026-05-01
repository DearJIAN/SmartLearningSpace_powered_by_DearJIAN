#include <iostream>
#include <fstream>
#include <ctime>
#include <vector>
#include <windows.h>

using namespace std;

// 统计数字出现次数
void countDigits(int n, vector<int>& digitCount) {
    for (int i = 1; i <= n; i++) {
        int num = i;
        while (num > 0) {
            digitCount[num % 10]++;
            num /= 10;
        }
    }
}

int main() {
    SetConsoleOutputCP(CP_UTF8);  // 设置控制台输出为 UTF-8
    ifstream inputFile("input.txt");
    int n;
    inputFile >> n;
    cout << "Read n: " << n << endl; // 打印读取的数字
    inputFile.close();


    vector<int> digitCount(10, 0);

    clock_t start = clock();
    countDigits(n, digitCount);
    clock_t end = clock();

    // 打印结果
    for (int i = 0; i < 10; i++) {
        cout << "出现次数 " << i << ": " << digitCount[i] << endl;
    }

    // 计算运行时间
    double duration = (double)(end - start) / CLOCKS_PER_SEC * 1000; // 毫秒
    cout << "运行时间 " << duration << " 毫秒" << endl;

    // 输出到文件
    ofstream outputFile("output.txt");
    for (int i = 0; i < 10; i++) {
        outputFile << "出现次数 " << i << ": " << digitCount[i] << endl;
    }
    outputFile << "运行时间 " << duration << " 毫秒" << endl;
    outputFile.close();

    return 0;
}
