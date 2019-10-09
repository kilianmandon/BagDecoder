import records.*

fun main() {
    println("Hello World")
    val reader = BagReader.fromFile("/home/kilian/Documents/=KiZ4e_2019-09-20-14-22-59.bag")
    println("Version: ${reader.versionLine}")


    val bagHeader = reader.readRecord() as BagHeader
    println("Total chunks to read: ${bagHeader.chunkCount}")

    var count=0
    reader.forMessagesOnTopic("/tf") {

        count++
        if (count%1000==0) println("Count is $count")
    }

}

enum class Operation(val value: Byte) {
    BagHeader(3), Chunk(5), Connection(7),Message(2), IndexData(4), ChunkInfo(6)
}