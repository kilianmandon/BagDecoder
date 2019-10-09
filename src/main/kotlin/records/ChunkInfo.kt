package records

import RecordData

class ChunkInfo(recordData: RecordData) : Record {
    override val type: String
        get() = "ChunkInfo"

    /**
     * chunk info record version
     */
    val ver = recordData.fieldAsInt("ver")

    /**
     * offset of the chunk record
     */
    val chunkPos = recordData.fieldAsLong("chunk_pos")

    /**
     * timestamp of earliest message in the chunk
     */
    val startTime = recordData.fieldAsLong("start_time")

    /**
     * timestamp of latest message in the chunk
     */
    val endTime = recordData.fieldAsLong("end_time")

    /**
     * number of connections in the chunk
     */
    val count = recordData.fieldAsInt("count")

    /**
     * TODO: Decode that data
     */
    val data = recordData.data

}