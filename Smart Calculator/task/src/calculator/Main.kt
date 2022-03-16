package calculator

import java.math.BigInteger
import java.util.*


enum class TokenTypesEnum { NUMBER, OPERATOR, VARIABLE }

val numRegex = "([+-]?\\d+)"//.toRegex()
val operatorRegex = "([+]+|[-]+|[*/^()=])"//.toRegex()
val variableRegex = "([+-]?[A-Za-z]+)"//.toRegex()

//val variable = "[A-Za-z]+"
val num = "(($numRegex)|($variableRegex))"
val regex = "$num(\\s+$operatorRegex\\s+$num)*"//.toRegex()
val varRegex = "$variableRegex\\s*=\\s*($numRegex|$variableRegex)\\s*"//.toRegex()
val precedances = mapOf(
    "+" to 1,
    "-" to 1,
    "*" to 2,
    "/" to 2,
    "^" to 3,
    "(" to 4,
    ")" to 4

)

class Token(value: String, val tokenType: TokenTypesEnum) {
    var value = value.trim()
}
//class OperatorToken(val operator: Char) : Token()
//class IntOperanToken(val values: Int) : Token()
//class DoubleOperanToken(val values: Double) : Token()

class Expression(val expression: String) {
    //init {
//    println(expression)
//}
    var isNormal: Boolean? = null
    val tokens = if (expression.isEmpty()) mutableListOf() else parse()


    fun parse(): MutableList<Token> {

        val parsedToken = mutableListOf<Token>()
        val regx = "$numRegex|$variableRegex|$operatorRegex".toRegex()
        var rx2 = "[+-](\\d+|[A-Za-z]+)".toRegex()
        regx.findAll(expression).map { it.value }.forEach { parsedToken.add(createToken(it)) }

        val reviewIdxs = mutableListOf<Int>()
        parsedToken.forEachIndexed { index, token ->
            if (index > 0 && token.value.matches(rx2)) reviewIdxs.add(index)
        }
//        println(reviewIdxs)
        val opRegex = "[+-]".toRegex()
        for (i in reviewIdxs.lastIndex downTo 0) {
            val index = reviewIdxs[i]

            if (parsedToken[index - 1].tokenType != TokenTypesEnum.OPERATOR || parsedToken[index - 1].value == ")") {
                val newToken = createToken(opRegex.find(parsedToken[index].value)!!.value)

                parsedToken[index].value = parsedToken[index].value.replace(opRegex, "")
                parsedToken.add(index, newToken)
            }
        }

        isNormal = parsedToken.filter { it.value == "=" }.isEmpty()
        if (!validateExpression(parsedToken)) {
            throw Exception("Invalid expression")
        }
//println("isNormal:$isNormal")
//        if (!regex.toRegex().matches(expression)) {
//            if (!varRegex.toRegex().matches(expression)) {
//                throw Exception("Invalid expression")
//            } else {
//                isNormal = false
//            }
//        } else {
//            isNormal = true
//        }
//        return mutableListOf<Token>().also { it.addAll(splitExpression()) }
        return parsedToken
    }

    private fun validateExpression(parsedToken: MutableList<Token>): Boolean {
        for (i in 0 until parsedToken.lastIndex) {
            if (parsedToken[i].tokenType == TokenTypesEnum.NUMBER ||
                parsedToken[i].tokenType == TokenTypesEnum.VARIABLE
            ) {
                if (parsedToken[i + 1].tokenType == TokenTypesEnum.NUMBER ||
                    parsedToken[i + 1].tokenType == TokenTypesEnum.VARIABLE
                ) {
                    return false
                }
            }

            if (parsedToken[i].tokenType == TokenTypesEnum.OPERATOR &&
                parsedToken[i].value !in listOf("(", ")")
            ) {
                if (parsedToken[i + 1].tokenType == TokenTypesEnum.OPERATOR &&
                    parsedToken[i + 1].value !in listOf("(", ")")
                ) {

                    return false
                }
            }
        }
        if (!isNormal!!) {
            if (!parsedToken[0].value.matches("[A-Za-z]+".toRegex())) {
                return false
            }
        }

        return true
    }

//    private fun splitExpression(): List<Token> {
//        return if (isNormal == true)
//            expression.split(" ").map { createToken(it) }
//        else if (isNormal == false)
//            expression.split("=").map { createToken(it) }
//        else
//            emptyList()
//    }

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
    val variables = mutableMapOf<String, BigInteger>()

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

