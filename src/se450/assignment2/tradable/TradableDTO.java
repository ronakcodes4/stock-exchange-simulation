package se450.assignment2.tradable;

import se450.assignment1.price.Price;
import se450.assignment2.book.BookSide;

public record TradableDTO(
        String tradableId,
        String user,
        String product,
        Price price,
        int originalVolume,
        int remainingVolume,
        int cancelledVolume,
        int filledVolume,
        BookSide side,
        boolean isQuote
) {


    public TradableDTO {
        if (tradableId == null || tradableId.isBlank()) {
            throw new IllegalArgumentException("TradableDTO tradableId cannot be null or empty.");
        }
        if (user == null || !user.matches("^[A-Za-z]{3}$")) {
            throw new IllegalArgumentException("TradableDTO User code must be exactly 3 letters: " + user);
        }
        if (product == null || !product.matches("^[A-Za-z0-9.]{1,5}$")) {
            throw new IllegalArgumentException("TradableDTO Product symbol must be 1-5 letters/digits or '.': " + product);
        }
        if (price == null) {
            throw new IllegalArgumentException("TradableDTO Price cannot be null.");
        }
        if (side == null) {
            throw new IllegalArgumentException("TradableDTO Side (BUY/SELL) cannot be null.");
        }
        if (originalVolume <= 0) {
            throw new IllegalArgumentException("TradableDTO Original volume must be > 0: " + originalVolume);
        }
        if (remainingVolume < 0 || remainingVolume > originalVolume) {
            throw new IllegalArgumentException("TradableDTO Remaining volume (" + remainingVolume + ") must be between 0 and original volume (" + originalVolume + ").");
        }
        if (cancelledVolume < 0 || cancelledVolume > originalVolume) {
            throw new IllegalArgumentException("TradableDTO Cancelled volume (" + cancelledVolume + ") must be between 0 and original volume (" + originalVolume + ").");
        }
        if (filledVolume < 0 || filledVolume > originalVolume) {
            throw new IllegalArgumentException("TradableDTO Filled volume (" + filledVolume + ") must be between 0 and original volume (" + originalVolume + ").");
        }

    }


    public TradableDTO(Tradable t) {
        this(
                (t == null ? null : t.getId()),
                (t == null ? null : t.getUser()),
                (t == null ? null : t.getProduct()),
                (t == null ? null : t.getPrice()),
                (t == null ? 0 : t.getOriginalVolume()),
                (t == null ? 0 : t.getRemainingVolume()),
                (t == null ? 0 : t.getCancelledVolume()),
                (t == null ? 0 : t.getFilledVolume()),
                (t == null ? null : t.getSide()),
                (t != null && t.isQuote())
        );
        if (t == null) {
            throw new IllegalArgumentException("Cannot create TradableDTO from a null Tradable object.");
        }
    }

    public String getID() {
        return this.tradableId;
    }


    @Override
    public String toString() {
        String mainDesc;
        if (isQuote) {
            mainDesc = String.format("side quote for %s: %s", product, price.toString());
        } else {
            mainDesc = String.format("side order for %s: Price: %s", product, price.toString());
        }

        return String.format("%s %s %s, Orig Vol: %3d, Rem Vol: %3d, Fill Vol: %3d, Cxl'd Vol: %3d, ID: %s",
                user,
                side.toString(),
                mainDesc,
                originalVolume,
                remainingVolume,
                filledVolume,
                cancelledVolume,
                tradableId
        );
    }
}