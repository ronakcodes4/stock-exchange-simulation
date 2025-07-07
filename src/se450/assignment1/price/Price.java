package se450.assignment1.price;

import se450.assignment1.exceptions.InvalidPriceException;

public class Price implements Comparable<Price> {
    private final int cents;

    public Price(int centsIn) {
        //centsIn is for input cents to avoid confusion
        this.cents = centsIn;
    }

    public boolean isNegative() {
        return cents < 0;
    }

    @Override
    public int compareTo(Price other) {
        if (other == null) {
            throw new NullPointerException("Cannot compare to null");
        }
        return Integer.compare(this.cents, other.cents);
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(this.cents);
    }

    public Price add(Price p) throws InvalidPriceException {
        if (p == null) {
            throw new InvalidPriceException("Cannot add a null Price.");
        }
        return new Price(this.cents + p.cents);
    }

    public Price subtract(Price p) throws InvalidPriceException {
        if (p == null) {
            throw new InvalidPriceException("Cannot add a null Price.");
        }
        return new Price(this.cents - p.cents);
    }

    public Price multiply(int n) {
        return new Price(this.cents * n);
    }

    public boolean greaterThan(Price p) throws InvalidPriceException {
        if (p == null) {
            throw new InvalidPriceException("Cannot compare to null");
        }
        return this.cents > p.cents;
    }

    public boolean greaterOrEqual(Price p) throws InvalidPriceException {
        if (p == null) {
            throw new InvalidPriceException("Cannot compare to null");
        }
        return this.cents >= p.cents;
    }

    public boolean lessThan(Price p) throws InvalidPriceException {
        if (p == null) {
            throw new InvalidPriceException("Cannot compare to null");
        }
        return this.cents < p.cents;
    }

    public boolean lessOrEqual(Price p) throws InvalidPriceException {
        if (p == null) {
            throw new InvalidPriceException("Cannot compare to null");
        }
        return this.cents <= p.cents;
    }

    @Override
    public String toString() {
        int absCents = Math.abs(this.cents);
        int dollars = absCents / 100;
        int centsPart = absCents % 100;

        String dollarStr = String.format("%,d", dollars);
        String centStr = String.format("%02d", centsPart);

        return (this.cents < 0 ? "$-" : "$") + dollarStr + "." + centStr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Price price = (Price) o;
        return cents == price.cents;
    }
}
