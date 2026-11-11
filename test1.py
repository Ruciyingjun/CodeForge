
import os

def calculate_total(price, quantity):
    # 故意没写 docstring
    if price < 0:
        print("Price cannot be negative") # 故意用了 print 而不是抛异常
        return None
    
    total = price * quantity
    
    # 故意写一段没用的无用代码
    unused_variable = "hello"
    
    return total

# 故意用硬编码密码
db_password = "my_secret_password_123"

def connect_db():
    print("Connecting...")
