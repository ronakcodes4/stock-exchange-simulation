package se450.assignment2.quote;

import se450.assignment1.price.Price;
import se450.assignment1.price.PriceFactory;
import se450.assignment1.exceptions.InvalidPriceException;
import se450.assignment2.book.BookSide;
import se450.assignment2.exception.InvalidQuoteException;


public class Quote {

    private final String user;
    private final String product;
    private final QuoteSide buySide;
    private final QuoteSide sellSide;
    private static final Price ZERO_PRICE;

    static {
        try {
            ZERO_PRICE = PriceFactory.makePrice(0);
        } catch (Exception e) {
            throw new RuntimeException("Could not initialize ZERO_PRICE for Quote.", e);
        }
    }

    public Quote(String product, Price buyPrice, int buyVolume, Price sellPrice, int sellVolume, String user)
            throws InvalidQuoteException {

        if (user == null || !user.matches("^[A-Za-z]{3}$")) {
            throw new InvalidQuoteException("User code must be exactly 3 letters: " + user);
        }
        this.user = user;

        if (product == null || !product.matches("^[A-Za-z0-9.]{1,5}$")) {
            throw new InvalidQuoteException("Product symbol must be 1-5 letters/digits or '.': " + product);
        }
        this.product = product;

        if (buyPrice == null) {
            throw new InvalidQuoteException("Quote BUY price cannot be null.");
        }
        try {
            if (buyPrice.lessOrEqual(ZERO_PRICE)) {
                throw new InvalidQuoteException("Quote BUY price must be greater than $0.00: " + buyPrice);
            }
        } catch (InvalidPriceException e) {
            throw new InvalidQuoteException("Invalid price comparison for BUY price: " + buyPrice, e);
        }


        if (sellPrice == null) {
            throw new InvalidQuoteException("Quote SELL price cannot be null.");
        }
        try {
            if (sellPrice.lessOrEqual(ZERO_PRICE)) {
                throw new InvalidQuoteException("Quote SELL price must be greater than $0.00: " + sellPrice);
            }
        } catch (InvalidPriceException e) {
            throw new InvalidQuoteException("Invalid price comparison for SELL price: " + sellPrice, e);
        }


        if (buyVolume <= 0 || buyVolume >= 10000) {
            throw new InvalidQuoteException("Quote BUY volume must be > 0 and < 10,000: " + buyVolume);
        }

        if (sellVolume <= 0 || sellVolume >= 10000) {
            throw new InvalidQuoteException("Quote SELL volume must be > 0 and < 10,000: " + sellVolume);
        }

        this.buySide = new QuoteSide(this.user, this.product, buyPrice, buyVolume, BookSide.BUY);
        this.sellSide = new QuoteSide(this.user, this.product, sellPrice, sellVolume, BookSide.SELL);
    }


    public String getUser() {
        return user;
    }


    public String getProduct() {
        return product;
    }


    public String getSymbol() {
        return product;
    }


    public QuoteSide getBuySide() {
        return buySide;
    }


    public QuoteSide getSellSide() {
        return sellSide;
    }


    public QuoteSide getQuoteSide(BookSide sideIn) {
        if (sideIn == BookSide.BUY) {
            return buySide;
        } else if (sideIn == BookSide.SELL) {
            return sellSide;
        }
        return null;
    }
}