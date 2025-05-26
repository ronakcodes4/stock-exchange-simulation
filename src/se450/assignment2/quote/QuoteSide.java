package se450.assignment2.quote; // Assuming this is your A2 package

import se450.assignment1.Price;
import se450.assignment3.book.BookSide; // UPDATED IMPORT
import se450.assignment2.exception.InvalidQuoteException; // Keep A2 exception
import se450.assignment2.tradable.Tradable;
import se450.assignment2.tradable.TradableDTO;

public class QuoteSide implements Tradable {

    private static final Price ZERO_PRICE; // Initialized in static block or ensure PriceFactory is used

    static {
        // It's safer to initialize Price objects via PriceFactory if it's available
        // and handles potential exceptions during static initialization.
        // For now, direct instantiation as per your A2 code, assuming Price constructor is public.
        // If Price constructor is not public, this needs PriceFactory.makePrice(0);
        ZERO_PRICE = new Price(0);
    }


    private final String user;
    private final String product;
    private final Price price;
    private final int originalVolume;
    private int remainingVolume;
    private int cancelledVolume;
    private int filledVolume;
    private final BookSide side; // UPDATED TYPE
    private final boolean isQuote; // This field was in your A2 QuoteSide
    private final String id;

    public QuoteSide(String user,
                     String product,
                     Price price,
                     int volume,
                     BookSide side, // UPDATED PARAMETER TYPE
                     boolean isQuote) // Your A2 QuoteSide constructor had this
            throws InvalidQuoteException { // Keep A2 exception

        // user & product checks
        if (user == null || user.isBlank()) {
            throw new IllegalArgumentException("User cannot be null or empty");
        }
        if (!user.matches("[A-Za-z]{3}")) { // Assuming A-Z
            throw new IllegalArgumentException(
                    "User code must be exactly 3 letters: " + user);
        }
        if (product == null || product.isBlank()) {
            throw new IllegalArgumentException("Product cannot be null or empty");
        }
        if (!product.matches("[A-Za-z0-9.]{1,5}")) {
            throw new IllegalArgumentException(
                    "Product must be 1–5 letters/numbers or '.': " + product);
        }

        // price checks
        if (price == null) {
            throw new InvalidQuoteException("QuoteSide price cannot be null");
        }
        // Assuming Price.compareTo and Price.isNegative are available from A1
        try {
            if (price.lessOrEqual(ZERO_PRICE)) { // Using lessOrEqual for non-positive check
                throw new InvalidQuoteException(
                        "QuoteSide price must be > $0.00: " + price);
            }
        } catch (se450.assignment1.InvalidPriceException e) {
            // This happens if ZERO_PRICE is null in lessOrEqual, should not occur with static init
            throw new RuntimeException("Error comparing price in QuoteSide constructor", e);
        }


        // volume checks
        if (volume <= 0 || volume >= 10000) { // A2 Order spec: < 10000. A2 QuoteSide spec: > 0. Let's align with Order.
            throw new InvalidQuoteException(
                    "QuoteSide volume must be > 0 and < 10000: " + volume);
        }

        // side
        if (side == null) {
            throw new IllegalArgumentException("Side cannot be null");
        }

        this.user            = user;
        this.product         = product;
        this.price           = price;
        this.originalVolume  = volume;
        this.remainingVolume = volume;
        this.cancelledVolume = 0;
        this.filledVolume    = 0;
        this.side            = side;
        this.isQuote         = isQuote; // Store this
        this.id              = user + product + price.toString() + System.nanoTime();
    }

    @Override public String   getUser()            { return user; }
    @Override public String   getProduct()         { return product; }
    @Override public Price    getPrice()           { return price; }
    @Override public BookSide getSide()            { return side; } // UPDATED RETURN TYPE
    @Override public String   getId()              { return id; }
    @Override public int      getOriginalVolume()  { return originalVolume; }
    @Override public int      getRemainingVolume() { return remainingVolume; }
    @Override public int      getCancelledVolume() { return cancelledVolume; }
    @Override public int      getFilledVolume()    { return filledVolume; }
    @Override public boolean  isQuote()            { return this.isQuote; } // Return the stored field

    @Override public void setCancelledVolume(int vol) {
        if (vol < 0) throw new IllegalArgumentException("Cancelled volume must be >= 0. Received: " + vol);
        this.cancelledVolume = vol;
    }
    @Override public void setRemainingVolume(int vol) {
        if (vol < 0) throw new IllegalArgumentException("Remaining volume must be >= 0. Received: " + vol);
        this.remainingVolume = vol;
    }
    @Override public void setFilledVolume(int vol) {
        if (vol < 0) throw new IllegalArgumentException("Filled volume must be >= 0. Received: " + vol);
        this.filledVolume = vol;
    }

    // public void adjustRemainingVolume(int delta) { // If needed by Tradable interface
    //     int newRem = this.remainingVolume + delta;
    //     if (newRem < 0) {
    //         throw new IllegalArgumentException("Adjust would make remaining volume negative");
    //     }
    //     this.remainingVolume = newRem;
    // }

    @Override
    public TradableDTO makeTradableDTO() {
        return new TradableDTO(
                id, user, product, price,
                originalVolume, remainingVolume,
                cancelledVolume, filledVolume,
                side, // This 'side' is now se450.assignment3.book.BookSide
                this.isQuote // Pass the stored isQuote value
        );
    }

    @Override
    public String toString() {
        // A3 Output for quotes in book/fills: "ANA BUY side quote for WMT: Price: $133.00, Orig Vol:  40, Rem Vol:  17, Fill Vol:  23, Cxl'd Vol:   0, ID: ..."
        // This is very similar to the DTO format.
        return String.format(
                "%s %s side quote for %s: Price: %s, Orig Vol: %3d, Rem Vol: %3d, Fill Vol: %3d, Cxl'd Vol: %3d, ID: %s",
                user,
                side.toString(), // BUY or SELL
                product,
                price.toString(),
                originalVolume,
                remainingVolume,
                filledVolume,
                cancelledVolume, // Changed from CXL Vol for consistency
                id
        );
    }
}
