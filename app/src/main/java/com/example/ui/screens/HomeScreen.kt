package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.Constants
import com.example.data.model.DebtReceivableEntity
import com.example.data.model.FinancialScheduleEntity
import com.example.data.model.TransactionItemEntity
import com.example.ui.FinancialUiState
import com.example.ui.components.AdjustBalanceDialog
import com.example.ui.components.AppCard
import com.example.ui.components.AppHeader
import com.example.ui.components.AppPillButton
import com.example.ui.components.BalanceSheetBlock
import com.example.ui.components.EmptyState
import com.example.ui.components.HeaderIconButton
import com.example.ui.components.IconTile
import com.example.ui.components.MoneySize
import com.example.ui.components.MoneyText
import com.example.ui.components.MoneyTone
import com.example.ui.components.ObligationCard
import com.example.ui.components.ObligationState
import com.example.ui.components.SalaryPromptDialog
import com.example.ui.components.SectionHeader
import com.example.ui.components.SettleDebtDialog
import com.example.ui.components.ThemeDirectionDialog
import com.example.ui.theme.AppTheme
import com.example.ui.theme.ThemeDirection
import com.example.util.DateUtils
import java.util.Calendar

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
    onResetToSingleSample: () -> Unit = {},
    themeDirection: ThemeDirection = ThemeDirection.BUKU_KAS,
    onThemeDirectionChange: (ThemeDirection) -> Unit = {}
) {
    val tokens = AppTheme.tokens
    val context = LocalContext.current

    var showAdjustDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var activeSalaryDialogSchedule by remember { mutableStateOf<FinancialScheduleEntity?>(null) }
    var activeSettleDebtItem by remember { mutableStateOf<DebtReceivableEntity?>(null) }
    var expenseToPayItem by remember { mutableStateOf<FinancialScheduleEntity?>(null) }

    val dueSalary = uiState.activeSalaryPrompts.firstOrNull()
    val dueExpenses = uiState.duePendingExpenses
    val upcomingExpenses = uiState.upcomingExpenses
    val activePiutang = uiState.debtsAndReceivables.filter {
        it.type == Constants.DEBT_PIUTANG && !it.isFullySettled
    }
    val actionCount = dueExpenses.size + if (dueSalary != null) 1 else 0

    Column(modifier = Modifier.fillMaxSize()) {
        AppHeader(
            title = "Beranda",
            subtitle = headerSubtitle(uiState, actionCount)
        ) {
            Box {
                HeaderIconButton(
                    icon = Icons.Default.MoreVert,
                    contentDescription = "Menu beranda",
                    onClick = { menuExpanded = true },
                    testTag = "btn_manage_sample_data"
                )
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Jadwal gaji / pemasukan") },
                        onClick = {
                            menuExpanded = false
                            onOpenAddSchedule()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Catat piutang") },
                        onClick = {
                            menuExpanded = false
                            onOpenAddDebt()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Buka kalender") },
                        onClick = {
                            menuExpanded = false
                            onNavigateToCalendar()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Tema tampilan") },
                        onClick = {
                            menuExpanded = false
                            showThemeDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Kelola & bersihkan data") },
                        onClick = {
                            menuExpanded = false
                            showResetDialog = true
                        }
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("home_screen_content"),
            contentPadding = PaddingValues(top = 4.dp, bottom = 100.dp)
        ) {
            // ---- Posisi keuangan ----
            item(key = "home_balance") {
                BalanceSheetBlock(
                    netWorth = uiState.netWorth,
                    cash = uiState.cashBalance,
                    receivable = uiState.totalPiutang,
                    payable = uiState.totalHutang,
                    ready = uiState.isInitialized,
                    onAdjustClick = { showAdjustDialog = true },
                    testTag = "main_balance_card",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
            }

            // ---- Perlu tindakan ----
            item(key = "home_section_action") {
                SectionHeader(
                    title = "Perlu Tindakan",
                    trailingText = if (actionCount > 0) "$actionCount item" else "Beres"
                )
            }

            if (dueSalary != null) {
                item(key = "home_salary_${dueSalary.id}") {
                    ObligationCard(
                        title = dueSalary.title,
                        amount = dueSalary.nominal,
                        state = ObligationState.DueToday,
                        statusText = "Waktunya gajian",
                        metaText = recurrenceLabel(dueSalary.recurrence),
                        actionLabel = "Konfirmasi",
                        onAction = { activeSalaryDialogSchedule = dueSalary },
                        amountTone = MoneyTone.Positive,
                        ready = uiState.isInitialized,
                        cardTestTag = "banner_salary_alert",
                        actionTestTag = "btn_banner_confirm_salary",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                }
            }

            items(dueExpenses.take(3), key = { "home_expense_${it.id}" }) { expense ->
                val state = obligationStateFor(expense.nextDueDate)
                ObligationCard(
                    title = expense.title,
                    amount = expense.nominal,
                    state = state,
                    statusText = statusLabelFor(state, expense.nextDueDate),
                    metaText = recurrenceLabel(expense.recurrence),
                    actionLabel = "Bayar",
                    onAction = { expenseToPayItem = expense },
                    ready = uiState.isInitialized,
                    cardTestTag = "expense_checklist_item_${expense.id}",
                    actionTestTag = "cb_pay_expense_${expense.id}",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }

            if (dueSalary == null && dueExpenses.isEmpty()) {
                item(key = "home_action_empty") {
                    EmptyState(
                        icon = Icons.Default.CheckCircle,
                        title = "Semua terkendali",
                        description = "Tidak ada tagihan atau jadwal gaji yang perlu ditindak hari ini.",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                }
            }

            // ---- Akan datang ----
            if (upcomingExpenses.isNotEmpty()) {
                item(key = "home_section_upcoming") {
                    SectionHeader(
                        title = "Akan Datang",
                        trailingText = "Ceklis",
                        trailingAction = onNavigateToChecklist
                    )
                }
                items(upcomingExpenses.take(2), key = { "home_upcoming_${it.id}" }) { upcoming ->
                    ObligationCard(
                        title = upcoming.title,
                        amount = upcoming.nominal,
                        state = ObligationState.Scheduled,
                        statusText = DateUtils.formatShortDate(upcoming.nextDueDate),
                        metaText = recurrenceLabel(upcoming.recurrence),
                        actionLabel = "Ceklis",
                        onAction = onNavigateToChecklist,
                        ready = uiState.isInitialized,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                }
            }

            // ---- Piutang aktif ----
            if (activePiutang.isNotEmpty()) {
                item(key = "home_section_piutang") {
                    SectionHeader(
                        title = "Piutang Aktif",
                        trailingText = "Kelola",
                        trailingAction = onNavigateToDebts
                    )
                }
                items(activePiutang.take(2), key = { "home_piutang_${it.id}" }) { debt ->
                    ObligationCard(
                        title = debt.personName,
                        amount = debt.remainingAmount,
                        state = ObligationState.Scheduled,
                        statusText = "Piutang aktif",
                        metaText = debt.notes.takeIf { it.isNotBlank() },
                        actionLabel = "Bayar",
                        onAction = { activeSettleDebtItem = debt },
                        amountTone = MoneyTone.Receivable,
                        ready = uiState.isInitialized,
                        actionTestTag = "btn_settle_piutang_${debt.id}",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                }
            }

            // ---- Mutasi terkini ----
            item(key = "home_section_history") {
                SectionHeader(
                    title = "Mutasi Terkini",
                    trailingText = "Semua",
                    trailingAction = onNavigateToHistory
                )
            }

            if (uiState.recentTransactions.isEmpty()) {
                item(key = "home_tx_empty") {
                    EmptyState(
                        icon = Icons.Default.CalendarMonth,
                        title = if (uiState.isInitialized) "Belum ada mutasi" else "Memuat data",
                        description = if (uiState.isInitialized) {
                            "Setiap perubahan saldo akan tercatat di sini beserta alasannya."
                        } else {
                            "Sebentar, sedang membaca catatan keuangan kamu."
                        },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                }
            } else {
                items(uiState.recentTransactions.take(3), key = { "home_tx_${it.id}" }) { tx ->
                    TransactionRowItem(
                        transaction = tx,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                }
            }

            item(key = "home_cta") {
                AppPillButton(
                    label = "Catat tagihan baru",
                    onClick = onNavigateToChecklist,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    testTag = "btn_home_add_expense"
                )
            }
        }
    }

    if (expenseToPayItem != null) {
        val expense = expenseToPayItem!!
        var selectedPeriods by remember { mutableIntStateOf(1) }
        val isRecurring = expense.recurrence != Constants.REC_ONCE
        val totalDeduction = expense.nominal * selectedPeriods

        AlertDialog(
            onDismissRequest = { expenseToPayItem = null },
            shape = RoundedCornerShape(tokens.radiusSheet),
            containerColor = tokens.surface,
            title = {
                Text(
                    text = "Konfirmasi Pembayaran",
                    style = tokens.titleStyle,
                    fontWeight = FontWeight.Bold,
                    color = tokens.ink
                )
            },
            text = {
                Column {
                    Text(
                        text = "Bayar \"${expense.title}\"? Saldo kas akan dipotong sekarang.",
                        style = tokens.bodyStyle,
                        color = tokens.ink2
                    )

                    if (isRecurring) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Bayar untuk berapa periode?",
                            style = tokens.labelStyle,
                            color = tokens.ink
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(1, 2, 3).forEach { count ->
                                FilterChip(
                                    selected = selectedPeriods == count,
                                    onClick = { selectedPeriods = count },
                                    label = { Text(if (count == 1) "1 Bulan" else "$count Bulan") },
                                    modifier = Modifier.testTag("chip_period_$count")
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    AppCard(
                        containerColor = tokens.surfaceSunken,
                        shape = RoundedCornerShape(tokens.radiusChip)
                    ) {
                        Text(
                            text = "TOTAL DIPOTONG",
                            style = tokens.captionStyle,
                            color = tokens.ink3
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        MoneyText(
                            amount = totalDeduction,
                            size = MoneySize.AmountL,
                            tone = MoneyTone.Negative
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (selectedPeriods > 1) {
                                "Membayar $selectedPeriods periode sekaligus. Tagihan muncul lagi setelah periode tersebut."
                            } else {
                                "Membayar periode ini. Tagihan muncul lagi mendekati tenggat berikutnya."
                            },
                            style = tokens.captionStyle,
                            color = tokens.ink3
                        )
                    }
                }
            },
            confirmButton = {
                Surface(
                    onClick = {
                        onPayExpense(expense.id, selectedPeriods)
                        expenseToPayItem = null
                    },
                    shape = RoundedCornerShape(tokens.radiusPill),
                    color = tokens.brand,
                    border = BorderStroke(tokens.borderWidth, tokens.cardBorder),
                    modifier = Modifier.testTag("btn_confirm_pay_expense_modal")
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Konfirmasi & Potong Saldo",
                            style = tokens.labelStyle,
                            color = tokens.onBrand,
                            maxLines = 1
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { expenseToPayItem = null }) {
                    Text(text = "Batal", style = tokens.labelStyle, color = tokens.ink2)
                }
            }
        )
    }

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

    if (showThemeDialog) {
        ThemeDirectionDialog(
            current = themeDirection,
            onSelect = { direction -> onThemeDirectionChange(direction) },
            onDismiss = { showThemeDialog = false }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            shape = RoundedCornerShape(tokens.radiusSheet),
            containerColor = tokens.surface,
            title = {
                Text(
                    text = "Kelola & Bersihkan Data",
                    style = tokens.titleStyle,
                    fontWeight = FontWeight.Bold,
                    color = tokens.ink
                )
            },
            text = {
                Column {
                    Text(
                        text = "Aplikasi siap dipakai untuk pencatatan keuangan nyata. Kamu bisa membersihkan seluruh data contoh untuk mulai dari saldo Rp 0, atau menyisakan satu contoh sebagai panduan.",
                        style = tokens.bodyStyle,
                        color = tokens.ink2
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    AppPillButton(
                        label = "Bersihkan semua data (mulai Rp 0)",
                        onClick = {
                            onClearAllData()
                            showResetDialog = false
                            Toast.makeText(
                                context,
                                "Semua data dibersihkan. Saldo kas mulai Rp 0.",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        containerColor = tokens.danger,
                        contentColor = tokens.onDanger,
                        testTag = "btn_confirm_clear_all"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    AppPillButton(
                        label = "Sisakan 1 contoh tagihan",
                        onClick = {
                            onResetToSingleSample()
                            showResetDialog = false
                            Toast.makeText(
                                context,
                                "Data direset menyisakan 1 contoh tagihan panduan.",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        containerColor = tokens.surface,
                        contentColor = tokens.ink,
                        testTag = "btn_confirm_single_sample"
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(text = "Tutup", style = tokens.labelStyle, color = tokens.ink2)
                }
            }
        )
    }
}

@Composable
private fun TransactionRowItem(
    transaction: TransactionItemEntity,
    modifier: Modifier = Modifier
) {
    val tokens = AppTheme.tokens
    val isIncome = transaction.amount >= 0

    AppCard(modifier = modifier, contentPadding = PaddingValues(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(
                icon = if (isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                tint = if (isIncome) tokens.inText else tokens.outText,
                background = if (isIncome) tokens.inBg else tokens.outBg,
                contentDescription = if (isIncome) "Uang masuk" else "Uang keluar",
                tileSize = 34.dp,
                iconSize = 16.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title,
                    style = tokens.bodyStyle,
                    fontWeight = FontWeight.SemiBold,
                    color = tokens.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = DateUtils.formatDateTimeIndo(transaction.timestamp),
                    style = tokens.captionStyle,
                    color = tokens.ink3,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            MoneyText(
                amount = transaction.amount,
                size = MoneySize.AmountM,
                tone = if (isIncome) MoneyTone.Positive else MoneyTone.Negative,
                signed = true
            )
        }
    }
}

private fun headerSubtitle(uiState: FinancialUiState, actionCount: Int): String {
    val date = DateUtils.formatDateIndo(System.currentTimeMillis())
    val summary = when {
        !uiState.isInitialized -> "memuat data"
        actionCount > 0 -> "$actionCount perlu ditindak"
        else -> "tidak ada yang perlu ditindak"
    }
    return "$date \u00b7 $summary"
}

private fun recurrenceLabel(recurrence: String): String = when (recurrence) {
    Constants.REC_ONCE -> "Sekali"
    Constants.REC_BI_WEEKLY -> "2 mingguan"
    Constants.REC_MONTHLY -> "Bulanan"
    Constants.REC_CUSTOM -> "Interval khusus"
    else -> recurrence
}

private fun daysUntil(targetMillis: Long): Long {
    val today = Calendar.getInstance()
    val target = Calendar.getInstance()
    target.timeInMillis = targetMillis
    listOf(today, target).forEach { calendar ->
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
    }
    val dayMillis = 24L * 60L * 60L * 1000L
    return (target.timeInMillis - today.timeInMillis) / dayMillis
}

private fun obligationStateFor(dueMillis: Long): ObligationState {
    val days = daysUntil(dueMillis)
    return when {
        days < 0L -> ObligationState.Overdue
        days == 0L -> ObligationState.DueToday
        days <= 3L -> ObligationState.DueSoon
        else -> ObligationState.Scheduled
    }
}

private fun statusLabelFor(state: ObligationState, dueMillis: Long): String {
    val days = daysUntil(dueMillis)
    return when (state) {
        ObligationState.Overdue -> "Terlambat ${-days} hari"
        ObligationState.DueToday -> "Jatuh tempo hari ini"
        ObligationState.DueSoon -> "Jatuh tempo $days hari lagi"
        ObligationState.Scheduled -> DateUtils.formatShortDate(dueMillis)
        ObligationState.Done -> "Lunas"
    }
}
