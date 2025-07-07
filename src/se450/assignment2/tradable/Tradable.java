package se450.assignment2.tradable;

import se450.assignment1.price.Price;
import se450.assignment2.book.BookSide;

public interface Tradable {
    String getId();

    String getUser();

    String getProduct();

    Price getPrice();

    BookSide getSide();

    boolean isQuote();

    int getOriginalVolume();

    int getRemainingVolume();

    int getCancelledVolume();

    int getFilledVolume();

    void setRemainingVolume(int newVol);

    void setCancelledVolume(int newVol);

    void setFilledVolume(int newVol);

    TradableDTO makeTradableDTO();
}
