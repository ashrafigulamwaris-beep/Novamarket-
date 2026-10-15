package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.InitialDataSeeder
import com.example.data.local.NovaMarketDatabase
import com.example.data.model.*
import com.example.data.repository.CartItemWithProduct
import com.example.data.repository.NovaMarketRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class SortOption(val title: String) {
    POPULARITY("Popularity"),
    PRICE_LOW_HIGH("Price: Low to High"),
    PRICE_HIGH_LOW("Price: High to Low"),
    NEWEST("Newest First")
}

data class FilterState(
    val selectedCategoryId: String? = null,
    val searchQuery: String = "",
    val minPrice: Double = 0.0,
    val maxPrice: Double = 100000.0,
    val inStockOnly: Boolean = false,
    val minRating: Float = 0f,
    val sortOption: SortOption = SortOption.POPULARITY
)

data class AdminMetrics(
    val totalSales: Double = 0.0,
    val totalOrders: Int = 0,
    val pendingOrders: Int = 0,
    val awaitingPacking: Int = 0,
    val pickupsScheduled: Int = 0,
    val packagesCollected: Int = 0,
    val inTransit: Int = 0,
    val deliveredOrders: Int = 0,
    val returnRequests: Int = 0,
    val lowStockCount: Int = 0,
    val totalCustomers: Int = 0
)

class NovaMarketViewModel(application: Application) : AndroidViewModel(application) {

    private val db = NovaMarketDatabase.getDatabase(application)
    val repository = NovaMarketRepository(db)

    // Current User Session
    val currentUser = repository.currentUser
    val selectedPinCode = repository.selectedPinCode
    val selectedCityState = repository.selectedCityState

    // Owner Registered Pickup Address & Courier Configurations
    val primaryPickupAddress = repository.getPrimaryPickupAddress().stateIn(viewModelScope, SharingStarted.Lazily, null)
    val courierConfig = repository.getCourierConfig().stateIn(viewModelScope, SharingStarted.Lazily, null)
    val ownerNotifications = repository.getOwnerNotifications().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val unreadNotificationCount = repository.getUnreadNotificationCount().stateIn(viewModelScope, SharingStarted.Lazily, 0)
    val serviceablePins = repository.getAllServiceablePins().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Store Catalog & Content
    val activeCategories = repository.getActiveCategories().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allCategoriesAdmin = repository.getAllCategories().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val activeProducts = repository.getActiveProducts().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allProductsAdmin = repository.getAllProductsAdmin().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val featuredProducts = repository.getFeaturedProducts().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val activeBanners = repository.getActiveBanners().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val activeCoupons = repository.getActiveCoupons().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val storeSettings = repository.getStoreSettings().stateIn(viewModelScope, SharingStarted.Lazily, null)

    // Search and Filters
    private val _filterState = MutableStateFlow(FilterState())
    val filterState = _filterState.asStateFlow()

