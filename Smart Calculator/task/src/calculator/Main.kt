package calculator

class Token(val value: String) {

}
//class OperatorToken(val operator: Char) : Token()
//class IntOperanToken(val values: Int) : Token()
//class DoubleOperanToken(val values: Double) : Token()

class Expression(val expression: String) {

    val tokens = if (expression.isEmpty()) mutableListOf() else parse()

    fun parse(): MutableList<Token> {
        val regex = "[+-]?\\d+(\\s+[+-]+\\s+[+-]?\\d+)*".toRegex()
        if (!regex.matches(expression)) {
            throw Exception("Invalid expression")
        }
        return mutableListOf<Token>().also { it.addAll(expression.split(" ").map { Token(it) }) }
    }

    fun evaluate(): Int {
//        if (tokens.size == 1) {
//            return tokens[0].value.toInt()
//        }
        var result = tokens[0].value.toInt()
        for (i in 1..tokens.lastIndex step 2) {
            val op = reduceOperator(tokens[i].value)
            val num2 = tokens[i + 1].value.toInt()

            when (op) {
                "+" -> {
                    result += num2
                }
                "-" -> {
                    result -= num2
                }

            }
        }
        return result
    }

    private fun reduceOperator(value: String): String {
        var op = value
        while (op.length > 1) {
            op = op.replace("--", "+")
            op = op.replace("++", "+")
            op = op.replace("+-", "-")


        }
        return op
    }
}

object Calculator {
    fun parse(expression: String): Expression {
        return Expression(expression.replace("\\s+".toRegex(), " "))
    }
}

fun main() {
    var expression = readln().trim().lowercase()
    while (expression != "/exit") {
        if (expression.isNotBlank()) {
             if (expression == "/help") {
                println("The program calculates the sum of numbers")
            } else if (expression[0] == '/') {
                println("Unknown command")
            } else {
                try {
                    val ex = Calculator.parse(expression)
                    if (ex.tokens.isNotEmpty()) {
                        println(ex.evaluate())
                    }
                } catch (ex: Exception) {
                    println(ex.message)
                }
            }
        }
        expression = readln()
    }
    println("Bye!")
}
