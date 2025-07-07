package se450.assignment3.manager;

import se450.assignment2.book.ProductBook;
import se450.assignment3.exceptions.DataValidationException;
import se450.assignment2.tradable.Tradable;
import se450.assignment2.tradable.TradableDTO;
import se450.assignment2.quote.Quote;
import se450.assignment2.book.BookSide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class ProductManager {
    private static volatile ProductManager instance;

    private final HashMap<String, ProductBook> productBooks;


    private ProductManager() {
        this.productBooks = new HashMap<>();
    }


    public static ProductManager getInstance() {
        if (instance == null) {
            synchronized (ProductManager.class) {
                if (instance == null) {
                    instance = new ProductManager();
                }
            }
        }
        return instance;
    }


    public void addProduct(String symbol) throws DataValidationException {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new DataValidationException("Product symbol cannot be null or empty.");
        }
        if (!symbol.matches("^[A-Za-z0-9.]{1,5}$")) {
            throw new DataValidationException("Invalid product symbol format: " + symbol +
                    ". Must be 1-5 letters/numbers, can include '.'");
        }
        if (productBooks.containsKey(symbol.toUpperCase())) {
            return;
        }
        productBooks.put(symbol.toUpperCase(), new ProductBook(symbol.toUpperCase()));
    }

    public ProductBook getProductBook(String symbol) throws DataValidationException {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new DataValidationException("Symbol cannot be null or empty for getProductBook.");
        }
        ProductBook book = productBooks.get(symbol.toUpperCase());
        if (book == null) {
            throw new DataValidationException("Product book for symbol '" + symbol.toUpperCase() + "' not found.");
        }
        return book;
    }

    public String getRandomProduct() throws DataValidationException {
        if (productBooks.isEmpty()) {
            throw new DataValidationException("No products available to select randomly.");
        }
        List<String> symbols = new ArrayList<>(productBooks.keySet());
        return symbols.get(new Random().nextInt(symbols.size()));
    }


    public TradableDTO addTradable(Tradable o) throws DataValidationException {
        if (o == null) {
            throw new DataValidationException("Cannot add null Tradable via ProductManager.");
        }
        ProductBook book = getProductBook(o.getProduct());
        try {
            return book.add(o);
        } catch (Exception e) {
            throw new DataValidationException("Failed to add tradable: " + e.getMessage(), e);
        }
    }


    public TradableDTO[] addQuote(Quote q) throws DataValidationException {
        if (q == null) {
            throw new DataValidationException("Cannot add null Quote via ProductManager.");
        }
        ProductBook book = getProductBook(q.getProduct());

        book.removeQuotesForUser(q.getUser());

        TradableDTO buySideDTO = this.addTradable(q.getBuySide());
        TradableDTO sellSideDTO = this.addTradable(q.getSellSide());

        return new TradableDTO[]{buySideDTO, sellSideDTO};
    }

    public TradableDTO cancel(TradableDTO o) throws DataValidationException {
        if (o == null) {
            throw new DataValidationException("Cannot cancel null TradableDTO via ProductManager.");
        }
        ProductBook book = getProductBook(o.product());

        TradableDTO resultDTO = book.cancel(o.side(), o.tradableId());

        if (resultDTO == null) {
            System.out.println("FAILURE TO CANCEL: " + o.toString());
        }
        return resultDTO;
    }


    public TradableDTO[] cancelQuote(String symbol, String user) throws DataValidationException {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new DataValidationException("Symbol cannot be null or empty for cancelQuote.");
        }
        if (user == null || user.trim().isEmpty()) {
            throw new DataValidationException("User cannot be null or empty for cancelQuote.");
        }
        ProductBook book = getProductBook(symbol);

        return book.removeQuotesForUser(user);
    }

    @Override
    public String toString() {
        if (productBooks.isEmpty()) {
            return "<No Product Books Managed>";
        }
        return productBooks.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getValue().toString())
                .collect(Collectors.joining("\n"));
    }
}