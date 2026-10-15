package com.example.data.repository

import com.example.data.local.NovaMarketDatabase
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class CartItemWithProduct(
    val cartItem: CartItemEntity,
    val product: ProductEntity
)

class NovaMarketRepository(private val db: NovaMarketDatabase) {

    // Current Session (Defaults to Customer, Owner can authenticate with owner credentials)
    private val _currentUser = MutableStateFlow<UserEntity?>(
        UserEntity(
            id = "usr_customer",
            email = "customer@example.com",
            fullName = "Aarav Sharma",
            phone = "9812345678",
            passwordHash = "customer123",
            isAdmin = false,
            isOwner = false
        )
    )
    val currentUser = _currentUser.asStateFlow()

    // Delivery Location PIN Code (Indian default PIN: 122001 Gurugram)
    private val _selectedPinCode = MutableStateFlow("122001")
    val selectedPinCode = _selectedPinCode.asStateFlow()

    private val _selectedCityState = MutableStateFlow("Gurugram, Haryana")
    val selectedCityState = _selectedCityState.asStateFlow()

    suspend fun updatePinCode(pin: String): ServiceablePinEntity? {
        _selectedPinCode.value = pin
        val serviceable = db.serviceablePinDao().checkPinServiceable(pin)
        if (serviceable != null) {
            _selectedCityState.value = "${serviceable.city}, ${serviceable.state}"
        } else {
            _selectedCityState.value = when (pin.take(2)) {
                "11" -> "New Delhi, Delhi"
                "12" -> "Gurugram, Haryana"
                "40" -> "Mumbai, Maharashtra"
                "56" -> "Bengaluru, Karnataka"
                "50" -> "Hyderabad, Telangana"
                "60" -> "Chennai, Tamil Nadu"
                "70" -> "Kolkata, West Bengal"
                else -> "India (Standard Delivery Zone)"
            }
        }
        return serviceable
    }

    suspend fun checkPinServiceable(pin: String): ServiceablePinEntity? {
        return db.serviceablePinDao().checkPinServiceable(pin)
    }

    fun getAllServiceablePins(): Flow<List<ServiceablePinEntity>> = db.serviceablePinDao().getAllServiceablePins()

    // ----------------------------------------------------
    // AUTHENTICATION & OWNER AUTHORIZATION
    // ----------------------------------------------------

    suspend fun login(email: String, pass: String): Result<UserEntity> {
        val user = db.userDao().getUserByEmail(email.trim())
        return if (user != null && user.passwordHash == pass) {
            _currentUser.value = user
            Result.success(user)
        } else {
            Result.failure(Exception("Invalid email or password."))
        }
    }

    // Normal customer registration: NEVER assigns admin or owner roles!
    suspend fun registerCustomer(name: String, email: String, phone: String, pass: String): Result<UserEntity> {
        val existing = db.userDao().getUserByEmail(email.trim())
        if (existing != null) {
            return Result.failure(Exception("An account with this email already exists."))
        }
        val newUser = UserEntity(
            id = "usr_" + UUID.randomUUID().toString().take(8),
            email = email.trim(),
            fullName = name.trim(),
            phone = phone.trim(),
            passwordHash = pass,
            isAdmin = false,
            isOwner = false // Enforced: Customers cannot become store owners
        )
        db.userDao().insertUser(newUser)
        _currentUser.value = newUser
        return Result.success(newUser)
    }

    fun logout() {
        _currentUser.value = null
    }

    fun switchToOwner() {
        _currentUser.value = UserEntity(
            id = "owner_primary",
            email = "owner@novamarket.in",
            fullName = "NovaMarket Store Owner",
            phone = "9876543210",
            passwordHash = "owner2026!",
            isAdmin = true,
            isOwner = true
        )
    }

    fun switchToCustomer() {
        _currentUser.value = UserEntity(
            id = "usr_customer",
            email = "customer@example.com",
            fullName = "Aarav Sharma",
            phone = "9812345678",
            passwordHash = "customer123",
            isAdmin = false,
            isOwner = false
        )
    }

    private fun verifyOwnerRole(user: UserEntity?) {
        if (user == null || !user.isOwner) {
            throw SecurityException("Access Denied: Only the verified store owner has permission to perform this action.")
        }
    }

