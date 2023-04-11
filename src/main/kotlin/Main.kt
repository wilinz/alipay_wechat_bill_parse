import com.github.doyaaaaaken.kotlincsv.dsl.context.ExcessFieldsRowBehaviour
import com.github.doyaaaaaken.kotlincsv.dsl.context.InsufficientFieldsRowBehaviour
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.io.File
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class BillData(
    var datetime: LocalDateTime,
    var category: String,
    var transactionPartner: String,
    var partnerAccount: String = "",
    var name: String,
    var type: String,
    var amount: BigDecimal,
    var paymentMethod: String,
    var status: String,
    var transactionNo: String,
    var shopNo: String,
    var comment: String
)

fun main() {
    alipay()
    wechat()
}

fun alipay() {
    val file =
        File("")

    val bills = mutableListOf<BillData>()
    csvReader {
        insufficientFieldsRowBehaviour = InsufficientFieldsRowBehaviour.EMPTY_STRING
        excessFieldsRowBehaviour = ExcessFieldsRowBehaviour.TRIM
        charset = "GBK"
    }.open(file) {
        var startIndex = -1
        readAllAsSequence(13).forEachIndexed { index, row0 ->
            val row = row0.map { it.trim() }
            if (row[0].contains("电子客户回单")) {
                startIndex = index + 2
            }
            if (startIndex != -1 && index >= startIndex) {
                val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val datetime = LocalDateTime.from(dateTimeFormatter.parse(row[0]))
                val type = when (row[5]) {
                    "收入" -> "in"
                    "支出" -> "out"
                    else -> "other"
                }
                if (type == "other") return@forEachIndexed
                val amount = row[6].toBigDecimal()
                val comment = if (row[11] == "/") "" else row[11]
                val name = if (row[4] == "/") "未命名" else row[4]
                bills.add(
                    BillData(
                        datetime = datetime,
                        category = row[1],
                        transactionPartner = row[2],
                        partnerAccount = row[3],
                        name = name,
                        type = type,
                        amount = amount,
                        paymentMethod = row[7],
                        status = row[8],
                        transactionNo = row[9],
                        shopNo = row[10],
                        comment = comment
                    )
                )
            }
        }
        bills.forEach {
            println(it)
        }
    }
}

fun wechat() {
    val file =
        File("")

    val bills = mutableListOf<BillData>()
    csvReader {
        insufficientFieldsRowBehaviour = InsufficientFieldsRowBehaviour.EMPTY_STRING
        excessFieldsRowBehaviour = ExcessFieldsRowBehaviour.TRIM
    }.open(file) {
        var startIndex = -1
        readAllAsSequence(11).forEachIndexed { index, row0 ->
            val row = row0.map { it.trim() }
            if (row[0].contains("微信支付账单明细列表")) {
                startIndex = index + 2
            }
            if (startIndex != -1 && index >= startIndex) {
                val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val datetime = LocalDateTime.from(dateTimeFormatter.parse(row[0]))
                val type = when (row[4]) {
                    "收入" -> "in"
                    "支出" -> "out"
                    else -> "other"
                }
                if (type == "other") return@forEachIndexed
                val amount = row[5].substringUnicode(1).toBigDecimal()
                val comment = if (row[10] == "/") "" else row[10]
                val name = if (row[3] == "/") "未命名" else row[3]
                bills.add(
                    BillData(
                        datetime = datetime,
                        category = row[1],
                        transactionPartner = row[2],
                        name = name,
                        type = type,
                        amount = amount,
                        paymentMethod = row[6],
                        status = row[7],
                        transactionNo = row[8],
                        shopNo = row[9],
                        comment = comment
                    )
                )
            }
        }
        bills.forEach {
            println(it)
        }
    }
}

/**
 * 返回从给定代码点索引处开始的新字符串。
 * @param startCodePoint 开始代码点的索引
 * @return 新的子字符串
 */
fun String.substringUnicode(startCodePoint: Int): String {
    val startIndex = this.offsetByCodePoints(0, startCodePoint)
    return this.substring(startIndex)
}

/**
 * 返回一个新字符串，该字符串从给定的开始代码点索引处开始，到给定的结束代码点索引处结束（不包括结束码点）。
 * @param startCodePoint 开始代码点的索引
 * @param endCodePoint 结束代码点的索引（不包括）
 * @return 新的子字符串
 */
fun String.substringUnicode(startCodePoint: Int, endCodePoint: Int): String {
    val startIndex = this.offsetByCodePoints(0, startCodePoint)
    val endIndex = this.offsetByCodePoints(startIndex, endCodePoint - startCodePoint)
    return this.substring(startIndex, endIndex)
}