    val filteredProducts = combine(activeProducts, _filterState) { products, filter ->
        products.filter { prod ->
            val matchesCategory = filter.selectedCategoryId == null || prod.categoryId == filter.selectedCategoryId
            val matchesQuery = filter.searchQuery.isBlank() ||
                    prod.name.contains(filter.searchQuery, ignoreCase = true) ||
                    prod.brand.contains(filter.searchQuery, ignoreCase = true) ||
                    prod.sku.contains(filter.searchQuery, ignoreCase = true) ||
                    prod.description.contains(filter.searchQuery, ignoreCase = true)
            val matchesPrice = prod.price in filter.minPrice..filter.maxPrice
            val matchesStock = !filter.inStockOnly || prod.stockQuantity > 0
            val matchesRating = prod.rating >= filter.minRating
            matchesCategory && matchesQuery && matchesPrice && matchesStock && matchesRating
        }.let { list ->
            when (filter.sortOption) {
                SortOption.POPULARITY -> list.sortedByDescending { it.rating * (it.reviewCount + 1) }
                SortOption.PRICE_LOW_HIGH -> list.sortedBy { it.price }
                SortOption.PRICE_HIGH_LOW -> list.sortedByDescending { it.price }
                SortOption.NEWEST -> list.sortedByDescending { it.id }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Cart Items
    val cartItems = currentUser.flatMapLatest { user ->
        val userId = user?.id ?: "usr_customer"
        repository.getCartItems(userId)
    }.combine(activeProducts) { items, products ->
        val productMap = products.associateBy { it.id }
        items.mapNotNull { item ->
            val prod = productMap[item.productId]
            if (prod != null) CartItemWithProduct(item, prod) else null
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _appliedCoupon = MutableStateFlow<CouponEntity?>(null)
    val appliedCoupon = _appliedCoupon.asStateFlow()

    private val _couponError = MutableStateFlow<String?>(null)
    val couponError = _couponError.asStateFlow()

    // Wishlist
    val wishlistItems = currentUser.flatMapLatest { user ->
        val userId = user?.id ?: "usr_customer"
        repository.getWishlistItems(userId)
    }.combine(activeProducts) { items, products ->
        val productMap = products.associateBy { it.id }
        items.mapNotNull { productMap[it.productId] }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Saved Addresses
    val savedAddresses = currentUser.flatMapLatest { user ->
        val userId = user?.id ?: "usr_customer"
        repository.getAddresses(userId)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // User Orders
    val userOrders = currentUser.flatMapLatest { user ->
        val userId = user?.id ?: "usr_customer"
        repository.getOrdersForUser(userId)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // All Orders (For Owner)
    val allOrdersAdmin = repository.getAllOrdersAdmin().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Owner Dashboard Metrics
    val adminMetrics = combine(allOrdersAdmin, allProductsAdmin) { orders, products ->
        val eligibleOrders = orders.filter { it.orderStatus != "CANCELLED" }
        val sales = eligibleOrders.sumOf { it.totalAmount }
        val pending = orders.count { it.orderStatus in listOf("ORDER_CONFIRMED", "PROCESSING") }
        val awaitingPacking = orders.count { it.orderStatus in listOf("ORDER_CONFIRMED", "PROCESSING") }
        val pickupsScheduled = orders.count { it.orderStatus == "PICKUP_CONFIRMED" }
        val packagesCollected = orders.count { it.orderStatus == "PICKED_UP" }
        val inTransit = orders.count { it.orderStatus in listOf("IN_TRANSIT", "OUT_FOR_DELIVERY") }
        val delivered = orders.count { it.orderStatus == "DELIVERED" }
        val returnRequests = orders.count { it.orderStatus in listOf("RETURN_REQUESTED", "RETURNED") }
        val lowStock = products.count { it.stockQuantity < 5 }

        AdminMetrics(
            totalSales = sales,
            totalOrders = orders.size,
            pendingOrders = pending,
            awaitingPacking = awaitingPacking,
            pickupsScheduled = pickupsScheduled,
            packagesCollected = packagesCollected,
            inTransit = inTransit,
            deliveredOrders = delivered,
            returnRequests = returnRequests,
            lowStockCount = lowStock,
            totalCustomers = 124
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, AdminMetrics())

    init {
        viewModelScope.launch {
            InitialDataSeeder.seedIfNeeded(db)
        }
    }

    // Filter mutations
    fun setCategoryFilter(categoryId: String?) {
        _filterState.update { it.copy(selectedCategoryId = categoryId) }
    }

    fun setSearchQuery(query: String) {
        _filterState.update { it.copy(searchQuery = query) }
    }

    fun setSortOption(sort: SortOption) {
        _filterState.update { it.copy(sortOption = sort) }
    }

    fun setPriceRange(min: Double, max: Double) {
        _filterState.update { it.copy(minPrice = min, maxPrice = max) }
    }

    fun toggleInStockOnly(inStock: Boolean) {
        _filterState.update { it.copy(inStockOnly = inStock) }
    }

    fun setMinRating(rating: Float) {
        _filterState.update { it.copy(minRating = rating) }
    }

    fun resetFilters() {
        _filterState.value = FilterState()
    }

    // Cart actions
    fun addToCart(productId: String, variant: String = "", qty: Int = 1) {
        viewModelScope.launch {
            val userId = currentUser.value?.id ?: "usr_customer"
            repository.addToCart(userId, productId, variant, qty)
        }
    }

    fun updateCartQuantity(cartItemId: String, qty: Int) {
        viewModelScope.launch {
            repository.updateCartQuantity(cartItemId, qty)
        }
    }

    fun removeFromCart(cartItemId: String) {
        viewModelScope.launch {
            repository.removeFromCart(cartItemId)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            val userId = currentUser.value?.id ?: "usr_customer"
            repository.clearCart(userId)
        }
    }

    fun applyCoupon(code: String, subtotal: Double) {
        viewModelScope.launch {
            _couponError.value = null
            val result = repository.validateCoupon(code, subtotal)
            result.onSuccess { coupon ->
                _appliedCoupon.value = coupon
            }.onFailure { err ->
                _couponError.value = err.message
            }
        }
    }

    fun removeCoupon() {
        _appliedCoupon.value = null
        _couponError.value = null
    }

    fun toggleWishlist(productId: String) {
        viewModelScope.launch {
            val userId = currentUser.value?.id ?: "usr_customer"
            repository.toggleWishlist(userId, productId)
        }
    }

    fun checkAndSetPinCode(pin: String) {
        if (pin.length == 6 && pin.all { it.isDigit() }) {
            viewModelScope.launch {
                repository.updatePinCode(pin)
            }
        }
    }

    suspend fun verifyPinServiceability(pin: String): ServiceablePinEntity? {
        return repository.checkPinServiceable(pin)
    }

    // Order Placement
    fun placeOrder(
        paymentMethod: String,
        deliveryAddress: AddressEntity,
        onSuccess: (OrderEntity) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val user = currentUser.value
            val userId = user?.id ?: "usr_customer"
            val customerName = user?.fullName ?: deliveryAddress.fullName
            val customerPhone = user?.phone ?: deliveryAddress.phone

            val addressString = "${deliveryAddress.houseFlat}, ${deliveryAddress.streetArea}" +
                    (if (deliveryAddress.villageLocality.isNotBlank()) ", Locality: ${deliveryAddress.villageLocality}" else "") +
                    (if (deliveryAddress.landmark.isNotBlank()) ", Near ${deliveryAddress.landmark}" else "") +
                    ", ${deliveryAddress.city}" +
                    (if (deliveryAddress.district.isNotBlank()) ", Dist: ${deliveryAddress.district}" else "") +
                    ", ${deliveryAddress.state} - ${deliveryAddress.pinCode}"

            val items = cartItems.value
            val coupon = _appliedCoupon.value?.code ?: ""

            val res = repository.placeOrder(
                userId = userId,
                customerName = customerName,
                customerPhone = customerPhone,
                shippingAddress = addressString,
                destinationPin = deliveryAddress.pinCode,
                items = items,
                paymentMethod = paymentMethod,
                couponCode = coupon
            )

            res.onSuccess { order ->
                _appliedCoupon.value = null
                onSuccess(order)
            }.onFailure { err ->
                onError(err.message ?: "Could not place order")
            }
        }
    }

    fun cancelOrder(orderId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val res = repository.cancelOrder(orderId)
            res.onSuccess { onSuccess() }.onFailure { onError(it.message ?: "Cancellation failed") }
        }
    }

    fun requestReturn(orderId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val res = repository.requestReturn(orderId)
            res.onSuccess { onSuccess() }.onFailure { onError(it.message ?: "Return request failed") }
        }
    }

    // Addresses
    fun saveAddress(address: AddressEntity) {
        viewModelScope.launch {
            repository.saveAddress(address)
        }
    }

    fun deleteAddress(address: AddressEntity) {
        viewModelScope.launch {
            repository.deleteAddress(address)
        }
    }

    fun setDefaultAddress(addressId: String) {
        viewModelScope.launch {
            val userId = currentUser.value?.id ?: "usr_customer"
            repository.setDefaultAddress(addressId, userId)
        }
    }

    // Customer Authentication (Can NEVER create Owner accounts)
    fun login(email: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val res = repository.login(email, pass)
            res.onSuccess { onSuccess() }.onFailure { onError(it.message ?: "Login failed") }
        }
    }

    fun registerCustomer(name: String, email: String, phone: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val res = repository.registerCustomer(name, email, phone, pass)
            res.onSuccess { onSuccess() }.onFailure { onError(it.message ?: "Registration failed") }
        }
    }

    fun logout() {
        repository.logout()
    }

    fun switchToOwnerRole() {
        repository.switchToOwner()
    }

    fun switchToCustomerRole() {
        repository.switchToCustomer()
    }

    // Reviews
    fun submitReview(productId: String, rating: Int, comment: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val user = currentUser.value
            val userId = user?.id ?: "usr_guest"
            val userName = user?.fullName ?: "Verified Buyer"
            repository.addReview(productId, rating, comment, userId, userName)
            onSuccess()
        }
    }

    // ----------------------------------------------------
    // OWNER-ONLY OPERATIONS (Enforced on backend/repository)
    // ----------------------------------------------------

    fun ownerMarkOrderAsPacked(orderId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val res = repository.markOrderAsPacked(orderId, currentUser.value)
            res.onSuccess { onSuccess() }.onFailure { onError(it.message ?: "Packing update failed") }
        }
    }

    fun ownerRequestCourierPickup(orderId: String, courier: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val res = repository.requestCourierPickup(orderId, courier, currentUser.value)
            res.onSuccess { onSuccess() }.onFailure { onError(it.message ?: "Pickup request failed") }
        }
    }

    fun ownerMarkCourierCollected(orderId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val res = repository.markCourierCollected(orderId, currentUser.value)
            res.onSuccess { onSuccess() }.onFailure { onError(it.message ?: "Collection update failed") }
        }
    }

