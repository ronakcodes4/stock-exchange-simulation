package se450.assignment2.book; // This is your A2 package

import se450.assignment1.Price;
import se450.assignment1.InvalidPriceException; // For throws clause
import se450.assignment2.exception.InvalidOrderException;
import se450.assignment2.quote.Quote;
import se450.assignment2.tradable.Tradable;
import se450.assignment2.tradable.TradableDTO;
import se450.assignment3.book.BookSide; // KEY CHANGE: Use A3 BookSide enum

import java.util.ArrayList;
import java.util.List;
//import java.util.Map;

public class ProductBook {
    private final String product;
    private final ProductBookSide buySide;  // This is se450.assignment2.book.ProductBookSide
    private final ProductBookSide sellSide; // This is se450.assignment2.book.ProductBookSide

    public ProductBook(String productSymbol) throws IllegalArgumentException { // Matched A2 constructor
        if (productSymbol == null || productSymbol.trim().isEmpty()) { // Basic validation
            throw new IllegalArgumentException("Product symbol cannot be null or empty for ProductBook");
        }
        // A2 PDF: "must be 1 to 5 letters/numbers, they can also have a period"
        if (!productSymbol.matches("^[A-Za-z0-9.]{1,5}$")) {
            throw new IllegalArgumentException("Invalid product symbol format for ProductBook: " + productSymbol);
        }
        this.product = productSymbol;
        // ProductBookSide constructor now expects se450.assignment3.book.BookSide
        this.buySide = new ProductBookSide(BookSide.BUY, this.product);
        this.sellSide = new ProductBookSide(BookSide.SELL, this.product);
    }

    public String getProduct() { // Added for completeness, though not in A2 PDF for ProductBook
        return product;
    }

    public TradableDTO add(Tradable t) throws InvalidOrderException { // A2 PDF has this method signature
        if (t == null) {
            throw new IllegalArgumentException("Tradable cannot be null for ProductBook.add");
        }
        System.out.println("**ADD: " + t); // A2 PDF: "First print a message..."

        // t.getSide() will return se450.assignment3.book.BookSide
        ProductBookSide sideToAddTo = (t.getSide() == BookSide.BUY) ? buySide : sellSide;
        TradableDTO dto = sideToAddTo.add(t); // This add is ProductBookSide.add(Tradable)

        try {
            tryTrade();
        } catch (InvalidPriceException e) {
            // Handle or propagate. For A2, maybe wrap or log.
            System.err.println("Warning: InvalidPriceException during tryTrade in A2 ProductBook: " + e.getMessage());
        }
        return dto;
    }

    public TradableDTO[] add(Quote q) throws InvalidOrderException { // A2 PDF signature
        if (q == null) {
            throw new IllegalArgumentException("Quote cannot be null for ProductBook.add");
        }
        // A2 PDF: "First, call removeQuotesForUser"
        // removeQuotesForUser in A2 ProductBookSide returns a single DTO or null.
        // We need to call it on both sides.
        TradableDTO removedBuy = buySide.removeQuotesForUser(q.getUser()); // or q.getUserName()
        TradableDTO removedSell = sellSide.removeQuotesForUser(q.getUser());
        // The A2 PDF is a bit inconsistent here on what removeQuotesForUser returns and how it's used.

        // A2 PDF: "Call the "add" method of the BUY ProductBookSide...save the TradableDTO"
        // This should be ProductBookSide.add(Tradable), not ProductBook.add(Tradable) to avoid recursive tryTrade.
        TradableDTO buySideDTO = buySide.add(q.getBuySide()); // Assuming Quote.getBuySide() returns Tradable (QuoteSide)
        TradableDTO sellSideDTO = sellSide.add(q.getSellSide());

        try {
            tryTrade();
        } catch (InvalidPriceException e) {
            System.err.println("Warning: InvalidPriceException during tryTrade in A2 ProductBook: " + e.getMessage());
        }
        return new TradableDTO[]{buySideDTO, sellSideDTO};
    }

    public TradableDTO cancel(BookSide side, String orderId) throws InvalidOrderException { // Parameter uses A3 BookSide
        if (side == null || orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Side and Order ID must be provided for cancel.");
        }
        ProductBookSide sideToCancelFrom = (side == BookSide.BUY) ? buySide : sellSide;
        return sideToCancelFrom.cancel(orderId); // This calls A2 ProductBookSide.cancel
    }

