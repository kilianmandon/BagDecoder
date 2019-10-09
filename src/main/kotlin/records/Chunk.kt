package records

import RecordData

class Chunk(private val recordData: RecordData) : Record {
    override val type: String
        get() = "Chunk"

    /**
     * Compression type of the data. Supported compression values are none and bz2.
     */
    val compression = recordData.fieldAsString("compression")

    /**
     * Size in bytes of the uncompressed chunk.
     */
    val size = recordData.fieldAsInt("size")

    val subRecords by lazy {
        val list = mutableListOf<Record>()
        val stream = recordData.data.inputStream()
        while (stream.available()>0) {
            list.add(RecordData.readRecordFromStream(stream))
        }
        return@lazy list.toTypedArray()
    }

    fun recordAtIndex(index: Int) : Record {
        return RecordData.readRecordFromStream(recordData.data.inputStream(index, recordData.data.size-index))
    }

}