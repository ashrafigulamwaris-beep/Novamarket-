package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.InvoiceDialog
import com.example.ui.components.ShippingLabelDialog
import com.example.ui.components.formatInr
import com.example.ui.theme.*
import com.example.ui.viewmodel.AdminMetrics

enum class OwnerTab(val title: String) {
    OVERVIEW("Overview"),
    NOTIFICATIONS("Alerts"),
    ORDERS("Orders & Dispatch"),
    PRODUCTS("My Products"),
    PICKUP_ADDRESS("Warehouse Pickup"),
    COURIER_CONFIG("Courier API"),
    CATEGORIES("Categories"),
    COUPONS("Coupons"),
    SETTINGS("Settings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    metrics: AdminMetrics,
    orders: List<OrderEntity>,
    products: List<ProductEntity>,
    categories: List<CategoryEntity>,
    coupons: List<CouponEntity>,
    pickupAddress: PickupAddressEntity?,
    courierConfig: CourierConfigEntity?,
    notifications: List<OwnerNotificationEntity>,
    unreadNotifCount: Int,
    serviceablePins: List<ServiceablePinEntity>,
    storeSettings: StoreSettingsEntity?,
    onBackClick: () -> Unit,
    onMarkPacked: (orderId: String) -> Unit,
    onRequestPickup: (orderId: String, courier: String) -> Unit,
    onMarkCourierCollected: (orderId: String) -> Unit,
    onAdvanceShipmentStatus: (orderId: String, newStatus: String) -> Unit,
    onSaveProduct: (ProductEntity) -> Unit,
    onDeleteProduct: (ProductEntity) -> Unit,
    onSavePickupAddress: (PickupAddressEntity) -> Unit,
    onSaveCourierConfig: (CourierConfigEntity) -> Unit,
    onSaveCategory: (CategoryEntity) -> Unit,
    onSaveCoupon: (CouponEntity) -> Unit,
    onSaveSettings: (StoreSettingsEntity) -> Unit,
    onMarkNotificationRead: (String) -> Unit,
    onClearAllNotifications: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(OwnerTab.OVERVIEW) }

