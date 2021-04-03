import cv2
# from sklearn.decomposition import PCA
import joblib
import numpy as np
import paddlex as pdx
import time
from PIL import Image

np.seterr(divide='ignore',invalid='ignore')

# 初始化
start1 = time.time()
# Model for segmention
seg_model = pdx.load_model('./seg_model/best_model')
# Model for classification
cls_model = joblib.load('./cls_model/GBC.model')
pca = joblib.load('./cls_model/pca.m')

image_name = '/home/project/java/iris.jpg'  #用于常看分割结果的图片
temp_jpg = './output/visualize_result/fyf1_7_cut.jpg'

end = time.time()
print("初始化:%.4f秒"%(end-start1))

# 预处理
start = time.time()
seg_result = np.array(seg_model.predict(image_name)['label_map'])

# pdx.seg.visualize(image_name, seg_result, weight=0.4, save_dir='./output/visualize_result/')

p1 = Image.open(image_name).convert('L')
p1_arr = np.array(p1)

# for i in range(p1_arr.shape[0]):
# 	for j in range(p1_arr.shape[1]):
# 		if seg_result[i, j] == 1:
# 			seg_result[i, j] = p1_arr[i, j]
# 		else:
# 			seg_result[i, j] = 0

seg_result = np.where(seg_result==1,p1_arr,0)

img = Image.fromarray(seg_result)
img.save(temp_jpg)

# 灰度转换
img = cv2.imread(temp_jpg)
gary = cv2.cvtColor(img,cv2.COLOR_BGR2GRAY)
# 直方图均衡化处理
hist_equ = cv2.equalizeHist(gary)
cv2.imwrite(temp_jpg,hist_equ)

end = time.time()
print("分割预处理:%.4f秒"%(end-start))


# 分类
start = time.time()
# Classification
pred = cv2.imread(temp_jpg)

X_max = np.load('cls_model/X_max.npy')
X_min = np.load('cls_model/X_min.npy')
X_diff = np.load('cls_model/X_diff.npy')
pred_scaler = np.where(X_diff==0,pred.reshape(1,-1),(pred.reshape(1,-1) - X_min) / X_diff)
pred_pca = pca.transform(pred_scaler)

cls_result = {'pred':[],'score':[]}
cls_result['pred'] = cls_model.predict(pred_pca)
cls_result['score'] = cls_model.predict_proba(pred_pca)
end = time.time()
print("分类:%.4f秒"%(end-start))
print("Total:%.4f秒"%(end-start1))
print("Predict Result :", cls_result['pred'][0],"\nScore :", max(cls_result['score'][0]))
if (max(cls_result['score'][0]) >= 0.9):
    print("1")
    print(cls_result['pred'][0])
else: print("0")
