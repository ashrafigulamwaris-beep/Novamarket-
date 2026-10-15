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
import com.example.data.model.AddressEntity
import com.example.data.model.CouponEntity
import com.example.data.repository.CartItemWithProduct
import com.example.ui.components.formatInr
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    cartItems: List<CartItemWithProduct>,
    addresses: List<AddressEntity>,
    appliedCoupon: CouponEntity?,
    onBackClick: () -> Unit,
    onSaveNewAddress: (AddressEntity) -> Unit,
    onConfirmOrder: (paymentMethod: String, address: AddressEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedAddressId by remember(addresses) {
        mutableStateOf(addresses.firstOrNull { it.isDefault }?.id ?: addresses.firstOrNull()?.id)
    }

    var showAddAddressDialog by remember { mutableStateOf(addresses.isEmpty()) }
    var selectedPaymentMethod by remember { mutableStateOf("UPI") } // UPI, COD, CARD, NETBANKING
    var upiAppChoice by remember { mutableStateOf("Google Pay") }
    var customUpiId by remember { mutableStateOf("") }
    var codCaptchaAnswer by remember { mutableStateOf("") }
    var isProcessingPayment by remember { mutableStateOf(false) }

    val subtotal = remember(cartItems) {
        cartItems.sumOf { it.product.price * it.cartItem.quantity }
    }
    val couponDiscount = remember(subtotal, appliedCoupon) {
        if (appliedCoupon == null || subtotal < appliedCoupon.minOrderValue) 0.0
        else {
            if (appliedCoupon.discountPercent > 0) {
                (subtotal * (appliedCoupon.discountPercent / 100.0)).coerceAtMost(appliedCoupon.maxDiscount)
            } else appliedCoupon.flatDiscount
        }
    }
    val shippingFee = if (subtotal >= 499.0 || subtotal == 0.0) 0.0 else 40.0
    val taxableAmount = (subtotal - couponDiscount).coerceAtLeast(0.0)
    val taxGst = taxableAmount * 0.18
    val grandTotal = taxableAmount + shippingFee + taxGst

    val chosenAddress = remember(selectedAddressId, addresses) {
        addresses.firstOrNull { it.id == selectedAddressId }
    }

    // New Address Modal Dialog
    if (showAddAddressDialog) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var house by remember { mutableStateOf("") }
        var street by remember { mutableStateOf("") }
        var villageLocality by remember { mutableStateOf("") }
        var district by remember { mutableStateOf("") }
        var landmark by remember { mutableStateOf("") }
        var city by remember { mutableStateOf("") }
        var state by remember { mutableStateOf("") }
        var pin by remember { mutableStateOf("") }
        var formError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { if (addresses.isNotEmpty()) showAddAddressDialog = false },
            title = { Text("Add Delivery Address (India)", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
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
                        label = { Text("Full Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { if (it.length <= 10 && it.all { c -> c.isDigit() }) phone = it },
                        label = { Text("10-Digit Mobile Number *") },
                        prefix = { Text("+91 ") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = pin,
                            onValueChange = {
                                if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                                    pin = it
                                    if (it == "122001") { city = "Gurugram"; district = "Gurugram"; state = "Haryana" }
                                    else if (it == "110001") { city = "New Delhi"; district = "Central Delhi"; state = "Delhi" }
                                    else if (it == "400001") { city = "Mumbai"; district = "Mumbai City"; state = "Maharashtra" }
                                    else if (it == "560001") { city = "Bengaluru"; district = "Bengaluru Urban"; state = "Karnataka" }
                                }
                            },
                            label = { Text("6-Digit PIN *") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text("City *") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 6.dp)) {
                        OutlinedTextField(
                            value = district,
                            onValueChange = { district = it },
                            label = { Text("District") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = state,
                            onValueChange = { state = it },
                            label = { Text("State *") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = house,
                        onValueChange = { house = it },
                        label = { Text("Flat / House No. / Building *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = street,
                        onValueChange = { street = it },
                        label = { Text("Street / Road *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = villageLocality,
                        onValueChange = { villageLocality = it },
                        label = { Text("Village / Locality / Sector") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = landmark,
                        onValueChange = { landmark = it },
                        label = { Text("Landmark (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (formError != null) {
                        Text(formError!!, color = CrimsonRed, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isBlank() || phone.length < 10 || pin.length < 6 || house.isBlank() || street.isBlank() || city.isBlank() || state.isBlank()) {
                            formError = "Please fill in all mandatory fields with valid Indian address details."
                        } else {
                            val newAddr = AddressEntity(
                                id = "addr_" + System.currentTimeMillis(),
                                userId = "usr_customer",
                                fullName = name,
                                phone = phone,
                                houseFlat = house,
                                streetArea = street,
                                landmark = landmark,
                                villageLocality = villageLocality,
                                district = district,
                                city = city,
                                state = state,
                                pinCode = pin,
                                isDefault = addresses.isEmpty()
                            )
                            onSaveNewAddress(newAddr)
                            selectedAddressId = newAddr.id
                            showAddAddressDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text("Save Address")
                }
            },
            dismissButton = {
                if (addresses.isNotEmpty()) {
                    TextButton(onClick = { showAddAddressDialog = false }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Secure Order Checkout", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
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
                        Text("Order Total", fontSize = 11.sp, color = TextSecondary)
                        Text(
                            text = formatInr(grandTotal),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyDark
                        )
                    }

                    Button(
                        onClick = {
                            if (chosenAddress == null) {
                                showAddAddressDialog = true
                            } else {
                                isProcessingPayment = true
                                onConfirmOrder(selectedPaymentMethod, chosenAddress)
                            }
                        },
                        enabled = !isProcessingPayment && chosenAddress != null,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color.Black),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("confirm_order_place_btn")
                    ) {
                        if (isProcessingPayment) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Securing Order...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Text(
                                text = if (selectedPaymentMethod == "COD") "Place Order (COD)" else "Pay & Place Order",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
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
                .testTag("checkout_scroll")
        ) {
            // Step 1: Select Delivery Address
            item {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = RoyalBlue,
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.size(22.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("1", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Delivery Address", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                            }
                            TextButton(onClick = { showAddAddressDialog = true }) {
                                Text("+ Add New", fontSize = 12.sp, color = RoyalBlue, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (addresses.isEmpty()) {
                            Text("No saved delivery address found. Please add an address to continue.", fontSize = 12.sp, color = CrimsonRed)
                        } else {
                            addresses.forEach { addr ->
                                val isSelected = addr.id == selectedAddressId
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) RoyalBlue else CardBorder
                                    ),
                                    color = if (isSelected) SkyBlueLight else Color.White,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable { selectedAddressId = addr.id }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { selectedAddressId = addr.id }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(addr.fullName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(color = Color(0xFFF1F5F9), shape = RoundedCornerShape(4.dp)) {
                                                    Text(addr.addressType, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                                }
                                            }
                                            Text("${addr.houseFlat}, ${addr.streetArea}", fontSize = 12.sp, color = TextSecondary)
                                            Text("${addr.city}, ${addr.state} - ${addr.pinCode}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                            Text("Mobile: +91 ${addr.phone}", fontSize = 11.sp, color = TextMuted)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Step 2: Items in Order
            item {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = RoyalBlue,
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.size(22.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("2", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Order Items Summary (${cartItems.size})", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        cartItems.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${item.product.name} (x${item.cartItem.quantity})",
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                                )
                                Text(
                                    text = formatInr(item.product.price * item.cartItem.quantity),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Step 3: Indian Payment Gateway & Verification Options
            item {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = RoyalBlue,
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.size(22.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("3", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Payment Options (India)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Option A: UPI
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedPaymentMethod == "UPI") RoyalBlue else CardBorder),
                            color = if (selectedPaymentMethod == "UPI") SkyBlueLight else Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedPaymentMethod = "UPI" }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = selectedPaymentMethod == "UPI",
                                        onClick = { selectedPaymentMethod = "UPI" }
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("UPI (Google Pay, PhonePe, Paytm, BHIM)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(color = EmeraldGreen, shape = RoundedCornerShape(4.dp)) {
                                        Text("INSTANT", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                    }
                                }

                                if (selectedPaymentMethod == "UPI") {
                                    Column(modifier = Modifier.padding(start = 36.dp, top = 6.dp)) {
                                        Text("Select UPI App or enter VPA ID:", fontSize = 11.sp, color = TextSecondary)
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.padding(vertical = 6.dp)
                                        ) {
                                            listOf("Google Pay", "PhonePe", "Paytm", "Other UPI").forEach { appName ->
                                                FilterChip(
                                                    selected = upiAppChoice == appName,
                                                    onClick = { upiAppChoice = appName },
                                                    label = { Text(appName, fontSize = 10.sp) }
                                                )
                                            }
                                        }
                                        if (upiAppChoice == "Other UPI") {
                                            OutlinedTextField(
                                                value = customUpiId,
                                                onValueChange = { customUpiId = it },
                                                placeholder = { Text("username@upi", fontSize = 11.sp) },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth().height(48.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Option B: Cash on Delivery (COD)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedPaymentMethod == "COD") RoyalBlue else CardBorder),
                            color = if (selectedPaymentMethod == "COD") SkyBlueLight else Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedPaymentMethod = "COD" }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = selectedPaymentMethod == "COD",
                                        onClick = { selectedPaymentMethod = "COD" }
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Cash on Delivery (COD)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                if (selectedPaymentMethod == "COD") {
                                    Text(
                                        text = "Pay with cash or UPI to the courier delivery agent when your order arrives at your doorstep.",
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(start = 36.dp, top = 4.dp)
                                    )
                                }
                            }
                        }

                        // Option C: Credit / Debit Cards
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedPaymentMethod == "CARD") RoyalBlue else CardBorder),
                            color = if (selectedPaymentMethod == "CARD") SkyBlueLight else Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedPaymentMethod = "CARD" }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = selectedPaymentMethod == "CARD",
                                        onClick = { selectedPaymentMethod = "CARD" }
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Credit / Debit Cards (Visa, MasterCard, RuPay)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                if (selectedPaymentMethod == "CARD") {
                                    Text(
                                        text = "Processed through 128-bit SSL encrypted PCI-DSS compliant Indian payment gateway with 3D Secure OTP verification.",
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(start = 36.dp, top = 4.dp)
                                    )
                                }
                            }
                        }

                        // Option D: Net Banking
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedPaymentMethod == "NETBANKING") RoyalBlue else CardBorder),
                            color = if (selectedPaymentMethod == "NETBANKING") SkyBlueLight else Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedPaymentMethod = "NETBANKING" }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = selectedPaymentMethod == "NETBANKING",
                                        onClick = { selectedPaymentMethod = "NETBANKING" }
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Net Banking (SBI, HDFC, ICICI, Axis)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Step 4: Price details summary
            item {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("FINAL PAYMENT BREAKDOWN", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Divider(color = DividerColor, modifier = Modifier.padding(vertical = 8.dp))

                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                            Text("Item Subtotal", fontSize = 12.sp)
                            Text(formatInr(subtotal), fontSize = 12.sp)
                        }
                        if (couponDiscount > 0) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                                Text("Coupon Discount", fontSize = 12.sp, color = EmeraldGreen)
                                Text("-${formatInr(couponDiscount)}", fontSize = 12.sp, color = EmeraldGreen, fontWeight = FontWeight.Bold)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                            Text("Delivery Fee", fontSize = 12.sp)
                            Text(if (shippingFee == 0.0) "FREE" else formatInr(shippingFee), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                            Text("CGST + SGST (18%)", fontSize = 12.sp)
                            Text(formatInr(taxGst), fontSize = 12.sp)
                        }
                        Divider(color = DividerColor, modifier = Modifier.padding(vertical = 6.dp))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Amount to Pay", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                            Text(formatInr(grandTotal), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
