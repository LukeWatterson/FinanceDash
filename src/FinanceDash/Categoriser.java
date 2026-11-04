package FinanceDash;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Categoriser {

    public static final String DEFAULT_KEYWORDS_FILE = "keywords.txt";
    private static String keywordsFilePath = DEFAULT_KEYWORDS_FILE;

    private static List<Category> categories;
    private static Map<Category, Set<String>> keywords = new HashMap<>();

    static {
        setUpKeywords();
    }

    public Categoriser() {
        categories = DBM.getAllCategories();
        setUpKeywords();
    }

    public static String getKeywordsFilePath() {
        return keywordsFilePath;
    }

    public static synchronized void setKeywordsFilePath(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            keywordsFilePath = DEFAULT_KEYWORDS_FILE;
        } else {
            keywordsFilePath = filePath.trim();
        }
        setUpKeywords();
    }

    public static int categorise(List<Transaction> transactions) {
        int count = 0;
        if (transactions == null) {
            return 0;
        }
        for (Transaction transaction : transactions) {
            if (categorise(transaction)) {
                count++;
            }
        }
        return count;
    }

    public static Boolean categorise(Transaction transaction) {
        if (transaction == null) {
            return false;
        }
        Category matched = categorise(transaction.getDescription());
        if (matched != null) {
            Category dbCategory = getCategoryByName(matched.getName());
            transaction.setCategory(dbCategory != null ? dbCategory : matched);
            return true;
        }
        return false;
    }

    public static Category categorise(String description) {
        if (description == null || description.trim().isEmpty()) {
            return null;
        }
        if (keywords.isEmpty()) {
            setUpKeywords();
        }
        String descLower = description.toLowerCase();
        Category bestCategory = null;
        int longestMatchLength = -1;

        for (Map.Entry<Category, Set<String>> entry : keywords.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (keyword != null && !keyword.isEmpty() && descLower.contains(keyword.toLowerCase())) {
                    if (keyword.length() > longestMatchLength) {
                        longestMatchLength = keyword.length();
                        bestCategory = entry.getKey();
                    }
                }
            }
        }
        return bestCategory;
    }

    public static boolean categoriseManually(Transaction transaction, Category category) {
        if (transaction == null || category == null) {
            return false;
        }
        Category dbCategory = getCategoryByName(category.getName());
        Category finalCat = dbCategory != null ? dbCategory : category;
        transaction.setCategory(finalCat);
        if (transaction.getDescription() != null && !transaction.getDescription().trim().isEmpty()) {
            addKeyword(finalCat, transaction.getDescription().trim());
        }
        return true;
    }

    public static boolean categoriseManually(Transaction transaction, String categoryName) {
        if (transaction == null || categoryName == null || categoryName.trim().isEmpty()) {
            return false;
        }
        Category cat = getCategoryByName(categoryName.trim());
        if (cat == null) {
            cat = Category.get(categoryName.trim());
        }
        return categoriseManually(transaction, cat);
    }

    public static synchronized void addKeyword(Category category, String keyword) {
        if (category == null || keyword == null) {
            return;
        }
        String cleanKeyword = keyword.trim();
        if (cleanKeyword.isEmpty()) {
            return;
        }
        if (keywords.isEmpty()) {
            setUpKeywords();
        }

        Category targetKey = null;
        for (Category existing : keywords.keySet()) {
            if (existing.getName().equalsIgnoreCase(category.getName())) {
                targetKey = existing;
                break;
            }
        }
        if (targetKey == null) {
            Category dbCat = getCategoryByName(category.getName());
            targetKey = dbCat != null ? dbCat : category;
        }

        Set<String> set = keywords.computeIfAbsent(targetKey, k -> new LinkedHashSet<>());
        boolean alreadyExists = false;
        for (String s : set) {
            if (s.equalsIgnoreCase(cleanKeyword)) {
                alreadyExists = true;
                break;
            }
        }
        if (!alreadyExists) {
            set.add(cleanKeyword);
            saveKeywords();
        }
    }

    public static synchronized void addKeyword(String categoryName, String keyword) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return;
        }
        Category cat = getCategoryByName(categoryName.trim());
        if (cat == null) {
            cat = Category.get(categoryName.trim());
        }
        addKeyword(cat, keyword);
    }

    private static Category getCategoryByName(String name) {
        if (name == null) {
            return null;
        }
        if (categories == null) {
            categories = DBM.getAllCategories();
        }
        if (categories != null) {
            for (Category c : categories) {
                if (c.getName().equalsIgnoreCase(name.trim())) {
                    return c;
                }
            }
        }
        return Category.get(name);
    }

    public List<Category> getCategories() {
        if (categories == null) {
            categories = DBM.getAllCategories();
        }
        return categories;
    }

    public Map<Category, Set<String>> getKeywords() {
        if (keywords.isEmpty()) {
            setUpKeywords();
        }
        return Collections.unmodifiableMap(keywords);
    }

    public static synchronized void setUpKeywords() {
        File file = new File(keywordsFilePath);
        if (file.exists() && file.isFile() && file.length() > 0) {
            loadKeywordsFromFile(file);
        } else {
            resetToDefaultKeywords();
            saveKeywordsToFile(file);
        }
    }

    public static synchronized void resetToDefaultKeywords() {
        keywords.clear();

        keywords.put(Category.FOOD, new LinkedHashSet<>(List.of(
                // Supermarkets
                "tesco", "aldi", "sainsbury", "sainsburys", "lidl",
                "asda", "morrisons", "waitrose", "co-op", "coop",
                "marks spencer", "m&s", "iceland",

                // Takeaways and fast food
                "mcdonalds", "kfc", "burger king", "subway",
                "dominos", "pizza hut", "papa johns",
                "taco bell", "five guys", "nandos", "itsu",
                "tortilla", "papa john's", "deliveroo", "just eat",
                "ubereats", "uber eats", "foodhub", "johns van",
                "pizza pronto", "urban pitta",

                // Coffee shops and cafes
                "starbucks", "costa", "caffe nero", "nero",
                "greggs", "pret", "cafe nero",

                // Sheffield student food and drink
                "university of sheffield", "student union",
                "sheffield su", "the edge", "coffee revolution",
                "ikea food", "sheffield students union", "bulk", "my protein"
        )));

        keywords.put(Category.TRANSPORT, new LinkedHashSet<>(List.of(
                // Public transport
                "tfl", "stagecoach", "first bus", "firstbus",
                "supertram", "travel south yorkshire", "tsyb",
                "trainline", "national rail", "northern rail",
                "northern", "crosscountry", "east midlands railway",
                "emr", "megabus", "national express", "first south yorkshite",

                // Taxis and ride-hailing
                "uber", "bolt", "veezu", "taxi",

                // Driving
                "shell", "bp", "esso", "texaco", "petrol",
                "parking", "ringgo", "paybyphone", "justpark",
                "chargepoint", "ev charging",

                // Cycling
                "lime", "voi", "bike hire"
        )));

        keywords.put(Category.ENTERTAINMENT, new LinkedHashSet<>(List.of(
                // Streaming and subscriptions
                "netflix", "spotify", "disney+", "disney plus",
                "amazon prime", "prime video", "youtube premium",
                "apple music", "tidal", "deezer", "now tv",
                "paramount+", "crunchyroll", "audible",

                // Gaming
                "steam", "playstation", "xbox", "nintendo",
                "epic games", "riot games", "blizzard",
                "minecraft", "humble bundle",

                // Cinema and activities
                "odeon", "cineworld", "showcase", "vue cinema",
                "the light", "ticketmaster", "dice fm",
                "eventbrite", "escape room", "tenpin",
                "rock climbing", "climbing works",

                // Nights out and socialising
                "leadmill", "corporation", "tank nightclub",
                "the union", "pub", "francis newton", "wetherspoon"
        )));

        keywords.put(Category.EXTRAS, new LinkedHashSet<>(List.of(
                // Online shopping
                "amazon", "ebay", "etsy", "aliexpress",
                "temu", "shein", "asos", "vinted",
                "depop", "boohoo", "next", "argos",

                // Technology
                "currys", "apple store", "cex", "game",
                "pc world", "ebuyer", "scan computers",

                // Clothing and general retail
                "primark", "h&m", "uniqlo", "tk maxx",
                "sports direct", "jd sports", "nike",
                "adidas", "foot locker", "dunelm",

                // Household and student essentials
                "ikea", "b&m", "home bargains", "the range",

                // Personal care
                "boots", "superdrug", "lush", "savers",
                "barber", "hairdresser"
        )));
    }

    public static synchronized void saveKeywords() {
        saveKeywordsToFile(new File(keywordsFilePath));
    }

    public static synchronized void saveKeywordsToFile(File file) {
        if (file == null) {
            return;
        }
        try {
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
                for (Map.Entry<Category, Set<String>> entry : keywords.entrySet()) {
                    String catName = entry.getKey().getName();
                    for (String kw : entry.getValue()) {
                        if (kw != null && !kw.trim().isEmpty()) {
                            writer.write(catName + ":" + kw.trim());
                            writer.newLine();
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error saving keywords to " + file.getPath() + ": " + e.getMessage());
        }
    }

    public static synchronized void loadKeywords() {
        loadKeywordsFromFile(new File(keywordsFilePath));
    }

    public static synchronized void loadKeywordsFromFile(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        Map<Category, Set<String>> loaded = new LinkedHashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) {
                    continue;
                }
                int colonIdx = line.indexOf(':');
                if (colonIdx == -1) {
                    colonIdx = line.indexOf('=');
                }
                if (colonIdx != -1) {
                    String catName = line.substring(0, colonIdx).trim();
                    String kw = line.substring(colonIdx + 1).trim();
                    if (!catName.isEmpty() && !kw.isEmpty()) {
                        Category cat = getCategoryByName(catName);
                        if (cat == null) {
                            cat = Category.get(catName);
                        }
                        Category existingKey = null;
                        for (Category c : loaded.keySet()) {
                            if (c.getName().equalsIgnoreCase(cat.getName())) {
                                existingKey = c;
                                break;
                            }
                        }
                        if (existingKey != null) {
                            cat = existingKey;
                        }
                        loaded.computeIfAbsent(cat, k -> new LinkedHashSet<>()).add(kw);
                    }
                }
            }
            if (!loaded.isEmpty()) {
                keywords = loaded;
            }
        } catch (IOException e) {
            System.err.println("Error reading keywords from " + file.getPath() + ": " + e.getMessage());
        }
    }
}
