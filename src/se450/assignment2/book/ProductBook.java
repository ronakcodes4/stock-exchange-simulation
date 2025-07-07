package se450.assignment2.book;

import se450.assignment1.price.Price;
import se450.assignment1.exceptions.InvalidPriceException;
import se450.assignment2.exception.InvalidOrderException;
import se450.assignment2.exception.InvalidQuoteException;
import se450.assignment2.quote.Quote;
import se450.assignment2.tradable.Tradable;
import se450.assignment2.tradable.TradableDTO;
import se450.assignment3.exceptions.DataValidationException;
import se450.assignment4.market.CurrentMarketTracker;

import java.util.ArrayList;
import java.util.List;

public class ProductBook {
    private final String product;
    private final ProductBookSide buySide;
    private final ProductBookSide sellSide;

    public ProductBook(String productSymbol) throws DataValidationException {
        if (productSymbol == null || !productSymbol.matches("^[A-Za-z0-9.]{1,5}$")) {
            throw new DataValidationException("Product symbol must be 1-5 letters/numbers or '.' for ProductBook: " + productSymbol);
        }
        this.product = productSymbol;

        this.buySide = new ProductBookSide(this.product, BookSide.BUY);
        this.sellSide = new ProductBookSide(this.product, BookSide.SELL);
    }

    public String getProduct() {
        return product;
    }


    public TradableDTO add(Tradable t) throws DataValidationException {
        if (t == null) {
            throw new DataValidationException("Cannot add null Tradable to ProductBook.");
        }

        ProductBookSide sideToAddTo = (t.getSide() == BookSide.BUY) ? buySide : sellSide;
        TradableDTO dto = sideToAddTo.add(t);

        try {
            tryTrade();
        } catch (InvalidPriceException e) {
            System.err.println("Error during tryTrade after adding tradable " + (t.getId() != null ? t.getId() : "null") + ": " + e.getMessage());
        }
        updateMarket();
        return dto;
    }


    public TradableDTO[] add(Quote q) throws DataValidationException, InvalidOrderException, InvalidQuoteException {
        if (q == null) {
            throw new DataValidationException("Cannot add null Quote to ProductBook.");
        }


        this.removeQuotesForUser(q.getUser());


        TradableDTO buySideDTO = this.add(q.getBuySide());
        TradableDTO sellSideDTO = this.add(q.getSellSide());

        updateMarket();


        return new TradableDTO[]{buySideDTO, sellSideDTO};
    }


    public TradableDTO cancel(BookSide side, String orderId) throws DataValidationException {
        if (side == null || orderId == null || orderId.trim().isEmpty()) {
            throw new DataValidationException("Side and Order ID must be valid for cancel operation.");
        }
        ProductBookSide sideToCancelFrom = (side == BookSide.BUY) ? buySide : sellSide;
        TradableDTO cancelledDTO = sideToCancelFrom.cancel(orderId);

        updateMarket();
        return cancelledDTO;
    }


    public TradableDTO[] removeQuotesForUser(String userName) throws DataValidationException {
        if (userName == null || userName.trim().isEmpty()) {
            throw new DataValidationException("Username must be provided for removeQuotesForUser.");
        }

        ArrayList<TradableDTO> buyCancelled = buySide.removeQuotesForUser(userName);
        ArrayList<TradableDTO> sellCancelled = sellSide.removeQuotesForUser(userName);

        List<TradableDTO> allCancelled = new ArrayList<>();
        if (buyCancelled != null) allCancelled.addAll(buyCancelled);
        if (sellCancelled != null) allCancelled.addAll(sellCancelled);

        TradableDTO[] result = allCancelled.toArray(new TradableDTO[0]);
        updateMarket();
        return result;
    }

