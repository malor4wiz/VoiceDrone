package com.example.voicedrone

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioRecord.OnRecordPositionUpdateListener
import android.media.MediaRecorder
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class RecordPage : AppCompatActivity() {
    private val PERMISSIONS_REQUEST_CODE_RECORD_AUDIO = 1
    private val PERMISSIONS_REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 2

    var audioRecord : AudioRecord? = null
    val SAMPLING_RATE = 16000
    private var bufSize = 0
    private var shortData: ShortArray? = null
    private val wav1 = MyWaveFile()
    var flag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.record_page)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 既に許可されているか確認
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
                // 許可されていなかったらリクエストする
                // ダイアログが表示される
                requestPermissions(
                    arrayOf(
                        Manifest.permission.RECORD_AUDIO
                    ),
                    PERMISSIONS_REQUEST_CODE_RECORD_AUDIO);
                return;
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 既に許可されているか確認
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                // 許可されていなかったらリクエストする
                // ダイアログが表示される
                requestPermissions(
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    PERMISSIONS_REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
                return;
            }
        }
        initAudioRecord()

    }

    fun onClick(v: View?) {
        if (!flag) {
            startAudioRecord()
        } else {
            stopAudioRecord()
        }
        flag = !flag
    }

    fun onClickRequest(v: View?) {
        try {
            UploadHttpRequest().execute(
                "http://35.200.72.132/speech"
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    //AudioRecordの初期化
    private fun initAudioRecord() {
        wav1.createFile("/sdcard/voice_drone/here.wav")
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
                Log.w("onPeriodicNotification", "here")
                Log.w("shortData:", shortData.toString())
                // TODO Auto-generated method stub
                    audioRecord!!.read(shortData!!, 0, bufSize / 2) // 読み込む
                    wav1.addBigEndianData(shortData!!) // ファイルに書き出す
            }

            override fun onMarkerReached(recorder: AudioRecord) {
                // TODO Auto-generated method stub
            }
        })
        // コールバックが呼ばれる間隔を指定
        audioRecord!!.positionNotificationPeriod = bufSize / 2
    }

    private fun startAudioRecord() {
        audioRecord!!.startRecording()
        Log.w("shortData:", shortData.toString())
        audioRecord!!.read(shortData!!, 0, bufSize / 2)
    }

    //オーディオレコードを停止する
    private fun stopAudioRecord() {
        audioRecord!!.stop()
    }

    inner class UploadHttpRequest() : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg params: String): String? {
            val uri = params[0]
            var connection: HttpURLConnection? = null
            val sb = StringBuilder()
            try {
                val stream = File("/sdcard/voice_drone/here.wav").readBytes()

                //streamをbyte配列に変換し, Base64でエンコード
                val encodedJpg: String = Base64.getEncoder().encodeToString(stream)

                //jsonにエンコードした画像データを埋め込む
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
                e.printStackTrace()
            } finally {
                connection?.disconnect()
            }
            return sb.toString()
        }

        override fun onPostExecute(string: String?) {
            Log.w("result:", string)
//            val jsonObject = JSONObject(string)
//            Log.w("result:", jsonObject.get("result").toString())
        }
    }
}