    // ----------------------------------------------------
    // OWNER-ONLY PRODUCT MANAGEMENT
    // ----------------------------------------------------

    fun getActiveProducts(): Flow<List<ProductEntity>> = db.productDao().getAllActiveProducts()
    fun getAllProductsAdmin(): Flow<List<ProductEntity>> = db.productDao().getAllProductsAdmin()
    fun getFeaturedProducts(): Flow<List<ProductEntity>> = db.productDao().getFeaturedProducts()
    fun getProductsByCategory(catId: String): Flow<List<ProductEntity>> = db.productDao().getProductsByCategory(catId)
    fun searchProducts(query: String): Flow<List<ProductEntity>> = db.productDao().searchProducts(query)
    suspend fun getProduct(id: String): ProductEntity? = db.productDao().getProductById(id)

    suspend fun saveProduct(product: ProductEntity, caller: UserEntity?) {
        verifyOwnerRole(caller)
        db.productDao().insertOrUpdateProduct(product.copy(createdByOwnerId = caller!!.id))
    }

    suspend fun deleteProduct(product: ProductEntity, caller: UserEntity?) {
        verifyOwnerRole(caller)
        db.productDao().deleteProduct(product)
    }

    suspend fun updateStockAndPrice(productId: String, stock: Int, price: Double, caller: UserEntity?) {
        verifyOwnerRole(caller)
        db.productDao().updateStockAndPrice(productId, stock, price)
    }

    // ----------------------------------------------------
    // CATEGORY MANAGEMENT
    // ----------------------------------------------------

    fun getActiveCategories(): Flow<List<CategoryEntity>> = db.categoryDao().getActiveCategories()
    fun getAllCategories(): Flow<List<CategoryEntity>> = db.categoryDao().getAllCategories()

    suspend fun saveCategory(category: CategoryEntity, caller: UserEntity?) {
        verifyOwnerRole(caller)
        db.categoryDao().insertCategory(category)
    }

    suspend fun deleteCategory(category: CategoryEntity, caller: UserEntity?) {
        verifyOwnerRole(caller)
        db.categoryDao().deleteCategory(category)
    }

    // ----------------------------------------------------
    // BANNERS
    // ----------------------------------------------------

    fun getActiveBanners(): Flow<List<BannerEntity>> = db.bannerDao().getActiveBanners()
    suspend fun saveBanner(banner: BannerEntity, caller: UserEntity?) {
        verifyOwnerRole(caller)
        db.bannerDao().insertBanner(banner)
    }

    // ----------------------------------------------------
    // CART & WISHLIST
    // ----------------------------------------------------

    fun getCartItems(userId: String): Flow<List<CartItemEntity>> = db.cartDao().getCartItems(userId)

    suspend fun addToCart(userId: String, productId: String, variant: String = "", quantity: Int = 1) {
        val existing = db.cartDao().getCartItems(userId).firstOrNull()?.find {
            it.productId == productId && it.selectedVariant == variant
        }
        if (existing != null) {
            db.cartDao().updateQuantity(existing.id, existing.quantity + quantity)
        } else {
            db.cartDao().addToCart(
                CartItemEntity(
                    id = "cart_" + UUID.randomUUID().toString().take(8),
                    userId = userId,
                    productId = productId,
                    selectedVariant = variant,
                    quantity = quantity
                )
            )
        }
    }

    suspend fun updateCartQuantity(cartItemId: String, qty: Int) {
        if (qty <= 0) {
            db.cartDao().removeFromCart(cartItemId)
        } else {
            db.cartDao().updateQuantity(cartItemId, qty)
        }
    }

    suspend fun removeFromCart(cartItemId: String) = db.cartDao().removeFromCart(cartItemId)
    suspend fun clearCart(userId: String) = db.cartDao().clearCart(userId)

    fun getWishlistItems(userId: String): Flow<List<WishlistItemEntity>> = db.wishlistDao().getWishlistItems(userId)

    suspend fun toggleWishlist(userId: String, productId: String): Boolean {
        val inList = db.wishlistDao().isInWishlist(userId, productId)
        if (inList) {
            db.wishlistDao().removeFromWishlist(userId, productId)
            return false
        } else {
            db.wishlistDao().addToWishlist(
                WishlistItemEntity(
                    id = "wish_" + UUID.randomUUID().toString().take(8),
                    userId = userId,
                    productId = productId
                )
            )
            return true
        }
    }

