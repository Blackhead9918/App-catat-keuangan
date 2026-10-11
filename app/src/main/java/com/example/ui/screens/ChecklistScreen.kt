package com.example.ui.screens

import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.FinancialScheduleEntity
import com.example.ui.FinancialUiState
import com.example.ui.components.AddScheduleDialog
import com.example.ui.theme.BankingBlueContainer
import com.example.ui.theme.BankingBluePrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.util.CurrencyUtils
import com.example.util.DateUtils

@Composable
fun ChecklistScreen(
    uiState: FinancialUiState,
    onPayExpense: (scheduleId: Long, periodsCount: Int) -> Unit,
    onAddExpense: (
        title: String,
        amount: Long,
        type: String,
        recurrence: String,
        intervalDays: Int,
        startDate: Long,
        notificationNote: String
    ) -> Unit,
    onDeleteExpense: (schedule: FinancialScheduleEntity) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var itemToConfirmPayment by remember { mutableStateOf<FinancialScheduleEntity?>(null) }
    var selectedTab by remember { mutableStateOf(0) } // 0 = Perlu Bayar Sekarang, 1 = Bulan Depan / Mendatang, 2 = Selesai

    val dueList = uiState.duePendingExpenses
    val upcomingList = uiState.upcomingExpenses
    val completedList = uiState.schedules.filter { it.type == Constants.TYPE_EXPENSE && it.isCompleted }
    val totalDue = dueList.sumOf { it.nominal }

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
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = "CEKLIS TAGIHAN",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Tagihan lunas otomatis hilang hingga siklus berikutnya",
                            fontSize = 11.sp,
                            color = Color(0xFFDBEAFE)
                        )
                    }

                    Surface(
                        onClick = { showAddDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.22f),
                        modifier = Modifier.testTag("btn_add_expense_checklist_top")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tambah", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("checklist_screen_content"),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {

            // Summary Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (dueList.isNotEmpty()) ExpenseRed.copy(alpha = 0.12f) else IncomeGreen.copy(alpha = 0.12f),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (dueList.isNotEmpty()) Icons.Default.Checklist else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (dueList.isNotEmpty()) ExpenseRed else IncomeGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Tagihan Perlu Dibayar Sekarang:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = CurrencyUtils.formatRupiah(totalDue),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (dueList.isNotEmpty()) ExpenseRed else IncomeGreen
                            )
                            Text(
                                text = if (dueList.isNotEmpty()) "${dueList.size} item jatuh tempo / mendekati tenggat"
                                else "Semua tagihan periode ini telah lunas!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
                        label = { Text("Perlu Bayar (${dueList.size})") },
                        modifier = Modifier.testTag("chip_tab_due")
                    )
                    FilterChip(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        label = { Text("Bulan Depan (${upcomingList.size})") },
                        modifier = Modifier.testTag("chip_tab_upcoming")
                    )
                    FilterChip(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        label = { Text("Selesai (${completedList.size})") },
                        modifier = Modifier.testTag("chip_tab_completed")
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    // Tagihan Perlu Dibayar Sekarang
                    if (dueList.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
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
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = IncomeGreen,
                                        modifier = Modifier.size(42.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Semua Tagihan Bulan Ini Lunas!",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Tagihan rutin (seperti Wifi) otomatis disembunyikan dan baru akan muncul lagi saat mendekati tenggat bulan depan.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    OutlinedButton(
                                        onClick = { selectedTab = 1 },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Lihat Jadwal Bulan Depan (${upcomingList.size})", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    } else {
                        items(dueList, key = { "chk_due_${it.id}" }) { item ->
                            DueExpenseRow(
                                item = item,
                                onChecklist = { itemToConfirmPayment = item },
                                onDelete = { onDeleteExpense(item) }
                            )
                        }
                    }
                }
                1 -> {
                    // Jadwal Bulan Depan / Mendatang (Sudah Lunas Bulan Ini)
                    if (upcomingList.isEmpty()) {
                        item {
                            Text(
                                text = "Tidak ada tagihan mendatang.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                            )
                        }
                    } else {
                        item {
                            Text(
                                text = "Tagihan di bawah ini sudah dibayar untuk bulan ini. Akan otomatis muncul kembali di ceklis saat mendekati tanggal tenggat:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                            )
                        }
                        items(upcomingList, key = { "chk_up_${it.id}" }) { item ->
                            UpcomingExpenseRow(
                                item = item,
                                onPayEarly = { itemToConfirmPayment = item },
                                onDelete = { onDeleteExpense(item) }
                            )
                        }
                    }
                }
                2 -> {
                    // Riwayat Selesai
                    if (completedList.isEmpty()) {
                        item {
                            Text(
                                text = "Belum ada riwayat tagihan selesai.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                            )
                        }
                    } else {
                        items(completedList, key = { "chk_comp_${it.id}" }) { item ->
                            CompletedExpenseRow(
                                item = item,
                                onDelete = { onDeleteExpense(item) }
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
            containerColor = BankingBluePrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 20.dp)
                .testTag("fab_add_expense")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Tagihan")
        }
    }

    // Modal Confirmation Dialog with Multi-Month Pay feature
    if (itemToConfirmPayment != null) {
        val expense = itemToConfirmPayment!!
        var selectedPeriods by remember { mutableIntStateOf(1) }
        val isRecurring = expense.recurrence != Constants.REC_ONCE

        AlertDialog(
            onDismissRequest = { itemToConfirmPayment = null },
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Konfirmasi Pembayaran Tagihan",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Konfirmasi pembayaran untuk \"${expense.title}\"?",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (isRecurring) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Bayar Berapa Bulan Sekaligus?",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Anda bisa bayar langsung untuk beberapa bulan ke depan. Tagihan akan hilang dan baru muncul saat periode tersebut berakhir.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(1, 2, 3, 6).forEach { count ->
                                FilterChip(
                                    selected = selectedPeriods == count,
                                    onClick = { selectedPeriods = count },
                                    label = { Text("$count Bulan") },
                                    modifier = Modifier.testTag("chip_dialog_period_$count")
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val totalAmount = expense.nominal * selectedPeriods
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Total Saldo yang Dipotong:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = CurrencyUtils.formatRupiah(totalAmount),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = ExpenseRed
                            )
                            Text(
                                text = "Saldo kas Anda saat ini: ${CurrencyUtils.formatRupiah(uiState.cashBalance)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onPayExpense(expense.id, selectedPeriods)
                        itemToConfirmPayment = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BankingBluePrimary),
                    modifier = Modifier.testTag("btn_confirm_pay_expense")
                ) {
                    Text("Ceklis & Potong Saldo")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { itemToConfirmPayment = null }) {
                    Text("Batal")
                }
            }
        )
    }

    if (showAddDialog) {
        AddScheduleDialog(
            initialDateMillis = System.currentTimeMillis(),
            initialType = Constants.TYPE_EXPENSE,
            isTypeFixed = true,
            dialogTitle = "Tambah Tagihan Baru",
            onConfirm = { title, amount, type, recurrence, intervalDays, startDate, notificationNote ->
                onAddExpense(
                    title,
                    amount,
                    Constants.TYPE_EXPENSE,
                    recurrence,
                    intervalDays,
                    startDate,
                    notificationNote.ifBlank { "Bayar sebelum jatuh tempo" }
                )
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun DueExpenseRow(
    item: FinancialScheduleEntity,
    onChecklist: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .testTag("checklist_item_${item.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = false,
                onCheckedChange = { onChecklist() },
                colors = CheckboxDefaults.colors(
                    checkedColor = BankingBluePrimary,
                    uncheckedColor = ExpenseRed
                ),
                modifier = Modifier.testTag("cb_item_${item.id}")
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Jatuh tempo: ${DateUtils.formatShortDate(item.nextDueDate)} • ${item.notificationNote}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyUtils.formatRupiah(item.nominal),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ExpenseRed
                )
                Text(
                    text = "Ceklis bayar",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Hapus",
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun UpcomingExpenseRow(
    item: FinancialScheduleEntity,
    onPayEarly: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = BankingBlueContainer,
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = BankingBluePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Sudah lunas bulan ini • Jatuh tempo: ${DateUtils.formatDateIndo(item.nextDueDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = CurrencyUtils.formatRupiah(item.nominal),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

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
private fun CompletedExpenseRow(
    item: FinancialScheduleEntity,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
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
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                val execDate = item.lastExecutedDate?.let { DateUtils.formatDateIndo(it) } ?: "Selesai"
                Text(
                    text = "Telah lunas ($execDate)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = CurrencyUtils.formatRupiah(item.nominal),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

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
