package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "account_balance")
data class AccountBalanceEntity(
    @PrimaryKey val id: Int = 1,
    val cashBalance: Long = 0L,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "financial_schedules")
data class FinancialScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val nominal: Long,
    val type: String, // "INCOME" or "EXPENSE"
    val recurrence: String, // "ONCE", "BI_WEEKLY", "MONTHLY", "CUSTOM_DAYS"
    val intervalDays: Int = 14,
    val startDate: Long,
    val nextDueDate: Long,
    val notificationNote: String = "Waktunya jadwal finansial!",
    val isCompleted: Boolean = false,
    val lastExecutedDate: Long? = null
)

@Entity(tableName = "debts_and_receivables")
data class DebtReceivableEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val personName: String,
    val type: String, // "PIUTANG" (orang punya hutang ke kita = aset) or "HUTANG" (kita punya hutang ke orang)
    val originalAmount: Long,
    val remainingAmount: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val dueDate: Long? = null,
    val notes: String = "",
    val isFullySettled: Boolean = false
)

@Entity(tableName = "transactions")
data class TransactionItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val amount: Long, // Nominal dalam Rupiah
    val type: String, // "INCOME", "EXPENSE", "SALARY_CONFIRMED", "BILL_PAID", "MANUAL_ADJUSTMENT", "RECEIVABLE_RECEIVED", "DEBT_PAID"
    val reasonOrNote: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val relatedId: Long? = null
)

object Constants {
    const val TYPE_INCOME = "INCOME"
    const val TYPE_EXPENSE = "EXPENSE"

    const val REC_ONCE = "ONCE"
    const val REC_BI_WEEKLY = "BI_WEEKLY"
    const val REC_MONTHLY = "MONTHLY"
    const val REC_CUSTOM = "CUSTOM_DAYS"

    const val DEBT_PIUTANG = "PIUTANG" // Uang kita di orang lain (Aset)
    const val DEBT_HUTANG = "HUTANG"   // Uang orang lain di kita (Kewajiban)

    const val TX_SALARY_CONFIRMED = "SALARY_CONFIRMED"
    const val TX_BILL_PAID = "BILL_PAID"
    const val TX_MANUAL_ADJUSTMENT = "MANUAL_ADJUSTMENT"
    const val TX_RECEIVABLE_RECEIVED = "RECEIVABLE_RECEIVED"
    const val TX_DEBT_PAID = "DEBT_PAID"
}
