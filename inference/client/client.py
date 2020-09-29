import base64
import requests
filepath = "./left_1_16k.wav"
with open(filepath, 'rb') as f:
    #print(f.read()[:100])
    audio_encoded = base64.b64encode(f.read())  # read file into RAM and encode it
data = {
    #"audio": str(audio_encoded),  # base64 string
    "audio": audio_encoded.decode('utf-8'),  # base64 string
    #"audio": "test",  # base64 string
}
url = 'http://localhost:80/speech'
r = requests.post(url, json=data)  # note json= here. Headers will be set automatically.
print(r)
print(r.text)
