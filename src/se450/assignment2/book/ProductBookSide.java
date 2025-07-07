package se450.assignment2.book;

import se450.assignment1.price.Price;
import se450.assignment1.exceptions.InvalidPriceException;
import se450.assignment2.tradable.Tradable;
import se450.assignment2.tradable.TradableDTO;
import se450.assignment3.exceptions.DataValidationException;
import se450.assignment3.manager.UserManager;

import java.util.*;
import java.util.stream.Collectors;

public class ProductBookSide {
    private final BookSide side;
    private final String product;
    private final TreeMap<Price, ArrayList<Tradable>> bookEntries;

    public ProductBookSide(String product, BookSide side) throws DataValidationException {
        if (side == null) {
            throw new DataValidationException("BookSide cannot be null for ProductBookSide.");
        }
        if (product == null || product.trim().isEmpty()) {

            throw new DataValidationException("Product symbol cannot be null or empty for ProductBookSide.");
        }
        if (!product.matches("^[A-Za-z0-9.]{1,5}$")) {
            throw new DataValidationException("Invalid product symbol format for ProductBookSide: " + product);
        }

        this.side = side;
        this.product = product;

        if (this.side == BookSide.BUY) {
            this.bookEntries = new TreeMap<>(Comparator.reverseOrder());
        } else {
            this.bookEntries = new TreeMap<>(Comparator.naturalOrder());
        }
    }

    public TradableDTO add(Tradable t) throws DataValidationException {
        if (t == null) {
            throw new DataValidationException("Cannot add null tradable to ProductBookSide.");
        }
        if (t.getSide() != this.side) {
            throw new DataValidationException("Tradable side (" + t.getSide() + ") does not match ProductBookSide (" + this.side + ").");
        }
        if (!t.getProduct().equals(this.product)) {
            throw new DataValidationException("Tradable product (" + t.getProduct() + ") does not match ProductBookSide product (" + this.product + ").");
        }

        bookEntries.computeIfAbsent(t.getPrice(), k -> new ArrayList<>()).add(t);
        TradableDTO dto = t.makeTradableDTO();
        UserManager.getInstance().updateTradable(t.getUser(), dto);
        return dto;
    }

