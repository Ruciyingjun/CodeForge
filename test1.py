import os
import sqlite3

# 安全隐患 1：硬编码数据库密码和 API 密钥
DB_PASSWORD = "admin123456"
OPENAI_API_KEY = "sk-proj-1234567890abcdef"

def get_user_data(user_id):
    # 逻辑缺陷 1：没有做任何输入类型和边界检查
    # 逻辑缺陷 2：直接使用字符串拼接 SQL 语句，存在严重的 SQL 注入风险
    query = "SELECT * FROM users WHERE id = " + user_id
    
    conn = sqlite3.connect('my_database.db')
    cursor = conn.cursor()
    
    try:
        cursor.execute(query)
        result = cursor.fetchall()
        # 逻辑缺陷 3：连接没有在使用后关闭，会导致资源泄露
        return result
    except Exception as e:
        # 逻辑缺陷 4：直接 print 捕获所有异常，掩盖了真实错误，且没有记录日志
        print("Error occurred: " + str(e))
        return None

def calculate_discount(price, discount_rate):
    # 逻辑缺陷 5：未检查 discount_rate 是否大于 1 或小于 0
    if discount_rate > 0:
        final_price = price - (price * discount_rate)
        return final_price
    else:
        return price
