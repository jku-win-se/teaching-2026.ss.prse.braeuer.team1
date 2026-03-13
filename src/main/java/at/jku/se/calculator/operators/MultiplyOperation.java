package at.jku.se.calculator.operators;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import at.jku.se.calculator.factory.ICalculationOperation;

public class MultiplyOperation implements ICalculationOperation {

    private static final Logger LOGGER = LogManager.getLogger(MultiplyOperation.class);

    @Override
    public String calculate(String txt) {
        LOGGER.info("Multiply Operation executed: " + txt);
        String[] terms = txt.split("\\*");
        if (terms.length == 2) {
            String left = terms[0].trim();
            String right = terms[1].trim();

            if (!isInteger(left)) {
                LOGGER.error("Invalid Value");
                throw new IllegalArgumentException(String.format("%s is not a valid number", left));
            }
            if (!isInteger(right)) {
                LOGGER.error("Invalid Value");
                throw new IllegalArgumentException(String.format("%s is not a valid number", right));
            }

            return String.valueOf(Integer.parseInt(left) * Integer.parseInt(right));
        } else {
            throw new IllegalArgumentException("Input not correct!");
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
