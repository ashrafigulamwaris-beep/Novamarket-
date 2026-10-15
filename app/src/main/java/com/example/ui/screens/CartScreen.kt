package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.CouponEntity
import com.example.data.repository.CartItemWithProduct
import com.example.ui.components.formatInr
import com.example.ui.theme.*

@Composable
fun CartScreen(
    cartItems: List<CartItemWithProduct>,
    appliedCoupon: CouponEntity?,
    couponError: String?,
    onQuantityChange: (cartItemId: String, newQty: Int) -> Unit,
    onRemoveItem: (cartItemId: String) -> Unit,
    onApplyCoupon: (code: String, subtotal: Double) -> Unit,
    onRemoveCoupon: () -> Unit,
    onProceedToCheckout: () -> Unit,
    onStartShopping: () -> Unit,
    modifier: Modifier = Modifier
) {
    val subtotal = remember(cartItems) {
        cartItems.sumOf { it.product.price * it.cartItem.quantity }
    }

    val couponDiscount = remember(subtotal, appliedCoupon) {
        if (appliedCoupon == null || subtotal < appliedCoupon.minOrderValue) 0.0
        else {
            if (appliedCoupon.discountPercent > 0) {
                (subtotal * (appliedCoupon.discountPercent / 100.0)).coerceAtMost(appliedCoupon.maxDiscount)
            } else {
                appliedCoupon.flatDiscount
            }
        }
    }

    val freeShippingMin = 499.0
    val shippingFee = if (subtotal >= freeShippingMin || subtotal == 0.0) 0.0 else 40.0
    val taxableAmount = (subtotal - couponDiscount).coerceAtLeast(0.0)
    val taxGst = taxableAmount * 0.18 // 18% GST in India
    val grandTotal = taxableAmount + shippingFee + taxGst

    var couponInput by remember { mutableStateOf("") }

    if (cartItems.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(OffWhite)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Outlined.RemoveShoppingCart,
                    contentDescription = "Empty Cart",
                    tint = TextMuted,
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Your NovaMarket Cart is Empty",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyDark
                )
                Text(
                    text = "Explore top Indian deals and add your favorite items to your cart today.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onStartShopping,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                    modifier = Modifier.testTag("empty_cart_shop_btn")
                ) {
                    Text("Start Shopping Now", fontWeight = FontWeight.Bold)
                }
            }
        }
    } else {
        Scaffold(
            bottomBar = {
                Surface(
                    color = Color.White,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Payable", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = formatInr(grandTotal),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyDark
                            )
                        }

                        Button(
                            onClick = onProceedToCheckout,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color.Black),
                            modifier = Modifier
                                .height(46.dp)
                                .testTag("cart_proceed_checkout_btn")
                        ) {
                            Text("Proceed to Buy (${cartItems.sumOf { it.cartItem.quantity }})", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            },
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(OffWhite)
                    .testTag("cart_scroll")
            ) {
                // Free Shipping Threshold Progress
                item {
                    val progress = (subtotal / freeShippingMin).toFloat().coerceIn(0f, 1f)
                    Surface(
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            if (subtotal >= freeShippingMin) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Congratulations! Your order qualifies for FREE Delivery.",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldGreen
                                    )
                                }
                            } else {
                                val remaining = freeShippingMin - subtotal
                                Text(
                                    text = "Add ${formatInr(remaining)} more to get FREE Delivery!",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = NavyDark
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { progress },
                                    color = EmeraldGreen,
                                    trackColor = MintLight,
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                                )
                            }
                        }
                    }
                }

                // Cart Items List
                items(cartItems, key = { it.cartItem.id }) { item ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                            .testTag("cart_item_${item.cartItem.id}")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                // Thumbnail
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(item.product.imagesJson.split(",").firstOrNull())
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = item.product.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(76.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF1F5F9))
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.product.brand.uppercase(),
                                        fontSize = 10.sp,
                                        color = TextMuted,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = item.product.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 2,
                                        color = TextPrimary
                                    )
                                    if (item.cartItem.selectedVariant.isNotEmpty()) {
                                        Text(
                                            text = item.cartItem.selectedVariant,
                                            fontSize = 11.sp,
                                            color = RoyalBlue,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = formatInr(item.product.price),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        if (item.product.originalPrice > item.product.price) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = formatInr(item.product.originalPrice),
                                                fontSize = 12.sp,
                                                color = TextMuted,
                                                textDecoration = TextDecoration.LineThrough
                                            )
                                        }
                                    }
                                }
                            }

                            Divider(color = DividerColor, modifier = Modifier.padding(vertical = 10.dp))

                            // Quantity Stepper and Remove
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Decrement
                                    OutlinedIconButton(
                                        onClick = {
                                            onQuantityChange(item.cartItem.id, item.cartItem.quantity - 1)
                                        },
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                                    }

                                    Text(
                                        text = "${item.cartItem.quantity}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 14.dp)
                                    )

                                    // Increment
                                    OutlinedIconButton(
                                        onClick = {
                                            if (item.cartItem.quantity < item.product.stockQuantity) {
                                                onQuantityChange(item.cartItem.id, item.cartItem.quantity + 1)
                                            }
                                        },
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                                    }
                                }

                                TextButton(
                                    onClick = { onRemoveItem(item.cartItem.id) },
                                    colors = ButtonDefaults.textButtonColors(contentColor = CrimsonRed)
                                ) {
                                    Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Remove", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Remove", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Coupon Card Section
                item {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.LocalOffer, contentDescription = "Coupon", tint = AmberGold, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Apply Promo Code / Coupon", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            if (appliedCoupon != null) {
                                Surface(
                                    color = MintLight,
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "${appliedCoupon.code} Applied!",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = EmeraldGreen
                                            )
                                            Text(
                                                text = appliedCoupon.description,
                                                fontSize = 11.sp,
                                                color = TextSecondary
                                            )
                                        }
                                        IconButton(onClick = onRemoveCoupon) {
                                            Icon(imageVector = Icons.Default.Close, contentDescription = "Remove Coupon", tint = CrimsonRed)
                                        }
                                    }
                                }
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = couponInput,
                                        onValueChange = { couponInput = it.uppercase() },
                                        placeholder = { Text("e.g. WELCOME100, NOVA10", fontSize = 11.sp) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(50.dp)
                                            .testTag("coupon_input_field")
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            if (couponInput.isNotBlank()) {
                                                onApplyCoupon(couponInput, subtotal)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .height(50.dp)
                                            .testTag("apply_coupon_btn")
                                    ) {
                                        Text("Apply", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (couponError != null) {
                                    Text(
                                        text = couponError,
                                        fontSize = 11.sp,
                                        color = CrimsonRed,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Price Summary Breakdown Card
                item {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "PRICE DETAILS (${cartItems.sumOf { it.cartItem.quantity }} Items)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 0.5.sp
                            )
                            Divider(color = DividerColor, modifier = Modifier.padding(vertical = 8.dp))

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                            ) {
                                Text("Total MRP / Subtotal", fontSize = 13.sp, color = TextPrimary)
                                Text(formatInr(subtotal), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }

                            if (couponDiscount > 0) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                                ) {
                                    Text("Coupon Discount", fontSize = 13.sp, color = EmeraldGreen)
                                    Text("-${formatInr(couponDiscount)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                            ) {
                                Text("Delivery Charges", fontSize = 13.sp, color = TextPrimary)
                                if (shippingFee == 0.0) {
                                    Text("FREE", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                                } else {
                                    Text(formatInr(shippingFee), fontSize = 13.sp)
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                            ) {
                                Text("Estimated GST (18%)", fontSize = 13.sp, color = TextPrimary)
                                Text(formatInr(taxGst), fontSize = 13.sp)
                            }

                            Divider(color = DividerColor, modifier = Modifier.padding(vertical = 8.dp))

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Total Amount", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                                Text(formatInr(grandTotal), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
                            }

                            if (couponDiscount > 0) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "You will save ${formatInr(couponDiscount)} on this order!",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreen
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}
