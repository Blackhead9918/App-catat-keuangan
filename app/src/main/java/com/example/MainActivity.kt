package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Constants
import com.example.data.model.DebtReceivableEntity
import com.example.data.model.FinancialScheduleEntity
import com.example.ui.FinancialViewModel
import com.example.ui.components.AddDebtDialog
import com.example.ui.components.AddScheduleDialog
import com.example.ui.components.ConfirmSheet
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.ChecklistScreen
import com.example.ui.screens.DebtAssetScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.AppTheme
import com.example.ui.theme.PencatatKeuanganTheme
import com.example.ui.theme.ThemeDirection
import com.example.ui.theme.ThemePreference

enum class NavTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    HOME("Beranda", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet, "nav_home"),
    CALENDAR("Kalender", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth, "nav_calendar"),
    CHECKLIST("Ceklis", Icons.Filled.Checklist, Icons.Outlined.Checklist, "nav_checklist"),
    DEBT("Aset/Hutang", Icons.Filled.Handshake, Icons.Outlined.Handshake, "nav_debt"),
    HISTORY("Riwayat", Icons.Filled.History, Icons.Outlined.History, "nav_history")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            // Pilihan tema dibaca sekali saat komposisi pertama, lalu disimpan ke
            // SharedPreferences setiap kali user menggantinya.
            var direction by remember { mutableStateOf(ThemePreference.read(context)) }

            PencatatKeuanganTheme(direction = direction) {
                PencatatKeuanganApp(
                    themeDirection = direction,
                    onThemeDirectionChange = { newDirection ->
                        ThemePreference.write(context, newDirection)
                        direction = newDirection
                    }
                )
            }
        }
    }
}

