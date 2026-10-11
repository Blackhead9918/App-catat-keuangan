package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Constants
import com.example.data.model.FinancialScheduleEntity
import com.example.ui.FinancialUiState
import com.example.ui.components.AddScheduleDialog
import com.example.ui.components.SalaryPromptDialog
import com.example.ui.theme.BankingBluePrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.util.CalendarDay
import com.example.util.CurrencyUtils
import com.example.util.DateUtils
import java.util.Calendar

@Composable
fun CalendarScreen(
    uiState: FinancialUiState,
    onChangeMonth: (delta: Int) -> Unit,
    onSelectDate: (dateMillis: Long) -> Unit,
    onAddSchedule: (
        title: String,
        amount: Long,
        type: String,
        recurrence: String,
        intervalDays: Int,
        startDate: Long,
        notificationNote: String
    ) -> Unit,
    onConfirmSalary: (scheduleId: Long, actualAmount: Long, note: String) -> Unit,
    onDismissSalary: (scheduleId: Long) -> Unit,
    onPayExpense: (scheduleId: Long) -> Unit,
    onDeleteSchedule: (schedule: FinancialScheduleEntity) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var activeSalarySchedule by remember { mutableStateOf<FinancialScheduleEntity?>(null) }

    val currentCal = Calendar.getInstance().apply {
        set(Calendar.YEAR, uiState.currentCalendarYear)
        set(Calendar.MONTH, uiState.currentCalendarMonth)
        set(Calendar.DAY_OF_MONTH, 1)
    }

    val daysInMonth = remember(uiState.currentCalendarYear, uiState.currentCalendarMonth) {
        DateUtils.getMonthDays(uiState.currentCalendarYear, uiState.currentCalendarMonth)
    }

    // Schedules mapped to selected date
    val schedulesOnSelectedDate = uiState.schedules.filter { schedule ->
        DateUtils.isSameDay(schedule.nextDueDate, uiState.selectedCalendarDate)
    }

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
                    Column {
                        Text(
                            text = "KALENDER FINANSIAL",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Sinkron dengan jadwal gajian & tagihan nyata",
                            fontSize = 11.sp,
                            color = Color(0xFFDBEAFE)
                        )
                    }

                    Surface(
                        onClick = { onSelectDate(System.currentTimeMillis()) },
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.testTag("btn_today")
                    ) {
                        Text(
                            text = "Hari Ini",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("calendar_screen_content"),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {

            // Month Navigation Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { onChangeMonth(-1) },
                                modifier = Modifier.testTag("btn_prev_month")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Bulan Sebelumnya"
                                )
                            }

                            Text(
                                text = DateUtils.formatMonthYear(currentCal),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            IconButton(
                                onClick = { onChangeMonth(1) },
                                modifier = Modifier.testTag("btn_next_month")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Bulan Berikutnya"
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Day headers
                        Row(modifier = Modifier.fillMaxWidth()) {
                            val daysOfWeek = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
                            daysOfWeek.forEach { dayName ->
                                Text(
                                    text = dayName,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Calendar Grid (7 columns per row)
                        val chunkedDays = daysInMonth.chunked(7)
                        chunkedDays.forEach { week ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                week.forEach { day ->
                                    CalendarDayCell(
                                        day = day,
                                        isSelected = DateUtils.isSameDay(day.timeInMillis, uiState.selectedCalendarDate),
                                        schedules = uiState.schedules,
                                        onClick = { onSelectDate(day.timeInMillis) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Legend dots
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DotLegend(color = IncomeGreen, label = "Gajian / Pemasukan")
                    Spacer(modifier = Modifier.width(16.dp))
                    DotLegend(color = ExpenseRed, label = "Tagihan / Pengeluaran")
                }
            }

            // Agenda on Selected Date
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Agenda ${DateUtils.formatDateIndo(uiState.selectedCalendarDate)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${schedulesOnSelectedDate.size} jadwal di tanggal ini",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { showAddDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BankingBluePrimary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("btn_add_schedule_for_date")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tambah", fontSize = 12.sp)
                    }
                }
            }

            if (schedulesOnSelectedDate.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tidak ada jadwal finansial di tanggal ini.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { showAddDialog = true },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Buat Jadwal Baru di Tanggal Ini", fontSize = 12.sp)
                            }
                        }
                    }
                }
            } else {
                items(schedulesOnSelectedDate, key = { "cal_sched_${it.id}" }) { schedule ->
                    ScheduleCardItem(
                        schedule = schedule,
                        onConfirmSalary = { activeSalarySchedule = schedule },
                        onPayExpense = { onPayExpense(schedule.id) },
                        onDelete = { onDeleteSchedule(schedule) }
                    )
                }
            }

            // All Recurring Schedules Overview
            val recurringList = uiState.schedules.filter { it.recurrence != Constants.REC_ONCE }
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Daftar Rutinitas Finansial Aktif",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }

            if (recurringList.isEmpty()) {
                item {
                    Text(
                        text = "Belum ada jadwal berulang (seperti gajian 2 mingguan).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                }
            } else {
                items(recurringList, key = { "rec_${it.id}" }) { schedule ->
                    RecurringOverviewCard(
                        schedule = schedule,
                        onDelete = { onDeleteSchedule(schedule) }
                    )
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
                .testTag("fab_add_schedule")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Jadwal")
        }
    }

    if (showAddDialog) {
        AddScheduleDialog(
            initialDateMillis = uiState.selectedCalendarDate,
            onConfirm = { title, amount, type, recurrence, intervalDays, startDate, notificationNote ->
                onAddSchedule(title, amount, type, recurrence, intervalDays, startDate, notificationNote)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    if (activeSalarySchedule != null) {
        SalaryPromptDialog(
            schedule = activeSalarySchedule!!,
            onConfirm = { scheduleId, actualAmount, note ->
                onConfirmSalary(scheduleId, actualAmount, note)
                activeSalarySchedule = null
            },
            onDismiss = { scheduleId ->
                onDismissSalary(scheduleId)
                activeSalarySchedule = null
            }
        )
    }
}

@Composable
private fun CalendarDayCell(
    day: CalendarDay,
    isSelected: Boolean,
    schedules: List<FinancialScheduleEntity>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val daySchedules = schedules.filter { DateUtils.isSameDay(it.nextDueDate, day.timeInMillis) }
    val hasIncome = daySchedules.any { it.type == Constants.TYPE_INCOME }
    val hasExpense = daySchedules.any { it.type == Constants.TYPE_EXPENSE }

    val textColor = when {
        isSelected -> Color.White
        day.isToday -> BankingBluePrimary
        day.isCurrentMonth -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }

    Column(
        modifier = modifier
            .padding(2.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    isSelected -> BankingBluePrimary
                    day.isToday -> BankingBluePrimary.copy(alpha = 0.12f)
                    else -> Color.Transparent
                }
            )
            .border(
                width = if (day.isToday && !isSelected) 1.dp else 0.dp,
                color = if (day.isToday && !isSelected) BankingBluePrimary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .testTag("cal_day_${day.dayOfMonth}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = day.dayOfMonth.toString(),
            fontSize = 13.sp,
            fontWeight = if (isSelected || day.isToday) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )

        // Event dots
        Row(
            modifier = Modifier.padding(top = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (hasIncome) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White else IncomeGreen)
                )
            }
            if (hasExpense) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White else ExpenseRed)
                )
            }
        }
    }
}

@Composable
private fun DotLegend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
    }
}

