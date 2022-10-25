fun solution(strings: MutableList<String>, str: String): MutableList<String> {
val result = mutableListOf<String>()

    for (i in strings){
         result.add(i.replace(str, "Banana"))
    }

    return result
}