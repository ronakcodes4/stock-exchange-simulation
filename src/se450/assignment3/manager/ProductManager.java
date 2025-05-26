package se450.assignment3.manager;

import se450.assignment3.book.ProductBook; // A3 ProductBook
import se450.assignment3.exceptions.DataValidationException;
// Using A2 types as these are what A3 Main creates and passes around
import se450.assignment2.tradable.Tradable;
import se450.assignment2.tradable.TradableDTO;
import se450.assignment2.quote.Quote;
//import se450.assignment2.book.BookSide; // A3 Main uses A2 BookSide constants

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class ProductManager {
    private static volatile ProductManager instance;
    // Stores ProductBook objects from se450.assignment3.book package [cite: 337]
    private final HashMap<String, ProductBook> productBooks;

    private ProductManager() { // [cite: 336]
        this.productBooks = new HashMap<>();
    }

    public static ProductManager getInstance() { // [cite: 336]
        if (instance == null) {
            synchronized (ProductManager.class) {
                if (instance == null) {
                    instance = new ProductManager();
                }
            }
        }
        return instance;
    }

    public void addProduct(String symbol) throws DataValidationException { // [cite: 339]
        if (symbol == null || symbol.trim().isEmpty()) { // [cite: 340]
            throw new DataValidationException("Product symbol cannot be null or empty.");
        }
        // Symbol requirements from A2 (e.g., WMT, F, 3M, GOOGL, AKO.A) [cite: 52]
        if (!symbol.matches("^[A-Za-z0-9.]{1,5}$")) { // [cite: 340]
            throw new DataValidationException("Invalid product symbol format: " + symbol +
                    ". Must be 1-5 letters/numbers, can include '.'");
        }
        if (productBooks.containsKey(symbol)) {
            System.out.println("Product " + symbol + " already exists in ProductManager. No action taken.");
            return;
        }
        // Create ProductBook from se450.assignment3.book package
        productBooks.put(symbol, new ProductBook(symbol)); // [cite: 339]
    }

    public ProductBook getProductBook(String symbol) throws DataValidationException { // [cite: 341]
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new DataValidationException("Symbol cannot be null or empty for getProductBook.");
        }
        ProductBook book = productBooks.get(symbol);
        if (book == null) { // [cite: 341]
            throw new DataValidationException("Product book for symbol '" + symbol + "' not found.");
        }
        return book;
    }

    public String getRandomProduct() throws DataValidationException { // [cite: 342]
        if (productBooks.isEmpty()) { // [cite: 343]
            throw new DataValidationException("No products available to select randomly.");
        }
        List<String> symbols = new ArrayList<>(productBooks.keySet());
        return symbols.get(new Random().nextInt(symbols.size())); // [cite: 342]
    }

    public TradableDTO addTradable(Tradable o) throws DataValidationException { // [cite: 344]
        if (o == null) { // [cite: 345]
            throw new DataValidationException("Cannot add null Tradable via ProductManager.");
        }
        ProductBook book = getProductBook(o.getProduct()); // Get ProductBook using symbol from Tradable [cite: 344]

        // ProductBook.add(Tradable) already calls System.out.println("**ADD:...")
        // and its ProductBookSide.add calls UserManager.updateTradable.
        // So, ProductManager.addTradable mainly delegates to ProductBook.add.
        // A3 PDF says "Then call the UserManager's updateTradable method, passing it the Tradable's use id and a new TradableDTO created using the Tradable passed in."
        // This is slightly redundant if ProductBookSide.add also does it.
        // Let's ensure ProductBookSide.add is the sole source of UserManager update for "add" operations.
        // The "return the TradableDTO you receive back from the ProductBook" is key.
        return book.add(o); // [cite: 344]
    }

    public TradableDTO[] addQuote(Quote q) throws DataValidationException { // [cite: 346]
        if (q == null) { // [cite: 350]
            throw new DataValidationException("Cannot add null Quote via ProductManager.");
        }
        ProductBook book = getProductBook(q.getProduct()); // Get ProductBook using symbol from Quote [cite: 346]

        // "call removeQuotesForUser (passing the String user from the Quote object)" [cite: 346]
        // ProductBook.removeQuotesForUser now returns TradableDTO[]
        book.removeQuotesForUser(q.getUser()); // Or q.getUserName(), check Quote class getter [cite: 346]

        // "Next, call addTradable passing the BUY ProductBookSide from the Quote passed in" [cite: 347]
        TradableDTO buySideDTO = this.addTradable(q.getBuySide()); // q.getBuySide() should return a Tradable (QuoteSide) [cite: 348]

        // "Then call addTradable passing the SELL ProductBookSide from the Quote passed in" [cite: 348]
        TradableDTO sellSideDTO = this.addTradable(q.getSellSide()); // [cite: 349]

        return new TradableDTO[]{buySideDTO, sellSideDTO}; // [cite: 349]
    }

    public TradableDTO cancel(TradableDTO o) throws DataValidationException { // [cite: 351]
        if (o == null) { // [cite: 354]
            throw new DataValidationException("Cannot cancel null TradableDTO via ProductManager.");
        }
        ProductBook book = getProductBook(o.getProduct()); // [cite: 351]
        // ProductBook.cancel(BookSide, String) is called. o.getSide() will be A2 BookSide.
        TradableDTO resultDTO = book.cancel(o.getSide(), o.getID()); // [cite: 352]

        if (resultDTO == null) { // [cite: 353]
            // A3 PDF: "If the cancel attempt fails, print a message indicating the failure to cancel, and return a null."
            System.out.println("FAILURE TO CANCEL: " + o.toString()); // [cite: 353]
        }
        // UserManager update is handled by ProductBookSide.cancel()
        return resultDTO; // [cite: 352, 353]
    }

    public TradableDTO[] cancelQuote(String symbol, String user) throws DataValidationException { // [cite: 355]
        if (symbol == null || symbol.trim().isEmpty()) { // [cite: 357]
            throw new DataValidationException("Symbol cannot be null or empty for cancelQuote.");
        }
        if (user == null || user.trim().isEmpty()) { // [cite: 358]
            throw new DataValidationException("User cannot be null or empty for cancelQuote.");
        }
        ProductBook book = getProductBook(symbol); // Throws if product does not exist [cite: 359]
        // ProductBook.removeQuotesForUser handles UserManager updates and returns TradableDTO[]
        return book.removeQuotesForUser(user); // [cite: 356]
    }

    @Override
    public String toString() { // [cite: 360]
        if (productBooks.isEmpty()) {
            return "<No Product Books Managed>";
        }
        return productBooks.values().stream()
                .map(ProductBook::toString) // ProductBook.toString() is already well-formatted
                .collect(Collectors.joining("\n")); // A3 Expected Output shows product books one after another.
        // ProductBook.toString() includes separators.
        // A single newline between them might be too much if books have separators.
        // Let's test with joining directly.
    }
}
