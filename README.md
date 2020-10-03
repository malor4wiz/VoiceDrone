# VoiceDrone

CPUでの動作を想定しています。
以下の二部に分かれています。

- training: 学習しモデルファイルを保存
- inference: モデルファイルを読み込み、WebAPIを提供

※ファイルサイズが大きい（音声、学習済みモデル合わせて数百MB）ため、cloneに時間がかかる場合があります

## training

`python3 train_speech_commands.py --model=vgg19_bn --optim=sgd --lr-scheduler=plateau --learning-rate=0.01 --lr-scheduler-patience=5 --max-epochs=3 --batch-size=96`

## inference

### server

#### 直接実行
`/server`で`uvicorn main:app --port [PORT]`を実行

#### コンテナ実行
- `inference`に移動
- docker build . -t voice-drone-inference
- sudo docker run -p [PORT]:80  voice-drone-inference

### client
`/client`で`python3 client.py --url http://localhost:[PORT]/speech`を実行
