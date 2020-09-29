# VoiceDrone

以下の二部に分かれています。

- training: 学習しモデルファイルを保存
- inference: モデルファイルを読み込み、WebAPIを提供

## training

## inference

`/server`で`uvicorn main:app --port 80`を実行

`/client`で`python client.py`を実行
