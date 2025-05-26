package se450.assignment3.book; // A3 Package

import se450.assignment1.Price;
import se450.assignment1.InvalidPriceException;

// A2 classes that will be used by A3 Main
// These A2 classes (Order, QuoteSide, TradableDTO) MUST have been modified
// to use se450.assignment3.book.BookSide internally for this to work seamlessly.
import se450.assignment2.tradable.Tradable;
import se450.assignment2.tradable.TradableDTO;

// A3 classes
import se450.assignment3.exceptions.DataValidationException;
import se450.assignment3.manager.UserManager;
// Using BookSide from THIS package (se450.assignment3.book.BookSide)
// No explicit import needed for BookSide if it's in the same package,
// but ensure there's NO import for se450.assignment2.book.BookSide.

import java.util.*;
import java.util.stream.Collectors;

public class ProductBookSide {
    private final BookSide side; // This will now correctly be se450.assignment3.book.BookSide
    private final String product;
    private final TreeMap<Price, ArrayList<Tradable>> bookEntries;

    // Constructor now expects and uses se450.assignment3.book.BookSide
    public ProductBookSide(BookSide side, String product) {
        if (side == null) {
            throw new IllegalArgumentException("BookSide cannot be null.");
        }
        if (product == null || product.trim().isEmpty()) {
            throw new IllegalArgumentException("Product symbol cannot be null or empty for ProductBookSide.");
        }
        this.side = side;
        this.product = product;
        // Comparison with se450.assignment3.book.BookSide.BUY
        if (this.side == BookSide.BUY) {
            this.bookEntries = new TreeMap<>(Comparator.reverseOrder());
        } else {
            this.bookEntries = new TreeMap<>(Comparator.naturalOrder());
        }
    }

    public TradableDTO add(Tradable t) throws DataValidationException {
        if (t == null) {
            throw new DataValidationException("Cannot add null tradable.");
        }
        // CRITICAL: t.getSide() must return se450.assignment3.book.BookSide
        // This requires that your A2 Order/QuoteSide classes were modified.
        if (t.getSide() != this.side) { // Now compares two se450.assignment3.book.BookSide objects
            throw new DataValidationException("Side mismatch for tradable: " + t.getSide() +
                    " (class: " + (t.getSide() != null ? t.getSide().getClass().getName() : "null") + ")" +
                    " does not match book side: " + this.side +
                    " (class: " + this.side.getClass().getName() + ")." +
                    " Ensure Tradable objects (Order/QuoteSide from A2) are modified to use A3 BookSide.");
        }
        if (!t.getProduct().equals(this.product)) {
            throw new DataValidationException("Product mismatch for tradable: " + t.getProduct() +
                    " does not match book product: " + this.product);
        }

        // **ADD:** message is printed by ProductBook.add() in A3.
        // System.out.println("**ADD: " + t.toString()); // This line was in your uploaded A3 ProductBookSide

        bookEntries.computeIfAbsent(t.getPrice(), k -> new ArrayList<>()).add(t);

        TradableDTO dtoAfterAdd = t.makeTradableDTO();
        UserManager.getInstance().updateTradable(t.getUser(), dtoAfterAdd);
        return dtoAfterAdd;
    }

    public TradableDTO cancel(String tradableId) throws DataValidationException {
        if (tradableId == null || tradableId.trim().isEmpty()) {
            throw new DataValidationException("Tradable ID for cancel cannot be null or empty.");
        }
        for (Price price : bookEntries.keySet()) {
            ArrayList<Tradable> tradablesAtPrice = bookEntries.get(price);
            Iterator<Tradable> iterator = tradablesAtPrice.iterator();
            while (iterator.hasNext()) {
                Tradable t = iterator.next();
                if (t.getId().equals(tradableId)) {
                    // The A3 PDF for ProductBookSide.cancel says "first print a message..."
                    System.out.println("**CANCEL: " + t.toString()); // Keep as per spec
                    t.setCancelledVolume(t.getCancelledVolume() + t.getRemainingVolume());
                    t.setRemainingVolume(0);
                    iterator.remove();
                    if (tradablesAtPrice.isEmpty()) {
                        bookEntries.remove(price);
                    }
                    TradableDTO dtoAfterCancel = t.makeTradableDTO();
                    UserManager.getInstance().updateTradable(t.getUser(), dtoAfterCancel);
                    return dtoAfterCancel;
                }
            }
        }
        return null;
    }

