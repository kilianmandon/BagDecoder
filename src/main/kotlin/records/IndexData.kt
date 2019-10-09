package records

import RecordData
import java.nio.ByteBuffer
import java.nio.ByteOrder

class IndexData(recordData: RecordData) : Record {
    override val type: String
        get() = "IndexData"

    /**
     * Index data record version
     */
    val ver = recordData.fieldAsInt("ver")

    /**
     * connection ID.
     */
    val conn = recordData.fieldAsInt("conn")

    /**
     * Number of messages on conn in the preceding chunk.
     */
    val count = recordData.fieldAsInt("count")

    /**
     * TODO: Decode the index data to times and offsets
     */
    val data by lazy {
        val list = mutableListOf<StampedOffset>()
        val buffer = ByteBuffer.wrap(recordData.data).order(ByteOrder.LITTLE_ENDIAN)
        while (buffer.hasRemaining()) {
            list.add(StampedOffset(buffer.long, buffer.int))
        }
        return@lazy list
    }

    data class StampedOffset(val timeStamp: Long, val offset: Int)



}