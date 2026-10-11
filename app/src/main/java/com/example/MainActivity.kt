package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Constants
import com.example.ui.FinancialViewModel
import com.example.ui.components.AddDebtDialog
import com.example.ui.components.AddScheduleDialog
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.ChecklistScreen
import com.example.ui.screens.DebtAssetScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import androidx.compose.foundation.border
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.theme.BankingBlueContainer
import com.example.ui.theme.BankingBluePrimary
import com.example.ui.theme.PencatatKeuanganTheme

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
            PencatatKeuanganTheme {
                PencatatKeuanganApp()
            }
        }
    }
}

@Composable
fun PencatatKeuanganApp(
    viewModel: FinancialViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var currentTab by remember { mutableStateOf(NavTab.HOME) }

    // Global dialogs triggered from shortcuts
    var showGlobalAddScheduleDialog by remember { mutableStateOf(false) }
    var showGlobalAddDebtDialog by remember { mutableStateOf(false) }

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
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                modifier = Modifier
                    .testTag("bottom_nav_bar")
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                        shape = androidx.compose.ui.graphics.RectangleShape
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
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BankingBluePrimary,
                            selectedTextColor = BankingBluePrimary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = BankingBlueContainer
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        androidx.compose.foundation.layout.Box(
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
                        onResetToSingleSample = { viewModel.resetToSingleSample() }
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
                            viewModel.payExpenseChecklist(scheduleId, 1)
                        },
                        onDeleteSchedule = { schedule ->
                            viewModel.deleteSchedule(schedule)
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
                            viewModel.deleteSchedule(schedule)
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
                            viewModel.deleteDebt(debt)
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
}
