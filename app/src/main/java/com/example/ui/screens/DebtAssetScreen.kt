package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.data.model.DebtReceivableEntity
import com.example.ui.FinancialUiState
import com.example.ui.components.AddDebtDialog
import com.example.ui.components.SettleDebtDialog
import com.example.ui.theme.BankingBluePrimary
import com.example.ui.theme.BlueReceivable
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.util.CurrencyUtils
import com.example.util.DateUtils

@Composable
fun DebtAssetScreen(
    uiState: FinancialUiState,
    onAddDebt: (
        personName: String,
        type: String,
        amount: Long,
        dueDate: Long?,
        notes: String
    ) -> Unit,
    onSettleDebt: (debtId: Long, paymentAmount: Long, note: String) -> Unit,
    onDeleteDebt: (debt: DebtReceivableEntity) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var activeSettleItem by remember { mutableStateOf<DebtReceivableEntity?>(null) }
    var selectedTab by remember { mutableStateOf(0) } // 0 = Piutang (Aset), 1 = Hutang (Kewajiban), 2 = Lunas

    val piutangList = uiState.debtsAndReceivables.filter { it.type == Constants.DEBT_PIUTANG && !it.isFullySettled }
    val hutangList = uiState.debtsAndReceivables.filter { it.type == Constants.DEBT_HUTANG && !it.isFullySettled }
    val settledList = uiState.debtsAndReceivables.filter { it.isFullySettled }

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
                    Column {
                        Text(
                            text = "ASET PIUTANG & HUTANG",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Aset terpisah dari kas, bertambah saat dilunasi",
                            fontSize = 11.sp,
                            color = Color(0xFFDBEAFE)
                        )
                    }

                    Surface(
                        onClick = { showAddDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.testTag("btn_add_debt_top")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Catat Baru", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("debt_screen_content"),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {

            // Summary Dashboard Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Total Aset Piutang (Ada di Orang):",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = CurrencyUtils.formatRupiah(uiState.totalPiutang),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = BlueReceivable
                                )
                                Text(
                                    text = "Belum masuk kas aktif sampai dibayar",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }

                            Divider(
                                modifier = Modifier
                                    .height(40.dp)
                                    .width(1.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )

                            Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
                                Text(
                                    text = "Tanggungan Hutang:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = CurrencyUtils.formatRupiah(uiState.totalHutang),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = ExpenseRed
                                )
                                Text(
                                    text = "Kewajiban bayar ke orang lain",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Balance Display note
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Saldo Kas: ${CurrencyUtils.formatRupiah(uiState.cashBalance)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Kekayaan: ${CurrencyUtils.formatRupiah(uiState.netWorth)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // Tabs
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        label = { Text("Piutang (${piutangList.size})") },
                        modifier = Modifier.testTag("tab_debt_piutang")
                    )
                    FilterChip(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        label = { Text("Hutang (${hutangList.size})") },
                        modifier = Modifier.testTag("tab_debt_hutang")
                    )
                    FilterChip(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        label = { Text("Lunas (${settledList.size})") },
                        modifier = Modifier.testTag("tab_debt_settled")
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    if (piutangList.isEmpty()) {
                        item {
                            EmptyDebtCard(
                                message = "Tidak ada catatan piutang aktif saat ini.",
                                buttonLabel = "Catat Piutang Orang Lain",
                                onAdd = { showAddDialog = true }
                            )
                        }
                    } else {
                        items(piutangList, key = { "debt_piut_${it.id}" }) { item ->
                            DebtCardRow(
                                item = item,
                                onSettle = { activeSettleItem = item },
                                onDelete = { onDeleteDebt(item) }
                            )
                        }
                    }
                }
                1 -> {
                    if (hutangList.isEmpty()) {
                        item {
                            EmptyDebtCard(
                                message = "Bagus! Anda tidak memiliki tanggungan hutang ke orang lain.",
                                buttonLabel = "Catat Hutang Baru",
                                onAdd = { showAddDialog = true }
                            )
                        }
                    } else {
                        items(hutangList, key = { "debt_hut_${it.id}" }) { item ->
                            DebtCardRow(
                                item = item,
                                onSettle = { activeSettleItem = item },
                                onDelete = { onDeleteDebt(item) }
                            )
                        }
                    }
                }
                2 -> {
                    if (settledList.isEmpty()) {
                        item {
                            Text(
                                text = "Belum ada riwayat hutang/piutang yang telah lunas.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                            )
                        }
                    } else {
                        items(settledList, key = { "debt_settled_${it.id}" }) { item ->
                            SettledDebtCardRow(
                                item = item,
                                onDelete = { onDeleteDebt(item) }
                            )
                        }
                    }
                }
            }
        }
    }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = BlueReceivable,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 20.dp)
                .testTag("fab_add_debt")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Piutang/Hutang")
        }
    }

    if (showAddDialog) {
        AddDebtDialog(
            onConfirm = { personName, type, amount, dueDate, notes ->
                onAddDebt(personName, type, amount, dueDate, notes)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    if (activeSettleItem != null) {
        SettleDebtDialog(
            item = activeSettleItem!!,
            onConfirm = { debtId, paymentAmount, note ->
                onSettleDebt(debtId, paymentAmount, note)
                activeSettleItem = null
            },
            onDismiss = { activeSettleItem = null }
        )
    }
}

@Composable
private fun DebtCardRow(
    item: DebtReceivableEntity,
    onSettle: () -> Unit,
    onDelete: () -> Unit
) {
    val isPiutang = item.type == Constants.DEBT_PIUTANG
    val accentColor = if (isPiutang) BlueReceivable else ExpenseRed

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp)
            .testTag("debt_card_${item.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = accentColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Handshake,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = item.personName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isPiutang) "Punya hutang ke saya (Aset)" else "Hutang saya ke beliau",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus",
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Sisa Belum Dibayar:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyUtils.formatRupiah(item.remainingAmount),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentColor
                    )
                    if (item.remainingAmount != item.originalAmount) {
                        Text(
                            text = "Dari total pinjaman awal: ${CurrencyUtils.formatRupiah(item.originalAmount)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Button(
                    onClick = onSettle,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPiutang) BankingBluePrimary else ExpenseRed
                    ),
                    modifier = Modifier
                        .align(Alignment.Bottom)
                        .testTag("btn_settle_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.PriceCheck,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isPiutang) "Terima Bayar" else "Bayar Hutang", fontSize = 12.sp)
                }
            }

            if (item.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Keterangan: ${item.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SettledDebtCardRow(
    item: DebtReceivableEntity,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = IncomeGreen,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${item.personName} (${if (item.type == Constants.DEBT_PIUTANG) "Piutang Lunas" else "Hutang Lunas"})",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Total ${CurrencyUtils.formatRupiah(item.originalAmount)} telah diselesaikan",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Hapus",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyDebtCard(
    message: String,
    buttonLabel: String,
    onAdd: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Handshake,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onAdd,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(buttonLabel)
            }
        }
    }
}
