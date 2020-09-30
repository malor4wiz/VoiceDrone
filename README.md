# VoiceDrone

CPUでの動作を想定しています。
以下の二部に分かれています。

- training: 学習しモデルファイルを保存
- inference: モデルファイルを読み込み、WebAPIを提供

## training

`python3 train_speech_commands.py --model=vgg19_bn --optim=sgd --lr-scheduler=plateau --learning-rate=0.01 --lr-scheduler-patience=5 --max-epochs=3 --batch-size=96`

## inference

`/server`で`uvicorn main:app --port [PORT]`を実行

`/client`で`python3 client.py --url http://localhost:[PORT]/speech`を実行
