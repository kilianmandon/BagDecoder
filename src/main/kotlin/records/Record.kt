package records

import RecordData

interface Record {

    val type: String

    companion object {
        fun decodeRecord(data: RecordData) : Record {
            return when (data.op) {
                Operation.Connection.value -> Connection(data)
                Operation.BagHeader.value -> BagHeader(data)
                Operation.Chunk.value -> Chunk(data)
                Operation.ChunkInfo.value -> ChunkInfo(data)
                Operation.IndexData.value -> IndexData(data)
                Operation.Message.value -> MessageData(data)
                else -> throw IllegalStateException("Operation values should be exhaustive by Connection, BagHeader, Chunk, ChunkInfo, IndexData, Message.")
            }
        }
    }
}