# VoiceDrone

CPUでの動作を想定しています。
以下の二部に分かれています。

- training: 学習しモデルファイルを保存
- inference: モデルファイルを読み込み、WebAPIを提供

※ファイルサイズが大きい（音声、学習済みモデル合わせて数百MB）ため、cloneに時間がかかる場合があります

## training
- `/training`に移動

環境構築の上、直接実行するかコンテナを実行します

### 直接実行
`python3 train_speech_commands.py --model=vgg19_bn --optim=sgd --lr-scheduler=plateau --learning-rate=0.01 --lr-scheduler-patience=5 --max-epochs=3 --batch-size=96`

### コンテナ実行
- `docker build . -t voice-drone-training`
- `sudo docker run -it --shm-size=256m voice-drone-training`

## inference
- `/inference`に移動

### server
環境構築の上、直接実行するかコンテナを実行します

#### 直接実行
`/inference/server`で`uvicorn main:app --port [PORT]`を実行

#### コンテナ実行
- `docker build . -t voice-drone-inference`
- `sudo docker run -p [PORT]:80  voice-drone-inference`

### client
`/inference/client`で`python3 client.py --url http://localhost:[PORT]/speech`を実行