    public TradableDTO cancel(String tradableId) throws DataValidationException {
        if (tradableId == null || tradableId.trim().isEmpty()) {
            throw new DataValidationException("Tradable ID for cancel cannot be null or empty.");
        }

        for (Map.Entry<Price, ArrayList<Tradable>> entry : bookEntries.entrySet()) {
            ArrayList<Tradable> tradablesAtPrice = entry.getValue();
            Iterator<Tradable> iterator = tradablesAtPrice.iterator();
            while (iterator.hasNext()) {
                Tradable t = iterator.next();
                if (t.getId().equals(tradableId)) {
//                    System.out.println("**CANCEL: " + t.toString());

                    int remainingBeforeCancel = t.getRemainingVolume();
                    t.setCancelledVolume(t.getCancelledVolume() + remainingBeforeCancel);
                    t.setRemainingVolume(0);

                    TradableDTO dto = t.makeTradableDTO();
                    UserManager.getInstance().updateTradable(t.getUser(), dto);

                    iterator.remove();
                    if (tradablesAtPrice.isEmpty()) {
                        bookEntries.remove(entry.getKey());
                    }
                    return dto;
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
        List<Price> pricesToIterate = new ArrayList<>(bookEntries.keySet());

        for (Price price : pricesToIterate) {
            ArrayList<Tradable> tradablesAtPrice = bookEntries.get(price);
            if (tradablesAtPrice == null) continue;

            Iterator<Tradable> iterator = tradablesAtPrice.iterator();
            while (iterator.hasNext()) {
                Tradable t = iterator.next();
                if (t.isQuote() && t.getUser().equals(userName)) {
//                    System.out.println("**CANCEL: " + t.toString());

                    int remainingBeforeCancel = t.getRemainingVolume();
                    t.setCancelledVolume(t.getCancelledVolume() + remainingBeforeCancel);
                    t.setRemainingVolume(0);

                    TradableDTO dto = t.makeTradableDTO();
                    cancelledQuotesDTOs.add(dto);
                    UserManager.getInstance().updateTradable(t.getUser(), dto);

                    iterator.remove();
                }
            }
            if (tradablesAtPrice.isEmpty()) {
                bookEntries.remove(price);
            }
        }
        return cancelledQuotesDTOs;
    }

    public Price topOfBookPrice() {
        if (bookEntries.isEmpty()) return null;
        try {
            return bookEntries.firstKey();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public int topOfBookVolume() {
        Price topPrice = topOfBookPrice();
        if (topPrice == null) return 0;

        ArrayList<Tradable> tradablesAtTop = bookEntries.get(topPrice);
        if (tradablesAtTop == null || tradablesAtTop.isEmpty()) return 0;

        return tradablesAtTop.stream().filter(t -> t.getRemainingVolume() > 0).mapToInt(Tradable::getRemainingVolume).sum();
    }

    public List<TradableDTO> tradeOut(Price tradeExecutionPrice, int totalVolumeToTradeFromThisSide)
            throws InvalidPriceException {
        List<TradableDTO> filledDTOs = new ArrayList<>();
        int volumeActuallyFilledThisCall = 0;
        List<Price> priceLevels = new ArrayList<>(bookEntries.keySet());

        for (Price currentBookPrice : priceLevels) {
            if (volumeActuallyFilledThisCall >= totalVolumeToTradeFromThisSide) break;

            boolean canTradeAtThisPrice;
            if (this.side == BookSide.BUY) {
                canTradeAtThisPrice = currentBookPrice.greaterOrEqual(tradeExecutionPrice);
            } else {
                canTradeAtThisPrice = currentBookPrice.lessOrEqual(tradeExecutionPrice);
            }

            if (!canTradeAtThisPrice) {
                if (this.side == BookSide.BUY && currentBookPrice.lessThan(tradeExecutionPrice)) break;
                if (this.side == BookSide.SELL && currentBookPrice.greaterThan(tradeExecutionPrice)) break;
                continue;
            }

            ArrayList<Tradable> tradablesAtThisPrice = bookEntries.get(currentBookPrice);
            if (tradablesAtThisPrice == null || tradablesAtThisPrice.isEmpty()) continue;

            long totalVolumeAvailableAtThisLevelForRatio = 0;
            for (Tradable t : tradablesAtThisPrice) {
                if (t.getRemainingVolume() > 0) totalVolumeAvailableAtThisLevelForRatio += t.getRemainingVolume();
            }
            if (totalVolumeAvailableAtThisLevelForRatio == 0) continue;


            int volumeNeededToFillForThisCall = totalVolumeToTradeFromThisSide - volumeActuallyFilledThisCall;

            if (volumeNeededToFillForThisCall >= totalVolumeAvailableAtThisLevelForRatio) {

                for (Tradable t : new ArrayList<>(tradablesAtThisPrice)) {
                    if (volumeActuallyFilledThisCall >= totalVolumeToTradeFromThisSide) break;
                    if (t.getRemainingVolume() == 0) continue;

                    int fillAmountForT = t.getRemainingVolume();

                    t.setFilledVolume(t.getFilledVolume() + fillAmountForT);
                    t.setRemainingVolume(0);

                    TradableDTO dto = t.makeTradableDTO();
                    filledDTOs.add(dto);
                    UserManager.getInstance().updateTradable(t.getUser(), dto);

                    volumeActuallyFilledThisCall += fillAmountForT;
                    tradablesAtThisPrice.remove(t);
                }
            } else {

                int volumeToDistributeProRata = volumeNeededToFillForThisCall;
                int filledThisLevelProRata = 0;

                List<Tradable> tradablesToProcessCopy = new ArrayList<>(tradablesAtThisPrice);

                for (int i = 0; i < tradablesToProcessCopy.size(); ++i) {
                    Tradable t = tradablesToProcessCopy.get(i);

                    if (filledThisLevelProRata >= volumeToDistributeProRata) break;
                    if (t.getRemainingVolume() == 0) continue;

                    int actualFillForT;
                    if (i == tradablesToProcessCopy.size() - 1) {
                        actualFillForT = volumeToDistributeProRata - filledThisLevelProRata;
                    } else {
                        double ratio = (double) t.getRemainingVolume() / totalVolumeAvailableAtThisLevelForRatio;
                        actualFillForT = (int) Math.ceil(volumeToDistributeProRata * ratio);
                    }

                    actualFillForT = Math.min(actualFillForT, t.getRemainingVolume());
                    actualFillForT = Math.min(actualFillForT, volumeToDistributeProRata - filledThisLevelProRata);

                    if (actualFillForT > 0) {
                        t.setFilledVolume(t.getFilledVolume() + actualFillForT);
                        t.setRemainingVolume(t.getRemainingVolume() - actualFillForT);

                        TradableDTO dto = t.makeTradableDTO();
                        filledDTOs.add(dto);
                        UserManager.getInstance().updateTradable(t.getUser(), dto);

                        volumeActuallyFilledThisCall += actualFillForT;
                        filledThisLevelProRata += actualFillForT;

                        if (t.getRemainingVolume() == 0) {
                            tradablesAtThisPrice.remove(t);
                        }
                    }
                }
            }
            if (tradablesAtThisPrice.isEmpty()) {
                bookEntries.remove(currentBookPrice);
            }
        }
        return filledDTOs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Side: ").append(side).append("\n");

        if (bookEntries.isEmpty()) {
            sb.append("\t<Empty>\n");
        } else {
            boolean hasDisplayableContent = false;
            for (Map.Entry<Price, ArrayList<Tradable>> entry : bookEntries.entrySet()) {
                List<Tradable> displayableTradables = entry.getValue().stream()
                        .filter(t -> t.getRemainingVolume() > 0)
                        .collect(Collectors.toList());

                if (!displayableTradables.isEmpty()) {
                    hasDisplayableContent = true;
                    sb.append("\t").append(entry.getKey().toString()).append(":\n");
                    for (Tradable t : displayableTradables) {
                        sb.append("\t\t").append(t.makeTradableDTO().toString()).append("\n");
                    }
                }
            }
            if (!hasDisplayableContent) {
                sb.append("\t<Empty>\n");
            }
        }
        return sb.toString();
    }
}