    // ----------------------------------------------------
    // CUSTOMER ADDRESSES
    // ----------------------------------------------------

    fun getAddresses(userId: String): Flow<List<AddressEntity>> = db.addressDao().getAddressesForUser(userId)

    suspend fun saveAddress(address: AddressEntity) {
        if (address.isDefault) {
            db.addressDao().resetDefaultAddresses(address.userId)
        }
        db.addressDao().insertAddress(address)
    }

    suspend fun deleteAddress(address: AddressEntity) = db.addressDao().deleteAddress(address)

    suspend fun setDefaultAddress(addressId: String, userId: String) {
        db.addressDao().resetDefaultAddresses(userId)
        db.addressDao().setDefaultAddress(addressId, userId)
    }

    // ----------------------------------------------------
    // REGISTERED SELLER PICKUP ADDRESS (OWNER-ONLY)
    // ----------------------------------------------------

    fun getPrimaryPickupAddress(): Flow<PickupAddressEntity?> = db.pickupAddressDao().getPrimaryPickupAddress()

    suspend fun savePickupAddress(address: PickupAddressEntity, caller: UserEntity?) {
        verifyOwnerRole(caller)
        db.pickupAddressDao().savePickupAddress(address)
    }

    // ----------------------------------------------------
    // COURIER CONFIGURATION
    // ----------------------------------------------------

    fun getCourierConfig(): Flow<CourierConfigEntity?> = db.courierConfigDao().getCourierConfig()

    suspend fun saveCourierConfig(config: CourierConfigEntity, caller: UserEntity?) {
        verifyOwnerRole(caller)
        db.courierConfigDao().saveCourierConfig(config)
    }

    // ----------------------------------------------------
    // OWNER NOTIFICATIONS
    // ----------------------------------------------------

    fun getOwnerNotifications(): Flow<List<OwnerNotificationEntity>> = db.ownerNotificationDao().getAllNotifications()
    fun getUnreadNotificationCount(): Flow<Int> = db.ownerNotificationDao().getUnreadCount()

    suspend fun markNotificationRead(id: String) = db.ownerNotificationDao().markAsRead(id)
    suspend fun markAllNotificationsRead() = db.ownerNotificationDao().markAllAsRead()
    suspend fun clearAllNotifications() = db.ownerNotificationDao().clearAll()

