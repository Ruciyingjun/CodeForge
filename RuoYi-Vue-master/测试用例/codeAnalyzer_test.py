def calculate_grade(score):
    if score>100:    # 缺少空格
        return "Invalid score"
    elif score>=90:
        return"A"    # 缺少空格
    elif score>=80:
        return "B"
    elif score>=70:
        return "C"
    elif score>=60:
        return "D"
    else:
        return"F"    # 缺少空格

def add_numbers(a,b):    # 参数之间缺少空格
    Result=a+b    # 变量名大写，运算符两边缺少空格
    print(Result)    # 直接打印而不是返回值
    return Result

# 测试代码
print(calculate_grade(85))
print(add_numbers(10,20))