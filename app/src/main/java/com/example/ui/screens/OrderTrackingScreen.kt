package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderEntity
import com.example.ui.components.InvoiceDialog
import com.example.ui.components.formatInr
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(
    order: OrderEntity,
    onBackClick: () -> Unit,
    onCancelOrder: (orderId: String) -> Unit,
    onRequestReturn: (orderId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showInvoiceDialog by remember { mutableStateOf(false) }

    var showShippingLabelDialog by remember { mutableStateOf(false) }

    if (showInvoiceDialog) {
        InvoiceDialog(order = order, onDismiss = { showInvoiceDialog = false })
    }

    if (showShippingLabelDialog) {
        com.example.ui.components.ShippingLabelDialog(
            order = order,
            pickupAddress = null,
            onDismiss = { showShippingLabelDialog = false }
        )
    }

    val steps = listOf(
        "Order Confirmed" to "Seller verified & accepted order",
        "Packed at Warehouse" to "Quality checked & packed by seller in secure box",
        "Courier Pickup Scheduled" to "${order.courierName} • Slot: ${order.pickupScheduledSlot.ifEmpty { "11:00 AM - 02:00 PM" }}",
        "Picked Up by Courier" to "Courier collected package from seller address (AWB: ${order.trackingNumber.ifEmpty { "Assigned Soon" }})",
        "In Transit" to "Parcel moving via hub network to destination city",
        "Out for Delivery" to "Courier delivery executive out for doorstep delivery",
        "Delivered" to "Delivered to your shipping address"
    )

    val currentStepIndex = when (order.orderStatus) {
        "PENDING_PAYMENT" -> 0
        "PLACED", "ORDER_CONFIRMED", "PROCESSING" -> 0
        "PACKED" -> 1
        "PICKUP_REQUESTED", "PICKUP_CONFIRMED" -> 2
        "PICKED_UP" -> 3
        "IN_TRANSIT" -> 4
        "OUT_FOR_DELIVERY" -> 5
        "DELIVERED" -> 6
        "CANCELLED" -> -1
        "RETURN_REQUESTED", "RETURNED" -> 7
        else -> 1
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Details & Tracking", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { showShippingLabelDialog = true }) {
                        Icon(imageVector = Icons.Default.QrCode, contentDescription = "Label", tint = RoyalBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Waybill", color = RoyalBlue, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    TextButton(onClick = { showInvoiceDialog = true }, modifier = Modifier.testTag("open_invoice_btn")) {
                        Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = "Invoice", tint = RoyalBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Tax Invoice", color = RoyalBlue, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(OffWhite)
                .testTag("order_tracking_scroll")
        ) {
            // Header Card
            item {
                Surface(
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text("Order #${order.id}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyDark)
                                val dateStr = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.ENGLISH)
                                    .format(java.util.Date(order.createdAt))
                                Text("Placed on $dateStr", fontSize = 11.sp, color = TextSecondary)
                            }

                            // Status badge
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when (order.orderStatus) {
                                    "DELIVERED" -> MintLight
                                    "CANCELLED" -> RoseLight
                                    else -> SkyBlueLight
                                }
                            ) {
                                Text(
                                    text = order.orderStatus.replace("_", " "),
                                    color = when (order.orderStatus) {
                                        "DELIVERED" -> EmeraldGreen
                                        "CANCELLED" -> CrimsonRed
                                        else -> RoyalBlue
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Courier: ${order.courierName} • Tracking AWB: ${order.trackingNumber.ifEmpty { "Pending dispatch" }}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                }
            }

            // Real-Time Delivery Step Tracker
            item {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("LIVE SHIPMENT STATUS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Spacer(modifier = Modifier.height(14.dp))

                        if (order.orderStatus == "CANCELLED") {
                            Surface(color = RoseLight, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Cancel, contentDescription = null, tint = CrimsonRed)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("This order has been cancelled.", color = CrimsonRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        } else {
                            steps.forEachIndexed { index, (title, sub) ->
                                val isDone = index <= currentStepIndex
                                val isCurrent = index == currentStepIndex

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    // Step Indicator Circle & Line
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.width(28.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isDone) EmeraldGreen else Color(0xFFE2E8F0)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isDone) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(TextMuted)
                                                )
                                            }
                                        }

                                        if (index < steps.size - 1) {
                                            Box(
                                                modifier = Modifier
                                                    .width(2.dp)
                                                    .height(36.dp)
                                                    .background(if (index < currentStepIndex) EmeraldGreen else CardBorder)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.padding(bottom = 16.dp)) {
                                        Text(
                                            text = title,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = if (isDone) TextPrimary else TextMuted
                                        )
                                        Text(
                                            text = sub,
                                            fontSize = 11.sp,
                                            color = if (isDone) TextSecondary else TextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Delivery Address Card
            item {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("DELIVERY ADDRESS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(order.customerName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(order.shippingAddressJson, fontSize = 12.sp, color = TextSecondary)
                        Text("Phone: +91 ${order.customerPhone}", fontSize = 11.sp, color = TextMuted)
                    }
                }
            }

            // Items Purchased Card
            item {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("ITEMS IN THIS ORDER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        val items = order.itemsSummaryJson.split(";").map { it.trim() }.filter { it.isNotEmpty() }
                        items.forEach { itemText ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(itemText, fontSize = 12.sp, color = TextPrimary)
                            }
                        }
                    }
                }
            }

            // Payment and Price Details Card
            item {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("PAYMENT DETAILS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Divider(color = DividerColor, modifier = Modifier.padding(vertical = 8.dp))

                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                            Text("Payment Mode", fontSize = 12.sp)
                            Text("${order.paymentMethod} (${order.paymentStatus})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                            Text("Item Subtotal", fontSize = 12.sp)
                            Text(formatInr(order.subtotal), fontSize = 12.sp)
                        }
                        if (order.discount > 0) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                                Text("Discount (${order.couponCode})", fontSize = 12.sp, color = EmeraldGreen)
                                Text("-${formatInr(order.discount)}", fontSize = 12.sp, color = EmeraldGreen, fontWeight = FontWeight.Bold)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                            Text("Delivery Fee", fontSize = 12.sp)
                            Text(if (order.shippingFee == 0.0) "FREE" else formatInr(order.shippingFee), fontSize = 12.sp)
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                            Text("GST (18%)", fontSize = 12.sp)
                            Text(formatInr(order.taxGst), fontSize = 12.sp)
                        }
                        Divider(color = DividerColor, modifier = Modifier.padding(vertical = 6.dp))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Total Paid", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                            Text(formatInr(order.totalAmount), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
                        }
                    }
                }
            }

            // Order Actions (Cancel / Return)
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (order.orderStatus in listOf("PLACED", "CONFIRMED", "PROCESSING")) {
                        OutlinedButton(
                            onClick = { onCancelOrder(order.id) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonRed),
                            modifier = Modifier.fillMaxWidth().height(46.dp).testTag("cancel_order_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Cancel, contentDescription = "Cancel", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cancel Order", fontWeight = FontWeight.Bold)
                        }
                    } else if (order.orderStatus == "DELIVERED") {
                        Button(
                            onClick = { onRequestReturn(order.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyDark),
                            modifier = Modifier.fillMaxWidth().height(46.dp).testTag("request_return_btn")
                        ) {
                            Icon(imageVector = Icons.Default.PublishedWithChanges, contentDescription = "Return", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Request 7-Day Return / Replacement", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MyOrdersScreen(
    orders: List<OrderEntity>,
    onOrderClick: (OrderEntity) -> Unit,
    onStartShopping: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredOrders = remember(orders, selectedFilter) {
        when (selectedFilter) {
            "ACTIVE" -> orders.filter { it.orderStatus !in listOf("DELIVERED", "CANCELLED", "RETURNED") }
            "DELIVERED" -> orders.filter { it.orderStatus == "DELIVERED" }
            "CANCELLED" -> orders.filter { it.orderStatus == "CANCELLED" }
            else -> orders
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OffWhite)
            .testTag("my_orders_screen")
    ) {
        // Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("ALL" to "All Orders", "ACTIVE" to "Active Orders", "DELIVERED" to "Delivered", "CANCELLED" to "Cancelled").forEach { (code, label) ->
                FilterChip(
                    selected = selectedFilter == code,
                    onClick = { selectedFilter = code },
                    label = { Text(label, fontSize = 11.sp) }
                )
            }
        }

        if (filteredOrders.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Outlined.ReceiptLong, contentDescription = null, tint = TextMuted, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No Orders Found", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyDark)
                    Text("Your placed orders and delivery tracking will appear here.", fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(vertical = 4.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onStartShopping, colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)) {
                        Text("Browse NovaMarket")
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredOrders) { order ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOrderClick(order) }
                            .testTag("order_item_${order.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Order #${order.id}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyDark)
                                Surface(
                                    color = when (order.orderStatus) {
                                        "DELIVERED" -> MintLight
                                        "CANCELLED" -> RoseLight
                                        else -> SkyBlueLight
                                    },
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = order.orderStatus.replace("_", " "),
                                        color = when (order.orderStatus) {
                                            "DELIVERED" -> EmeraldGreen
                                            "CANCELLED" -> CrimsonRed
                                            else -> RoyalBlue
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = order.itemsSummaryJson,
                                fontSize = 12.sp,
                                maxLines = 2,
                                color = TextPrimary,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                            ) {
                                Text(
                                    text = formatInr(order.totalAmount),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RoyalBlue
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Track & Invoice", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
                                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
