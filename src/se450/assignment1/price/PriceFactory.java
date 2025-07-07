package se450.assignment1.price;

import se450.assignment3.exceptions.DataValidationException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PriceFactory {

    private static final Map<Integer, Price> CACHE = new ConcurrentHashMap<>();

    private PriceFactory() {
    }


    public static Price makePrice(int valueInCents) {
        return CACHE.computeIfAbsent(valueInCents, Price::new);
    }

    public static Price makePrice(String stringValueIn) throws DataValidationException {
        if (stringValueIn == null || stringValueIn.trim().isEmpty()) {
            throw new DataValidationException("Price string cannot be null or empty.");
        }

        String cleanString = stringValueIn.trim();
        boolean isNegative = false;

        if (cleanString.startsWith("$-")) {
            isNegative = true;
            cleanString = cleanString.substring(2);
        } else if (cleanString.startsWith("$")) {
            cleanString = cleanString.substring(1);
        } else if (cleanString.startsWith("-")) {
            isNegative = true;
            cleanString = cleanString.substring(1);
        }

        cleanString = cleanString.replace(",", "");

        if (cleanString.isEmpty() && stringValueIn.contains("$") && stringValueIn.length() > 1) {
            throw new DataValidationException("Invalid price format: " + stringValueIn + " (just currency symbol or sign)");
        }
        if (cleanString.isEmpty() && !stringValueIn.contains("$")) {
            throw new DataValidationException("Invalid price format after cleaning: " + stringValueIn);
        }


        try {
            long totalCents = getTotalCents(stringValueIn, cleanString, isNegative);

            return makePrice((int) totalCents);

        } catch (NumberFormatException e) {
            throw new DataValidationException("Invalid numeric format in price string: " + stringValueIn, e);
        }
    }

    private static long getTotalCents(String stringValueIn, String cleanString, boolean isNegative) throws DataValidationException {
        long dollars = 0;
        long cents = 0;

        if (cleanString.contains(".")) {
            String[] parts = cleanString.split("\\.");
            if (parts.length > 2) {
                throw new DataValidationException("Invalid price format: too many decimal points in " + stringValueIn);
            }
            if (!parts[0].isEmpty()) {
                dollars = Long.parseLong(parts[0]);
            }
            if (parts.length == 2) {
                if (parts[1].length() == 1) {
                    cents = Long.parseLong(parts[1]) * 10;
                } else if (parts[1].length() == 2) {
                    cents = Long.parseLong(parts[1]);
                } else if (parts[1].length() > 2) {
                    throw new DataValidationException("Price cents part has too many digits: " + parts[1]);
                }

            }
        } else {
            dollars = Long.parseLong(cleanString);
        }

        if (dollars > Integer.MAX_VALUE / 100 || dollars < Integer.MIN_VALUE / 100) {
            throw new DataValidationException("Dollar value out of reasonable range for int cents conversion.");
        }

        long totalCents = dollars * 100 + cents;
        if (isNegative) {
            totalCents = -totalCents;
        }

        if (totalCents > Integer.MAX_VALUE || totalCents < Integer.MIN_VALUE) {
            throw new DataValidationException("Total cents value exceeds Integer range.");
        }
        return totalCents;
    }
}