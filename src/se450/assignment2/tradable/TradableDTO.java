package se450.assignment2.tradable;

import se450.assignment1.Price;
import se450.assignment3.book.BookSide; // IMPORTANT: Uses A3 BookSide

import java.util.Objects;

public class TradableDTO {
    private final String id;
    private final String user;
    private final String product;
    private final Price price;
    private final int originalVolume;
    private final int remainingVolume;
    private final int cancelledVolume;
    private final int filledVolume;
    private final BookSide side; // Uses A3 BookSide
    private final boolean isQuote;

    public TradableDTO(String id,
                       String user,
                       String product,
                       Price price,
                       int originalVolume,
                       int remainingVolume,
                       int cancelledVolume,
                       int filledVolume,
                       BookSide side, // Expects A3 BookSide
                       boolean isQuote) {
        // Validations (ensure these are robust as per your A2 requirements)
        if (id == null || id.isBlank()) throw new IllegalArgumentException("ID cannot be null or empty");
        if (user == null || !user.matches("[A-Za-z]{3}")) throw new IllegalArgumentException("User code must be 3 letters");
        if (product == null || !product.matches("[A-Za-z0-9.]{1,5}")) throw new IllegalArgumentException("Product must be 1-5 letters/digits or '.'");
        if (price == null) throw new IllegalArgumentException("Price cannot be null");
        if (side == null) throw new IllegalArgumentException("Side cannot be null");
        if (originalVolume <= 0) throw new IllegalArgumentException("Original volume must be > 0");
        // Add other volume consistency checks if needed

        this.id = id;
        this.user = user;
        this.product = product;
        this.price = price;
        this.originalVolume = originalVolume;
        this.remainingVolume = remainingVolume;
        this.cancelledVolume = cancelledVolume;
        this.filledVolume = filledVolume;
        this.side = side;
        this.isQuote = isQuote;
    }

    // Constructor from a Tradable object
    public TradableDTO(Tradable t) {
        this(t.getId(), t.getUser(), t.getProduct(), t.getPrice(),
                t.getOriginalVolume(), t.getRemainingVolume(),
                t.getCancelledVolume(), t.getFilledVolume(),
                t.getSide(), // Tradable.getSide() MUST return A3 BookSide
                t.isQuote());
    }

    public String getID() { return id; }
    public String tradableId() { return id; } // ADDED to match Main.java call

    public String getUser() { return user; }
    public String getProduct() { return product; }
    public Price getPrice() { return price; }
    public int getOriginalVolume() { return originalVolume; }
    public int getRemainingVolume() { return remainingVolume; }
    public int getCancelledVolume() { return cancelledVolume; }
    public int getFilledVolume() { return filledVolume; }
    public BookSide getSide() { return side; } // Returns A3 BookSide
    public boolean isQuote() { return isQuote; }

    @Override
    public String toString() {
        // Format to match Part3ExpectedOutput.txt for user tradables and book display
        // Example: Product: WMT, Price: $134.00, Orig Vol:  88, Rem Vol:   0, Fill Vol:  88, Cxl'd Vol:   0, ID: ...
        // Or for book entries: ANA BUY side order for WMT: Price: $134.00, Orig Vol:  88, Rem Vol:   0, Fill Vol:  88, Cxl'd Vol:   0, ID: ...
        // The A3 PDF for User.toString() shows the "Product: ..." format.
        // The A3 PDF for ProductBook.toString() shows the "USER SIDE type for PRODUCT: Price..." format.
        // Your A2 DTO toString was: USER SIDE [side quote for] PRODUCT: PRICE, Orig Vol: ..., etc.
        // This seems fine for book display and fill messages.
        // Let's refine it slightly for the "Cxl'd Vol" and spacing.

        String typeString = isQuote ? "side quote for" : "order for"; // A3 output uses "BUY side order for" or "BUY side quote for"
        // The "side" (BUY/SELL) is already part of `side.toString()`

        return String.format("%s %s %s %s: Price: %s, Orig Vol: %3d, Rem Vol: %3d, Fill Vol: %3d, Cxl'd Vol: %3d, ID: %s",
                user,
                side.toString(), // e.g., "BUY"
                typeString,      // "order for" or "side quote for"
                product,
                price.toString(),
                originalVolume,
                remainingVolume,
                filledVolume,
                cancelledVolume,
                id
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TradableDTO that = (TradableDTO) o;
        return originalVolume == that.originalVolume &&
                remainingVolume == that.remainingVolume &&
                cancelledVolume == that.cancelledVolume &&
                filledVolume == that.filledVolume &&
                isQuote == that.isQuote &&
                Objects.equals(id, that.id) &&
                Objects.equals(user, that.user) &&
                Objects.equals(product, that.product) &&
                Objects.equals(price, that.price) &&
                side == that.side;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, product, price, originalVolume, remainingVolume, cancelledVolume, filledVolume, side, isQuote);
    }
}
