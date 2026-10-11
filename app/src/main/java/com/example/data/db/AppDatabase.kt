package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AccountBalanceEntity
import com.example.data.model.Constants
import com.example.data.model.DebtReceivableEntity
import com.example.data.model.FinancialScheduleEntity
import com.example.data.model.TransactionItemEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        AccountBalanceEntity::class,
        FinancialScheduleEntity::class,
        DebtReceivableEntity::class,
        TransactionItemEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun financialDao(): FinancialDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pencatat_keuangan.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                seedStarterData()
            }

            override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                super.onDestructiveMigration(db)
                seedStarterData()
            }

            private fun seedStarterData() {
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        val dao = database.financialDao()
                        val now = System.currentTimeMillis()

                        // Saldo Kas Bersih (Rp 0) siap digunakan oleh pengguna
                        dao.setBalance(
                            AccountBalanceEntity(
                                id = 1,
                                cashBalance = 0L,
                                lastUpdated = now
                            )
                        )

                        // Hanya 1 contoh tagihan terjadwal agar pengguna baru memahami alur ceklis & kalender
                        dao.insertSchedule(
                            FinancialScheduleEntity(
                                title = "Contoh: Langganan Wifi / Internet",
                                nominal = 150_000L,
                                type = Constants.TYPE_EXPENSE,
                                recurrence = Constants.REC_MONTHLY,
                                intervalDays = 30,
                                startDate = now,
                                nextDueDate = now + (3L * 24 * 60 * 60 * 1000), // Jatuh tempo 3 hari lagi
                                notificationNote = "Contoh tagihan bulanan: centang saat sudah lunas",
                                isCompleted = false
                            )
                        )
                    }
                }
            }
        }
    }
}
