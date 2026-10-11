package com.example.data.repository

import com.example.data.db.FinancialDao
import com.example.data.model.AccountBalanceEntity
import com.example.data.model.Constants
import com.example.data.model.DebtReceivableEntity
import com.example.data.model.FinancialScheduleEntity
import com.example.data.model.TransactionItemEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class FinancialRepository(private val dao: FinancialDao) {

    val balanceFlow: Flow<AccountBalanceEntity?> = dao.getBalanceFlow()
    val allSchedulesFlow: Flow<List<FinancialScheduleEntity>> = dao.getAllSchedulesFlow()
    val pendingExpensesFlow: Flow<List<FinancialScheduleEntity>> = dao.getPendingExpensesFlow()
    val allDebtsFlow: Flow<List<DebtReceivableEntity>> = dao.getAllDebtsFlow()
    val recentTransactionsFlow: Flow<List<TransactionItemEntity>> = dao.getRecentTransactionsFlow(15)
    val allTransactionsFlow: Flow<List<TransactionItemEntity>> = dao.getAllTransactionsFlow()

    suspend fun getOrCreateBalance(): AccountBalanceEntity {
        val existing = dao.getBalanceSync()
        if (existing != null) return existing
        val newBal = AccountBalanceEntity(id = 1, cashBalance = 0L, lastUpdated = System.currentTimeMillis())
        dao.setBalance(newBal)
        return newBal
    }

    suspend fun adjustCashBalance(targetBalance: Long, reason: String) {
        val current = getOrCreateBalance()
        val diff = targetBalance - current.cashBalance
        val now = System.currentTimeMillis()

        dao.setBalance(current.copy(cashBalance = targetBalance, lastUpdated = now))

        dao.insertTransaction(
            TransactionItemEntity(
                title = if (diff >= 0) "Penambahan Saldo Kas" else "Pengurangan Saldo Kas",
                amount = diff,
                type = Constants.TX_MANUAL_ADJUSTMENT,
                reasonOrNote = reason.ifBlank { "Penyesuaian saldo manual" },
                timestamp = now
            )
        )
    }

    suspend fun addCashBalanceDirect(amount: Long, title: String, reason: String) {
        val current = getOrCreateBalance()
        val target = current.cashBalance + amount
        val now = System.currentTimeMillis()

        dao.setBalance(current.copy(cashBalance = target, lastUpdated = now))

        dao.insertTransaction(
            TransactionItemEntity(
                title = title,
                amount = amount,
                type = if (amount >= 0) Constants.TYPE_INCOME else Constants.TYPE_EXPENSE,
                reasonOrNote = reason,
                timestamp = now
            )
        )
    }

    suspend fun confirmSalaryReceived(
        scheduleId: Long,
        actualAmount: Long,
        reasonOrNote: String
    ) {
        val schedule = dao.getScheduleById(scheduleId) ?: return
        val current = getOrCreateBalance()
        val now = System.currentTimeMillis()

        // 1. Tambah saldo
        val newBalance = current.cashBalance + actualAmount
        dao.setBalance(current.copy(cashBalance = newBalance, lastUpdated = now))

        // 2. Catat riwayat transaksi
        dao.insertTransaction(
            TransactionItemEntity(
                title = "Gaji Diterima: ${schedule.title}",
                amount = actualAmount,
                type = Constants.TX_SALARY_CONFIRMED,
                reasonOrNote = reasonOrNote.ifBlank { "Konfirmasi penerimaan gaji terjadwal" },
                timestamp = now,
                relatedId = scheduleId
            )
        )

        // 3. Update jadwal ke siklus berikutnya jika berulang
        val updatedSchedule = when (schedule.recurrence) {
            Constants.REC_BI_WEEKLY -> {
                val nextDue = calculateNextDate(schedule.nextDueDate, 14)
                schedule.copy(
                    nextDueDate = nextDue,
                    lastExecutedDate = now
                )
            }
            Constants.REC_MONTHLY -> {
                val nextDue = calculateNextMonth(schedule.nextDueDate)
                schedule.copy(
                    nextDueDate = nextDue,
                    lastExecutedDate = now
                )
            }
            Constants.REC_CUSTOM -> {
                val days = if (schedule.intervalDays > 0) schedule.intervalDays else 14
                val nextDue = calculateNextDate(schedule.nextDueDate, days)
                schedule.copy(
                    nextDueDate = nextDue,
                    lastExecutedDate = now
                )
            }
            else -> { // ONCE
                schedule.copy(
                    isCompleted = true,
                    lastExecutedDate = now
                )
            }
        }
        dao.updateSchedule(updatedSchedule)
    }

    suspend fun markExpenseAsPaid(scheduleId: Long, periodsCount: Int = 1, note: String = "") {
        val schedule = dao.getScheduleById(scheduleId) ?: return
        val current = getOrCreateBalance()
        val now = System.currentTimeMillis()
        val safeCount = periodsCount.coerceAtLeast(1)
        val totalDeduction = schedule.nominal * safeCount

        // Potong saldo
        val newBalance = current.cashBalance - totalDeduction
        dao.setBalance(current.copy(cashBalance = newBalance, lastUpdated = now))

        // Catat transaksi
        val titleStr = if (safeCount > 1) {
            "Bayar Tagihan: ${schedule.title} ($safeCount Bulan Langsung)"
        } else {
            "Bayar Tagihan: ${schedule.title}"
        }
        val noteStr = note.ifBlank {
            if (safeCount > 1) "Pembayaran di muka untuk $safeCount periode"
            else "Konfirmasi ceklis pembayaran tagihan"
        }

        dao.insertTransaction(
            TransactionItemEntity(
                title = titleStr,
                amount = -totalDeduction,
                type = Constants.TX_BILL_PAID,
                reasonOrNote = noteStr,
                timestamp = now,
                relatedId = scheduleId
            )
        )

        // Update schedule status & next due date
        val updated = if (schedule.recurrence == Constants.REC_ONCE) {
            schedule.copy(isCompleted = true, lastExecutedDate = now)
        } else {
            val nextDue = when (schedule.recurrence) {
                Constants.REC_BI_WEEKLY -> calculateNextDate(schedule.nextDueDate, 14 * safeCount)
                Constants.REC_MONTHLY -> calculateNextMonth(schedule.nextDueDate, safeCount)
                Constants.REC_CUSTOM -> {
                    val days = if (schedule.intervalDays > 0) schedule.intervalDays else 30
                    calculateNextDate(schedule.nextDueDate, days * safeCount)
                }
                else -> calculateNextMonth(schedule.nextDueDate, safeCount)
            }
            schedule.copy(nextDueDate = nextDue, lastExecutedDate = now)
        }
        dao.updateSchedule(updated)
    }

    suspend fun settleDebt(
        debtId: Long,
        paymentAmount: Long,
        note: String
    ) {
        val debt = dao.getDebtById(debtId) ?: return
        val current = getOrCreateBalance()
        val now = System.currentTimeMillis()

        val newRemaining = (debt.remainingAmount - paymentAmount).coerceAtLeast(0L)
        val isSettled = newRemaining == 0L

        dao.updateDebt(
            debt.copy(
                remainingAmount = newRemaining,
                isFullySettled = isSettled
            )
        )

        if (debt.type == Constants.DEBT_PIUTANG) {
            // Uang masuk ke kita -> saldo kas bertambah
            val newBalance = current.cashBalance + paymentAmount
            dao.setBalance(current.copy(cashBalance = newBalance, lastUpdated = now))

            dao.insertTransaction(
                TransactionItemEntity(
                    title = "Pelunasan Piutang: ${debt.personName}",
                    amount = paymentAmount,
                    type = Constants.TX_RECEIVABLE_RECEIVED,
                    reasonOrNote = if (isSettled) "Lunas: $note" else "Cicilan (${newRemaining.toRupiah()} tersisa): $note",
                    timestamp = now,
                    relatedId = debtId
                )
            )
        } else {
            // Kita bayar hutang ke orang -> saldo kas berkurang
            val newBalance = current.cashBalance - paymentAmount
            dao.setBalance(current.copy(cashBalance = newBalance, lastUpdated = now))

            dao.insertTransaction(
                TransactionItemEntity(
                    title = "Bayar Hutang: ${debt.personName}",
                    amount = -paymentAmount,
                    type = Constants.TX_DEBT_PAID,
                    reasonOrNote = if (isSettled) "Lunas: $note" else "Cicilan tersisa ${newRemaining.toRupiah()}: $note",
                    timestamp = now,
                    relatedId = debtId
                )
            )
        }
    }

    suspend fun createSchedule(schedule: FinancialScheduleEntity): Long {
        return dao.insertSchedule(schedule)
    }

    suspend fun updateSchedule(schedule: FinancialScheduleEntity) {
        dao.updateSchedule(schedule)
    }

    suspend fun deleteSchedule(schedule: FinancialScheduleEntity) {
        dao.deleteSchedule(schedule)
    }

    suspend fun createDebt(debt: DebtReceivableEntity): Long {
        return dao.insertDebt(debt)
    }

    suspend fun updateDebt(debt: DebtReceivableEntity) {
        dao.updateDebt(debt)
    }

    suspend fun deleteDebt(debt: DebtReceivableEntity) {
        dao.deleteDebt(debt)
    }

    suspend fun clearAllData() {
        dao.clearAllDataAndResetBalance()
    }

    suspend fun resetToSingleSample() {
        clearAllData()
        val now = System.currentTimeMillis()
        dao.insertSchedule(
            FinancialScheduleEntity(
                title = "Contoh: Langganan Wifi / Internet",
                nominal = 150_000L,
                type = Constants.TYPE_EXPENSE,
                recurrence = Constants.REC_MONTHLY,
                intervalDays = 30,
                startDate = now,
                nextDueDate = now + (3L * 24 * 60 * 60 * 1000),
                notificationNote = "Contoh tagihan bulanan: centang saat sudah lunas",
                isCompleted = false
            )
        )
    }

    private fun calculateNextDate(fromMillis: Long, daysToAdd: Int): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = fromMillis
        cal.add(Calendar.DAY_OF_YEAR, daysToAdd)
        return cal.timeInMillis
    }

    private fun calculateNextMonth(fromMillis: Long, monthsToAdd: Int = 1): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = fromMillis
        cal.add(Calendar.MONTH, monthsToAdd)
        return cal.timeInMillis
    }

    private fun Long.toRupiah(): String {
        return "Rp " + java.text.NumberFormat.getIntegerInstance(java.util.Locale("id", "ID")).format(this)
    }
}
