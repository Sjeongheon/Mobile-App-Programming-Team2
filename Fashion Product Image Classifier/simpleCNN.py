# -*- coding: utf-8 -*-
"""
Created on Tue Dec 20 23:45:43 2022

@author: fight
"""

from sklearn.preprocessing import StandardScaler

import matplotlib.pyplot as plt
import matplotlib.image as mpimg

import tensorflow as tf
import numpy as np
import pandas as pd

import os

DATASET_PATH = "./fashion dataset/"
print(os.listdir(DATASET_PATH))
df = pd.read_csv(DATASET_PATH + "styles.csv", nrows=5000, error_bad_lines=False)
df['image'] = df.apply(lambda row: str(row['id']) + ".jpg", axis=1)
df = df.sample(frac=1).reset_index(drop=True)
df.head(10)


batch_size = 256

from keras_preprocessing.image import ImageDataGenerator

image_generator = ImageDataGenerator(
    validation_split=0.2
)

training_generator = image_generator.flow_from_dataframe(
    dataframe=df,
    directory=DATASET_PATH + "images",
    x_col="image",
    y_col="subCategory",
    target_size=(96,96),
    batch_size=batch_size,
    subset="training"
)

validation_generator = image_generator.flow_from_dataframe(
    dataframe=df,
    directory=DATASET_PATH + "images",
    x_col="image",
    y_col="subCategory",
    target_size=(96,96),
    batch_size=batch_size,
    subset="validation"
)

classes = len(training_generator.class_indices)
my_dict = training_generator.class_indices
key_list = list(my_dict.keys()) 
val_list = list(my_dict.values())

from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Conv2D
from tensorflow.keras.layers import MaxPooling2D
from tensorflow.keras.layers import Flatten, Dropout
from tensorflow.keras.layers import Dense

classifier = Sequential()

classifier.add(Conv2D(32,(3,3),input_shape = (96,96,3), activation = 'relu'))
classifier.add(Conv2D(64,(3,3), activation = 'relu'))
classifier.add(MaxPooling2D(pool_size=(3, 3)))
classifier.add(Dropout(0.1))
classifier.add(Conv2D(128,(5,5), activation = 'relu'))
classifier.add(MaxPooling2D(pool_size=(3, 3)))
classifier.add(Dropout(0.1))
classifier.add(Conv2D(128,(5,5), activation = 'relu'))
classifier.add(MaxPooling2D(pool_size=(3, 3)))
classifier.add(Dropout(0.1))
classifier.add(Flatten())
classifier.add(Dense(units=512,activation = 'relu'))
classifier.add(Dropout(0.2))
classifier.add(Dense(units=256,activation = 'relu'))
classifier.add(Dropout(0.1))
classifier.add(Dense(units=128,activation = 'relu'))
classifier.add(Dense(units=128,activation = 'relu'))

classifier.add(Dense(units=classes,activation = 'softmax'))

classifier.compile(optimizer='adam',
              loss="categorical_crossentropy",
              metrics=['accuracy'])
classifier.summary()


from math import ceil

hist = classifier.fit_generator(
    generator=training_generator,
    steps_per_epoch=ceil(0.8 * (df.shape[0] / batch_size)),

    validation_data=validation_generator,
    validation_steps=ceil(0.2 * (df.shape[0] / batch_size)),
    epochs=10,
    verbose=1
)

loss, acc = classifier.evaluate_generator(validation_generator, steps=ceil(0.2 * (df.size / batch_size)))
print("\n%s: %.2f%%" % (classifier.metrics_names[1], acc * 100))

fig, ax = plt.subplots(1,2,figsize=(15,4))

ax[0].plot(hist.history['loss'],'y',label='train loss')
ax[0].plot(hist.history['val_loss'],'r',label='val loss')
ax[1].plot(hist.history['accuracy'],'b',label='train acc')
ax[1].plot(hist.history['val_accuracy'],'g',label='val acc')

ax[0].set_xlabel('epoch')
ax[0].set_ylabel('loss')

ax[1].set_xlabel('epoch')
ax[1].set_ylabel('accuracy')

ax[0].legend(loc='upper left')
ax[1].legend(loc='upper left')
plt.show()

'''
classifier.evaluate(validation_generator)

converter = tf.lite.TFLiteConverter.from_keras_model(classifier)
tflite_model = converter.convert()

with open("Clothing_ClassifierTFlite_model.tflite",'wb') as f:
  f.write(tflite_model)
'''