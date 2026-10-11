package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Constants
import com.example.data.model.DebtReceivableEntity
import com.example.data.model.FinancialScheduleEntity
import com.example.ui.FinancialUiState
import com.example.ui.components.AdjustBalanceDialog
import com.example.ui.components.SalaryPromptDialog
import com.example.ui.components.SettleDebtDialog
import com.example.ui.theme.BankingBlueContainer
import com.example.ui.theme.BankingBluePrimary
import com.example.ui.theme.BlueReceivable
import com.example.ui.theme.CardGradientEnd
import com.example.ui.theme.CardGradientMid
import com.example.ui.theme.CardGradientStart
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.LightOnBackground
import com.example.ui.theme.LightOnSurfaceVariant
import com.example.util.CurrencyUtils
import com.example.util.DateUtils

@Composable
fun HomeScreen(
    uiState: FinancialUiState,
    onAdjustBalance: (newBalance: Long, reason: String) -> Unit,
    onConfirmSalary: (scheduleId: Long, actualAmount: Long, note: String) -> Unit,
    onDismissSalary: (scheduleId: Long) -> Unit,
    onPayExpense: (scheduleId: Long, periodsCount: Int) -> Unit,
    onSettleDebt: (debtId: Long, paymentAmount: Long, note: String) -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToChecklist: () -> Unit,
    onNavigateToDebts: () -> Unit,
    onOpenAddSchedule: () -> Unit,
    onOpenAddDebt: () -> Unit,
    onNavigateToHistory: () -> Unit = {},
    onClearAllData: () -> Unit = {},
    onResetToSingleSample: () -> Unit = {}
) {
    val context = LocalContext.current
    var showAdjustDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var activeSalaryDialogSchedule by remember { mutableStateOf<FinancialScheduleEntity?>(null) }
    var activeSettleDebtItem by remember { mutableStateOf<DebtReceivableEntity?>(null) }
    var expenseToPayItem by remember { mutableStateOf<FinancialScheduleEntity?>(null) }

    val dueSalary = uiState.activeSalaryPrompts.firstOrNull()

    // Filter active items that actually need checking
    val dueExpenses = uiState.duePendingExpenses
    val activePiutang = uiState.debtsAndReceivables.filter { it.type == Constants.DEBT_PIUTANG && !it.isFullySettled }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header "PENCATAT KEUANGAN" tetap / tidak terscroll
        BankingHomeHeader(
            todayDate = DateUtils.formatDateIndo(System.currentTimeMillis()),
            onManageDataClick = { showResetDialog = true }
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("home_screen_content"),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {

        // Salary Prompt Banner (If due today or pending confirmation)
        if (dueSalary != null) {
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically()
                ) {
                    SalaryAlertBanner(
                        schedule = dueSalary,
                        onOpenPrompt = { activeSalaryDialogSchedule = dueSalary },
                        onDismiss = { onDismissSalary(dueSalary.id) }
                    )
                }
            }
        }

        // Signature ATM / Debit Banking Blue Card (Kartu Rekening Kas)
        item {
            BankingBalanceCard(
                cashBalance = uiState.cashBalance,
                piutangTotal = uiState.totalPiutang,
                hutangTotal = uiState.totalHutang,
                netWorth = uiState.netWorth,
                onAdjustClick = { showAdjustDialog = true }
            )
        }

        // Quick Action Shortcuts (Clean White Banking Buttons)
        item {
            BankingQuickActions(
                onAddSchedule = onOpenAddSchedule,
                onAddExpense = onNavigateToChecklist,
                onAddDebt = onOpenAddDebt
            )
        }

        // TAGIHAN & PENGELUARAN:
        // HILANGKAN KATEGORI BILA SUDAH TIDAK ADA YANG PERLU DICEKLIS / KOSONG!
        if (dueExpenses.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                SectionHeader(
                    title = "Tagihan Perlu Dibayar (${dueExpenses.size})",
                    actionText = "Semua Tagihan",
                    onActionClick = onNavigateToChecklist
                )
            }

            items(dueExpenses.take(3), key = { "home_expense_${it.id}" }) { expense ->
                HomeExpenseChecklistItem(
                    schedule = expense,
                    onChecked = { expenseToPayItem = expense }
                )
            }
        }

        // ASET PIUTANG:
        // HILANGKAN KATEGORI BILA SUDAH TIDAK ADA YANG PERLU DICEKLIS / SUDAH LUNAS / KOSONG!
        if (activePiutang.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                SectionHeader(
                    title = "Aset Piutang Aktif (${activePiutang.size})",
                    actionText = "Kelola Aset",
                    onActionClick = onNavigateToDebts
                )
            }

            items(activePiutang.take(2), key = { "home_piutang_${it.id}" }) { debt ->
                HomePiutangItem(
                    debt = debt,
                    onSettle = { activeSettleDebtItem = debt }
                )
            }
        }

        // Status Card if both are empty (Semua tanggungan sudah beres)
        if (dueExpenses.isEmpty() && activePiutang.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = BankingBluePrimary.copy(alpha = 0.12f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = BankingBluePrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Semua Tanggungan Terkendali",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tidak ada tagihan atau piutang yang perlu diceklis saat ini.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Recent Transactions History
        item {
            Spacer(modifier = Modifier.height(10.dp))
            SectionHeader(
                title = "Mutasi Rekening & Kas Terkini",
                actionText = "Riwayat Lengkap",
                onActionClick = onNavigateToHistory
            )
        }

        if (uiState.recentTransactions.isNotEmpty()) {
            items(uiState.recentTransactions.take(5), key = { "home_tx_${it.id}" }) { tx ->
                BankingTransactionRowItem(transaction = tx)
            }
        } else {
            item {
                Text(
                    text = "Belum ada transaksi tercatat.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }
        }
    }
    }

    // Modal Pay Expense with Multi-month Option
    if (expenseToPayItem != null) {
        val expense = expenseToPayItem!!
        var selectedPeriods by remember { mutableIntStateOf(1) }
        val isRecurring = expense.recurrence != Constants.REC_ONCE

        AlertDialog(
            onDismissRequest = { expenseToPayItem = null },
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
                            text = "Pilih Periode Pembayaran:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(1, 2, 3).forEach { count ->
                                FilterChip(
                                    selected = selectedPeriods == count,
                                    onClick = { selectedPeriods = count },
                                    label = {
                                        Text(if (count == 1) "1 Bulan" else "$count Bulan")
                                    },
                                    modifier = Modifier.testTag("chip_period_$count")
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val totalDeduction = expense.nominal * selectedPeriods
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Total Nominal yang Dipotong:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = CurrencyUtils.formatRupiah(totalDeduction),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = ExpenseRed
                            )
                            Text(
                                text = if (selectedPeriods > 1) "Membayar $selectedPeriods bulan sekaligus. Tagihan ini akan hilang dan baru muncul $selectedPeriods bulan lagi."
                                else "Membayar untuk periode ini. Tagihan akan hilang dan baru muncul mendekati tenggat bulan depan.",
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
                        expenseToPayItem = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BankingBluePrimary),
                    modifier = Modifier.testTag("btn_confirm_pay_expense_modal")
                ) {
                    Text("Konfirmasi & Potong Saldo", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { expenseToPayItem = null }) {
                    Text("Batal")
                }
            }
        )
    }

    // Dialogs
    if (showAdjustDialog) {
        AdjustBalanceDialog(
            currentBalance = uiState.cashBalance,
            onConfirm = { newBal, reason ->
                onAdjustBalance(newBal, reason)
                showAdjustDialog = false
            },
            onDismiss = { showAdjustDialog = false }
        )
    }

    if (activeSalaryDialogSchedule != null) {
        SalaryPromptDialog(
            schedule = activeSalaryDialogSchedule!!,
            onConfirm = { scheduleId, actualAmount, note ->
                onConfirmSalary(scheduleId, actualAmount, note)
                activeSalaryDialogSchedule = null
            },
            onDismiss = { scheduleId ->
                onDismissSalary(scheduleId)
                activeSalaryDialogSchedule = null
            }
        )
    }

    if (activeSettleDebtItem != null) {
        SettleDebtDialog(
            item = activeSettleDebtItem!!,
            onConfirm = { debtId, amount, note ->
                onSettleDebt(debtId, amount, note)
                activeSettleDebtItem = null
            },
            onDismiss = { activeSettleDebtItem = null }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = null,
                        tint = BankingBluePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Kelola & Bersihkan Data",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = LightOnBackground
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Aplikasi Anda siap digunakan untuk pencatatan keuangan riil. Anda dapat membersihkan seluruh data contoh untuk mulai dari saldo Rp 0, atau memuat 1 contoh saja sebagai panduan.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LightOnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Pilih opsi pengelolaan data:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = LightOnBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            onClearAllData()
                            showResetDialog = false
                            Toast.makeText(context, "Semua data berhasil dibersihkan. Saldo kas mulai Rp 0.", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_confirm_clear_all")
                    ) {
                        Text(
                            text = "Bersihkan Semua Data (Mulai Rp 0)",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            onResetToSingleSample()
                            showResetDialog = false
                            Toast.makeText(context, "Data berhasil direset menyisakan 1 contoh tagihan panduan.", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BankingBluePrimary),
                        border = BorderStroke(1.dp, BankingBluePrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_confirm_single_sample")
                    ) {
                        Text(
                            text = "Sisakan 1 Contoh Tagihan (Panduan)",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showResetDialog = false }
                ) {
                    Text(
                        text = "Tutup",
                        color = LightOnSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        )
    }
}

@Composable
private fun BankingHomeHeader(
    todayDate: String,
    onManageDataClick: () -> Unit
) {
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
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = BankingBluePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "PENCATAT KEUANGAN",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Layanan Keuangan & Kas Mandiri",
                        fontSize = 11.sp,
                        color = Color(0xFFDBEAFE)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.18f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = todayDate,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                Surface(
                    onClick = onManageDataClick,
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.25f),
                    modifier = Modifier.testTag("btn_manage_sample_data")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset Data",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Reset",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SalaryAlertBanner(
    schedule: FinancialScheduleEntity,
    onOpenPrompt: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, BankingBluePrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clickable { onOpenPrompt() }
            .testTag("banner_salary_alert")
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = GoldAccent.copy(alpha = 0.15f),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = BankingBlueContainer
                ) {
                    Text(
                        text = "GAJIAN TIBA",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = BankingBluePrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${schedule.title} (${CurrencyUtils.formatRupiah(schedule.nominal)})",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = LightOnBackground
                )
                Text(
                    text = "Ketuk untuk konfirmasi penerimaan gaji",
                    fontSize = 11.sp,
                    color = LightOnSurfaceVariant
                )
            }

            Button(
                onClick = onOpenPrompt,
                colors = ButtonDefaults.buttonColors(containerColor = BankingBluePrimary),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("btn_banner_confirm_salary")
            ) {
                Text("Konfirmasi", fontSize = 12.sp, color = Color.White)
            }
        }
    }
}

@Composable
private fun BankingBalanceCard(
    cashBalance: Long,
    piutangTotal: Long,
    hutangTotal: Long,
    netWorth: Long,
    onAdjustClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .testTag("main_balance_card")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            CardGradientStart,
                            CardGradientMid,
                            CardGradientEnd
                        )
                    )
                )
                .padding(22.dp)
        ) {
            // Subtle watermark circles on the right
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.05f),
                    radius = size.width * 0.45f,
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.9f, size.height * 0.15f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.03f),
                    radius = size.width * 0.7f,
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.95f, size.height * 0.4f)
                )
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                // Top Row: Saldo Kas Header + Atur Saldo Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Total Saldo Kas",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.95f),
                            letterSpacing = 0.3.sp
                        )
                    }

                    Surface(
                        onClick = onAdjustClick,
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.22f),
                        modifier = Modifier.testTag("btn_adjust_balance_open")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
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

                Spacer(modifier = Modifier.height(10.dp))

                // Big Bold Balance (Crisp White)
                Text(
                    text = CurrencyUtils.formatRupiah(cashBalance),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Sub-section: Piutang & Hutang
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF001F4D).copy(alpha = 0.55f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Aset Piutang (Di Orang):",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFBAE6FD),
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = CurrencyUtils.formatRupiahWithSign(piutangTotal),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (piutangTotal > 0) Color(0xFF67E8F9) else Color.White
                                )
                            }

                            Divider(
                                modifier = Modifier
                                    .height(28.dp)
                                    .width(1.dp),
                                color = Color.White.copy(alpha = 0.25f)
                            )

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 12.dp)
                            ) {
                                Text(
                                    text = "Tanggungan Hutang:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFFECDD3),
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = if (hutangTotal > 0) "-${CurrencyUtils.formatRupiah(hutangTotal)}" else "Rp 0",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (hutangTotal > 0) Color(0xFFFCA5A5) else Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = Color.White.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Kekayaan Bersih (Kas + Piutang - Hutang):",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Text(
                                text = CurrencyUtils.formatRupiah(netWorth),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BankingQuickActions(
    onAddSchedule: () -> Unit,
    onAddExpense: () -> Unit,
    onAddDebt: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickActionButton(
            label = "Jadwal Gaji",
            icon = Icons.Default.CalendarMonth,
            color = BankingBluePrimary,
            onClick = onAddSchedule,
            modifier = Modifier.weight(1f).testTag("quick_action_schedule")
        )
        QuickActionButton(
            label = "Ceklis Tagihan",
            icon = Icons.Default.Checklist,
            color = ExpenseRed,
            onClick = onAddExpense,
            modifier = Modifier.weight(1f).testTag("quick_action_expense")
        )
        QuickActionButton(
            label = "Catat Piutang",
            icon = Icons.Default.Handshake,
            color = BlueReceivable,
            onClick = onAddDebt,
            modifier = Modifier.weight(1f).testTag("quick_action_debt")
        )
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        tonalElevation = 2.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.12f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = LightOnBackground,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    actionText: String,
    onActionClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = LightOnBackground
        )
        Text(
            text = actionText,
            style = MaterialTheme.typography.labelMedium,
            color = BankingBluePrimary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable { onActionClick() }
        )
    }
}

