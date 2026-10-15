package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.OrderEntity
import com.example.data.model.PickupAddressEntity
import com.example.ui.theme.*

@Composable
fun ShippingLabelDialog(
    order: OrderEntity,
    pickupAddress: PickupAddressEntity?,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .testTag("shipping_label_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null, tint = RoyalBlue)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("COURIER SHIPPING LABEL & PACKING SLIP", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Divider(color = DividerColor)

                // Label Body (Standard Indian Logistics Label Layout)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, Color.Black, RoundedCornerShape(4.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            // Top Row: Courier Logo & Routing Code
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    Text(
                                        text = order.courierName.uppercase(),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp,
                                        color = NavyDark
                                    )
                                    Text(
                                        text = "EXPRESS SURFACE LOGISTICS",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }

                                Surface(
                                    color = if (order.paymentMethod == "COD") AmberGold else NavyDark,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (order.paymentMethod == "COD") "COD: ${formatInr(order.totalAmount)}" else "PREPAID",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (order.paymentMethod == "COD") Color.Black else Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Divider(color = Color.Black, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

                            // Barcode & AWB Display
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                // Simulated 1D Barcode pattern
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().height(42.dp)
                                ) {
                                    val pattern = listOf(3, 1, 2, 4, 1, 3, 2, 1, 4, 2, 3, 1, 2, 3, 4, 1, 2, 3, 1, 4, 2, 1, 3)
                                    pattern.forEach { w ->
                                        Box(
                                            modifier = Modifier
                                                .width(w.dp)
                                                .fillMaxHeight()
                                                .background(Color.Black)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                    }
                                }
                                Text(
                                    text = "AWB: ${order.trackingNumber.ifEmpty { "PENDING-PICKUP-ALLOCATION" }}",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                Text("Routing Code: DEL/GUR/SEC18/HUB", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Divider(color = Color.Black, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

                            // Shipping To (Customer)
                            Text("DELIVER TO:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextSecondary)
                            Text(order.customerName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(order.shippingAddressJson, fontSize = 12.sp, lineHeight = 16.sp, modifier = Modifier.padding(vertical = 2.dp))
                            Text("Contact Mobile: +91 ${order.customerPhone}", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                            Divider(color = Color.Black, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

                            // Pickup From (Seller Registered Address)
                            Text("PICKUP FROM (SELLER):", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextSecondary)
                            val pickup = pickupAddress
                            if (pickup != null) {
                                Text(pickup.facilityName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${pickup.addressLine1}, ${pickup.addressLine2}", fontSize = 11.sp)
                                Text("${pickup.city}, ${pickup.state} - ${pickup.pinCode}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Text("Contact Person: ${pickup.contactPerson} (+91 ${pickup.contactPhone})", fontSize = 11.sp)
                            } else {
                                Text("NovaMarket Central Hub, Plot 88, Sector 18, Udyog Vihar Phase 4, Gurugram, HR - 122016", fontSize = 11.sp)
                            }

                            Divider(color = Color.Black, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

                            // Order contents & QR
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("ORDER #${order.id}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(order.itemsSummaryJson, fontSize = 11.sp, maxLines = 2)
                                    Text("Invoice: ${order.invoiceNumber}", fontSize = 10.sp, color = TextMuted)
                                    if (order.pickupScheduledSlot.isNotEmpty()) {
                                        Text("Courier Pickup Window: ${order.pickupScheduledSlot}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.QrCode2,
                                    contentDescription = "Scan QR",
                                    modifier = Modifier.size(54.dp)
                                )
                            }
                        }
                    }
                }

                // Action Bar
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = NavyDark),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(46.dp).testTag("print_shipping_label_btn")
                ) {
                    Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Print Shipping Label & Packing Slip", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
