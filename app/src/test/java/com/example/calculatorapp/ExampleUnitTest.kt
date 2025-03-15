package com.example.calculatorapp
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mozilla.javascript.Context
import java.util.Scanner
import kotlin.math.ceil
import kotlin.random.Random

/**
 * Number State Machine
 * controls the states of number building
 */
sealed class NumberState {
    object Start : NumberState()
    object Number : NumberState()
    object Decimal : NumberState()
    object NumberAfterDecimal : NumberState()
    object End : NumberState()
}

/**
 * Exponent State Machine
 * controls the states of exponent building
 */
sealed class ExponentState {
    object Start : ExponentState()
    object Number : ExponentState()
    object NumberAfterUnary : ExponentState()
    object BinaryOperator : ExponentState()
    object UnaryOperator : ExponentState()
    object OpenParenthesis : ExponentState()
    object CloseParenthesis : ExponentState()
    object OpenBracket : ExponentState()
    object CloseBracket : ExponentState()
    object Cleanup : ExponentState()
    object End : ExponentState()
}

/**
 * Expression State Machine
 * controls the states of expression building
 */
sealed class ExpressionState {
    object Start : ExpressionState()
    object Number : ExpressionState()
    object NumberAfterUnary : ExpressionState()
    object BinaryOperator : ExpressionState()
    object UnaryOperator : ExpressionState()
    object ExponentOperator : ExpressionState()
    object OpenParenthesis : ExpressionState()
    object CloseParenthesis : ExpressionState()
    object OpenBracket : ExpressionState()
    object CloseBracket : ExpressionState()
    object Cleanup : ExpressionState()
    object End : ExpressionState()
}
/**
 * Expression Builder State Machines
 * controls the flow of expression generation
 */
class ExpressionBuilderStateMachine {
    // Initial states
    private var currentNumberState: NumberState = NumberState.Start
    private var currentExpressionState: ExpressionState = ExpressionState.Start
    private var currentExponentState: ExponentState = ExponentState.Start

    // Dictionaries of precedence and mappings
    private val precedence = mapOf(
        '+' to 1, '-' to 1,
        '*' to 2, '/' to 2,
        '^' to 3,
        's' to 4, 'c' to 4, 't' to 4, 'o' to 4,
        'l' to 5, 'g' to 5, 'q' to 5,
        'n' to 6,
    )
    private val specialOperators = mapOf(
        's' to "sin", 'c' to "cos",
        't' to "tan", 'o' to "cot",
        'l' to "ln", 'g' to "log",
        'q' to "sqrt",
    )
    // List of operators
    private val numbers = listOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
    private val unaryOperators = listOf("sin", "cos", "tan", "cot", "ln", "log", "sqrt", "-")
    private val binaryOperators = listOf('+', '-', '*', '/', '^')
    private val expOperators = listOf('+', '-', '*', '/')
    private val decimal = '.'
    private val badCharacters = listOf('x', 'c', 'e', '%', '!', '$', '_', '|', '&', '<', '>')

    var expression = ""
    var number = ""
    var exponent = ""
    val builderCycles = 100

    private var parenthesisCount = 0
    private var bracketCount = 0
    private var groupingStack = ArrayDeque<Char>()

    private var expParenthesisCount = 0
    private var expBracketCount = 0
    private var expGroupingStack = ArrayDeque<Char>()

    /** Expression Transition Map
        requires a manual transition to cleanup to end builder
    **/
    private val expressionTransitions: Map<ExpressionState, List<ExpressionState>> = mapOf(
        ExpressionState.Start to listOf(ExpressionState.Number, ExpressionState.UnaryOperator,
            ExpressionState.OpenParenthesis, ExpressionState.OpenBracket),
        ExpressionState.Number to listOf(ExpressionState.BinaryOperator, ExpressionState.CloseParenthesis,
            ExpressionState.CloseBracket),
        ExpressionState.NumberAfterUnary to listOf(ExpressionState.CloseParenthesis),
        ExpressionState.BinaryOperator to listOf(ExpressionState.Number, ExpressionState.UnaryOperator,
            ExpressionState.OpenParenthesis, ExpressionState.OpenBracket),
        ExpressionState.UnaryOperator to listOf(ExpressionState.NumberAfterUnary),
        ExpressionState.ExponentOperator to listOf(ExpressionState.BinaryOperator),
        ExpressionState.OpenParenthesis to listOf(ExpressionState.Number, ExpressionState.UnaryOperator),
        ExpressionState.CloseParenthesis to listOf(ExpressionState.BinaryOperator, ExpressionState.CloseParenthesis,
            ExpressionState.CloseBracket),
        ExpressionState.OpenBracket to listOf(ExpressionState.Number, ExpressionState.UnaryOperator),
        ExpressionState.CloseBracket to listOf(ExpressionState.BinaryOperator, ExpressionState.CloseParenthesis,
            ExpressionState.CloseBracket),
        ExpressionState.Cleanup to listOf(ExpressionState.End), // manual transition required
        ExpressionState.End to emptyList() // No transitions from End
    )

