package se450.assignment3.book; // A3 Package

import se450.assignment1.Price;
import se450.assignment1.InvalidPriceException;

import se450.assignment2.quote.Quote;
import se450.assignment2.tradable.Tradable;
import se450.assignment2.tradable.TradableDTO;

import se450.assignment3.exceptions.DataValidationException;

import java.util.ArrayList;
import java.util.List;

public class ProductBook {
    private final String product;
    private final ProductBookSide buySide;  // Instance of se450.assignment3.book.ProductBookSide
    private final ProductBookSide sellSide; // Instance of se450.assignment3.book.ProductBookSide

    public ProductBook(String product) throws DataValidationException {
        if (product == null || product.trim().isEmpty() || !product.matches("[A-Za-z0-9.]{1,5}")) {
            throw new DataValidationException("Product symbol is invalid for ProductBook: " + product);
        }
        this.product = product;
        this.buySide = new ProductBookSide(BookSide.BUY, this.product);
        this.sellSide = new ProductBookSide(BookSide.SELL, this.product);
    }

    public String getProduct() {
        return product;
    }

    public TradableDTO add(Tradable t) throws DataValidationException {
        if (t == null) {
            throw new DataValidationException("Tradable cannot be null for ProductBook.add");
        }
        System.out.println("**ADD: " + t.toString());

        ProductBookSide sideToAddTo = (t.getSide() == BookSide.BUY) ? buySide : sellSide;
        TradableDTO dto = sideToAddTo.add(t); // ProductBookSide.add now handles UserManager update
        try {
            tryTrade();
        } catch (InvalidPriceException e) {
            throw new DataValidationException("Error during tryTrade after adding tradable: " + e.getMessage(), e);
        }
        return dto;
    }

    public TradableDTO[] add(Quote q) throws DataValidationException {
        if (q == null) {
            throw new DataValidationException("Quote cannot be null for ProductBook.add");
        }
        removeQuotesForUser(q.getUser());
        TradableDTO buySideDTO = this.add(q.getBuySide());
        TradableDTO sellSideDTO = this.add(q.getSellSide());

        return new TradableDTO[]{buySideDTO, sellSideDTO};
    }

    public TradableDTO cancel(BookSide side, String orderId) throws DataValidationException {
        if (side == null || orderId == null || orderId.trim().isEmpty()) {
            throw new DataValidationException("Side and Order ID must be provided for cancel.");
        }
        ProductBookSide sideToCancelFrom = (side == BookSide.BUY) ? buySide : sellSide;
        return sideToCancelFrom.cancel(orderId);
    }

    public TradableDTO[] removeQuotesForUser(String userName) throws DataValidationException {
        if (userName == null || userName.trim().isEmpty()) {
            throw new DataValidationException("Username must be provided for removeQuotesForUser.");
        }
        ArrayList<TradableDTO> buySideCancelledDTOs = buySide.removeQuotesForUser(userName);
        ArrayList<TradableDTO> sellSideCancelledDTOs = sellSide.removeQuotesForUser(userName);
        List<TradableDTO> allCancelled = new ArrayList<>(buySideCancelledDTOs);
        allCancelled.addAll(sellSideCancelledDTOs);
        return allCancelled.toArray(new TradableDTO[0]);
    }

    private void tryTrade() throws DataValidationException, InvalidPriceException {
        while (true) {
            Price topBuyPrice = buySide.topOfBookPrice();
            Price topSellPrice = sellSide.topOfBookPrice();

            if (topBuyPrice == null || topSellPrice == null || topBuyPrice.lessThan(topSellPrice)) {
                break;
            }

            int buyVolumeAtTop = buySide.topOfBookVolume();
            int sellVolumeAtTop = sellSide.topOfBookVolume();
            int volumeToTrade = Math.min(buyVolumeAtTop, sellVolumeAtTop);

            if (volumeToTrade == 0) break;

            Price executionPrice;
            if (topBuyPrice.equals(topSellPrice)) {
                executionPrice = topBuyPrice;
            } else {
                executionPrice = topSellPrice;
            }

            List<TradableDTO> buyFills = buySide.tradeOut(executionPrice, volumeToTrade);
            List<TradableDTO> sellFills = sellSide.tradeOut(executionPrice, volumeToTrade);

            for (TradableDTO bdto : buyFills) {
                String fillType = (bdto.getRemainingVolume() == 0) ? "FULL FILL" : "PARTIAL FILL";
                System.out.println("\t" + fillType + ": (BUY " + String.format("%3d", bdto.getFilledVolume()) + ") " + bdto.toString());
            }
            for (TradableDTO sdto : sellFills) {
                String fillType = (sdto.getRemainingVolume() == 0) ? "FULL FILL" : "PARTIAL FILL";
                System.out.println("\t" + fillType + ": (SELL " + String.format("%3d", sdto.getFilledVolume()) + ") " + sdto.toString());
            }

            if (buyFills.isEmpty() && sellFills.isEmpty() && volumeToTrade > 0) {
                break;
            }
        }
    }

    public String getTopOfBookString(BookSide side) {
        if (side == null) {
            throw new IllegalArgumentException("Side cannot be null for getTopOfBookString.");
        }
        Price p = (side == BookSide.BUY) ? buySide.topOfBookPrice() : sellSide.topOfBookPrice();
        int vol = (side == BookSide.BUY) ? buySide.topOfBookVolume() : sellSide.topOfBookVolume();
        String priceStr = (p == null) ? "$0.00" : p.toString();
        return String.format("%s x %d", priceStr, vol);
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
