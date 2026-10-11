package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AccountBalanceEntity
import com.example.data.model.DebtReceivableEntity
import com.example.data.model.FinancialScheduleEntity
import com.example.data.model.TransactionItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FinancialDao {

    // Balance
    @Query("SELECT * FROM account_balance WHERE id = 1 LIMIT 1")
    fun getBalanceFlow(): Flow<AccountBalanceEntity?>

    @Query("SELECT * FROM account_balance WHERE id = 1 LIMIT 1")
    suspend fun getBalanceSync(): AccountBalanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setBalance(balance: AccountBalanceEntity)

    // Schedules
    @Query("SELECT * FROM financial_schedules ORDER BY nextDueDate ASC")
    fun getAllSchedulesFlow(): Flow<List<FinancialScheduleEntity>>

    @Query("SELECT * FROM financial_schedules WHERE type = 'EXPENSE' AND isCompleted = 0 ORDER BY nextDueDate ASC")
    fun getPendingExpensesFlow(): Flow<List<FinancialScheduleEntity>>

    @Query("SELECT * FROM financial_schedules WHERE id = :id LIMIT 1")
    suspend fun getScheduleById(id: Long): FinancialScheduleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: FinancialScheduleEntity): Long

    @Update
    suspend fun updateSchedule(schedule: FinancialScheduleEntity)

    @Delete
    suspend fun deleteSchedule(schedule: FinancialScheduleEntity)

    // Debts & Receivables
    @Query("SELECT * FROM debts_and_receivables ORDER BY isFullySettled ASC, createdAt DESC")
    fun getAllDebtsFlow(): Flow<List<DebtReceivableEntity>>

    @Query("SELECT * FROM debts_and_receivables WHERE id = :id LIMIT 1")
    suspend fun getDebtById(id: Long): DebtReceivableEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: DebtReceivableEntity): Long

    @Update
    suspend fun updateDebt(debt: DebtReceivableEntity)

    @Delete
    suspend fun deleteDebt(debt: DebtReceivableEntity)

    // Transactions
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactionsFlow(): Flow<List<TransactionItemEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentTransactionsFlow(limit: Int = 10): Flow<List<TransactionItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionItemEntity): Long

    @Delete
    suspend fun deleteTransaction(transaction: TransactionItemEntity)

    // Clear Data / Reset
    @Query("DELETE FROM account_balance")
    suspend fun clearBalance()

    @Query("DELETE FROM financial_schedules")
    suspend fun clearSchedules()

    @Query("DELETE FROM debts_and_receivables")
    suspend fun clearDebts()

    @Query("DELETE FROM transactions")
    suspend fun clearTransactions()

    @androidx.room.Transaction
    suspend fun clearAllDataAndResetBalance() {
        clearBalance()
        clearSchedules()
        clearDebts()
        clearTransactions()
        setBalance(
            AccountBalanceEntity(
                id = 1,
                cashBalance = 0L,
                lastUpdated = System.currentTimeMillis()
            )
        )
    }
}
