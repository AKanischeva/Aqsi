package com.example.aqsi.utils

import android.content.Context
import com.example.aqsi.BuildConfig
import com.example.aqsi.R
import com.example.aqsi.Status
import com.example.aqsi.db.AppDatabase
import com.example.aqsi.db.orders.OrdersEntity
import com.example.aqsi.db.routeSheet.RouteSheetEntity
import com.google.gson.internal.bind.util.ISO8601Utils
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile
import org.apache.commons.net.ftp.FTPFileFilter
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import ru.aqsi.commons.models.orders.Order
import ru.aqsi.commons.models.orders.OrderContent
import ru.aqsi.commons.models.orders.OrderPosition
import ru.aqsi.commons.models.orders.OrderStatus
import ru.aqsi.commons.rmk.AqsiOrders
import ru.aqsi.commons.rmk.enums.BargainSubject
import ru.aqsi.commons.rmk.enums.ChequeType
import ru.aqsi.commons.rmk.enums.PaymentType
import ru.aqsi.commons.rmk.enums.TaxType
import xdroid.toaster.Toaster.toast
import xdroid.toaster.Toaster.toastLong
import java.io.*
import java.text.ParsePosition
import java.text.SimpleDateFormat
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
//            client.logout()
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
        var routeSheetId = UUID.randomUUID().toString()
        if(!result.isNullOrEmpty()) {
            val file = result.get(0)
            file?.let {
                downloadFile(context, file.name, true) { success ->
                    if (success) {
                        var ordersXml = parseRouteSheet(context, fileName)
                        val ordersEntity = arrayListOf<OrdersEntity>()
                        ordersXml.forEach {
                            downloadFile(context, it, false) { orderSuccess ->
                                if (orderSuccess) {
                                    val order = parseOrder(context)
                                    AqsiOrders.createOrder(context, order)
                                    val orderDb = OrdersEntity(
                                        order.uid, routeSheetId, order.number, order.dateTime, order.status,
                                        order.getPriceAmount(),order.content.customer?:"", order.deliveryAddress ?: ""
                                    )
                                    AppDatabase(context).ordersDao().insert(orderDb)
                                    ordersEntity.add(orderDb)
                                }
                            }
                        }
                        val sdf = SimpleDateFormat("dd.MM.yyyy")
                        val routeSheetDate = sdf.format(Date())
                        AppDatabase(context).routeSheetDao().insert(
                            RouteSheetEntity(
                                routeSheetId, fileName, routeSheetDate, Status.IN_WORK.title,
                                ordersEntity
                            )
                        )
                    } else {
                        toast(context.getString(R.string.route_sheet_download_error))
                    }
                }
            }
        } else {
            toast(context.getString(R.string.route_sheet_download_file_not_found))
        }
    }

    fun downloadFile(context: Context, fileName: String, isRouteSheet: Boolean, callback: (isSuccess: Boolean) -> Unit) {
        try {
            client.setFileType(FTP.BINARY_FILE_TYPE)
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
                toast(context.getString(R.string.route_sheet_download_success))
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
            number = "",
            externalSystem = "aqsi",
            dateTime = ISO8601Utils.format(Date()),
            cashier = null,
            clientId = null,
            deliveryAddress = "",
            pickAddress = "",
            comment = "",
            status = OrderStatus.NOT_PAID.title,
            updatedAt = ISO8601Utils.format(Date()),
            content = OrderContent(
                type = ChequeType.INPUT.id,
                positions = arrayListOf(
                    OrderPosition(
                        id = UUID.randomUUID().toString(),
                        text = "",
                        quantity = 0.0,
                        price = 0.0,
                        tax = TaxType.TAX_20.id,
                        paymentMethodType = PaymentType.FULL_PAYMENT.id,
                        paymentSubjectType = BargainSubject.GOODS.id
                    )
                )
            )
        )
        var bargainSubject = -1
        try {
            while (event  != XmlPullParser.END_DOCUMENT) {
                var tagName = parser.name
                when (event) {
                    XmlPullParser.START_TAG -> {
                        when (tagName) {
                            "order" -> {
                                order.comment = parser.getAttributeValue(null, "comment")
                                order.content = OrderContent(positions = arrayListOf(), type = ChequeType.INPUT.id)
                                val c = order.content
                                c.customerContact = parser.getAttributeValue(null, "phone")
                                c.customer = parser.getAttributeValue(null, "client")
                                order.deliveryAddress = parser.getAttributeValue(null, "address")
                                order.number = parser.getAttributeValue(null, "order_num")
                                val date = parser.getAttributeValue(null, "order_date")
                                val time = parser.getAttributeValue(null, "order_time")
                                val sdf = SimpleDateFormat("dd.MM.yyyy hh:mm:ss")
                                order.dateTime = ISO8601Utils.format(sdf.parse(date + " " + time))
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

                                } else {
                                    var position = OrderPosition(
                                        tax = parser.getAttributeValue(null, "tax").toInt(),
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

    fun parseRouteSheet(context: Context, number: String): ArrayList<String> {
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

    fun formatDate(date: String):String {
        try {
            val parsedDate = ISO8601Utils.parse(date, ParsePosition(0))
            return SimpleDateFormat("dd.MM.yyyy").format(parsedDate)
        } catch (e: Exception) {
            return date
        }
    }

}