package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Constants
import com.example.data.model.TransactionItemEntity
import com.example.ui.FinancialUiState
import com.example.ui.components.AdjustBalanceDialog
import com.example.ui.theme.BankingBluePrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.util.CurrencyUtils
import com.example.util.DateUtils

@Composable
fun HistoryScreen(
    uiState: FinancialUiState,
    onAdjustBalance: ((newBalance: Long, reason: String) -> Unit)? = null
) {
    var selectedFilter by remember { mutableStateOf("ALL") }
    var showAdjustDialog by remember { mutableStateOf(false) }

    val filteredList = uiState.allTransactions.filter { tx ->
        when (selectedFilter) {
            "INCOME" -> tx.type != Constants.TX_MANUAL_ADJUSTMENT && tx.amount >= 0
            "EXPENSE" -> tx.type != Constants.TX_MANUAL_ADJUSTMENT && tx.amount < 0
            "ADJUSTMENT" -> tx.type == Constants.TX_MANUAL_ADJUSTMENT ||
                    tx.title.contains("Penyesuaian", ignoreCase = true) ||
                    tx.title.contains("Saldo Kas", ignoreCase = true) ||
                    tx.reasonOrNote.contains("penyesuaian", ignoreCase = true)
            else -> true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header tetap / tidak terscroll
            Surface(
                color = BankingBluePrimary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            text = "RIWAYAT & MUTASI KAS",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Transparansi mutasi saldo lengkap dengan alasan & catatan",
                            fontSize = 11.sp,
                            color = Color(0xFFDBEAFE)
                        )
                    }

                    if (onAdjustBalance != null) {
                        Surface(
                            onClick = { showAdjustDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.22f),
                            modifier = Modifier.testTag("btn_history_adjust_balance")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Atur Saldo",
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Atur Saldo",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Filters tetap di atas / tidak terscroll
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        label = { Text("Semua (${uiState.allTransactions.size})") },
                        modifier = Modifier.testTag("filter_all")
                    )
                    FilterChip(
                        selected = selectedFilter == "INCOME",
                        onClick = { selectedFilter = "INCOME" },
                        label = { Text("Pemasukan") },
                        modifier = Modifier.testTag("filter_income")
                    )
                    FilterChip(
                        selected = selectedFilter == "EXPENSE",
                        onClick = { selectedFilter = "EXPENSE" },
                        label = { Text("Pengeluaran") },
                        modifier = Modifier.testTag("filter_expense")
                    )
                    FilterChip(
                        selected = selectedFilter == "ADJUSTMENT",
                        onClick = { selectedFilter = "ADJUSTMENT" },
                        label = { Text("Penyesuaian") },
                        modifier = Modifier.testTag("filter_adjustment")
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("history_screen_content"),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {

            if (filteredList.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = if (selectedFilter == "ADJUSTMENT")
                                    "Belum ada mutasi penyesuaian saldo kas manual."
                                else
                                    "Belum ada riwayat transaksi pada kategori ini.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp)
                            )

                            if (selectedFilter == "ADJUSTMENT" && onAdjustBalance != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { showAdjustDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = BankingBluePrimary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Sesuaikan Saldo Sekarang")
                                }
                            }
                        }
                    }
                }
            } else {
                items(filteredList, key = { "hist_tx_${it.id}" }) { tx ->
                    HistoryItemCard(transaction = tx)
                }
            }
        }
    }

        if (showAdjustDialog && onAdjustBalance != null) {
            AdjustBalanceDialog(
                currentBalance = uiState.cashBalance,
                onConfirm = { newBal, reason ->
                    onAdjustBalance(newBal, reason)
                    showAdjustDialog = false
                },
                onDismiss = { showAdjustDialog = false }
            )
        }
    }
}

@Composable
private fun HistoryItemCard(transaction: TransactionItemEntity) {
    val isPositive = transaction.amount >= 0
    val iconColor = if (isPositive) IncomeGreen else ExpenseRed

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = iconColor.copy(alpha = 0.12f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isPositive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                if (transaction.reasonOrNote.isNotBlank()) {
                    Text(
                        text = "Alasan: ${transaction.reasonOrNote}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = DateUtils.formatDateTimeIndo(transaction.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Text(
                text = CurrencyUtils.formatRupiahWithSign(transaction.amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isPositive) IncomeGreen else ExpenseRed
            )
        }
    }
}
