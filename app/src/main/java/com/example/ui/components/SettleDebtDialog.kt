package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PriceCheck
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
import com.example.data.model.Constants
import com.example.data.model.DebtReceivableEntity
import com.example.ui.theme.BankingBluePrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.util.CurrencyUtils

@Composable
fun SettleDebtDialog(
    item: DebtReceivableEntity,
    onConfirm: (debtId: Long, paymentAmount: Long, note: String) -> Unit,
    onDismiss: () -> Unit
) {
    var isFullPayment by remember { mutableStateOf(true) }
    var customAmountInput by remember { mutableStateOf(item.remainingAmount.toString()) }
    var noteInput by remember { mutableStateOf("") }

    val paymentAmount = if (isFullPayment) {
        item.remainingAmount
    } else {
        CurrencyUtils.parseRupiahInput(customAmountInput).coerceAtMost(item.remainingAmount)
    }

    val remainingAfterPayment = (item.remainingAmount - paymentAmount).coerceAtLeast(0L)
    val isPiutang = item.type == Constants.DEBT_PIUTANG
    val isValid = paymentAmount > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PriceCheck,
                    contentDescription = null,
                    tint = if (isPiutang) IncomeGreen else ExpenseRed,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = if (isPiutang) "Terima Bayar Piutang" else "Bayar Hutang",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "${item.personName} (${if (isPiutang) "Piutang / Aset" else "Hutang"})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Sisa yang belum dibayar: ${CurrencyUtils.formatRupiah(item.remainingAmount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = isFullPayment,
                        onClick = { isFullPayment = true },
                        label = { Text("Lunas Penuh") },
                        modifier = Modifier.weight(1f).testTag("chip_settle_full")
                    )
                    Spacer(modifier = Modifier.weight(0.1f))
                    FilterChip(
                        selected = !isFullPayment,
                        onClick = {
                            isFullPayment = false
                            if (customAmountInput.isEmpty() || customAmountInput == item.remainingAmount.toString()) {
                                customAmountInput = (item.remainingAmount / 2).toString()
                            }
                        },
                        label = { Text("Bayar Cicilan") },
                        modifier = Modifier.weight(1f).testTag("chip_settle_partial")
                    )
                }

                if (!isFullPayment) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = customAmountInput,
                        onValueChange = { customAmountInput = it },
                        label = { Text("Nominal Pembayaran (Rp)") },
                        placeholder = { Text("Misal: 200000 atau 200rb") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        supportingText = {
                            Text("Dibayar: ${CurrencyUtils.formatRupiah(paymentAmount)}")
                        },
                        modifier = Modifier.fillMaxWidth().testTag("input_settle_amount")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    label = { Text("Catatan / Keterangan Pembayaran") },
                    placeholder = { Text("Misal: Transfer Bank / Tunai cash") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_settle_note")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Impact Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isPiutang) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (isPiutang) "Dampak: Saldo Kas Bertambah" else "Dampak: Saldo Kas Berkurang",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isPiutang) "+${CurrencyUtils.formatRupiah(paymentAmount)}" else "-${CurrencyUtils.formatRupiah(paymentAmount)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isPiutang) IncomeGreen else ExpenseRed
                        )
                        Text(
                            text = if (remainingAfterPayment == 0L) "Status: Lunas Sepenuhnya!" else "Sisa Piutang Tersisa: ${CurrencyUtils.formatRupiah(remainingAfterPayment)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val defaultNote = if (remainingAfterPayment == 0L) "Pelunasan penuh" else "Cicilan pembayaran"
                    val finalNote = noteInput.ifBlank { defaultNote }
                    onConfirm(item.id, paymentAmount, finalNote)
                },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPiutang) BankingBluePrimary else ExpenseRed
                ),
                modifier = Modifier.testTag("btn_confirm_settle")
            ) {
                Text(
                    text = if (remainingAfterPayment == 0L) "Konfirmasi Lunas" else "Catat Cicilan",
                    color = androidx.compose.ui.graphics.Color.White
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancel_settle")
            ) {
                Text("Batal")
            }
        }
    )
}
