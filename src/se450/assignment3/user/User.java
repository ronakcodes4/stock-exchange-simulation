package se450.assignment3.user;

import se450.assignment2.tradable.TradableDTO;
import se450.assignment3.exceptions.DataValidationException;
import se450.assignment4.market.CurrentMarketObserver;
import se450.assignment4.market.CurrentMarketSide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


public class User implements CurrentMarketObserver {
    private final String userId;
    private final HashMap<String, TradableDTO> tradables;
    private final HashMap<String, CurrentMarketSide[]> currentMarkets;

    public User(String userId) throws DataValidationException {
        validateUserId(userId);
        this.userId = userId.toUpperCase();
        this.tradables = new HashMap<>();
        this.currentMarkets = new HashMap<>();
    }

    private void validateUserId(String id) throws DataValidationException {
        if (id == null || !id.matches("^[a-zA-Z]{3}$")) {
            throw new DataValidationException("User ID must be 3 letters. Received: " + id);
        }
    }

    public String getUserId() {
        return userId;
    }

    public void updateTradable(TradableDTO dto) {
        if (dto == null) {
            return;
        }
        this.tradables.put(dto.tradableId(), dto);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("User Id: ").append(this.userId).append("\n");

        if (!tradables.isEmpty()) {
            List<TradableDTO> sortedTradables = new ArrayList<>(tradables.values());
            Collections.sort(sortedTradables, Comparator
                    .comparing(TradableDTO::product)
                    .thenComparing(TradableDTO::price, Comparator.reverseOrder())
                    .thenComparing(TradableDTO::tradableId));

            for (TradableDTO dto : sortedTradables) {
                sb.append("\tProduct: ").append(dto.product())
                        .append(", Price: ").append(dto.price().toString())
                        .append(", OriginalVolume: ").append(dto.originalVolume())
                        .append(", RemainingVolume: ").append(dto.remainingVolume())
                        .append(", CancelledVolume: ").append(dto.cancelledVolume())
                        .append(", FilledVolume: ").append(dto.filledVolume())
                        .append(", User: ").append(dto.user())
                        .append(", Side: ").append(dto.side().toString())

                        .append(", Id: ").append(dto.tradableId()).append("\n");
            }
        }
        return sb.toString().trim();
    }

    @Override
    public void updateCurrentMarket(String symbol, CurrentMarketSide buySide, CurrentMarketSide sellSide) {
        if (symbol == null) {
            return;
        }
        CurrentMarketSide[] marketSides = {buySide, sellSide};
        this.currentMarkets.put(symbol, marketSides);
    }

    public String getCurrentMarkets() {
        if (currentMarkets.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();


        List<String> sortedSymbols = new ArrayList<>(currentMarkets.keySet());
        Collections.sort(sortedSymbols);

        for (String symbol : sortedSymbols) {
            CurrentMarketSide[] sides = currentMarkets.get(symbol);
            CurrentMarketSide buySide = sides[0];
            CurrentMarketSide sellSide = sides[1];

            String buySideStr = (buySide != null && buySide.getPrice() != null) ? buySide.toString() : "$0.00x0";
            String sellSideStr = (sellSide != null && sellSide.getPrice() != null) ? sellSide.toString() : "$0.00x0";

            sb.append(String.format("%s   %s - %s%n", symbol, buySideStr, sellSideStr));
        }
        return sb.toString().trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}