package com.example.calculatorapp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.Test
import org.mozilla.javascript.Context
import kotlin.math.ceil
import kotlin.random.Random

/**
 * Number State Machine
 * controls the states of number building
 */
sealed class NumberState {
    data object Start : NumberState()
    data object Number : NumberState()
    data object Decimal : NumberState()
    data object NumberAfterDecimal : NumberState()
    data object End : NumberState()
}

/**
 * Exponent State Machine
 * controls the states of exponent building
 */
sealed class ExponentState {
    data object Start : ExponentState()
    data object Number : ExponentState()
    data object NumberAfterUnary : ExponentState()
    data object BinaryOperator : ExponentState()
    data object UnaryOperator : ExponentState()
    data object OpenParenthesis : ExponentState()
    data object CloseParenthesis : ExponentState()
    data object OpenBracket : ExponentState()
    data object CloseBracket : ExponentState()
    data object Cleanup : ExponentState()
    data object End : ExponentState()
}

/**
 * Expression State Machine
 * controls the states of expression building
 */
sealed class ExpressionState {
    data object Start : ExpressionState()
    data object Number : ExpressionState()
    data object NumberAfterUnary : ExpressionState()
    data object BinaryOperator : ExpressionState()
    data object UnaryOperator : ExpressionState()
    data object ExponentOperator : ExpressionState()
    data object OpenParenthesis : ExpressionState()
    data object CloseParenthesis : ExpressionState()
    data object OpenBracket : ExpressionState()
    data object CloseBracket : ExpressionState()
    data object Cleanup : ExpressionState()
    data object End : ExpressionState()
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

    // List of operators
    private val numbers = listOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
    private val unaryOperators = listOf("sin", "cos", "tan", "cot", "ln", "log", "sqrt", "-")
    private val lowUnaryOperators = listOf("sin", "cos", "tan", "cot")
    private val binaryOperators = listOf('+', '-', '*', '/', '^')
    private val lowBinaryOperators = listOf('+', '-', '*')
    private val expOperators = listOf('+', '-', '*', '/')
    private val lowExpOperators = listOf('+', '-', '*')
    private val decimal = '.'
    private val badCharacters = listOf('x', 'c', 'e', '%', '!', '$', '_', '|', '&', '<', '>')
    // Expression settings and variables
    var expression = ""
    var number = ""
    var exponent = ""
    var complexity = 1
    val builderCycles = 100
    // Grouping stacks for parenthesis and brackets
    private var groupingStack = ArrayDeque<Char>()
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

    /**
     * This will set the Expression state machine
     * to cleanup
     */
    private fun setCurrentExpressionCleanup() {
         currentExpressionState = ExpressionState.Cleanup
    }

    /**
     * This will transition the Number state machine
     */
    private fun transitionNumberState() {
        val possibleNextNumberStates = numberTransitions[currentNumberState] ?: throw IllegalStateException("Invalid state: $currentNumberState")
        currentNumberState = if (possibleNextNumberStates.isEmpty()) NumberState.End
            else possibleNextNumberStates[Random.nextInt(possibleNextNumberStates.size)]
    }

    /**
     * This will transition the Expression state machine
     */
    private fun transitionExpressionState() {
        val possibleNextExpressionStates = expressionTransitions[currentExpressionState] ?: throw IllegalStateException("Invalid state: $currentExpressionState")
        currentExpressionState = if (possibleNextExpressionStates.isEmpty()) ExpressionState.End
            else possibleNextExpressionStates[Random.nextInt(possibleNextExpressionStates.size)]
    }

    /**
     * This will transition the Exponent state machine
     */
    private fun transitionExponentState() {
        val possibleNextExponentStates = exponentTransitions[currentExponentState] ?: throw IllegalStateException("Invalid state: $currentExponentState")
        currentExponentState = if (possibleNextExponentStates.isEmpty()) ExponentState.End
            else possibleNextExponentStates[Random.nextInt(possibleNextExponentStates.size)]
    }

