package com.example.simplecalculator

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import kotlin.math.floor

class MainActivity : AppCompatActivity() {

    // ── Views ────────────────────────────────────────────────────────────────
    private lateinit var tvResult: TextView
    private lateinit var tvExpression: TextView

    // ── Calculator State ─────────────────────────────────────────────────────
    private var currentInput  = ""
    private var firstOperand  = 0.0
    private var operator      = ""
    private var justEvaluated = false

    // ── Lifecycle ────────────────────────────────────────────────────────────
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvResult     = findViewById(R.id.tvResult)
        tvExpression = findViewById(R.id.tvExpression)

        setupButtons()
    }

    // ── Button Setup ─────────────────────────────────────────────────────────
    private fun setupButtons() {

        // Digit buttons
        findViewById<MaterialButton>(R.id.btn0).setOnClickListener { onDigit("0") }
        findViewById<MaterialButton>(R.id.btn1).setOnClickListener { onDigit("1") }
        findViewById<MaterialButton>(R.id.btn2).setOnClickListener { onDigit("2") }
        findViewById<MaterialButton>(R.id.btn3).setOnClickListener { onDigit("3") }
        findViewById<MaterialButton>(R.id.btn4).setOnClickListener { onDigit("4") }
        findViewById<MaterialButton>(R.id.btn5).setOnClickListener { onDigit("5") }
        findViewById<MaterialButton>(R.id.btn6).setOnClickListener { onDigit("6") }
        findViewById<MaterialButton>(R.id.btn7).setOnClickListener { onDigit("7") }
        findViewById<MaterialButton>(R.id.btn8).setOnClickListener { onDigit("8") }
        findViewById<MaterialButton>(R.id.btn9).setOnClickListener { onDigit("9") }
        findViewById<MaterialButton>(R.id.btnDot).setOnClickListener { onDigit(".") }

        // Operator buttons
        findViewById<MaterialButton>(R.id.btnAdd).setOnClickListener      { onOperator("+") }
        findViewById<MaterialButton>(R.id.btnSubtract).setOnClickListener { onOperator("−") }
        findViewById<MaterialButton>(R.id.btnMultiply).setOnClickListener { onOperator("×") }
        findViewById<MaterialButton>(R.id.btnDivide).setOnClickListener   { onOperator("÷") }

        // Special buttons
        findViewById<MaterialButton>(R.id.btnEquals).setOnClickListener     { onEquals()     }
        findViewById<MaterialButton>(R.id.btnClear).setOnClickListener      { onClear()      }
        findViewById<MaterialButton>(R.id.btnToggleSign).setOnClickListener { onToggleSign() }
        findViewById<MaterialButton>(R.id.btnPercent).setOnClickListener    { onPercent()    }
    }

    // ── Handlers ─────────────────────────────────────────────────────────────

    private fun onDigit(digit: String) {
        if (justEvaluated) {
            currentInput  = ""
            justEvaluated = false
        }
        if (digit == "." && currentInput.contains(".")) return
        if (digit == "." && currentInput.isEmpty()) currentInput = "0"
        if (currentInput.length >= 12) return

        currentInput += digit
        tvResult.text = formatDisplay(currentInput)
    }

    private fun onOperator(op: String) {
        justEvaluated = false

        if (currentInput.isEmpty() && operator.isNotEmpty()) {
            operator = op
            tvExpression.text = "${formatNumber(firstOperand)} $op"
            return
        }

        if (currentInput.isNotEmpty() && operator.isNotEmpty()) {
            firstOperand = compute(firstOperand, operator, currentInput.toDoubleOrNull() ?: 0.0)
            tvResult.text = formatNumber(firstOperand)
        } else if (currentInput.isNotEmpty()) {
            firstOperand = currentInput.toDoubleOrNull() ?: 0.0
        }

        operator     = op
        currentInput = ""
        tvExpression.text = "${formatNumber(firstOperand)} $op"
    }

    private fun onEquals() {
        if (operator.isEmpty() || currentInput.isEmpty()) return

        val second     = currentInput.toDoubleOrNull() ?: return
        val expression = "${formatNumber(firstOperand)} $operator ${formatNumber(second)}"
        val result     = compute(firstOperand, operator, second)

        tvExpression.text = "$expression ="
        tvResult.text     = formatNumber(result)

        firstOperand  = result
        currentInput  = ""
        operator      = ""
        justEvaluated = true
    }

    private fun onClear() {
        currentInput      = ""
        firstOperand      = 0.0
        operator          = ""
        justEvaluated     = false
        tvResult.text     = "0"
        tvExpression.text = ""
    }

    private fun onToggleSign() {
        if (currentInput.isEmpty() || currentInput == "0") return
        currentInput = if (currentInput.startsWith("-"))
            currentInput.substring(1)
        else
            "-$currentInput"
        tvResult.text = formatDisplay(currentInput)
    }

    private fun onPercent() {
        val value = currentInput.toDoubleOrNull() ?: return
        currentInput  = trimNumber(value / 100.0)
        tvResult.text = formatDisplay(currentInput)
    }

    // ── Math ─────────────────────────────────────────────────────────────────

    private fun compute(a: Double, op: String, b: Double): Double = when (op) {
        "+"  -> a + b
        "−"  -> a - b
        "×"  -> a * b
        "÷"  -> if (b == 0.0) { showError(); Double.NaN } else a / b
        else -> b
    }

    // ── Display Helpers ───────────────────────────────────────────────────────

    private fun formatDisplay(raw: String): String {
        if (raw.isEmpty() || raw == "-") return raw.ifEmpty { "0" }
        val d = raw.toDoubleOrNull() ?: return raw
        val formatted = formatNumber(d)
        return if (raw.endsWith(".")) "$formatted." else formatted
    }

    private fun formatNumber(value: Double): String {
        if (value.isNaN())      return "Error"
        if (value.isInfinite()) return if (value > 0) "∞" else "-∞"
        return if (value == floor(value) && !value.isInfinite())
            value.toLong().toString()
        else
            "%.10g".format(value).trimEnd('0').trimEnd('.')
    }

    private fun trimNumber(value: Double): String =
        if (value == floor(value)) value.toLong().toString()
        else value.toBigDecimal().stripTrailingZeros().toPlainString()

    private fun showError() {
        tvResult.text     = "Error"
        tvExpression.text = "Cannot divide by zero"
        currentInput      = ""
        operator          = ""
        justEvaluated     = true
    }
}