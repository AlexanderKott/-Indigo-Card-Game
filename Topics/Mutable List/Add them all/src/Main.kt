fun solution(first: List<Int>, second: List<Int>): MutableList<Int> {
    val x = first.toMutableList()
        x.addAll(second)
    return x
}