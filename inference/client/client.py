import base64
import requests
import json
import sys
import argparse

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='開発用サンプルクライアント')
    parser.add_argument('--url', default='http://localhost:8000/speech')
    args = parser.parse_args()  # 引数を解析
    
    filepath = "./left_1_16k.wav"
    with open(filepath, 'rb') as f:
        audio_encoded = base64.b64encode(f.read())  # read file into RAM and encode it
    data = {
        "audio": audio_encoded.decode('utf-8'),  # base64 string
    }
    url = args.url
    r = requests.post(url, json=data)  # note json= here. Headers will be set automatically.

    data = r.json()
    print ('======= Raw JSON =======')
    print (json.dumps(data, indent=4))
    print ('========================')
    print('status code: ', r.status_code)
    print('left score: ', data['prob']['left'])
    print('right score: ', data['prob']['right'])
    print ('========================')