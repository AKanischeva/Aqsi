package com.example.aqsi.utils

import android.content.Context
import android.os.Environment
import com.example.aqsi.BuildConfig
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile
import org.apache.commons.net.ftp.FTPFileFilter
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.*


object FTPUtils {

    private var client: FTPClient = FTPClient()

    fun init(context: Context): Boolean {
        try {
            client.connect(BuildConfig.HOSTNAME, BuildConfig.PORT)
            client.login(BuildConfig.LOGIN, BuildConfig.PASSWORD)
            client.enterLocalPassiveMode()
            val dirToSearch = "uploads_test/IN"

//            val filter = FTPFileFilter { ftpFile -> ftpFile.isFile && ftpFile.name.contains("Java") }
            val filter = FTPFileFilter { ftpFile -> ftpFile.isFile && ftpFile.name.endsWith("-r.xml")}

            val result: Array<FTPFile> = client.listFiles(dirToSearch, filter)

            if (result != null && result.size > 0) {
                println("SEARCH RESULT:")
                for (aFile in result) {
                    println(aFile.name)
                }
            }

//            downloadFile(context)//todo
            parseXml(context)

            client.logout()

            client.disconnect()
            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun findRouteSheet(fileName: String = "1999406313") {//todo
        val dirToSearch = "uploads_test/IN"
        val filter = FTPFileFilter { ftpFile -> ftpFile.isFile && ftpFile.name.contains(fileName) && ftpFile.name.endsWith("-r.xml")}
        val result: Array<FTPFile> = client.listFiles(dirToSearch, filter)

        val a = 5
    }

    fun downloadFile(context: Context) {
        try {
            client.setFileType(FTP.BINARY_FILE_TYPE)

//             APPROACH #1: using retrieveFile(String, OutputStream)
            val remoteFile1 = "uploads_test/IN/1999409287-66-r.xml"
            val file = File(context.filesDir, "1.xml").apply {
                if (exists()) {
                    delete()
                }
                createNewFile()
            }//cacheDir
            val outputStream1: OutputStream = BufferedOutputStream(FileOutputStream(file))
            var success: Boolean = client.retrieveFile(remoteFile1, outputStream1)
            outputStream1.close()
            if (success) {
                println("File #1 has been downloaded successfully.")
            }

//            // APPROACH #2: using InputStream retrieveFileStream(String)
//            val remoteFile2 = "uploads_test/IN/1999409290-23846-r.xml"
//            val file = File(context.filesDir, "1.xml")//cacheDir
//            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()
//            val outputStream2: OutputStream = BufferedOutputStream(FileOutputStream(file))
//            val inputStream: InputStream = client.retrieveFileStream(remoteFile2)
//            val bytesArray = ByteArray(4096)
//            var bytesRead = -1
//            while (inputStream.read(bytesArray).also { bytesRead = it } != -1) {
//                outputStream2.write(bytesArray, 0, bytesRead)
//            }
//            val success = client.completePendingCommand()
//            if (success) {
//                println("File #2 has been downloaded successfully.")
//            }
//            outputStream2.close()
//            inputStream.close()
        } catch (ex: IOException) {
            println("Error: " + ex.message)
            ex.printStackTrace()
        } finally {
            try {
                if (client.isConnected()) {
                    client.logout()
                    client.disconnect()
                }
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
        }
    }

    fun parseXml(context: Context) {
        val file = File(context.filesDir, "1.xml")
        val targetStream: InputStream = FileInputStream(file)
        var factory = XmlPullParserFactory.newInstance()
        var parser = factory.newPullParser()
        parser.setInput(targetStream, null)
        var event = parser.eventType
        var editText = ""
        while (event  != XmlPullParser.END_DOCUMENT) {
            var tagName = parser.name
            when (event) {
                XmlPullParser.END_TAG -> {
                    if(tagName == "file") {
                        var name = parser.getAttributeValue(null, "name")
                        var a = 1
//                        var name = "\n" + parser.getAttributeValue(0) + " " + parser.getAttributeValue(1)
//                        editText.plus(name)
                    }
                }
            }
            event = parser.next()
        }
    }
}