    /** Exponent Transition Map
    requires a manual transition to cleanup to end builder
     **/
    private val exponentTransitions: Map<ExponentState, List<ExponentState>> = mapOf(
        ExponentState.Start to listOf(ExponentState.Number, ExponentState.UnaryOperator,
            ExponentState.OpenParenthesis, ExponentState.OpenBracket),
        ExponentState.Number to listOf(ExponentState.BinaryOperator, ExponentState.CloseParenthesis,
            ExponentState.CloseBracket, ExponentState.Cleanup),
        ExponentState.NumberAfterUnary to listOf(ExponentState.CloseParenthesis),
        ExponentState.BinaryOperator to listOf(ExponentState.Number, ExponentState.UnaryOperator,
            ExponentState.OpenParenthesis, ExponentState.OpenBracket),
        ExponentState.UnaryOperator to listOf(ExponentState.NumberAfterUnary),
        ExponentState.OpenParenthesis to listOf(ExponentState.Number, ExponentState.UnaryOperator),
        ExponentState.CloseParenthesis to listOf(ExponentState.BinaryOperator, ExponentState.CloseParenthesis,
            ExponentState.CloseBracket, ExponentState.Cleanup),
        ExponentState.OpenBracket to listOf(ExponentState.Number, ExponentState.UnaryOperator),
        ExponentState.CloseBracket to listOf(ExponentState.BinaryOperator, ExponentState.CloseParenthesis,
            ExponentState.CloseBracket, ExponentState.Cleanup),
        ExponentState.Cleanup to listOf(ExponentState.End), // manual transition required
        ExponentState.End to emptyList() // No transitions from End
    )

    /** Number Transition Map
    requires a manual transition to cleanup to end builder
     **/
    private val numberTransitions: Map<NumberState, List<NumberState>> = mapOf(
        NumberState.Start to listOf(NumberState.Number, NumberState.Decimal),
        NumberState.Number to listOf(NumberState.Number, NumberState.Decimal, NumberState.End),
        NumberState.Decimal to listOf(NumberState.NumberAfterDecimal),
        NumberState.NumberAfterDecimal to listOf(NumberState.NumberAfterDecimal, NumberState.End),
        NumberState.End to emptyList() // No transitions from End
    )

    fun getCurrentNumberState(): NumberState {
        return currentNumberState
    }

    fun getCurrentExpressionState(): ExpressionState {
        return currentExpressionState
    }

    fun getCurrentExponentState(): ExponentState {
        return currentExponentState
    }

    fun setCurrentExpressionCleanup() {
         currentExpressionState = ExpressionState.Cleanup
    }

    fun transitionNumberState() {
        val possibleNextNumberStates = numberTransitions[currentNumberState] ?: throw IllegalStateException("Invalid state: $currentNumberState")
        if (possibleNextNumberStates.isEmpty()) {
            currentNumberState = NumberState.End
        } else {
            currentNumberState = possibleNextNumberStates[Random.nextInt(possibleNextNumberStates.size)]
        }
    }

    fun transitionExpressionState() {
        val possibleNextExpressionStates = expressionTransitions[currentExpressionState] ?: throw IllegalStateException("Invalid state: $currentExpressionState")
        if (possibleNextExpressionStates.isEmpty()) {
            currentExpressionState = ExpressionState.End
        } else {
            currentExpressionState = possibleNextExpressionStates[Random.nextInt(possibleNextExpressionStates.size)]
        }
    }

    fun transitionExponentState() {
        val possibleNextExponentStates = exponentTransitions[currentExponentState] ?: throw IllegalStateException("Invalid state: $currentExponentState")
        if (possibleNextExponentStates.isEmpty()) {
            currentExponentState = ExponentState.End
        } else {
            currentExponentState = possibleNextExponentStates[Random.nextInt(possibleNextExponentStates.size)]
        }
    }

