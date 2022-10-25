fun names(namesList: List<String>): List<String> {
    var count = 0
    val nameCount = mutableListOf<String>()

    val mapp = namesList.groupBy { it }.map { it.key to it.value.count() }

    for (name in mapp){
        nameCount.add("The name ${name.first} is repeated ${name.second} times")
    }

    return nameCount
}