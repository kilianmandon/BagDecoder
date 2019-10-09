package records

import RecordData

/**
 * The bag header record is padded out by filling data with ASCII space characters (0x20) so that additional information can be added after the bag file is recorded.
 * Currently, this padding is such that the header is 4096 bytes long.
 */
class BagHeader(recordData: RecordData) : Record {
    override val type: String
        get() = "BagHeader"

    /**
     * Offset of first record after the chunk section.
     */
    val indexPos = recordData.fieldAsLong("index_pos")

    /**
     * Number of unique connections in the file.
     */
    val connCount = recordData.fieldAsInt("conn_count")

    /**
     * Number of chunk records in the file
     */
    val chunkCount = recordData.fieldAsInt("chunk_count")
}