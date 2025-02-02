package com.example.calculatorapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.*
import com.example.calculatorapp.ui.theme.AllClearButton
import com.example.calculatorapp.ui.theme.BackgroundDark
import com.example.calculatorapp.ui.theme.ButtonText
import com.example.calculatorapp.ui.theme.CalculatorAppTheme
import com.example.calculatorapp.ui.theme.ClearButton
import com.example.calculatorapp.ui.theme.GoldButton
import com.example.calculatorapp.ui.theme.NumberBackground
import com.example.calculatorapp.ui.theme.NumberButton
import com.example.calculatorapp.ui.theme.NumberZeroButton
import java.util.Stack
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.sqrt
import kotlin.math.tan
import kotlin.math.tanh



/**
 * This is the main activity class that drives the UI
 * extends the ComponentActivity
 */
class MainActivity : ComponentActivity() {
    /**
     * Override the onCreate function
     * @param savedInstanceState This is an instance of Bundle
     * that can be empty
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalculatorAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Calculator("standard")
                }
            }
        }
    }
}

/**
 * This is the Main Calculator composable
 * @param style This is the style of calculator to present
 * @param modifier This is a Modifier object to pass along
 */
@Composable
fun Calculator(style: String, modifier: Modifier = Modifier) {
    // Layout Constraint for placement handling
    ConstraintLayout(
        modifier = Modifier.background(BackgroundDark)
    ) {
        // Create references for the composables to constrain
        val (viewBox, buttonBox) = createRefs()
        // Author details
        var author = "Summers"
        var cwuid ="49933065"
        // Create the state for mutable text fields
        var viewText by rememberSaveable { mutableStateOf(author) }
        var sumText by rememberSaveable { mutableStateOf(cwuid) }

        val context = LocalContext.current
        val showToast = fun(message: String) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }

        // This is the view box
        Column(
            // Constraint to top of button box
            modifier = Modifier
                .constrainAs(viewBox) {
                    bottom.linkTo(buttonBox.top, margin = 8.dp)
                }
        )
        {
            // This is the view box
            ViewBox(viewText);
            // This is the summation box
            SummationBox(sumText);
        }

        // This is the calculator buttons box
        Column(
            // Constraint to bottom of the parent
            modifier = Modifier
                .constrainAs(buttonBox) {
                    bottom.linkTo(parent.bottom, margin = 16.dp)
                }
                .padding(8.dp)
                .fillMaxWidth()
                .background(NumberBackground, RoundedCornerShape(12.dp)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            // First row of buttons
            ButtonsRow1(viewText,author,
                viewTextOnChange={ newValue -> viewText = newValue });
            // Second row of buttons
            ButtonsRow2(viewText,author,
                viewTextOnChange={ newValue -> viewText = newValue });
            // Third row of buttons
            ButtonsRow3(viewText,author,
                viewTextOnChange={ newValue -> viewText = newValue });
            // Fourth row of buttons
            ButtonsRow4(viewText,author,
                viewTextOnChange={ newValue -> viewText = newValue });
            // Fifth row of buttons
            ButtonsRow5(viewText,author,
                viewTextOnChange={ newValue -> viewText = newValue });
            // Six row of buttons
            ButtonsRow6(viewText,sumText,author,
                viewTextOnChange={ newValue -> viewText = newValue },
                sumTextOnChange={ newValue -> sumText = newValue },
                showToast=showToast);
        }
    }
}

/**
 * This is the View Box composable
 * @param values This is the value string to render
 */
@Composable
fun ViewBox(values: String) {
    Text(
        text = values,
        textAlign = TextAlign.Right,
        fontSize = 42.sp,
        lineHeight = 45.sp,
        color = Color.White,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    )
}

/**
 * This is the Summation Box composable
 * @param values This is the value string to render
 */
@Composable
fun SummationBox(values: String) {
    Text(
        text = values,
        textAlign = TextAlign.Right,
        fontSize = 58.sp,
        color = Color.White,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    )
}

/**
 * This is the Row 1 Buttons composable
 * @param viewText This is the current value of the view Text
 * @param author This is the current value of the author
 * @param viewTextOnChange This is the callback function that updates view Text
 * @param viewTextOnChange This is the callback function that updates view Text
 */
@Composable
fun ButtonsRow1(viewText: String, author: String, viewTextOnChange: ((String) -> Unit) = {}) {
    // First row of buttons
    Row() {
        FloatingActionButton(
            // Updates the viewText with a substring of one less of length
            onClick = {
                if (viewText.length > 0)
                    viewTextOnChange("${viewText.subSequence(0,viewText.length-1)}")
            },
            shape = RoundedCornerShape(50),

            modifier = Modifier
                .padding(6.dp)
                .size(65.dp),
            containerColor = ClearButton,
            contentColor = ButtonText)
        {
            Text(
                "C",
                fontSize = 30.sp,
            )
        }

        FloatingActionButton(
            // Updates the viewText depending on initial values; otherwise it appends
            onClick = {
                if (viewText != author && viewText != "0") viewTextOnChange(viewText +"(");
                else viewTextOnChange("(");
            },
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .padding(7.dp)
                .size(65.dp),
            containerColor = NumberButton,
            contentColor = ButtonText)
        {
            Text(
                "(",
                fontSize = 30.sp,
            )
        }

        FloatingActionButton(
            // Updates the viewText depending on initial values; otherwise it appends
            onClick = {
                if (viewText != author && viewText != "0") viewTextOnChange(viewText +")");
                else viewTextOnChange(")");
            },
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .padding(7.dp)
                .size(65.dp),
            containerColor = NumberButton,
            contentColor = ButtonText)
        {
            Text(
                ")",
                fontSize = 30.sp,
            )
        }

        FloatingActionButton(
            // Updates the viewText depending on initial values; otherwise it appends
            onClick = {
                if (viewText != author && viewText != "0") viewTextOnChange(viewText +"{");
                else viewTextOnChange("{");
            },
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .padding(7.dp)
                .size(65.dp),
            containerColor = GoldButton,
            contentColor = ButtonText)
        {
            Text(
                "{",
                fontSize = 30.sp,
            )
        }

        FloatingActionButton(
            // Updates the viewText depending on initial values; otherwise it appends
            onClick = {
                if (viewText != author && viewText != "0") viewTextOnChange(viewText +"}");
                else viewTextOnChange("}");
            },
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .padding(7.dp)
                .size(65.dp),
            containerColor = GoldButton,
            contentColor = ButtonText)
        {
            Text(
                "}",
                fontSize = 30.sp,
            )
        }

        FloatingActionButton(
            // Updates the viewText depending on initial values; otherwise it appends
            onClick = {
                if (viewText != author && viewText != "0") viewTextOnChange(viewText +"sin(");
                else viewTextOnChange("sin(");
            },
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .padding(7.dp)
                .size(65.dp),
            containerColor = GoldButton,
            contentColor = ButtonText)
        {
            Text(
                "sin",
                fontSize = 30.sp,
            )
        }
    }
}

/**
 * This is the Row 2 Buttons composable
 * @param viewText This is the current value of the view Text
 * @param author This is the current value of the author
 * @param viewTextOnChange This is the callback function that updates view Text
 */
@Composable
fun ButtonsRow2(viewText: String, author: String, viewTextOnChange: ((String) -> Unit) = {}) {
    Row() {
        FloatingActionButton(
            // Updates the viewText depending on initial values; otherwise it appends
            onClick = {
                if (viewText != author && viewText != "0") viewTextOnChange(viewText +"sin(");
                else viewTextOnChange("sin(");
            },
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .padding(7.dp)
                .size(65.dp),
            containerColor = NumberButton,
            contentColor = ButtonText)
        {
            Text(
                "sin",
                fontSize = 30.sp,
            )
        }

        FloatingActionButton(
            // Updates the viewText depending on initial values; otherwise it appends
            onClick = {
                if (viewText != author && viewText != "0") viewTextOnChange(viewText +"cos(");
                else viewTextOnChange("cos(");
            },
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .padding(7.dp)
                .size(65.dp),
            containerColor = NumberButton,
            contentColor = ButtonText)
        {
            Text(
                "cos",
                fontSize = 30.sp,
            )
        }

        FloatingActionButton(
            // Updates the viewText depending on initial values; otherwise it appends
            onClick = {
                if (viewText != author && viewText != "0") viewTextOnChange(viewText +"tan(");
                else viewTextOnChange("tan(");
            },
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .padding(7.dp)
                .size(65.dp),
            containerColor = NumberButton,
            contentColor = ButtonText)
        {
            Text(
                "tan",
                fontSize = 30.sp,
            )
        }

        FloatingActionButton(
            // Updates the viewText depending on initial values; otherwise it appends
            onClick = {
                if (viewText != author && viewText != "0") viewTextOnChange(viewText +"cot(");
                else viewTextOnChange("cot(");
            },
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .padding(7.dp)
                .size(65.dp),
            containerColor = GoldButton,
            contentColor = ButtonText)
        {
            Text(
                "cot",
                fontSize = 30.sp,
            )
        }

        FloatingActionButton(
            // Updates the viewText depending on initial values; otherwise it appends
            onClick = {
                if (viewText != author && viewText != "0") viewTextOnChange(viewText +"ln(");
                else viewTextOnChange("ln(");
            },
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .padding(7.dp)
                .size(65.dp),
            containerColor = GoldButton,
            contentColor = ButtonText)
        {
            Text(
                "ln",
                fontSize = 30.sp,
            )
        }
    }
}

/**
 * This is the Row 3 Buttons composable
 * @param viewText This is the current value of the view Text
 * @param author This is the current value of the author
 * @param viewTextOnChange This is the callback function that updates view Text
 */
@Composable
fun ButtonsRow3(viewText: String, author: String, viewTextOnChange: ((String) -> Unit) = {}) {
    Row() {
        FloatingActionButton(
            // Updates the viewText depending on initial values; otherwise it appends
            onClick = {
                if (viewText != author && viewText != "0") viewTextOnChange(viewText +"7");
                else viewTextOnChange("7");
            },
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .padding(7.dp)
                .size(65.dp),
            containerColor = NumberButton,
            contentColor = ButtonText)
        {
            Text(
                "7",
                fontSize = 30.sp,
            )
        }

        FloatingActionButton(
            // Updates the viewText depending on initial values; otherwise it appends
            onClick = {
                if (viewText != author && viewText != "0") viewTextOnChange(viewText +"8");
                else viewTextOnChange("8");
            },
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .padding(7.dp)
                .size(65.dp),
            containerColor = NumberButton,
            contentColor = ButtonText)
        {
            Text(
                "8",
                fontSize = 30.sp,
            )
        }

        FloatingActionButton(
            // Updates the viewText depending on initial values; otherwise it appends
            onClick = {
                if (viewText != author && viewText != "0") viewTextOnChange(viewText +"9");
                else viewTextOnChange("9");
            },
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .padding(7.dp)
                .size(65.dp),
            containerColor = NumberButton,
            contentColor = ButtonText)
        {
            Text(
                "9",
                fontSize = 30.sp,
            )
        }

        FloatingActionButton(
            // Updates the viewText depending on initial values; otherwise it appends
            onClick = {
                if (viewText != author && viewText != "0") viewTextOnChange(viewText +"*");
                else viewTextOnChange("*");
            },
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .padding(7.dp)
                .size(65.dp),
            containerColor = GoldButton,
            contentColor = ButtonText)
        {
            Text(
                "*",
                fontSize = 30.sp,
            )
        }

        FloatingActionButton(
            // Updates the viewText depending on initial values; otherwise it appends
            onClick = {
                if (viewText != author && viewText != "0") viewTextOnChange(viewText +"log(");
                else viewTextOnChange("log(");
            },
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .padding(7.dp)
                .size(65.dp),
            containerColor = GoldButton,
            contentColor = ButtonText)
        {
            // Aligned row for formatting
            Row(
                verticalAlignment = Alignment.Bottom
            )
            {
                Text(
                    "log",
                    fontSize = 25.sp,

                    )

                Text(
                    "10",
                    fontSize = 15.sp,
                )
            }
        }
    }
}

/**
 * This is the Row 4 Buttons composable
 * @param viewText This is the current value of the view Text
 * @param author This is the current value of the author
 * @param viewTextOnChange This is the callback function that updates view Text
 */
@Composable
fun ButtonsRow4(viewText: String, author: String, viewTextOnChange: ((String) -> Unit) = {}) {
    Row() {
        FloatingActionButton(
            onClick = {
                // Updates the viewText depending on initial values; otherwise it appends
                if (viewText != author && viewText != "0") viewTextOnChange(viewText +"4");
                else viewTextOnChange("4");
            },
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .padding(7.dp)
                .size(65.dp),
            containerColor = NumberButton,
            contentColor = ButtonText)
        {
            Text(
                "4",
                fontSize = 30.sp,
            )
        }

        FloatingActionButton(
            // Updates the viewText depending on initial values; otherwise it appends
            onClick = {
                if (viewText != author && viewText != "0") viewTextOnChange(viewText +"5");
                else viewTextOnChange("5");
            },
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .padding(7.dp)
                .size(65.dp),
            containerColor = NumberButton,
            contentColor = ButtonText)
        {
            Text(
                "5",
                fontSize = 30.sp,
            )
        }

        FloatingActionButton(
            // Updates the viewText depending on initial values; otherwise it appends
            onClick = {
                if (viewText != author && viewText != "0") viewTextOnChange(viewText +"6");
                else viewTextOnChange("6");
            },
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .padding(7.dp)
                .size(65.dp),
            containerColor = NumberButton,
            contentColor = ButtonText)
        {
            Text(
                "6",
                fontSize = 30.sp,
            )
        }

        FloatingActionButton(
            // Updates the viewText depending on initial values; otherwise it appends
            onClick = {
                if (viewText != author && viewText != "0") viewTextOnChange(viewText +"+");
                else viewTextOnChange("+");
            },
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .padding(7.dp)
                .size(65.dp),
            containerColor = GoldButton,
            contentColor = ButtonText)
        {
            Text(
                "+",
                fontSize = 30.sp,
            )
        }

        FloatingActionButton(
            // Updates the viewText depending on initial values; otherwise it appends
            onClick = {
                if (viewText != author && viewText != "0") viewTextOnChange(viewText +"^");
                else viewTextOnChange("^");
            },
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .padding(7.dp)
                .size(65.dp),
            containerColor = GoldButton,
            contentColor = ButtonText)
        {
            Text(
                "^",
                fontSize = 30.sp,
            )
        }
    }
}

/**
 * This is the Row 5 Buttons composable
 * @param viewText This is the current value of the view Text
 * @param author This is the current value of the author
 * @param viewTextOnChange This is the callback function that updates view Text
 */
@Composable
fun ButtonsRow5(viewText: String, author: String, viewTextOnChange: ((String) -> Unit) = {}) {
    Row() {
        FloatingActionButton(
            onClick = {
                // Updates the viewText depending on initial values; otherwise it appends
                if (viewText != author && viewText != "0") viewTextOnChange(viewText +"1");
                else viewTextOnChange("1");
            },
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .padding(7.dp)
                .size(65.dp),
            containerColor = NumberButton,
            contentColor = ButtonText)
        {
            Text(
                "1",
                fontSize = 30.sp,
            )
        }

        FloatingActionButton(
            // Updates the viewText depending on initial values; otherwise it appends
            onClick = {
                if (viewText != author && viewText != "0") viewTextOnChange(viewText +"2");
                else viewTextOnChange("2");
            },
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .padding(7.dp)
                .size(65.dp),
            containerColor = NumberButton,
            contentColor = ButtonText)
        {
            Text(
                "2",
                fontSize = 30.sp,
            )
        }

        FloatingActionButton(
            // Updates the viewText depending on initial values; otherwise it appends
            onClick = {
                if (viewText != author && viewText != "0") viewTextOnChange(viewText +"3");
                else viewTextOnChange("3");
            },
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .padding(7.dp)
                .size(65.dp),
            containerColor = NumberButton,
            contentColor = ButtonText)
        {
            Text(
                "3",
                fontSize = 30.sp,
            )
        }

        FloatingActionButton(
            // Updates the viewText depending on initial values; otherwise it appends
            onClick = {
                if (viewText != author && viewText != "0") viewTextOnChange(viewText +"-");
                else viewTextOnChange("-");
            },
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .padding(7.dp)
                .size(65.dp),
            containerColor = GoldButton,
            contentColor = ButtonText)
        {
            Text(
                "-",
                fontSize = 30.sp,
            )
        }

        FloatingActionButton(
            // Updates the viewText depending on initial values; otherwise it appends
            onClick = {
                if (viewText != author && viewText != "0") viewTextOnChange(viewText +"/");
                else viewTextOnChange("/");
            },
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .padding(7.dp)
                .size(65.dp),
            containerColor = GoldButton,
            contentColor = ButtonText)
        {
            Text(
                "/",
                fontSize = 30.sp,
            )
        }
    }
}


/**
 * This is the Row 6 Buttons composable
 * @param viewText This is the current value of the view Text
 * @param author This is the current value of the author
 * @param viewTextOnChange This is the callback function that updates view Text
 * @param sumTextOnChange This is the callback function that updates sum Text
 * @param showToast This is the callback function for showing Toasts
 */
@Composable
fun ButtonsRow6(viewText: String, sums: String, author: String,
                viewTextOnChange: ((String) -> Unit) = {},
                sumTextOnChange: ((String) -> Unit) = {},
                showToast: ((String) -> Unit) = {})
{
    Row() {
        // This is the AC button
        FloatingActionButton(
            // Updates the viewText and sumText to be 0
            onClick = {
                viewTextOnChange("0");
                sumTextOnChange("0");
            },
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .padding(7.dp)
                .size(65.dp)
                .widthIn(max = 56.dp)
                .heightIn(min = 66.dp),
            containerColor = AllClearButton,
            contentColor = ButtonText)
        {
            Text(
                "AC",
                fontSize = 30.sp,
            )
        }

        FloatingActionButton(
            // Updates the viewText depending on initial values; otherwise it appends
            onClick = {
                if (viewText != author && viewText != "0") { viewTextOnChange(viewText +"0"); }
            },
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .padding(7.dp)
                .size(65.dp),
            containerColor = NumberZeroButton,
            contentColor = ButtonText)
        {
            Text(
                "0",
                fontSize = 30.sp,
            )
        }

        FloatingActionButton(
            // Updates the viewText depending on initial values; otherwise it appends
            onClick = {
                if (viewText != author && viewText != "0") viewTextOnChange(viewText +".");
                else viewTextOnChange(".");
            },
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .padding(7.dp)
                .size(65.dp),
            containerColor = NumberButton,
            contentColor = ButtonText)
        {
            Text(
                ".",
                fontSize = 30.sp,
            )
        }

        FloatingActionButton(
            // Updates the viewText depending on initial values; otherwise it appends
            onClick = {
                if (viewText != author && viewText != "0") viewTextOnChange(viewText +"sqrt(");
                else viewTextOnChange("sqrt(");
            },
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .padding(7.dp)
                .size(65.dp),
            containerColor = GoldButton,
            contentColor = ButtonText)
        {
            Image(
                painter = painterResource(id = R.drawable.squareroot),
                contentDescription = "sqrt",
                contentScale = ContentScale.Inside,
                modifier = Modifier
                    .size(100.dp) // Set the initial size
                    .scale(1.75f))
        }

        // This is the equals or evaluate button
        FloatingActionButton(
            // Evaluates the viewText and renders it in the sumText
            onClick = {
                try {
                    // Covert the expression to PN
                    val expr_pn = toPrefix(viewText, showToast);

                    val expr_value = evaluatePN(expr_pn, showToast)

                    // Truncates the result to 12 digits to fit the Summation Box
                    val result = truncateString(expr_value.toString(),12)
                    if (result.toDouble().isNaN()) {
                        sumTextOnChange(result);
                        showToast("Evaluation is NaN. Error in expression.")
                        throw Exception("Evaluation is NaN. Error in expression.")
                    }
                    else sumTextOnChange(result);
                    System.out.println("This is the result: $result")
                }
                // Does nothing except log the error
                catch (e: Exception) {
                    System.out.println("Error: $e");
                }
            },
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .padding(7.dp)
                .size(65.dp),
            containerColor = GoldButton,
            contentColor = ButtonText)
        {
            Text(
                "=",
                fontSize = 30.sp,
            )
        }
    }
}

/**
 * This is the truncate string function
 * This is needed to ensure the view box does not overflow
 * @param text This is string to truncate
 * @param length This is length to truncate string to
 * @return substring string of the original text
 */
fun truncateString(text: String, length: Int): String {
    if (text.length > length) return text.substring(0,length)
    else return text
}

/**
 * This is the to prefix function
 * This is needed to ensure the expression can be encoded as PN
 * @param expression This is string to encode
 * @param showToast This is the callback function for showing Toasts
 * @return Polish Notation string of the original expression
 */
fun toPrefix(expression: String,
             showToast: ((String) -> Unit) = {}): String {
    // Variables
    val operators = Stack<Char>()
    val operands = Stack<String>()
    // Dictionaries of operators
    val precedence = mapOf(
        '+' to 1, '-' to 1,
        '*' to 2, '/' to 2,
        '^' to 3,
        's' to 4, 'c' to 4, 't' to 4, 'o' to 4,
        'l' to 5, 'g' to 5,
        'n' to 6,
    )
    // Initial words and number character arrays
    var word = emptyArray<Char>()
    var number = emptyArray<Char>()
    // Crawl through the expression from end to start
    for (i in expression.length - 1 downTo 0) {
        // Let's get the current character and adjacent characters
        var c = expression[i]
        var nextc: Char? = null
        var prevc: Char? = null
        // Adjacent characters initialization
        if (i - 1 >= 0) nextc = expression[i - 1]
        if (i + 1 <= expression.length - 1) prevc = expression[i + 1]

        // Handle special string functions
        if (Character.isLetter(c)) {
            // Add the character to the word
            word += c
            // If there are more letters, go to next iteration
            if (nextc != null && Character.isLetter(nextc)) {
                continue;
            }
            // Otherwise we capture the word, encode it, and add to operators
            else {
                var nc = '0';
                when (word.reversedArray().joinToString("")) {
                    "sin" -> nc = 's'
                    "cos" -> nc = 'c'
                    "tan" -> nc = 't'
                    "cot" -> nc = 'o'
                    "ln" -> nc = 'l'
                    "log" -> nc = 'g'
                    "sqrt" -> nc = 'q'
                }
                word = emptyArray<Char>() // reset word array
                operators.push(nc)
            }
        // Capture numbers
        } else if (Character.isDigit(c) || c == '.') {
            // Add the character to the number
            number += c
            // If there are more digits, go to next iteration
            if (nextc != null && (Character.isDigit(nextc) || nextc == '.')) continue
            // Otherwise we capture the number, encode it, and add to operands
            else {
                operands.push("'" + number.joinToString("") + "'")
                number = emptyArray<Char>()
            }
        // Bracket cases
        } else if (c == '}') {
            operators.push(c)
        } else if (c == '{') {
            while (!operators.isEmpty() && operators.peek() != '}') {
                evalOperator(operators, operands)
            }
            // Handle errors where no matching close bracket
            try {
                operators.pop()
            }
            catch (e: Exception) {
                if (operators.isEmpty()) {
                    showError(expression, "bracket", i, showToast=showToast)
                    break;
                }
            }
        // Parenthesis cases
        } else if (c == ')') {
            operators.push(c)
        } else if (c == '(') {
            while (!operators.isEmpty() && operators.peek() != ')') {
                evalOperator(operators, operands)
            }
            // Handle errors where no matching close bracket
            try {
                operators.pop()
            }
            catch (e: Exception) {
                if (operators.isEmpty()) {
                    showError(expression, "parenthesis", i, showToast=showToast)
                    break;
                }
            }
        // Handle Operators
        } else if (precedence.containsKey(c)) {
            // Unary case checks for negative sign
            // Start of expression case
            if (c == '-' && nextc == null) c = 'n'
            // Check next character to see if it is unary
            else if (c == '-' && precedence.containsKey(nextc)
                || (c == '-' && (nextc == '(' || nextc == '{'))) c = 'n'
            // Handle the rest of the operators based on precedence
            while (!operators.isEmpty() && precedence.getOrDefault(
                    operators.peek(),
                    0
                ) > precedence[c]!!
            ) {
                // Break out if no operands
                if (operands.isEmpty()) break;
                // Process operators if there are some operands
                else evalOperator(operators, operands)
            }
            // Push the operator to the stack
            operators.push(c)
        }
    }
    // Evaluate the remaining operators
    while (!operators.isEmpty()) {
        evalOperator(operators, operands)
    }
    // Return the coded string
    return operands.pop()
}

/**
 * This is the to evalOperator function
 * This will determine if an operator is unary or binary
 * @param operators This is the operators stack to use
 * @param operands This is the operands stack to use
 * @param index This is the index of the error
 * @param showToast This is the showToast callback
 */
fun showError(expression: String, charType: String, index: Int,
              showToast: ((String) -> Unit)) {
    // Variables
    var startCount = ""
    var endCount = ""
    var message = ""

    // Parse the type and setup the variables
    when (charType) {
        "parenthesis" -> {
            startCount = expression.filter { it == '(' }
            endCount = expression.filter { it == ')' }
        }
        "bracket" -> {
            startCount = expression.filter { it == '{' }
            endCount = expression.filter { it == '}' }
        }
    }
    // Setup the message
    var errorIndex = 0
    if (startCount.length > endCount.length) {
        errorIndex = startCount.length - endCount.length
        message = "${charType} error at index ${index} of expression"
    }
    else {
        errorIndex = endCount.length - startCount.length
        message = "${charType} error at index ${index} of expression"
    }
    // Show the toast
    showToast(message)
}


/**
 * This is the to evalOperator function
 * This will determine if an operator is unary or binary
 * @param operators This is the operators stack to use
 * @param operands This is the operands stack to use
 */
fun evalOperator(operators: Stack<Char>, operands: Stack<String>) {
    // Sanity checks of stacks
    if (!operators.isEmpty() && !operands.isEmpty()) {
        // List of uniary operators
        val unaryOps = listOf('s', 'c', 't', 'o', 'l', 'g')
        val unaryChecks = listOf('(', ')', '{', '}', '+', '-', '*', '/', '^')
        var unaryType: Boolean = false
        var unaryTest: Boolean = false

        // Check for negative unary case
        unaryType = unaryOps.contains(operators.peek())
        // Matches potential unary type, let's confirm
        if (unaryType) {
            // Explicit unary sizes
            if (operands.size == 1 && operators.size == 1) {
                unaryType = true;
                unaryTest = true
            // Check for unary based on next character
            } else {
                val op = operators.pop()
                if (!operators.isEmpty()) {
                    unaryTest = !unaryChecks.contains(operators.peek())
                } else unaryTest = true
                operators.push(op)
            }
        }
        // Addition ending parenthesis and bracket handling
        if (operators.peek() != '}' && operators.peek() != ')') {
            // Unary cases
            if ((unaryType && unaryTest) || operands.size == 1) {
                val op1 = operands.pop()
                var op = operators.pop()
                operands.push(op.toString() + op1)
            }
            else if ((unaryType && unaryTest) || operands.size == 0) {
                var op = operators.pop()
                operands.push(op.toString())
            }
            // Clear binary cases
            else {
                val op1 = operands.pop()
                val op2 = operands.pop()
                val op = operators.pop()
                operands.push(op.toString() + op1 + op2)
            }
        }
        // Pop off the superfluous operator
        else operators.pop()
    }
}

/**
 * This is the to decode and evaluate polish notation expression
 * This is needed to ensure the expression can be decoded and evaluated
 * @param prefix This is string to decode and evaluate
 * @param showToast This is the showToast callback
 * @return evaluated value for decoded expression
 */
fun evaluatePN(prefix: String,
               showToast: (String) -> Unit): Double {
    val stack = Stack<Double>()
    val unaryOps = listOf('s', 'c', 't', 'o', 'l', 'g', 'n', 'q')
    var number = emptyArray<Char>()
    var numstart: Boolean = false
    // Iterate through the PN from end to start
    for (i in prefix.length - 1 downTo 0) {
        // Get the first character
        val c = prefix[i]
        // Check for operand start
        if (c == '\'' && !numstart) {
            numstart = true
            continue
        }
        // Already working on operand, so capture the rest
        else if (numstart) {
            println("This is the current number: ${number.joinToString("")}")
            println("This is the current char: ${c.toString()}")
            if (c == '\'') {
                stack.push(number.joinToString("").toDouble())
                //println("This is the number: ${number.joinToString("")}")
                numstart = false
                number = emptyArray<Char>()
            }
            else number += c
        }
        // Handle unary operations
        else if (unaryOps.contains(c)) {
            try {
                val a = stack.pop()
                when (c) {
                    's' -> stack.push(sin(a))
                    'c' -> stack.push(cos(a))
                    't' -> stack.push(tan(a))
                    'o' -> stack.push(tanh(a))
                    'l' -> stack.push(ln(a))
                    'g' -> stack.push(log10(a))
                    'q' -> stack.push(sqrt(a))
                    'n' -> stack.push(a.toDouble() * -1)
                }
                println("This is the current value of evaluation: ${stack.peek()}")
                println("This is the current evaluation stack: " +
                        "${stack.toArray().joinToString(",")}")
            // Handle exceptions
            } catch (e: Exception) {
                showToast("There is an invalid operation specified")
                println("This is the invalid operation exception: ${e.toString()}")
                throw java.lang.Exception("There is an invalid operation specified")
            }

        }
        // Handle binary operators
        else {
            try {
                val a = stack.pop()
                val b = stack.pop()
                when (c) {
                    '+' -> stack.push(a + b)
                    '-' -> stack.push(a - b)
                    '*' -> stack.push(a * b)
                    '/' -> stack.push(a / b)
                    '^' -> stack.push(a.toDouble().pow(b.toDouble()))
                }
                println("This is the current value of evaluation: ${stack.peek()}")
            // Handle exceptions
            } catch (e: Exception) {
                showToast("There is an invalid operation specified")
                throw java.lang.Exception("There is an invalid operation specified")
            }

        }
    }
    return stack.pop()
}

/**
 * This is the Main preview function composable
 * This is what loads when live-preview is enabled
 * @param showBackground This is set to true for default background color
 */
@Preview(showBackground = true)
@Composable
fun CalculatorPreview() {
    CalculatorAppTheme {
        // Only the default style is implemented currently
        Calculator("standard")
    }
}