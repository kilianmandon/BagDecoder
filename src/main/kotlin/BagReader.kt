import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

class BagReader(val stream: InputStream) {

    fun readRecord() = Record.readRecordFromStream(stream)

    companion object {
        fun fromFile(fileName: String): BagReader {
            return BagReader(Files.newInputStream(Paths.get(fileName)))
        }
    }
}

data class Record(val headerLength: Int, val headerData: HeaderData, val dataLength: Int, val data: RecordData) {
    companion object {
        fun readRecordFromStream(stream: InputStream) : Record {
            val length = stream.readInt()
            val headerData = HeaderData.readHeaderData(stream.readAmount(length))
            val dataLength = stream.readInt()
            val dataBytes = stream.readAmount(dataLength)
            val recordData = RecordData(dataBytes)
            return Record(length, headerData, dataLength, recordData)
        }
    }
}

data class HeaderData(val fields: Map<String, ByteArray>) {
    companion object {
        fun readHeaderData(data: ByteArray) : HeaderData {
            var offset = 0
            val buffer = ByteBuffer.wrap(data)
            val fields  = mutableMapOf<String, ByteArray>()
            while (offset<data.size) {
                val len = buffer.int
                val pureData = buffer.readAmount(len)
                val splitIndex = pureData.indexOf('='.toByte())
                val name = pureData.sliceArray(0 until splitIndex).toString(Charset.defaultCharset())
                val headerData = pureData.sliceArray(splitIndex until len)
                fields[name] = headerData
            }
            return HeaderData(fields)

        }
    }
}

data class RecordData(val data: ByteArray)

fun InputStream.readAmount(byteCount: Int) : ByteArray {
    val myBytes = ByteArray(byteCount)
    var read = 0
    while (read<byteCount) {
        read += this.read(myBytes, read, byteCount - read)
    }
    return myBytes
}
fun ByteBuffer.readAmount(byteCount: Int) : ByteArray {
    val myBytes = ByteArray(byteCount)
    this.get(myBytes)
    return myBytes
}
fun InputStream.readInt() = ByteBuffer.wrap(this.readAmount(4)).order(ByteOrder.LITTLE_ENDIAN).int
