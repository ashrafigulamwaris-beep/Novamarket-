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
import com.example.data.model.ProductEntity
import com.example.data.model.UserEntity
import com.example.ui.components.ProductCard
import com.example.ui.theme.*

@Composable
fun AccountScreen(
    user: UserEntity?,
    orderCount: Int,
    wishlistCount: Int,
    onOrdersClick: () -> Unit,
    onWishlistClick: () -> Unit,
    onAddressesClick: () -> Unit,
    onAdminPanelClick: () -> Unit,
    onPolicyClick: (String) -> Unit,
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onSwitchRole: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showOwnerAuthDialog by remember { mutableStateOf(false) }

    if (showOwnerAuthDialog) {
        var ownerPassInput by remember { mutableStateOf("") }
        var ownerAuthError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showOwnerAuthDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = RoyalBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Owner Identity Verification", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column {
                    Text(
                        text = "NovaMarket is a single-owner store. Only the verified store owner has authorization to add products, set prices, and schedule courier pickups.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = ownerPassInput,
                        onValueChange = { ownerPassInput = it },
                        label = { Text("Owner Password") },
                        placeholder = { Text("Enter owner secret password") },
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().testTag("owner_password_input")
                    )

                    if (ownerAuthError != null) {
                        Text(
                            text = ownerAuthError!!,
                            color = CrimsonRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(color = OffWhite, shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Demo Owner Password: owner2026!",
                            fontSize = 10.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (ownerPassInput == "owner2026!") {
                            ownerAuthError = null
                            showOwnerAuthDialog = false
                            onSwitchRole() // switches to owner session
                            onAdminPanelClick()
                        } else {
                            ownerAuthError = "Incorrect owner password. Access denied to customer account."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                    modifier = Modifier.testTag("verify_owner_btn")
                ) {
                    Text("Verify & Enter")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOwnerAuthDialog = false }) { Text("Cancel") }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(OffWhite)
            .testTag("account_screen_scroll")
    ) {
        // User Profile Header Card
        item {
            Surface(
                color = NavyDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(RoyalBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user?.fullName?.take(1)?.uppercase() ?: "G",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = user?.fullName ?: "Guest Customer",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = user?.email ?: "Sign in to track orders and save addresses",
                                color = SkyBlueLight,
                                fontSize = 12.sp
                            )
                            if (user?.phone?.isNotBlank() == true) {
                                Text(
                                    text = "+91 ${user.phone}",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        if (user?.isOwner == true) {
                            Surface(color = AmberGold, shape = RoundedCornerShape(4.dp)) {
                                Text("SOLE STORE OWNER", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }

                    // Role indicator
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onSwitchRole,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberGold),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AmberGold),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("switch_role_btn")
                    ) {
                        Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (user?.isOwner == true) "Switch to Customer View (Aarav Sharma)" else "Switch to Owner View (owner@novamarket.in)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Quick Stats / Navigation Grid
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOrdersClick() }
                        .testTag("account_orders_card")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.ReceiptLong, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("My Orders", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("$orderCount orders", fontSize = 11.sp, color = TextMuted)
                    }
                }

                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onWishlistClick() }
                        .testTag("account_wishlist_card")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.FavoriteBorder, contentDescription = null, tint = CrimsonRed, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Wishlist", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("$wishlistCount saved", fontSize = 11.sp, color = TextMuted)
                    }
                }
            }
        }

        // Admin Management Panel Shortcut
        item {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .clickable {
                        if (user?.isOwner == true) {
                            onAdminPanelClick()
                        } else {
                            showOwnerAuthDialog = true
                        }
                    }
                    .testTag("admin_dashboard_shortcut")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AmberGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.Black)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (user?.isOwner == true) "Store Owner Dashboard" else "Store Owner Portal (Restricted)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (user?.isOwner == true) "Warehouse Pickup, Dispatch, Inventory & Settings" else "Owner verification required to access",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color.White)
                }
            }
        }

        // Account Settings Options
        item {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    AccountOptionRow(
                        icon = Icons.Outlined.LocationOn,
                        title = "Saved Delivery Addresses",
                        subtitle = "Manage home, office & PIN codes",
                        onClick = onAddressesClick
                    )
                    Divider(color = DividerColor)
                    AccountOptionRow(
                        icon = Icons.Outlined.HelpOutline,
                        title = "Frequently Asked Questions (FAQs)",
                        subtitle = "Payments, deliveries & refunds",
                        onClick = { onPolicyClick("FAQS") }
                    )
                    Divider(color = DividerColor)
                    AccountOptionRow(
                        icon = Icons.Outlined.HeadsetMic,
                        title = "Contact Us & Customer Support",
                        subtitle = "+91 1800 200 6682 • Toll-Free",
                        onClick = { onPolicyClick("CONTACT") }
                    )
                    Divider(color = DividerColor)
                    AccountOptionRow(
                        icon = Icons.Outlined.LocalShipping,
                        title = "Shipping & Delivery Policy",
                        subtitle = "Pan-India logistics timelines",
                        onClick = { onPolicyClick("SHIPPING") }
                    )
                    Divider(color = DividerColor)
                    AccountOptionRow(
                        icon = Icons.Outlined.PublishedWithChanges,
                        title = "7-Day Return & Refund Policy",
                        subtitle = "Hassle-free replacements",
                        onClick = { onPolicyClick("RETURNS") }
                    )
                    Divider(color = DividerColor)
                    AccountOptionRow(
                        icon = Icons.Outlined.Security,
                        title = "Privacy Policy & Terms",
                        subtitle = "Customer data protection",
                        onClick = { onPolicyClick("PRIVACY") }
                    )
                }
            }
        }

        // Logout or Login action
        item {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                if (user != null) {
                    OutlinedButton(
                        onClick = onLogoutClick,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonRed),
                        modifier = Modifier.fillMaxWidth().height(46.dp).testTag("account_logout_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Log Out of Account", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onLoginClick,
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                        modifier = Modifier.fillMaxWidth().height(46.dp).testTag("account_login_btn")
                    ) {
                        Text("Sign In / Register", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AccountOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
            Text(subtitle, fontSize = 11.sp, color = TextMuted)
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
    }
}

@Composable
fun WishlistScreen(
    wishlistProducts: List<ProductEntity>,
    onProductClick: (ProductEntity) -> Unit,
    onRemoveFromWishlist: (String) -> Unit,
    onAddToCart: (ProductEntity) -> Unit,
    onStartShopping: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (wishlistProducts.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize().background(OffWhite).padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(imageVector = Icons.Outlined.FavoriteBorder, contentDescription = null, tint = TextMuted, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Your Wishlist is Empty", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyDark)
                Text("Tap the heart icon on any product to save it here for later.", fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(vertical = 4.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onStartShopping, colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)) {
                    Text("Explore Products")
                }
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = modifier.fillMaxSize().background(OffWhite).testTag("wishlist_scroll")
        ) {
            items(wishlistProducts) { prod ->
                ProductCard(
                    product = prod,
                    isInWishlist = true,
                    onProductClick = { onProductClick(prod) },
                    onWishlistToggle = { onRemoveFromWishlist(prod.id) },
                    onAddToCart = { onAddToCart(prod) },
                    onBuyNow = { onAddToCart(prod) }
                )
            }
        }
    }
}
