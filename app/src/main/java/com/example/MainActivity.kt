package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.OrderEntity
import com.example.data.model.ProductEntity
import com.example.ui.components.BottomNavTab
import com.example.ui.components.NovaBottomBar
import com.example.ui.components.NovaTopBar
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavyDark
import com.example.ui.theme.OffWhite
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.SkyBlueLight
import com.example.ui.viewmodel.NovaMarketViewModel

sealed class Screen {
    object Home : Screen()
    object ProductList : Screen()
    data class ProductDetail(val product: ProductEntity) : Screen()
    object Cart : Screen()
    object Checkout : Screen()
    data class OrderTracking(val order: OrderEntity) : Screen()
    object MyOrders : Screen()
    object Wishlist : Screen()
    object Account : Screen()
    object AdminDashboard : Screen()
    data class Policy(val type: String) : Screen()
    object Auth : Screen()
    object SavedAddresses : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: NovaMarketViewModel = viewModel()
                NovaMarketApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovaMarketApp(viewModel: NovaMarketViewModel) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var currentTab by remember { mutableStateOf(BottomNavTab.HOME) }
    var showPinDialog by remember { mutableStateOf(false) }

    // State collections from ViewModel
    val currentUser by viewModel.currentUser.collectAsState()
    val selectedPinCode by viewModel.selectedPinCode.collectAsState()
    val selectedCityState by viewModel.selectedCityState.collectAsState()
    val categories by viewModel.activeCategories.collectAsState()
    val allProducts by viewModel.activeProducts.collectAsState()
    val featuredProducts by viewModel.featuredProducts.collectAsState()
    val filteredProducts by viewModel.filteredProducts.collectAsState()
    val banners by viewModel.activeBanners.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val wishlistItems by viewModel.wishlistItems.collectAsState()
    val userOrders by viewModel.userOrders.collectAsState()
    val addresses by viewModel.savedAddresses.collectAsState()
    val appliedCoupon by viewModel.appliedCoupon.collectAsState()
    val couponError by viewModel.couponError.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val adminMetrics by viewModel.adminMetrics.collectAsState()
    val allOrdersAdmin by viewModel.allOrdersAdmin.collectAsState()
    val allProductsAdmin by viewModel.allProductsAdmin.collectAsState()
    val allCategoriesAdmin by viewModel.allCategoriesAdmin.collectAsState()
    val activeCoupons by viewModel.activeCoupons.collectAsState()
    val storeSettings by viewModel.storeSettings.collectAsState()
    val primaryPickupAddress by viewModel.primaryPickupAddress.collectAsState()
    val courierConfig by viewModel.courierConfig.collectAsState()
    val ownerNotifications by viewModel.ownerNotifications.collectAsState()
    val unreadNotifCount by viewModel.unreadNotificationCount.collectAsState()
    val serviceablePins by viewModel.serviceablePins.collectAsState()

    val wishlistProductIds = remember(wishlistItems) {
        wishlistItems.map { it.id }.toSet()
    }

    val cartItemCount = remember(cartItems) {
        cartItems.sumOf { it.cartItem.quantity }
    }

    // Hardware/System back handler
    BackHandler(enabled = currentScreen != Screen.Home) {
        if (currentScreen is Screen.ProductDetail || currentScreen is Screen.Checkout || currentScreen is Screen.OrderTracking || currentScreen is Screen.Policy || currentScreen is Screen.SavedAddresses || currentScreen is Screen.Auth || currentScreen is Screen.AdminDashboard) {
            currentScreen = Screen.Home
            currentTab = BottomNavTab.HOME
        } else {
            currentScreen = Screen.Home
            currentTab = BottomNavTab.HOME
        }
    }

