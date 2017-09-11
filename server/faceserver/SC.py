#!/usr/bin/env python
# -*- coding: utf-8 -*-
import numpy as np
from numpy import linalg as LA
from scipy.sparse.csgraph import laplacian
from sklearn.cluster import KMeans

def euclid(si,sk):
	sit = si.copy()
	skt = sk.copy()
	return np.sqrt(np.sum((sit-skt)**2))

def get_distance_matrix(s):
	dim = s.shape[0]
	dis = np.zeros((dim,dim))
	for i in xrange(dim):
		for j in xrange(i+1,dim):
			dis[i][j] = euclid(s[i],s[j])
			dis[j][i] = dis[i][j]
	return dis

def self_tune(dis,k):
	dim = dis.shape[0]
	para = np.zeros((1,dim))
	for i in xrange(dim):
		k_index = dis[i].argsort()[-k]
		para[0][i] = np.sqrt(dis[i][k_index])
	return para

def get_affinity_matrix(dis,para):
	dim = dis.shape[0]
	para_matrix = np.dot(para.T,para)
	affinity_matrix = np.exp(-dis*dis/para_matrix)
	mask = (affinity_matrix == 1.0)
	affinity_matrix[mask] = 0.0
	return affinity_matrix

def pairwise(affinity_matrix,labels):
	labeled = len(labels)
	for i in xrange(labeled):
		for j in xrange(i+1,labeled):
			if labels[i] == labels[j]:
				affinity_matrix[i][j] = 1
			else:
				affinity_matrix[i][j] = 0
				pass
			affinity_matrix[j][i] = affinity_matrix[i][j]

def get_laplacian_matrix(affinity_matrix):
	dim = affinity_matrix.shape[0]
	laplacian_matrix = np.eye(dim) - laplacian(affinity_matrix,normed = True)
	return laplacian_matrix

def get_cluster_matrix(laplacian_matrix,pernum):
	dim = laplacian_matrix.shape[0]
	w, v = LA.eig(laplacian_matrix)
	cluster_matrix = np.zeros((dim,pernum))
	w_sorted = w.argsort()
	for i in xrange(pernum):
		cluster_matrix[:,i] = np.real(v[:,w_sorted[-i-1]])
	return cluster_matrix

def normalize(matrix):
	dim = matrix.shape[0]
	for i in xrange(dim):
		matrix[i] /= LA.norm(matrix[i])
			
class SpectralClustering(object):
	def __init__(self,n_clusters = 4,k_neibor = 8,random_state = 0,pair = False):
		self.n_clusters = n_clusters
		self.k_neibor = k_neibor
		self.random_state = random_state
		self.pair = pair

	def fit_predict(self,data,labels):
		dis = get_distance_matrix(data)
		para = self_tune(dis,self.k_neibor)
		affinity_matrix = get_affinity_matrix(dis,para)
		#print affinity_matrix
		if self.pair:
			pairwise(affinity_matrix,labels)
			#print affinity_matrix
			pass
		laplacian_matrix = get_laplacian_matrix(affinity_matrix)
		cluster_matrix = get_cluster_matrix(laplacian_matrix,self.n_clusters)
		#print para
		#print affinity_matrix
		#print cluster_matrix
		#print cluster_matrix.shape
		normalize(cluster_matrix)
		kmeans = KMeans(n_clusters=self.n_clusters, random_state=self.random_state,n_jobs=-1).fit(cluster_matrix)
		#print laplacian_matrix
		#print cluster_matrix
		self.mark_ = laplacian_matrix[-1][:-1]
		return laplacian_matrix[-1].argsort()[-1]
		#print kmeans.labels_
		#return kmeans.labels_