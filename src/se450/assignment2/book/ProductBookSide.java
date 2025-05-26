package se450.assignment2.book; // This is your A2 package

import se450.assignment1.Price;
import se450.assignment1.InvalidPriceException; // Assuming this is used by Price methods
import se450.assignment2.exception.InvalidOrderException; // A2 specific exception
import se450.assignment2.tradable.Tradable;
import se450.assignment2.tradable.TradableDTO;
import se450.assignment3.book.BookSide; // KEY CHANGE: Use A3 BookSide enum

import java.util.*;
import java.util.stream.Collectors;

public class ProductBookSide {
    private final BookSide side; // Uses se450.assignment3.book.BookSide
    private final String product; // Product symbol this book side is for

    // Store actual Tradable objects
    private final TreeMap<Price, ArrayList<Tradable>> bookEntries;

    public ProductBookSide(BookSide side, String product) { // Parameter uses A3 BookSide
        if (side == null) {
            throw new IllegalArgumentException("Side cannot be null");
        }
        if (product == null || product.trim().isEmpty()) {
            throw new IllegalArgumentException("Product cannot be null or empty");
        }
        this.side = side;
        this.product = product;

        if (this.side == BookSide.BUY) {
            this.bookEntries = new TreeMap<>(Comparator.reverseOrder());
        } else {
            this.bookEntries = new TreeMap<>(Comparator.naturalOrder());
        }
    }

    public TradableDTO add(Tradable t) {
        if (t == null) {
            throw new IllegalArgumentException("Tradable cannot be null");
        }
        if (t.getPrice() == null) { // Basic check
            throw new IllegalArgumentException("Tradable price cannot be null");
        }
        // t.getSide() will return se450.assignment3.book.BookSide due to prior updates
        if (t.getSide() != this.side) {
            throw new IllegalArgumentException(
                    "Tradable side mismatch: " + t.getSide() + " vs " + this.side);
        }
        if (!t.getProduct().equals(this.product)) {
            throw new IllegalArgumentException(
                    "Product mismatch: " + t.getProduct() + " vs " + this.product);
        }

        System.out.println("**ADD: " + t.toString()); // As per A2 output style

        bookEntries.computeIfAbsent(t.getPrice(), k -> new ArrayList<>()).add(t);
        return t.makeTradableDTO(); // DTO will also use A3 BookSide
    }

