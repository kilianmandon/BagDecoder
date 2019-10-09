package records

import RecordData

class MessageData(recordData: RecordData) : Record {
    override val type: String
        get() = "MessageData"

    /**
     * ID for connection on which message arrived.
     */
    val conn = recordData.fieldAsInt("conn")

    /**
     * Time at which the message was received.
     */
    val time = recordData.fieldAsLong("time")

    /**
     * Serialized in the ROS serialization format.
     * TODO: Look up the ROS serialization format and check how to decode it.
     */
    val data = recordData.data
}