package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.FinancialScheduleEntity
import com.example.ui.theme.BankingBluePrimary
import com.example.util.CurrencyUtils
import com.example.util.DateUtils

@Composable
fun SalaryPromptDialog(
    schedule: FinancialScheduleEntity,
    onConfirm: (scheduleId: Long, actualAmount: Long, note: String) -> Unit,
    onDismiss: (scheduleId: Long) -> Unit
) {
    var isCustomAmount by remember { mutableStateOf(false) }
    var amountInput by remember { mutableStateOf(schedule.nominal.toString()) }
    var noteInput by remember { mutableStateOf("") }

    val parsedAmount = CurrencyUtils.parseRupiahInput(amountInput)
    val isTimeReached = DateUtils.isDueExact(schedule.nextDueDate)

    AlertDialog(
        onDismissRequest = { onDismiss(schedule.id) },
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = BankingBluePrimary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Waktunya Gajian!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Sudah menerima gaji \"${schedule.title}\"?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Rencana Nominal Terjadwal:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = CurrencyUtils.formatRupiah(schedule.nominal),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (schedule.notificationNote.isNotBlank()) {
                            Text(
                                text = "Catatan: ${schedule.notificationNote}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Nominal berbeda / ada potongan?",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = isCustomAmount,
                        onCheckedChange = { isCustomAmount = it },
                        modifier = Modifier.testTag("switch_custom_salary")
                    )
                }

                if (isCustomAmount) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        label = { Text("Nominal Aktual Diterima") },
                        placeholder = { Text("Contoh: 3500000 atau 3.5jt") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        supportingText = {
                            Text("Terbaca: ${CurrencyUtils.formatRupiah(parsedAmount)}")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_custom_salary_amount")
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = noteInput,
                        onValueChange = { noteInput = it },
                        label = { Text("Alasan Perbedaan Nominal (Wajib)") },
                        placeholder = { Text("Misal: Dipotong asuransi / bonus lembur") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_custom_salary_reason")
                    )
                }

                if (!isTimeReached) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Jadwal gajian belum tiba! Waktu ditentukan pada ${DateUtils.formatDateTimeIndo(schedule.nextDueDate)}. Tombol klaim/terima akan aktif saat waktu tiba.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            val canConfirm = isTimeReached && (!isCustomAmount || (parsedAmount > 0 && noteInput.isNotBlank()))
            Button(
                onClick = {
                    val finalAmount = if (isCustomAmount) parsedAmount else schedule.nominal
                    val finalNote = if (isCustomAmount) noteInput else "Gaji diterima sesuai jadwal"
                    onConfirm(schedule.id, finalAmount, finalNote)
                },
                enabled = canConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTimeReached) BankingBluePrimary else MaterialTheme.colorScheme.surfaceVariant,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.testTag("btn_confirm_salary_yes")
            ) {
                Text(
                    text = if (isTimeReached) "Ya, Sudah Terima" else "Belum Waktunya",
                    color = if (isTimeReached) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.outline
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = { onDismiss(schedule.id) },
                modifier = Modifier.testTag("btn_confirm_salary_no")
            ) {
                Text("Belum / Nanti")
            }
        }
    )
}
