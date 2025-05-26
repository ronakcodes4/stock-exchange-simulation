package se450.assignment2.quote;

import se450.assignment1.Price;
import se450.assignment3.book.BookSide;
import se450.assignment2.exception.InvalidQuoteException;

public class Quote {

    private final QuoteSide buySideLeg;  // Renamed to avoid conflict with getBuySide() if that's a direct field accessor
    private final QuoteSide sellSideLeg; // Renamed
    private final String userName; // A2 PDF uses userName in constructor, user in fields. Let's be consistent.
    private final String productSymbol; // A2 PDF uses symbol in constructor, product in fields.

    public Quote(String product, // Parameter name from A2 PDF for constructor
                 Price buyPrice, int buyVolume,
                 Price sellPrice, int sellVolume,
                 String user) // Parameter name from A2 PDF for constructor
            throws InvalidQuoteException {

        // product & user string validation (from your A2 Quote.java)
        if (product == null || product.isBlank()) {
            throw new IllegalArgumentException("Product cannot be null or empty");
        }
        if (!product.matches("[A-Za-z0-9.]{1,5}")) {
            throw new IllegalArgumentException(
                    "Product must be 1–5 letters/numbers or '.': " + product);
        }
        if (user == null || user.isBlank()) {
            throw new IllegalArgumentException("User cannot be null or empty");
        }
        if (!user.matches("[A-Za-z]{3}")) { // Assuming A-Z
            throw new IllegalArgumentException(
                    "User code must be exactly 3 letters: " + user);
        }

        // price validation (from your A2 Quote.java)
        if (buyPrice == null) {
            throw new InvalidQuoteException("Quote buy price cannot be null");
        }
        Price zeroPrice;
                zeroPrice = se450.assignment3.price.PriceFactory.makePrice(0); // Use A3 PriceFactory

                try {
            if (buyPrice.lessOrEqual(zeroPrice)) {
                throw new InvalidQuoteException(
                        "Quote buy price must be > $0.00: " + buyPrice);
            }
            if (sellPrice == null) {
                throw new InvalidQuoteException("Quote sell price cannot be null");
            }
            if (sellPrice.lessOrEqual(zeroPrice)) {
                throw new InvalidQuoteException(
                        "Quote sell price must be > $0.00: " + sellPrice);
            }
        } catch (se450.assignment1.InvalidPriceException e) {
            // This exception is from Price.lessOrEqual if its parameter is null.
            // zeroPrice should not be null here.
            throw new RuntimeException("Error comparing prices in Quote constructor", e);
        }


        // volume validation (from your A2 Quote.java, ensuring < 10000 like Order)
        if (buyVolume <= 0 || buyVolume >= 10000) {
            throw new InvalidQuoteException(
                    "Quote buy volume must be > 0 and < 10000: " + buyVolume);
        }
        if (sellVolume <= 0 || sellVolume >= 10000) {
            throw new InvalidQuoteException(
                    "Quote sell volume must be > 0 and < 10000: " + sellVolume);
        }

        this.userName = user;
        this.productSymbol = product;

        // build the two legs using se450.assignment3.book.BookSide
        this.buySideLeg  = new QuoteSide(user, product, buyPrice,  buyVolume,  BookSide.BUY,  true);
        this.sellSideLeg = new QuoteSide(user, product, sellPrice, sellVolume, BookSide.SELL, true);
    }

    // Getter for user (as per A2 PDF for Quote: public String getUser())
    public String getUserName() { return userName; } // Matching your A2 Quote.java method name
    public String getUser() { return userName; } // Alias for consistency if ProductManager uses getUser

    // Getter for product symbol (as per A2 PDF for Quote: public String getSymbol())
    public String getProduct() { return productSymbol; } // Matching your A2 Quote.java method name
    public String getSymbol() { return productSymbol; } // Alias for consistency

    // Getter for QuoteSides (as per your A2 Quote.java)
    public QuoteSide getBuySide()   { return buySideLeg; }
    public QuoteSide getSellSide()  { return sellSideLeg; }

    // Getter as specified in A2 PDF for Quote: public QuoteSide getQuoteSide(BookSide sideIn)
    public QuoteSide getQuoteSide(BookSide sideIn) {
        if (sideIn == BookSide.BUY) {
            return buySideLeg;
        } else if (sideIn == BookSide.SELL) {
            return sellSideLeg;
        }
        return null; // Or throw exception for invalid side
    }
}
