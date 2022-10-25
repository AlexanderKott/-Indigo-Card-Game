// do not change this data class
data class Box(val height: Int, val length: Int, val width: Int)

fun main() {
    var arr = arrayOf<Int>()
    repeat(4){
        arr+= readLine()!!.toInt()
    }

    val box1 = Box(arr[0], arr[1], arr[3])
    val box2 = Box(arr[0], arr[2], arr[3])

    println(box1)
    println(box2)

}