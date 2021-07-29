package com.example.aqsi.utils

import android.content.Context
import xdroid.toaster.Toaster.toast
import xdroid.toaster.Toaster.toastLong
import com.example.aqsi.BuildConfig
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile
import org.apache.commons.net.ftp.FTPFileFilter
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import ru.aqsi.commons.rmk.AqsiOrders
import com.google.gson.internal.bind.util.ISO8601Utils
import ru.aqsi.commons.models.orders.Order
import ru.aqsi.commons.models.orders.OrderContent
import ru.aqsi.commons.models.orders.OrderPosition
import ru.aqsi.commons.models.orders.OrderStatus
import ru.aqsi.commons.rmk.enums.BargainSubject
import ru.aqsi.commons.rmk.enums.ChequeType
import ru.aqsi.commons.rmk.enums.PaymentType
import ru.aqsi.commons.rmk.enums.TaxType
import java.io.*
import java.util.*
import kotlin.collections.ArrayList


object FTPUtils {

    const val DOWNLOAD_PATH = "uploads_test/IN"
    private var client: FTPClient = FTPClient()


    fun init(context: Context): Boolean {
        try {
            client.connect(BuildConfig.HOSTNAME, BuildConfig.PORT)
            client.login(BuildConfig.LOGIN, BuildConfig.PASSWORD)
            client.enterLocalPassiveMode()
            //todo success

//            downloadFile(context)//todo
//            parseXml(context)

//            client.logout()
//
//            client.disconnect()
            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun findRouteSheet(context: Context, fileName: String) {
        val dirToSearch = DOWNLOAD_PATH
        val filter = FTPFileFilter { ftpFile -> ftpFile.isFile && ftpFile.name.contains("-$fileName-") && ftpFile.name.endsWith("-r.xml")}
        val result: Array<FTPFile>? = client.listFiles(dirToSearch, filter)
        val file = result?.get(0)
        file?.let {
            downloadFile(context, file.name, true, {success ->
                if (success) {
                    var orders = parseRouteSheet(context)
                    orders.forEach {
                        downloadFile(context, it, false, {orderSuccess ->
                            if(orderSuccess) {
                                val order = parseOrder(context)
                                AqsiOrders.createOrder(context, order)
                                toast("Dowloaded " + it)
                            }
                        })
                    }
                    var a = 1
                } else {
                    toast("Error, not success")

                }
            })
        } ?: kotlin.run {
            toast("Error, file not found")
        }


        val a = 5
    }

    fun downloadFile(context: Context, fileName: String, isRouteSheet: Boolean, callback: (isSuccess: Boolean) -> Unit) {
        try {
            client.setFileType(FTP.BINARY_FILE_TYPE)

//             APPROACH #1: using retrieveFile(String, OutputStream)
            val remoteFile1 = "$DOWNLOAD_PATH/$fileName"
            val fileName = if(isRouteSheet) {
                "routeSheet.xml"
            } else {
                "order.xml"
            }
            val file = File(context.filesDir, fileName).apply {
                if (exists()) {
                    delete()
                }
                createNewFile()
            }//cacheDir
            val outputStream1: OutputStream = BufferedOutputStream(FileOutputStream(file))
            var success: Boolean = client.retrieveFile(remoteFile1, outputStream1)
            outputStream1.close()
            if (success) {
                toastLong("Route sheet has been downloaded successfully")
                callback.invoke(true)
            } else {
                callback.invoke(false)
            }
        } catch (ex: IOException) {
            toastLong("Error: " + ex.message)
            ex.printStackTrace()
            callback.invoke(false)
        } finally {
//            try {
//                if (client.isConnected()) {
//                    client.logout()
//                    client.disconnect()
//                }
//            } catch (ex: IOException) {
//                ex.printStackTrace()
//            }
        }
    }

    fun parseOrder(context: Context): Order {
        val file = File(context.filesDir, "order.xml")
        val targetStream: InputStream = FileInputStream(file)
        var factory = XmlPullParserFactory.newInstance()
        var parser = factory.newPullParser()
        parser.setInput(targetStream, null)
        var event = parser.eventType
        var editText = ""
        val order = Order(
            uid = UUID.randomUUID().toString(),
            number = "X-19",
            externalSystem = "aqsi",
            dateTime = ISO8601Utils.format(Date()),
            cashier = null,
            clientId = null,
            deliveryAddress = "Ростов на дону",
            pickAddress = "г. Москва",
            comment = "Как можно быстрее!",
            status = OrderStatus.NOT_PAID.title,
            updatedAt = ISO8601Utils.format(Date()),
            content = OrderContent(
                type = ChequeType.INPUT.id,
                positions = arrayListOf(
                    OrderPosition(
                        id = UUID.randomUUID().toString(),
                        text = "Мороженое",
                        quantity = 5.0,
                        price = 101.93,
                        tax = TaxType.TAX_20.id,
                        paymentMethodType = PaymentType.FULL_PAYMENT.id,
                        paymentSubjectType = BargainSubject.GOODS.id
                    )
                )
            )
        )
        try {
            while (event  != XmlPullParser.END_DOCUMENT) {
                var tagName = parser.name
                var bargainSubject = -1
                when (event) {
                    XmlPullParser.START_TAG -> {
                        when (tagName) {
                            "order" -> {
//                                order.uid = parser.getAttributeValue(null, "order_num")
                                order.comment = parser.getAttributeValue(null, "comment")
                                order.content = OrderContent(positions = arrayListOf(), type = ChequeType.INPUT.id)
                                val c = order.content
                                c?.customerContact = parser.getAttributeValue(null, "phone")
                                c?.customer = parser.getAttributeValue(null, "client")
                                c?.settlementAddress = parser.getAttributeValue(null, "address")
                                order.dateTime = ISO8601Utils.format(Date())//todo
                                order.number = parser.getAttributeValue(null, "order_num")
                            }
                            "goods" -> {
                                bargainSubject = BargainSubject.GOODS.id
                            }
                            "services" -> {
                                bargainSubject = BargainSubject.SERVICE.id
                            }
                            "payments" -> {
                                bargainSubject = BargainSubject.PAYMENT.id
                            }
                            "item" -> {
                                if(bargainSubject == BargainSubject.PAYMENT.id) {
                                    //todo
                                } else {
                                    var position = OrderPosition(
                                        tax = TaxType.TAX_20.id,//todo
                                        editable = isEditable(parser.getAttributeValue(null, "edit")),
                                        quantity = parser.getAttributeValue(null, "quantity").toDouble(),
                                        unitOfMeasurement = parser.getAttributeValue(null, "unit"),
                                        price = parser.getAttributeValue(null, "price").toDouble(),
                                        barcodes = arrayOf((parser.getAttributeValue(null, "barcode"))),
                                        id = parser.getAttributeValue(null, "id"),
                                        sku = parser.getAttributeValue(null, "article"),
                                        text = parser.getAttributeValue(null, "name"),
                                        paymentMethodType = 4,
                                        paymentSubjectType = bargainSubject
                                    )
                                    order.content?.positions?.add(position)
                                }
                            }
                        }

                    }
                }
                event = parser.next()
            }

        } catch (e: Exception) {
            toast(e.message)
        }
        return order
    }

    fun isEditable(editable: String): Boolean {
        return editable == "allow"
    }

    fun parseRouteSheet(context: Context): ArrayList<String> {
        val file = File(context.filesDir, "routeSheet.xml")
        val targetStream: InputStream = FileInputStream(file)
        var factory = XmlPullParserFactory.newInstance()
        var parser = factory.newPullParser()
        parser.setInput(targetStream, null)
        var event = parser.eventType
        var orders = ArrayList<String>()
        while (event  != XmlPullParser.END_DOCUMENT) {
            var tagName = parser.name
            when (event) {
                XmlPullParser.END_TAG -> {
                    if(tagName == "file") {
                        orders.add(parser.getAttributeValue(null, "name"))
                    }
                }
            }
            event = parser.next()
        }
        return orders
    }


}