package com.example.data.local

import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object InitialDataSeeder {

    suspend fun seedIfNeeded(db: NovaMarketDatabase) = withContext(Dispatchers.IO) {
        val categoryDao = db.categoryDao()
        val productDao = db.productDao()
        val couponDao = db.couponDao()
        val bannerDao = db.bannerDao()
        val storeSettingsDao = db.storeSettingsDao()
        val userDao = db.userDao()
        val pickupDao = db.pickupAddressDao()
        val pinDao = db.serviceablePinDao()
        val courierConfigDao = db.courierConfigDao()
        val notificationDao = db.ownerNotificationDao()

        // 1. Seed Owner Account (Sole Store Owner) & Test Customer
        val existingOwner = userDao.getUserByEmail("owner@novamarket.in")
        if (existingOwner == null) {
            userDao.insertUser(
                UserEntity(
                    id = "owner_primary",
                    email = "owner@novamarket.in",
                    fullName = "NovaMarket Store Owner",
                    phone = "9876543210",
                    passwordHash = "owner2026!",
                    isAdmin = true,
                    isOwner = true
                )
            )

            userDao.insertUser(
                UserEntity(
                    id = "usr_customer",
                    email = "customer@example.com",
                    fullName = "Aarav Sharma",
                    phone = "9812345678",
                    passwordHash = "customer123",
                    isAdmin = false,
                    isOwner = false // Customers can NEVER be owners
                )
            )

            // Seed a sample customer address
            db.addressDao().insertAddress(
                AddressEntity(
                    id = "addr_1",
                    userId = "usr_customer",
                    fullName = "Aarav Sharma",
                    phone = "9812345678",
                    houseFlat = "Flat 402, Lotus Residency",
                    streetArea = "Sector 14, MG Road",
                    landmark = "Near Metro Station Gate 2",
                    villageLocality = "DLF Phase 1",
                    district = "Gurugram",
                    city = "Gurugram",
                    state = "Haryana",
                    pinCode = "122001",
                    addressType = "HOME",
                    isDefault = true
                )
            )
        }

        // 2. Seed Registered Seller Pickup Address (For Courier Collection)
        val existingPickup = pickupDao.getPrimaryPickupAddressSync()
        if (existingPickup == null) {
            pickupDao.savePickupAddress(
                PickupAddressEntity(
                    id = "primary_pickup",
                    facilityName = "NovaMarket Central Fulfillment Warehouse",
                    contactPerson = "Mohammad Waris (Store Owner)",
                    contactPhone = "9876543210",
                    contactEmail = "dispatch@novamarket.in",
                    addressLine1 = "Plot 88, Sector 18, Udyog Vihar Phase 4",
                    addressLine2 = "Near DLF Cyber City Gate 2",
                    landmark = "Opposite Metro Pillar 142",
                    city = "Gurugram",
                    district = "Gurugram",
                    state = "Haryana",
                    pinCode = "122016",
                    pickupInstructions = "Loading Dock 2, Security clearance required. Dispatch hours: 10:00 AM - 05:30 PM",
                    isVerified = true
                )
            )
        }

        // 3. Seed Courier Configuration (Sandbox / Shiprocket API ready)
        val existingConfig = courierConfigDao.getCourierConfigSync()
        if (existingConfig == null) {
            courierConfigDao.saveCourierConfig(
                CourierConfigEntity(
                    id = 1,
                    providerName = "Shiprocket / Delhivery Indian Logistics",
                    integrationMode = "TEST_SANDBOX", // Clear indicator for test environment
                    apiKeyConfigured = false, // Set to true when user injects secrets
                    autoAcceptPickups = true,
                    defaultPickupTimeWindow = "11:00 AM - 02:00 PM",
                    supportTrackingWebhooks = true
                )
            )
        }

        // 4. Seed Serviceable PIN Codes across all regions of India
        val indianPins = listOf(
            ServiceablePinEntity("110001", "New Delhi", "Central Delhi", "Delhi", "NORTH", isCodAvailable = true, estimatedDays = 2),
            ServiceablePinEntity("110020", "New Delhi", "South Delhi", "Delhi", "NORTH", isCodAvailable = true, estimatedDays = 2),
            ServiceablePinEntity("122001", "Gurugram", "Gurugram", "Haryana", "NORTH", isCodAvailable = true, estimatedDays = 1),
            ServiceablePinEntity("122016", "Gurugram", "Gurugram", "Haryana", "NORTH", isCodAvailable = true, estimatedDays = 1),
            ServiceablePinEntity("201301", "Noida", "Gautam Buddha Nagar", "Uttar Pradesh", "NORTH", isCodAvailable = true, estimatedDays = 2),
            ServiceablePinEntity("400001", "Mumbai", "Mumbai City", "Maharashtra", "WEST", isCodAvailable = true, estimatedDays = 2),
            ServiceablePinEntity("400050", "Mumbai", "Mumbai Suburban", "Maharashtra", "WEST", isCodAvailable = true, estimatedDays = 2),
            ServiceablePinEntity("560001", "Bengaluru", "Bengaluru Urban", "Karnataka", "SOUTH", isCodAvailable = true, estimatedDays = 3),
            ServiceablePinEntity("560034", "Bengaluru", "Bengaluru Urban", "Karnataka", "SOUTH", isCodAvailable = true, estimatedDays = 3),
            ServiceablePinEntity("500001", "Hyderabad", "Hyderabad", "Telangana", "SOUTH", isCodAvailable = true, estimatedDays = 3),
            ServiceablePinEntity("600001", "Chennai", "Chennai", "Tamil Nadu", "SOUTH", isCodAvailable = true, estimatedDays = 3),
            ServiceablePinEntity("700001", "Kolkata", "Kolkata", "West Bengal", "EAST", isCodAvailable = true, estimatedDays = 3),
            ServiceablePinEntity("302001", "Jaipur", "Jaipur", "Rajasthan", "NORTH", isCodAvailable = true, estimatedDays = 2),
            ServiceablePinEntity("380001", "Ahmedabad", "Ahmedabad", "Gujarat", "WEST", isCodAvailable = true, estimatedDays = 2),
            ServiceablePinEntity("141001", "Ludhiana", "Ludhiana", "Punjab", "NORTH", isCodAvailable = true, estimatedDays = 2),
            ServiceablePinEntity("226001", "Lucknow", "Lucknow", "Uttar Pradesh", "NORTH", isCodAvailable = true, estimatedDays = 2),
            ServiceablePinEntity("682001", "Kochi", "Ernakulam", "Kerala", "SOUTH", isCodAvailable = true, estimatedDays = 4),
            ServiceablePinEntity("781001", "Guwahati", "Kamrup Metropolitan", "Assam", "NORTHEAST", isCodAvailable = true, estimatedDays = 4),
            ServiceablePinEntity("190001", "Srinagar", "Srinagar", "Jammu & Kashmir", "NORTH", isCodAvailable = true, estimatedDays = 4),
            ServiceablePinEntity("800001", "Patna", "Patna", "Bihar", "EAST", isCodAvailable = true, estimatedDays = 3)
        )
        pinDao.insertAll(indianPins)

        // 5. Seed Owner Notifications
        notificationDao.insertNotification(
            OwnerNotificationEntity(
                id = "notif_1",
                type = "NEW_ORDER",
                title = "New Order Received #NOVA-2026-10492",
                message = "Aarav Sharma placed an order for ₹4,599. Ready to pack at your registered warehouse.",
                orderId = "NOVA-2026-10492",
                timestamp = System.currentTimeMillis() - 86400000L,
                isRead = false
            )
        )

        // 6. Seed Store Settings
        storeSettingsDao.saveSettings(
            StoreSettingsEntity(
                id = 1,
                storeName = "NovaMarket",
                tagline = "Everything You Need, One Destination.",
                supportEmail = "support@novamarket.in",
                supportPhone = "+91 1800 200 6682",
                businessAddress = "Plot 88, Sector 18, Udyog Vihar Phase 4, Gurugram, Haryana 122016, India",
                freeShippingMin = 499.0,
                defaultShippingFee = 40.0,
                gstPercent = 18.0,
                codAvailable = true,
                upiId = "novamarket.pay@icici"
            )
        )

        // 7. Seed Coupons
        couponDao.insertAll(
            listOf(
                CouponEntity(
                    code = "WELCOME100",
                    description = "₹100 Off on your first order above ₹499",
                    flatDiscount = 100.0,
                    minOrderValue = 499.0,
                    isActive = true
                ),
                CouponEntity(
                    code = "NOVA10",
                    description = "10% Instant Discount on orders above ₹999",
                    discountPercent = 10,
                    minOrderValue = 999.0,
                    maxDiscount = 500.0,
                    isActive = true
                ),
                CouponEntity(
                    code = "FESTIVE500",
                    description = "Flat ₹500 Off on festive orders above ₹2,999",
                    flatDiscount = 500.0,
                    minOrderValue = 2999.0,
                    isActive = true
                )
            )
        )

        // 8. Seed Banners
        bannerDao.insertAll(
            listOf(
                BannerEntity(
                    id = "ban_1",
                    title = "Great Nova Shopping Festival",
                    subtitle = "Unbeatable deals on Laptops, Smartphones & Audio",
                    discountTag = "UP TO 60% OFF",
                    categoryTarget = "cat_electronics",
                    drawableResName = "banner_electronics",
                    displayOrder = 1
                ),
                BannerEntity(
                    id = "ban_2",
                    title = "Fashion Collection Mega Sale",
                    subtitle = "Ethnic wear, modern casuals & luxury timepieces",
                    discountTag = "UP TO 50% OFF",
                    categoryTarget = "cat_fashion",
                    drawableResName = "banner_fashion",
                    displayOrder = 2
                )
            )
        )

        // 9. Seed Categories
        val categories = listOf(
            CategoryEntity("cat_electronics", "Electronics", "Devices", 1, true, "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500"),
            CategoryEntity("cat_mobiles", "Mobile Accessories", "Smartphone", 2, true, "https://images.unsplash.com/photo-1583394838336-acd977736f90?w=500"),
            CategoryEntity("cat_fashion", "Fashion", "Checkroom", 3, true, "https://images.unsplash.com/photo-1483985988355-763728e1935b?w=500"),
            CategoryEntity("cat_men", "Men's Clothing", "Person", 4, true, "https://images.unsplash.com/photo-1617137984095-74e4e5e3613f?w=500"),
            CategoryEntity("cat_women", "Women's Clothing", "Face", 5, true, "https://images.unsplash.com/photo-1525507119028-ed4c629a60a3?w=500"),
            CategoryEntity("cat_home", "Home & Kitchen", "Home", 6, true, "https://images.unsplash.com/photo-1556911220-e15b29be8c8f?w=500"),
            CategoryEntity("cat_books", "Books", "MenuBook", 7, true, "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500"),
            CategoryEntity("cat_beauty", "Beauty & Care", "Spa", 8, true, "https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?w=500"),
            CategoryEntity("cat_grocery", "Grocery & Gourmet", "LocalGroceryStore", 9, true, "https://images.unsplash.com/photo-1542838132-92c53300491e?w=500"),
            CategoryEntity("cat_sports", "Sports & Fitness", "FitnessCenter", 10, true, "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=500"),
            CategoryEntity("cat_islamic", "Islamic Products", "Star", 11, true, "https://images.unsplash.com/photo-1584281729288-89c02ff4a6cf?w=500"),
            CategoryEntity("cat_new", "New Arrivals", "NewReleases", 12, true, "https://images.unsplash.com/photo-1607082348824-0a96f2a4b9da?w=500")
        )
        categoryDao.insertAll(categories)

        // 10. Seed Products
        val products = listOf(
            ProductEntity(
                id = "prod_1",
                sku = "NM-ELEC-NB13",
                name = "NovaBook Ultra Slim 14\" Laptop (Core i5, 16GB RAM, 512GB SSD)",
                description = "Ultra-portable aluminum unibody laptop with anti-glare FHD IPS display, backlit keyboard, all-day 12-hour battery life, and fast USB-C charging. Ideal for professionals and students in India.",
                categoryId = "cat_electronics",
                brand = "NovaTech",
                price = 44999.0,
                originalPrice = 58999.0,
                stockQuantity = 18,
                rating = 4.8f,
                reviewCount = 142,
                isFeatured = true,
                imagesJson = "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=800,https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800",
                variantsJson = "Color:Space Gray,Silver;Storage:512GB SSD,1TB SSD",
                specsJson = "Processor:12th Gen Intel Core i5;RAM:16GB DDR5;Display:14 Inch Full HD IPS;Warranty:1 Year On-site Manufacturer Warranty",
                returnDays = 7,
                weightKg = 1.4
            ),
            ProductEntity(
                id = "prod_2",
                sku = "NM-AUDIO-EARBUDS",
                name = "NovaBuds Pro Active Noise Cancelling Wireless Earbuds with 40H Playtime",
                description = "Immerse yourself in deep bass and studio sound with 32dB active noise cancellation, quad-mics with ENC for crystal clear Indian calling, IPX5 sweat resistance, and ultra-low latency gaming mode.",
                categoryId = "cat_electronics",
                brand = "NovaAudio",
                price = 2499.0,
                originalPrice = 4999.0,
                stockQuantity = 52,
                rating = 4.6f,
                reviewCount = 388,
                isFeatured = true,
                imagesJson = "https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=800,https://images.unsplash.com/photo-1572536147248-ac59a8abfa4b?w=800",
                variantsJson = "Color:Midnight Black,Pearl White,Navy Blue",
                specsJson = "Driver:11mm Titanium Dynamic;Battery:40 Hours Total;Bluetooth:5.3 with AAC Codec;Warranty:1 Year Replacement Warranty",
                returnDays = 7,
                weightKg = 0.2
            ),
            ProductEntity(
                id = "prod_3",
                sku = "NM-MOB-CHG65W",
                name = "NovaPower 65W GaN Fast Charger (Dual USB-C + USB-A ports)",
                description = "Next-gen Gallium Nitride (GaN) high-speed charging brick that powers laptops, MacBooks, iPhones, Samsung Galaxy, and OnePlus at warp speeds. Compact Indian 2-pin plug design.",
                categoryId = "cat_mobiles",
                brand = "NovaPower",
                price = 1499.0,
                originalPrice = 2499.0,
                stockQuantity = 45,
                rating = 4.7f,
                reviewCount = 94,
                isFeatured = true,
                imagesJson = "https://images.unsplash.com/photo-1583863788434-e58a36330cf0?w=800",
                variantsJson = "Color:White,Matte Black",
                specsJson = "Total Output:65W Max;Protocol:PD 3.0, PPS, QC 4.0;Made in:India;Warranty:6 Months Replacement",
                returnDays = 7,
                weightKg = 0.25
            ),
            ProductEntity(
                id = "prod_4",
                sku = "NM-ETH-KURTA",
                name = "Royal Heritage Pure Khadi Cotton Men's Kurta & Pyjama Set",
                description = "Hand-woven pure cotton ethnic kurta crafted for comfort and festive sophistication. Breathable fabric suitable for Indian summers, festivals, Eid, Diwali, and family occasions.",
                categoryId = "cat_men",
                brand = "Virasat",
                price = 1299.0,
                originalPrice = 2299.0,
                stockQuantity = 30,
                rating = 4.5f,
                reviewCount = 67,
                isFeatured = true,
                imagesJson = "https://images.unsplash.com/photo-1589310243389-96a5483213a8?w=800",
                variantsJson = "Size:S (38),M (40),L (42),XL (44),XXL (46);Color:Navy Blue,Ivory White,Emerald Green",
                specsJson = "Fabric:100% Khadi Cotton;Weave:Handloom;Fit:Regular Comfort;Wash:Machine Washable Cold",
                returnDays = 7,
                weightKg = 0.45
            ),
            ProductEntity(
                id = "prod_5",
                sku = "NM-FASH-SAREE",
                name = "Banarasi Soft Silk Saree with Zari Jacquard Border & Blouse Piece",
                description = "Traditional Banarasi woven art silk saree featuring rich floral golden zari work, ornate pallu, and matching unstitched blouse piece. Perfect for weddings, receptions, and festivals.",
                categoryId = "cat_women",
                brand = "Virasat Sarees",
                price = 2199.0,
                originalPrice = 4599.0,
                stockQuantity = 22,
                rating = 4.9f,
                reviewCount = 112,
                isFeatured = true,
                imagesJson = "https://images.unsplash.com/photo-1610030469983-98e550d6193c?w=800",
                variantsJson = "Color:Royal Red,Teal Blue,Mustard Gold",
                specsJson = "Length:6.3 Meters with Blouse;Fabric:Woven Art Silk;Origin:Varanasi, India;Dry Clean:Recommended",
                returnDays = 7,
                weightKg = 0.6
            ),
            ProductEntity(
                id = "prod_6",
                sku = "NM-KITCH-COOK",
                name = "NovaChef 3-Piece Hard Anodized Non-Stick Induction Cookware Set",
                description = "Durable hard anodized non-stick frying pan, kadai with glass lid, and flat tawa. Heavy gauge aluminum base ensures even heat distribution and zero hot spots. 100% PFOA-free.",
                categoryId = "cat_home",
                brand = "NovaChef",
                price = 2799.0,
                originalPrice = 4299.0,
                stockQuantity = 16,
                rating = 4.6f,
                reviewCount = 83,
                isFeatured = false,
                imagesJson = "https://images.unsplash.com/photo-1584990347449-3998f48325a7?w=800",
                variantsJson = "Color:Granite Black,Burgundy Red",
                specsJson = "Set Contains:24cm Fry Pan, 24cm Kadai + Lid, 28cm Dosa Tawa;Base:Induction & Gas Friendly;Warranty:2 Years",
                returnDays = 7,
                weightKg = 2.1
            ),
            ProductEntity(
                id = "prod_7",
                sku = "NM-ISLAM-RUG",
                name = "Luxury Velvet Orthopedic Padded Janamaz (Islamic Prayer Mat)",
                description = "Extra-thick memory foam padded prayer mat designed to relieve knee and joint pressure during Salah. Soft embossed Turkish velvet finish with anti-skid base.",
                categoryId = "cat_islamic",
                brand = "Noor Islamic",
                price = 1199.0,
                originalPrice = 1899.0,
                stockQuantity = 28,
                rating = 4.9f,
                reviewCount = 156,
                isFeatured = true,
                imagesJson = "https://images.unsplash.com/photo-1584281729288-89c02ff4a6cf?w=800",
                variantsJson = "Color:Emerald Green,Deep Navy,Ruby Red",
                specsJson = "Thickness:20mm Memory Foam;Dimensions:70 x 120 cm;Material:Embossed Velvet;Origin:Turkey/India",
                returnDays = 7,
                weightKg = 0.95
            ),
            ProductEntity(
                id = "prod_8",
                sku = "NM-ISLAM-AJWA",
                name = "Original Madinah Ajwa Al-Aliyah Dates 500g (Vacuum Packed Fresh)",
                description = "Authentic premium Grade-A Ajwa dates directly harvested from the blessed orchards of Madinah Munawwarah. Naturally soft, sweet, rich in minerals, and packed in hygienic ziplock pouch.",
                categoryId = "cat_islamic",
                brand = "Madinah Harvest",
                price = 899.0,
                originalPrice = 1299.0,
                stockQuantity = 60,
                rating = 4.9f,
                reviewCount = 204,
                isFeatured = false,
                imagesJson = "https://images.unsplash.com/photo-1594910488669-e93297a7e6c9?w=800",
                variantsJson = "Pack Size:500g,1kg (Pack of 2)",
                specsJson = "Origin:Madinah, Saudi Arabia;Shelf Life:12 Months;Certification:FSSAI Approved;Net Weight:500g",
                returnDays = 7,
                weightKg = 0.55
            ),
            ProductEntity(
                id = "prod_9",
                sku = "NM-FIT-DUMBBELL",
                name = "Adjustable Quick-Select 20kg Dumbbell Set with Anti-Slip Grip",
                description = "Space-saving home gym dumbbell set with fast gear selector mechanism. Easily adjust weight from 2.5kg to 20kg in seconds with safety lock system.",
                categoryId = "cat_sports",
                brand = "FitNova",
                price = 4999.0,
                originalPrice = 7999.0,
                stockQuantity = 12,
                rating = 4.7f,
                reviewCount = 49,
                isFeatured = false,
                imagesJson = "https://images.unsplash.com/photo-1583454110551-21f2fa2afe61?w=800",
                variantsJson = "Weight:20kg Single,40kg Pair",
                specsJson = "Material:Cast Iron Core with Nylon Coated Plates;Increments:2.5kg to 20kg;Warranty:1 Year",
                returnDays = 7,
                weightKg = 20.0
            ),
            ProductEntity(
                id = "prod_10",
                sku = "NM-BEAUTY-SAFFRON",
                name = "Kumkumadi Tailam Ayurvedic Miracle Night Glow Face Oil (30ml)",
                description = "100% natural cold-pressed facial oil infused with Kashmiri Saffron (Kesar), Sandalwood, and 26 precious Ayurvedic herbs for youthful, glowing, and blemish-free skin.",
                categoryId = "cat_beauty",
                brand = "VedaVitals",
                price = 799.0,
                originalPrice = 1499.0,
                stockQuantity = 40,
                rating = 4.8f,
                reviewCount = 98,
                isFeatured = false,
                imagesJson = "https://images.unsplash.com/photo-1608248597359-597500505187?w=800",
                variantsJson = "Volume:30ml,50ml",
                specsJson = "Skin Type:All Skin Types;100% Natural:Ayush Certified;Free From:Parabens, Mineral Oil, Sulphates",
                returnDays = 7,
                weightKg = 0.15
            )
        )
        productDao.insertAll(products)

        // 11. Seed Sample Order with Courier Pickup Workflow Initialized
        db.orderDao().insertOrder(
            OrderEntity(
                id = "NOVA-2026-10492",
                userId = "usr_customer",
                customerName = "Aarav Sharma",
                customerPhone = "9812345678",
                shippingAddressJson = "Flat 402, Lotus Residency, Sector 14, MG Road, Gurugram, Haryana - 122001",
                itemsSummaryJson = "NovaBuds Pro Active Noise Cancelling (1x ₹2,499), NovaPower 65W GaN Charger (1x ₹1,499)",
                subtotal = 3998.0,
                discount = 100.0,
                couponCode = "WELCOME100",
                shippingFee = 0.0,
                taxGst = 701.64,
                totalAmount = 4599.64,
                paymentMethod = "UPI",
                paymentStatus = "PAID",
                orderStatus = "PICKED_UP",
                courierName = "Delhivery Surface",
                trackingNumber = "DLH-9921048",
                estimatedDeliveryDays = 2,
                statusHistoryJson = "Order placed via UPI on Oct 14; Confirmed by Seller; Packed at Gurugram Warehouse; Courier Pickup Scheduled; Parcel Picked up by Delhivery Agent (Ramesh Kumar - +91 9898981234)",
                invoiceNumber = "INV-NOVA-2026-0089",
                pickupRequestId = "PKP-NM-10492",
                pickupScheduledSlot = "11:00 AM - 02:00 PM",
                pickupAddressSnapshot = "Plot 88, Sector 18, Udyog Vihar Phase 4, Gurugram, HR - 122016",
                courierPickupStatus = "COLLECTED",
                createdAt = System.currentTimeMillis() - 86400000L
            )
        )
    }
}
