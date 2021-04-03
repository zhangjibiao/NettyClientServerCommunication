# -*- coding: UTF-8 -*-
import sys
import time

print("模拟等待，等待5s")
sys.stdout.flush()#必须有,不对，这个是保证能即使收到，不加时，会在程序结束时才输出
#time.sleep之前会结束程序是因为 读取输出语句的时候没搞好
time.sleep(5) # 休眠

#print("模拟程序出错 除数为0")
#sys.stdout.flush()

#a = 45/0
print("Iris captured successfully")
sys.stdout.flush()
time.sleep(5)

print("模拟等待下一次捕获虹膜，等待10S")
sys.stdout.flush()
time.sleep(10)

print("Iris captured successfully")
sys.stdout.flush()
time.sleep(5)

print("模拟程序出错,输出日志，等待5S")
sys.stdout.flush()

print("python程序出错日志")
sys.stdout.flush()



time.sleep(60*60*24)