@Composable
private fun ScheduleCardItem(
    schedule: FinancialScheduleEntity,
    onConfirmSalary: () -> Unit,
    onPayExpense: () -> Unit,
    onDelete: () -> Unit
) {
    val isIncome = schedule.type == Constants.TYPE_INCOME
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .testTag("schedule_card_${schedule.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (isIncome) IncomeGreen.copy(alpha = 0.15f) else ExpenseRed.copy(alpha = 0.15f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isIncome) Icons.Default.CheckCircle else Icons.Default.Event,
                        contentDescription = null,
                        tint = if (isIncome) IncomeGreen else ExpenseRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = schedule.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${schedule.notificationNote} • ${getRecurrenceText(schedule)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = CurrencyUtils.formatRupiah(schedule.nominal),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isIncome) IncomeGreen else ExpenseRed
                )
            }

            if (isIncome && !schedule.isCompleted) {
                val isTimeReached = DateUtils.isDueExact(schedule.nextDueDate)
                if (isTimeReached) {
                    Button(
                        onClick = onConfirmSalary,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("btn_schedule_confirm_${schedule.id}")
                    ) {
                        Text("Terima", fontSize = 11.sp)
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.testTag("badge_schedule_locked_${schedule.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LockClock,
                                contentDescription = "Waktu belum tiba",
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Belum Tiba",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            } else if (!isIncome && !schedule.isCompleted) {
                Button(
                    onClick = onPayExpense,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("btn_schedule_pay_${schedule.id}")
                ) {
                    Text("Bayar", fontSize = 11.sp)
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Hapus Jadwal",
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun RecurringOverviewCard(
    schedule: FinancialScheduleEntity,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                imageVector = Icons.Default.Repeat,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${schedule.title} (${CurrencyUtils.formatRupiah(schedule.nominal)})",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${getRecurrenceText(schedule)} • Jatuh tempo: ${DateUtils.formatShortDate(schedule.nextDueDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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

private fun getRecurrenceText(schedule: FinancialScheduleEntity): String {
    return when (schedule.recurrence) {
        Constants.REC_BI_WEEKLY -> "Tiap 2 Minggu (14 Hari)"
        Constants.REC_MONTHLY -> "Setiap Bulan"
        Constants.REC_CUSTOM -> "Tiap ${schedule.intervalDays} Hari"
        else -> "Sekali Saja"
    }
}