    fun processNumberState() {
        // Cases for states
        when (currentNumberState) {
            NumberState.Start -> {
                //println("Number Start State")
                transitionNumberState()
            }
            NumberState.Number -> {
                val randomNumber = numbers[Random.nextInt(numbers.size)]
                expression += randomNumber
                number += randomNumber
                transitionNumberState()
            }
            NumberState.Decimal -> {
                expression += decimal
                number += decimal
                transitionNumberState()
            }
            NumberState.NumberAfterDecimal -> {
                val randomNumber = numbers[Random.nextInt(numbers.size)]
                expression += randomNumber
                number += randomNumber
                transitionNumberState()
            }
            NumberState.End -> {}
        }
    }

    fun processExpressionState() {
        //println("This is the expression state: ${currentExpressionState.toString()}")
        // Cases for states
        when (currentExpressionState) {
            ExpressionState.Start -> {
                //println("Expression Start State")
                transitionExpressionState()
            }
            ExpressionState.Number -> {
                number = buildNumber()
                //println("This is the number: $number")
                //println("This is the expression: $expression")
                transitionExpressionState()
            }
            ExpressionState.NumberAfterUnary -> {
                number = buildNumber()
                //println("This is the unary number: $number")
                //println("This is the expression: $expression")
                transitionExpressionState()
            }
            ExpressionState.BinaryOperator -> {
                val operator = binaryOperators[Random.nextInt(binaryOperators.size)]

                expression += operator
                if (operator == '^') {
                    expression += "("
//                    closeAllGrouping()
//                    expression = "pow(" + expression + ", "
                    expression += buildExponent()
                    expression += ")"
                    //println("This is the expression: $expression")
                    //Thread.sleep(1000)
                    currentExpressionState = ExpressionState.ExponentOperator
                }
                //else expression += operator
                transitionExpressionState()
            }
            ExpressionState.UnaryOperator -> {
                expression += unaryOperators[Random.nextInt(unaryOperators.size)]
                expression += "("
                groupingStack.addLast(')')
                transitionExpressionState()
            }
            ExpressionState.ExponentOperator -> {
                transitionExpressionState()
            }
            ExpressionState.OpenParenthesis -> {
                expression += "("
                groupingStack.addLast(')')
                transitionExpressionState()
            }
            ExpressionState.CloseParenthesis -> {
                closeGrouping()
                transitionExpressionState()
            }
            ExpressionState.OpenBracket -> {
                expression += "{"
                groupingStack.addLast('}')
                transitionExpressionState()
            }
            ExpressionState.CloseBracket -> {
                closeGrouping()
                transitionExpressionState()
            }
            ExpressionState.Cleanup -> {
                //closeAllGrouping()
                while (!groupingStack.isEmpty()) {
                    val closure = groupingStack.removeLast()
                    expression += closure
                    //println("This is the closure count: ${groupingStack.size}")
                    //println("This is the current expression: $expression")
                }
                transitionExpressionState()
            }
            ExpressionState.End -> {}
        }
    }

    fun processExponentState() {
        //println("This is the exponent state: ${currentExponentState.toString()}")
        // Cases for states
        when (currentExponentState) {
            ExponentState.Start -> {
                //println("Exponent Start State")
                transitionExponentState()
            }
            ExponentState.Number -> {
                number = buildNumber()
                exponent += number
                //println("This is the number: $number")
                //println("This is the exponent expression: $exponent")
                transitionExponentState()
            }
            ExponentState.NumberAfterUnary -> {
                number = buildNumber()
                exponent += number
                //println("This is the unary number: $number")
                //println("This is the exponent expression: $exponent")
                transitionExponentState()
            }
            ExponentState.BinaryOperator -> {
                val operator = expOperators[Random.nextInt(expOperators.size)]
                exponent += operator
                transitionExponentState()
            }
            ExponentState.UnaryOperator -> {
                exponent += unaryOperators[Random.nextInt(unaryOperators.size)]
                exponent += "("
                expGroupingStack.addLast(')')
                transitionExponentState()
            }
            ExponentState.OpenParenthesis -> {
                exponent += "("
                expGroupingStack.addLast(')')
                transitionExponentState()
            }
            ExponentState.CloseParenthesis -> {
                closeExpGrouping()
                transitionExponentState()
            }
            ExponentState.OpenBracket -> {
                exponent += "{"
                expGroupingStack.addLast('}')
                transitionExponentState()
            }
            ExponentState.CloseBracket -> {
                closeExpGrouping()
                transitionExponentState()
            }
            ExponentState.Cleanup -> {
                while (!expGroupingStack.isEmpty()) {
                    val closure = expGroupingStack.removeLast()
                    exponent += closure
                    //println("This is the exponent closure count: ${expGroupingStack.size}")
                    //println("This is the current exponent: $exponent")
                }
                transitionExponentState()
            }
            ExponentState.End -> {}
        }
    }

