package indigo

data class TableInfo(val cardsCount: Int, val topCard: String)

class IndigoGame {
    companion object {
        const val HUMAN = 1
        const val COMPUTER = 2
        const val EXIT_COMMAND = "exit"
    }

    enum class CardsWinner { NO_ONE, HUMAN, COMPUTER }

    private var cardsDeck = mutableListOf<String>()
    private val cardsOnTable = mutableListOf<String>()
    private val cardsHumanHand = mutableListOf<String>()
    private val cardsComputerHand = mutableListOf<String>()

    private var humanScores = 0
    private var compScores = 0
    private val humanPocket = mutableListOf<String>()
    private val compPocket = mutableListOf<String>()

    private var whoCardsWinner = CardsWinner.NO_ONE


    private var firstPlayerGoesFirst = true
    private var compWasFirst = false

    private val gameUI = GameUI()
    private val ai = IndigoGameAI()


    private fun setSecondPlayerGoesFirst() {
        firstPlayerGoesFirst = false
        compWasFirst = true
    }

    private fun whoGoesNext(): Int {
        firstPlayerGoesFirst = !firstPlayerGoesFirst
        return if (!firstPlayerGoesFirst) HUMAN else COMPUTER
    }

    private fun placeCardsToTable(): String {
        cardsOnTable.addAll(getCards(4))
        return cardsOnTable.joinToString(" ")
    }

    private fun tableCardsInform(): TableInfo {
        return if (cardsOnTable.isNotEmpty())
            TableInfo(cardsOnTable.size, cardsOnTable.last())
        else TableInfo(-1, "-")
    }

    fun resetCards() {
        val suits = listOf('♦', '♥', '♠', '♣')
        val ranks = listOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")

        cardsDeck.clear()
        for (suit in suits) {
            for (rank in ranks) {
                cardsDeck.add("$rank$suit")
            }
        }
    }

    fun shuffleCards() = cardsDeck.shuffle()

    private fun getCards(cardsAmount: Int): List<String> {
        val cardsSet = cardsDeck.subList(0, cardsAmount)
        cardsDeck = cardsDeck.subList(cardsAmount, cardsDeck.size)
        return cardsSet
    }

    private fun allCardsAreRunOut() = cardsDeck.size + cardsHumanHand.size + cardsComputerHand.size == 0
    private fun cardsHumanUserHas() = cardsHumanHand.size

    fun dealCardsToPlayers() {
        cardsHumanHand.addAll(getCards(6))
        cardsComputerHand.addAll(getCards(6))
    }

    private fun whatIsInHumanPlayerHand(): String {
        val result = buildString {
            cardsHumanHand.forEachIndexed { i, str ->
                append("${i + 1})$str ")
            }
        }
        return result
    }


    private fun playerCardBeatsTableTopCard(userCard: String): Boolean {
        if (cardsOnTable.isEmpty()) return false
        val tableTopCard = cardsOnTable.last()
        return if (userCard.last() == tableTopCard.last()) {
            true
        } else userCard.dropLast(1) == tableTopCard.dropLast(1)

    }

    private fun calcScores(playerID: Int, cardsOnTable: MutableList<String>, chosenElement: String = "void") {
        val scoredCards = listOf("A", "10", "J", "Q", "K")
        val cards = mutableListOf<String>()
        cards.addAll(cardsOnTable)
        if (chosenElement != "void") {
            cards.add(chosenElement)
        }
        for (card in cards) {
            for (scard in scoredCards) {
                if (card.contains(scard)) {
                    if (playerID == HUMAN) {
                        humanScores++
                    } else {
                        compScores++
                    }
                }
            }
        }
    }

    private fun add3Scores(player: Int) {
        if (player == HUMAN) {
            humanScores += 3
        } else {
            compScores += 3
        }
    }

    private fun getScoresWinner(): Int {
        return when {
            humanPocket.size == compPocket.size -> {
                if (compWasFirst) 2 else 1
            }
            humanPocket.size < compPocket.size -> {
                2
            }
            humanPocket.size > compPocket.size -> {
                1
            }
            else -> throw java.lang.ArithmeticException()
        }
    }

    private fun giveCardsToLastWinner() {
        when {
            whoCardsWinner == CardsWinner.NO_ONE -> {
                if (compWasFirst) {
                    compPocket.addAll(cardsOnTable)
                } else {
                    humanPocket.addAll(cardsOnTable)
                }
            }
            whoCardsWinner == CardsWinner.HUMAN -> {
                humanPocket.addAll(cardsOnTable)
            }
            whoCardsWinner == CardsWinner.COMPUTER -> {
                compPocket.addAll(cardsOnTable)
            }
        }
    }

    inner class GameUI {

        fun printPlayFirstAsk(action: () -> Unit) {
            while (true) {
                println("Play first?")
                val userAnswer = readLine()!!.lowercase()
                if (userAnswer == "no") {
                    action()
                }
                if (userAnswer == "no" || userAnswer == "yes") break
            }
        }

        fun printInitialCardsOnTheTable(cards: String) {
            println("Initial cards on the table: $cards\n")
        }

        fun print(info: TableInfo) {
            if (info.cardsCount != -1)
                println("${info.cardsCount} cards on the table, and the top card is ${info.topCard} ")
            else println("\nNo cards on the table")
        }

        fun print(hand: String) {
            println("Cards in hand: $hand")
        }

        fun printComputerTurn(turn: String) {
            println(cardsComputerHand.joinToString(" ")) //todo remove line
            println("Computer plays $turn\n")
        }

        fun print(count: Int) {
            println("Choose a card to play (1-${count}): ")
        }

        fun printTurnStats() {
            println("Score: Player $humanScores - Computer $compScores")
            println("Cards: Player ${humanPocket.size} - Computer ${compPocket.size}")
        }

        fun printPlayerWinsCards() {
            println("Player wins cards")
        }

        fun printCompWinsCards() {
            println("Computer wins cards")
        }

    }