    /**
     * This will process the Number state
     * from the Number state machine
     */
    private fun processNumberState() {
        // Cases for states
        when (currentNumberState) {
            // Number start state
            NumberState.Start -> {
                transitionNumberState()
            }
            // Generate a number
            NumberState.Number -> {
                val randomNumber = numbers[Random.nextInt(numbers.size)]
                expression += randomNumber
                number += randomNumber
                transitionNumberState()
            }
            // Generate a decimal
            NumberState.Decimal -> {
                expression += decimal
                number += decimal
                transitionNumberState()
            }
            // Generate a number after decimal
            NumberState.NumberAfterDecimal -> {
                val randomNumber = numbers[Random.nextInt(numbers.size)]
                expression += randomNumber
                number += randomNumber
                transitionNumberState()
            }
            // Number End state
            NumberState.End -> {}
        }
    }

    /**
     * This will process the Expression state
     * from the Expression state machine
     */
    private fun processExpressionState() {
        // Cases for states
        when (currentExpressionState) {
            // Expression start state
            ExpressionState.Start -> {
                transitionExpressionState()
            }
            // Generate a number
            ExpressionState.Number -> {
                number = buildNumber()
                transitionExpressionState()
            }
            // Numbers that are after unary operators
            ExpressionState.NumberAfterUnary -> {
                number = buildNumber()
                transitionExpressionState()
            }
            // Generate a binary operator
            ExpressionState.BinaryOperator -> {
                val operator = if (complexity == 1) binaryOperators[Random.nextInt(binaryOperators.size)]
                    else lowBinaryOperators[Random.nextInt(lowBinaryOperators.size)]
                // Set the operator
                expression += operator
                if (operator == '^') {
                    expression += "("
                    expression += buildExponent()
                    expression += ")"
                    currentExpressionState = ExpressionState.ExponentOperator
                }
                transitionExpressionState()
            }
            // Generate a unary operator
            ExpressionState.UnaryOperator -> {
                expression += if (complexity == 1) unaryOperators[Random.nextInt(unaryOperators.size)]
                    else lowUnaryOperators[Random.nextInt(lowUnaryOperators.size)]
                expression += "("
                groupingStack.addLast(')')
                transitionExpressionState()
            }
            // Handle clean transition from Exponent state
            ExpressionState.ExponentOperator -> {
                transitionExpressionState()
            }
            // Generate an open parenthesis
            ExpressionState.OpenParenthesis -> {
                expression += "("
                groupingStack.addLast(')')
                transitionExpressionState()
            }
            // Close out the next group closure
            ExpressionState.CloseParenthesis -> {
                closeGrouping()
                transitionExpressionState()
            }
            // Generate an open bracket
            ExpressionState.OpenBracket -> {
                expression += "{"
                groupingStack.addLast('}')
                transitionExpressionState()
            }
            // Close out the next group closure
            ExpressionState.CloseBracket -> {
                closeGrouping()
                transitionExpressionState()
            }
            // Expression cleanup state
            ExpressionState.Cleanup -> {
                //closeAllGrouping()
                while (!groupingStack.isEmpty()) {
                    val closure = groupingStack.removeLast()
                    expression += closure
                }
                transitionExpressionState()
            }
            // Expression End State
            ExpressionState.End -> {}
        }
    }

