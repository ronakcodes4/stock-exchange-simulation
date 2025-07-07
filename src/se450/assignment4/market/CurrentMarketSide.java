package se450.assignment4.market;

import se450.assignment1.price.Price;

public class CurrentMarketSide {
    private final Price price;
    private final int volume;

    public CurrentMarketSide(Price price, int volume) {
        this.price = price;
        this.volume = volume;
    }

    public Price getPrice() {
        return price;
    }

    public int getVolume() {
        return volume;
    }

    @Override
    public String toString() {
        if (price == null) {
            return "$0.00x0";
        }
        return String.format("%sx%d", price.toString(), volume);
    }
}