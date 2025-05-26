package se450.assignment2.tradable;

import se450.assignment1.Price;
import se450.assignment3.book.BookSide; // IMPORTANT: Uses A3 BookSide

public interface Tradable {
    String getId();
    String getUser();
    String getProduct();
    Price getPrice();
    BookSide getSide(); // IMPORTANT: Returns A3 BookSide
    boolean isQuote();

    int getOriginalVolume();
    int getRemainingVolume();
    int getCancelledVolume();
    int getFilledVolume();

    void setRemainingVolume(int newVol);
    void setCancelledVolume(int newVol);
    void setFilledVolume(int newVol);

    TradableDTO makeTradableDTO(); // This will now create a DTO that uses A3 BookSide
}
