package com.example.voicedrone.pages

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioRecord.OnRecordPositionUpdateListener
import android.media.MediaRecorder
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.voicedrone.MyWaveFile
import com.example.voicedrone.R
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class RecordPage : AppCompatActivity() {
    private val PERMISSIONS_REQUEST_CODE = 0

    var audioRecord : AudioRecord? = null
    private val SAMPLING_RATE = 16000
    private var bufSize = 0
    private var shortData: ShortArray? = null
    private val wav1 = MyWaveFile()
    private var fileName: String = ""
    private var recordFlag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.record_page)

        // 権限チェック
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSIONS_REQUEST_CODE);
        } else {
            prepareAudioRecord()
        }

    }

    override fun onResume() {
        super.onResume()

    }

    // requestPermissions の後の処理
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            prepareAudioRecord()
        } else {
            Toast.makeText(applicationContext, "permissionが許可されていません", Toast.LENGTH_SHORT).show()
        }
    }

    private fun prepareAudioRecord() {
        try {
            // アプリ固有の外部ストレージを使用
            val dir = File(
                applicationContext.getExternalFilesDir(
                    Environment.DIRECTORY_MUSIC)?.path
            )
            // フォルダ作成
            if (!dir?.mkdirs()) {
                Log.e("log", "Directory is not created")
            }
            if (dir != null) {
                fileName = dir.absolutePath + "/indication.wav"
                Log.d("log", "path: $fileName")
                initAudioRecord()
            }
        } catch (e: Exception) {
            Log.i("RecordPage", e.toString())
            Toast.makeText(applicationContext, "音声ファイルの作成に失敗しました", Toast.LENGTH_SHORT).show()
        }

    }

    fun onClickRecordButton(v: View?) {
        if (!recordFlag) {
            val recordButton = findViewById<Button>(R.id.RecordButton)
            recordButton.text = "STOP"
            startAudioRecord()
        } else {
            stopAudioRecord()
            val recordButton = findViewById<Button>(R.id.RecordButton)
            recordButton.text = "RECORD"
        }
        recordFlag = !recordFlag

    }

    //AudioRecordの初期化
    private fun initAudioRecord() {
        wav1.createFile(fileName)
        // AudioRecordオブジェクトを作成
        bufSize = AudioRecord.getMinBufferSize(
            SAMPLING_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLING_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufSize
        )
        shortData = ShortArray(bufSize / 2)

        // コールバックを指定
        audioRecord!!.setRecordPositionUpdateListener(object : OnRecordPositionUpdateListener {
            // フレームごとの処理
            override fun onPeriodicNotification(recorder: AudioRecord) {
                audioRecord!!.read(shortData!!, 0, bufSize / 2) // 読み込む
                wav1.addBigEndianData(shortData!!) // ファイルに書き出す
            }

            override fun onMarkerReached(recorder: AudioRecord) {}
        })
        // コールバックが呼ばれる間隔を指定
        audioRecord!!.positionNotificationPeriod = bufSize / 2
    }

    private fun startAudioRecord() {
        try{
            audioRecord!!.startRecording()
            audioRecord!!.read(shortData!!, 0, bufSize / 2)
        } catch (e: Exception) {
            Log.i("RecordPage", e.toString())
            Toast.makeText(applicationContext, "録音開始時にエラーが発生しました", Toast.LENGTH_SHORT).show()
        }

    }

    //オーディオレコードを停止する
    private fun stopAudioRecord() {
        audioRecord!!.stop()
        audioRecord!!.release()
        wav1.close()

        try {
            UploadHttpRequest().execute(
                "http://35.200.72.132/speech"
            )
        } catch (e: IOException) {
            Log.i("RecordPage", e.toString())
            Toast.makeText(applicationContext, "録音終了時にエラーが発生しました", Toast.LENGTH_SHORT).show()
        }
    }

    inner class UploadHttpRequest() : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg params: String): String? {
            val uri = params[0]
            var connection: HttpURLConnection? = null
            val sb = StringBuilder()
            try {
                val stream = File(fileName).readBytes()

                // streamをBase64でエンコード
                val encodedJpg: String = Base64.getEncoder().encodeToString(stream)

                // jsonにエンコードした画像データを埋め込む
                val json: String = String.format("{ \"audio\":\"%s\" } ", encodedJpg)

                val url = URL(uri)
                connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 3000 //接続タイムアウトを設定する。
                connection.readTimeout = 50000 //レスポンスデータ読み取りタイムアウトを設定する。
                connection.requestMethod = "POST" //HTTPのメソッドをPOSTに設定する。

                //ヘッダーを設定する
                connection.setRequestProperty("User-Agent", "Android")
                connection.setRequestProperty("Accept", "application/json")
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doInput = true //リクエストのボディ送信を許可する
                connection.doOutput = true //レスポンスのボディ受信を許可する
                connection.useCaches = false //キャッシュを使用しない
                connection.instanceFollowRedirects = false
                connection.connect()

                // データを投げる
                val out: OutputStream = connection.outputStream
                val autoFlush = false
                val encoding = "UTF-8"
                val ps = PrintStream(out, autoFlush, encoding)
                ps.print(json)
                ps.close()

                // データを受け取る
                val `is`: InputStream = connection.inputStream
                val reader = BufferedReader(InputStreamReader(`is`, "UTF-8"))
                var line: String? = ""
                while (reader.readLine().also{line = it} != null) sb.append(line)
                `is`.close()

            } catch (e: IOException) {
                Log.i("RecordPage", e.toString())
                Toast.makeText(applicationContext, "音声データの送信に失敗しました", Toast.LENGTH_SHORT).show()
            } finally {
                connection?.disconnect()
            }
            return sb.toString()
        }

        override fun onPostExecute(string: String?) {
            try {
                val jsonObject = JSONObject(string)

                val intent = Intent(application, RecordResultPage::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                val result = jsonObject.get("result").toString()
                val rightRecognitionRate = jsonObject.getJSONObject("prob").get("right").toString()
                val leftRecognitionRate = jsonObject.getJSONObject("prob").get("left").toString()

                intent.putExtra("result", result)
                intent.putExtra("rightRecognitionRate", rightRecognitionRate)
                intent.putExtra("leftRecognitionRate", leftRecognitionRate)

                startActivity(intent)
            } catch (Je : JSONException) {
                Log.i("RecordPage", Je.toString())
                Toast.makeText(applicationContext, "音声データの解析結果取得に失敗しました", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

