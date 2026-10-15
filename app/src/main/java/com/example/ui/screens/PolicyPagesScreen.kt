package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PolicyPagesScreen(
    policyType: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (title, content) = when (policyType) {
        "ABOUT" -> "About NovaMarket" to """
            Welcome to NovaMarket – India's Own Online Shopping Destination!
            
            NovaMarket is an independent, direct-to-customer retail enterprise dedicated to providing authentic, premium quality products across India. From high-performance laptops and smartphones to heritage khadi kurtas, Banarasi silk sarees, Turkish prayer mats, and Ayurvedic wellness essentials, NovaMarket brings you genuine products directly from trusted domestic and international suppliers.
            
            Our Promise:
            • 100% Genuine & Authentic Products
            • Direct sourcing with zero third-party affiliate markups
            • Transparent Indian Rupee (₹) pricing with GST compliance
            • Fast delivery to all 19,000+ PIN codes across India
            • Dedicated 24/7 customer care based in Gurugram, India
        """.trimIndent()

        "CONTACT" -> "Contact Us & Support" to """
            We are always here to assist our valued customers across India.
            
            Customer Helpline:
            • Toll-Free Support: +91 1800 200 6682 (Mon-Sat, 9:00 AM – 8:00 PM IST)
            • Email Support: support@novamarket.in
            • Order Inquiries: orders@novamarket.in
            
            Registered Corporate Office:
            NovaMarket Retail India Ltd.
            Plot 88, Udyog Vihar Phase 4,
            Gurugram, Haryana 122016, India
            
            Grievance Officer:
            Mr. Rajesh Verma
            Email: grievance@novamarket.in
        """.trimIndent()

        "FAQS" -> "Frequently Asked Questions" to """
            1. How do I track my NovaMarket order?
            Go to 'My Orders' from the Account menu and tap on your order. You can view the live step-by-step dispatch status along with the courier name (Delhivery, BlueDart) and Air Waybill (AWB) tracking number.
            
            2. What payment methods are accepted?
            We support all major Indian payment options:
            • UPI (Google Pay, PhonePe, Paytm, BHIM)
            • Cash on Delivery (COD)
            • Credit and Debit Cards (Visa, MasterCard, RuPay)
            • Net Banking across 50+ Indian banks
            
            3. Is delivery free?
            Yes! All orders above ₹499 qualify for FREE standard delivery across India. Orders under ₹499 incur a nominal fee of ₹40.
            
            4. How do I download my GST Tax Invoice?
            Open your order in 'My Orders' and tap the 'Tax Invoice' button to view and download your official GST invoice with itemized tax breakdown.
        """.trimIndent()

        "SHIPPING" -> "Shipping & Delivery Policy" to """
            NovaMarket ships to all reachable PIN codes across India in partnership with top logistics carriers including Delhivery, BlueDart, and Ekart Express.
            
            Delivery Timelines:
            • Metro Cities (Delhi-NCR, Mumbai, Bengaluru, Hyderabad, Chennai, Kolkata): 2–3 business days.
            • Tier 2 & Tier 3 Cities: 3–5 business days.
            • Remote & North-East Areas: 5–7 business days.
            
            Dispatch Workflow:
            All orders placed before 2:00 PM IST are processed and dispatched on the same business day from our automated fulfillment centers.
        """.trimIndent()

        "RETURNS" -> "7-Day Return & Refund Policy" to """
            At NovaMarket, customer satisfaction is our top priority. We offer a hassle-free 7-Day Replacement & Return Policy on all eligible items.
            
            Eligibility Criteria:
            • Items must be unused, in original condition with tags and packaging intact.
            • Electronics must include all original cords, accessories, and warranty cards.
            
            Refund Process:
            • For prepaid orders (UPI/Card/NetBanking), refunds are credited to the source payment method within 3–5 business days of item pickup.
            • For Cash on Delivery (COD) orders, refunds are credited directly to your bank account or UPI ID.
        """.trimIndent()

        else -> "Privacy Policy & Terms" to """
            NovaMarket is committed to safeguarding your personal data and financial privacy in full compliance with the Information Technology Act, 2000 and Digital Personal Data Protection Act of India.
            
            Data Security:
            • We never store raw card numbers, CVVs, or UPI PINs.
            • All payments are processed through RBI-approved, PCI-DSS compliant payment gateways.
            • Your phone number and address are used exclusively for delivery fulfillment and order tracking notifications.
        """.trimIndent()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(OffWhite)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .testTag("policy_content_scroll")
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyDark
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = content,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}
