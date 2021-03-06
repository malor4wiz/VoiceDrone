from typing import Optional
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import base64
import argparse
import time
import csv
import os
from tqdm import *
import torch
from torch.autograd import Variable
from torch.utils.data import DataLoader
from torchvision.transforms import *
import torchnet
from datasets import *
from transforms import *
import models
import numpy as np
 
n_mels = 32
model = models.create_model(model_name='vgg19_bn', num_classes=len(CLASSES),in_channels=1)
model.load_state_dict(torch.load('model.pth')['state_dict'])
model.float()
app = FastAPI()

def to_melspectrogram(samples, n_mels):
        samples = samples
        sample_rate = 16000
        s = librosa.feature.melspectrogram(samples, sr=sample_rate, n_mels=n_mels)
        melspec = librosa.power_to_db(s, ref=np.max)
        return melspec

def load_audio(path):
    sample_rate = 16000
    samples, sample_rate = librosa.load(path, sample_rate)
    return samples

def fix_audio_length(samples):
        sample_rate = 16000
        duration = 1
        length = int(duration * sample_rate)
        if length < len(samples):
            samples = samples[:length]
        elif length > len(samples):
            samples = np.pad(samples, (0, length - len(samples)), "constant")
        return samples

def softmax(a):
    c = np.max(a)
    exp_a = np.exp(a - c)
    sum_exp_a = np.sum(exp_a)
    y = exp_a / sum_exp_a
    return y

class Audio(BaseModel):
    audio: str

@app.get("/healthcheck")
def read_root():
    return "OK"

@app.post("/speech")
def recognize(audio: Audio):
    b = base64.b64decode(audio.audio)
    try:
        data = librosa.util.buf_to_float(b)
    except:
        raise HTTPException(status_code=400, detail="Data length is Invalid.")
    data = fix_audio_length(data)
    data = to_melspectrogram(data, n_mels=n_mels)
    data = data.reshape(1,1,n_mels,n_mels)
    data = torch.from_numpy(data)
    with torch.no_grad():
        data = Variable(data)
    model.eval()
    output = model(data.float())
    amax = torch.argmax(output)
    prob = softmax(output.detach().numpy()[0]).astype(np.float)
    prob = dict(zip(CLASSES, prob))
    return {
        "result": CLASSES[amax],
        "prob":prob
    }