    /**
     * This handles the processing for Exponent states
     * used in the Exponent state machines
     */
    private fun processExponentState() {
        // Cases for states
        when (currentExponentState) {
            // Exponent start state
            ExponentState.Start -> {
                transitionExponentState()
            }
            // Generate a number
            ExponentState.Number -> {
                number = buildNumber()
                exponent += number
                transitionExponentState()
            }
            // Numbers that are after unary operators
            ExponentState.NumberAfterUnary -> {
                number = buildNumber()
                exponent += number
                transitionExponentState()
            }
            // Generate a binary operator
            ExponentState.BinaryOperator -> {
                val operator = if (complexity == 1) expOperators[Random.nextInt(expOperators.size)]
                else lowExpOperators[Random.nextInt(lowExpOperators.size)]
                exponent += operator
                transitionExponentState()
            }
            // Generate a unary operator
            ExponentState.UnaryOperator -> {
                exponent += if (complexity == 1) unaryOperators[Random.nextInt(unaryOperators.size)]
                else lowUnaryOperators[Random.nextInt(lowUnaryOperators.size)]
                exponent += "("
                expGroupingStack.addLast(')')
                transitionExponentState()
            }
            // Generate open parenthesis
            ExponentState.OpenParenthesis -> {
                exponent += "("
                expGroupingStack.addLast(')')
                transitionExponentState()
            }
            // Close out the next group closure
            ExponentState.CloseParenthesis -> {
                closeExpGrouping()
                transitionExponentState()
            }
            // Generate an open bracket
            ExponentState.OpenBracket -> {
                exponent += "{"
                expGroupingStack.addLast('}')
                transitionExponentState()
            }
            // Close out the next group closure
            ExponentState.CloseBracket -> {
                closeExpGrouping()
                transitionExponentState()
            }
            // Cleanup all closures
            ExponentState.Cleanup -> {
                while (!expGroupingStack.isEmpty()) {
                    val closure = expGroupingStack.removeLast()
                    exponent += closure
                }
                transitionExponentState()
            }
            // Exponent end state
            ExponentState.End -> {}
        }
    }

    /**
     * This will create nested pow functions
     * to ensure compatibility with Oracle
     * @param expression String of the exponent expression
     * @return String in exponent format. example: pow(x,y)
     */
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

    /**
     * This will close an Expression
     * from the expression state machine
     */
    private fun closeGrouping() {
        if (!groupingStack.isEmpty()) expression += groupingStack.removeLast()
    }

    /**
     * This will close an Exponent
     * from the exponent state machine
     */
    private fun closeExpGrouping() {
        if (!expGroupingStack.isEmpty()) exponent += expGroupingStack.removeLast()
    }

    /**
     * This will build an Expression
     * from the expression state machine
     * @param length This is the length of the expression
     * @param comp This is the complexity of the expression
     * @return String of the expression that was generated
     */
    fun buildExpression(length: Int, comp: Int): String {
        // Variables
        var transformedExp = ""
        complexity = comp

        // Loop through the builder for state cycles
        for (number in 0..length) {
            processExpressionState()
        }
        // Check last state and reconcile closure (complex state decision)
        val binaryState = (currentExpressionState == ExpressionState.BinaryOperator) &&
            (!charArrayOf(')','}').contains(expression.get(expression.length-1)))
        val unaryState = (currentExpressionState == ExpressionState.NumberAfterUnary) ||
            (unaryOperators.contains(expression.substring(expression.length-1)))
        val groupedState = (currentExpressionState == ExpressionState.OpenParenthesis) ||
            (currentExpressionState == ExpressionState.OpenBracket)
        val numberState = currentExpressionState == ExpressionState.Number
        val operatorState = binaryOperators.contains(expression.get(expression.length-1))
        val bracketState = charArrayOf('(','{').contains(expression.get(expression.length-1))

        // If the next state matches a state that needs to be reconciled, add a number
        if (binaryState || groupedState || unaryState || numberState || operatorState
            || bracketState) {
            currentExpressionState = ExpressionState.Number
            // Process the next state for the expression
            processExpressionState()
        }
        // Cleanup the expression (groupings are cleanly ended)
        setCurrentExpressionCleanup()
        processExpressionState()
        // Transform the expression for Oracle
        transformedExp = transformOracle()
        // Return the expression
        return transformedExp
    }

    /**
     * This will transform the expression
     * to ensure compatibility with Oracle library
     * Changes exponent and removed brackets
     * @return String of the expression transformed for Oracle
     */
    private fun transformOracle(): String {
        // Variable
        var transformedExp = ""
        // Replace brackets to support Rhino format
        transformedExp = expression.replace('{', '(')
        transformedExp = transformedExp.replace('}', ')')

        transformedExp = exponentFormat(transformedExp)

        return transformedExp
    }

    /**
     * This will build a Number
     * from the number state machine
     * @return String of the number generated
     */
    private fun buildNumber(): String {
        currentNumberState = NumberState.Start
        number = ""
        // Build the number
        while (currentNumberState != NumberState.End) {
            processNumberState()
        }
        return number
    }

