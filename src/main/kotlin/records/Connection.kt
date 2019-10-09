package records

import RecordData

class Connection(recordData: RecordData) : Record {
    override val type: String
        get() = "Connection"

    /**
     * Unique connection ID
     */
    val conn = recordData.fieldAsInt("conn")

    /**
     * Topic on which the messages are stored.
     */
    val topic = recordData.fieldAsString("topic")

    /**
     * TODO: Decode the connection header from this data
     */
    val data = String(recordData.data)
}