    public ArrayList<TradableDTO> removeQuotesForUser(String userName) throws DataValidationException {
        if (userName == null || userName.trim().isEmpty()) {
            throw new DataValidationException("Username for removeQuotesForUser cannot be null or empty.");
        }
        ArrayList<TradableDTO> cancelledQuotesDTOs = new ArrayList<>();
        for (Price price : new ArrayList<>(bookEntries.keySet())) {
            ArrayList<Tradable> tradablesAtPrice = bookEntries.get(price);
            if (tradablesAtPrice == null) continue;
            Iterator<Tradable> iterator = tradablesAtPrice.iterator();
            while (iterator.hasNext()) {
                Tradable t = iterator.next();
                if (t.isQuote() && t.getUser().equals(userName)) {
                    t.setCancelledVolume(t.getCancelledVolume() + t.getRemainingVolume());
                    t.setRemainingVolume(0);
                    iterator.remove();
                    TradableDTO dto = t.makeTradableDTO();
                    cancelledQuotesDTOs.add(dto);
                    UserManager.getInstance().updateTradable(t.getUser(), dto);
                }
            }
            if (tradablesAtPrice.isEmpty()) {
                bookEntries.remove(price);
            }
        }
        return cancelledQuotesDTOs;
    }

    public List<TradableDTO> tradeOut(Price fillPrice, int volumeToFill) throws DataValidationException, InvalidPriceException {
        if (fillPrice == null || volumeToFill <= 0) {
            throw new DataValidationException("Invalid price or volume for tradeOut.");
        }
        List<TradableDTO> affectedTradableDTOs = new ArrayList<>();
        int currentVolumeFilledTotal = 0;
        List<Price> pricesToConsider = new ArrayList<>();

        if (side == BookSide.BUY) { // Uses se450.assignment3.book.BookSide
            pricesToConsider.addAll(bookEntries.keySet().stream()
                    .filter(bookBuyPrice -> {
                        try {
                            return bookBuyPrice.greaterOrEqual(fillPrice);
                        } catch (InvalidPriceException e) {
                            throw new RuntimeException("Unexpected IPE in tradeOut filter", e);
                        }
                    })
                    .collect(Collectors.toList()));
        } else { // SELL side
            pricesToConsider.addAll(bookEntries.keySet().stream()
                    .filter(bookSellPrice -> {
                        try {
                            return bookSellPrice.lessOrEqual(fillPrice);
                        } catch (InvalidPriceException e) {
                            throw new RuntimeException("Unexpected IPE in tradeOut filter", e);
                        }
                    })
                    .collect(Collectors.toList()));
        }

        for (Price currentBookPrice : pricesToConsider) {
            if (currentVolumeFilledTotal >= volumeToFill) break;
            ArrayList<Tradable> tradablesAtThisPrice = bookEntries.get(currentBookPrice);
            if (tradablesAtThisPrice == null) continue;

            Iterator<Tradable> iterator = tradablesAtThisPrice.iterator();
            while (iterator.hasNext()) {
                if (currentVolumeFilledTotal >= volumeToFill) break;
                Tradable t = iterator.next();
                int volumeAvailableInTradable = t.getRemainingVolume();
                int volumeToFillFromThisTradable = Math.min(volumeAvailableInTradable, volumeToFill - currentVolumeFilledTotal);
                if (volumeToFillFromThisTradable > 0) {
                    t.setFilledVolume(t.getFilledVolume() + volumeToFillFromThisTradable);
                    t.setRemainingVolume(t.getRemainingVolume() - volumeToFillFromThisTradable);
                    currentVolumeFilledTotal += volumeToFillFromThisTradable;
                    TradableDTO currentDto = t.makeTradableDTO();
                    affectedTradableDTOs.add(currentDto);
                    UserManager.getInstance().updateTradable(t.getUser(), currentDto);
                    if (t.getRemainingVolume() == 0) {
                        iterator.remove();
                    }
                }
            }
            if (tradablesAtThisPrice.isEmpty()) {
                bookEntries.remove(currentBookPrice);
            }
        }
        return affectedTradableDTOs;
    }

    public Price topOfBookPrice() {
        try {
            return bookEntries.isEmpty() ? null : bookEntries.firstKey();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public int topOfBookVolume() {
        Price topPrice = topOfBookPrice();
        if (topPrice == null) return 0;
        ArrayList<Tradable> topTradables = bookEntries.get(topPrice);
        if (topTradables == null) return 0;
        return topTradables.stream().mapToInt(Tradable::getRemainingVolume).sum();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Side: ").append(side).append("\n"); // side is se450.assignment3.book.BookSide

        boolean emptySideContent = true;
        for (Map.Entry<Price, ArrayList<Tradable>> entry : bookEntries.entrySet()) {
            List<Tradable> tradablesWithVolume = entry.getValue().stream()
                    .filter(t -> t.getRemainingVolume() > 0)
                    .collect(Collectors.toList());

            if (!tradablesWithVolume.isEmpty()) {
                emptySideContent = false;
                sb.append("\t").append(entry.getKey().toString()).append(":\n");
                for (Tradable t : tradablesWithVolume) {
                    sb.append("\t\t").append(t.makeTradableDTO().toString()).append("\n");
                }
            }
        }
        if (emptySideContent) {
            sb.append("\t<Empty>\n");
        }
        return sb.toString();
    }
}
