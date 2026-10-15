package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.BannerEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.ProductEntity
import com.example.ui.components.ProductCard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    banners: List<BannerEntity>,
    categories: List<CategoryEntity>,
    featuredProducts: List<ProductEntity>,
    allProducts: List<ProductEntity>,
    wishlistProductIds: Set<String>,
    onCategoryClick: (String) -> Unit,
    onProductClick: (ProductEntity) -> Unit,
    onWishlistToggle: (String) -> Unit,
    onAddToCart: (ProductEntity) -> Unit,
    onBuyNow: (ProductEntity) -> Unit,
    onViewAllClick: () -> Unit,
    onOpenPolicy: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val bestSellers = remember(allProducts) {
        allProducts.sortedByDescending { it.rating * (it.reviewCount + 1) }.take(4)
    }
    val newArrivals = remember(allProducts) {
        allProducts.takeLast(4).reversed()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(OffWhite)
            .testTag("home_screen_scroll")
    ) {
        // 1. Promotional Banner Carousel
        item {
            val pagerState = rememberPagerState(pageCount = { if (banners.isNotEmpty()) banners.size else 1 })

            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(horizontal = 12.dp)
                ) { page ->
                    val banner = banners.getOrNull(page)
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                if (banner != null && banner.categoryTarget.isNotEmpty()) {
                                    onCategoryClick(banner.categoryTarget)
                                }
                            }
                            .testTag("promo_banner_$page")
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            // Check if local drawable exists
                            val drawableRes = when (banner?.drawableResName) {
                                "banner_electronics" -> R.drawable.banner_electronics
                                "banner_fashion" -> R.drawable.banner_fashion
                                else -> null
                            }

                            if (drawableRes != null) {
                                Image(
                                    painter = painterResource(id = drawableRes),
                                    contentDescription = banner?.title ?: "Sale Banner",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(banner?.imageUrl?.ifEmpty { "https://images.unsplash.com/photo-1607082348824-0a96f2a4b9da?w=800" })
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = banner?.title ?: "Sale Banner",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }

                // Pager Indicators
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(if (banners.isNotEmpty()) banners.size else 1) { iteration ->
                        val color = if (pagerState.currentPage == iteration) RoyalBlue else CardBorder
                        val width = if (pagerState.currentPage == iteration) 20.dp else 6.dp
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .clip(CircleShape)
                                .background(color)
                                .height(6.dp)
                                .width(width)
                        )
                    }
                }
            }
        }

        // 2. India Shopping Trust Badge Strip
        item {
            Surface(
                color = SkyBlueLight,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = "Free Shipping",
                            tint = RoyalBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "FREE Delivery on orders ₹499+",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyDark
                            )
                            Text(
                                text = "All Indian PIN Codes Covered",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PublishedWithChanges,
                            contentDescription = "Easy Returns",
                            tint = EmeraldGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "7 Days Easy Returns",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyDark
                            )
                            Text(
                                text = "100% Original Products",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // 3. Category Horizontal Strip
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Shop by Category",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyDark
                    )
                    TextButton(onClick = onViewAllClick) {
                        Text("View All", fontSize = 12.sp, color = RoyalBlue, fontWeight = FontWeight.SemiBold)
                    }
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(categories) { cat ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(74.dp)
                                .clickable { onCategoryClick(cat.id) }
                                .testTag("cat_chip_${cat.id}")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(1.5.dp, SkyBlueLight, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(cat.imageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = cat.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = cat.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                color = TextPrimary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // 4. Featured Products Grid / Section
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Featured Products",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyDark
                        )
                        Text(
                            text = "Handpicked best deals for you",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    TextButton(onClick = onViewAllClick) {
                        Text("See All", fontSize = 12.sp, color = RoyalBlue, fontWeight = FontWeight.SemiBold)
                    }
                }

                // 2-column layout for featured products
                val chunkedFeatured = featuredProducts.take(4).chunked(2)
                chunkedFeatured.forEach { rowProducts ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (prod in rowProducts) {
                            Box(modifier = Modifier.weight(1f)) {
                                ProductCard(
                                    product = prod,
                                    isInWishlist = wishlistProductIds.contains(prod.id),
                                    onProductClick = { onProductClick(prod) },
                                    onWishlistToggle = { onWishlistToggle(prod.id) },
                                    onAddToCart = { onAddToCart(prod) },
                                    onBuyNow = { onBuyNow(prod) }
                                )
                            }
                        }
                        if (rowProducts.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // 5. Best Sellers Section
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Best Sellers in India",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyDark
                        )
                        Text(
                            text = "Most loved by our customers",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    TextButton(onClick = onViewAllClick) {
                        Text("Explore", fontSize = 12.sp, color = RoyalBlue, fontWeight = FontWeight.SemiBold)
                    }
                }

                val chunkedBestSellers = bestSellers.chunked(2)
                chunkedBestSellers.forEach { rowProducts ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (prod in rowProducts) {
                            Box(modifier = Modifier.weight(1f)) {
                                ProductCard(
                                    product = prod,
                                    isInWishlist = wishlistProductIds.contains(prod.id),
                                    onProductClick = { onProductClick(prod) },
                                    onWishlistToggle = { onWishlistToggle(prod.id) },
                                    onAddToCart = { onAddToCart(prod) },
                                    onBuyNow = { onBuyNow(prod) }
                                )
                            }
                        }
                        if (rowProducts.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // 6. New Arrivals
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "New Arrivals",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyDark
                        )
                        Text(
                            text = "Fresh stock added this week",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    TextButton(onClick = onViewAllClick) {
                        Text("View All", fontSize = 12.sp, color = RoyalBlue, fontWeight = FontWeight.SemiBold)
                    }
                }

                val chunkedNew = newArrivals.chunked(2)
                chunkedNew.forEach { rowProducts ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (prod in rowProducts) {
                            Box(modifier = Modifier.weight(1f)) {
                                ProductCard(
                                    product = prod,
                                    isInWishlist = wishlistProductIds.contains(prod.id),
                                    onProductClick = { onProductClick(prod) },
                                    onWishlistToggle = { onWishlistToggle(prod.id) },
                                    onAddToCart = { onAddToCart(prod) },
                                    onBuyNow = { onBuyNow(prod) }
                                )
                            }
                        }
                        if (rowProducts.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // 7. Comprehensive Store Footer (Policies, About Us, Contact, T&C)
        item {
            Surface(
                color = NavyDark,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Nova",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Market",
                            color = AmberGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                    Text(
                        text = "Everything You Need, One Destination.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(bottom = 12.dp))

                    Text(
                        text = "CUSTOMER SERVICE & POLICIES",
                        color = SkyBlueLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val policyLinks = listOf(
                        "About Us" to "ABOUT",
                        "Contact Us & Support" to "CONTACT",
                        "FAQs & Help Center" to "FAQS",
                        "Shipping & Delivery Policy" to "SHIPPING",
                        "7-Day Return & Refund Policy" to "RETURNS",
                        "Privacy Policy" to "PRIVACY",
                        "Terms & Conditions" to "TERMS"
                    )

                    policyLinks.forEach { (title, key) ->
                        Text(
                            text = "• $title",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenPolicy(key) }
                                .padding(vertical = 4.dp)
                                .testTag("footer_link_$key")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "© 2026 NovaMarket Retail India Ltd. All rights reserved.",
                        color = TextMuted,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Made with ❤️ for Indian Shoppers",
                        color = AmberGold,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 8.dp)
                    )
                }
            }
        }
    }
}
