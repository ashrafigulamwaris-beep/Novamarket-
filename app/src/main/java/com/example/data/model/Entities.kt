package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val fullName: String,
    val phone: String,
    val passwordHash: String,
    val isAdmin: Boolean = false,
    val isOwner: Boolean = false, // Sole store owner authorization
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "addresses")
data class AddressEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val fullName: String,
    val phone: String,
    val houseFlat: String,
    val streetArea: String,
    val landmark: String = "",
    val villageLocality: String = "",
    val district: String = "",
    val city: String,
    val state: String,
    val pinCode: String,
    val addressType: String = "HOME", // HOME or WORK
    val isDefault: Boolean = false
)

@Entity(tableName = "pickup_addresses")
data class PickupAddressEntity(
    @PrimaryKey val id: String = "primary_pickup",
    val facilityName: String = "NovaMarket Central Warehouse & Fulfillment Hub",
    val contactPerson: String = "Store Owner & Dispatch Head",
    val contactPhone: String = "9876543210",
    val contactEmail: String = "dispatch@novamarket.in",
    val addressLine1: String = "Plot 88, Sector 18, Udyog Vihar Phase 4",
    val addressLine2: String = "Near DLF Cyber City Gate 2",
    val landmark: String = "Opposite Metro Pillar 142",
    val city: String = "Gurugram",
    val district: String = "Gurugram",
    val state: String = "Haryana",
    val pinCode: String = "122016",
    val pickupInstructions: String = "Gate 2 Loading Bay, Ring Dispatch Bell. Available 10:00 AM - 05:30 PM",
    val isVerified: Boolean = true
)

@Entity(tableName = "serviceable_pins")
data class ServiceablePinEntity(
    @PrimaryKey val pinCode: String,
    val city: String,
    val district: String,
    val state: String,
    val zone: String, // NORTH, SOUTH, WEST, EAST, CENTRAL, NORTHEAST
    val isCodAvailable: Boolean = true,
    val estimatedDays: Int = 3,
    val isServiceable: Boolean = true
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val iconName: String,
    val displayOrder: Int = 0,
    val isActive: Boolean = true,
    val imageUrl: String = ""
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val sku: String,
    val name: String,
    val description: String,
    val categoryId: String,
    val brand: String,
    val price: Double,
    val originalPrice: Double,
    val stockQuantity: Int,
    val rating: Float = 4.5f,
    val reviewCount: Int = 0,
    val isFeatured: Boolean = false,
    val isActive: Boolean = true,
    val imagesJson: String = "", // comma-separated URLs
    val variantsJson: String = "", // e.g. "Size:S,M,L;Color:Black,Blue"
    val specsJson: String = "", // e.g. "Warranty:1 Year;Origin:Made in India"
    val returnDays: Int = 7,
    val weightKg: Double = 0.5,
    val createdByOwnerId: String = "owner_primary"
)

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val productId: String,
    val selectedVariant: String = "",
    val quantity: Int = 1,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "wishlist_items")
data class WishlistItemEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val productId: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String, // e.g. NOVA-2026-89124
    val userId: String,
    val customerName: String,
    val customerPhone: String,
    val shippingAddressJson: String,
    val itemsSummaryJson: String,
    val subtotal: Double,
    val discount: Double,
    val couponCode: String = "",
    val shippingFee: Double,
    val taxGst: Double,
    val totalAmount: Double,
    val paymentMethod: String, // UPI, COD, CARD, NETBANKING
    val paymentStatus: String, // PAID, PENDING, FAILED
    val orderStatus: String, // PENDING_PAYMENT, ORDER_CONFIRMED, PROCESSING, PACKED, PICKUP_REQUESTED, PICKUP_CONFIRMED, PICKED_UP, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, CANCELLED, DELIVERY_FAILED, RETURN_REQUESTED, RETURNED
    val courierName: String = "Delhivery Express",
    val trackingNumber: String = "",
    val estimatedDeliveryDays: Int = 3,
    val statusHistoryJson: String = "",
    val invoiceNumber: String = "",
    val pickupRequestId: String = "",
    val pickupScheduledSlot: String = "",
    val pickupAddressSnapshot: String = "",
    val courierPickupStatus: String = "NOT_REQUESTED", // NOT_REQUESTED, REQUESTED, CONFIRMED, COLLECTED
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "courier_pickups")
data class CourierPickupRequestEntity(
    @PrimaryKey val id: String, // e.g. PKP-NOVA-9812
    val orderId: String,
    val courierPartner: String,
    val pickupAddressSummary: String,
    val customerAddressSummary: String,
    val packageWeightKg: Double,
    val pickupStatus: String, // REQUESTED, SCHEDULED_CONFIRMED, COLLECTED, CANCELLED, FAILED
    val scheduledDate: String,
    val scheduledTimeSlot: String,
    val awbNumber: String,
    val courierAgentName: String = "",
    val courierAgentPhone: String = "",
    val providerApiMode: String = "TEST_INTEGRATION", // TEST_INTEGRATION, MANUAL_SHIPMENT, PRODUCTION_API
    val requestedAt: Long = System.currentTimeMillis(),
    val confirmedAt: Long? = null,
    val collectedAt: Long? = null
)

@Entity(tableName = "owner_notifications")
data class OwnerNotificationEntity(
    @PrimaryKey val id: String,
    val type: String, // NEW_ORDER, COD_ORDER, PAYMENT_SUCCESS, PACKED, PICKUP_CONFIRMED, PICKED_UP, DELIVERED, CANCELLED, RETURN_REQUESTED
    val title: String,
    val message: String,
    val orderId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "courier_config")
data class CourierConfigEntity(
    @PrimaryKey val id: Int = 1,
    val providerName: String = "Shiprocket / Delhivery Indian Logistics",
    val integrationMode: String = "TEST_SANDBOX", // TEST_SANDBOX, MANUAL_DISPATCH, LIVE_API
    val apiKeyConfigured: Boolean = false,
    val autoAcceptPickups: Boolean = true,
    val defaultPickupTimeWindow: String = "11:00 AM - 02:00 PM",
    val supportTrackingWebhooks: Boolean = true
)

@Entity(tableName = "coupons")
data class CouponEntity(
    @PrimaryKey val code: String,
    val description: String,
    val discountPercent: Int = 0,
    val flatDiscount: Double = 0.0,
    val minOrderValue: Double = 499.0,
    val maxDiscount: Double = 500.0,
    val isActive: Boolean = true
)

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey val id: String,
    val productId: String,
    val userId: String,
    val userName: String,
    val rating: Int,
    val comment: String,
    val isVerified: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "store_settings")
data class StoreSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val storeName: String = "NovaMarket",
    val tagline: String = "Everything You Need, One Destination.",
    val supportEmail: String = "support@novamarket.in",
    val supportPhone: String = "+91 1800 200 6682",
    val businessAddress: String = "Plot 88, Sector 18, Udyog Vihar Phase 4, Gurugram, Haryana 122016, India",
    val freeShippingMin: Double = 499.0,
    val defaultShippingFee: Double = 40.0,
    val gstPercent: Double = 18.0,
    val codAvailable: Boolean = true,
    val upiId: String = "novamarket.merchant@icici"
)

@Entity(tableName = "banners")
data class BannerEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subtitle: String,
    val discountTag: String,
    val categoryTarget: String,
    val drawableResName: String = "",
    val imageUrl: String = "",
    val displayOrder: Int = 0,
    val isActive: Boolean = true
)
