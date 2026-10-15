package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY isFeatured DESC, rating DESC")
    fun getAllActiveProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products ORDER BY id DESC")
    fun getAllProductsAdmin(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: String): ProductEntity?

    @Query("SELECT * FROM products WHERE categoryId = :catId AND isActive = 1")
    fun getProductsByCategory(catId: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE isFeatured = 1 AND isActive = 1")
    fun getFeaturedProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE isActive = 1 AND (name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%' OR sku LIKE '%' || :query || '%')")
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("UPDATE products SET stockQuantity = stockQuantity - :quantity WHERE id = :productId AND stockQuantity >= :quantity")
    suspend fun decrementStock(productId: String, quantity: Int): Int

    @Query("UPDATE products SET stockQuantity = stockQuantity + :quantity WHERE id = :productId")
    suspend fun incrementStock(productId: String, quantity: Int)

    @Query("UPDATE products SET stockQuantity = :newStock, price = :newPrice WHERE id = :productId")
    suspend fun updateStockAndPrice(productId: String, newStock: Int, newPrice: Double)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE isActive = 1 ORDER BY displayOrder ASC")
    fun getActiveCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY displayOrder ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)
}

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items WHERE userId = :userId ORDER BY addedAt DESC")
    fun getCartItems(userId: String): Flow<List<CartItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToCart(item: CartItemEntity)

    @Query("UPDATE cart_items SET quantity = :quantity WHERE id = :id")
    suspend fun updateQuantity(id: String, quantity: Int)

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun removeFromCart(id: String)

    @Query("DELETE FROM cart_items WHERE userId = :userId")
    suspend fun clearCart(userId: String)
}

@Dao
interface WishlistDao {
    @Query("SELECT * FROM wishlist_items WHERE userId = :userId ORDER BY addedAt DESC")
    fun getWishlistItems(userId: String): Flow<List<WishlistItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToWishlist(item: WishlistItemEntity)

    @Query("DELETE FROM wishlist_items WHERE userId = :userId AND productId = :productId")
    suspend fun removeFromWishlist(userId: String, productId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM wishlist_items WHERE userId = :userId AND productId = :productId)")
    suspend fun isInWishlist(userId: String, productId: String): Boolean
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders WHERE userId = :userId ORDER BY createdAt DESC")
    fun getOrdersForUser(userId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    suspend fun getOrderById(id: String): OrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Query("UPDATE orders SET orderStatus = :status, trackingNumber = :tracking, courierName = :courier, statusHistoryJson = :history WHERE id = :id")
    suspend fun updateOrderStatusFull(id: String, status: String, tracking: String, courier: String, history: String)

    @Query("UPDATE orders SET orderStatus = :status, statusHistoryJson = :history WHERE id = :id")
    suspend fun updateStatusWithHistory(id: String, status: String, history: String)

    @Query("UPDATE orders SET courierPickupStatus = :pickupStatus, pickupRequestId = :pickupId, pickupScheduledSlot = :slot WHERE id = :id")
    suspend fun updatePickupInfo(id: String, pickupStatus: String, pickupId: String, slot: String)
}

@Dao
interface PickupAddressDao {
    @Query("SELECT * FROM pickup_addresses WHERE id = 'primary_pickup' LIMIT 1")
    fun getPrimaryPickupAddress(): Flow<PickupAddressEntity?>

    @Query("SELECT * FROM pickup_addresses WHERE id = 'primary_pickup' LIMIT 1")
    suspend fun getPrimaryPickupAddressSync(): PickupAddressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePickupAddress(address: PickupAddressEntity)
}

@Dao
interface ServiceablePinDao {
    @Query("SELECT * FROM serviceable_pins WHERE isServiceable = 1")
    fun getAllServiceablePins(): Flow<List<ServiceablePinEntity>>

    @Query("SELECT * FROM serviceable_pins WHERE pinCode = :pin AND isServiceable = 1 LIMIT 1")
    suspend fun checkPinServiceable(pin: String): ServiceablePinEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pins: List<ServiceablePinEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePin(pin: ServiceablePinEntity)
}

@Dao
interface CourierPickupDao {
    @Query("SELECT * FROM courier_pickups ORDER BY requestedAt DESC")
    fun getAllPickups(): Flow<List<CourierPickupRequestEntity>>

    @Query("SELECT * FROM courier_pickups WHERE orderId = :orderId LIMIT 1")
    suspend fun getPickupForOrder(orderId: String): CourierPickupRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePickupRequest(pickup: CourierPickupRequestEntity)

    @Query("UPDATE courier_pickups SET pickupStatus = :status, awbNumber = :awb, confirmedAt = :confirmedAt, courierAgentName = :agentName, courierAgentPhone = :agentPhone WHERE id = :pickupId")
    suspend fun confirmPickup(pickupId: String, status: String, awb: String, confirmedAt: Long, agentName: String, agentPhone: String)
}

@Dao
interface OwnerNotificationDao {
    @Query("SELECT * FROM owner_notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<OwnerNotificationEntity>>

    @Query("SELECT COUNT(*) FROM owner_notifications WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: OwnerNotificationEntity)

    @Query("UPDATE owner_notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("UPDATE owner_notifications SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM owner_notifications")
    suspend fun clearAll()
}

@Dao
interface CourierConfigDao {
    @Query("SELECT * FROM courier_config WHERE id = 1 LIMIT 1")
    fun getCourierConfig(): Flow<CourierConfigEntity?>

    @Query("SELECT * FROM courier_config WHERE id = 1 LIMIT 1")
    suspend fun getCourierConfigSync(): CourierConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCourierConfig(config: CourierConfigEntity)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT COUNT(*) FROM users")
    fun getTotalCustomerCount(): Flow<Int>
}

@Dao
interface AddressDao {
    @Query("SELECT * FROM addresses WHERE userId = :userId ORDER BY isDefault DESC, id DESC")
    fun getAddressesForUser(userId: String): Flow<List<AddressEntity>>

    @Query("SELECT * FROM addresses WHERE id = :id LIMIT 1")
    suspend fun getAddressById(id: String): AddressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddress(address: AddressEntity)

    @Delete
    suspend fun deleteAddress(address: AddressEntity)

    @Query("UPDATE addresses SET isDefault = 0 WHERE userId = :userId")
    suspend fun resetDefaultAddresses(userId: String)

    @Query("UPDATE addresses SET isDefault = 1 WHERE id = :id AND userId = :userId")
    suspend fun setDefaultAddress(id: String, userId: String)
}

@Dao
interface CouponDao {
    @Query("SELECT * FROM coupons WHERE isActive = 1")
    fun getActiveCoupons(): Flow<List<CouponEntity>>

    @Query("SELECT * FROM coupons WHERE code = :code AND isActive = 1 LIMIT 1")
    suspend fun getCouponByCode(code: String): CouponEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoupon(coupon: CouponEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(coupons: List<CouponEntity>)
}

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE productId = :productId ORDER BY createdAt DESC")
    fun getReviewsForProduct(productId: String): Flow<List<ReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reviews: List<ReviewEntity>)
}

@Dao
interface StoreSettingsDao {
    @Query("SELECT * FROM store_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<StoreSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: StoreSettingsEntity)
}

@Dao
interface BannerDao {
    @Query("SELECT * FROM banners WHERE isActive = 1 ORDER BY displayOrder ASC")
    fun getActiveBanners(): Flow<List<BannerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBanner(banner: BannerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(banners: List<BannerEntity>)
}
