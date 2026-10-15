package com.example.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

enum class BottomNavTab(val title: String) {
    HOME("Home"),
    CATEGORIES("Categories"),
    WISHLIST("Wishlist"),
    CART("Cart"),
    ACCOUNT("Account")
}

@Composable
fun NovaBottomBar(
    currentTab: BottomNavTab,
    cartItemCount: Int,
    wishlistItemCount: Int,
    onTabSelected: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        modifier = modifier.testTag("nova_bottom_navigation")
    ) {
        // Tab 1: Home
        NavigationBarItem(
            selected = currentTab == BottomNavTab.HOME,
            onClick = { onTabSelected(BottomNavTab.HOME) },
            icon = {
                Icon(
                    imageVector = if (currentTab == BottomNavTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Home"
                )
            },
            label = {
                Text(
                    text = "Home",
                    fontSize = 11.sp,
                    fontWeight = if (currentTab == BottomNavTab.HOME) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = RoyalBlue,
                selectedTextColor = RoyalBlue,
                indicatorColor = SkyBlueLight,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            ),
            modifier = Modifier.testTag("nav_tab_home")
        )

        // Tab 2: Categories
        NavigationBarItem(
            selected = currentTab == BottomNavTab.CATEGORIES,
            onClick = { onTabSelected(BottomNavTab.CATEGORIES) },
            icon = {
                Icon(
                    imageVector = if (currentTab == BottomNavTab.CATEGORIES) Icons.Filled.Category else Icons.Outlined.Category,
                    contentDescription = "Categories"
                )
            },
            label = {
                Text(
                    text = "Categories",
                    fontSize = 11.sp,
                    fontWeight = if (currentTab == BottomNavTab.CATEGORIES) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = RoyalBlue,
                selectedTextColor = RoyalBlue,
                indicatorColor = SkyBlueLight,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            ),
            modifier = Modifier.testTag("nav_tab_categories")
        )

        // Tab 3: Wishlist
        NavigationBarItem(
            selected = currentTab == BottomNavTab.WISHLIST,
            onClick = { onTabSelected(BottomNavTab.WISHLIST) },
            icon = {
                BadgedBox(
                    badge = {
                        if (wishlistItemCount > 0) {
                            Badge(
                                containerColor = CrimsonRed,
                                contentColor = Color.White
                            ) {
                                Text("$wishlistItemCount", fontSize = 9.sp)
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (currentTab == BottomNavTab.WISHLIST) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Wishlist"
                    )
                }
            },
            label = {
                Text(
                    text = "Wishlist",
                    fontSize = 11.sp,
                    fontWeight = if (currentTab == BottomNavTab.WISHLIST) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = RoyalBlue,
                selectedTextColor = RoyalBlue,
                indicatorColor = SkyBlueLight,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            ),
            modifier = Modifier.testTag("nav_tab_wishlist")
        )

        // Tab 4: Cart
        NavigationBarItem(
            selected = currentTab == BottomNavTab.CART,
            onClick = { onTabSelected(BottomNavTab.CART) },
            icon = {
                BadgedBox(
                    badge = {
                        if (cartItemCount > 0) {
                            Badge(
                                containerColor = AmberGold,
                                contentColor = Color.Black
                            ) {
                                Text("$cartItemCount", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (currentTab == BottomNavTab.CART) Icons.Filled.ShoppingCart else Icons.Outlined.ShoppingCart,
                        contentDescription = "Cart"
                    )
                }
            },
            label = {
                Text(
                    text = "Cart",
                    fontSize = 11.sp,
                    fontWeight = if (currentTab == BottomNavTab.CART) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = RoyalBlue,
                selectedTextColor = RoyalBlue,
                indicatorColor = SkyBlueLight,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            ),
            modifier = Modifier.testTag("nav_tab_cart")
        )

        // Tab 5: Account
        NavigationBarItem(
            selected = currentTab == BottomNavTab.ACCOUNT,
            onClick = { onTabSelected(BottomNavTab.ACCOUNT) },
            icon = {
                Icon(
                    imageVector = if (currentTab == BottomNavTab.ACCOUNT) Icons.Filled.Person else Icons.Outlined.Person,
                    contentDescription = "Account"
                )
            },
            label = {
                Text(
                    text = "Account",
                    fontSize = 11.sp,
                    fontWeight = if (currentTab == BottomNavTab.ACCOUNT) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = RoyalBlue,
                selectedTextColor = RoyalBlue,
                indicatorColor = SkyBlueLight,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            ),
            modifier = Modifier.testTag("nav_tab_account")
        )
    }
}
