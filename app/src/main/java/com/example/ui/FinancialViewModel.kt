package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.Constants
import com.example.data.model.DebtReceivableEntity
import com.example.data.model.FinancialScheduleEntity
import com.example.data.model.TransactionItemEntity
import com.example.data.repository.FinancialRepository
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class FinancialUiState(
    val cashBalance: Long = 0L,
    val totalPiutang: Long = 0L, // Aset di orang lain
    val totalHutang: Long = 0L,  // Tanggungan kita
    val netWorth: Long = 0L,
    val schedules: List<FinancialScheduleEntity> = emptyList(),
    val duePendingExpenses: List<FinancialScheduleEntity> = emptyList(), // Tagihan yang perlu dibayar sekarang / mendekati tenggat
    val upcomingExpenses: List<FinancialScheduleEntity> = emptyList(),   // Tagihan bulan berikutnya (sudah lunas bulan ini)
    val debtsAndReceivables: List<DebtReceivableEntity> = emptyList(),
    val recentTransactions: List<TransactionItemEntity> = emptyList(),
    val allTransactions: List<TransactionItemEntity> = emptyList(),
    val activeSalaryPrompts: List<FinancialScheduleEntity> = emptyList(),
    val currentCalendarYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val currentCalendarMonth: Int = Calendar.getInstance().get(Calendar.MONTH),
    val selectedCalendarDate: Long = System.currentTimeMillis(),
    val isInitialized: Boolean = false
) {
    // Backward compatibility helper
    val pendingExpenses: List<FinancialScheduleEntity>
        get() = duePendingExpenses
}

class FinancialViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinancialRepository
    private val dismissedSalaryPrompts = MutableStateFlow<Set<Long>>(emptySet())
    private val calendarYearState = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    private val calendarMonthState = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH))
    private val selectedDateState = MutableStateFlow(System.currentTimeMillis())

    init {
        val db = AppDatabase.getInstance(application)
        repository = FinancialRepository(db.financialDao())
    }

    val uiState: StateFlow<FinancialUiState> = combine(
        repository.balanceFlow,
        repository.allSchedulesFlow,
        repository.allDebtsFlow,
        repository.recentTransactionsFlow,
        repository.allTransactionsFlow,
        dismissedSalaryPrompts,
        calendarYearState,
        calendarMonthState,
        selectedDateState
    ) { params ->
        val balanceEntity = params[0] as? com.example.data.model.AccountBalanceEntity
        @Suppress("UNCHECKED_CAST")
        val schedules = params[1] as? List<FinancialScheduleEntity> ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val debts = params[2] as? List<DebtReceivableEntity> ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val recentTx = params[3] as? List<TransactionItemEntity> ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val allTx = params[4] as? List<TransactionItemEntity> ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val dismissedSalaryIds = params[5] as? Set<Long> ?: emptySet()
        val year = params[6] as Int
        val month = params[7] as Int
        val selectedDate = params[8] as Long

        val cash = balanceEntity?.cashBalance ?: 0L
        val now = System.currentTimeMillis()
        val sevenDaysAhead = now + (7L * 24 * 60 * 60 * 1000)

        // Piutang: aset di orang lain (aktif / belum lunas)
        val piutangTotal = debts
            .filter { it.type == Constants.DEBT_PIUTANG && !it.isFullySettled }
            .sumOf { it.remainingAmount }

        // Hutang: tanggungan kita
        val hutangTotal = debts
            .filter { it.type == Constants.DEBT_HUTANG && !it.isFullySettled }
            .sumOf { it.remainingAmount }

        val net = cash + piutangTotal - hutangTotal

        // Separating expenses into:
        // 1. Due/Approaching: one-time pending OR recurring due within 7 days / past due
        // 2. Upcoming: recurring items whose current month/cycle has already been paid and next due is further ahead
        val allUncompletedExpenses = schedules.filter { it.type == Constants.TYPE_EXPENSE && !it.isCompleted }

        val dueExpenses = allUncompletedExpenses.filter { schedule ->
            if (schedule.recurrence == Constants.REC_ONCE) {
                true
            } else {
                // Recurring: only show if due today, overdue, or due within next 7 days
                schedule.nextDueDate <= sevenDaysAhead
            }
        }.sortedBy { it.nextDueDate }

        val upcoming = allUncompletedExpenses.filter { schedule ->
            schedule.recurrence != Constants.REC_ONCE && schedule.nextDueDate > sevenDaysAhead
        }.sortedBy { it.nextDueDate }

        // Salary prompt: scheduled income that is due or past due today, not yet dismissed in current session
        val dueSalaryList = schedules.filter { schedule ->
            schedule.type == Constants.TYPE_INCOME &&
                    !schedule.isCompleted &&
                    DateUtils.isDueOrPast(schedule.nextDueDate) &&
                    !dismissedSalaryIds.contains(schedule.id)
        }

        FinancialUiState(
            cashBalance = cash,
            totalPiutang = piutangTotal,
            totalHutang = hutangTotal,
            netWorth = net,
            schedules = schedules,
            duePendingExpenses = dueExpenses,
            upcomingExpenses = upcoming,
            debtsAndReceivables = debts,
            recentTransactions = recentTx,
            allTransactions = allTx,
            activeSalaryPrompts = dueSalaryList,
            currentCalendarYear = year,
            currentCalendarMonth = month,
            selectedCalendarDate = selectedDate,
            isInitialized = true
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FinancialUiState()
    )

    fun confirmSalaryReceived(scheduleId: Long, actualAmount: Long, note: String) {
        viewModelScope.launch {
            repository.confirmSalaryReceived(scheduleId, actualAmount, note)
            dismissedSalaryPrompts.update { it + scheduleId }
        }
    }

    fun dismissSalaryPrompt(scheduleId: Long) {
        dismissedSalaryPrompts.update { it + scheduleId }
    }

    fun payExpenseChecklist(scheduleId: Long, periodsCount: Int = 1, note: String = "") {
        viewModelScope.launch {
            repository.markExpenseAsPaid(scheduleId, periodsCount, note)
        }
    }

    fun adjustCashBalance(targetBalance: Long, reason: String) {
        viewModelScope.launch {
            repository.adjustCashBalance(targetBalance, reason)
        }
    }

    fun settleDebtOrReceivable(debtId: Long, paymentAmount: Long, note: String) {
        viewModelScope.launch {
            repository.settleDebt(debtId, paymentAmount, note)
        }
    }

    fun addSchedule(
        title: String,
        amount: Long,
        type: String,
        recurrence: String,
        intervalDays: Int,
        startDate: Long,
        notificationNote: String
    ) {
        viewModelScope.launch {
            val schedule = FinancialScheduleEntity(
                title = title,
                nominal = amount,
                type = type,
                recurrence = recurrence,
                intervalDays = intervalDays,
                startDate = startDate,
                nextDueDate = startDate,
                notificationNote = notificationNote.ifBlank { "Waktunya $title!" },
                isCompleted = false
            )
            repository.createSchedule(schedule)
        }
    }

    fun deleteSchedule(schedule: FinancialScheduleEntity) {
        viewModelScope.launch {
            repository.deleteSchedule(schedule)
        }
    }

    fun addDebtOrReceivable(
        personName: String,
        type: String,
        amount: Long,
        dueDate: Long?,
        notes: String
    ) {
        viewModelScope.launch {
            val item = DebtReceivableEntity(
                personName = personName,
                type = type,
                originalAmount = amount,
                remainingAmount = amount,
                createdAt = System.currentTimeMillis(),
                dueDate = dueDate,
                notes = notes,
                isFullySettled = false
            )
            repository.createDebt(item)
        }
    }

    fun deleteDebt(debt: DebtReceivableEntity) {
        viewModelScope.launch {
            repository.deleteDebt(debt)
        }
    }

    fun changeCalendarMonth(delta: Int) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, calendarYearState.value)
            set(Calendar.MONTH, calendarMonthState.value)
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, delta)
        }
        calendarYearState.value = cal.get(Calendar.YEAR)
        calendarMonthState.value = cal.get(Calendar.MONTH)
    }

    fun selectCalendarDate(dateMillis: Long) {
        selectedDateState.value = dateMillis
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            dismissedSalaryPrompts.value = emptySet()
        }
    }

    fun resetToSingleSample() {
        viewModelScope.launch {
            repository.resetToSingleSample()
            dismissedSalaryPrompts.value = emptySet()
        }
    }
}
