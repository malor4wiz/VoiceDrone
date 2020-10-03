#FROM python:3.7
FROM pytorch/pytorch
RUN apt update && apt install -y libsndfile1
RUN pip install fastapi uvicorn torchnet librosa
EXPOSE 80
COPY ./server /workspace
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "80"]
