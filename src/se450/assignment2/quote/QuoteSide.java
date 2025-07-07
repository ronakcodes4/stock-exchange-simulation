package se450.assignment2.quote;

import se450.assignment1.price.Price;
import se450.assignment1.exceptions.InvalidPriceException;
import se450.assignment1.price.PriceFactory;
import se450.assignment2.book.BookSide;
import se450.assignment2.exception.InvalidQuoteException;
import se450.assignment2.tradable.Tradable;
import se450.assignment2.tradable.TradableDTO;


public class QuoteSide implements Tradable {

    private final String user;
    private final String product;
    private final Price price;
    private final BookSide side;
    private final String id;
    private final int originalVolume;

    private int remainingVolume;
    private int cancelledVolume;
    private int filledVolume;

    private static final Price ZERO_PRICE;

    static {
        try {
            ZERO_PRICE = PriceFactory.makePrice(0);
        } catch (Exception e) {
            throw new RuntimeException("Could not initialize ZERO_PRICE for QuoteSide.", e);
        }
    }


    public QuoteSide(String user, String product, Price price, int originalVolume, BookSide side)
            throws InvalidQuoteException {

        if (user == null || !user.matches("^[A-Za-z]{3}$")) {
            throw new InvalidQuoteException("User code must be exactly 3 letters: " + user);
        }
        this.user = user;

        if (product == null || !product.matches("^[A-Za-z0-9.]{1,5}$")) {
            throw new InvalidQuoteException("Product symbol must be 1-5 letters/digits or '.': " + product);
        }
        this.product = product;

        if (price == null) {
            throw new InvalidQuoteException("QuoteSide price cannot be null.");
        }
        try {
            if (price.lessOrEqual(ZERO_PRICE)) {
                throw new InvalidQuoteException("QuoteSide price must be greater than $0.00: " + price);
            }
        } catch (InvalidPriceException e) {
            throw new InvalidQuoteException("Invalid price comparison for QuoteSide: " + price, e);
        }
        this.price = price;

        if (originalVolume <= 0 || originalVolume >= 10000) {
            throw new InvalidQuoteException("QuoteSide volume must be > 0 and < 10,000: " + originalVolume);
        }
        this.originalVolume = originalVolume;
        this.remainingVolume = originalVolume;

        if (side == null) {
            throw new InvalidQuoteException("QuoteSide (BUY/SELL) cannot be null.");
        }
        this.side = side;

        this.id = this.user + this.product + this.price.toString() + System.nanoTime();

        this.cancelledVolume = 0;
        this.filledVolume = 0;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public String getProduct() {
        return product;
    }

    @Override
    public Price getPrice() {
        return price;
    }

    @Override
    public BookSide getSide() {
        return side;
    }

    @Override
    public boolean isQuote() {
        return true;
    }

    @Override
    public int getOriginalVolume() {
        return originalVolume;
    }

    @Override
    public int getRemainingVolume() {
        return remainingVolume;
    }

    @Override
    public int getCancelledVolume() {
        return cancelledVolume;
    }

    @Override
    public int getFilledVolume() {
        return filledVolume;
    }

    @Override
    public void setRemainingVolume(int newVol) {
        if (newVol < 0) {
            throw new IllegalArgumentException("Remaining volume cannot be negative: " + newVol);
        }
        this.remainingVolume = newVol;
    }

    @Override
    public void setCancelledVolume(int newVol) {
        if (newVol < 0) {
            throw new IllegalArgumentException("Cancelled volume cannot be negative: " + newVol);
        }
        this.cancelledVolume = newVol;
    }

    @Override
    public void setFilledVolume(int newVol) {
        if (newVol < 0) {
            throw new IllegalArgumentException("Filled volume cannot be negative: " + newVol);
        }
        this.filledVolume = newVol;
    }

    @Override
    public TradableDTO makeTradableDTO() {
        return new TradableDTO(
                this.id,
                this.user,
                this.product,
                this.price,
                this.originalVolume,
                this.remainingVolume,
                this.cancelledVolume,
                this.filledVolume,
                this.side,
                this.isQuote()
        );
    }


    @Override
    public String toString() {
        return String.format("%s %s side quote for %s: %s, Orig Vol: %d, Rem Vol: %d, Fill Vol: %d, CXL Vol: %d, ID: %s",
                user,
                side.toString(),
                product,
                price.toString(),
                originalVolume,
                remainingVolume,
                filledVolume,
                cancelledVolume,
                id
        );
    }
}