    /**
     * This will build an Exponent
     * from the exponent state machine
     * @return String of the exponent expression generated
     */
    private fun buildExponent(): String {
        expGroupingStack = ArrayDeque<Char>()
        currentExponentState = ExponentState.Start
        exponent = ""
        // Build the number
        while (currentExponentState != ExponentState.End) {
            processExponentState()
        }
        // Return the exponent
        return exponent
    }

    /**
     * This will evaluate the expression
     * with the Rhino library as an Oracle
     * @param expression This is the expression as a String
     * @return Double of the evaluated result from Oracle
     */
    fun evaluate(expression: String, verbose: Boolean): Double {
        // Variables
        var result = 0.0
        // Handler for evaluation for Rhino
        try {
            // Setup the context for the evaluation
            val context = Context.enter()
            context.optimizationLevel = -1
            val scriptable = context.initStandardObjects()
            // Add Math functions to the scope
            val mathCos = "var cos = function(x) { return java.lang.Math.cos(x); };"           // Cosine function
            val mathSin = "var sin =  function(x) { return java.lang.Math.sin(x); };"          // Sine function
            val mathTan = "var tan = function(x) { return java.lang.Math.tan(x); };"           // Tangent function
            val mathCot = "var cot = function(x) { return 1/java.lang.Math.tan(x); };"         // Cotangent function
            val mathLog = "var log = function(x) { return java.lang.Math.log10(x); };"         // Base-10 logarithm
            val mathLn = "var ln = function(x) { return java.lang.Math.log(x); };"             // Natural logarithm (ln)
            val mathExp = "var pow = function(x, y) { return java.lang.Math.pow(x, y); };"     // Exponentiation (x^y)
            val mathSqrt = "var sqrt = function(x) { return java.lang.Math.sqrt(x); };"        // Square root
            // Insert code into context for math functions
            context.evaluateString(scriptable, mathCos, "MathJS", 1, null)
            context.evaluateString(scriptable, mathSin, "MathJS", 2, null)
            context.evaluateString(scriptable, mathTan, "MathJS", 3, null)
            context.evaluateString(scriptable, mathCot, "MathJS", 4, null)
            context.evaluateString(scriptable, mathLog, "MathJS", 5, null)
            context.evaluateString(scriptable, mathLn, "MathJS", 6, null)
            context.evaluateString(scriptable, mathExp, "MathJS", 7, null)
            context.evaluateString(scriptable, mathSqrt, "MathJS", 8, null)
            // Handle the oracle evaluation with Rhino
            val resultString = context.evaluateString(scriptable,expression,"Javascript",
                1,null).toString();
            result = resultString.toDouble()
        }
        // Handle exceptions and log errors
        catch (e: Exception) {
            if (verbose) println("Error: $e");
            result = 0.0;
        }
        // Return the evaluated result
        return result
    }

    /**
     * Randomize the characters
     * to generate bad expressions
     * @param expression This is the expression as a String
     * @return String of the bad expression with random characters
     */
    fun randomizeCharacters(expression: String): String {
        // Variables
        val badString = expression.toCharArray()
        // Randomize 5% of the characters as bad
        val randomRatio = .15
        // Change characters according to randomizer rate
        for (x in expression.indices) {
            if (x % ceil(expression.length / ceil(expression.length * randomRatio)).toInt() == 0)
                badString[x] = badCharacters[Random.nextInt(badCharacters.size)]
        }
        // Return expression with unknown characters
        return badString.joinToString("")
    }
}

/**
 * Suspend function to handle endless
 * timeouts or loops in the Calculator code
 * @param expression This is the string of the expression to process
 * @param resultOracle This is the result that Oracle had as a Double
 * @param oracleBadResults MutableList of bad expression results from oracle
 * @param oracleCorrectResults MutableList of good expression results from oracle
 * @param calcBadResults MutableList of bad expression results from calculator
 * @param calcCorrectResults MutableList of good expression results from calculator
 * @param verbose Boolean that defines the output verbosity
 * @return Double of Calculator expression evaluation
 */
