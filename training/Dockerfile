#FROM python:3.7
FROM pytorch/pytorch
RUN apt update && apt install -y libsndfile1
RUN pip install torchnet librosa
COPY . /workspace
CMD ["python3", "train_speech_commands.py", "--model=vgg19_bn", "--optim=sgd", "--lr-scheduler=plateau", "--learning-rate=0.01", "--lr-scheduler-patience=5", "--max-epochs=3", "--batch-size=96"]
