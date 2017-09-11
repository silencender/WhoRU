#!/usr/bin/env python
# -*- coding: utf-8 -*-
import openface
import cv2
import numpy as np
#np.set_printoptions(precision=2)
import openface
#from sklearn.cluster import SpectralClustering as SC
from SC import SpectralClustering as SC

of_dir = '/root/openface'
share_dir = '/root/prp/photos'
DIVIDE = '/'
UNNAMED = 'UNKNOWN'
net = openface.TorchNeuralNet(of_dir+'/models/openface/nn4.small2.v1.t7', imgDim=96)

class Analyse:
	def __init__(self,device,person,picname):
		img = share_dir + DIVIDE + device + DIVIDE + person + DIVIDE + picname
		bgrImg = cv2.imread(img.encode('utf-8'))
		rgbImg = cv2.cvtColor(bgrImg, cv2.COLOR_BGR2RGB)
		self.data = net.forward(rgbImg)

	def get_data(self):
		return self.data

class Recognize:
	def __init__(self,device,picname,people):
		analyser = Analyse(device,UNNAMED,picname)
		self.data = analyser.get_data()
		labels = []
		data = np.empty([0,128])
		for key,value in people.iteritems():
			for i in xrange(len(value)):
				labels.append(key)
			data = np.vstack((data,np.asarray(value)))
		data = np.vstack((data,self.data))
		k_neibor = 8
		if len(labels) < 8:
			k_neibor = len(labels)
		sc = SC(n_clusters=len(people),k_neibor = k_neibor)
		output = sc.fit_predict(data,labels)
		self.name = labels[output]
		self.confidence = 90

	def get_data(self):
		return self.data

	def get_result(self):
		return {'name':self.name,'confidence':self.confidence}