    private suspend fun createOwnerNotification(type: String, title: String, message: String, orderId: String) {
        db.ownerNotificationDao().insertNotification(
            OwnerNotificationEntity(
                id = "notif_" + UUID.randomUUID().toString().take(8),
                type = type,
                title = title,
                message = message,
                orderId = orderId,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    // ----------------------------------------------------
    // ORDER LIFECYCLE & TRANSACTION SAFEGUARDS
    // ----------------------------------------------------

    fun getOrdersForUser(userId: String): Flow<List<OrderEntity>> = db.orderDao().getOrdersForUser(userId)
    fun getAllOrdersAdmin(): Flow<List<OrderEntity>> = db.orderDao().getAllOrders()
    suspend fun getOrderById(id: String): OrderEntity? = db.orderDao().getOrderById(id)

    suspend fun placeOrder(
        userId: String,
        customerName: String,
        customerPhone: String,
        shippingAddress: String,
        destinationPin: String,
        items: List<CartItemWithProduct>,
        paymentMethod: String,
        couponCode: String = ""
    ): Result<OrderEntity> {
        if (items.isEmpty()) return Result.failure(Exception("Cart is empty."))

        // 1. All-India PIN Code Serviceability Verification
        val pinServiceable = db.serviceablePinDao().checkPinServiceable(destinationPin)
        if (pinServiceable != null && !pinServiceable.isServiceable) {
            return Result.failure(Exception("Sorry, delivery is currently not serviceable to PIN code $destinationPin."))
        }

        // 2. Validate & Reserve Stock in Room DB
        for (item in items) {
            if (item.product.stockQuantity < item.cartItem.quantity) {
                return Result.failure(Exception("Sorry, '${item.product.name}' only has ${item.product.stockQuantity} units left in stock."))
            }
        }

        for (item in items) {
            val updated = db.productDao().decrementStock(item.product.id, item.cartItem.quantity)
            if (updated <= 0) {
                return Result.failure(Exception("Stock reservation failed for ${item.product.name}."))
            }
        }

        val subtotal = items.sumOf { it.product.price * it.cartItem.quantity }
        var discount = 0.0
        if (couponCode.isNotEmpty()) {
            val coupon = db.couponDao().getCouponByCode(couponCode)
            if (coupon != null && subtotal >= coupon.minOrderValue) {
                discount = if (coupon.discountPercent > 0) {
                    (subtotal * (coupon.discountPercent / 100.0)).coerceAtMost(coupon.maxDiscount)
                } else coupon.flatDiscount
            }
        }

        val shippingFee = if (subtotal >= 499.0) 0.0 else 40.0
        val taxableSubtotal = (subtotal - discount).coerceAtLeast(0.0)
        val taxGst = taxableSubtotal * 0.18
        val total = taxableSubtotal + shippingFee + taxGst

        val orderNum = "NOVA-" + (100000..999999).random()
        val itemsSummary = items.joinToString("; ") { "${it.product.name} (${it.cartItem.quantity}x ₹${it.product.price.toInt()})" }

        val pickupAddress = db.pickupAddressDao().getPrimaryPickupAddressSync()
        val pickupSnapshot = pickupAddress?.let { "${it.facilityName}, ${it.addressLine1}, ${it.city}, ${it.state} - ${it.pinCode}" }
            ?: "Plot 88, Sector 18, Udyog Vihar Phase 4, Gurugram, Haryana - 122016"

        val initialHistory = "Order placed via $paymentMethod on " + SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH).format(Date())

        val newOrder = OrderEntity(
            id = orderNum,
            userId = userId,
            customerName = customerName,
            customerPhone = customerPhone,
            shippingAddressJson = shippingAddress,
            itemsSummaryJson = itemsSummary,
            subtotal = subtotal,
            discount = discount,
            couponCode = couponCode,
            shippingFee = shippingFee,
            taxGst = taxGst,
            totalAmount = total,
            paymentMethod = paymentMethod,
            paymentStatus = if (paymentMethod == "COD") "PENDING" else "PAID",
            orderStatus = "ORDER_CONFIRMED",
            courierName = "Delhivery Surface",
            trackingNumber = "",
            estimatedDeliveryDays = pinServiceable?.estimatedDays ?: 3,
            statusHistoryJson = initialHistory,
            invoiceNumber = "INV-NOVA-2026-" + (1000..9999).random(),
            pickupAddressSnapshot = pickupSnapshot,
            courierPickupStatus = "NOT_REQUESTED",
            createdAt = System.currentTimeMillis()
        )

        db.orderDao().insertOrder(newOrder)
        db.cartDao().clearCart(userId)

        // Generate immediate Notification for the Owner
        createOwnerNotification(
            type = if (paymentMethod == "COD") "COD_ORDER" else "NEW_ORDER",
            title = "New Order #$orderNum (${formatInr(total)})",
            message = "Customer $customerName placed an order ($paymentMethod). Ready for packing at your warehouse.",
            orderId = orderNum
        )

        return Result.success(newOrder)
    }

    // ----------------------------------------------------
    // SELLER PACKING & COURIER PICKUP WORKFLOW
    // ----------------------------------------------------

    suspend fun markOrderAsPacked(orderId: String, caller: UserEntity?): Result<Unit> {
        verifyOwnerRole(caller)
        val order = db.orderDao().getOrderById(orderId) ?: return Result.failure(Exception("Order not found"))
        val history = order.statusHistoryJson + "; Packed by Owner at registered warehouse on " + SimpleDateFormat("dd MMM, hh:mm a", Locale.ENGLISH).format(Date())
        db.orderDao().updateStatusWithHistory(orderId, "PACKED", history)

        createOwnerNotification(
            type = "PACKED",
            title = "Order #$orderId Packed",
            message = "Package is packed and ready for courier pickup scheduling.",
            orderId = orderId
        )
        return Result.success(Unit)
    }

    suspend fun requestCourierPickup(
        orderId: String,
        courierPartner: String,
        caller: UserEntity?
    ): Result<CourierPickupRequestEntity> {
        verifyOwnerRole(caller)
        val order = db.orderDao().getOrderById(orderId) ?: return Result.failure(Exception("Order not found"))
        val pickupAddr = db.pickupAddressDao().getPrimaryPickupAddressSync()
            ?: return Result.failure(Exception("Registered pickup address is missing. Please configure your warehouse pickup address first."))

        val courierConfig = db.courierConfigDao().getCourierConfigSync()
        val isSandbox = courierConfig?.integrationMode == "TEST_SANDBOX"

        val pickupId = "PKP-NM-" + (10000..99999).random()
        val awb = "DLH-" + (10000000..99999999).random()
        val timeSlot = courierConfig?.defaultPickupTimeWindow ?: "11:00 AM - 02:00 PM"
        val pickupDate = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date(System.currentTimeMillis() + 86400000L))

        val pickupRequest = CourierPickupRequestEntity(
            id = pickupId,
            orderId = orderId,
            courierPartner = courierPartner,
            pickupAddressSummary = "${pickupAddr.facilityName}, ${pickupAddr.addressLine1}, ${pickupAddr.city} (${pickupAddr.pinCode})",
            customerAddressSummary = order.shippingAddressJson,
            packageWeightKg = 0.8,
            pickupStatus = "SCHEDULED_CONFIRMED",
            scheduledDate = pickupDate,
            scheduledTimeSlot = timeSlot,
            awbNumber = awb,
            courierAgentName = "Ramesh Kumar (Logistics Agent)",
            courierAgentPhone = "+91 9812981298",
            providerApiMode = courierConfig?.integrationMode ?: "TEST_SANDBOX",
            requestedAt = System.currentTimeMillis(),
            confirmedAt = System.currentTimeMillis()
        )

        db.courierPickupDao().savePickupRequest(pickupRequest)

        val updatedHistory = order.statusHistoryJson + "; Courier pickup scheduled with $courierPartner (AWB: $awb). Pickup Slot: $timeSlot at seller warehouse."
        db.orderDao().updateOrderStatusFull(orderId, "PICKUP_CONFIRMED", awb, courierPartner, updatedHistory)
        db.orderDao().updatePickupInfo(orderId, "CONFIRMED", pickupId, timeSlot)

        createOwnerNotification(
            type = "PICKUP_CONFIRMED",
            title = "Pickup Confirmed #$orderId ($courierPartner)",
            message = "Courier agent will arrive at your warehouse on $pickupDate between $timeSlot. AWB: $awb.",
            orderId = orderId
        )

        return Result.success(pickupRequest)
    }

    suspend fun markCourierCollected(orderId: String, caller: UserEntity?): Result<Unit> {
        verifyOwnerRole(caller)
        val order = db.orderDao().getOrderById(orderId) ?: return Result.failure(Exception("Order not found"))
        val history = order.statusHistoryJson + "; Package collected by courier delivery agent from seller address on " + SimpleDateFormat("dd MMM, hh:mm a", Locale.ENGLISH).format(Date())

        db.orderDao().updateStatusWithHistory(orderId, "PICKED_UP", history)
        db.orderDao().updatePickupInfo(orderId, "COLLECTED", order.pickupRequestId, order.pickupScheduledSlot)

        createOwnerNotification(
            type = "PICKED_UP",
            title = "Package Picked Up #$orderId",
            message = "Courier agent collected package from your warehouse. Parcel is moving to regional hub.",
            orderId = orderId
        )
        return Result.success(Unit)
    }

    suspend fun advanceShipmentStatus(orderId: String, newStatus: String, caller: UserEntity?): Result<Unit> {
        verifyOwnerRole(caller)
        val order = db.orderDao().getOrderById(orderId) ?: return Result.failure(Exception("Order not found"))
        val timestampStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.ENGLISH).format(Date())
        val history = order.statusHistoryJson + "; Status updated to ${newStatus.replace("_", " ")} on $timestampStr"

        db.orderDao().updateStatusWithHistory(orderId, newStatus, history)

        val notifType = when (newStatus) {
            "IN_TRANSIT" -> "IN_TRANSIT"
            "OUT_FOR_DELIVERY" -> "OUT_FOR_DELIVERY"
            "DELIVERED" -> "DELIVERED"
            "CANCELLED" -> "CANCELLED"
            else -> "STATUS_UPDATE"
        }

        createOwnerNotification(
            type = notifType,
            title = "Order #$orderId ${newStatus.replace("_", " ")}",
            message = "Shipment tracking updated to $newStatus for customer ${order.customerName}.",
            orderId = orderId
        )

        return Result.success(Unit)
    }

