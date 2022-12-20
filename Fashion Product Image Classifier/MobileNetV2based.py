# -*- coding: utf-8 -*-
"""
Created on Tue Dec 20 22:53:53 2022

@author: fight
"""

from sklearn.preprocessing import StandardScaler

import matplotlib.pyplot as plt
import matplotlib.image as mpimg

import numpy as np
import pandas as pd

import os

DATASET_PATH = "./fashion dataset/myntradataset/"
print(os.listdir(DATASET_PATH))
df = pd.read_csv(DATASET_PATH + "styles.csv", nrows=5000, error_bad_lines=False)
df['image'] = df.apply(lambda row: str(row['id']) + ".jpg", axis=1)
df = df.sample(frac=1).reset_index(drop=True)
df.head(10)

batch_size = 32

from keras_preprocessing.image import ImageDataGenerator

image_generator = ImageDataGenerator(validation_split=0.2)

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

from keras.models import Sequential, Model
from keras.layers import Conv2D, MaxPooling2D, Flatten, Dense, GlobalAveragePooling2D
from keras.applications.mobilenet_v2 import MobileNetV2

# create the base pre-trained model
base_model = MobileNetV2(input_shape=(96, 96, 3), include_top=False, weights='imagenet')

# add a global spatial average pooling layer
x = base_model.output
x = GlobalAveragePooling2D()(x)
x = Dense(1024, activation='relu')(x)
predictions = Dense(classes, activation='softmax')(x)

# this is the model we will train
model = Model(inputs=base_model.input, outputs=predictions)

# first: train only the top layers (which were randomly initialized)
# i.e. freeze all convolutional InceptionV3 layers
for layer in base_model.layers:
    layer.trainable = False

model.compile(optimizer='adam', loss='categorical_crossentropy', metrics=['accuracy'])
model.summary()

from math import ceil

model.fit_generator(
    generator=training_generator,
    steps_per_epoch=ceil(0.8 * (df.size / batch_size)),

    validation_data=validation_generator,
    validation_steps=ceil(0.2 * (df.size / batch_size)),

    epochs=5,
    verbose=1
)

loss, acc = model.evaluate_generator(validation_generator, steps=ceil(0.2 * (df.size / batch_size)))
print("\n%s: %.2f%%" % (model.metrics_names[1], acc * 100))
