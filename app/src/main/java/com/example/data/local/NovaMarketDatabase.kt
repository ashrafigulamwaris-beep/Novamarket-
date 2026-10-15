package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.*
import com.example.data.model.*

@Database(
    entities = [
        UserEntity::class,
        AddressEntity::class,
        PickupAddressEntity::class,
        ServiceablePinEntity::class,
        CourierPickupRequestEntity::class,
        OwnerNotificationEntity::class,
        CourierConfigEntity::class,
        CategoryEntity::class,
        ProductEntity::class,
        CartItemEntity::class,
        WishlistItemEntity::class,
        OrderEntity::class,
        CouponEntity::class,
        ReviewEntity::class,
        StoreSettingsEntity::class,
        BannerEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class NovaMarketDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun cartDao(): CartDao
    abstract fun wishlistDao(): WishlistDao
    abstract fun orderDao(): OrderDao
    abstract fun userDao(): UserDao
    abstract fun addressDao(): AddressDao
    abstract fun pickupAddressDao(): PickupAddressDao
    abstract fun serviceablePinDao(): ServiceablePinDao
    abstract fun courierPickupDao(): CourierPickupDao
    abstract fun ownerNotificationDao(): OwnerNotificationDao
    abstract fun courierConfigDao(): CourierConfigDao
    abstract fun couponDao(): CouponDao
    abstract fun reviewDao(): ReviewDao
    abstract fun storeSettingsDao(): StoreSettingsDao
    abstract fun bannerDao(): BannerDao

    companion object {
        @Volatile
        private var INSTANCE: NovaMarketDatabase? = null

        fun getDatabase(context: Context): NovaMarketDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NovaMarketDatabase::class.java,
                    "novamarket_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
