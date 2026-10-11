package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Constants
import com.example.ui.theme.BankingBluePrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.util.CurrencyUtils
import com.example.util.DateUtils
import java.util.Calendar

@Composable
fun AddScheduleDialog(
    initialDateMillis: Long = System.currentTimeMillis(),
    initialType: String = Constants.TYPE_EXPENSE,
    isTypeFixed: Boolean = false,
    dialogTitle: String = if (initialType == Constants.TYPE_INCOME) "Jadwal Pemasukan / Gajian" else "Tambah Tagihan Baru",
    onConfirm: (
        title: String,
        amount: Long,
        type: String,
        recurrence: String,
        intervalDays: Int,
        startDate: Long,
        notificationNote: String
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountInput by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(initialType) }
    var selectedRecurrence by remember {
        mutableStateOf(if (initialType == Constants.TYPE_INCOME) Constants.REC_BI_WEEKLY else Constants.REC_MONTHLY)
    }
    var customDaysInput by remember { mutableStateOf("14") }
    var notificationNote by remember {
        mutableStateOf(if (initialType == Constants.TYPE_INCOME) "Waktunya gajian!" else "Bayar sebelum jatuh tempo")
    }

    // Date & Time state
    var selectedDateMillis by remember { mutableLongStateOf(initialDateMillis) }
    var selectedHour by remember {
        val cal = Calendar.getInstance().apply { timeInMillis = initialDateMillis }
        mutableIntStateOf(if (initialType == Constants.TYPE_INCOME) 8 else cal.get(Calendar.HOUR_OF_DAY))
    }
    var selectedMinute by remember { mutableIntStateOf(0) }

    val parsedAmount = CurrencyUtils.parseRupiahInput(amountInput)
    val isValid = title.isNotBlank() && parsedAmount > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = if (selectedType == Constants.TYPE_INCOME) IncomeGreen else ExpenseRed,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = dialogTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Type selector (only show if not fixed)
                if (!isTypeFixed) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedType == Constants.TYPE_EXPENSE,
                            onClick = {
                                selectedType = Constants.TYPE_EXPENSE
                                if (notificationNote.startsWith("Waktunya gajian")) {
                                    notificationNote = "Bayar sebelum jatuh tempo"
                                }
                            },
                            label = { Text("Pengeluaran / Tagihan") },
                            modifier = Modifier.weight(1f).testTag("chip_type_expense")
                        )
                        FilterChip(
                            selected = selectedType == Constants.TYPE_INCOME,
                            onClick = {
                                selectedType = Constants.TYPE_INCOME
                                if (notificationNote.startsWith("Bayar")) {
                                    notificationNote = "Waktunya gajian!"
                                }
                            },
                            label = { Text("Pemasukan / Gajian") },
                            modifier = Modifier.weight(1f).testTag("chip_type_income")
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (selectedType == Constants.TYPE_INCOME) "Nama Pemasukan / Gaji*" else "Nama Tagihan / Pengeluaran*") },
                    placeholder = {
                        Text(if (selectedType == Constants.TYPE_INCOME) "Misal: Gaji Pokok Kantor" else "Misal: Tagihan Wifi / Listrik")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_schedule_title")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Nominal Rupiah*") },
                    placeholder = { Text(if (selectedType == Constants.TYPE_INCOME) "Misal: 5000000 atau 5jt" else "Misal: 150000 atau 150rb") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    supportingText = {
                        Text("Terbaca: ${CurrencyUtils.formatRupiah(parsedAmount)}")
                    },
                    modifier = Modifier.fillMaxWidth().testTag("input_schedule_amount")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Frekuensi Rutin:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedRecurrence == Constants.REC_MONTHLY,
                        onClick = { selectedRecurrence = Constants.REC_MONTHLY },
                        label = { Text("Bulanan") },
                        modifier = Modifier.testTag("chip_rec_monthly")
                    )
                    FilterChip(
                        selected = selectedRecurrence == Constants.REC_BI_WEEKLY,
                        onClick = { selectedRecurrence = Constants.REC_BI_WEEKLY },
                        label = { Text("2 Minggu") },
                        modifier = Modifier.testTag("chip_rec_biweekly")
                    )
                    FilterChip(
                        selected = selectedRecurrence == Constants.REC_ONCE,
                        onClick = { selectedRecurrence = Constants.REC_ONCE },
                        label = { Text("Sekali") },
                        modifier = Modifier.testTag("chip_rec_once")
                    )
                    FilterChip(
                        selected = selectedRecurrence == Constants.REC_CUSTOM,
                        onClick = { selectedRecurrence = Constants.REC_CUSTOM },
                        label = { Text("Custom") },
                        modifier = Modifier.testTag("chip_rec_custom")
                    )
                }

                if (selectedRecurrence == Constants.REC_CUSTOM) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customDaysInput,
                        onValueChange = { customDaysInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Interval Rutin (Hari)") },
                        placeholder = { Text("Contoh: 14 atau 21") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_custom_days")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Date & Hour Setting
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = BankingBluePrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Jatuh Tempo / Waktu Tiba:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick date choices
                        val nowCal = Calendar.getInstance()
                        val todayMillis = nowCal.timeInMillis
                        val tomorrowMillis = (nowCal.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }.timeInMillis
                        val next7DaysMillis = (nowCal.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 7) }.timeInMillis
                        val payday25Millis = (nowCal.clone() as Calendar).apply {
                            set(Calendar.DAY_OF_MONTH, 25)
                            if (timeInMillis < todayMillis) add(Calendar.MONTH, 1)
                        }.timeInMillis

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = DateUtils.isSameDay(selectedDateMillis, todayMillis),
                                onClick = { selectedDateMillis = todayMillis },
                                label = { Text("Hari Ini") }
                            )
                            FilterChip(
                                selected = DateUtils.isSameDay(selectedDateMillis, tomorrowMillis),
                                onClick = { selectedDateMillis = tomorrowMillis },
                                label = { Text("Besok") }
                            )
                            FilterChip(
                                selected = DateUtils.isSameDay(selectedDateMillis, payday25Millis),
                                onClick = { selectedDateMillis = payday25Millis },
                                label = { Text("Tgl 25") }
                            )
                            FilterChip(
                                selected = DateUtils.isSameDay(selectedDateMillis, next7DaysMillis),
                                onClick = { selectedDateMillis = next7DaysMillis },
                                label = { Text("+7 Hari") }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Hour selection
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = BankingBluePrimary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Jam Pelaksanaan:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                Pair(8, "08:00 (Pagi)"),
                                Pair(12, "12:00 (Siang)"),
                                Pair(17, "17:00 (Sore)"),
                                Pair(20, "20:00 (Malam)"),
                                Pair(nowCal.get(Calendar.HOUR_OF_DAY), "Jam Sekarang")
                            ).forEach { (h, lbl) ->
                                FilterChip(
                                    selected = selectedHour == h,
                                    onClick = { selectedHour = h },
                                    label = { Text(lbl) }
                                )
                            }
                        }

                        // Compute resulting timestamp preview
                        val finalCalendar = Calendar.getInstance().apply {
                            timeInMillis = selectedDateMillis
                            set(Calendar.HOUR_OF_DAY, selectedHour)
                            set(Calendar.MINUTE, selectedMinute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BankingBluePrimary.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, BankingBluePrimary.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Waktu Dijadwalkan: ${DateUtils.formatDateTimeIndo(finalCalendar.timeInMillis)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = BankingBluePrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = notificationNote,
                    onValueChange = { notificationNote = it },
                    label = { Text("Catatan / Pesan Pengingat") },
                    placeholder = { Text("Misal: Waktunya gajian! / Segera bayar sebelum denda") },
                    modifier = Modifier.fillMaxWidth().testTag("input_schedule_note")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val days = when (selectedRecurrence) {
                        Constants.REC_BI_WEEKLY -> 14
                        Constants.REC_MONTHLY -> 30
                        Constants.REC_CUSTOM -> customDaysInput.toIntOrNull() ?: 14
                        else -> 0
                    }

                    val finalCalendar = Calendar.getInstance().apply {
                        timeInMillis = selectedDateMillis
                        set(Calendar.HOUR_OF_DAY, selectedHour)
                        set(Calendar.MINUTE, selectedMinute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    onConfirm(
                        title.trim(),
                        parsedAmount,
                        selectedType,
                        selectedRecurrence,
                        days,
                        finalCalendar.timeInMillis,
                        notificationNote.trim()
                    )
                },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedType == Constants.TYPE_INCOME) IncomeGreen else ExpenseRed
                ),
                modifier = Modifier.testTag("btn_save_schedule")
            ) {
                Text("Simpan Jadwal")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancel_schedule")
            ) {
                Text("Batal")
            }
        }
    )
}
