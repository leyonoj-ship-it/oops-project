package com.smartdine.config;

import com.smartdine.model.*;
import com.smartdine.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class DataInitializer implements CommandLineRunner {

    private final PersonRepository personRepository;
    private final CustomerRepository customerRepository;
    private final AdminRepository adminRepository;
    private final EstablishmentOwnerRepository establishmentOwnerRepository;
    private final EstablishmentRepository establishmentRepository;
    private final FoodCategoryRepository foodCategoryRepository;
    private final FoodItemRepository foodItemRepository;
    private final DiningTableRepository diningTableRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final ClearanceOfferRepository clearanceOfferRepository;
    private final OfferRepository offerRepository;

    public DataInitializer(PersonRepository personRepository,
                           CustomerRepository customerRepository,
                           AdminRepository adminRepository,
                           EstablishmentOwnerRepository establishmentOwnerRepository,
                           EstablishmentRepository establishmentRepository,
                           FoodCategoryRepository foodCategoryRepository,
                           FoodItemRepository foodItemRepository,
                           DiningTableRepository diningTableRepository,
                           OrderRepository orderRepository,
                           ReviewRepository reviewRepository,
                           ClearanceOfferRepository clearanceOfferRepository,
                           OfferRepository offerRepository) {
        this.personRepository = personRepository;
        this.customerRepository = customerRepository;
        this.adminRepository = adminRepository;
        this.establishmentOwnerRepository = establishmentOwnerRepository;
        this.establishmentRepository = establishmentRepository;
        this.foodCategoryRepository = foodCategoryRepository;
        this.foodItemRepository = foodItemRepository;
        this.diningTableRepository = diningTableRepository;
        this.orderRepository = orderRepository;
        this.reviewRepository = reviewRepository;
        this.clearanceOfferRepository = clearanceOfferRepository;
        this.offerRepository = offerRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (personRepository.count() > 0) {
            return; // Data already initialized
        }

        // ==========================================
        // 1. SEED USERS (Inheritance hierarchy)
        // ==========================================
        Customer customer1 = new Customer("Aarav Sharma", "customer@smartdine.com", "9845012345", "Indiranagar 100ft Road, Bengaluru");
        customer1.addLoyaltyPoints(120);
        customer1.setPreferredAmbiance("CANDLE_LIGHT");
        customerRepository.save(customer1);

        Customer customer2 = new Customer("Neha Kapoor", "neha@smartdine.com", "9876543211", "Koramangala 4th Block, Bengaluru");
        customer2.addLoyaltyPoints(65);
        customerRepository.save(customer2);

        Admin admin = new Admin("Dr. Priya Rao", "admin@smartdine.com", "9800112233", "Platform Operations & Moderation", 5);
        adminRepository.save(admin);

        EstablishmentOwner hotelOwner1 = new EstablishmentOwner("Chef Vikram Malhotra", "hotel@smartdine.com", "9811223344");
        establishmentOwnerRepository.save(hotelOwner1);

        EstablishmentOwner canteenOwner = new EstablishmentOwner("Ramesh Gupta", "canteen@smartdine.com", "9822334455");
        establishmentOwnerRepository.save(canteenOwner);

        EstablishmentOwner hotelOwner2 = new EstablishmentOwner("Siddharth Roy", "roy@smartdine.com", "9833445566");
        establishmentOwnerRepository.save(hotelOwner2);

        // ==========================================
        // 2. SEED ESTABLISHMENTS (HOTEL and CANTEEN unified!)
        // ==========================================
        // 1. The Grand Taj Heritage (HOTEL)
        Establishment est1 = new Establishment(
                "The Grand Taj Heritage Hotel",
                EstablishmentType.HOTEL,
                "Award-winning luxury dining experience with regal ambiance, gourmet curries, tender steaks, and romantic candle light arrangements.",
                "MG Road, Central Business District",
                "Bengaluru",
                12.9752,
                77.6080,
                "+91 80 4123 4567",
                "11:00",
                "23:00",
                "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=1000&q=80"
        );
        est1.setOwner(hotelOwner1);
        est1.setRating(4.8);
        est1.setReviewCount(142);
        est1.getSupportedAmbiances().addAll(Arrays.asList(
                AmbianceType.CANDLE_LIGHT,
                AmbianceType.ROMANTIC,
                AmbianceType.PREMIUM,
                AmbianceType.FAMILY
        ));
        establishmentRepository.save(est1);

        // 2. Campus Central Canteen (CANTEEN)
        Establishment est2 = new Establishment(
                "Campus Central Canteen",
                EstablishmentType.CANTEEN,
                "Vibrant college campus canteen offering affordable, fast, delicious snacks, biriyanis, cold coffee, and south Indian delicacies.",
                "University Campus, East Wing Boulevard",
                "Bengaluru",
                12.9698,
                77.5912,
                "+91 80 2299 8877",
                "08:00",
                "21:30",
                "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?auto=format&fit=crop&w=1000&q=80"
        );
        est2.setOwner(canteenOwner);
        est2.setRating(4.3);
        est2.setReviewCount(310);
        est2.getSupportedAmbiances().addAll(Arrays.asList(
                AmbianceType.CASUAL,
                AmbianceType.FRIENDS,
                AmbianceType.FAMILY
        ));
        establishmentRepository.save(est2);

        // 3. Royal Spice Multicuisine (HOTEL)
        Establishment est3 = new Establishment(
                "Royal Spice Multicuisine Restaurant",
                EstablishmentType.HOTEL,
                "Famous for fragrant dum biriyanis, sizzling paneer platters, mocktails, and festive family celebration dining.",
                "100ft Road, Indiranagar",
                "Bengaluru",
                12.9784,
                77.6408,
                "+91 80 4321 9876",
                "12:00",
                "23:30",
                "https://images.unsplash.com/photo-1552566626-52f8b828add9?auto=format&fit=crop&w=1000&q=80"
        );
        est3.setOwner(hotelOwner2);
        est3.setRating(4.6);
        est3.setReviewCount(88);
        est3.getSupportedAmbiances().addAll(Arrays.asList(
                AmbianceType.FAMILY,
                AmbianceType.BIRTHDAY,
                AmbianceType.ROMANTIC,
                AmbianceType.CASUAL
        ));
        establishmentRepository.save(est3);

        // 4. Engineering Cafeteria & Bakery (CANTEEN)
        Establishment est4 = new Establishment(
                "Engineering Cafeteria & Bakery",
                EstablishmentType.CANTEEN,
                "Student-favourite hangout spot serving hot grilled sandwiches, rolls, freshly brewed cold coffee, and bakery treats.",
                "Tech Quadrangle, Gate 3",
                "Bengaluru",
                12.9650,
                77.5850,
                "+91 80 2345 6789",
                "07:30",
                "20:00",
                "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?auto=format&fit=crop&w=1000&q=80"
        );
        est4.setRating(4.4);
        est4.setReviewCount(195);
        est4.getSupportedAmbiances().addAll(Arrays.asList(
                AmbianceType.CASUAL,
                AmbianceType.QUIET_DINING
        ));
        establishmentRepository.save(est4);

        // 5. Oasis Garden Bistro (HOTEL)
        Establishment est5 = new Establishment(
                "Oasis Garden Bistro",
                EstablishmentType.HOTEL,
                "Serene open-air garden restaurant surrounded by lush greenery, fairy lights, handmade pastas, and artisan mocktails.",
                "Lavelle Road, Shanthala Nagar",
                "Bengaluru",
                12.9680,
                77.5990,
                "+91 80 4567 8901",
                "12:00",
                "23:00",
                "https://images.unsplash.com/photo-1414235077428-338989a2e8c0?auto=format&fit=crop&w=1000&q=80"
        );
        est5.setRating(4.7);
        est5.setReviewCount(76);
        est5.getSupportedAmbiances().addAll(Arrays.asList(
                AmbianceType.OUTDOOR,
                AmbianceType.CANDLE_LIGHT,
                AmbianceType.ROMANTIC,
                AmbianceType.QUIET_DINING
        ));
        establishmentRepository.save(est5);

        // ==========================================
        // 3. SEED DINING TABLES
        // ==========================================
        for (Establishment est : Arrays.asList(est1, est2, est3, est4, est5)) {
            for (int i = 1; i <= 15; i++) {
                AmbianceType tableAmbiance = (i <= 4) ? AmbianceType.CANDLE_LIGHT :
                                             (i <= 8) ? AmbianceType.FAMILY :
                                             (i <= 12) ? AmbianceType.ROMANTIC : AmbianceType.DEFAULT;
                int capacity = (i % 3 == 0) ? 6 : (i % 2 == 0) ? 4 : 2;
                DiningTable table = new DiningTable(i, capacity, tableAmbiance);
                table.setEstablishment(est);
                diningTableRepository.save(table);
            }
        }

        // ==========================================
        // 4. SEED FOOD CATEGORIES
        // ==========================================
        FoodCategory catStarters = foodCategoryRepository.save(new FoodCategory("Starters & Appetizers", 1));
        FoodCategory catMains = foodCategoryRepository.save(new FoodCategory("Main Course", 2));
        FoodCategory catBreads = foodCategoryRepository.save(new FoodCategory("Biriyanis & Rice", 3));
        FoodCategory catDesserts = foodCategoryRepository.save(new FoodCategory("Desserts", 4));
        FoodCategory catBeverages = foodCategoryRepository.save(new FoodCategory("Mocktails & Beverages", 5));
        FoodCategory catSnacks = foodCategoryRepository.save(new FoodCategory("Quick Bites & Snacks", 6));

        // ==========================================
        // 5. SEED FOOD ITEMS WITH AMBIANCE TAGS
        // ==========================================
        // -- EST 1 (The Grand Taj Heritage Hotel) --
        createFood(est1, catMains, "Chicken Dum Biriyani (Royal Handi)", "Slow-cooked fragrant basmati rice with succulent marinated chicken, saffron, and aromatic spices.", 280.0, 240.0, null, false, 40,
                "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?auto=format&fit=crop&w=600&q=80",
                "NON_VEG", "FAMILY", "DINNER", "SPICY", "ROMANTIC");

        createFood(est1, catMains, "Paneer Butter Masala (Rich Cashew Gravy)", "Tender cottage cheese cubes simmered in a luscious butter tomato gravy with fresh cream.", 240.0, 200.0, null, true, 35,
                "https://images.unsplash.com/photo-1631452180519-c014fe946bc7?auto=format&fit=crop&w=600&q=80",
                "VEG", "FAMILY", "DINNER", "ROMANTIC");

        createFood(est1, catMains, "Grilled Herb Chicken Steak", "Tender chicken breast seared to perfection with mushroom pepper sauce and roasted vegetables.", 350.0, 310.0, null, false, 25,
                "https://images.unsplash.com/photo-1544025162-d76694265947?auto=format&fit=crop&w=600&q=80",
                "NON_VEG", "CANDLE_LIGHT", "ROMANTIC", "PREMIUM", "DINNER");

        createFood(est1, catDesserts, "Belgian Molten Chocolate Lava Cake", "Warm molten center oozing premium dark chocolate, served with vanilla bean ice cream.", 180.0, 150.0, null, true, 30,
                "https://images.unsplash.com/photo-1606313564200-e75d5e30476c?auto=format&fit=crop&w=600&q=80",
                "VEG", "DESSERT", "CANDLE_LIGHT", "ROMANTIC", "BIRTHDAY");

        createFood(est1, catBeverages, "Velvet Sunset Mocktail", "Refreshing blend of cranberry, passion fruit, fresh mint, and sparkling tonic water.", 160.0, null, null, true, 50,
                "https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?auto=format&fit=crop&w=600&q=80",
                "VEG", "DRINK", "CANDLE_LIGHT", "ROMANTIC", "PREMIUM");

        // -- EST 2 (Campus Central Canteen) --
        FoodItem canteenItem1 = createFood(est2, catSnacks, "Crispy Chicken Kathi Roll", "Flaky parotta wrapped with spiced chicken chunks, chopped onions, and mint chutney.", 100.0, 90.0, 50.0, false, 20,
                "https://images.unsplash.com/photo-1626777552726-4a6b54c97e46?auto=format&fit=crop&w=600&q=80",
                "NON_VEG", "CASUAL", "LIGHT_MEAL", "SPICY");

        createFood(est2, catSnacks, "Paneer Tikka Grilled Sandwich", "Jumbo multi-grain bread stuffed with marinated paneer, capsicum, and melted mozzarella.", 80.0, 70.0, null, true, 45,
                "https://images.unsplash.com/photo-1528735602780-2552fd46c7af?auto=format&fit=crop&w=600&q=80",
                "VEG", "CASUAL", "LIGHT_MEAL");

        createFood(est2, catBreads, "Student Special Chicken Biriyani", "Quick, piping hot flavorful chicken biriyani with boiled egg and cooling raita.", 140.0, 120.0, null, false, 60,
                "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?auto=format&fit=crop&w=600&q=80",
                "NON_VEG", "CASUAL", "DINNER");

        createFood(est2, catBeverages, "Iced Chocolate Cold Coffee", "Thick chilled cold coffee blended with rich chocolate syrup and ice cream scoop.", 60.0, 50.0, null, true, 70,
                "https://images.unsplash.com/photo-1517701604599-bb29b565090c?auto=format&fit=crop&w=600&q=80",
                "VEG", "DRINK", "CASUAL");

        FoodItem canteenItem2 = createFood(est2, catDesserts, "Fudge Walnut Brownie", "Fudgy chocolate brownie baked with roasted walnuts.", 70.0, 60.0, 35.0, true, 15,
                "https://images.unsplash.com/photo-1606313564200-e75d5e30476c?auto=format&fit=crop&w=600&q=80",
                "VEG", "DESSERT", "CASUAL");

        // -- EST 3 (Royal Spice Multicuisine Restaurant) --
        createFood(est3, catMains, "Mutton Rogan Josh", "Kashmiri style slow braised tender mutton in rich aromatic spiced red gravy.", 380.0, 340.0, null, false, 20,
                "https://images.unsplash.com/photo-1544025162-d76694265947?auto=format&fit=crop&w=600&q=80",
                "NON_VEG", "FAMILY", "DINNER", "SPICY");

        createFood(est3, catStarters, "Tandoori Chicken Platter (Full)", "Whole chicken charred in clay oven with yogurt, roasted cumin, and lemon wedges.", 360.0, 300.0, null, false, 30,
                "https://images.unsplash.com/photo-1599488615731-7e5c2823ff28?auto=format&fit=crop&w=600&q=80",
                "NON_VEG", "FAMILY", "BIRTHDAY", "SPICY");

        createFood(est3, catDesserts, "Royal Gulab Jamun with Rabri", "Hot soft mawa jamuns drowned in cardamom saffron rabri.", 140.0, 120.0, null, true, 40,
                "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?auto=format&fit=crop&w=600&q=80",
                "VEG", "DESSERT", "FAMILY", "BIRTHDAY");

        // -- EST 5 (Oasis Garden Bistro) --
        createFood(est5, catMains, "Truffle Mushroom Fettuccine", "Handmade flat ribbon pasta with creamy wild mushroom ragu, shaved parmesan, and truffle oil.", 320.0, 280.0, null, true, 25,
                "https://images.unsplash.com/photo-1621996346565-e3d5d6281781?auto=format&fit=crop&w=600&q=80",
                "VEG", "OUTDOOR", "ROMANTIC", "CANDLE_LIGHT", "QUIET_DINING");

        createFood(est5, catBeverages, "Blue Lagoon Sparkling Mojito", "Fresh lime, curacao syrup, sparkling soda, and crushed ice.", 150.0, 130.0, null, true, 40,
                "https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?auto=format&fit=crop&w=600&q=80",
                "VEG", "DRINK", "OUTDOOR", "ROMANTIC");

        // ==========================================
        // 6. SEED SMART FOOD CLEARANCE OFFERS (Closing time 50% dynamic offer)
        // ==========================================
        ClearanceOffer co1 = new ClearanceOffer(est2, canteenItem1, 50, 10, "20:30", "22:00");
        clearanceOfferRepository.save(co1);

        ClearanceOffer co2 = new ClearanceOffer(est2, canteenItem2, 50, 8, "20:30", "22:00");
        clearanceOfferRepository.save(co2);

        // ==========================================
        // 7. SEED PROMOTIONAL OFFERS
        // ==========================================
        offerRepository.save(new Offer(est1, "Candle Light Couple Special", "Flat 20% OFF on all gourmet steaks and desserts when booking Candle Light dining", 20, LocalDate.now().plusDays(30)));
        offerRepository.save(new Offer(est2, "Campus Happy Hours", "Enjoy 25% OFF on cold beverages between 4:00 PM and 6:00 PM", 25, LocalDate.now().plusDays(15)));

        // ==========================================
        // 8. SEED COMPLETED ORDERS (To immediately demonstrate turnover)
        // ==========================================
        Order pastOrder1 = new Order("SD-918231", customer1, est1);
        pastOrder1.setStatus(OrderStatus.COMPLETED);
        pastOrder1.setPaymentMethod(PaymentMethod.UPI);
        pastOrder1.setPaymentStatus(PaymentStatus.COMPLETED);
        pastOrder1.setPaymentReference("UPI-TXN-17182931");
        pastOrder1.setCreatedAt(LocalDateTime.now().minusHours(3));
        OrderItem poi1 = new OrderItem(foodItemRepository.findByEstablishment_EstablishmentId(est1.getEstablishmentId()).get(0), 2, "Extra gravy");
        pastOrder1.addItem(poi1);
        pastOrder1.calculateTotals();
        orderRepository.save(pastOrder1);

        Order pastOrder2 = new Order("SD-918232", customer2, est2, 12);
        pastOrder2.setIsFastServe(true);
        pastOrder2.setStatus(OrderStatus.COMPLETED);
        pastOrder2.setPaymentMethod(PaymentMethod.CASH);
        pastOrder2.setPaymentStatus(PaymentStatus.COMPLETED);
        pastOrder2.setPaymentReference("CASH-REC-B9A1");
        pastOrder2.setCreatedAt(LocalDateTime.now().minusHours(1));
        OrderItem poi2 = new OrderItem(canteenItem1, 3, "No onions");
        pastOrder2.addItem(poi2);
        pastOrder2.calculateTotals();
        orderRepository.save(pastOrder2);

        // ==========================================
        // 9. SEED CUSTOMER REVIEWS
        // ==========================================
        reviewRepository.save(new Review(customer1, est1, 5.0, "Outstanding candle light dinner setup! The chicken biriyani and chocolate lava cake were exquisite."));
        reviewRepository.save(new Review(customer2, est2, 4.5, "Fast Serve is amazing! I was sitting at table 12, entered the table number, and had hot rolls in 5 minutes."));
        reviewRepository.save(new Review(customer1, est3, 4.0, "Great family dining ambiance and authentic spiced curries. Highly recommended."));

        System.out.println(">>> SmartDine demo seed data loaded successfully!");
    }

    private FoodItem createFood(Establishment est, FoodCategory cat, String name, String desc,
                                double price, Double offerPrice, Double clearancePrice,
                                boolean isVeg, int stock, String imageUrl, String... tags) {
        FoodItem item = new FoodItem(name, desc, price, isVeg, imageUrl);
        item.setEstablishment(est);
        item.setCategory(cat);
        item.setOfferPrice(offerPrice);
        item.setClearancePrice(clearancePrice);
        item.setStockQuantity(stock);
        item.setTags(new HashSet<>(Arrays.asList(tags)));
        return foodItemRepository.save(item);
    }
}