    fun ownerAdvanceShipmentStatus(orderId: String, newStatus: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val res = repository.advanceShipmentStatus(orderId, newStatus, currentUser.value)
            res.onSuccess { onSuccess() }.onFailure { onError(it.message ?: "Status update failed") }
        }
    }

    fun ownerSaveProduct(product: ProductEntity, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.saveProduct(product, currentUser.value)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to save product")
            }
        }
    }

    fun ownerDeleteProduct(product: ProductEntity, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.deleteProduct(product, currentUser.value)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to delete product")
            }
        }
    }

    fun ownerSavePickupAddress(address: PickupAddressEntity, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.savePickupAddress(address, currentUser.value)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to save pickup address")
            }
        }
    }

    fun ownerSaveCourierConfig(config: CourierConfigEntity, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.saveCourierConfig(config, currentUser.value)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to save courier configuration")
            }
        }
    }

    fun ownerSaveCategory(category: CategoryEntity) {
        viewModelScope.launch {
            try {
                repository.saveCategory(category, currentUser.value)
            } catch (_: Exception) {}
        }
    }

    fun ownerSaveCoupon(coupon: CouponEntity) {
        viewModelScope.launch {
            try {
                repository.saveCoupon(coupon, currentUser.value)
            } catch (_: Exception) {}
        }
    }

    fun ownerSaveStoreSettings(settings: StoreSettingsEntity) {
        viewModelScope.launch {
            try {
                repository.saveStoreSettings(settings, currentUser.value)
            } catch (_: Exception) {}
        }
    }

    fun markNotificationRead(id: String) {
        viewModelScope.launch {
            repository.markNotificationRead(id)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead()
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearAllNotifications()
        }
    }
}
