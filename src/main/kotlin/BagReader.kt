import records.*
import java.io.InputStream
import java.io.InterruptedIOException
import java.lang.IllegalStateException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

class BagReader(val stream: InputStream) {

    val versionLine = String(stream.readUntil('\n'.toByte()))

    fun readRecord() = RecordData.readRecordFromStream(stream)

    companion object {
        fun fromFile(fileName: String): BagReader {
            return BagReader(Files.newInputStream(Paths.get(fileName)))
        }
    }

    fun forMessagesOnTopicDeprecated(topic: String, callback: (MessageData)->Unit) {
        val topicToID = mutableMapOf<String, Int>()
        val idToTopic = mutableMapOf<Int, String>()


        var chunksRead = 0

        while (stream.available()>0) {
            val record = readRecord()
            if (record is Chunk) {
                chunksRead++
                if (chunksRead%1000==0) println("Chunks read: $chunksRead")
                record.subRecords.forEach {
                    if (it is Connection) {
                        topicToID[it.topic] = it.conn
                        idToTopic[it.conn] = it.topic
                    } else if (it is MessageData) {
                        if (idToTopic[it.conn] == topic) callback(it)
                    }
                }
            }
        }
    }

    fun forMessagesOnTopic(topic: String, callback: (MessageData) -> Unit) {
        val topicToID = mutableMapOf<String, Int>()
        val idToTopic = mutableMapOf<Int, String>()
        lateinit var lastChunk: Chunk


        while (stream.available()>0) {
            val record = readRecord()
            if (record is Chunk) {
                lastChunk=record

                if (!topicToID.containsKey(topic))
                record.subRecords.forEach {
                    if (it is Connection) {
                        topicToID[it.topic] = it.conn
                        idToTopic[it.conn] = it.topic
                    }
                }
            } else if (record is IndexData) {
                if (idToTopic[record.conn]==topic) {
                    record.data.forEach {
                        val msg = lastChunk.recordAtIndex(it.offset) as? MessageData ?: throw IllegalStateException("Message at ${it.offset} in $lastChunk is not a message.")
                        callback(msg)
                    }
                }
            }
        }
    }

}

private fun InputStream.readUntil(toByte: Byte): ByteArray {
    val bytes = mutableListOf<Byte>()
    loop@do {
        val next = this.read()

        if (next==-1) throw InterruptedIOException("Could not read the next byte from stream")
        bytes.add(next.toByte())
        if (next==toByte.toInt()) break@loop

    } while (true)
    return bytes.toByteArray()
}

data class RecordData(val headerLength: Int, val headerData: HeaderData, val dataLength: Int, val data: ByteArray) {

    val op: Byte
        get() = headerData.fields["op"]?.get(0) as Byte
    companion object {
        fun readRecordFromStream(stream: InputStream) : Record {

            val length = stream.readInt()


            val headerData = HeaderData.readHeaderData(stream.readNBytes(length))

            val dataLength = stream.readInt()

            val dataBytes = stream.readNBytes(dataLength)
            return Record.decodeRecord(RecordData(length, headerData, dataLength, dataBytes))
        }

    }

    fun getField(field: String) : ByteArray {
        return headerData.fields[field] ?: throw NoSuchElementException("Field $field not set in $this")
    }

    fun fieldAsString(field: String) = String(getField(field))

    fun fieldAsLong(field: String) = ByteBuffer.wrap(getField(field)).order(ByteOrder.LITTLE_ENDIAN).long

    fun fieldAsInt(field: String) = ByteBuffer.wrap(getField(field)).order(ByteOrder.LITTLE_ENDIAN).int


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RecordData

        if (headerLength != other.headerLength) return false
        if (headerData != other.headerData) return false
        if (dataLength != other.dataLength) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = headerLength
        result = 31 * result + headerData.hashCode()
        result = 31 * result + dataLength
        result = 31 * result + data.contentHashCode()
        return result
    }
}

data class HeaderData(val fields: Map<String, ByteArray>) {
    companion object {
        fun readHeaderData(data: ByteArray) : HeaderData {
            val buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)
            val fields  = mutableMapOf<String, ByteArray>()
            while (buffer.hasRemaining()) {
                val len = buffer.int

                val pureData = buffer.readAmount(len)
                val splitIndex = pureData.indexOf('='.toByte())
                val name = pureData.sliceArray(0 until splitIndex).toString(Charset.defaultCharset())
                val headerData = pureData.sliceArray(splitIndex+1 until len)
                fields[name] = headerData
            }
            return HeaderData(fields)

        }
    }
}


@Deprecated(message="Deprecated, use the corresponding InputStream method instead", replaceWith = ReplaceWith("readNBytes(byteCount)"))
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
fun InputStream.readInt() = ByteBuffer.wrap(this.readNBytes(4)).order(ByteOrder.LITTLE_ENDIAN).int
