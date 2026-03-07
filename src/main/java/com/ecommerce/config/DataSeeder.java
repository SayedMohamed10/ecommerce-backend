package com.ecommerce.config;

import com.ecommerce.model.Category;
import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Only seed if database is empty
        if (productRepository.count() > 0) {
            System.out.println("Database already seeded. Skipping...");
            return;
        }

        System.out.println("Seeding database with sample data...");

        // Create Admin User
        seedAdminUser();

        // Create Categories
        Category electronics = createCategory("Electronics", "Smartphones, laptops, gadgets and more", "https://images.unsplash.com/photo-1498049794561-7780e7231661?w=400", 1);
        Category clothing = createCategory("Clothing", "Men's and women's fashion apparel", "https://images.unsplash.com/photo-1445205170230-053b83016050?w=400", 2);
        Category homeGarden = createCategory("Home & Garden", "Furniture, decor, and garden essentials", "https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=400", 3);
        Category sports = createCategory("Sports & Fitness", "Equipment, activewear, and accessories", "https://images.unsplash.com/photo-1461896836934-bd45ba8fcf9b?w=400", 4);
        Category books = createCategory("Books", "Fiction, non-fiction, and educational books", "https://images.unsplash.com/photo-1495446815901-a7297e633e8d?w=400", 5);
        Category beauty = createCategory("Beauty & Health", "Skincare, makeup, and wellness products", "https://images.unsplash.com/photo-1596462502278-27bfdc403348?w=400", 6);

        // Seed Electronics Products
        createProduct("iPhone 15 Pro Max", "The most powerful iPhone ever with A17 Pro chip, titanium design, and 48MP camera system. Features USB-C, Action button, and up to 29 hours of video playback.",
                "Flagship smartphone with A17 Pro chip", new BigDecimal("1199.99"), new BigDecimal("1099.99"),
                50, electronics, "Apple", "APL-IP15PM-256",
                Arrays.asList("https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=600", "https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=600"),
                Arrays.asList("smartphone", "apple", "iphone", "5g", "flagship"), true, new BigDecimal("0.22"), "160.9 x 77.8 x 8.25 mm");

        createProduct("Samsung Galaxy S24 Ultra", "Samsung's ultimate smartphone with Galaxy AI, S Pen, 200MP camera, and titanium frame. Powered by Snapdragon 8 Gen 3.",
                "AI-powered flagship with S Pen", new BigDecimal("1299.99"), new BigDecimal("1199.99"),
                45, electronics, "Samsung", "SAM-S24U-256",
                Arrays.asList("https://images.unsplash.com/photo-1610945265064-0e34e5519bbf?w=600"),
                Arrays.asList("smartphone", "samsung", "galaxy", "5g", "ai"), true, new BigDecimal("0.23"), "162.3 x 79.0 x 8.6 mm");

        createProduct("MacBook Pro 16\" M3 Max", "Supercharged by M3 Max chip with up to 128GB unified memory. Stunning Liquid Retina XDR display, up to 22 hours battery life.",
                "Pro laptop with M3 Max chip", new BigDecimal("3499.99"), null,
                25, electronics, "Apple", "APL-MBP16-M3MAX",
                Arrays.asList("https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=600"),
                Arrays.asList("laptop", "apple", "macbook", "professional"), true, new BigDecimal("2.14"), "355.7 x 248.1 x 16.8 mm");

        createProduct("Sony WH-1000XM5 Headphones", "Industry-leading noise cancellation with Auto NC Optimizer. 30-hour battery, crystal clear hands-free calling, multipoint connection.",
                "Premium noise-cancelling headphones", new BigDecimal("399.99"), new BigDecimal("328.00"),
                80, electronics, "Sony", "SNY-WH1000XM5",
                Arrays.asList("https://images.unsplash.com/photo-1618366712010-f4ae9c647dcb?w=600"),
                Arrays.asList("headphones", "sony", "noise-cancelling", "wireless", "bluetooth"), false, new BigDecimal("0.25"), "Foldable design");

        createProduct("iPad Air M2", "Supercharged by the M2 chip. 11-inch Liquid Retina display, works with Apple Pencil Pro and Magic Keyboard.",
                "Versatile tablet with M2 chip", new BigDecimal("599.99"), null,
                60, electronics, "Apple", "APL-IPADAIR-M2",
                Arrays.asList("https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=600"),
                Arrays.asList("tablet", "apple", "ipad", "portable"), false, new BigDecimal("0.46"), "247.6 x 178.5 x 6.1 mm");

        createProduct("Dell XPS 15 Laptop", "InfinityEdge display with 3.5K OLED, 13th Gen Intel Core i7, 16GB RAM, 512GB SSD. Ultra-thin premium design.",
                "Premium ultrabook with OLED display", new BigDecimal("1799.99"), new BigDecimal("1599.99"),
                30, electronics, "Dell", "DEL-XPS15-I7",
                Arrays.asList("https://images.unsplash.com/photo-1593642702821-c8da6771f0c6?w=600"),
                Arrays.asList("laptop", "dell", "ultrabook", "oled"), false, new BigDecimal("1.86"), "344.4 x 230.1 x 17.8 mm");

        createProduct("Nintendo Switch OLED", "Vibrant 7-inch OLED screen, wide adjustable stand, wired LAN port, enhanced audio, 64GB internal storage.",
                "Handheld gaming console with OLED screen", new BigDecimal("349.99"), null,
                100, electronics, "Nintendo", "NIN-SWCH-OLED",
                Arrays.asList("https://images.unsplash.com/photo-1612287230202-1ff1d85d1bdf?w=600"),
                Arrays.asList("gaming", "nintendo", "console", "portable"), true, new BigDecimal("0.42"), "242 x 102 x 13.9 mm");

        // Seed Clothing Products
        createProduct("Classic Fit Denim Jacket", "Timeless denim jacket in premium washed cotton. Features button closure, chest pockets, and adjustable waist tabs. Perfect for layering.",
                "Premium washed cotton denim jacket", new BigDecimal("89.99"), new BigDecimal("69.99"),
                120, clothing, "Levi's", "LEV-DNJKT-M",
                Arrays.asList("https://images.unsplash.com/photo-1576995853123-5a10305d93c0?w=600"),
                Arrays.asList("jacket", "denim", "casual", "men"), false, new BigDecimal("0.85"), "Standard fit");

        createProduct("Women's Cashmere Sweater", "Luxuriously soft 100% cashmere crew neck sweater. Available in 8 colors. Relaxed fit for everyday elegance.",
                "100% cashmere crew neck sweater", new BigDecimal("149.99"), new BigDecimal("119.99"),
                75, clothing, "Uniqlo", "UNI-CASH-W",
                Arrays.asList("https://images.unsplash.com/photo-1434389677669-e08b4cda3a5d?w=600"),
                Arrays.asList("sweater", "cashmere", "women", "luxury"), true, new BigDecimal("0.30"), "Relaxed fit");

        createProduct("Athletic Running Shoes", "Lightweight mesh upper with responsive foam cushioning. Ideal for daily running and cross-training. Breathable and supportive.",
                "Lightweight responsive running shoes", new BigDecimal("129.99"), new BigDecimal("99.99"),
                200, clothing, "Nike", "NIK-RUN-001",
                Arrays.asList("https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600"),
                Arrays.asList("shoes", "running", "athletic", "nike"), true, new BigDecimal("0.28"), "US sizes 6-14");

        createProduct("Slim Fit Chino Pants", "Classic chino pants in stretch cotton twill. Slim fit through the leg, sits below the waist. Machine washable.",
                "Stretch cotton slim fit chinos", new BigDecimal("59.99"), null,
                150, clothing, "Dockers", "DOC-CHINO-SF",
                Arrays.asList("https://images.unsplash.com/photo-1473966968600-fa801b869a1a?w=600"),
                Arrays.asList("pants", "chino", "casual", "men", "slim-fit"), false, new BigDecimal("0.45"), "28-40 waist");

        // Seed Home & Garden Products
        createProduct("Minimalist Desk Lamp", "Modern LED desk lamp with adjustable brightness and color temperature. Touch control, USB charging port, memory function.",
                "LED desk lamp with touch control", new BigDecimal("49.99"), new BigDecimal("39.99"),
                90, homeGarden, "BenQ", "BNQ-LAMP-01",
                Arrays.asList("https://images.unsplash.com/photo-1507473885765-e6ed057ab6fe?w=600"),
                Arrays.asList("lamp", "desk", "led", "office", "minimalist"), false, new BigDecimal("0.68"), "150 x 150 x 450 mm");

        createProduct("Indoor Plant Set (3 Pack)", "Curated set of low-maintenance indoor plants including Snake Plant, Pothos, and ZZ Plant. Includes decorative ceramic pots.",
                "3 low-maintenance indoor plants with pots", new BigDecimal("45.99"), null,
                40, homeGarden, "PlantLife", "PL-SET3-IND",
                Arrays.asList("https://images.unsplash.com/photo-1459411552884-841db9b3cc2a?w=600"),
                Arrays.asList("plants", "indoor", "decoration", "home"), true, new BigDecimal("2.50"), "Various sizes");

        createProduct("Velvet Accent Chair", "Mid-century modern accent chair with velvet upholstery and gold-finished metal legs. Comfortable foam padding.",
                "Velvet mid-century accent chair", new BigDecimal("299.99"), new BigDecimal("249.99"),
                20, homeGarden, "West Elm", "WE-CHAIR-VLV",
                Arrays.asList("https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=600"),
                Arrays.asList("chair", "furniture", "velvet", "living-room", "mid-century"), true, new BigDecimal("12.00"), "680 x 710 x 800 mm");

        createProduct("Scented Candle Collection", "Set of 4 hand-poured soy candles in glass jars. Scents: Lavender, Vanilla, Cedar, and Ocean Breeze. 40-hour burn time each.",
                "Set of 4 hand-poured soy candles", new BigDecimal("34.99"), new BigDecimal("27.99"),
                110, homeGarden, "Yankee", "YNK-CNDL-4PK",
                Arrays.asList("https://images.unsplash.com/photo-1602607444873-73e3e190fbba?w=600"),
                Arrays.asList("candles", "scented", "home", "relaxation", "gift"), false, new BigDecimal("1.80"), "4 x 7.5cm diameter jars");

        // Seed Sports Products
        createProduct("Yoga Mat Premium", "Extra thick 6mm non-slip yoga mat with alignment lines. Made from eco-friendly TPE material. Includes carrying strap.",
                "6mm eco-friendly non-slip yoga mat", new BigDecimal("39.99"), new BigDecimal("29.99"),
                150, sports, "Manduka", "MND-YOGA-6MM",
                Arrays.asList("https://images.unsplash.com/photo-1601925260368-ae2f83cf8b7f?w=600"),
                Arrays.asList("yoga", "mat", "fitness", "eco-friendly"), true, new BigDecimal("1.20"), "183 x 61 x 0.6 cm");

        createProduct("Adjustable Dumbbell Set", "Space-saving adjustable dumbbells from 5 to 52.5 lbs each. Quick-change weight selection dial. Replaces 15 sets of dumbbells.",
                "5-52.5 lb adjustable dumbbells (pair)", new BigDecimal("399.99"), new BigDecimal("349.99"),
                35, sports, "Bowflex", "BWF-DMBL-552",
                Arrays.asList("https://images.unsplash.com/photo-1638536532686-d610adfc8e5c?w=600"),
                Arrays.asList("dumbbell", "weights", "strength", "home-gym"), true, new BigDecimal("23.80"), "Each: 43 x 20 x 23 cm");

        createProduct("Insulated Water Bottle 32oz", "Triple-wall vacuum insulated stainless steel. Keeps drinks cold 24hrs or hot 12hrs. BPA-free, leak-proof lid.",
                "32oz vacuum insulated water bottle", new BigDecimal("34.99"), null,
                200, sports, "Hydro Flask", "HF-BOTTLE-32",
                Arrays.asList("https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=600"),
                Arrays.asList("water-bottle", "insulated", "sports", "hydration"), false, new BigDecimal("0.45"), "280 x 89 mm");

        // Seed Books
        createProduct("Atomic Habits by James Clear", "An easy and proven way to build good habits and break bad ones. Over 15 million copies sold worldwide. Hardcover edition.",
                "Build good habits and break bad ones", new BigDecimal("27.00"), new BigDecimal("18.99"),
                200, books, "Avery Publishing", "BK-ATOMIC-HC",
                Arrays.asList("https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=600"),
                Arrays.asList("self-help", "habits", "productivity", "bestseller"), true, new BigDecimal("0.35"), "21 x 14.5 x 2.5 cm");

        createProduct("Clean Code by Robert C. Martin", "A handbook of agile software craftsmanship. Learn to write code that is easy to read, understand, and maintain. Essential for developers.",
                "Agile software craftsmanship handbook", new BigDecimal("49.99"), new BigDecimal("37.99"),
                80, books, "Pearson", "BK-CLNCODE-PB",
                Arrays.asList("https://images.unsplash.com/photo-1532012197267-da84d127e765?w=600"),
                Arrays.asList("programming", "software", "engineering", "coding"), true, new BigDecimal("0.55"), "23.5 x 17.5 x 2.5 cm");

        createProduct("The Design of Everyday Things", "Revised and expanded edition. Don Norman explores the psychology of everyday objects and the principles of good design.",
                "Essential guide to human-centered design", new BigDecimal("18.99"), null,
                65, books, "Basic Books", "BK-DESIGN-PB",
                Arrays.asList("https://images.unsplash.com/photo-1524578271613-d550eacf6090?w=600"),
                Arrays.asList("design", "ux", "psychology", "classic"), false, new BigDecimal("0.32"), "20.3 x 13.5 x 2.0 cm");

        // Seed Beauty Products
        createProduct("Vitamin C Brightening Serum", "20% Vitamin C with Hyaluronic Acid and Vitamin E. Brightens skin, fades dark spots, and boosts collagen. Dermatologist tested.",
                "20% Vitamin C face serum", new BigDecimal("28.99"), new BigDecimal("22.99"),
                130, beauty, "TruSkin", "TS-VITC-30ML",
                Arrays.asList("https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=600"),
                Arrays.asList("skincare", "serum", "vitamin-c", "brightening"), true, new BigDecimal("0.10"), "30ml bottle");

        createProduct("Professional Hair Dryer", "1875W ionic hair dryer with 3 heat settings and 2 speed settings. Includes diffuser and concentrator attachments. Lightweight design.",
                "1875W ionic hair dryer with attachments", new BigDecimal("59.99"), new BigDecimal("44.99"),
                85, beauty, "Dyson", "DYS-DRYER-01",
                Arrays.asList("https://images.unsplash.com/photo-1522338140262-f46f5913618a?w=600"),
                Arrays.asList("hair-dryer", "beauty", "styling", "ionic"), false, new BigDecimal("0.55"), "280 x 90 x 100 mm");

        createProduct("Natural Lip Balm Set (6 Pack)", "Organic beeswax lip balm in 6 flavors: Vanilla, Mint, Berry, Coconut, Honey, and Mango. Moisturizing and long-lasting.",
                "6-pack organic beeswax lip balm", new BigDecimal("12.99"), new BigDecimal("9.99"),
                250, beauty, "Burt's Bees", "BB-LIPBALM-6",
                Arrays.asList("https://images.unsplash.com/photo-1586495777744-4413f21062fa?w=600"),
                Arrays.asList("lip-balm", "organic", "beauty", "gift-set"), false, new BigDecimal("0.12"), "6 x 4.25g tubes");

        System.out.println("Database seeding completed successfully!");
        System.out.println("Categories created: " + categoryRepository.count());
        System.out.println("Products created: " + productRepository.count());
    }

    private void seedAdminUser() {
        if (userRepository.findByEmail("admin@shophub.com").isEmpty()) {
            User admin = new User();
            admin.setName("Admin");
            admin.setEmail("admin@shophub.com");
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setRole(User.Role.ADMIN);
            admin.setEnabled(true);
            admin.setEmailVerified(true);
            userRepository.save(admin);
            System.out.println("Admin user created: admin@shophub.com / Admin@123");
        }
    }

    private Category createCategory(String name, String description, String imageUrl, int displayOrder) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setImageUrl(imageUrl);
        category.setActive(true);
        category.setDisplayOrder(displayOrder);
        return categoryRepository.save(category);
    }

    private void createProduct(String name, String description, String shortDescription,
                               BigDecimal price, BigDecimal discountPrice, int stock,
                               Category category, String brand, String sku,
                               List<String> images, List<String> tags,
                               boolean featured, BigDecimal weight, String dimensions) {
        Product product = new Product();
        product.setName(name);
        product.setSlug(generateSlug(name));
        product.setDescription(description);
        product.setShortDescription(shortDescription);
        product.setPrice(price);
        product.setDiscountPrice(discountPrice);
        product.setStock(stock);
        product.setCategory(category);
        product.setBrand(brand);
        product.setSku(sku);
        product.setImages(images);
        product.setTags(tags);
        product.setFeatured(featured);
        product.setActive(true);
        product.setWeight(weight);
        product.setDimensions(dimensions);
        product.setAverageRating(3.5 + Math.random() * 1.5); // Random rating 3.5-5.0
        product.setReviewCount((int) (Math.random() * 200) + 10); // Random 10-210 reviews
        product.setViewCount((long) (Math.random() * 5000) + 100); // Random views
        product.setSoldCount((int) (Math.random() * 500) + 20); // Random sold count
        productRepository.save(product);
    }

    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
    }
}