suspend fun endlessFunction(expression: String, index: Int,
                            resultOracle: Double,
                            oracleBadResults: MutableList<Double>,
                            oracleCorrectResults: MutableList<Double>,
                            calcBadResults: MutableList<Double>,
                            calcCorrectResults: MutableList<Double>,
                            verbose: Boolean) {
    // Initialize result code
    var resultCode = 0.0
    // Handler for Calculator functions
    try {
        withTimeout(25000) {
            runInterruptible {
                // Local evaluation of the expression
                val pnExp = toPrefix(expression)
                resultCode = evaluatePN(pnExp)
            }
        }
    } catch (e: CancellationException) {
        if (verbose) println("Calculator timed out and was canceled")
    } catch (e: Exception) {
        if (verbose) println("Calculator Exception: ${e.message}")
    }
    finally {
        if (verbose) println("This is the result of the Program evaluation: $resultCode")
        // Capture the results
        if (index % 2 == 0) {
            oracleBadResults.add(resultOracle)
            calcBadResults.add(resultCode)
        } else {
            oracleCorrectResults.add(resultOracle)
            calcCorrectResults.add(resultCode)
        }
    }
}

/**
 * This will calculate and output the results of the
 * expression testing based on the number of tests
 * defined and the oracle that was used
 * @param oracleCorrectResults List of good expression results from oracle
 * @param oracleBadResults List of bad expression results from oracle
 * @param calcCorrectResults List of good expression results from calculator
 * @param calcBadResults List of bad expression results from calculator
 * @param tests Number of tests done
 * @param verbose Boolean that defines verbosity of output
 */
fun expressionTestAnalysis(oracleCorrectResults: MutableList<Double>,
                           oracleBadResults: MutableList<Double>,
                           calcCorrectResults: MutableList<Double>,
                           calcBadResults: MutableList<Double>,
                           tests: Int) {
    // Output the results
    println()
    println("Calculating test expression results...")
    // Good expression results
    var correct = 0
    var wrong = 0
    // Determine oracle count
    var oracleCount = if (oracleCorrectResults.size > calcCorrectResults.size) calcCorrectResults.size
        else oracleCorrectResults.size
    // Count correct results
    for (x in 0..< oracleCount) {
        if (oracleCorrectResults.get(x) == calcCorrectResults.get(x) ||
            (oracleCorrectResults.get(x) == 0.0 && oracleCorrectResults.get(x) != 0.0) ||
            (oracleCorrectResults.get(x).isInfinite() && oracleCorrectResults.get(x) == 0.0) ||
            (oracleCorrectResults.get(x) == 0.0 && oracleCorrectResults.get(x).isNaN())) correct++
        else wrong++
    }
    println("These are the total correct good expressions: $correct")
    println("These are the total wrong good expressions: $wrong")

    // Bad expression results
    var handledFailures = 0
    var unhandledFailures = 0

    oracleCount = if (oracleBadResults.size > calcBadResults.size) calcBadResults.size
    else oracleBadResults.size
    // Count the bad results
    for (x in 0..< oracleCount) {
        if (oracleBadResults.get(x) == calcBadResults.get(x) ||
            (oracleBadResults.get(x) == 0.0 && calcBadResults.get(x) != 0.0) ||
            (oracleBadResults.get(x).isInfinite() && calcBadResults.get(x) == 0.0) ||
            (oracleBadResults.get(x) == 0.0 && calcBadResults.get(x).isNaN()))
            handledFailures++
        else unhandledFailures++
    }
    // Reconcile unhandled failures
    if (unhandledFailures == 0) unhandledFailures = ((tests/2) - handledFailures)
    else if ((unhandledFailures + handledFailures) < (tests/2))
        unhandledFailures = ((tests/2) - handledFailures)
    if ((correct + wrong) < (tests/2)) unhandledFailures += (tests/2) - (correct + wrong)
    println("These are the total bad expressions handled: $handledFailures")
    println("These are the total bad expressions not handled: $unhandledFailures")

    // Total number of tests
    val totalGoodTests = correct + handledFailures
    val totalBadTests = wrong + unhandledFailures
    val totalFailedTest = handledFailures + unhandledFailures
    val totalTests = totalGoodTests + totalBadTests

    // Calculate statistics
    val accuracy = (totalGoodTests.toDouble() / totalTests) * 100
    val errorRate = (totalBadTests.toDouble() / totalTests) * 100
    val handledFailureRate = (handledFailures.toDouble() / totalFailedTest) * 100
    val unhandledFailureRate = (unhandledFailures.toDouble() / totalFailedTest) * 100

    // Display final analysis results
    println()
    println("Total tests: $totalTests")
    println("Accuracy: ${"%.2f".format(accuracy)}%")
    println("Error Rate: ${"%.2f".format(errorRate)}%")
    println("Handled Bad Expression Rate: ${"%.2f".format(handledFailureRate)}%")
    println("Unhandled Bad Expression Rate: ${"%.2f".format(unhandledFailureRate)}%")
}

