package at.jku.se.calculator.operators;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import at.jku.se.calculator.factory.ICalculationOperation;

/**
 * {@link ICalculationOperation} that subtracts two integer operands.
 *
 * <p>Expects an input string of the form {@code "a-b"} where both {@code a}
 * and {@code b} are valid integers. Throws {@link IllegalArgumentException}
 * if the input is malformed.</p>
 */
public class SubtractOperation implements ICalculationOperation {

    private static final Logger LOGGER = LogManager.getLogger(SubtractOperation.class);

    @Override
    public String calculate(String txt) {
        LOGGER.info("Subtract Operation executed: " + txt);
        
        // Split am Minus-Zeichen. 
        // Hinweis: Falls negative Zahlen unterstützt werden sollen (z.B. "5--3"), 
        // müsste der Regex komplexer sein. Analog zum Add-Beispiel wird hier "a-b" erwartet.
        String[] terms = txt.split("-");
        
        if (terms.length == 2) {
            if (!isInteger(terms[0])) {
                LOGGER.error("Invalid Value: " + terms[0]);
                throw new IllegalArgumentException(String.format("%s is not a valid number", terms[0]));
            }
            if (!isInteger(terms[1])) {
                LOGGER.error("Invalid Value: " + terms[1]);
                throw new IllegalArgumentException(String.format("%s is not a valid number", terms[1]));
            }

            int result = Integer.parseInt(terms[0]) - Integer.parseInt(terms[1]);
            return String.valueOf(result);
        } else {
            throw new IllegalArgumentException("Input not correct! Expected format: a-b");
        }
    }

    private boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}