    // Dialog state for editing/adding product
    var showProductDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }

    // Dialog state for Courier Pickup Action
    var showPickupDialog by remember { mutableStateOf(false) }
    var selectedOrderForPickup by remember { mutableStateOf<OrderEntity?>(null) }

    // Shipping Label & Invoice Modals
    var viewingShippingLabelOrder by remember { mutableStateOf<OrderEntity?>(null) }
    var viewingInvoiceOrder by remember { mutableStateOf<OrderEntity?>(null) }

    // Dialog for adding coupon
    var showAddCouponDialog by remember { mutableStateOf(false) }

    if (viewingShippingLabelOrder != null) {
        ShippingLabelDialog(
            order = viewingShippingLabelOrder!!,
            pickupAddress = pickupAddress,
            onDismiss = { viewingShippingLabelOrder = null }
        )
    }

    if (viewingInvoiceOrder != null) {
        InvoiceDialog(
            order = viewingInvoiceOrder!!,
            onDismiss = { viewingInvoiceOrder = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = AmberGold, shape = RoundedCornerShape(4.dp)) {
                            Text("SOLE STORE OWNER", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("NovaMarket Owner Control", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Notification Bell with Badge
                    IconButton(
                        onClick = { currentTab = OwnerTab.NOTIFICATIONS },
                        modifier = Modifier.testTag("owner_notifications_btn")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotifCount > 0) {
                                    Badge(containerColor = CrimsonRed, contentColor = Color.White) {
                                        Text("$unreadNotifCount", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Notifications, contentDescription = "Alerts", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NavyDark,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(OffWhite)
                .testTag("owner_dashboard_screen")
        ) {
            // Scrollable Owner Navigation Tabs
            ScrollableTabRow(
                selectedTabIndex = currentTab.ordinal,
                containerColor = NavyDark,
                contentColor = Color.White,
                edgePadding = 12.dp
            ) {
                OwnerTab.values().forEach { tab ->
                    Tab(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = tab.title,
                                    fontWeight = if (currentTab == tab) FontWeight.Bold else FontWeight.Normal,
                                    color = if (currentTab == tab) AmberGold else Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                                if (tab == OwnerTab.NOTIFICATIONS && unreadNotifCount > 0) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Surface(color = CrimsonRed, shape = RoundedCornerShape(8.dp)) {
                                        Text("$unreadNotifCount", fontSize = 9.sp, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                    }
                                }
                            }
                        }
                    )
                }
            }

            // Tab Content
            when (currentTab) {
                OwnerTab.OVERVIEW -> OwnerOverviewTab(
                    metrics = metrics,
                    recentOrders = orders,
                    pickupAddress = pickupAddress,
                    onGoToOrders = { currentTab = OwnerTab.ORDERS },
                    onGoToPickupAddress = { currentTab = OwnerTab.PICKUP_ADDRESS }
                )

                OwnerTab.NOTIFICATIONS -> OwnerNotificationsTab(
                    notifications = notifications,
                    onMarkRead = onMarkNotificationRead,
                    onClearAll = onClearAllNotifications,
                    onOrderClick = {
                        currentTab = OwnerTab.ORDERS
                    }
                )

                OwnerTab.ORDERS -> OwnerOrdersAndDispatchTab(
                    orders = orders,
                    pickupAddress = pickupAddress,
                    onMarkPacked = onMarkPacked,
                    onRequestPickupClick = { ord ->
                        selectedOrderForPickup = ord
                        showPickupDialog = true
                    },
                    onMarkCourierCollected = onMarkCourierCollected,
                    onAdvanceStatus = onAdvanceShipmentStatus,
                    onPrintShippingLabel = { ord -> viewingShippingLabelOrder = ord },
                    onPrintInvoice = { ord -> viewingInvoiceOrder = ord }
                )

                OwnerTab.PRODUCTS -> OwnerProductsTab(
                    products = products,
                    categories = categories,
                    onAddNewProduct = {
                        editingProduct = null
                        showProductDialog = true
                    },
                    onEditProduct = { prod ->
                        editingProduct = prod
                        showProductDialog = true
                    },
                    onDeleteProduct = onDeleteProduct
                )

                OwnerTab.PICKUP_ADDRESS -> OwnerPickupAddressTab(
                    pickupAddress = pickupAddress,
                    onSavePickupAddress = onSavePickupAddress
                )

                OwnerTab.COURIER_CONFIG -> OwnerCourierConfigTab(
                    courierConfig = courierConfig,
                    serviceablePins = serviceablePins,
                    onSaveCourierConfig = onSaveCourierConfig
                )

                OwnerTab.CATEGORIES -> AdminCategoriesTab(categories = categories, onSaveCategory = onSaveCategory)
                OwnerTab.COUPONS -> AdminCouponsTab(coupons = coupons, onAddCouponClick = { showAddCouponDialog = true })
                OwnerTab.SETTINGS -> AdminSettingsTab(storeSettings = storeSettings, onSaveSettings = onSaveSettings)
            }
        }
    }

    // Schedule Courier Pickup Dialog
    if (showPickupDialog && selectedOrderForPickup != null) {
        val ord = selectedOrderForPickup!!
        var selectedPartner by remember { mutableStateOf("Delhivery Surface") }
        val pickupAddr = pickupAddress

        AlertDialog(
            onDismissRequest = { showPickupDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null, tint = RoyalBlue)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Schedule Courier Pickup", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Step 7 in Workflow: Requesting courier to collect packed order from your registered address.", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Registered Pickup Address Snapshot
                    Surface(color = SkyBlueLight, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("REGISTERED PICKUP LOCATION (YOUR ADDRESS):", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = RoyalBlue)
                            if (pickupAddr != null) {
                                Text(pickupAddr.facilityName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("${pickupAddr.addressLine1}, ${pickupAddr.city} - ${pickupAddr.pinCode}", fontSize = 11.sp)
                                Text("Contact: ${pickupAddr.contactPerson} (+91 ${pickupAddr.contactPhone})", fontSize = 11.sp, color = TextMuted)
                            } else {
                                Text("Plot 88, Sector 18, Udyog Vihar Phase 4, Gurugram, HR - 122016", fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Select India Logistics Partner:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    listOf("Delhivery Surface", "BlueDart Express", "Shiprocket Air", "Ekart Logistics").forEach { partner ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedPartner = partner }
                                .padding(vertical = 3.dp)
                        ) {
                            RadioButton(selected = selectedPartner == partner, onClick = { selectedPartner = partner })
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(partner, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(color = MintLight, shape = RoundedCornerShape(6.dp), modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Estimated Pickup Window: Tomorrow 11:00 AM - 02:00 PM\nCourier agent will arrive with waybill barcode.",
                            fontSize = 11.sp,
                            color = EmeraldGreen,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRequestPickup(ord.id, selectedPartner)
                        showPickupDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text("Confirm Pickup Request")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPickupDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Add / Edit Product Dialog
    if (showProductDialog) {
        AdminProductEditDialog(
            initialProduct = editingProduct,
            categories = categories,
            onDismiss = { showProductDialog = false },
            onSave = { prod ->
                onSaveProduct(prod)
                showProductDialog = false
            }
        )
    }

    // Add Coupon Dialog
    if (showAddCouponDialog) {
        var code by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }
        var discountPercent by remember { mutableStateOf("10") }
        var minOrder by remember { mutableStateOf("499") }

        AlertDialog(
            onDismissRequest = { showAddCouponDialog = false },
            title = { Text("Create Discount Coupon", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column {
                    OutlinedTextField(value = code, onValueChange = { code = it.uppercase() }, label = { Text("Coupon Code (e.g. MEGA20)") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp))
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp))
                    OutlinedTextField(value = discountPercent, onValueChange = { discountPercent = it }, label = { Text("Discount Percentage (%)") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp))
                    OutlinedTextField(value = minOrder, onValueChange = { minOrder = it }, label = { Text("Min Order Value (₹)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (code.isNotBlank()) {
                            onSaveCoupon(
                                CouponEntity(
                                    code = code.trim(),
                                    description = desc.trim(),
                                    discountPercent = discountPercent.toIntOrNull() ?: 10,
                                    minOrderValue = minOrder.toDoubleOrNull() ?: 499.0,
                                    isActive = true
                                )
                            )
                            showAddCouponDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text("Create Coupon")
                }
            },
            dismissButton = { TextButton(onClick = { showAddCouponDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun OwnerOverviewTab(
    metrics: AdminMetrics,
    recentOrders: List<OrderEntity>,
    pickupAddress: PickupAddressEntity?,
    onGoToOrders: () -> Unit,
    onGoToPickupAddress: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize().testTag("owner_overview_tab")
    ) {
        // Warehouse Pickup Status Card
        item {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth().clickable { onGoToPickupAddress() }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(SkyBlueLight, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Warehouse, contentDescription = null, tint = RoyalBlue)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Registered Seller Pickup Address", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(color = MintLight, shape = RoundedCornerShape(4.dp)) {
                                Text("VERIFIED", color = EmeraldGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                            }
                        }
                        Text(
                            text = pickupAddress?.let { "${it.addressLine1}, ${it.city} (${it.pinCode})" } ?: "Configure warehouse pickup location",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
                }
            }
        }

        // Analytics Metrics Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    MetricCard(title = "Total Sales", value = formatInr(metrics.totalSales), icon = Icons.Default.CurrencyRupee, color = RoyalBlue, modifier = Modifier.weight(1f))
                    MetricCard(title = "Total Orders", value = "${metrics.totalOrders}", icon = Icons.Default.ShoppingCart, color = EmeraldGreen, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    MetricCard(title = "Awaiting Packing", value = "${metrics.awaitingPacking}", icon = Icons.Default.Inventory2, color = AmberGold, modifier = Modifier.weight(1f))
                    MetricCard(title = "Pickups Scheduled", value = "${metrics.pickupsScheduled}", icon = Icons.Default.LocalShipping, color = RoyalBlue, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    MetricCard(title = "In Transit", value = "${metrics.inTransit}", icon = Icons.Default.AltRoute, color = NavyMedium, modifier = Modifier.weight(1f))
                    MetricCard(title = "Delivered", value = "${metrics.deliveredOrders}", icon = Icons.Default.CheckCircle, color = EmeraldGreen, modifier = Modifier.weight(1f))
                }
            }
        }

        // Recent Orders Header
        item {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
            ) {
                Text("Dispatch Queue", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NavyDark)
                TextButton(onClick = onGoToOrders) {
                    Text("Manage All (${recentOrders.size})", fontSize = 12.sp, color = RoyalBlue)
                }
            }
        }

        items(recentOrders.take(5)) { order ->
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Order #${order.id}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("${order.customerName} • ${order.paymentMethod}", fontSize = 11.sp, color = TextSecondary)
                        Text(order.itemsSummaryJson, fontSize = 11.sp, maxLines = 1, color = TextMuted)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(formatInr(order.totalAmount), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = RoyalBlue)
                        Surface(
                            color = when (order.orderStatus) {
                                "DELIVERED" -> MintLight
                                "CANCELLED" -> RoseLight
                                "PICKED_UP", "IN_TRANSIT" -> SkyBlueLight
                                else -> AmberLight
                            },
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(order.orderStatus.replace("_", " "), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = RoyalBlue, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OwnerNotificationsTab(
    notifications: List<OwnerNotificationEntity>,
    onMarkRead: (String) -> Unit,
    onClearAll: () -> Unit,
    onOrderClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(12.dp).testTag("owner_notifications_tab")) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            Text("Owner Order Notifications", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NavyDark)
            if (notifications.isNotEmpty()) {
                TextButton(onClick = onClearAll) {
                    Text("Clear All", fontSize = 12.sp, color = CrimsonRed)
                }
            }
        }

        if (notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Outlined.NotificationsNone, contentDescription = null, tint = TextMuted, modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("No Notifications", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NavyDark)
                    Text("Important order, pickup and delivery alerts will appear here.", fontSize = 12.sp, color = TextSecondary)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                items(notifications) { notif ->
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = if (notif.isRead) Color.White else SkyBlueLight.copy(alpha = 0.5f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (notif.isRead) CardBorder else RoyalBlue.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth().clickable {
                            onMarkRead(notif.id)
                            onOrderClick()
                        }
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = when (notif.type) {
                                    "NEW_ORDER", "COD_ORDER" -> Icons.Default.ShoppingBag
                                    "PACKED", "PICKUP_CONFIRMED" -> Icons.Default.LocalShipping
                                    "PICKED_UP", "IN_TRANSIT" -> Icons.Default.AltRoute
                                    "DELIVERED" -> Icons.Default.CheckCircle
                                    "ORDER_CANCELLED" -> Icons.Default.Cancel
                                    else -> Icons.Default.Notifications
                                },
                                contentDescription = null,
                                tint = when (notif.type) {
                                    "DELIVERED" -> EmeraldGreen
                                    "ORDER_CANCELLED" -> CrimsonRed
                                    "PICKUP_CONFIRMED", "PACKED" -> AmberGold
                                    else -> RoyalBlue
                                },
                                modifier = Modifier.size(24.dp).padding(top = 2.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(notif.message, fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(vertical = 2.dp))
                                val timeStr = java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.ENGLISH).format(java.util.Date(notif.timestamp))
                                Text(timeStr, fontSize = 10.sp, color = TextMuted)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OwnerOrdersAndDispatchTab(
    orders: List<OrderEntity>,
    pickupAddress: PickupAddressEntity?,
    onMarkPacked: (String) -> Unit,
    onRequestPickupClick: (OrderEntity) -> Unit,
    onMarkCourierCollected: (String) -> Unit,
    onAdvanceStatus: (String, String) -> Unit,
    onPrintShippingLabel: (OrderEntity) -> Unit,
    onPrintInvoice: (OrderEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf("ALL") }

    val filtered = remember(orders, searchQuery, filterStatus) {
        orders.filter { ord ->
            val matchQuery = searchQuery.isBlank() || ord.id.contains(searchQuery, true) || ord.customerName.contains(searchQuery, true)
            val matchStatus = filterStatus == "ALL" || ord.orderStatus == filterStatus
            matchQuery && matchStatus
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp).testTag("owner_orders_tab")) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by Order ID or Customer Name", fontSize = 12.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Status filter tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("ALL" to "All", "ORDER_CONFIRMED" to "Confirmed", "PACKED" to "Packed", "PICKUP_CONFIRMED" to "Pickup Ready", "PICKED_UP" to "In Transit").forEach { (st, label) ->
                FilterChip(
                    selected = filterStatus == st,
                    onClick = { filterStatus = st },
                    label = { Text(label, fontSize = 10.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
            items(filtered) { order ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Order #${order.id}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(formatInr(order.totalAmount), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = RoyalBlue)
                        }
                        Text("Customer: ${order.customerName} (+91 ${order.customerPhone})", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text("Delivery: ${order.shippingAddressJson}", fontSize = 11.sp, color = TextSecondary)
                        Text("Items: ${order.itemsSummaryJson}", fontSize = 11.sp, color = TextMuted)

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = SkyBlueLight, shape = RoundedCornerShape(4.dp)) {
                                Text("STATUS: ${order.orderStatus.replace("_", " ")}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = RoyalBlue, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                            if (order.trackingNumber.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("AWB: ${order.trackingNumber}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Divider(color = DividerColor, modifier = Modifier.padding(vertical = 10.dp))

                        // SELLER TO COURIER ACTIONS
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            when (order.orderStatus) {
                                "ORDER_CONFIRMED", "PROCESSING" -> {
                                    Button(
                                        onClick = { onMarkPacked(order.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                                        modifier = Modifier.fillMaxWidth().height(40.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Step 5: Mark Packed (Ready for Dispatch)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                "PACKED" -> {
                                    Button(
                                        onClick = { onRequestPickupClick(order) },
                                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color.Black),
                                        modifier = Modifier.fillMaxWidth().height(40.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Step 6: Ready for Pickup (Request Courier)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                "PICKUP_CONFIRMED" -> {
                                    Surface(color = MintLight, shape = RoundedCornerShape(6.dp), modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = "Pickup Scheduled! Courier: ${order.courierName} • Slot: ${order.pickupScheduledSlot}\nAgent will collect parcel from your registered pickup address.",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = EmeraldGreen,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }

                                    Button(
                                        onClick = { onMarkCourierCollected(order.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = NavyDark),
                                        modifier = Modifier.fillMaxWidth().height(40.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Step 10: Courier Collected Package From My Address", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                "PICKED_UP" -> {
                                    Button(
                                        onClick = { onAdvanceStatus(order.id, "IN_TRANSIT") },
                                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                                        modifier = Modifier.fillMaxWidth().height(38.dp)
                                    ) {
                                        Text("Advance to: In Transit", fontSize = 11.sp)
                                    }
                                }

                                "IN_TRANSIT" -> {
                                    Button(
                                        onClick = { onAdvanceStatus(order.id, "OUT_FOR_DELIVERY") },
                                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                                        modifier = Modifier.fillMaxWidth().height(38.dp)
                                    ) {
                                        Text("Advance to: Out for Delivery", fontSize = 11.sp)
                                    }
                                }

                                "OUT_FOR_DELIVERY" -> {
                                    Button(
                                        onClick = { onAdvanceStatus(order.id, "DELIVERED") },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                        modifier = Modifier.fillMaxWidth().height(38.dp)
                                    ) {
                                        Text("Confirm Delivery: Delivered to Customer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Print Shipping Label & Invoice Actions
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { onPrintShippingLabel(order) },
                                    modifier = Modifier.weight(1f).height(36.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Shipping Label", fontSize = 11.sp)
                                }

                                OutlinedButton(
                                    onClick = { onPrintInvoice(order) },
                                    modifier = Modifier.weight(1f).height(36.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Tax Invoice", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OwnerPickupAddressTab(
    pickupAddress: PickupAddressEntity?,
    onSavePickupAddress: (PickupAddressEntity) -> Unit
) {
    var facilityName by remember { mutableStateOf(pickupAddress?.facilityName ?: "NovaMarket Central Warehouse") }
    var contactPerson by remember { mutableStateOf(pickupAddress?.contactPerson ?: "Mohammad Waris (Store Owner)") }
    var contactPhone by remember { mutableStateOf(pickupAddress?.contactPhone ?: "9876543210") }
    var contactEmail by remember { mutableStateOf(pickupAddress?.contactEmail ?: "dispatch@novamarket.in") }
    var addressLine1 by remember { mutableStateOf(pickupAddress?.addressLine1 ?: "Plot 88, Sector 18, Udyog Vihar Phase 4") }
    var addressLine2 by remember { mutableStateOf(pickupAddress?.addressLine2 ?: "Near DLF Cyber City Gate 2") }
    var landmark by remember { mutableStateOf(pickupAddress?.landmark ?: "Opposite Metro Pillar 142") }
    var city by remember { mutableStateOf(pickupAddress?.city ?: "Gurugram") }
    var district by remember { mutableStateOf(pickupAddress?.district ?: "Gurugram") }
    var state by remember { mutableStateOf(pickupAddress?.state ?: "Haryana") }
    var pinCode by remember { mutableStateOf(pickupAddress?.pinCode ?: "122016") }
    var pickupInstructions by remember { mutableStateOf(pickupAddress?.pickupInstructions ?: "Loading Dock 2. Available 10:00 AM - 5:30 PM") }
    var saveSuccessNotice by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .verticalScroll(rememberScrollState())
            .testTag("owner_pickup_address_tab")
    ) {
        Surface(color = SkyBlueLight, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Warehouse, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Seller Registered Pickup Facility", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
                    Text("Couriers (Delhivery, BlueDart, Shiprocket) will collect customer packages strictly from this address.", fontSize = 11.sp, color = TextSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(value = facilityName, onValueChange = { facilityName = it }, label = { Text("Warehouse / Facility Name *") }, modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedTextField(value = contactPerson, onValueChange = { contactPerson = it }, label = { Text("Contact Person *") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = contactPhone, onValueChange = { contactPhone = it }, label = { Text("Mobile Number *") }, prefix = { Text("+91 ") }, modifier = Modifier.weight(1f))
        }
        OutlinedTextField(value = contactEmail, onValueChange = { contactEmail = it }, label = { Text("Dispatch Contact Email") }, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp))
        OutlinedTextField(value = addressLine1, onValueChange = { addressLine1 = it }, label = { Text("Building / Plot / Street Address *") }, modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp))
        OutlinedTextField(value = addressLine2, onValueChange = { addressLine2 = it }, label = { Text("Area / Sector *") }, modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp))
        OutlinedTextField(value = landmark, onValueChange = { landmark = it }, label = { Text("Landmark") }, modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City *") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = district, onValueChange = { district = it }, label = { Text("District") }, modifier = Modifier.weight(1f))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 6.dp)) {
            OutlinedTextField(value = state, onValueChange = { state = it }, label = { Text("State *") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = pinCode, onValueChange = { pinCode = it }, label = { Text("PIN Code *") }, modifier = Modifier.weight(1f))
        }

        OutlinedTextField(
            value = pickupInstructions,
            onValueChange = { pickupInstructions = it },
            label = { Text("Pickup Instructions for Courier Delivery Boy") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        )

        Button(
            onClick = {
                val updated = PickupAddressEntity(
                    id = "primary_pickup",
                    facilityName = facilityName,
                    contactPerson = contactPerson,
                    contactPhone = contactPhone,
                    contactEmail = contactEmail,
                    addressLine1 = addressLine1,
                    addressLine2 = addressLine2,
                    landmark = landmark,
                    city = city,
                    district = district,
                    state = state,
                    pinCode = pinCode,
                    pickupInstructions = pickupInstructions,
                    isVerified = true
                )
                onSavePickupAddress(updated)
                saveSuccessNotice = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Save Registered Pickup Address", fontWeight = FontWeight.Bold)
        }

        if (saveSuccessNotice) {
            Text("Pickup address saved! Couriers will route pickups here.", color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
fun OwnerCourierConfigTab(
    courierConfig: CourierConfigEntity?,
    serviceablePins: List<ServiceablePinEntity>,
    onSaveCourierConfig: (CourierConfigEntity) -> Unit
) {
    var integrationMode by remember { mutableStateOf(courierConfig?.integrationMode ?: "TEST_SANDBOX") }
    var defaultWindow by remember { mutableStateOf(courierConfig?.defaultPickupTimeWindow ?: "11:00 AM - 02:00 PM") }
    var autoAccept by remember { mutableStateOf(courierConfig?.autoAcceptPickups ?: true) }
    var savedNotice by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .verticalScroll(rememberScrollState())
            .testTag("owner_courier_config_tab")
    ) {
        Text("Courier Aggregator & Serviceability (India)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyDark)
        Spacer(modifier = Modifier.height(10.dp))

        // Integration Mode Card
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("INTEGRATION ENVIRONMENT:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextSecondary)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    RadioButton(selected = integrationMode == "TEST_SANDBOX", onClick = { integrationMode = "TEST_SANDBOX" })
                    Text("Test Mode / Sandbox (Shiprocket Simulator)", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = integrationMode == "MANUAL_DISPATCH", onClick = { integrationMode = "MANUAL_DISPATCH" })
                    Text("Manual Dispatch (Call Courier Agent Manually)", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = integrationMode == "LIVE_API", onClick = { integrationMode = "LIVE_API" })
                    Text("Production Live API (Requires Shiprocket Credentials)", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // API Setup explanation
        Surface(color = OffWhite, shape = RoundedCornerShape(8.dp), border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Connecting Live Shiprocket / Delhivery Account:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyDark)
                Text(
                    text = "1. Register merchant account at shiprocket.in\n2. Add your SHIPROCKET_EMAIL and SHIPROCKET_PASSWORD in AI Studio Secrets panel.\n3. Automatic courier assignment, barcode generation, and live GPS tracking will be activated.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = defaultWindow,
            onValueChange = { defaultWindow = it },
            label = { Text("Default Daily Pickup Window") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Auto-accept pickup requests in sandbox", fontSize = 13.sp)
            Switch(checked = autoAccept, onCheckedChange = { autoAccept = it })
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val updated = CourierConfigEntity(
                    id = 1,
                    integrationMode = integrationMode,
                    defaultPickupTimeWindow = defaultWindow,
                    autoAcceptPickups = autoAccept
                )
                onSaveCourierConfig(updated)
                savedNotice = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Save Logistics Settings", fontWeight = FontWeight.Bold)
        }

        if (savedNotice) {
            Text("Logistics settings updated successfully!", color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Serviceable PINs summary
        Text("Configured Serviceable Indian PIN Codes (${serviceablePins.size}):", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
        Spacer(modifier = Modifier.height(6.dp))
        serviceablePins.take(10).forEach { pin ->
            Text("• ${pin.pinCode} - ${pin.city}, ${pin.state} (${pin.estimatedDays} days est., COD: ${if (pin.isCodAvailable) "Yes" else "No"})", fontSize = 11.sp, color = TextSecondary)
        }
    }
}

@Composable
fun OwnerProductsTab(
    products: List<ProductEntity>,
    categories: List<CategoryEntity>,
    onAddNewProduct: () -> Unit,
    onEditProduct: (ProductEntity) -> Unit,
    onDeleteProduct: (ProductEntity) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(12.dp).testTag("owner_products_tab")) {
        // Prominent Owner-Only Add Product CTA
        Button(
            onClick = onAddNewProduct,
            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("owner_add_product_btn")
        ) {
            Icon(imageVector = Icons.Default.AddBox, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Add New Product (Sole Store Owner)", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
            items(products) { prod ->
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                            Text("SKU: ${prod.sku} • Stock: ${prod.stockQuantity} units", fontSize = 11.sp, color = if (prod.stockQuantity < 5) CrimsonRed else TextSecondary)
                            Text("Price: ${formatInr(prod.price)} (MRP: ${formatInr(prod.originalPrice)})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = RoyalBlue)
                        }

                        IconButton(onClick = { onEditProduct(prod) }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = RoyalBlue)
                        }
                        IconButton(onClick = { onDeleteProduct(prod) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = CrimsonRed)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = title, fontSize = 11.sp, color = TextSecondary)
                Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyDark)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductEditDialog(
    initialProduct: ProductEntity?,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (ProductEntity) -> Unit
) {
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var brand by remember { mutableStateOf(initialProduct?.brand ?: "") }
    var sku by remember { mutableStateOf(initialProduct?.sku ?: ("NM-" + (1000..9999).random())) }
    var description by remember { mutableStateOf(initialProduct?.description ?: "") }
    var categoryId by remember { mutableStateOf(initialProduct?.categoryId ?: (categories.firstOrNull()?.id ?: "electronics")) }
    var priceText by remember { mutableStateOf(initialProduct?.price?.toInt()?.toString() ?: "999") }
    var mrpText by remember { mutableStateOf(initialProduct?.originalPrice?.toInt()?.toString() ?: "1499") }
    var stockText by remember { mutableStateOf(initialProduct?.stockQuantity?.toString() ?: "20") }
    var weightText by remember { mutableStateOf(initialProduct?.weightKg?.toString() ?: "0.5") }
    var returnDaysText by remember { mutableStateOf(initialProduct?.returnDays?.toString() ?: "7") }
    var imagesText by remember { mutableStateOf(initialProduct?.imagesJson ?: "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&q=80") }
    var variantsText by remember { mutableStateOf(initialProduct?.variantsJson ?: "Color:Black,Blue,White") }
    var specsText by remember { mutableStateOf(initialProduct?.specsJson ?: "Warranty:1 Year Brand Warranty;Origin:India") }
    var isFeatured by remember { mutableStateOf(initialProduct?.isFeatured ?: false) }
    var isActive by remember { mutableStateOf(initialProduct?.isActive ?: true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (initialProduct == null) "Add New Product (Sole Store Owner)" else "Edit Product #${initialProduct.sku}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Title *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = { Text("Brand *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = sku,
                        onValueChange = { sku = it },
                        label = { Text("SKU *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).padding(bottom = 6.dp)
                    )
                }

                // Category selector
                Text("Category:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp, bottom = 4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.take(4).forEach { cat ->
                        FilterChip(
                            selected = categoryId == cat.id,
                            onClick = { categoryId = cat.id },
                            label = { Text(cat.name, fontSize = 11.sp) }
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 6.dp)) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { if (it.all { c -> c.isDigit() }) priceText = it },
                        label = { Text("Selling Price (₹) *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = mrpText,
                        onValueChange = { if (it.all { c -> c.isDigit() }) mrpText = it },
                        label = { Text("MRP (₹) *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).padding(bottom = 6.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = stockText,
                        onValueChange = { if (it.all { c -> c.isDigit() }) stockText = it },
                        label = { Text("Stock Qty *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        label = { Text("Weight (Kg)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).padding(bottom = 6.dp)
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Detailed Description") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                )

                OutlinedTextField(
                    value = imagesText,
                    onValueChange = { imagesText = it },
                    label = { Text("Image URLs (comma-separated)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                )

                OutlinedTextField(
                    value = variantsText,
                    onValueChange = { variantsText = it },
                    label = { Text("Variants (e.g. Size:S,M,L;Color:Black,Blue)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                )

                OutlinedTextField(
                    value = specsText,
                    onValueChange = { specsText = it },
                    label = { Text("Specs (e.g. Warranty:1 Year;Origin:India)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Featured on Homepage", fontSize = 12.sp)
                    Switch(checked = isFeatured, onCheckedChange = { isFeatured = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Active / In Stock for Customers", fontSize = 12.sp)
                    Switch(checked = isActive, onCheckedChange = { isActive = it })
                }

                if (errorMessage != null) {
                    Text(errorMessage!!, color = CrimsonRed, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || brand.isBlank() || priceText.isBlank()) {
                        errorMessage = "Please enter Product Title, Brand, and Price."
                        return@Button
                    }
                    val price = priceText.toDoubleOrNull() ?: 0.0
                    val mrp = (mrpText.toDoubleOrNull() ?: price).coerceAtLeast(price)
                    val stock = stockText.toIntOrNull() ?: 10
                    val weight = weightText.toDoubleOrNull() ?: 0.5
                    val retDays = returnDaysText.toIntOrNull() ?: 7

                    val product = ProductEntity(
                        id = initialProduct?.id ?: ("prod_" + java.util.UUID.randomUUID().toString().take(8)),
                        sku = sku.ifBlank { "NM-" + (1000..9999).random() },
                        name = name.trim(),
                        description = description.trim().ifBlank { "High quality authentic product sold directly by NovaMarket." },
                        categoryId = categoryId,
                        brand = brand.trim(),
                        price = price,
                        originalPrice = mrp,
                        stockQuantity = stock,
                        rating = initialProduct?.rating ?: 4.5f,
                        reviewCount = initialProduct?.reviewCount ?: 0,
                        isFeatured = isFeatured,
                        isActive = isActive,
                        imagesJson = imagesText.trim(),
                        variantsJson = variantsText.trim(),
                        specsJson = specsText.trim(),
                        returnDays = retDays,
                        weightKg = weight,
                        createdByOwnerId = "owner_primary"
                    )
                    onSave(product)
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
            ) {
                Text("Save Product")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AdminCategoriesTab(
    categories: List<CategoryEntity>,
    onSaveCategory: (CategoryEntity) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Button(
            onClick = { showAddDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Add New Category", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
            items(categories) { cat ->
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(cat.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyDark)
                            Text("ID: ${cat.id} • Icon: ${cat.iconName}", fontSize = 11.sp, color = TextSecondary)
                        }
                        Switch(
                            checked = cat.isActive,
                            onCheckedChange = { active ->
                                onSaveCategory(cat.copy(isActive = active))
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var catName by remember { mutableStateOf("") }
        var iconName by remember { mutableStateOf("Category") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Create Category", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column {
                    OutlinedTextField(
                        value = catName,
                        onValueChange = { catName = it },
                        label = { Text("Category Name (e.g. Home & Kitchen)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = iconName,
                        onValueChange = { iconName = it },
                        label = { Text("Icon Identifier") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (catName.isNotBlank()) {
                            val id = catName.lowercase().replace(" ", "_").replace("&", "and")
                            onSaveCategory(CategoryEntity(id = id, name = catName.trim(), iconName = iconName.trim(), displayOrder = categories.size + 1, isActive = true))
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun AdminCouponsTab(
    coupons: List<CouponEntity>,
    onAddCouponClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Button(
            onClick = onAddCouponClick,
            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Create Discount Coupon", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
            items(coupons) { coup ->
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Surface(color = RoyalBlue.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                                Text(coup.code, fontWeight = FontWeight.Bold, color = RoyalBlue, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                            Text(coup.description, fontSize = 11.sp, color = TextSecondary, modifier = Modifier.padding(top = 4.dp))
                            Text(
                                if (coup.discountPercent > 0) "${coup.discountPercent}% OFF (Max ₹${coup.maxDiscount.toInt()}) on min order ₹${coup.minOrderValue.toInt()}"
                                else "Flat ₹${coup.flatDiscount.toInt()} OFF on min order ₹${coup.minOrderValue.toInt()}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = EmeraldGreen
                            )
                        }
                        Surface(
                            color = if (coup.isActive) EmeraldGreen.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                if (coup.isActive) "ACTIVE" else "INACTIVE",
                                color = if (coup.isActive) EmeraldGreen else Color.Gray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSettingsTab(
    storeSettings: StoreSettingsEntity?,
    onSaveSettings: (StoreSettingsEntity) -> Unit
) {
    val current = storeSettings ?: StoreSettingsEntity()
    var storeName by remember { mutableStateOf(current.storeName) }
    var tagline by remember { mutableStateOf(current.tagline) }
    var supportEmail by remember { mutableStateOf(current.supportEmail) }
    var supportPhone by remember { mutableStateOf(current.supportPhone) }
    var businessAddress by remember { mutableStateOf(current.businessAddress) }
    var freeShippingMin by remember { mutableStateOf(current.freeShippingMin.toInt().toString()) }
    var defaultShippingFee by remember { mutableStateOf(current.defaultShippingFee.toInt().toString()) }
    var upiId by remember { mutableStateOf(current.upiId) }
    var codAvailable by remember { mutableStateOf(current.codAvailable) }
    var savedNotice by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
    ) {
        Text("Store Identity & Customer Support", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyDark)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(value = storeName, onValueChange = { storeName = it }, label = { Text("Store Name") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp))
        OutlinedTextField(value = tagline, onValueChange = { tagline = it }, label = { Text("Tagline") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp))
        OutlinedTextField(value = supportEmail, onValueChange = { supportEmail = it }, label = { Text("Customer Support Email") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp))
        OutlinedTextField(value = supportPhone, onValueChange = { supportPhone = it }, label = { Text("Customer Support Phone / Helpline") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp))
        OutlinedTextField(value = businessAddress, onValueChange = { businessAddress = it }, label = { Text("Registered Business Address") }, maxLines = 2, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp))

        Text("All-India Shipping & Delivery Charges", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyDark)
        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = freeShippingMin,
                onValueChange = { if (it.all { c -> c.isDigit() }) freeShippingMin = it },
                label = { Text("Free Shipping Above (₹)") },
                singleLine = true,
                modifier = Modifier.weight(1f).padding(bottom = 6.dp)
            )
            OutlinedTextField(
                value = defaultShippingFee,
                onValueChange = { if (it.all { c -> c.isDigit() }) defaultShippingFee = it },
                label = { Text("Standard Shipping (₹)") },
                singleLine = true,
                modifier = Modifier.weight(1f).padding(bottom = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text("Payments Configuration (India)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyDark)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = upiId,
            onValueChange = { upiId = it },
            label = { Text("Merchant UPI ID (e.g. novamarket@icici)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Enable Cash on Delivery (COD) across India", fontSize = 13.sp)
            Switch(checked = codAvailable, onCheckedChange = { codAvailable = it })
        }

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = {
                val updated = current.copy(
                    storeName = storeName.trim(),
                    tagline = tagline.trim(),
                    supportEmail = supportEmail.trim(),
                    supportPhone = supportPhone.trim(),
                    businessAddress = businessAddress.trim(),
                    freeShippingMin = freeShippingMin.toDoubleOrNull() ?: 499.0,
                    defaultShippingFee = defaultShippingFee.toDoubleOrNull() ?: 40.0,
                    upiId = upiId.trim(),
                    codAvailable = codAvailable
                )
                onSaveSettings(updated)
                savedNotice = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Save Store Settings", fontWeight = FontWeight.Bold)
        }

        if (savedNotice) {
            Text("Store settings saved successfully!", color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