    private fun exponentFormat(expression: String): String {
        // Find the index of the rightmost '^' operator
        val caretIndex = expression.indexOfLast { it == '^' }

        // Base case
        if (caretIndex == -1) return expression

        // Handle recursive cases
        // Split the expression into left (base) and right (exponent) parts
        val base = expression.substring(0, caretIndex).trim()
        val exponent = expression.substring(caretIndex + 1).trim()
        // Recursively transform the base and exponent
        val transformedBase = exponentFormat(base)
        val transformedExponent = exponentFormat(exponent)
        // Wrap result in pow(x,y)
        return "pow($transformedBase, $transformedExponent)"
    }

    fun closeGrouping() {
        if (!groupingStack.isEmpty()) {
            //println("Grouping Stack size: ${groupingStack.size}")
            expression += groupingStack.removeLast()
        }
    }

    fun closeExpGrouping() {
        if (!expGroupingStack.isEmpty()) {
            //println("Exponent Grouping Stack size: ${expGroupingStack.size}")
            //println(expGroupingStack.toString())
            exponent += expGroupingStack.removeLast()
        }
    }

    fun buildExpression(): String {
        // Variables
        var transformedExp = ""

        //println("Builder Generator")
        // Loop through the builder for state cycles
        for (number in 0 until builderCycles) {
            processExpressionState()
        }
        // Check last state and reconcile
        if ((currentExpressionState == ExpressionState.BinaryOperator) ||
            (currentExpressionState == ExpressionState.OpenParenthesis) ||
            (currentExpressionState == ExpressionState.OpenBracket) ||
            (currentExpressionState == ExpressionState.UnaryOperator))
            currentExpressionState = ExpressionState.Number
        processExpressionState()

        //println("This is the remaining parenthesis: $parenthesisCount and brackets: $bracketCount")
        // Cleanup the expression (groupings are cleanly ended)
        setCurrentExpressionCleanup()
        processExpressionState()

        transformedExp = transformOracle()

        // Return the expression
        return transformedExp

    }

    private fun transformOracle(): String {
        // Variable
        var transformedExp = ""
        // Replace brackets to support Rhino format
        transformedExp = expression.replace('{', '(')
        transformedExp = transformedExp.replace('}', ')')

        transformedExp = exponentFormat(transformedExp)
        //println("This is the exponent expression adjusted: $transformedExp")

        return transformedExp
    }

    private fun buildNumber(): String {
        currentNumberState = NumberState.Start
        number = ""
        // Build the number
        while (currentNumberState != NumberState.End) {
            processNumberState()
            //transitionNumberState()
        }
        //transitionNumberState()
        return number
    }

    private fun buildExponent(): String {
        expGroupingStack = ArrayDeque<Char>()
        currentExponentState = ExponentState.Start
        exponent = ""
        // Build the number
        while (currentExponentState != ExponentState.End) {
            processExponentState()
            //println("This is the current exponent string: $exponent")
        }
        //transitionNumberState()
        //println("This is the final exponent string: $exponent")
        return exponent
    }

    fun evaluate(expression: String): Double {
        // Variables
        var result = 0.0
        try {
            // Setup the context for the evaluation
            var context = Context.enter()
            context.optimizationLevel = -1
            var scriptable = context.initStandardObjects()


            // Add Math functions to the scope
            val mathCos = "var cos = function(x) { return java.lang.Math.cos(x); };"         // Cosine function
            val mathSin = "var sin =  function(x) { return java.lang.Math.sin(x); };"          // Sine function
            val mathTan = "var tan = function(x) { return java.lang.Math.tan(x); };"          // Tangent function
            val mathCot = "var cot = function(x) { return 1/java.lang.Math.tan(x); };"        // Cotangent function
            val mathLog = "var log = function(x) { return java.lang.Math.log10(x); };"      // Base-10 logarithm
            val mathLn = "var ln = function(x) { return java.lang.Math.log(x); };"            // Natural logarithm (ln)
            val mathExp = "var pow = function(x, y) { return java.lang.Math.pow(x, y); };"    // Exponentiation (x^y)
            val mathSqrt = "var sqrt = function(x) { return java.lang.Math.sqrt(x); };"       // Square root
            // Insert code into context
            context.evaluateString(scriptable, mathCos, "MathJS", 1, null)
            context.evaluateString(scriptable, mathSin, "MathJS", 2, null)
            context.evaluateString(scriptable, mathTan, "MathJS", 3, null)
            context.evaluateString(scriptable, mathCot, "MathJS", 4, null)
            context.evaluateString(scriptable, mathLog, "MathJS", 5, null)
            context.evaluateString(scriptable, mathLn, "MathJS", 6, null)
            context.evaluateString(scriptable, mathExp, "MathJS", 7, null)
            context.evaluateString(scriptable, mathSqrt, "MathJS", 8, null)

            var resultString = context.evaluateString(scriptable,expression,"Javascript",
                1,null).toString();
            result = resultString.toDouble()
            // Truncates the result to 12 digits to fit the Summation Box
            //resultString = truncateString(resultString,12)
            println("This is the result: $result")
            //sumTextOnChange(result)
        }
        // Does nothing except log the error
        catch (e: Exception) {
            println("Error: $e");
            result = 0.0;
        }
        return result
    }