    private void tryTrade() throws InvalidPriceException, DataValidationException {
        while (true) {
            Price topBuyPrice = buySide.topOfBookPrice();
            Price topSellPrice = sellSide.topOfBookPrice();

            if (topBuyPrice == null || topSellPrice == null || topBuyPrice.lessThan(topSellPrice)) {
                break;
            }

            int buyVolumeAtTop = buySide.topOfBookVolume();
            int sellVolumeAtTop = sellSide.topOfBookVolume();
            int volumeToTrade = Math.min(buyVolumeAtTop, sellVolumeAtTop);

            if (volumeToTrade <= 0) break;

            Price executionPrice = topBuyPrice;


            List<TradableDTO> sellFills = sellSide.tradeOut(executionPrice, volumeToTrade);
            List<TradableDTO> buyFills = buySide.tradeOut(executionPrice, volumeToTrade);

            for (TradableDTO dto : buyFills) {
                String fillType = (dto.remainingVolume() == 0) ? "FULL FILL" : "PARTIAL FILL";
                System.out.printf("\t%s: (BUY %d) %s %s %s for %s: Price: %s, Orig Vol: %d, Rem Vol: %d, Fill Vol: %d, Cxl'd Vol: %d, ID: %s%n",
                        fillType, dto.originalVolume() - dto.remainingVolume() - dto.cancelledVolume(),
                        dto.user(),
                        dto.side().toString(), (dto.isQuote() ? "side quote" : "side order"), dto.product(),
                        dto.price().toString(), dto.originalVolume(), dto.remainingVolume(),
                        dto.filledVolume(), dto.cancelledVolume(), dto.tradableId());
            }
            for (TradableDTO dto : sellFills) {
                String fillType = (dto.remainingVolume() == 0) ? "FULL FILL" : "PARTIAL FILL";
                System.out.printf("\t%s: (SELL %d) %s %s %s for %s: Price: %s, Orig Vol: %d, Rem Vol: %d, Fill Vol: %d, Cxl'd Vol: %d, ID: %s%n",
                        fillType, dto.originalVolume() - dto.remainingVolume() - dto.cancelledVolume(),
                        dto.user(),
                        dto.side().toString(), (dto.isQuote() ? "side quote" : "side order"), dto.product(),
                        dto.price().toString(), dto.originalVolume(), dto.remainingVolume(),
                        dto.filledVolume(), dto.cancelledVolume(), dto.tradableId());
            }

            if (buyFills.isEmpty() && sellFills.isEmpty() && volumeToTrade > 0) {
                System.err.println("tryTrade Warning: No fills occurred despite cross for " + product +
                        " with tradeable volume " + volumeToTrade +
                        " at BUY price " + (topBuyPrice != null ? topBuyPrice.toString() : "N/A") +
                        " and SELL price " + (topSellPrice != null ? topSellPrice.toString() : "N/A") +
                        ". Execution Price: " + (executionPrice != null ? executionPrice.toString() : "N/A") +
                        ". Breaking trade loop.");
                break;
            }
        }
    }


    public String getTopOfBookString(BookSide side) {
        if (side == null) {

            return "Invalid Side x 0";
        }
        ProductBookSide currentPBSide = (side == BookSide.BUY) ? buySide : sellSide;
        Price topPrice = currentPBSide.topOfBookPrice();
        int topVolume = currentPBSide.topOfBookVolume();
        String priceString = (topPrice == null) ? "$0.00" : topPrice.toString();
        return String.format("%s x %d", priceString, topVolume);
    }


    private void updateMarket() {
        Price buyPrice = buySide.topOfBookPrice();
        int buyVolume = buySide.topOfBookVolume();
        Price sellPrice = sellSide.topOfBookPrice();
        int sellVolume = sellSide.topOfBookVolume();

        CurrentMarketTracker.getInstance().updateMarket(this.product, buyPrice, buyVolume, sellPrice, sellVolume);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("--------------------------------------------\n");
        sb.append("Product Book: ").append(product).append("\n");
        sb.append(buySide.toString());
        sb.append(sellSide.toString());
        sb.append("--------------------------------------------");
        return sb.toString();
    }
}