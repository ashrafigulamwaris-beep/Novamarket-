package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.model.CategoryEntity
import com.example.data.model.ProductEntity
import com.example.ui.components.ProductCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.FilterState
import com.example.ui.viewmodel.SortOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    products: List<ProductEntity>,
    categories: List<CategoryEntity>,
    filterState: FilterState,
    wishlistProductIds: Set<String>,
    onCategorySelect: (String?) -> Unit,
    onSearchChange: (String) -> Unit,
    onSortChange: (SortOption) -> Unit,
    onFilterChange: (minPrice: Double, maxPrice: Double, inStockOnly: Boolean, minRating: Float) -> Unit,
    onClearFilters: () -> Unit,
    onProductClick: (ProductEntity) -> Unit,
    onWishlistToggle: (String) -> Unit,
    onAddToCart: (ProductEntity) -> Unit,
    onBuyNow: (ProductEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showFilterSheet by remember { mutableStateOf(false) }

    // BottomSheet for filters
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false }
        ) {
            var tempMinPrice by remember { mutableStateOf(filterState.minPrice.toFloat()) }
            var tempMaxPrice by remember { mutableStateOf(filterState.maxPrice.coerceAtMost(60000.0).toFloat()) }
            var tempInStock by remember { mutableStateOf(filterState.inStockOnly) }
            var tempRating by remember { mutableStateOf(filterState.minRating) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .testTag("filter_bottom_sheet")
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Filter Products", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                    TextButton(onClick = {
                        tempMinPrice = 0f
                        tempMaxPrice = 60000f
                        tempInStock = false
                        tempRating = 0f
                        onClearFilters()
                        showFilterSheet = false
                    }) {
                        Text("Reset All", color = CrimsonRed, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Price Range
                Text(
                    text = "Price Range: ₹${tempMinPrice.toInt()} - ₹${tempMaxPrice.toInt()}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                RangeSlider(
                    value = tempMinPrice..tempMaxPrice,
                    onValueChange = { range ->
                        tempMinPrice = range.start
                        tempMaxPrice = range.endInclusive
                    },
                    valueRange = 0f..60000f,
                    colors = SliderDefaults.colors(thumbColor = RoyalBlue, activeTrackColor = RoyalBlue)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // In stock toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("In Stock Only", fontSize = 14.sp)
                    Switch(
                        checked = tempInStock,
                        onCheckedChange = { tempInStock = it }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Rating Filter
                Text("Minimum Customer Rating", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    listOf(0f to "Any", 3f to "3★ & above", 4f to "4★ & above", 4.5f to "4.5★+").forEach { (rating, label) ->
                        FilterChip(
                            selected = tempRating == rating,
                            onClick = { tempRating = rating },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SkyBlueLight,
                                selectedLabelColor = RoyalBlue
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onFilterChange(tempMinPrice.toDouble(), tempMaxPrice.toDouble(), tempInStock, tempRating)
                        showFilterSheet = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("apply_filter_btn")
                ) {
                    Text("Apply Filters", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OffWhite)
            .testTag("product_list_screen")
    ) {
        // Search Input Header
        Surface(color = NavyDark, modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = filterState.searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Search products, brands, models...", fontSize = 13.sp, color = TextMuted) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = RoyalBlue)
                    },
                    trailingIcon = {
                        if (filterState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = RoyalBlue,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("search_input_field")
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { showFilterSheet = true },
                    modifier = Modifier
                        .size(46.dp)
                        .background(RoyalBlue, RoundedCornerShape(10.dp))
                        .testTag("open_filters_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Filters",
                        tint = Color.White
                    )
                }
            }
        }

        // Category Filter Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = filterState.selectedCategoryId == null,
                    onClick = { onCategorySelect(null) },
                    label = { Text("All Products", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = RoyalBlue,
                        selectedLabelColor = Color.White
                    )
                )
            }
            items(categories) { cat ->
                FilterChip(
                    selected = filterState.selectedCategoryId == cat.id,
                    onClick = { onCategorySelect(cat.id) },
                    label = { Text(cat.name, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = RoyalBlue,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Sort Strip + Result Count
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${products.size} Products found",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )

            // Sort Menu Trigger
            var sortMenuExpanded by remember { mutableStateOf(false) }
            Box {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.clickable { sortMenuExpanded = true }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = filterState.sortOption.title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Sort",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = sortMenuExpanded,
                    onDismissRequest = { sortMenuExpanded = false }
                ) {
                    SortOption.values().forEach { sort ->
                        DropdownMenuItem(
                            text = { Text(sort.title, fontSize = 12.sp) },
                            onClick = {
                                onSortChange(sort)
                                sortMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Product Grid or Empty State
        if (products.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.SearchOff,
                        contentDescription = "No products",
                        tint = TextMuted,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No products found",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyDark
                    )
                    Text(
                        text = "Try adjusting your search terms or filters to find what you are looking for.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )
                    Button(
                        onClick = onClearFilters,
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                    ) {
                        Text("Reset Filters")
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(products) { prod ->
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
        }
    }
}
