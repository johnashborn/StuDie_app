package com.example.studie;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class solution extends AppCompatActivity {

    private EditText equationText;
    private TextView solutionText;
    private ImageButton restartButton;
    private ImageButton solveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_solution);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        equationText = findViewById(R.id.equationText);
        solutionText = findViewById(R.id.solutionText);
        restartButton = findViewById(R.id.restart);
        solveButton = findViewById(R.id.solveButton);

        // Get the recognized text from camera
        String mathProblem = getIntent().getStringExtra("math_problem");
        Log.d("SOLUTION", "Original text: " + mathProblem);

        if (mathProblem == null || mathProblem.trim().isEmpty()) {
            equationText.setText("Enter your math problem here...");
            solutionText.setText("Please enter a math problem above and it will be solved automatically");
            setupEditTextListener();
            setupRestartButton();
            setupSolveButton();
            return;
        }

        // Clean and process the OCR text
        String cleanedExpression = cleanOCRText(mathProblem);
        Log.d("SOLUTION", "Cleaned text: " + cleanedExpression);

        // Display the cleaned expression in the editable field
        equationText.setText(cleanedExpression);

        // Solve the initial expression
        solveCurrentExpression();

        // Set up listeners for editing, restart, and solve
        setupEditTextListener();
        setupRestartButton();
        setupSolveButton();
    }

    private void setupEditTextListener() {
        equationText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not needed
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Solve the expression after user stops typing (with a small delay)
                equationText.removeCallbacks(solveRunnable);
                equationText.postDelayed(solveRunnable, 1000); // 1 second delay
            }
        });
    }

    private final Runnable solveRunnable = new Runnable() {
        @Override
        public void run() {
            solveCurrentExpression();
        }
    };

    private void setupRestartButton() {
        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear the equation text and solution
                equationText.setText("");
                solutionText.setText("Enter a math problem above to see the solution...");
                equationText.requestFocus();
            }
        });
    }

    private void setupSolveButton() {
        solveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                solveCurrentExpression();
            }
        });
    }

    private void solveCurrentExpression() {
        String currentExpression = equationText.getText().toString().trim();

        if (currentExpression.isEmpty()) {
            solutionText.setText("Enter a math problem above to see the solution...");
            return;
        }

        if (currentExpression.equals("Enter your math problem here...")) {
            solutionText.setText("Please enter a math problem above and it will be solved automatically");
            return;
        }

        // Try to solve the expression with step-by-step explanation
        SolutionResult solution = solveMathExpressionWithSteps(currentExpression);
        solutionText.setText(solution.getFormattedSolution());
    }

    // Fraction class for handling fraction operations
    private static class Fraction {
        private long numerator;
        private long denominator;

        public Fraction(long numerator, long denominator) {
            if (denominator == 0) throw new IllegalArgumentException("Denominator cannot be zero");
            this.numerator = numerator;
            this.denominator = denominator;
            simplify();
        }

        public Fraction(double decimal) {
            // Convert decimal to fraction
            long precision = 10000;
            this.numerator = Math.round(decimal * precision);
            this.denominator = precision;
            simplify();
        }

        private void simplify() {
            long gcd = gcd(Math.abs(numerator), Math.abs(denominator));
            numerator /= gcd;
            denominator /= gcd;

            if (denominator < 0) {
                numerator = -numerator;
                denominator = -denominator;
            }
        }

        private long gcd(long a, long b) {
            return b == 0 ? a : gcd(b, a % b);
        }

        public Fraction add(Fraction other) {
            long newNum = this.numerator * other.denominator + other.numerator * this.denominator;
            long newDen = this.denominator * other.denominator;
            return new Fraction(newNum, newDen);
        }

        public Fraction subtract(Fraction other) {
            long newNum = this.numerator * other.denominator - other.numerator * this.denominator;
            long newDen = this.denominator * other.denominator;
            return new Fraction(newNum, newDen);
        }

        public Fraction multiply(Fraction other) {
            return new Fraction(this.numerator * other.numerator, this.denominator * other.denominator);
        }

        public Fraction divide(Fraction other) {
            if (other.numerator == 0) throw new ArithmeticException("Division by zero");
            return new Fraction(this.numerator * other.denominator, this.denominator * other.numerator);
        }

        public double toDouble() {
            return (double) numerator / denominator;
        }

        @Override
        public String toString() {
            if (denominator == 1) return String.valueOf(numerator);
            return numerator + "/" + denominator;
        }
    }

    // Inner class to hold solution and steps
    private static class SolutionResult {
        private String finalAnswer;
        private List<String> steps;
        private boolean isError;
        private String errorMessage;

        public SolutionResult() {
            this.steps = new ArrayList<>();
            this.isError = false;
        }

        public void addStep(String step) {
            steps.add(step);
        }

        public void setFinalAnswer(String answer) {
            this.finalAnswer = answer;
        }

        public void setError(String message) {
            this.isError = true;
            this.errorMessage = message;
        }

        public String getFormattedSolution() {
            if (isError) {
                return "Error: " + errorMessage;
            }

            StringBuilder sb = new StringBuilder();

            if (!steps.isEmpty()) {
                sb.append("Step-by-step solution:\n\n");
                for (int i = 0; i < steps.size(); i++) {
                    sb.append("Step ").append(i + 1).append(": ").append(steps.get(i)).append("\n");
                }
                sb.append("\n");
            }

            sb.append("Final Answer: ").append(finalAnswer);
            return sb.toString();
        }
    }

    private String cleanOCRText(String rawText) {
        if (rawText == null) return "";

        String cleaned = rawText.trim();

        // Remove newlines and extra spaces
        cleaned = cleaned.replaceAll("\\s+", " ");

        // Common OCR corrections
        cleaned = cleaned.replaceAll("[×X]", "*");
        cleaned = cleaned.replaceAll("÷", "/");
        cleaned = cleaned.replaceAll("−", "-");
        cleaned = cleaned.replaceAll("[{}\\[\\]]", "()");
        cleaned = cleaned.replaceAll("²", "^2");
        cleaned = cleaned.replaceAll("³", "^3");
        cleaned = cleaned.replaceAll("⁴", "^4");
        cleaned = cleaned.replaceAll("⁵", "^5");

        // Handle square root symbols
        cleaned = cleaned.replaceAll("√", "sqrt");
        cleaned = cleaned.replaceAll("∛", "cbrt");

        // Handle power notation
        cleaned = cleaned.replaceAll("\\^", "^");

        // Handle trigonometric functions
        cleaned = cleaned.replaceAll("sin", "sin");
        cleaned = cleaned.replaceAll("cos", "cos");
        cleaned = cleaned.replaceAll("tan", "tan");

        // Remove common OCR artifacts but keep math functions and letters
        // Keep: numbers, operators, parentheses, spaces, equals, exponent, and letter sequences for functions
        cleaned = cleaned.replaceAll("[^0-9+\\-*/().\\s=\\^a-zA-Z]", "");

        // Handle equations
        if (cleaned.contains("=")) {
            String[] parts = cleaned.split("=");
            if (parts.length > 1) {
                if (parts[1].trim().isEmpty() || parts[1].trim().equals("?")) {
                    cleaned = parts[0].trim();
                }
            }
        }

        // Remove trailing operators
        cleaned = cleaned.replaceAll("[+\\-*/]$", "");
        return cleaned.trim();
    }

    private SolutionResult solveMathExpressionWithSteps(String expression) {
        SolutionResult result = new SolutionResult();


        if (!expression.contains("=") && expression.matches(".*[a-zA-Z].*")) {
            return simplifyExpression(expression);
        }

        if (expression == null || expression.trim().isEmpty()) {
            result.setError("No valid expression found");
            return result;
        }

        try {
            // Check for equations first
            if (expression.contains("=")) {
                // Check for quadratic equations
                if (isQuadraticEquation(expression)) {
                    return solveQuadraticEquation(expression);
                }

                // Check for linear equations
                if (isLinearEquation(expression)) {
                    return solveLinearEquation(expression);
                }
            }

            // Check for square root expressions
            if (containsSquareRoot(expression)) {
                return solveSquareRootExpression(expression);
            }

            // Check for trigonometric expressions
            if (containsTrigFunctions(expression)) {
                return solveTrigonometricExpression(expression);
            }

            // Check for fraction expressions
            if (containsFractions(expression)) {
                return solveFractionExpression(expression);
            }

            // Check if it's basic arithmetic (including expressions with parentheses)
            if (isBasicArithmetic(expression)) {
                return solveBasicArithmeticWithSteps(expression);
            }

            result.setError("Could not solve: " + expression);
            return result;

        } catch (Exception e) {
            Log.e("SOLUTION", "Error solving expression: " + expression, e);
            result.setError("Error solving expression: " + e.getMessage());
            return result;
        }
    }

    private boolean isQuadraticEquation(String expression) {
        return expression.contains("=") &&
                (expression.matches(".*x\\^2.*") ||
                        expression.matches(".*x2.*") ||
                        expression.contains("x²"));
    }

    private boolean isLinearEquation(String expression) {
        return expression.contains("=") &&
                expression.matches(".*[a-zA-Z].*") &&
                !expression.matches(".*x\\^2.*") &&
                !expression.matches(".*x2.*") &&
                !expression.contains("x²");
    }

    private boolean containsTrigFunctions(String expression) {
        return expression.contains("sin") ||
                expression.contains("cos") ||
                expression.contains("tan");
    }

    private boolean containsSquareRoot(String expression) {
        return expression.contains("sqrt") ||
                expression.contains("√") ||
                expression.matches(".*\\bsqrt\\b.*");
    }

    private boolean containsFractions(String expression) {
        Pattern fractionPattern = Pattern.compile("\\d+/\\d+");
        Matcher matcher = fractionPattern.matcher(expression);

        int fractionCount = 0;
        while (matcher.find()) {
            fractionCount++;
        }

        if (fractionCount > 1) {
            return true;
        }

        if (fractionCount == 1) {
            String withoutFractions = expression.replaceAll("\\d+/\\d+", "X");
            return !withoutFractions.matches(".*[+\\-*].*");
        }

        return false;
    }

    private boolean isBasicArithmetic(String expression) {
        // Updated to handle square roots and more complex expressions
        return expression.matches("^[0-9+\\-*/().\\s\\^a-zA-Z]+$") && !expression.contains("=");
    }

    private SolutionResult solveSquareRootExpression(String expression) {
        SolutionResult result = new SolutionResult();

        try {
            result.addStep("Identified square root expression: " + expression);

            // Handle different square root patterns
            String currentExpression = expression;

            // Pattern 1: sqrt(number) - exact function call
            Pattern sqrtPattern1 = Pattern.compile("sqrt\\((\\d+(?:\\.\\d+)?)\\)");
            Matcher matcher1 = sqrtPattern1.matcher(currentExpression);

            while (matcher1.find()) {
                String numberStr = matcher1.group(1);
                double number = Double.parseDouble(numberStr);
                double sqrtResult = Math.sqrt(number);

                result.addStep("Calculate √" + numberStr + " = " + formatNumber(sqrtResult));

                currentExpression = currentExpression.replace(matcher1.group(0), formatNumber(sqrtResult));
                matcher1 = sqrtPattern1.matcher(currentExpression);
            }

            // Pattern 2: sqrt followed by number (without parentheses)
            Pattern sqrtPattern2 = Pattern.compile("sqrt\\s*(\\d+(?:\\.\\d+)?)");
            Matcher matcher2 = sqrtPattern2.matcher(currentExpression);

            while (matcher2.find()) {
                String numberStr = matcher2.group(1);
                double number = Double.parseDouble(numberStr);
                double sqrtResult = Math.sqrt(number);

                result.addStep("Calculate √" + numberStr + " = " + formatNumber(sqrtResult));

                currentExpression = currentExpression.replace(matcher2.group(0), formatNumber(sqrtResult));
                matcher2 = sqrtPattern2.matcher(currentExpression);
            }

            // Pattern 3: Handle expressions like "2*sqrt(4)" or "sqrt(9)+5"
            Pattern sqrtPattern3 = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*\\*\\s*sqrt\\((\\d+(?:\\.\\d+)?)\\)");
            Matcher matcher3 = sqrtPattern3.matcher(currentExpression);

            while (matcher3.find()) {
                String multiplier = matcher3.group(1);
                String numberStr = matcher3.group(2);
                double mult = Double.parseDouble(multiplier);
                double number = Double.parseDouble(numberStr);
                double sqrtResult = Math.sqrt(number);
                double finalResult = mult * sqrtResult;

                result.addStep("Calculate " + multiplier + " × √" + numberStr + " = " + multiplier + " × " + formatNumber(sqrtResult) + " = " + formatNumber(finalResult));

                currentExpression = currentExpression.replace(matcher3.group(0), formatNumber(finalResult));
                matcher3 = sqrtPattern3.matcher(currentExpression);
            }

            // If there are remaining operations, solve them
            if (currentExpression.matches("^[0-9+\\-*/().\\s\\^]+$")) {
                try {
                    Expression e = new ExpressionBuilder(currentExpression).build();
                    double finalResult = e.evaluate();

                    if (!currentExpression.equals(formatNumber(finalResult))) {
                        result.addStep("Final calculation: " + currentExpression + " = " + formatNumber(finalResult));
                    }

                    result.setFinalAnswer(formatNumber(finalResult));
                } catch (Exception e) {
                    result.setFinalAnswer(currentExpression);
                }
            } else {
                result.setFinalAnswer(currentExpression);
            }

            return result;

        } catch (Exception e) {
            result.setError("Failed to solve square root expression: " + e.getMessage());
            return result;
        }
    }

    private SolutionResult solveBasicArithmeticWithSteps(String expression) {
        SolutionResult result = new SolutionResult();

        try {
            result.addStep("Original expression: " + expression);

            // First, validate the expression
            if (!isValidMathExpression(expression)) {
                result.setError("Invalid mathematical expression");
                return result;
            }

            String currentExpression = expression;

            // Handle parentheses first - step by step
            while (currentExpression.contains("(")) {
                currentExpression = solveParenthesesStepByStep(currentExpression, result);
            }

            // Handle exponents
            currentExpression = solveExponents(currentExpression, result);

            // Handle multiplication and division (left to right)
            currentExpression = solveMultiplicationDivision(currentExpression, result);

            // Handle addition and subtraction (left to right)
            currentExpression = solveAdditionSubtraction(currentExpression, result);

            // Final evaluation to ensure correctness
            try {
                Expression e = new ExpressionBuilder(expression).build();
                double finalResult = e.evaluate();

                if (Double.isNaN(finalResult) || Double.isInfinite(finalResult)) {
                    result.setError("Invalid mathematical result");
                    return result;
                }

                result.setFinalAnswer(formatNumber(finalResult));
            } catch (Exception e) {
                // If exp4j fails, try to use our step-by-step result
                result.setFinalAnswer(currentExpression);
            }

            return result;

        } catch (Exception e) {
            result.setError("Failed to solve arithmetic expression: " + e.getMessage());
            return result;
        }
    }

    private String solveParenthesesStepByStep(String expression, SolutionResult result) {
        // Find the innermost parentheses
        Pattern pattern = Pattern.compile("\\(([^()]+)\\)");
        Matcher matcher = pattern.matcher(expression);

        if (matcher.find()) {
            String innerExpression = matcher.group(1);
            result.addStep("Solve parentheses: (" + innerExpression + ")");

            try {
                Expression e = new ExpressionBuilder(innerExpression).build();
                double innerResult = e.evaluate();

                String resultStr = formatNumber(innerResult);
                result.addStep("(" + innerExpression + ") = " + resultStr);

                return expression.replace(matcher.group(0), resultStr);
            } catch (Exception e) {
                // If exp4j fails, try manual calculation
                try {
                    double manualResult = evaluateSimpleExpression(innerExpression);
                    String resultStr = formatNumber(manualResult);
                    result.addStep("(" + innerExpression + ") = " + resultStr);
                    return expression.replace(matcher.group(0), resultStr);
                } catch (Exception ex) {
                    return expression;
                }
            }
        }

        return expression;
    }

    private double evaluateSimpleExpression(String expression) throws Exception {
        // Simple left-to-right evaluation for basic arithmetic
        expression = expression.trim();

        // Handle single number
        if (expression.matches("^\\d+(\\.\\d+)?$")) {
            return Double.parseDouble(expression);
        }

        // For more complex expressions, use exp4j
        Expression e = new ExpressionBuilder(expression).build();
        return e.evaluate();
    }

    private String formatNumber(double number) {
        if (Math.abs(number - Math.round(number)) < 1e-9) {
            return String.valueOf(Math.round(number));
        } else {
            return String.format("%.4f", number);
        }
    }

    private String formatCoeff(double coeff) {
        if (Math.abs(coeff - 1) < 1e-9) return "";
        if (Math.abs(coeff - (-1)) < 1e-9) return "-";
        if (Math.abs(coeff - Math.round(coeff)) < 1e-9) {
            return String.valueOf(Math.round(coeff));
        } else {
            return String.format("%.2f", coeff);
        }
    }

    private boolean isValidMathExpression(String expression) {
        if (expression == null || expression.trim().isEmpty()) return false;

        int parentheses = 0;
        for (char c : expression.toCharArray()) {
            if (c == '(') parentheses++;
            else if (c == ')') parentheses--;
            if (parentheses < 0) return false;
        }
        return parentheses == 0;
    }

    private SolutionResult solveQuadraticEquation(String expression) {
        SolutionResult result = new SolutionResult();

        try {
            result.addStep("Identified quadratic equation: " + expression);

            // Parse quadratic equation ax² + bx + c = 0
            String[] parts = expression.split("=");
            String leftSide = parts[0].trim();
            String rightSide = parts.length > 1 ? parts[1].trim() : "0";

            // Move right side to left side
            double rightValue = 0;
            if (!rightSide.equals("0")) {
                try {
                    rightValue = Double.parseDouble(rightSide);
                } catch (NumberFormatException e) {
                    // If right side is not a simple number, handle it differently
                    rightValue = 0;
                }
            }

            // Extract coefficients a, b, c
            double a = 0, b = 0, c = -rightValue; // Start with negative of right side

            // Normalize the expression for easier parsing
            String normalizedLeft = leftSide.replaceAll("\\s+", "");
            normalizedLeft = normalizedLeft.replace("x²", "x^2");
            normalizedLeft = normalizedLeft.replace("x2", "x^2");

            // Add '+' at the beginning if it doesn't start with '+' or '-'
            if (!normalizedLeft.startsWith("+") && !normalizedLeft.startsWith("-")) {
                normalizedLeft = "+" + normalizedLeft;
            }

            // Parse x² coefficient
            Pattern x2Pattern = Pattern.compile("([+-]?\\d*)x\\^2");
            Matcher x2Matcher = x2Pattern.matcher(normalizedLeft);
            if (x2Matcher.find()) {
                String coeff = x2Matcher.group(1);
                if (coeff.equals("+") || coeff.isEmpty()) a = 1;
                else if (coeff.equals("-")) a = -1;
                else a = Double.parseDouble(coeff);
            }

            // Parse x coefficient (but not x^2)
            Pattern xPattern = Pattern.compile("([+-]?\\d*)x(?!\\^2)");
            Matcher xMatcher = xPattern.matcher(normalizedLeft);
            if (xMatcher.find()) {
                String coeff = xMatcher.group(1);
                if (coeff.equals("+") || coeff.isEmpty()) b = 1;
                else if (coeff.equals("-")) b = -1;
                else b = Double.parseDouble(coeff);
            }

            // Parse constant term
            String withoutX = normalizedLeft.replaceAll("([+-]?\\d*)x\\^?2?", "");
            Pattern constPattern = Pattern.compile("([+-]?\\d+)");
            Matcher constMatcher = constPattern.matcher(withoutX);
            if (constMatcher.find()) {
                c += Double.parseDouble(constMatcher.group(1));
            }

            result.addStep("Standard form: " + formatCoeff(a) + "x² + " + formatCoeff(b) + "x + " + c + " = 0");
            result.addStep("Coefficients: a = " + a + ", b = " + b + ", c = " + c);

            // Apply quadratic formula
            result.addStep("Using quadratic formula: x = (-b ± √(b² - 4ac)) / 2a");

            double discriminant = b * b - 4 * a * c;
            result.addStep("Discriminant = b² - 4ac = " + b + "² - 4(" + a + ")(" + c + ") = " + discriminant);

            if (discriminant > 0) {
                double sqrt = Math.sqrt(discriminant);
                double x1 = (-b + sqrt) / (2 * a);
                double x2 = (-b - sqrt) / (2 * a);

                result.addStep("Since discriminant > 0, there are two real solutions:");
                result.addStep("x₁ = (-" + b + " + √" + discriminant + ") / (2 × " + a + ") = " + formatNumber(x1));
                result.addStep("x₂ = (-" + b + " - √" + discriminant + ") / (2 × " + a + ") = " + formatNumber(x2));

                result.setFinalAnswer("x₁ = " + formatNumber(x1) + ", x₂ = " + formatNumber(x2));
            } else if (discriminant == 0) {
                double x = -b / (2 * a);
                result.addStep("Since discriminant = 0, there is one real solution:");
                result.addStep("x = -" + b + " / (2 × " + a + ") = " + formatNumber(x));
                result.setFinalAnswer("x = " + formatNumber(x));
            } else {
                result.addStep("Since discriminant < 0, there are no real solutions (complex solutions exist)");
                result.setFinalAnswer("No real solutions");
            }

            return result;

        } catch (Exception e) {
            result.setError("Failed to solve quadratic equation: " + e.getMessage());
            return result;
        }
    }

    private SolutionResult solveLinearEquation(String expression) {
        SolutionResult result = new SolutionResult();

        try {
            result.addStep("Identified linear equation: " + expression);

            String[] parts = expression.split("=");
            String leftSide = parts[0].trim();
            String rightSide = parts[1].trim();

            result.addStep("Left side: " + leftSide);
            result.addStep("Right side: " + rightSide);

            // Simple linear equation solver for ax + b = c
            // Extract coefficient of x and constant term
            double coeffX = 0, constLeft = 0, constRight = 0;

            // Parse left side
            Pattern xPattern = Pattern.compile("([+-]?\\d*)x");
            Matcher xMatcher = xPattern.matcher(leftSide);
            if (xMatcher.find()) {
                String coeff = xMatcher.group(1);
                if (coeff.isEmpty() || coeff.equals("+")) coeffX = 1;
                else if (coeff.equals("-")) coeffX = -1;
                else coeffX = Double.parseDouble(coeff);
            }

            // Parse constant on left side
            String leftWithoutX = leftSide.replaceAll("[+-]?\\d*x", "");
            if (!leftWithoutX.trim().isEmpty()) {
                try {
                    constLeft = Double.parseDouble(leftWithoutX.trim());
                } catch (NumberFormatException e) {
                    constLeft = 0;
                }
            }

            // Parse right side
            try {
                constRight = Double.parseDouble(rightSide);
            } catch (NumberFormatException e) {
                constRight = 0;
            }

            result.addStep("Equation in standard form: " + formatCoeff(coeffX) + "x + " + constLeft + " = " + constRight);

            // Solve for x
            result.addStep("Subtract " + constLeft + " from both sides:");
            double newRight = constRight - constLeft;
            result.addStep(formatCoeff(coeffX) + "x = " + newRight);

            if (coeffX != 0) {
                result.addStep("Divide both sides by " + coeffX + ":");
                double x = newRight / coeffX;
                result.addStep("x = " + newRight + " / " + coeffX + " = " + formatNumber(x));
                result.setFinalAnswer("x = " + formatNumber(x));
            } else {
                result.setFinalAnswer("No solution or infinite solutions");
            }

            return result;

        } catch (Exception e) {
            result.setError("Failed to solve linear equation: " + e.getMessage());
            return result;
        }
    }

    private SolutionResult solveTrigonometricExpression(String expression) {
        SolutionResult result = new SolutionResult();

        try {
            result.addStep("Identified trigonometric expression: " + expression);

            // Handle basic trigonometric evaluations
            if (expression.contains("sin")) {
                result.addStep("Evaluating sine function");
                return evaluateTrigFunction(expression, "sin", result);
            } else if (expression.contains("cos")) {
                result.addStep("Evaluating cosine function");
                return evaluateTrigFunction(expression, "cos", result);
            } else if (expression.contains("tan")) {
                result.addStep("Evaluating tangent function");
                return evaluateTrigFunction(expression, "tan", result);
            }

            result.setError("Unsupported trigonometric expression");
            return result;

        } catch (Exception e) {
            result.setError("Failed to solve trigonometric expression: " + e.getMessage());
            return result;
        }
    }

    private SolutionResult evaluateTrigFunction(String expression, String function, SolutionResult result) {
        try {
            // Extract angle from expression like sin(30) or cos(π/4)
            Pattern pattern = Pattern.compile(function + "\\((\\d+(?:\\.\\d+)?)\\)");
            Matcher matcher = pattern.matcher(expression);

            if (matcher.find()) {
                double angle = Double.parseDouble(matcher.group(1));
                result.addStep("Angle: " + angle + " degrees");

                // Convert to radians
                double radians = Math.toRadians(angle);
                result.addStep("Convert to radians: " + angle + "° = " + formatNumber(radians) + " radians");

                double value = 0;
                switch (function) {
                    case "sin":
                        value = Math.sin(radians);
                        result.addStep("sin(" + angle + "°) = " + formatNumber(value));
                        break;
                    case "cos":
                        value = Math.cos(radians);
                        result.addStep("cos(" + angle + "°) = " + formatNumber(value));
                        break;
                    case "tan":
                        value = Math.tan(radians);
                        result.addStep("tan(" + angle + "°) = " + formatNumber(value));
                        break;
                }

                result.setFinalAnswer(formatNumber(value));
                return result;
            }

            result.setError("Could not parse trigonometric function");
            return result;

        } catch (Exception e) {
            result.setError("Error evaluating trigonometric function: " + e.getMessage());
            return result;
        }
    }

    private SolutionResult solveFractionExpression(String expression) {
        SolutionResult result = new SolutionResult();

        try {
            result.addStep("Identified fraction expression: " + expression);

            // Find all fractions in the expression
            Pattern fractionPattern = Pattern.compile("(\\d+)/(\\d+)");
            Matcher matcher = fractionPattern.matcher(expression);

            List<Fraction> fractions = new ArrayList<>();
            List<String> operators = new ArrayList<>();

            String remaining = expression;

            // Parse fractions and operators
            while (matcher.find()) {
                int numerator = Integer.parseInt(matcher.group(1));
                int denominator = Integer.parseInt(matcher.group(2));
                fractions.add(new Fraction(numerator, denominator));

                // Find operator before this fraction (if any)
                String beforeFraction = remaining.substring(0, matcher.start());
                if (beforeFraction.contains("+")) {
                    operators.add("+");
                } else if (beforeFraction.contains("-")) {
                    operators.add("-");
                } else if (beforeFraction.contains("*")) {
                    operators.add("*");
                } else if (beforeFraction.contains("/") && !beforeFraction.endsWith("/")) {
                    operators.add("/");
                }

                remaining = remaining.substring(matcher.end());
            }

            if (fractions.isEmpty()) {
                result.setError("No fractions found in expression");
                return result;
            }

            // If only one fraction, just display it
            if (fractions.size() == 1) {
                result.addStep("Single fraction: " + fractions.get(0).toString());
                result.setFinalAnswer(fractions.get(0).toString());
                return result;
            }

            // Process multiple fractions
            result.addStep("Found " + fractions.size() + " fractions to process");

            Fraction resultFraction = fractions.get(0);
            result.addStep("Starting with: " + resultFraction.toString());

            for (int i = 1; i < fractions.size(); i++) {
                String operator = i-1 < operators.size() ? operators.get(i-1) : "+";
                Fraction nextFraction = fractions.get(i);

                result.addStep("Next operation: " + resultFraction.toString() + " " + operator + " " + nextFraction.toString());

                switch (operator) {
                    case "+":
                        resultFraction = resultFraction.add(nextFraction);
                        result.addStep("Addition result: " + resultFraction.toString());
                        break;
                    case "-":
                        resultFraction = resultFraction.subtract(nextFraction);
                        result.addStep("Subtraction result: " + resultFraction.toString());
                        break;
                    case "*":
                        resultFraction = resultFraction.multiply(nextFraction);
                        result.addStep("Multiplication result: " + resultFraction.toString());
                        break;
                    case "/":
                        resultFraction = resultFraction.divide(nextFraction);
                        result.addStep("Division result: " + resultFraction.toString());
                        break;
                    default:
                        resultFraction = resultFraction.add(nextFraction);
                        result.addStep("Default addition result: " + resultFraction.toString());
                        break;
                }
            }

            result.setFinalAnswer(resultFraction.toString());
            return result;

        } catch (Exception e) {
            result.setError("Failed to solve fraction expression: " + e.getMessage());
            return result;
        }
    }

    private String solveExponents(String expression, SolutionResult result) {
        // Handle exponents (^) - right to left
        Pattern exponentPattern = Pattern.compile("(\\d+(?:\\.\\d+)?)\\^(\\d+(?:\\.\\d+)?)");
        Matcher matcher = exponentPattern.matcher(expression);

        String currentExpression = expression;

        while (matcher.find()) {
            double base = Double.parseDouble(matcher.group(1));
            double exponent = Double.parseDouble(matcher.group(2));
            double exponentResult = Math.pow(base, exponent);

            result.addStep("Calculate exponent: " + base + "^" + exponent + " = " + formatNumber(exponentResult));

            currentExpression = currentExpression.replace(matcher.group(0), formatNumber(exponentResult));
            matcher = exponentPattern.matcher(currentExpression);
        }

        return currentExpression;
    }

    private String solveMultiplicationDivision(String expression, SolutionResult result) {
        // Handle multiplication and division (left to right)
        Pattern mdPattern = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*([*/])\\s*(\\d+(?:\\.\\d+)?)");
        Matcher matcher = mdPattern.matcher(expression);

        String currentExpression = expression;

        while (matcher.find()) {
            double num1 = Double.parseDouble(matcher.group(1));
            String operator = matcher.group(2);
            double num2 = Double.parseDouble(matcher.group(3));

            double mdResult;
            if (operator.equals("*")) {
                mdResult = num1 * num2;
                result.addStep("Multiply: " + num1 + " × " + num2 + " = " + formatNumber(mdResult));
            } else {
                if (num2 == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                mdResult = num1 / num2;
                result.addStep("Divide: " + num1 + " ÷ " + num2 + " = " + formatNumber(mdResult));
            }

            currentExpression = currentExpression.replace(matcher.group(0), formatNumber(mdResult));
            matcher = mdPattern.matcher(currentExpression);
        }

        return currentExpression;
    }

    private SolutionResult simplifyExpression(String expression) {
        SolutionResult result = new SolutionResult();

        if (expression == null || expression.trim().isEmpty()) {
            result.setError("No valid expression found");
            return result;
        }

        try {
            result.addStep("Original expression: " + expression);

            String simplified = expression.trim();

            // Step 1: Normalize expression
            simplified = simplified.replaceAll("\\s+", "");
            simplified = simplified.replace("x²", "x^2");
            simplified = simplified.replace("x2", "x^2");

            // Add explicit multiplication signs where needed
            simplified = simplified.replaceAll("(\\d)([a-zA-Z])", "$1*$2"); // 2x -> 2*x
            simplified = simplified.replaceAll("([a-zA-Z])(\\d)", "$1*$2"); // x2 -> x*2

            if (!simplified.equals(expression.trim().replaceAll("\\s+", ""))) {
                result.addStep("Normalized: " + simplified);
            }

            // Step 2: Combine all like terms in one go
            simplified = combineAllTerms(simplified, result);

            // Step 3: Clean up result
            simplified = cleanUpExpression(simplified);

            result.setFinalAnswer(simplified);
            return result;

        } catch (Exception e) {
            result.setError("Error simplifying expression: " + e.getMessage());
            return result;
        }
    }

    private static class Term {
        double coefficient;
        String variable; // "x^2", "x", or "" for constants

        Term(double coeff, String var) {
            this.coefficient = coeff;
            this.variable = var;
        }
    }

    private String combineAllTerms(String expression, SolutionResult result) {
        try {
            // Parse all terms from the expression
            List<Term> terms = parseAllTerms(expression);

            if (terms.isEmpty()) {
                return expression;
            }

            // Group terms by variable type and sum coefficients
            Map<String, Double> combined = new LinkedHashMap<>(); // Preserve order
            combined.put("x^2", 0.0);  // x² terms
            combined.put("x", 0.0);    // x terms
            combined.put("", 0.0);     // constant terms

            for (Term term : terms) {
                combined.merge(term.variable, term.coefficient, Double::sum);
            }

            // Build step-by-step explanation
            if (terms.size() > 1) {
                StringBuilder stepBuilder = new StringBuilder("Combining like terms: ");

                // Show x² terms if any
                List<Term> x2Terms = terms.stream().filter(t -> "x^2".equals(t.variable)).collect(Collectors.toList());
                if (x2Terms.size() > 1) {
                    stepBuilder.append("x² terms, ");
                }

                // Show x terms if any
                List<Term> xTerms = terms.stream().filter(t -> "x".equals(t.variable)).collect(Collectors.toList());
                if (xTerms.size() > 1) {
                    stepBuilder.append("x terms, ");
                }

                // Show constants if any
                List<Term> constants = terms.stream().filter(t -> "".equals(t.variable)).collect(Collectors.toList());
                if (constants.size() > 1) {
                    stepBuilder.append("constants");
                }

                result.addStep(stepBuilder.toString().replaceAll(", $", ""));
            }

            // Reconstruct the simplified expression
            return reconstructExpression(combined);

        } catch (Exception e) {
            // If parsing fails, return original expression
            return expression;
        }
    }

    private List<Term> parseAllTerms(String expression) {
        List<Term> terms = new ArrayList<>();

        // Add '+' at the beginning if it doesn't start with '+' or '-'
        String normalized = expression;
        if (!normalized.startsWith("+") && !normalized.startsWith("-")) {
            normalized = "+" + normalized;
        }

        // Pattern to match: optional sign, optional coefficient, optional variable part
        Pattern termPattern = Pattern.compile("([+-])?(\\d*(?:\\.\\d+)?)?\\*?(x(?:\\^2|²)?)?");
        Matcher matcher = termPattern.matcher(normalized);

        int lastEnd = 0;
        while (matcher.find()) {
            String sign = matcher.group(1);
            String coeffStr = matcher.group(2);
            String varPart = matcher.group(3);

            // Skip empty matches
            if (matcher.start() == matcher.end()) {
                continue;
            }

            // Skip if we haven't advanced
            if (matcher.start() < lastEnd) {
                continue;
            }
            lastEnd = matcher.end();

            // Parse coefficient
            double coeff = 1.0;
            if (coeffStr != null && !coeffStr.isEmpty()) {
                coeff = Double.parseDouble(coeffStr);
            }

            // Apply sign
            if ("-".equals(sign)) {
                coeff = -coeff;
            }

            // Determine variable type
            String variable = "";
            if (varPart != null) {
                if (varPart.contains("^2") || varPart.contains("²")) {
                    variable = "x^2";
                } else if (varPart.equals("x")) {
                    variable = "x";
                }
            }

            // Only add if coefficient is not zero or if it's a standalone constant
            if (Math.abs(coeff) > 1e-9 || (variable.isEmpty() && coeffStr != null && !coeffStr.isEmpty())) {
                terms.add(new Term(coeff, variable));
            }
        }

        return terms;
    }

    private String reconstructExpression(Map<String, Double> combined) {
        StringBuilder result = new StringBuilder();

        // Add terms in order: x², x, constants
        String[] order = {"x^2", "x", ""};

        for (String varType : order) {
            double coeff = combined.get(varType);

            if (Math.abs(coeff) < 1e-9) {
                continue; // Skip zero coefficients
            }

            // Add sign
            if (result.length() > 0) {
                if (coeff > 0) {
                    result.append(" + ");
                } else {
                    result.append(" - ");
                    coeff = -coeff; // Make positive for display
                }
            } else if (coeff < 0) {
                result.append("-");
                coeff = -coeff;
            }

            // Add coefficient and variable
            if ("x^2".equals(varType)) {
                if (Math.abs(coeff - 1.0) < 1e-9) {
                    result.append("x²");
                } else {
                    result.append(formatNumber(coeff)).append("x²");
                }
            } else if ("x".equals(varType)) {
                if (Math.abs(coeff - 1.0) < 1e-9) {
                    result.append("x");
                } else {
                    result.append(formatNumber(coeff)).append("x");
                }
            } else { // constant
                result.append(formatNumber(coeff));
            }
        }

        return result.length() > 0 ? result.toString() : "0";
    }

    private String cleanUpExpression(String expression) {
        // Remove leading + sign
        if (expression.startsWith("+")) {
            expression = expression.substring(1);
        }

        // Remove empty multiplication signs
        expression = expression.replace("*", "");

        // Fix double signs
        expression = expression.replace("+-", "-");
        expression = expression.replace("-+", "-");

        // Replace ^2 with ²
        expression = expression.replace("x^2", "x²");

        return expression;
    }

    private String solveAdditionSubtraction(String expression, SolutionResult result) {
        // Handle addition and subtraction (left to right)
        Pattern asPattern = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*([+\\-])\\s*(\\d+(?:\\.\\d+)?)");
        Matcher matcher = asPattern.matcher(expression);

        String currentExpression = expression;

        while (matcher.find()) {
            double num1 = Double.parseDouble(matcher.group(1));
            String operator = matcher.group(2);
            double num2 = Double.parseDouble(matcher.group(3));

            double asResult;
            if (operator.equals("+")) {
                asResult = num1 + num2;
                result.addStep("Add: " + num1 + " + " + num2 + " = " + formatNumber(asResult));
            } else {
                asResult = num1 - num2;
                result.addStep("Subtract: " + num1 + " - " + num2 + " = " + formatNumber(asResult));
            }

            currentExpression = currentExpression.replace(matcher.group(0), formatNumber(asResult));
            matcher = asPattern.matcher(currentExpression);
        }

        return currentExpression;
    }
}