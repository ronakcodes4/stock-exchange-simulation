package se450.assignment4.market;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CurrentMarketPublisher {
    private static volatile CurrentMarketPublisher instance;
    private final HashMap<String, ArrayList<CurrentMarketObserver>> filters;

    private CurrentMarketPublisher() {
        filters = new HashMap<>();
    }

    public static CurrentMarketPublisher getInstance() {
        if (instance == null) {
            synchronized (CurrentMarketPublisher.class) {
                if (instance == null) {
                    instance = new CurrentMarketPublisher();
                }
            }
        }
        return instance;
    }

    public void subscribeCurrentMarket(String symbol, CurrentMarketObserver cmo) {
        if (symbol == null || symbol.trim().isEmpty() || cmo == null) {
            return;
        }
        filters.computeIfAbsent(symbol, k -> new ArrayList<>()).add(cmo);

    }

    public void unSubscribeCurrentMarket(String symbol, CurrentMarketObserver cmo) {
        if (symbol == null || symbol.trim().isEmpty() || cmo == null) {
            return;
        }
        if (filters.containsKey(symbol)) {
            ArrayList<CurrentMarketObserver> observers = filters.get(symbol);
            observers.remove(cmo);
            if (observers.isEmpty()) {
                filters.remove(symbol);
            }
        }
    }

    public void acceptCurrentMarket(String symbol, CurrentMarketSide buySide, CurrentMarketSide sellSide) {
        if (symbol == null || !filters.containsKey(symbol)) {
            return;
        }
        List<CurrentMarketObserver> observers = filters.get(symbol);
        for (CurrentMarketObserver observer : new ArrayList<>(observers)) {
            observer.updateCurrentMarket(symbol, buySide, sellSide);
        }
    }
}