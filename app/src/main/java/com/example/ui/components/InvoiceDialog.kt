package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.OrderEntity
import com.example.ui.theme.*

@Composable
fun InvoiceDialog(
    order: OrderEntity,
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
                .testTag("invoice_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header with Close Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "TAX INVOICE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = NavyDark
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "GST Verified",
                            tint = EmeraldGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_invoice_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Divider(color = DividerColor, thickness = 1.dp)

                // Scrollable Invoice Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 12.dp)
                ) {
                    // Store & Invoice Details
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "NovaMarket Retail India Ltd.",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = NavyDark
                            )
                            Text(
                                text = "Cyber City, DLF Phase 2, Gurugram, HR - 122002",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "GSTIN: 06AAACN9182K1Z9",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = RoyalBlue
                            )
                            Text(
                                text = "PAN: AAACN9182K",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Invoice No: ${order.invoiceNumber.ifEmpty { "INV-" + order.id }}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Order ID: ${order.id}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            val orderDate = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.ENGLISH)
                                .format(java.util.Date(order.createdAt))
                            Text(
                                text = "Date: $orderDate",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "Place of Supply: Haryana (06)",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Bill To / Ship To Section
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = OffWhite),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "BILL TO / DELIVER TO:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = order.customerName,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = order.shippingAddressJson,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "Phone: +91 ${order.customerPhone}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Itemized Table Header
                    Surface(
                        color = NavyDark,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Description",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(2f)
                            )
                            Text(
                                text = "HSN",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(0.8f)
                            )
                            Text(
                                text = "Tax",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(0.8f)
                            )
                            Text(
                                text = "Amount",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Order items breakdown
                    val items = order.itemsSummaryJson.split(";").map { it.trim() }.filter { it.isNotEmpty() }
                    items.forEach { itemDesc ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(0.5.dp, DividerColor)
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = itemDesc,
                                fontSize = 11.sp,
                                color = TextPrimary,
                                modifier = Modifier.weight(2f)
                            )
                            Text(
                                text = "847130",
                                fontSize = 10.sp,
                                color = TextSecondary,
                                modifier = Modifier.weight(0.8f)
                            )
                            Text(
                                text = "18%",
                                fontSize = 10.sp,
                                color = TextSecondary,
                                modifier = Modifier.weight(0.8f)
                            )
                            Text(
                                text = "Itemized",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // GST Calculation Summary
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(OffWhite, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                        ) {
                            Text("Subtotal:", fontSize = 12.sp, color = TextSecondary)
                            Text(formatInr(order.subtotal), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        if (order.discount > 0) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                            ) {
                                Text("Coupon Discount (${order.couponCode}):", fontSize = 12.sp, color = EmeraldGreen)
                                Text("-${formatInr(order.discount)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = EmeraldGreen)
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                        ) {
                            Text("Shipping Charges:", fontSize = 12.sp, color = TextSecondary)
                            Text(if (order.shippingFee == 0.0) "FREE" else formatInr(order.shippingFee), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                        ) {
                            Text("CGST (9.0%):", fontSize = 12.sp, color = TextSecondary)
                            Text(formatInr(order.taxGst / 2), fontSize = 12.sp)
                        }
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                        ) {
                            Text("SGST (9.0%):", fontSize = 12.sp, color = TextSecondary)
                            Text(formatInr(order.taxGst / 2), fontSize = 12.sp)
                        }
                        Divider(color = DividerColor, modifier = Modifier.padding(vertical = 6.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Grand Total:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                            Text(formatInr(order.totalAmount), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Payment Mode: ${order.paymentMethod} • Status: ${order.paymentStatus}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Digital Signatory Stamp
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "This is a computer generated invoice and requires no physical signature under Indian IT Act 2000.",
                            fontSize = 9.sp,
                            color = TextMuted,
                            modifier = Modifier.weight(1f).padding(end = 12.dp)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen),
                                color = MintLight,
                                modifier = Modifier.padding(bottom = 4.dp)
                            ) {
                                Text(
                                    text = "★ DIGITALLY SIGNED ★\nNovaMarket Retail Ltd",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreen,
                                    modifier = Modifier.padding(6.dp)
                                )
                            }
                            Text("Authorized Signatory", fontSize = 10.sp, color = TextSecondary)
                        }
                    }
                }

                // Action Bar
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("invoice_download_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = "Download", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Download PDF", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("invoice_share_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share Invoice", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