@Composable
private fun HomeExpenseChecklistItem(
    schedule: FinancialScheduleEntity,
    onChecked: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .testTag("expense_checklist_item_${schedule.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = false,
                onCheckedChange = { onChecked() },
                colors = CheckboxDefaults.colors(checkedColor = BankingBluePrimary),
                modifier = Modifier.testTag("cb_pay_expense_${schedule.id}")
            )

            Spacer(modifier = Modifier.width(6.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = schedule.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = LightOnBackground
                )
                Text(
                    text = "Jatuh tempo: ${DateUtils.formatShortDate(schedule.nextDueDate)} • ${schedule.notificationNote}",
                    style = MaterialTheme.typography.bodySmall,
                    color = LightOnSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyUtils.formatRupiah(schedule.nominal),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ExpenseRed
                )
                Text(
                    text = "Ceklis bayar",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = BankingBluePrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun HomePiutangItem(
    debt: DebtReceivableEntity,
    onSettle: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
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
                color = BlueReceivable.copy(alpha = 0.12f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Handshake,
                        contentDescription = null,
                        tint = BlueReceivable,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = debt.personName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = LightOnBackground
                )
                Text(
                    text = "Sisa: ${CurrencyUtils.formatRupiah(debt.remainingAmount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = BlueReceivable
                )
                if (debt.notes.isNotBlank()) {
                    Text(
                        text = debt.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = LightOnSurfaceVariant
                    )
                }
            }

            Button(
                onClick = onSettle,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BankingBluePrimary),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("btn_settle_piutang_${debt.id}")
            ) {
                Text("Bayar", fontSize = 12.sp, color = Color.White)
            }
        }
    }
}

@Composable
private fun BankingTransactionRowItem(transaction: com.example.data.model.TransactionItemEntity) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
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
            val isIncome = transaction.amount >= 0
            val iconColor = if (isIncome) IncomeGreen else ExpenseRed

            Surface(
                shape = CircleShape,
                color = iconColor.copy(alpha = 0.12f),
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = LightOnBackground
                )
                if (transaction.reasonOrNote.isNotBlank()) {
                    Text(
                        text = transaction.reasonOrNote,
                        style = MaterialTheme.typography.bodySmall,
                        color = LightOnSurfaceVariant
                    )
                }
                Text(
                    text = DateUtils.formatDateTimeIndo(transaction.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Text(
                text = CurrencyUtils.formatRupiahWithSign(transaction.amount),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (isIncome) IncomeGreen else ExpenseRed
            )
        }
    }
}
