package se450.assignment4.market;

import se450.assignment1.price.Price;
import se450.assignment1.exceptions.InvalidPriceException;
import se450.assignment1.price.PriceFactory;

public class CurrentMarketTracker {
    private static volatile CurrentMarketTracker instance;

    private CurrentMarketTracker() {
    }

    public static CurrentMarketTracker getInstance() {
        if (instance == null) {
            synchronized (CurrentMarketTracker.class) {
                if (instance == null) {
                    instance = new CurrentMarketTracker();
                }
            }
        }
        return instance;
    }

    public void updateMarket(String symbol, Price buyPrice, int buyVolume, Price sellPrice, int sellVolume) {
        CurrentMarketSide buySide = new CurrentMarketSide(buyPrice, buyVolume);
        CurrentMarketSide sellSide = new CurrentMarketSide(sellPrice, sellVolume);

        Price marketWidth = null;
        try {
            if (buyPrice != null && sellPrice != null) {
                marketWidth = sellPrice.subtract(buyPrice);
            } else {
                marketWidth = PriceFactory.makePrice(0);
            }
        } catch (InvalidPriceException e) {
            System.err.println("Error calculating market width: " + e.getMessage());
            try {
                marketWidth = PriceFactory.makePrice(0);
            } catch (Exception priceEx) {

            }
        } catch (Exception e) {
            System.err.println("Error calculating market width with A3 PriceFactory: " + e.getMessage());
            try {
                marketWidth = PriceFactory.makePrice("0");
            } catch (Exception priceEx) {

            }
        }


        String buySideString = (buyPrice != null) ? buySide.toString() : "$0.00x0";
        String sellSideString = (sellPrice != null) ? sellSide.toString() : "$0.00x0";
        String marketWidthString = (marketWidth != null) ? marketWidth.toString() : "[$0.00]";
        if (marketWidth != null && !marketWidthString.startsWith("[")) {
            marketWidthString = "[" + marketWidthString + "]";
        }


        System.out.println("*********** Current Market ***********");
        System.out.printf("* %s %s - %s %s%n",
                symbol,
                buySideString,
                sellSideString,
                marketWidthString);
        System.out.println("**************************************");

        CurrentMarketPublisher.getInstance().acceptCurrentMarket(symbol, buySide, sellSide);
    }
}