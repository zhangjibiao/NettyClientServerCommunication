# -*- coding: utf-8 -*-
import cv2
import sys
 
cam = cv2.VideoCapture(0)
cam.set(3, 640) # 窗口宽度
cam.set(4, 640) # 窗口高度

 
eyeCascade = cv2.CascadeClassifier('/home/pi/iris_recognition/Send/haarcascade_eye.xml')
# 每个人都以独立的id记录数据
# eye_id = input('\n 输入ID并按下回车 ==>  ')
eye_id = str('fyf_test')

print("摄像头初始化中...")
sys.stdout.flush()
# 初始化
count = 0

while(True):
    ret, img = cam.read()
    img = cv2.flip(img,1) # 垂直翻转图像
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

    eyes = eyeCascade.detectMultiScale(
        gray,     
        scaleFactor=1.2,
        minNeighbors=5,     
        minSize=(180,180)
    ) 
    for (x,y,w,h) in eyes:
        #No save image, Camera preheating
        cv2.rectangle(img, (x,y), (x+w,y+h), (255,0,0), 2)     
        count += 1
        
       # 保存截取的图像
        if count>=15:
            cv2.imwrite("dataset/" + str(eye_id) + '_' + str(count-14) + ".jpg", gray[0:640,0:640])
        cv2.imshow('image', img)
        cv2.moveWindow("image",0,0)
       
        
    k = cv2.waitKey(100) & 0xff # 按esc退出摄像头界面
    if k == 27:
        break
    elif count >= 29: # 截取30张人脸图像后退出
        break
 
# 释放摄像头
print("释放摄像头")
sys.stdout.flush()
print("Iris captured successfully") #when print this, java program send the image
sys.stdout.flush()
cam.release()
cv2.destroyAllWindows()