    suspend fun cancelOrder(orderId: String): Result<Unit> {
        val order = db.orderDao().getOrderById(orderId) ?: return Result.failure(Exception("Order not found"))
        if (order.orderStatus in listOf("SHIPPED", "PICKED_UP", "IN_TRANSIT", "OUT_FOR_DELIVERY", "DELIVERED")) {
            return Result.failure(Exception("Order has already been dispatched with courier and cannot be cancelled directly."))
        }

        val history = order.statusHistoryJson + "; Order cancelled on " + SimpleDateFormat("dd MMM, hh:mm a", Locale.ENGLISH).format(Date())
        db.orderDao().updateStatusWithHistory(orderId, "CANCELLED", history)

        createOwnerNotification(
            type = "ORDER_CANCELLED",
            title = "Order Cancelled #$orderId",
            message = "Customer requested cancellation for Order #$orderId. Restocking inventory.",
            orderId = orderId
        )
        return Result.success(Unit)
    }

    suspend fun requestReturn(orderId: String): Result<Unit> {
        val order = db.orderDao().getOrderById(orderId) ?: return Result.failure(Exception("Order not found"))
        val history = order.statusHistoryJson + "; Return requested under 7-day policy on " + SimpleDateFormat("dd MMM, hh:mm a", Locale.ENGLISH).format(Date())
        db.orderDao().updateStatusWithHistory(orderId, "RETURN_REQUESTED", history)

        createOwnerNotification(
            type = "RETURN_REQUESTED",
            title = "Return Requested #$orderId",
            message = "Customer ${order.customerName} requested a return/replacement for Order #$orderId.",
            orderId = orderId
        )
        return Result.success(Unit)
    }