/**
 * Example local unit test, which will execute on the development machine (host).
 * Will generate random expressions to test out the accuracy of the calculator
 * on various expressions of differing length and complexity
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class CalculatorUnitTests {
    /**
     * Generate expressions and test them out
     * will generate good and bad expressions
     * and test based on the number of tests defined in the Unit test
     */
    @Test
    fun expressionTest() = runTest {
        // Variables for tests
        val oracleCorrectResults = mutableListOf<Double>()
        val calcCorrectResults = mutableListOf<Double>()
        val oracleBadResults = mutableListOf<Double>()
        val calcBadResults = mutableListOf<Double>()
        // Generate the expressions
        //val tests = 10000000
        val tests = 7500000
        //val tests = 10
        val verbose = false
        // Expression complexity
        val min = 25
        val max = 100

        println("Generating $tests expressions...");

        for (x in 1..tests) {
            // Instantiate the expression state machine builder
            val expr_builder = ExpressionBuilderStateMachine()
            // Generate a random integer between 25 and 100 for expression
            val expressionLength = Random.nextInt(max - min + 1) + min
            val expressionComplexity = Random.nextInt(2)
            // Build an expression
            var expression = expr_builder.buildExpression(expressionLength, expressionComplexity)
            if (verbose) {
                println("Expression Length (in state changes): $expressionLength")
                when (expressionComplexity) {
                    0 -> println("Expression has Low Complexity (in operators)")
                    1 -> println("Expression has High Complexity (in operators)")
                }
            }
            // Determine if valid or bad expression (even are bad)
            if (x % 2 == 0) {
                if (verbose) {
                    println("This is a bad expression")
                    println("Expression before randomization: $expression")
                }
                expr_builder.expression = expr_builder.randomizeCharacters(expr_builder.expression)
                expression = if (expression.length != expr_builder.expression.length)
                    expr_builder.randomizeCharacters(expression)
                    else expr_builder.expression
            }
            else if (verbose) println("This is a good expression")
            if (verbose) {
                println("This is the Original Expression: ${expr_builder.expression}")
                println("This is the Rhino Expression: $expression")
            }
            // Global Oracle test against Rhino Android evaluate
            val resultOracle = expr_builder.evaluate(expression, verbose)
            //var resultCode = 0.0
            if (verbose) println("This is the result of the Oracle evaluation: $resultOracle")

            // Handle the Calculator evaluation
            // Coroutine implementation for handle stuck evaluations
            try {
                val scope = CoroutineScope(Dispatchers.Default)
                val job = scope.launch {
                    try {
                        endlessFunction(expr_builder.expression, x, resultOracle,
                            oracleBadResults, oracleCorrectResults, calcBadResults,
                            calcCorrectResults, verbose)
                    } catch (e: TimeoutCancellationException) {
                        if (verbose) println("Job evaluation timed out!")
                    } catch (e: CancellationException) {
                        if (verbose) println("Job evaluation timed out!")
                    } catch (e: Exception) {
                        if (verbose) println("Job Exception: ${e.message}")
                    }
                }
                // Join the job
                job.join()
            // Handle scope exceptions (do nothing)
            } catch (_: Exception) {}
        }

        // Output the Test Analysis
        expressionTestAnalysis(oracleCorrectResults, oracleBadResults, calcCorrectResults,
            calcBadResults, tests)

    }
}