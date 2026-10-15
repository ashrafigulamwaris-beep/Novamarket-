package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
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
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedAddressesScreen(
    addresses: List<AddressEntity>,
    onBackClick: () -> Unit,
    onSaveAddress: (AddressEntity) -> Unit,
    onDeleteAddress: (AddressEntity) -> Unit,
    onSetDefault: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
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
        var type by remember { mutableStateOf("HOME") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Delivery Address (India)", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name *") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp))
                    OutlinedTextField(value = phone, onValueChange = { if (it.length <= 10 && it.all { c -> c.isDigit() }) phone = it }, label = { Text("10-Digit Mobile Number *") }, prefix = { Text("+91 ") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = pin,
                            onValueChange = {
                                pin = it
                                if (it == "122001") { city = "Gurugram"; district = "Gurugram"; state = "Haryana" }
                                else if (it == "110001") { city = "New Delhi"; district = "Central Delhi"; state = "Delhi" }
                                else if (it == "400001") { city = "Mumbai"; district = "Mumbai City"; state = "Maharashtra" }
                            },
                            label = { Text("PIN Code *") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City *") }, modifier = Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 6.dp)) {
                        OutlinedTextField(value = district, onValueChange = { district = it }, label = { Text("District") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = state, onValueChange = { state = it }, label = { Text("State *") }, modifier = Modifier.weight(1f))
                    }
                    OutlinedTextField(value = house, onValueChange = { house = it }, label = { Text("Flat / House No. *") }, modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp))
                    OutlinedTextField(value = street, onValueChange = { street = it }, label = { Text("Street / Road *") }, modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp))
                    OutlinedTextField(value = villageLocality, onValueChange = { villageLocality = it }, label = { Text("Village / Locality / Sector") }, modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp))
                    OutlinedTextField(value = landmark, onValueChange = { landmark = it }, label = { Text("Landmark (Optional)") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank() && phone.isNotBlank() && pin.isNotBlank() && city.isNotBlank()) {
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
                                addressType = type,
                                isDefault = addresses.isEmpty()
                            )
                            onSaveAddress(newAddr)
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text("Save Address")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved Delivery Addresses", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = RoyalBlue,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_address_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Address")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(OffWhite)
                .testTag("saved_addresses_scroll")
        ) {
            items(addresses) { addr ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(addr.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(color = SkyBlueLight, shape = RoundedCornerShape(4.dp)) {
                                    Text(addr.addressType, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = RoyalBlue, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            if (addr.isDefault) {
                                Surface(color = MintLight, shape = RoundedCornerShape(4.dp)) {
                                    Text("DEFAULT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                        }

                        Text("${addr.houseFlat}, ${addr.streetArea}", fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(top = 4.dp))
                        Text("${addr.city}, ${addr.state} - ${addr.pinCode}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text("Mobile: +91 ${addr.phone}", fontSize = 11.sp, color = TextMuted)

                        Divider(color = DividerColor, modifier = Modifier.padding(vertical = 8.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (!addr.isDefault) {
                                TextButton(onClick = { onSetDefault(addr.id) }) {
                                    Text("Set as Default", fontSize = 12.sp, color = RoyalBlue)
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Default Address", fontSize = 11.sp, color = EmeraldGreen, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            IconButton(onClick = { onDeleteAddress(addr) }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = CrimsonRed)
                            }
                        }
                    }
                }
            }
        }
    }
}