    // Location PIN Code Dialog
    if (showPinDialog) {
        var pinInput by remember { mutableStateOf(selectedPinCode) }
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = RoyalBlue)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Choose Delivery Location", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text("Enter a 6-digit Indian PIN code to check delivery availability and speed:", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) pinInput = it },
                        placeholder = { Text("e.g. 110001, 560001") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("pin_code_modal_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Popular Delivery Cities:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        listOf("110001" to "Delhi", "122001" to "Gurugram", "400001" to "Mumbai", "560001" to "Bengaluru").forEach { (pin, name) ->
                            FilterChip(
                                selected = pinInput == pin,
                                onClick = { pinInput = pin },
                                label = { Text("$name ($pin)", fontSize = 10.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinInput.length == 6) {
                            viewModel.checkAndSetPinCode(pinInput)
                            showPinDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text("Apply PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            // Show NovaTopBar on Home and ProductList
            if (currentScreen is Screen.Home || currentScreen is Screen.ProductList) {
                NovaTopBar(
                    selectedPinCode = selectedPinCode,
                    selectedCityState = selectedCityState,
                    cartItemCount = cartItemCount,
                    wishlistItemCount = wishlistItems.size,
                    onSearchClick = {
                        currentScreen = Screen.ProductList
                    },
                    onLocationClick = { showPinDialog = true },
                    onCartClick = {
                        currentScreen = Screen.Cart
                        currentTab = BottomNavTab.CART
                    },
                    onWishlistClick = {
                        currentScreen = Screen.Wishlist
                        currentTab = BottomNavTab.WISHLIST
                    }
                )
            }
        },
        bottomBar = {
            // Persistent Bottom Bar for primary shopping tabs
            if (currentScreen !is Screen.Checkout && currentScreen !is Screen.AdminDashboard) {
                NovaBottomBar(
                    currentTab = currentTab,
                    cartItemCount = cartItemCount,
                    wishlistItemCount = wishlistItems.size,
                    onTabSelected = { tab ->
                        currentTab = tab
                        when (tab) {
                            BottomNavTab.HOME -> currentScreen = Screen.Home
                            BottomNavTab.CATEGORIES -> {
                                viewModel.setCategoryFilter(null)
                                currentScreen = Screen.ProductList
                            }
                            BottomNavTab.WISHLIST -> currentScreen = Screen.Wishlist
                            BottomNavTab.CART -> currentScreen = Screen.Cart
                            BottomNavTab.ACCOUNT -> currentScreen = Screen.Account
                        }
                    }
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val screen = currentScreen) {
                is Screen.Home -> {
                    HomeScreen(
                        banners = banners,
                        categories = categories,
                        featuredProducts = featuredProducts,
                        allProducts = allProducts,
                        wishlistProductIds = wishlistProductIds,
                        onCategoryClick = { catId ->
                            viewModel.setCategoryFilter(catId)
                            currentScreen = Screen.ProductList
                        },
                        onProductClick = { prod ->
                            currentScreen = Screen.ProductDetail(prod)
                        },
                        onWishlistToggle = { prodId ->
                            viewModel.toggleWishlist(prodId)
                        },
                        onAddToCart = { prod ->
                            viewModel.addToCart(prod.id)
                        },
                        onBuyNow = { prod ->
                            viewModel.addToCart(prod.id)
                            currentScreen = Screen.Checkout
                        },
                        onViewAllClick = {
                            viewModel.setCategoryFilter(null)
                            currentScreen = Screen.ProductList
                        },
                        onOpenPolicy = { policyKey ->
                            currentScreen = Screen.Policy(policyKey)
                        }
                    )
                }

                is Screen.ProductList -> {
                    ProductListScreen(
                        products = filteredProducts,
                        categories = categories,
                        filterState = filterState,
                        wishlistProductIds = wishlistProductIds,
                        onCategorySelect = { catId ->
                            viewModel.setCategoryFilter(catId)
                        },
                        onSearchChange = { query ->
                            viewModel.setSearchQuery(query)
                        },
                        onSortChange = { sort ->
                            viewModel.setSortOption(sort)
                        },
                        onFilterChange = { minP, maxP, inStock, rating ->
                            viewModel.setPriceRange(minP, maxP)
                            viewModel.toggleInStockOnly(inStock)
                            viewModel.setMinRating(rating)
                        },
                        onClearFilters = {
                            viewModel.resetFilters()
                        },
                        onProductClick = { prod ->
                            currentScreen = Screen.ProductDetail(prod)
                        },
                        onWishlistToggle = { prodId ->
                            viewModel.toggleWishlist(prodId)
                        },
                        onAddToCart = { prod ->
                            viewModel.addToCart(prod.id)
                        },
                        onBuyNow = { prod ->
                            viewModel.addToCart(prod.id)
                            currentScreen = Screen.Checkout
                        }
                    )
                }

                is Screen.ProductDetail -> {
                    val reviews by viewModel.repository.getReviews(screen.product.id).collectAsState(initial = emptyList())
                    val related = remember(allProducts, screen.product) {
                        allProducts.filter { it.categoryId == screen.product.categoryId && it.id != screen.product.id }
                    }

                    ProductDetailScreen(
                        product = screen.product,
                        reviews = reviews,
                        relatedProducts = related,
                        isInWishlist = wishlistProductIds.contains(screen.product.id),
                        deliveryPinCode = selectedPinCode,
                        onBackClick = {
                            currentScreen = Screen.Home
                        },
                        onWishlistToggle = {
                            viewModel.toggleWishlist(screen.product.id)
                        },
                        onAddToCart = { variant, qty ->
                            viewModel.addToCart(screen.product.id, variant, qty)
                        },
                        onBuyNow = { variant, qty ->
                            viewModel.addToCart(screen.product.id, variant, qty)
                            currentScreen = Screen.Checkout
                        },
                        onPinCodeCheck = { pin ->
                            viewModel.checkAndSetPinCode(pin)
                        },
                        onAddReview = { rating, comment ->
                            viewModel.submitReview(screen.product.id, rating, comment) {}
                        },
                        onRelatedProductClick = { rel ->
                            currentScreen = Screen.ProductDetail(rel)
                        }
                    )
                }

                is Screen.Cart -> {
                    CartScreen(
                        cartItems = cartItems,
                        appliedCoupon = appliedCoupon,
                        couponError = couponError,
                        onQuantityChange = { cartId, qty ->
                            viewModel.updateCartQuantity(cartId, qty)
                        },
                        onRemoveItem = { cartId ->
                            viewModel.removeFromCart(cartId)
                        },
                        onApplyCoupon = { code, subtotal ->
                            viewModel.applyCoupon(code, subtotal)
                        },
                        onRemoveCoupon = {
                            viewModel.removeCoupon()
                        },
                        onProceedToCheckout = {
                            currentScreen = Screen.Checkout
                        },
                        onStartShopping = {
                            currentScreen = Screen.Home
                            currentTab = BottomNavTab.HOME
                        }
                    )
                }

                is Screen.Checkout -> {
                    CheckoutScreen(
                        cartItems = cartItems,
                        addresses = addresses,
                        appliedCoupon = appliedCoupon,
                        onBackClick = {
                            currentScreen = Screen.Cart
                        },
                        onSaveNewAddress = { newAddr ->
                            viewModel.saveAddress(newAddr)
                        },
                        onConfirmOrder = { paymentMethod, address ->
                            viewModel.placeOrder(
                                paymentMethod = paymentMethod,
                                deliveryAddress = address,
                                onSuccess = { placedOrder ->
                                    currentScreen = Screen.OrderTracking(placedOrder)
                                },
                                onError = {
                                    // Error handling
                                }
                            )
                        }
                    )
                }

                is Screen.OrderTracking -> {
                    OrderTrackingScreen(
                        order = screen.order,
                        onBackClick = {
                            currentScreen = Screen.MyOrders
                        },
                        onCancelOrder = { orderId ->
                            viewModel.cancelOrder(orderId, onSuccess = { currentScreen = Screen.MyOrders }, onError = {})
                        },
                        onRequestReturn = { orderId ->
                            viewModel.requestReturn(orderId, onSuccess = { currentScreen = Screen.MyOrders }, onError = {})
                        }
                    )
                }

                is Screen.MyOrders -> {
                    MyOrdersScreen(
                        orders = userOrders,
                        onOrderClick = { ord ->
                            currentScreen = Screen.OrderTracking(ord)
                        },
                        onStartShopping = {
                            currentScreen = Screen.Home
                            currentTab = BottomNavTab.HOME
                        }
                    )
                }

                is Screen.Wishlist -> {
                    WishlistScreen(
                        wishlistProducts = wishlistItems,
                        onProductClick = { prod ->
                            currentScreen = Screen.ProductDetail(prod)
                        },
                        onRemoveFromWishlist = { prodId ->
                            viewModel.toggleWishlist(prodId)
                        },
                        onAddToCart = { prod ->
                            viewModel.addToCart(prod.id)
                            viewModel.toggleWishlist(prod.id)
                        },
                        onStartShopping = {
                            currentScreen = Screen.Home
                            currentTab = BottomNavTab.HOME
                        }
                    )
                }

                is Screen.Account -> {
                    AccountScreen(
                        user = currentUser,
                        orderCount = userOrders.size,
                        wishlistCount = wishlistItems.size,
                        onOrdersClick = {
                            currentScreen = Screen.MyOrders
                        },
                        onWishlistClick = {
                            currentScreen = Screen.Wishlist
                            currentTab = BottomNavTab.WISHLIST
                        },
                        onAddressesClick = {
                            currentScreen = Screen.SavedAddresses
                        },
                        onAdminPanelClick = {
                            currentScreen = Screen.AdminDashboard
                        },
                        onPolicyClick = { key ->
                            currentScreen = Screen.Policy(key)
                        },
                        onLoginClick = {
                            currentScreen = Screen.Auth
                        },
                        onLogoutClick = {
                            viewModel.logout()
                        },
                        onSwitchRole = {
                            if (currentUser?.isOwner == true) {
                                viewModel.switchToCustomerRole()
                            } else {
                                viewModel.switchToOwnerRole()
                            }
                        }
                    )
                }

                is Screen.AdminDashboard -> {
                    AdminDashboardScreen(
                        metrics = adminMetrics,
                        orders = allOrdersAdmin,
                        products = allProductsAdmin,
                        categories = allCategoriesAdmin,
                        coupons = activeCoupons,
                        pickupAddress = primaryPickupAddress,
                        courierConfig = courierConfig,
                        notifications = ownerNotifications,
                        unreadNotifCount = unreadNotifCount,
                        serviceablePins = serviceablePins,
                        storeSettings = storeSettings,
                        onBackClick = {
                            currentScreen = Screen.Account
                        },
                        onMarkPacked = { orderId ->
                            viewModel.ownerMarkOrderAsPacked(orderId, onSuccess = {}, onError = {})
                        },
                        onRequestPickup = { orderId, courier ->
                            viewModel.ownerRequestCourierPickup(orderId, courier, onSuccess = {}, onError = {})
                        },
                        onMarkCourierCollected = { orderId ->
                            viewModel.ownerMarkCourierCollected(orderId, onSuccess = {}, onError = {})
                        },
                        onAdvanceShipmentStatus = { orderId, newStatus ->
                            viewModel.ownerAdvanceShipmentStatus(orderId, newStatus, onSuccess = {}, onError = {})
                        },
                        onSaveProduct = { prod ->
                            viewModel.ownerSaveProduct(prod, onSuccess = {}, onError = {})
                        },
                        onDeleteProduct = { prod ->
                            viewModel.ownerDeleteProduct(prod, onSuccess = {}, onError = {})
                        },
                        onSavePickupAddress = { addr ->
                            viewModel.ownerSavePickupAddress(addr, onSuccess = {}, onError = {})
                        },
                        onSaveCourierConfig = { cfg ->
                            viewModel.ownerSaveCourierConfig(cfg, onSuccess = {}, onError = {})
                        },
                        onSaveCategory = { cat ->
                            viewModel.ownerSaveCategory(cat)
                        },
                        onSaveCoupon = { coup ->
                            viewModel.ownerSaveCoupon(coup)
                        },
                        onSaveSettings = { settings ->
                            viewModel.ownerSaveStoreSettings(settings)
                        },
                        onMarkNotificationRead = { id ->
                            viewModel.markNotificationRead(id)
                        },
                        onClearAllNotifications = {
                            viewModel.clearAllNotifications()
                        }
                    )
                }

                is Screen.Policy -> {
                    PolicyPagesScreen(
                        policyType = screen.type,
                        onBackClick = {
                            currentScreen = Screen.Home
                        }
                    )
                }

                is Screen.Auth -> {
                    AuthScreen(
                        onBackClick = {
                            currentScreen = Screen.Account
                        },
                        onLogin = { email, pass ->
                            viewModel.login(
                                email, pass,
                                onSuccess = { currentScreen = Screen.Account },
                                onError = {}
                            )
                        },
                        onRegister = { name, email, phone, pass ->
                            viewModel.registerCustomer(
                                name, email, phone, pass,
                                onSuccess = { currentScreen = Screen.Account },
                                onError = {}
                            )
                        },
                        authError = null
                    )
                }

                is Screen.SavedAddresses -> {
                    SavedAddressesScreen(
                        addresses = addresses,
                        onBackClick = {
                            currentScreen = Screen.Account
                        },
                        onSaveAddress = { addr ->
                            viewModel.saveAddress(addr)
                        },
                        onDeleteAddress = { addr ->
                            viewModel.deleteAddress(addr)
                        },
                        onSetDefault = { id ->
                            viewModel.setDefaultAddress(id)
                        }
                    )
                }
            }
        }
    }
}
