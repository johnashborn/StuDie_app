package com.example.studie;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.regex.Pattern;

public class solution extends AppCompatActivity {

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

        TextView equationText = findViewById(R.id.equationText);
        TextView solutionText = findViewById(R.id.solutionText);

        // Get the recognized text from camera
        String mathProblem = getIntent().getStringExtra("math_problem");
        Log.d("SOLUTION", "Original text: " + mathProblem);

        if (mathProblem == null || mathProblem.trim().isEmpty()) {
            equationText.setText("No math problem received");
            solutionText.setText("Please try taking another picture");
            return;
        }

        // Clean and process the OCR text
        String cleanedExpression = cleanOCRText(mathProblem);
        Log.d("SOLUTION", "Cleaned text: " + cleanedExpression);

        // Display the original and cleaned expression
        equationText.setText("Problem: " + cleanedExpression);

        // Try to solve the expression
        String solution = solveMathExpression(cleanedExpression);
        solutionText.setText(solution);
    }

    private String cleanOCRText(String rawText) {
        if (rawText == null) return "";

        String cleaned = rawText.trim();

        // Remove newlines and extra spaces
        cleaned = cleaned.replaceAll("\\s+", " ");

        // Common OCR corrections
        cleaned = cleaned.replaceAll("[×xX]", "*");  // Replace multiplication symbols
        cleaned = cleaned.replaceAll("÷", "/");       // Replace division symbol
        cleaned = cleaned.replaceAll("−", "-");       // Replace minus symbol
        cleaned = cleaned.replaceAll("[{}\\[\\]]", "()"); // Replace brackets

        // Remove common OCR artifacts
        cleaned = cleaned.replaceAll("[^0-9+\\-*/().\\s=^]", ""); // Keep only math chars

        // Handle equations (take left side if there's an equals sign)
        if (cleaned.contains("=")) {
            String[] parts = cleaned.split("=");
            if (parts.length > 1) {
                // If right side is empty or just a question mark, solve left side
                if (parts[1].trim().isEmpty() || parts[1].trim().equals("?")) {
                    cleaned = parts[0].trim();
                } else {
                    // Otherwise, try to solve the full equation
                    cleaned = parts[0].trim();
                }
            }
        }

        // Remove trailing operators
        cleaned = cleaned.replaceAll("[+\\-*/]$", "");

        return cleaned.trim();
    }

    private String solveMathExpression(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return "No valid expression found";
        }

        try {
            // Try basic arithmetic first
            String result = solveBasicArithmetic(expression);
            if (result != null) {
                return result;
            }

            // Try algebraic expressions
            result = solveAlgebraicExpression(expression);
            if (result != null) {
                return result;
            }

            return "Could not solve: " + expression;

        } catch (Exception e) {
            Log.e("SOLUTION", "Error solving expression: " + expression, e);
            return "Error solving expression: " + e.getMessage();
        }
    }

    private String solveBasicArithmetic(String expression) {
        try {
            // Use exp4j for basic arithmetic
            Expression e = new ExpressionBuilder(expression).build();
            double result = e.evaluate();

            if (Double.isNaN(result) || Double.isInfinite(result)) {
                Log.d("SOLUTION", "exp4j returned invalid result for: " + expression);
                return null;
            }

            // Check if result is a whole number
            if (result == Math.floor(result)) {
                return "Result: " + (int) result;
            } else {
                return "Result: " + String.format("%.4f", result);
            }

        } catch (Exception e) {
            Log.d("SOLUTION", "Basic arithmetic failed: " + e.getMessage());
            return null;
        }
    }

    private String solveAlgebraicExpression(String expression) {
        try {
            // Handle simple algebraic expressions
            // For now, we'll focus on expressions with variables

            // Check if expression contains variables
            if (expression.matches(".*[a-zA-Z].*")) {
                // Try to solve for common variables like x, y, etc.
                if (expression.contains("x")) {
                    return solveForVariable(expression, "x");
                } else if (expression.contains("y")) {
                    return solveForVariable(expression, "y");
                }
            }

            return null;
        } catch (Exception e) {
            Log.d("SOLUTION", "Algebraic solving failed: " + e.getMessage());
            return null;
        }
    }

    private String solveForVariable(String expression, String variable) {
        try {
            // This is a simplified approach - for more complex algebra,
            // you might want to use a more sophisticated library

            // For now, let's handle simple cases like "2x + 3 = 7"
            if (expression.contains("=")) {
                String[] parts = expression.split("=");
                if (parts.length == 2) {
                    String leftSide = parts[0].trim();
                    String rightSide = parts[1].trim();

                    // Try to solve simple linear equations
                    // This is a very basic implementation
                    return "Algebraic solving not fully implemented yet.\nExpression: " + expression;
                }
            }

            return null;
        } catch (Exception e) {
            Log.d("SOLUTION", "Variable solving failed: " + e.getMessage());
            return null;
        }
    }

    // Helper method to validate mathematical expressions
    private boolean isValidMathExpression(String expression) {
        // Check for balanced parentheses
        int parentheses = 0;
        for (char c : expression.toCharArray()) {
            if (c == '(') parentheses++;
            else if (c == ')') parentheses--;
            if (parentheses < 0) return false;
        }
        return parentheses == 0;
    }
}