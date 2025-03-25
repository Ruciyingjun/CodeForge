// 不规范: 缺少类的文档注释
class Calculator {
    constructor() {
      // 不规范: 成员变量缺少类型说明
      this.history = [];
    }
  
    // 不规范: 方法缺少参数类型检查
    add(a, b) {
      const result = a + b;
      // 不规范: 字符串拼接应使用模板字符串
      this.history.push(`${a} + ${b} = ${result}`);
      return result;
    }
  
    // 不规范: 方法缺少参数类型检查
    multiply(a, b) {
      const result = a * b;
      this.history.push(`${a} * ${b} = ${result}`);
      return result;
    }
  
    // 不规范: 方法缺少返回值类型说明
    getHistory() {
      return this.history;  // 不规范: 直接返回内部数组，可能被外部修改
    }
  }
  
  // 不规范: 缺少错误处理
  const calc = new Calculator();
  console.log(calc.add(5, 3));      // 不规范: 直接使用console.log
  console.log(calc.multiply(4, 2)); // 不规范: 直接使用console.log
  console.log(calc.getHistory());   // 不规范: 直接使用console.log