package calculator

enum class TokenTypesEnum { NUMBER, OPERATOR, VARIABLE }

val numRegex = "([+-]?\\d+)"//.toRegex()
val operatorRegex = "([+]+|[-]+)"//.toRegex()
val variableRegex = "([A-Za-z]+)"//.toRegex()
//val variable = "[A-Za-z]+"
val num = "(($numRegex)|($variableRegex))"
val regex = "$num(\\s+$operatorRegex\\s+$num)*"//.toRegex()
val varRegex = "$variableRegex\\s*=\\s*($numRegex|$variableRegex)\\s*"//.toRegex()

class Token(value: String, val tokenType: TokenTypesEnum) {
    val value = value.trim()
}
//class OperatorToken(val operator: Char) : Token()
//class IntOperanToken(val values: Int) : Token()
//class DoubleOperanToken(val values: Double) : Token()

class Expression(val expression: String) {

    var isNormal: Boolean? = null
    val tokens = if (expression.isEmpty()) mutableListOf() else parse()

    fun parse(): MutableList<Token> {

        if (!regex.toRegex().matches(expression)) {
            if (!varRegex.toRegex().matches(expression)) {
                throw Exception("Invalid expression")
            } else {
                isNormal = false
            }
        } else {
            isNormal = true
        }
        return mutableListOf<Token>().also { it.addAll(splitExpression()) }
    }

    private fun splitExpression(): List<Token> {
        return if (isNormal == true)
            expression.split(" ").map { createToken(it) }
        else if (isNormal == false)
            expression.split("=").map { createToken(it) }
        else
            emptyList()
    }

    private fun createToken(it: String): Token {

        return if (it.matches(numRegex.toRegex())) {
            Token(it, TokenTypesEnum.NUMBER)
        } else if (it.matches(operatorRegex.toRegex())) {
            Token(it, TokenTypesEnum.OPERATOR)
        } else {
            Token(it, TokenTypesEnum.VARIABLE)
        }
    }


}

object Calculator {
    val variables = mutableMapOf<String, Int>()

    fun parse(expression: String): Expression {
        return Expression(expression.replace("\\s+".toRegex(), " "))
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

    fun evaluate(ex: Expression): Int {
        var result = evaluateToken(ex.tokens[0]) //.value.toInt()
        for (i in 1..ex.tokens.lastIndex step 2) {
            val op = reduceOperator(ex.tokens[i].value)
            val num2 = evaluateToken(ex.tokens[i + 1])//.value.toInt()

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

    private fun evaluateToken(token: Token): Int {
        return if (token.tokenType == TokenTypesEnum.NUMBER) {
            token.value.toInt()
        } else if (token.tokenType == TokenTypesEnum.VARIABLE) {
           if(variables.containsKey(token.value)) {
               variables[token.value]!!
           }
            else throw Exception("Unknown variable")
        } else {
            throw Exception("Something wrong")
        }
    }

    fun updateVarMap(ex: Expression) {
        val assign = assign(ex)
        if (assign.second.toIntOrNull() == null) {
            if (variables.containsKey(assign.second)) {
                variables.put(assign.first, variables[assign.second]!!)
            } else {
                throw Exception("Invalid variable")
            }
        } else {
            variables.put(assign.first, assign.second.toInt())
        }

    }

    fun assign(ex: Expression): Pair<String, String> {
        return ex.tokens[0].value to ex.tokens[1].value
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
                        if (ex.isNormal == true) {
                            println(Calculator.evaluate(ex))
                        } else if (ex.isNormal == false) {
                            Calculator.updateVarMap(ex)
                        }
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


