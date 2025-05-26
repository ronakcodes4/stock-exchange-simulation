package se450.assignment2.order;

import se450.assignment1.Price;
import se450.assignment2.tradable.Tradable;

public class OrderBookEntry {
    private final String id;
    private final String user;
    private final Price price;
    private final int volume;
    private final Tradable tradable;

    /**
     * Wraps a Tradable for book‐entry view.
     * @param tradable non‐null Tradable
     * @throws IllegalArgumentException if tradable or any key field is invalid
     */
    public OrderBookEntry(Tradable tradable) {
        if (tradable == null) {
            throw new IllegalArgumentException("Tradable cannot be null");
        }
        String tId = tradable.getId();
        if (tId == null || tId.isBlank()) {
            throw new IllegalArgumentException("Tradable ID cannot be null or empty");
        }
        String tUser = tradable.getUser();
        if (tUser == null || tUser.isBlank()) {
            throw new IllegalArgumentException("Tradable user cannot be null or empty");
        }
        Price tPrice = tradable.getPrice();
        if (tPrice == null) {
            throw new IllegalArgumentException("Tradable price cannot be null");
        }
        int origVol = tradable.getOriginalVolume();
        if (origVol <= 0) {
            throw new IllegalArgumentException("Tradable original volume must be > 0: " + origVol);
        }

        this.id       = tId;
        this.user     = tUser;
        this.price    = tPrice;
        this.volume   = origVol;
        this.tradable = tradable;
    }

    public Tradable getTradable() { return tradable; }
    public String    getId()       { return id; }
    public String    getUser()     { return user; }
    public Price     getPrice()    { return price; }
    public int       getVolume()   { return volume; }

    @Override
    public String toString() {
        return String.format("%s: %s x %d (%s)", id, price, volume, user);
    }
}