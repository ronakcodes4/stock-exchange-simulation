package se450.assignment3.price;

import se450.assignment1.Price;

import se450.assignment3.exceptions.DataValidationException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class PriceFactory {
    private PriceFactory() {}
    private static final Map<Integer, Price> CACHE = new ConcurrentHashMap<>();

    public static Price makePrice(int value) {
        return CACHE.computeIfAbsent(value, Price::new);
    }

    public static Price makePrice(String stringValueIn) throws DataValidationException {
        if (stringValueIn == null || stringValueIn.trim().isEmpty()) {
            throw new DataValidationException("Empty or null string provided.");
        }

        try {
            String cleanString = stringValueIn.trim()
                    .replace("$", "")
                    .replace(",", "");

            boolean isNegative = cleanString.startsWith("-");

            if (isNegative) {
                cleanString = cleanString.substring(1);
            }

            int cents;

            if (cleanString.contains(".")) {
                String[] parts = cleanString.split("\\.", -1);
                if (parts.length != 2) {
                    throw new DataValidationException("Invalid decimal format: " + stringValueIn);
                }


                String dollarPart = parts[0].isEmpty() ? "0" : parts[0];
                String centsPart = parts[1];

                if (centsPart.isEmpty()) {
                    centsPart = "00";
                } else if (centsPart.length() != 2) {
                    throw new DataValidationException("Too many decimal digits: " + stringValueIn);
                }

                cents = Integer.parseInt(dollarPart) * 100 + Integer.parseInt(centsPart);
            } else {
                cents = Integer.parseInt(cleanString) * 100;
            }

            int finalCents = isNegative ? -cents : cents;
            return makePrice(finalCents);

        } catch (NumberFormatException e) {
            throw new DataValidationException("Invalid price format: " + stringValueIn);
        }
    }
}

