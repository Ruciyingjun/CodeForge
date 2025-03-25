public class Calculator {
    // 不规范: 魔法数字，应该使用 Math.PI
    private static final double PI = 3.14159;
    
    // 不规范: 成员变量缺少注释说明
    private int result = 0;
    
    // 不规范: 方法缺少文档注释
    public int add(int a, int b) {
        result = a + b;  // 不规范: 直接修改成员变量，可能引起并发问题
        return result;
    }
    
    // 不规范: 方法缺少文档注释
    public double calculateCircleArea(double radius) {
        if (radius < 0) {
            throw new IllegalArgumentException("半径不能为负数");
        }
        // 不规范: 使用了自定义的PI常量，而不是Math.PI
        return PI * radius * radius;
    }
    
    // 不规范: 方法缺少文档注释
    public boolean isPrime(int number) {
        if (number <= 1) {
            return false;
        }
        // 不规范: 变量名i不够描述性
        for (int i = 2; i <= Math.sqrt(number); i++) {
            if (number % i == 0) {
                return false;
            }
        }
        return true;
    }
}