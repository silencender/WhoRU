#!/usr/bin/env python
# -*- coding: utf-8 -*-
from face import Analyse,Recognize
from flask import Flask,request
import json
import numpy
app = Flask(__name__)

@app.route('/')
def index():
	return 'This is our face api'

@app.route('/analyse', methods=['POST'])
def analyse():
	device = str(request.form['device'])
	person = unicode(request.form['person'])
	picname = str(request.form['picname'])
	# device = 'd41d8cd98f00b204e9800998ecf8427e'
	# person = 'hhh'
	# picname = 'aligned_1505107494044.jpg'
	analyser = Analyse(device,person,picname)
	data = analyser.get_data().tolist()
	return json.dumps(data)

@app.route('/recognize', methods=['POST'])
def recognize():
	device = str(request.form['device'])
	picname = str(request.form['picname'])
	people = json.loads(str(request.form['people']))
	# device = 'd41d8cd98f00b204e9800998ecf8427e'
	# person = 'hhh'
	# picname = 'aligned_1505107494044.jpg'

	recognizer = Recognize(device,picname,people)
	result = recognizer.get_result()
	return json.dumps(result)

#print analyse()
app.run(host='0.0.0.0')