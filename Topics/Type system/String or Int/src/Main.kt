import java.lang.NumberFormatException

fun isNumber(input: String): Any {
    val result: Int
    try {
        result = Integer.valueOf(input)
    } catch (e: NumberFormatException) {
        return input
    }
    return result

}

