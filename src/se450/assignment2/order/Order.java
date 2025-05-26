package se450.assignment2.order;

import se450.assignment1.Price;
import se450.assignment3.book.BookSide; // IMPORTANT: Uses A3 BookSide
import se450.assignment2.exception.InvalidOrderException;
import se450.assignment2.tradable.Tradable;
import se450.assignment2.tradable.TradableDTO;

public class Order implements Tradable {

    private final String user;
    private final String product;
    private final Price price;
    private final BookSide side; // IMPORTANT: Uses A3 BookSide
    private final String id;

    private final int originalVolume;
    private int remainingVolume;
    private int cancelledVolume;
    private int filledVolume;

    public Order(String user,
                 String product,
                 Price price,
                 int originalVolume,
                 BookSide side) // IMPORTANT: Expects A3 BookSide
            throws InvalidOrderException {
        // Validations
        if (user == null || !user.matches("[A-Za-z]{3}")) throw new IllegalArgumentException("User code must be 3 letters");
        if (product == null || !product.matches("[A-Za-z0-9.]{1,5}")) throw new IllegalArgumentException("Product must be 1-5 letters/digits or '.'");
        if (price == null) throw new IllegalArgumentException("Price cannot be null");
        if (price.isNegative()) throw new InvalidOrderException("Order price must be >= $0.00: " + price);
        if (originalVolume <= 0 || originalVolume >= 10_000) throw new InvalidOrderException("Order volume must be > 0 and < 10000: " + originalVolume);
        if (side == null) throw new IllegalArgumentException("Side cannot be null");

        this.user = user;
        this.product = product;
        this.price = price;
        this.side = side;
        this.originalVolume = originalVolume;
        this.remainingVolume = originalVolume;
        this.cancelledVolume = 0;
        this.filledVolume = 0;
        this.id = user + product + price + System.nanoTime();
    }

    @Override public String getUser() { return user; }
    @Override public String getProduct() { return product; }
    @Override public Price getPrice() { return price; }
    @Override public BookSide getSide() { return side; } // IMPORTANT: Returns A3 BookSide
    @Override public String getId() { return id; }
    @Override public int getOriginalVolume() { return originalVolume; }
    @Override public int getRemainingVolume() { return remainingVolume; }
    @Override public int getCancelledVolume() { return cancelledVolume; }
    @Override public int getFilledVolume() { return filledVolume; }
    @Override public boolean isQuote() { return false; }

    @Override
    public void setCancelledVolume(int vol) {
        if (vol < 0) throw new IllegalArgumentException("Cancelled volume must be >= 0");
        this.cancelledVolume = vol;
    }

    @Override
    public void setRemainingVolume(int vol) {
        if (vol < 0) throw new IllegalArgumentException("Remaining volume must be >= 0");
        this.remainingVolume = vol;
    }

    @Override
    public void setFilledVolume(int vol) {
        if (vol < 0) throw new IllegalArgumentException("Filled volume must be >= 0");
        this.filledVolume = vol;
    }

    @Override
    public TradableDTO makeTradableDTO() {
        // Creates a DTO that uses A3 BookSide
        return new TradableDTO(this);
    }

    @Override
    public String toString() {
        return String.format("%s %s order for %s: Price: %s, Orig Vol: %3d, Rem Vol: %3d, Fill Vol: %3d, Cxl'd Vol: %3d, ID: %s",
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
