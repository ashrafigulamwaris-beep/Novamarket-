package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.ProductEntity
import com.example.data.model.ReviewEntity
import com.example.ui.components.formatInr
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    product: ProductEntity,
    reviews: List<ReviewEntity>,
    relatedProducts: List<ProductEntity>,
    isInWishlist: Boolean,
    deliveryPinCode: String,
    onBackClick: () -> Unit,
    onWishlistToggle: () -> Unit,
    onAddToCart: (selectedVariant: String, quantity: Int) -> Unit,
    onBuyNow: (selectedVariant: String, quantity: Int) -> Unit,
    onPinCodeCheck: (String) -> Unit,
    onAddReview: (rating: Int, comment: String) -> Unit,
    onRelatedProductClick: (ProductEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val images = remember(product.imagesJson) {
        product.imagesJson.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            .ifEmpty { listOf("https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800") }
    }

    // Parse variants (e.g. "Color:Black,Blue;Storage:128GB,256GB")
    val parsedVariants = remember(product.variantsJson) {
        if (product.variantsJson.isBlank()) emptyList()
        else {
            product.variantsJson.split(";").mapNotNull { group ->
                val parts = group.split(":")
                if (parts.size == 2) {
                    val groupName = parts[0].trim()
                    val options = parts[1].split(",").map { it.trim() }
                    groupName to options
                } else null
            }
        }
    }

    val selectedVariantsState = remember(product.id) {
        mutableStateMapOf<String, String>().apply {
            parsedVariants.forEach { (name, options) ->
                if (options.isNotEmpty()) put(name, options.first())
            }
        }
    }

    // Parse specs (e.g. "Warranty:1 Year;Made in:India")
    val parsedSpecs = remember(product.specsJson) {
        if (product.specsJson.isBlank()) emptyList()
        else {
            product.specsJson.split(";").mapNotNull { spec ->
                val parts = spec.split(":")
                if (parts.size == 2) parts[0].trim() to parts[1].trim() else null
            }
        }
    }

    var quantity by remember { mutableIntStateOf(1) }
    var pinInput by remember { mutableStateOf(deliveryPinCode) }
    var pinCheckedMessage by remember { mutableStateOf<String?>(null) }
    var showReviewDialog by remember { mutableStateOf(false) }

    val discountPercent = if (product.originalPrice > product.price) {
        (((product.originalPrice - product.price) / product.originalPrice) * 100).toInt()
    } else 0

    // Review Dialog
    if (showReviewDialog) {
        var userRating by remember { mutableIntStateOf(5) }
        var reviewText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            title = { Text("Write a Product Review", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column {
                    Text("Select your rating:", fontSize = 13.sp)
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (i in 1..5) {
                            IconButton(
                                onClick = { userRating = i },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (i <= userRating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                    contentDescription = "$i Star",
                                    tint = AmberGold,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = reviewText,
                        onValueChange = { reviewText = it },
                        label = { Text("Share your honest feedback") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reviewText.isNotBlank()) {
                            onAddReview(userRating, reviewText)
                            showReviewDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text("Submit Review")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReviewDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(product.brand, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onWishlistToggle) {
                        Icon(
                            imageVector = if (isInWishlist) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Wishlist",
                            tint = if (isInWishlist) CrimsonRed else TextPrimary
                        )
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
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Selected variant string
                    val currentVariantString = selectedVariantsState.entries.joinToString(", ") { "${it.key}: ${it.value}" }

                    OutlinedButton(
                        onClick = { onAddToCart(currentVariantString, quantity) },
                        enabled = product.stockQuantity > 0,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("detail_add_to_cart_btn")
                    ) {
                        Icon(imageVector = Icons.Outlined.ShoppingCart, contentDescription = "Cart", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add to Cart", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onBuyNow(currentVariantString, quantity) },
                        enabled = product.stockQuantity > 0,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color.Black),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("detail_buy_now_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Bolt, contentDescription = "Buy Now", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Buy Now", fontWeight = FontWeight.Bold)
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
                .testTag("product_detail_scroll")
        ) {
            // 1. Image Pager
            item {
                val pagerState = rememberPagerState(pageCount = { images.size })
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(bottom = 12.dp)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    ) { page ->
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(images[page])
                                .crossfade(true)
                                .build(),
                            contentDescription = product.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        )
                    }

                    // Dot Indicators
                    if (images.size > 1) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            repeat(images.size) { iteration ->
                                val color = if (pagerState.currentPage == iteration) RoyalBlue else CardBorder
                                Box(
                                    modifier = Modifier
                                        .padding(3.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .size(6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 2. Title, SKU & Rating Card
            item {
                Surface(
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = product.brand.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoyalBlue,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = product.name,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NavyDark,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
                        )
                        Text(
                            text = "SKU: ${product.sku}",
                            fontSize = 11.sp,
                            color = TextMuted
                        )

                        // Rating + Review Count
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Surface(
                                color = EmeraldGreen,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "%.1f".format(product.rating),
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = "Rating",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${product.reviewCount} Ratings & Reviews",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Divider(color = DividerColor, modifier = Modifier.padding(vertical = 8.dp))

                        // Price & Discounts
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = formatInr(product.price),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            if (discountPercent > 0) {
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = formatInr(product.originalPrice),
                                    fontSize = 14.sp,
                                    color = TextMuted,
                                    textDecoration = TextDecoration.LineThrough
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "$discountPercent% off",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreen
                                )
                            }
                        }

                        // Inclusive of all taxes badge
                        Text(
                            text = "Inclusive of all GST taxes • Free Shipping on ₹499+",
                            fontSize = 11.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        // Stock Availability
                        Spacer(modifier = Modifier.height(10.dp))
                        if (product.stockQuantity > 5) {
                            Surface(color = MintLight, shape = RoundedCornerShape(4.dp)) {
                                Text(
                                    text = "✓ In Stock (${product.stockQuantity} available)",
                                    color = EmeraldGreen,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        } else if (product.stockQuantity in 1..5) {
                            Surface(color = RoseLight, shape = RoundedCornerShape(4.dp)) {
                                Text(
                                    text = "Hurry, Only ${product.stockQuantity} left in stock!",
                                    color = CrimsonRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        } else {
                            Surface(color = RoseLight, shape = RoundedCornerShape(4.dp)) {
                                Text(
                                    text = "Currently Out of Stock",
                                    color = CrimsonRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Variant Selectors (Size, Color, etc.)
            if (parsedVariants.isNotEmpty()) {
                item {
                    Surface(
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Select Options", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                            Spacer(modifier = Modifier.height(8.dp))

                            parsedVariants.forEach { (groupName, options) ->
                                Text("$groupName:", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(vertical = 6.dp)
                                ) {
                                    options.forEach { option ->
                                        val isSelected = selectedVariantsState[groupName] == option
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                if (isSelected) RoyalBlue else CardBorder
                                            ),
                                            color = if (isSelected) SkyBlueLight else Color.White,
                                            modifier = Modifier.clickable {
                                                selectedVariantsState[groupName] = option
                                            }
                                        ) {
                                            Text(
                                                text = option,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) RoyalBlue else TextPrimary,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }
                    }
                }
            }

            // 4. Delivery PIN Code Check (India Specific)
            item {
                Surface(
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Outlined.LocationOn, contentDescription = "Pin", tint = RoyalBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Check Delivery & Services", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = pinInput,
                                onValueChange = { if (it.length <= 6 && it.all { ch -> ch.isDigit() }) pinInput = it },
                                placeholder = { Text("Enter 6-digit PIN code", fontSize = 12.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .testTag("pincode_check_input")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (pinInput.length == 6) {
                                        onPinCodeCheck(pinInput)
                                        pinCheckedMessage = "Express delivery available to $pinInput by BlueDart/Delhivery within 2-3 business days. Cash on Delivery is available."
                                    } else {
                                        pinCheckedMessage = "Please enter a valid 6-digit Indian PIN code."
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                                modifier = Modifier
                                    .height(50.dp)
                                    .testTag("pincode_check_btn")
                            ) {
                                Text("Check", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (pinCheckedMessage != null) {
                            Text(
                                text = pinCheckedMessage!!,
                                fontSize = 12.sp,
                                color = if (pinInput.length == 6) EmeraldGreen else CrimsonRed,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        // Delivery features bullets
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Tick", tint = EmeraldGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("7 Days Replacement or Return Policy", fontSize = 12.sp, color = TextSecondary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Tick", tint = EmeraldGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cash on Delivery (COD) eligible across India", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
            }

            // 5. Product Description
            item {
                Surface(
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Product Description", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = product.description,
                            fontSize = 13.sp,
                            color = TextSecondary,
                            lineHeight = 19.sp
                        )
                    }
                }
            }

            // 6. Product Specifications Table
            if (parsedSpecs.isNotEmpty()) {
                item {
                    Surface(
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Specifications", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                            Spacer(modifier = Modifier.height(8.dp))

                            parsedSpecs.forEachIndexed { idx, (key, value) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (idx % 2 == 0) OffWhite else Color.White)
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = key,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextSecondary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = value,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary,
                                        modifier = Modifier.weight(1.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 7. Customer Reviews Section
            item {
                Surface(
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Ratings & Reviews", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                            OutlinedButton(
                                onClick = { showReviewDialog = true },
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("rate_product_btn")
                            ) {
                                Text("Rate Product", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (reviews.isEmpty()) {
                            Text(
                                text = "No customer reviews yet. Be the first verified buyer to review this product!",
                                fontSize = 12.sp,
                                color = TextMuted,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            reviews.forEach { rev ->
                                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = EmeraldGreen,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                            ) {
                                                Text("${rev.rating}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                Icon(imageVector = Icons.Filled.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(rev.userName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        if (rev.isVerified) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(color = MintLight, shape = RoundedCornerShape(4.dp)) {
                                                Text("Verified Buyer", color = EmeraldGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                            }
                                        }
                                    }
                                    Text(
                                        text = rev.comment,
                                        fontSize = 12.sp,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                    Divider(color = DividerColor, modifier = Modifier.padding(top = 8.dp))
                                }
                            }
                        }
                    }
                }
            }

            // 8. Related Products
            if (relatedProducts.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp)
                    ) {
                        Text(
                            text = "Similar Products You May Like",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyDark,
                            modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 8.dp)
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(relatedProducts) { rel ->
                                Card(
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    modifier = Modifier
                                        .width(150.dp)
                                        .clickable { onRelatedProductClick(rel) }
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(rel.imagesJson.split(",").firstOrNull())
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = rel.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(100.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                        )
                                        Text(
                                            text = rel.name,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                        Text(
                                            text = formatInr(rel.price),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
