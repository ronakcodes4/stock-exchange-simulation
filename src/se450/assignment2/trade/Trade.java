// Trade.java
package se450.assignment2.trade;

import se450.assignment1.Price;


public class Trade {
    private final String product;
    private final Price price;
    private final String id;
    private final String buyer;
    private final String seller;
    private final int volume;

    /**
     * @param product  1–5 alphanumeric (or “.”) product symbol
     * @param price    non-null execution price ≥ $0.00
     * @param id       non-null, non-blank trade ID
     * @param buyer    3-letter user code of buyer
     * @param seller   3-letter user code of seller
     * @param volume   traded quantity > 0
     * @throws IllegalArgumentException on any invalid argument
     */
    public Trade(String product,
                 Price price,
                 String id,
                 String buyer,
                 String seller,
                 int volume) {
        // product
        if (product == null || product.isBlank()) {
            throw new IllegalArgumentException("Product cannot be null or empty");
        }
        if (!product.matches("[A-Za-z0-9.]{1,5}")) {
            throw new IllegalArgumentException(
                    "Product must be 1–5 letters/digits or '.': " + product);
        }
        // price
        if (price == null) {
            throw new IllegalArgumentException("Price cannot be null");
        }
        if (price.isNegative()) {
            throw new IllegalArgumentException(
                    "Price must be ≥ $0.00: " + price);
        }
        // id
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Trade ID cannot be null or empty");
        }
        // buyer
        if (buyer == null || buyer.isBlank()) {
            throw new IllegalArgumentException("Buyer cannot be null or empty");
        }
        if (!buyer.matches("[A-Za-z]{3}")) {
            throw new IllegalArgumentException(
                    "Buyer code must be exactly 3 letters: " + buyer);
        }
        // seller
        if (seller == null || seller.isBlank()) {
            throw new IllegalArgumentException("Seller cannot be null or empty");
        }
        if (!seller.matches("[A-Za-z]{3}")) {
            throw new IllegalArgumentException(
                    "Seller code must be exactly 3 letters: " + seller);
        }
        // volume
        if (volume <= 0) {
            throw new IllegalArgumentException(
                    "Trade volume must be > 0: " + volume);
        }

        this.product = product;
        this.price   = price;
        this.id      = id;
        this.buyer   = buyer;
        this.seller  = seller;
        this.volume  = volume;
    }

    public String getProduct()  { return product; }
    public Price  getPrice()    { return price; }
    public String getID()       { return id; }
    public String getBuyer()    { return buyer; }
    public String getSeller()   { return seller; }
    public int    getVolume()   { return volume; }

    @Override
    public String toString() {
        return String.format(
                "TRADE - Product: %s | Price: %s | Volume: %d | Buyer: %s | Seller: %s | TradeID: %s",
                product, price, volume, buyer, seller, id
        );
    }
}