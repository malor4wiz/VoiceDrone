package com.example.voicedrone

import android.util.Log
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.RandomAccessFile

class MyWaveFile {
    private val FILESIZE_SEEK = 4
    private val DATASIZE_SEEK = 40
    private var raf: RandomAccessFile? = null //リアルタイム処理なのでランダムアクセスファイルクラスを使用する
    private var recFile: File? = null //録音後の書き込み、読み込みようファイル
    private val RIFF =
        byteArrayOf('R'.toByte(), 'I'.toByte(), 'F'.toByte(), 'F'.toByte()) //wavファイルリフチャンクに書き込むチャンクID用
    private var fileSize = 36
    private val WAVE =
        byteArrayOf('W'.toByte(), 'A'.toByte(), 'V'.toByte(), 'E'.toByte()) //WAV形式でRIFFフォーマットを使用する
    private val fmt =
        byteArrayOf('f'.toByte(), 'm'.toByte(), 't'.toByte(), ' '.toByte()) //fmtチャンク　スペースも必要
    private val fmtSize = 16 //fmtチャンクのバイト数
    private val fmtID = byteArrayOf(1, 0) // フォーマットID リニアPCMの場合01 00 2byte
    private val chCount: Short = 1 //チャネルカウント モノラルなので1 ステレオなら2にする
    private val bytePerSec: Int = 16000 * (fmtSize / 8) * chCount //データ速度
    private val blockSize = (fmtSize / 8 * chCount).toShort() //ブロックサイズ (Byte/サンプリングレート * チャンネル数)
    private val bitPerSample: Short = 16 //サンプルあたりのビット数 WAVでは8bitか16ビットが選べる
    private val data =
        byteArrayOf('d'.toByte(), 'a'.toByte(), 't'.toByte(), 'a'.toByte()) //dataチャンク
    private var dataSize = 0 //波形データのバイト数
    fun createFile(fileName: String) {
        //	ファイルを作成
        recFile = File(fileName)
        if (recFile!!.exists()) {
            recFile!!.delete()
        }
        try {
            recFile!!.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        try {
            raf = RandomAccessFile(recFile, "rw")
        } catch (e: FileNotFoundException) {
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
            e.printStackTrace()
        }
    }

    private fun littleEndianInteger(i: Int): ByteArray {
        val buffer = ByteArray(4)
        buffer[0] = i.toByte()
        buffer[1] = (i shr 8).toByte()
        buffer[2] = (i shr 16).toByte()
        buffer[3] = (i shr 24).toByte()
        Log.i("MyWaveFile", "bufferInteger: $buffer")
        return buffer
    }

    // PCMデータを追記するメソッド
    fun addBigEndianData(shortData: ShortArray) {

        // ファイルにデータを追記
        try {
            raf!!.seek(raf!!.length())
            raf!!.write(littleEndianShorts(shortData))
        } catch (e: IOException) {
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
            e.printStackTrace()
        }
    }

    // short型変数をリトルエンディアンのbyte配列に変更
    private fun littleEndianShort(s: Short): ByteArray {
        val buffer = ByteArray(2)
        buffer[0] = s.toByte()
        buffer[1] = (s.toInt() shr 8).toByte()
        Log.i("MyWaveFile", "bufferShort: $buffer")
        return buffer
    }

    // short型配列をリトルエンディアンのbyte配列に変更
    private fun littleEndianShorts(s: ShortArray): ByteArray {
        val buffer = ByteArray(s.size * 2)
        var i = 0
        while (i < s.size) {
            buffer[2 * i] = s[i].toByte()
            buffer[2 * i + 1] = (s[i].toInt() shr 8).toByte()
            i++
        }
        Log.i("MyWaveFile", "bufferShort: $buffer")
        return buffer
    }

    // ファイルを閉じる
    fun close() {
        try {
            raf!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}