    fun evaluate(ex: Expression): BigInteger {
        val numStk = Stack<BigInteger>()
        val opStk = Stack<String>()

//        var result = evaluateToken(ex.tokens[0]) //.value.toBigInteger()
        for (i in 0..ex.tokens.lastIndex) {
            if (ex.tokens[i].tokenType == TokenTypesEnum.OPERATOR) {
                ex.tokens[i].value = reduceOperator(ex.tokens[i].value)
                if (opStk.isEmpty()) {
                    opStk.push(ex.tokens[i].value)
                } else {
                    if (ex.tokens[i].value == "(") {
                        opStk.push(ex.tokens[i].value)
                    } else if (ex.tokens[i].value == ")") {
                        processPran(numStk, opStk)
                    } else if (opStk.peek() != "(" && compareOperators(ex.tokens[i].value, opStk.peek()) <= 0) {

                        processOperation(numStk, opStk)
                        opStk.push(ex.tokens[i].value)

                    } else {
                        opStk.push(ex.tokens[i].value)
                    }
                }
            } else if (ex.tokens[i].tokenType == TokenTypesEnum.NUMBER) {
                numStk.push(ex.tokens[i].value.toBigInteger())
            } else if (ex.tokens[i].tokenType == TokenTypesEnum.VARIABLE) {
                numStk.push(evaluateToken(ex.tokens[i]))
            }

//            val num2 = evaluateToken(ex.tokens[i + 1])//.value.toBigInteger()


        }
        while (opStk.isNotEmpty()) {
            processOperation(numStk, opStk)
        }
        return numStk.pop()

    }

    private fun processPran(numStk: Stack<BigInteger>, opStk: Stack<String>) {
        if (opStk.isEmpty()) throw Exception("Invalid expression")
        var op = opStk.pop()!!
        while (op != "(") {
            val num2 = numStk.pop()!!
            val num1 = numStk.pop()!!

            numStk.push(doMath(num1, op, num2))
            if (opStk.isEmpty()) throw Exception("Invalid expression")
            op = opStk.pop()!!
        }
    }

    private fun processOperation(numStk: Stack<BigInteger>, opStk: Stack<String>) {
        val num2 = numStk.pop()!!
        val num1 = numStk.pop()!!
        if (opStk.isEmpty()) throw Exception("Invalid expression")

        val op = opStk.pop()!!
        if (op == "(") throw Exception("Invalid expression")

        numStk.push(doMath(num1, op, num2))
    }

    private fun doMath(num1: BigInteger, op: String, num2: BigInteger): BigInteger {

        val result: BigInteger
        when (op) {
            "+" -> {
                result = num1 + num2
            }
            "-" -> {
                result = num1 - num2
            }
            "*" -> {
                result = num1 * num2
            }
            "/" -> {
                result = num1 / num2
            }
            "^" -> {
                result = num1.pow(num2.toInt())
            }
            else -> throw Exception("Unknown Operator '$op'")

        }
//        println("$num1 $op $num2 = $result")
        return result
    }


    private fun compareOperators(op1: String, op2: String): Int {
        return precedances[op1]!! - precedances[op2]!!
    }

    private fun evaluateToken(token: Token): BigInteger {
        return if (token.tokenType == TokenTypesEnum.NUMBER) {
            token.value.toBigInteger()
        } else if (token.tokenType == TokenTypesEnum.VARIABLE) {
            val match = "[-+]".toRegex().find(token.value)
            val variuableName = "[A-Za-z]+".toRegex().find(token.value)!!.value

            if (variables.containsKey(variuableName)) {
                if (match == null || match.value == "+") {
                    variables[variuableName]!!
                } else {
                    variables[variuableName]!!.unaryMinus()
                }
            } else throw Exception("Unknown variable")

        } else {
            throw Exception("Something wrong")
        }
    }

    fun updateVarMap(ex: Expression) {
        val assign = assign(ex)
        if (assign.second.toBigIntegerOrNull() == null) {
            if (variables.containsKey(assign.second)) {
                variables.put(assign.first, variables[assign.second]!!)
            } else {
                throw Exception("Invalid variable")
            }
        } else {
            variables.put(assign.first, assign.second.toBigInteger())
        }

    }

    fun assign(ex: Expression): Pair<String, String> {
        return ex.tokens[0].value to evaluate(Expression(ex.tokens.subList(2, ex.tokens.size).map { it.value }
            .joinToString(" "))).toString()
    }
}

fun main() {
    var expression = readln().trim()
    while (expression != "/exit") {
        if (expression.isNotBlank()) {
            if (expression == "/help") {
                println("The program calculates the sum of numbers")
            } else if (expression[0] == '/') {
                println("Unknown command")
            } else {
                try {
                    val ex = Calculator.parse(expression)
//                    println(ex.tokens.map { "'${it.value}':${it.tokenType}" })
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