    fun randomizeCharacters(): String {
        // Variables
        var badString = expression.toCharArray()

        // Randomize 5% of the characters as bad
        val randomRatio = ceil(builderCycles * .05)

        for (x in 0..builderCycles) {
            if (x == ceil(builderCycles * .05).toInt())
                badString[x] = badCharacters[Random.nextInt(badCharacters.size)]
        }

        return badString.toString()
    }
}


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun expressionTest() {
        // Variables for tests
        var oracleResults = mutableListOf<Double>()
        var calcResults = mutableListOf<Double>()

        // Generate the expressions
        val tests = 100;

        // Calculate the midpoint
        val midpoint = tests / 2

        // Split the list into two halves
        val firstHalf = tests - midpoint
        val secondHalf = tests - firstHalf

        println("Generating $tests expressions...");

        for (x in 1..tests) {
            // Instantiate the expression state machine builder
            var expr_builder = ExpressionBuilderStateMachine()
            // Build an expression
            val expression = expr_builder.buildExpression()
            println("This is the expression built: $expression")
            // Global Oracle test against Rhino Android evaluate
            val resultOracle = expr_builder.evaluate(expression)
            println("This is the result of the Oracle evaluation: $resultOracle")
            try {
                // Local evaluation of the expression
                val pnExp = toPrefix(expr_builder.expression)
                println("This is the Original Expression: ${expr_builder.expression}")
                println("This is the PN Expression: $pnExp")
                val resultCode = evaluatePN(pnExp)
                println("This is the result of the Program evaluation: $resultCode")
                // Capture the results
                oracleResults.add(resultOracle)
                calcResults.add(resultCode)
            } catch (e: Exception) {
                println("Error with Calc processing: ${e.message}")
            }
        }

        // Output the results
        var correct = 0
        var wrong = 0
        println("Calculating test results...")
        //println("Size of oracle: ${oracleResults.size}")
        //println("Size of calc: ${calcResults.size}")
        for (x in 0..< oracleResults.size) {
            if (oracleResults.get(x) == calcResults.get(x)) correct++
            else wrong++
        }

        println("These are the total correct expressions: $correct")
        println("These are the total wrong expressions: $wrong")
        //var results = createExpressions(tests);
        //println("This is the results: ${results}");

    }

    @Test
    fun expressionFailedTest() {
        // Variables for tests
        var oracleResults = mutableListOf<Double>()
        var calcResults = mutableListOf<Double>()

        // Generate the expressions
        val tests = 100;

        println("Generating $tests failed expressions...");

        for (x in 1..tests) {
            // Instantiate the expression state machine builder
            var expr_builder = ExpressionBuilderStateMachine()

            // Build an expression
            var expression = expr_builder.buildExpression()

            println("This is the expression built: $expression")
            // Randomize the expression with failed characters
            expression = expr_builder.randomizeCharacters()
            // Global Oracle test against Rhino Android evaluate
            val resultOracle = expr_builder.evaluate(expression)
            println("This is the result of the Oracle evaluation: $resultOracle")
            try {
                // Local evaluation of the expression
                val pnExp = toPrefix(expr_builder.expression)
                println("This is the Original Expression: ${expr_builder.expression}")
                println("This is the PN Expression: $pnExp")
                val resultCode = evaluatePN(pnExp)
                println("This is the result of the Program evaluation: $resultCode")
                // Capture the results
                oracleResults.add(resultOracle)
                calcResults.add(resultCode)
            } catch (e: Exception) {
                println("Error with Calc processing: ${e.message}")
            }

        }

        // Output the results
        var correct = 0
        var wrong = 0
        println("Calculating failed test results...")
        //println("Size of oracle: ${oracleResults.size}")
        //println("Size of calc: ${calcResults.size}")
        for (x in 0..< oracleResults.size) {
            if (oracleResults.get(x) == calcResults.get(x)) correct++
            else wrong++
        }

        println("These are the total failed expressions handled: $correct")
        println("These are the total failed expressions not handled: $wrong")
    }
}