class MyWaveFile {
    private val FILESIZE_SEEK = 4
    private val DATASIZE_SEEK = 40
    private var raf //リアルタイム処理なのでランダムアクセスファイルクラスを使用する
            : RandomAccessFile? = null
    private var recFile //録音後の書き込み、読み込みようファイル
            : File? = null
    private var fileName = "/sdcard/voice_drone/here.wav" //録音ファイルのパス
    private val RIFF = byteArrayOf(
        'R'.toByte(),
        'I'.toByte(),
        'F'.toByte(),
        'F'.toByte()
    ) //wavファイルリフチャンクに書き込むチャンクID用
    private var fileSize = 36
    private val WAVE =
        byteArrayOf('W'.toByte(), 'A'.toByte(), 'V'.toByte(), 'E'.toByte()) //WAV形式でRIFFフォーマットを使用する
    private val fmt =
        byteArrayOf('f'.toByte(), 'm'.toByte(), 't'.toByte(), ' '.toByte()) //fmtチャンク　スペースも必要
    private val fmtSize = 16 //fmtチャンクのバイト数
    private val fmtID = byteArrayOf(1, 0) // フォーマットID リニアPCMの場合01 00 2byte
    private val chCount: Short = 1 //チャネルカウント モノラルなので1 ステレオなら2にする
    private val bytePerSec: Int = 16000 * (fmtSize / 8) * chCount //データ速度
    private val blockSize =
        (fmtSize / 8 * chCount).toShort() //ブロックサイズ (Byte/サンプリングレート * チャンネル数)
    private val bitPerSample: Short = 16 //サンプルあたりのビット数 WAVでは8bitか16ビットが選べる
    private val data =
        byteArrayOf('d'.toByte(), 'a'.toByte(), 't'.toByte(), 'a'.toByte()) //dataチャンク
    private var dataSize = 0 //波形データのバイト数
    fun createFile(fName: String) {
        fileName = fName
        //	ファイルを作成
        recFile = File(fileName)
        if (recFile!!.exists()) {
            recFile!!.delete()
        }
        try {
            recFile!!.createNewFile()
        } catch (e: IOException) {
            //	TODO	Auto-generated	catch	block
            e.printStackTrace()
        }
        try {
            raf = RandomAccessFile(recFile, "rw")
        } catch (e: FileNotFoundException) {
            //	TODO	Auto-generated	catch	block
            e.printStackTrace()
        }

        //wavのヘッダを書き込み
        try {
            raf!!.seek(0)
            raf!!.write(RIFF)
            raf!!.write(littleEndianInteger(fileSize))
            raf!!.write(WAVE)
            raf!!.write(fmt)
            raf!!.write(littleEndianInteger(fmtSize))
            raf!!.write(fmtID)
            raf!!.write(littleEndianShort(chCount))
            raf!!.write(littleEndianInteger(16000)) //サンプリング周波数
            raf!!.write(littleEndianInteger(bytePerSec))
            raf!!.write(littleEndianShort(blockSize))
            raf!!.write(littleEndianShort(bitPerSample))
            raf!!.write(data)
            raf!!.write(littleEndianInteger(dataSize))
        } catch (e: IOException) {
            //	TODO	Auto-generated	catch	block
            e.printStackTrace()
        }
    }

    private fun littleEndianInteger(i: Int): ByteArray {
        val buffer = ByteArray(4)
        buffer[0] = i.toByte()
        buffer[1] = (i shr 8).toByte()
        buffer[2] = (i shr 16).toByte()
        buffer[3] = (i shr 24).toByte()
        Log.w("bufferInteger:", buffer.toString())
        return buffer
    }

    // PCMデータを追記するメソッド
    fun addBigEndianData(shortData: ShortArray) {

        // ファイルにデータを追記
        try {
            raf!!.seek(raf!!.length())
            raf!!.write(littleEndianShorts(shortData))
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }

        // ファイルサイズを更新
        updateFileSize()

        // データサイズを更新
        updateDataSize()
    }

    // ファイルサイズを更新
    private fun updateFileSize() {
        fileSize = (recFile!!.length() - 8).toInt()
        val fileSizeBytes = littleEndianInteger(fileSize)
        try {
            raf!!.seek(FILESIZE_SEEK.toLong())
            raf!!.write(fileSizeBytes)
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }

    // データサイズを更新
    private fun updateDataSize() {
        dataSize = (recFile!!.length() - 44).toInt()
        val dataSizeBytes = littleEndianInteger(dataSize)
        try {
            raf!!.seek(DATASIZE_SEEK.toLong())
            raf!!.write(dataSizeBytes)
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }

    // short型変数をリトルエンディアンのbyte配列に変更
    private fun littleEndianShort(s: Short): ByteArray {
        val buffer = ByteArray(2)
        buffer[0] = s.toByte()
        buffer[1] = (s.toInt() shr 8).toByte()
        Log.w("bufferShort:", buffer.toString())
        return buffer
    }

    // short型配列をリトルエンディアンのbyte配列に変更
    private fun littleEndianShorts(s: ShortArray): ByteArray {
        val buffer = ByteArray(s.size * 2)
        var i: Int
        i = 0
        while (i < s.size) {
            buffer[2 * i] = s[i].toByte()
            buffer[2 * i + 1] = (s[i].toInt() shr 8).toByte()
            i++
        }
        Log.w("bufferShorts:", buffer.toString())
        return buffer
    }

    // ファイルを閉じる
    fun close() {
        try {
            raf!!.close()
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }
}