    public TradableDTO[] removeQuotesForUser(String userName) throws InvalidOrderException {
        if (userName == null || userName.trim().isEmpty()) {
            throw new IllegalArgumentException("Username must be provided for removeQuotesForUser.");
        }
        // A2 ProductBookSide.removeQuotesForUser returns a single DTO.
        TradableDTO buyCancelled = buySide.removeQuotesForUser(userName);
        TradableDTO sellCancelled = sellSide.removeQuotesForUser(userName);

        List<TradableDTO> cancelledList = new ArrayList<>();
        if (buyCancelled != null) cancelledList.add(buyCancelled);
        if (sellCancelled != null) cancelledList.add(sellCancelled);

        return cancelledList.toArray(new TradableDTO[0]);
    }

    public void tryTrade() throws InvalidPriceException { // Add throws for Price comparison methods
        while (true) {
            Price topBuyPrice = buySide.topOfBookPrice();
            Price topSellPrice = sellSide.topOfBookPrice();

            if (topBuyPrice == null || topSellPrice == null) break;

            // A2 PDF Appendix A: "Top BUY or SELL price is null?" -> return
            // "Top SELL > top BUY?" (means no cross) -> return
            // This implies if topBuyPrice < topSellPrice, no trade.
            if (topBuyPrice.lessThan(topSellPrice)) { // throws InvalidPriceException
                break;
            }

            // Books are crossed or touching: topBuyPrice >= topSellPrice
            // A2 PDF Appendix A: "Calculate the 'toTrade' amount as the min of the BUY & SELL top of book volumes."
            int buyVolumeAtTop = buySide.topOfBookVolume();
            int sellVolumeAtTop = sellSide.topOfBookVolume();
            int volumeToTrade = Math.min(buyVolumeAtTop, sellVolumeAtTop);

            if (volumeToTrade == 0) break;

            // A2 PDF Appendix A: "Call the 'tradeOut' method of the SELL product book side,
            // passing the top of BUY book price and the toTrade volume."
            // AND "Call the 'tradeOut' method of the BUY product book side,
            // passing the top of BUY book price and the toTrade volume."
            // This is a specific (and somewhat unusual) rule from the PDF.
            // It means both sides try to trade out their volume AT THE BEST BID PRICE.
            Price tradeExecutionPrice = topBuyPrice;

            List<TradableDTO> sellFills = sellSide.tradeOut(tradeExecutionPrice, volumeToTrade);
            List<TradableDTO> buyFills = buySide.tradeOut(tradeExecutionPrice, volumeToTrade);

            // Print fill messages as per A2 Main output style
            for (TradableDTO bdto : buyFills) {
                String fillType = (bdto.getRemainingVolume() == 0) ? "FULL FILL" : "PARTIAL FILL";
                // A2 output: "FULL FILL: (BUY 50) DEM BUY order: TGT at $134.00, Orig Vol: 50, Rem Vol: 0, Fill Vol: 50, CXL Vol: 0, ID: ..."
                // The (BUY XX) part is the volume filled FOR THAT DTO.
                // The DTO's getFilledVolume() is cumulative.
                // The A2 Main output's (BUY XX) seems to be the total filled volume for that order.
                System.out.println("\t" + fillType + ": (BUY " + String.format("%2d", bdto.getFilledVolume()) + ") " + bdto.toString());
            }
            for (TradableDTO sdto : sellFills) {
                String fillType = (sdto.getRemainingVolume() == 0) ? "FULL FILL" : "PARTIAL FILL";
                System.out.println("\t" + fillType + ": (SELL " + String.format("%2d", sdto.getFilledVolume()) + ") " + sdto.toString());
            }

            if (buyFills.isEmpty() && sellFills.isEmpty() && volumeToTrade > 0) {
                // If volumeToTrade was > 0 but no fills happened (e.g., tradeOut logic didn't match any)
                break;
            }
        }
    }

    public String getTopOfBookString(BookSide side) { // Parameter uses A3 BookSide
        if (side == null) {
            throw new IllegalArgumentException("Side cannot be null for getTopOfBookString.");
        }
        Price p = (side == BookSide.BUY) ? buySide.topOfBookPrice() : sellSide.topOfBookPrice();
        int vol = (side == BookSide.BUY) ? buySide.topOfBookVolume() : sellSide.topOfBookVolume();
        String priceStr = (p == null) ? "$0.00" : p.toString();
        // A2 output: "Top of BUY book: Top of BUY book: $133.95 x 80"
        return String.format("Top of %s book: %s x %d", side.toString(), priceStr, vol);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("--------------------------------------------\n");
        sb.append("Product Book: ").append(product).append("\n");
        // ProductBookSide.toString() from A2 handles its own formatting including "Side: BUY/SELL"
        sb.append(buySide.toString()); // Calls A2 ProductBookSide.toString()
        sb.append(sellSide.toString());// Calls A2 ProductBookSide.toString()
        sb.append("--------------------------------------------");
        return sb.toString();
    }
}
