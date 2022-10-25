// You can experiment here, it won’t be checked

fun main() {

    val k = 2
    for (i in 1..5){
        for (j in 1..5) {
            if ((i + j) % k == 1) {
                print("*")
            } else {
                print("o")

            }
        }
    println()
    }
}