@Composable
fun PencatatKeuanganApp(
    viewModel: FinancialViewModel = viewModel(),
    themeDirection: ThemeDirection = ThemeDirection.BUKU_KAS,
    onThemeDirectionChange: (ThemeDirection) -> Unit = {}
) {
    val tokens = AppTheme.tokens
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var currentTab by remember { mutableStateOf(NavTab.HOME) }

    // Global dialogs triggered from shortcuts
    var showGlobalAddScheduleDialog by remember { mutableStateOf(false) }
    var showGlobalAddDebtDialog by remember { mutableStateOf(false) }

    // Konfirmasi untuk aksi yang tidak bisa dibatalkan.
    var pendingScheduleDelete by remember { mutableStateOf<FinancialScheduleEntity?>(null) }
    var pendingDebtDelete by remember { mutableStateOf<DebtReceivableEntity?>(null) }
    var pendingCalendarPayId by remember { mutableStateOf<Long?>(null) }

    // Handle system back navigation to return to Home screen
    if (currentTab != NavTab.HOME) {
        BackHandler {
            currentTab = NavTab.HOME
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = tokens.surface,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .testTag("bottom_nav_bar")
                    .border(
                        width = tokens.borderWidth,
                        color = tokens.cardBorder,
                        shape = RectangleShape
                    )
            ) {
                NavTab.entries.forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = tokens.ink,
                            selectedTextColor = tokens.ink,
                            unselectedIconColor = tokens.ink3,
                            unselectedTextColor = tokens.ink3,
                            indicatorColor = tokens.brandSoft
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                NavTab.HOME -> {
                    HomeScreen(
                        uiState = uiState,
                        onAdjustBalance = { newBal, reason ->
                            viewModel.adjustCashBalance(newBal, reason)
                        },
                        onConfirmSalary = { scheduleId, actualAmount, note ->
                            viewModel.confirmSalaryReceived(scheduleId, actualAmount, note)
                        },
                        onDismissSalary = { scheduleId ->
                            viewModel.dismissSalaryPrompt(scheduleId)
                        },
                        onPayExpense = { scheduleId, periodsCount ->
                            viewModel.payExpenseChecklist(scheduleId, periodsCount)
                        },
                        onSettleDebt = { debtId, amount, note ->
                            viewModel.settleDebtOrReceivable(debtId, amount, note)
                        },
                        onNavigateToCalendar = { currentTab = NavTab.CALENDAR },
                        onNavigateToChecklist = { currentTab = NavTab.CHECKLIST },
                        onNavigateToDebts = { currentTab = NavTab.DEBT },
                        onNavigateToHistory = { currentTab = NavTab.HISTORY },
                        onOpenAddSchedule = { showGlobalAddScheduleDialog = true },
                        onOpenAddDebt = { showGlobalAddDebtDialog = true },
                        onClearAllData = { viewModel.clearAllData() },
                        onResetToSingleSample = { viewModel.resetToSingleSample() },
                        themeDirection = themeDirection,
                        onThemeDirectionChange = onThemeDirectionChange
                    )
                }
                NavTab.CALENDAR -> {
                    CalendarScreen(
                        uiState = uiState,
                        onChangeMonth = { delta ->
                            viewModel.changeCalendarMonth(delta)
                        },
                        onSelectDate = { dateMillis ->
                            viewModel.selectCalendarDate(dateMillis)
                        },
                        onAddSchedule = { title, amount, type, recurrence, intervalDays, startDate, notificationNote ->
                            viewModel.addSchedule(title, amount, type, recurrence, intervalDays, startDate, notificationNote)
                        },
                        onConfirmSalary = { scheduleId, actualAmount, note ->
                            viewModel.confirmSalaryReceived(scheduleId, actualAmount, note)
                        },
                        onDismissSalary = { scheduleId ->
                            viewModel.dismissSalaryPrompt(scheduleId)
                        },
                        onPayExpense = { scheduleId ->
                            // Jangan potong saldo langsung: minta konfirmasi dulu.
                            pendingCalendarPayId = scheduleId
                        },
                        onDeleteSchedule = { schedule ->
                            pendingScheduleDelete = schedule
                        }
                    )
                }
                NavTab.CHECKLIST -> {
                    ChecklistScreen(
                        uiState = uiState,
                        onPayExpense = { scheduleId, periodsCount ->
                            viewModel.payExpenseChecklist(scheduleId, periodsCount)
                        },
                        onAddExpense = { title, amount, type, recurrence, intervalDays, startDate, notificationNote ->
                            viewModel.addSchedule(title, amount, type, recurrence, intervalDays, startDate, notificationNote)
                        },
                        onDeleteExpense = { schedule ->
                            pendingScheduleDelete = schedule
                        }
                    )
                }
                NavTab.DEBT -> {
                    DebtAssetScreen(
                        uiState = uiState,
                        onAddDebt = { personName, type, amount, dueDate, notes ->
                            viewModel.addDebtOrReceivable(personName, type, amount, dueDate, notes)
                        },
                        onSettleDebt = { debtId, amount, note ->
                            viewModel.settleDebtOrReceivable(debtId, amount, note)
                        },
                        onDeleteDebt = { debt ->
                            pendingDebtDelete = debt
                        }
                    )
                }
                NavTab.HISTORY -> {
                    HistoryScreen(
                        uiState = uiState,
                        onAdjustBalance = { newBal, reason ->
                            viewModel.adjustCashBalance(newBal, reason)
                        }
                    )
                }
            }
        }
    }

    if (showGlobalAddScheduleDialog) {
        AddScheduleDialog(
            initialDateMillis = System.currentTimeMillis(),
            initialType = Constants.TYPE_INCOME,
            dialogTitle = "Jadwal Gaji / Pemasukan",
            onConfirm = { title, amount, type, recurrence, intervalDays, startDate, notificationNote ->
                viewModel.addSchedule(title, amount, type, recurrence, intervalDays, startDate, notificationNote)
                showGlobalAddScheduleDialog = false
            },
            onDismiss = { showGlobalAddScheduleDialog = false }
        )
    }

    if (showGlobalAddDebtDialog) {
        AddDebtDialog(
            onConfirm = { personName, type, amount, dueDate, notes ->
                viewModel.addDebtOrReceivable(personName, type, amount, dueDate, notes)
                showGlobalAddDebtDialog = false
            },
            onDismiss = { showGlobalAddDebtDialog = false }
        )
    }

    pendingScheduleDelete?.let { schedule ->
        ConfirmSheet(
            title = "Hapus jadwal ini?",
            message = "\"${schedule.title}\" akan dihapus dari daftar jadwal. Riwayat transaksi yang sudah tercatat tidak ikut terhapus.",
            confirmLabel = "Hapus jadwal",
            destructive = true,
            confirmTestTag = "btn_confirm_delete",
            onConfirm = {
                viewModel.deleteSchedule(schedule)
                pendingScheduleDelete = null
            },
            onDismiss = { pendingScheduleDelete = null }
        )
    }

    pendingDebtDelete?.let { debt ->
        val isReceivable = debt.type == Constants.DEBT_PIUTANG
        ConfirmSheet(
            title = if (isReceivable) "Hapus piutang ini?" else "Hapus hutang ini?",
            message = "Catatan ${if (isReceivable) "piutang" else "hutang"} untuk ${debt.personName} akan dihapus. Sisa yang belum dilunasi tidak lagi terhitung dalam kekayaan bersih.",
            confirmLabel = "Hapus catatan",
            destructive = true,
            amountPreview = debt.remainingAmount,
            hint = "Sisa yang belum dilunasi",
            confirmTestTag = "btn_confirm_delete",
            onConfirm = {
                viewModel.deleteDebt(debt)
                pendingDebtDelete = null
            },
            onDismiss = { pendingDebtDelete = null }
        )
    }

    val pendingPaySchedule = pendingCalendarPayId?.let { id ->
        uiState.schedules.firstOrNull { it.id == id }
    }
    if (pendingPaySchedule != null) {
        ConfirmSheet(
            title = "Konfirmasi pembayaran",
            message = "Bayar \"${pendingPaySchedule.title}\"? Saldo kas akan dipotong sekarang, dan tagihan muncul lagi pada siklus berikutnya.",
            confirmLabel = "Bayar & potong saldo",
            amountPreview = pendingPaySchedule.nominal,
            hint = "Jumlah yang dipotong dari saldo kas",
            confirmTestTag = "btn_confirm_pay_expense_modal",
            onConfirm = {
                viewModel.payExpenseChecklist(pendingPaySchedule.id, 1)
                pendingCalendarPayId = null
            },
            onDismiss = { pendingCalendarPayId = null }
        )
    }
}