    // ----------------------------------------------------
    // REVIEWS & COUPONS & STORE SETTINGS
    // ----------------------------------------------------

    fun getReviews(productId: String): Flow<List<ReviewEntity>> = db.reviewDao().getReviewsForProduct(productId)
    suspend fun addReview(productId: String, rating: Int, comment: String, userId: String, userName: String) {
        val review = ReviewEntity(
            id = "rev_" + UUID.randomUUID().toString().take(8),
            productId = productId,
            userId = userId,
            userName = userName,
            rating = rating,
            comment = comment,
            isVerified = true
        )
        db.reviewDao().insertReview(review)
    }

    fun getActiveCoupons(): Flow<List<CouponEntity>> = db.couponDao().getActiveCoupons()
    suspend fun validateCoupon(code: String, subtotal: Double): Result<CouponEntity> {
        val coupon = db.couponDao().getCouponByCode(code.trim().uppercase())
            ?: return Result.failure(Exception("Invalid or expired coupon code."))
        if (subtotal < coupon.minOrderValue) {
            return Result.failure(Exception("Minimum order value of ₹${coupon.minOrderValue.toInt()} required for this coupon."))
        }
        return Result.success(coupon)
    }

    suspend fun saveCoupon(coupon: CouponEntity, caller: UserEntity?) {
        verifyOwnerRole(caller)
        db.couponDao().insertCoupon(coupon)
    }

    fun getStoreSettings(): Flow<StoreSettingsEntity?> = db.storeSettingsDao().getSettings()
    suspend fun saveStoreSettings(settings: StoreSettingsEntity, caller: UserEntity?) {
        verifyOwnerRole(caller)
        db.storeSettingsDao().saveSettings(settings)
    }

    suspend fun getPickupForOrder(orderId: String): CourierPickupRequestEntity? =
        db.courierPickupDao().getPickupForOrder(orderId)
}

fun formatInr(amount: Double): String {
    val formatter = java.text.NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    return formatter.format(amount).replace("INR", "₹").trim()
}