    public TradableDTO cancel(String id) throws InvalidOrderException {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty for cancel.");
        }
        for (Price price : bookEntries.keySet()) {
            ArrayList<Tradable> tradablesAtPrice = bookEntries.get(price);
            Iterator<Tradable> iterator = tradablesAtPrice.iterator();
            while (iterator.hasNext()) {
                Tradable t = iterator.next();
                if (t.getId().equals(id)) {
                    System.out.println("**CANCEL: " + t.toString()); // As per A2 output style

                    // Update Tradable state for cancellation
                    t.setCancelledVolume(t.getCancelledVolume() + t.getRemainingVolume());
                    t.setRemainingVolume(0);

                    iterator.remove();
                    if (tradablesAtPrice.isEmpty()) {
                        bookEntries.remove(price);
                    }
                    return t.makeTradableDTO();
                }
            }
        }
        // A2 PDF for ProductBookSide.cancel: "If the tradable is not found, return null."
        // Your A2 code threw InvalidOrderException. Let's stick to A2 PDF for this A2 class.
        return null;
    }

    // This method in A2 PDF for ProductBookSide returned a single TradableDTO.
    // For consistency with how quotes are handled (potentially multiple per user at different prices),
    // it's better if it can handle removing all and returning a collection,
    // but let's stick to A2 PDF for this A2 class if it simplifies.
    // A2 PDF: "return a TradableDTO made from the cancelled quote. If no quote is not found, return null."
    public TradableDTO removeQuotesForUser(String userName) throws InvalidOrderException {
        if (userName == null || userName.trim().isEmpty()) {
            throw new IllegalArgumentException("User cannot be null or empty for removeQuotesForUser.");
        }
        TradableDTO lastCancelledDto = null;
        for (Price price : new ArrayList<>(bookEntries.keySet())) { // Iterate copy for modification
            ArrayList<Tradable> tradablesAtPrice = bookEntries.get(price);
            if (tradablesAtPrice == null) continue;

            Iterator<Tradable> iterator = tradablesAtPrice.iterator();
            while (iterator.hasNext()) {
                Tradable t = iterator.next();
                if (t.isQuote() && t.getUser().equals(userName)) {
                    // System.out.println("**CANCEL (from removeQuotesForUser A2): " + t.toString()); // A2 Main handles cancel messages for quotes via ProductBook

                    t.setCancelledVolume(t.getCancelledVolume() + t.getRemainingVolume());
                    t.setRemainingVolume(0);
                    iterator.remove();
                    lastCancelledDto = t.makeTradableDTO(); // Keep track of the last one for single DTO return
                }
            }
            if (tradablesAtPrice.isEmpty()) {
                bookEntries.remove(price);
            }
        }
        return lastCancelledDto; // Returns DTO of the last quote cancelled for this user on this side, or null
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
        if (topPrice == null || !bookEntries.containsKey(topPrice)) {
            return 0;
        }
        ArrayList<Tradable> topTradables = bookEntries.get(topPrice);
        return topTradables == null ? 0 : topTradables.stream().mapToInt(Tradable::getRemainingVolume).sum();
    }

    public List<TradableDTO> tradeOut(Price priceAtWhichToTrade, int volumeToFill) throws InvalidPriceException {
        if (priceAtWhichToTrade == null) {
            throw new IllegalArgumentException("Price cannot be null for tradeOut");
        }
        if (volumeToFill <= 0) {
            throw new IllegalArgumentException("Volume must be positive for tradeOut");
        }

        List<TradableDTO> filledTradableDTOs = new ArrayList<>();
        int currentTotalVolumeFilledInThisCall = 0;

        // Iterate through tradables at the specified priceAtWhichToTrade
        ArrayList<Tradable> tradablesAtFillPrice = bookEntries.get(priceAtWhichToTrade);
        if (tradablesAtFillPrice != null) {
            Iterator<Tradable> iterator = tradablesAtFillPrice.iterator();
            while (iterator.hasNext()) {
                if (currentTotalVolumeFilledInThisCall >= volumeToFill) break;

                Tradable t = iterator.next();
                int volFromThisTradable = Math.min(t.getRemainingVolume(), volumeToFill - currentTotalVolumeFilledInThisCall);

                if (volFromThisTradable > 0) {
                    t.setFilledVolume(t.getFilledVolume() + volFromThisTradable);
                    t.setRemainingVolume(t.getRemainingVolume() - volFromThisTradable);
                    currentTotalVolumeFilledInThisCall += volFromThisTradable;

                    filledTradableDTOs.add(t.makeTradableDTO());

                    if (t.getRemainingVolume() == 0) {
                        iterator.remove();
                    }
                }
            }
            if (tradablesAtFillPrice.isEmpty()) {
                bookEntries.remove(priceAtWhichToTrade);
            }
        }
        return filledTradableDTOs;
    }

    // For ProductBook.toString() in A2
    public Map<Price, List<TradableDTO>> getBookEntriesGroupedByPrice() {
        Map<Price, List<TradableDTO>> mapForDisplay =
                (side == BookSide.BUY) ? new TreeMap<>(Comparator.reverseOrder()) : new TreeMap<>(Comparator.naturalOrder());

        for (Map.Entry<Price, ArrayList<Tradable>> entry : bookEntries.entrySet()) {
            Price price = entry.getKey();
            ArrayList<Tradable> tradables = entry.getValue();
            if (!tradables.isEmpty()) {
                List<TradableDTO> dtos = tradables.stream()
                        .filter(t -> t.getRemainingVolume() > 0)
                        .map(Tradable::makeTradableDTO)
                        .collect(Collectors.toList());
                if (!dtos.isEmpty()) {
                    mapForDisplay.put(price, dtos);
                }
            }
        }
        return mapForDisplay;
    }

    // A2 ProductBookSide.toString()
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Side: ").append(side).append("\n");
        Map<Price, List<TradableDTO>> depth = getBookEntriesGroupedByPrice();

        if (depth.isEmpty()) {
            sb.append("\t<Empty>\n");
        } else {
            for (Map.Entry<Price, List<TradableDTO>> entry : depth.entrySet()) {
                sb.append("\t").append(entry.getKey().toString()).append(":\n");
                for (TradableDTO dto : entry.getValue()) {
                    sb.append("\t\t").append(dto.toString()).append("\n");
                }
            }
        }
        return sb.toString();
    }
}
