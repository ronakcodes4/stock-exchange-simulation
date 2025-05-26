package se450.assignment3.user;

import se450.assignment2.tradable.TradableDTO;
import se450.assignment3.exceptions.DataValidationException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class User {
    private final String userId;
    private final HashMap<String, TradableDTO> tradables;

    public User(String userId) throws DataValidationException {
        validateUserId(userId);
        this.userId = userId.toUpperCase();
        this.tradables = new HashMap<>();
    }

    private void validateUserId(String id) throws DataValidationException {
        if (id == null || !id.matches("[a-zA-Z]{3}")) {
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
        tradables.put(dto.getID(), dto);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("User Id: ").append(userId).append("\n");

        ArrayList<TradableDTO> sortedTradables = new ArrayList<>(tradables.values());
        // Sort by Product (ASCII), then Price (descending), then ID (ASCII)
        Collections.sort(sortedTradables, Comparator
                .comparing(TradableDTO::getProduct)
                .thenComparing(TradableDTO::getPrice, Comparator.reverseOrder())
                .thenComparing(TradableDTO::getID));

        for (TradableDTO dto : sortedTradables) {
            sb.append("\tProduct: ").append(dto.getProduct())
                    .append(", Price: ").append(dto.getPrice().toString())
                    // Expected output uses 2 spaces for padding, or rather, numbers are right-aligned in a field of 2 or 3.
                    // Let's try to match the " Vol: 88" vs " Vol:  8" style.
                    // Using String.format("%2d", ...) might be too tight if volumes go to 3 digits.
                    // The expected output seems to have variable spacing to align columns, which is hard with simple String.format.
                    // For now, let's use a consistent padding that looks reasonable.
                    // The expected output has "OriginalVolume: 88", "RemainingVolume: 0", etc.
                    .append(", OriginalVolume: ").append(String.format("%-3d", dto.getOriginalVolume()).replace(' ', ' ')) // Left align in 3 chars
                    .append(", RemainingVolume: ").append(String.format("%-3d",dto.getRemainingVolume()).replace(' ', ' '))
                    .append(", CancelledVolume: ").append(String.format("%-3d",dto.getCancelledVolume()).replace(' ', ' '))
                    .append(", FilledVolume: ").append(String.format("%-3d",dto.getFilledVolume()).replace(' ', ' '))
                    .append(", User: ").append(dto.getUser())
                    .append(", Side: ").append(dto.getSide().toString())
                    .append(", Id: ").append(dto.getID()).append("\n");
        }
        return sb.toString();
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