    inner class IndigoGameAI {

        fun getNextCard(topmost: String): String {

            val sameSuits = cardsComputerHand.groupBy { it.takeLast(1) }
            val sameRank = cardsComputerHand.groupBy { it.dropLast(1) }

            val maxCardsWithSameSuit = sameSuits.maxByOrNull { a -> a.value.count() }
            val maxSuitCardsSet = maxCardsWithSameSuit?.value ?: listOf()

            val maxCardsWithSameRank = sameRank.maxByOrNull { a -> a.value.count() }
            val maxRankCardsSet = maxCardsWithSameRank?.value ?: listOf()

            if (topmost == "") {
                if (maxSuitCardsSet.size > 1) {
                    return maxSuitCardsSet.random()
                }

                if (maxRankCardsSet.size > 1) {
                    return maxRankCardsSet.random()
                }

            } else {
                val suitCandidatsMap = sameSuits.filter { it.key == topmost.last().toString() }
                val rankCandidatesMap = sameRank.filter { it.key == topmost.dropLast(1) }

                val suitCandidates = suitCandidatsMap[topmost.last().toString()] ?: listOf()
                val rankCandidates = rankCandidatesMap[topmost.dropLast(1)] ?: listOf()

                if (suitCandidates.size >= 2) {
                    return suitCandidates.random()
                } else if (rankCandidates.size >= 2) {
                    return rankCandidates.random()
                } else {
                    val allCandidats = suitCandidates.toMutableList()
                    allCandidats.addAll(rankCandidates)

                    if (allCandidats.isNotEmpty()) {
                        return allCandidats.random()
                    } else {
                       if  (maxSuitCardsSet.isNotEmpty()) return maxSuitCardsSet.random()
                       if  (maxRankCardsSet.isNotEmpty()) return maxRankCardsSet.random()

                    } //Computer played card 7♣ instead one of [10♦, 10♠].

                }
            }
            return cardsComputerHand.random()
        }
    }


    fun gamePlay() {
        gameUI.printPlayFirstAsk(::setSecondPlayerGoesFirst)
        gameUI.printInitialCardsOnTheTable(placeCardsToTable())

        var userTurn: String = ""

        gameLoop@ while (true) {
            when (whoGoesNext()) {
                HUMAN -> {
                    gameUI.print(tableCardsInform())

                    if (allCardsAreRunOut()) {
                        val whoWiner = getScoresWinner()
                        add3Scores(whoWiner)
                        calcScores(whoWiner, cardsOnTable)
                        break@gameLoop
                    }

                    if (cardsHumanHand.isEmpty() && cardsDeck.isNotEmpty()) {
                        cardsHumanHand.addAll(getCards(6))
                    }

                    gameUI.print(whatIsInHumanPlayerHand())

                    while (true) {
                        gameUI.print(cardsHumanUserHas())
                        try {
                            userTurn = readLine()!!.lowercase()
                            if (userTurn == EXIT_COMMAND) {
                                break@gameLoop
                            }
                            if ((userTurn.toInt()) in 1..cardsHumanUserHas()) {
                                val chosenElement = cardsHumanHand[userTurn.toInt() - 1]

                                if (playerCardBeatsTableTopCard(chosenElement)) {
                                    whoCardsWinner = CardsWinner.HUMAN
                                    gameUI.printPlayerWinsCards()
                                    humanPocket.add(chosenElement)
                                    humanPocket.addAll(cardsOnTable)
                                    calcScores(HUMAN, cardsOnTable, chosenElement)
                                    cardsOnTable.clear()
                                    gameUI.printTurnStats()
                                } else {
                                    cardsOnTable.add(chosenElement)
                                }
                                cardsHumanHand.remove(chosenElement)
                                break
                            }

                        } catch (_: NumberFormatException) {
                        }
                    }
                }

                COMPUTER -> {
                    gameUI.print(tableCardsInform())
                    if (allCardsAreRunOut()) {
                        val whoWiner = getScoresWinner()
                        add3Scores(whoWiner)
                        calcScores(whoWiner, cardsOnTable)
                        break@gameLoop
                    }

                    if (cardsComputerHand.isEmpty() && cardsDeck.isNotEmpty()) {
                        cardsComputerHand.addAll(getCards(6))
                    }

                    val chosenElementByComp = ai.getNextCard(cardsOnTable.lastOrNull() ?: "") //  AI

                    gameUI.printComputerTurn(chosenElementByComp)

                    if (playerCardBeatsTableTopCard(chosenElementByComp)) {
                        gameUI.printCompWinsCards()
                        whoCardsWinner = CardsWinner.COMPUTER
                        compPocket.add(chosenElementByComp)
                        compPocket.addAll(cardsOnTable)
                        calcScores(COMPUTER, cardsOnTable, chosenElementByComp)
                        cardsOnTable.clear()
                        gameUI.printTurnStats()
                    } else {
                        cardsOnTable.add(chosenElementByComp)
                    }
                    cardsComputerHand.remove(chosenElementByComp)
                }
            }
        }

        if (userTurn == EXIT_COMMAND) return
        giveCardsToLastWinner()
        gameUI.printTurnStats()

    }
}


fun main() {
    println("Indigo Card Game")
    val game = IndigoGame()
    game.resetCards()
    game.shuffleCards()
    game.dealCardsToPlayers()
    game.gamePlay()
    println("Game Over")
}