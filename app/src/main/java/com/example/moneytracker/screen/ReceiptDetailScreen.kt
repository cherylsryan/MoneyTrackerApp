package com.example.moneytracker.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneytracker.model.Receipt
import com.example.moneytracker.model.ScanState
import com.example.moneytracker.util.formatCurrencyDisplay
import com.example.moneytracker.viewmodel.ReceiptScannerViewModel

private val CreamBackground = Color(0xFFF6EFC9)

@Composable
fun ReceiptDetailScreen(
    viewModel: ReceiptScannerViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val state = viewModel.state.value
    var receipt by remember { mutableStateOf<Receipt?>(null) }

    LaunchedEffect(state) {
        when (state) {
            is ScanState.Success -> {
                receipt = state.receipt
            }
            is ScanState.Saved -> {
                onSaved()
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = CreamBackground,
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { 
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Receipt Detail", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A237E))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                actions = { Spacer(Modifier.width(48.dp)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CreamBackground)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val currentReceipt = receipt
            
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (currentReceipt == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF1A237E))
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            SummaryRow("Store", currentReceipt.storeName)
                            SummaryRow("Date", currentReceipt.date)
                            SummaryRow("Total", "RM${formatCurrencyDisplay(currentReceipt.totalAmount)}")
                            
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF5F5F5))
                            
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("Item", modifier = Modifier.weight(1f), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                Text("Qty", modifier = Modifier.width(40.dp), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                Text("RM", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.End)
                            }
                            
                            currentReceipt.items.forEach { item ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.name, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                        if (item.description.isNotBlank()) {
                                            Text(item.description, fontSize = 12.sp, color = Color.Gray)
                                        }
                                    }
                                    Text("${item.qty}", modifier = Modifier.width(40.dp), fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                    Text(formatCurrencyDisplay(item.amount), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                            }
                        }
                    }
                }
            }

            if (state is ScanState.Error) {
                Text(text = state.message, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(bottom = 12.dp))
            }

            Button(
                onClick = { viewModel.saveReceipt() },
                enabled = currentReceipt != null &&
                        currentReceipt.totalAmount > 0.0 &&
                        state !is ScanState.Saving &&
                        state !is ScanState.Saved,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E), contentColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (currentReceipt != null && currentReceipt.totalAmount <= 0.0) {
                    Text(
                        text = "This receipt couldn't be read properly — no valid total detected.",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                if (state is ScanState.Saving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Confirm & Save to Expenses", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = FontWeight.Medium, color = Color.Gray, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.ExtraBold, color = Color.Black, fontSize = 14.sp)
    }
}
