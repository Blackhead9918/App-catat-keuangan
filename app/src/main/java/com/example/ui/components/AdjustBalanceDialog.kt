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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.ui.theme.BankingBluePrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.util.CurrencyUtils

@Composable
fun AdjustBalanceDialog(
    currentBalance: Long,
    onConfirm: (newBalance: Long, reason: String) -> Unit,
    onDismiss: () -> Unit
) {
    var balanceInput by remember { mutableStateOf(currentBalance.toString()) }
    var reasonInput by remember { mutableStateOf("") }

    val parsedTarget = CurrencyUtils.parseRupiahInput(balanceInput)
    val diff = parsedTarget - currentBalance
    val isReasonValid = reasonInput.trim().isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = BankingBluePrimary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Sesuaikan Saldo Kas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Perubahan saldo kas wajib disertai alasan atau penjelasan yang jelas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Saldo Saat Ini:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = CurrencyUtils.formatRupiah(currentBalance),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = balanceInput,
                    onValueChange = { balanceInput = it },
                    label = { Text("Target Saldo Baru (Rp)") },
                    placeholder = { Text("Misal: 1500000 atau 1.5jt") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    supportingText = {
                        val diffText = if (diff >= 0) "+${CurrencyUtils.formatRupiah(diff)}" else "-${CurrencyUtils.formatRupiah(-diff)}"
                        val diffColor = if (diff >= 0) IncomeGreen else ExpenseRed
                        Text(
                            text = "Total Baru: ${CurrencyUtils.formatRupiah(parsedTarget)} ($diffText)",
                            color = diffColor
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_adjust_balance_target")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = reasonInput,
                    onValueChange = { reasonInput = it },
                    label = { Text("Alasan / Penjelasan (Opsional)") },
                    placeholder = { Text("Misal: Penyesuaian uang saku tunai, koreksi selisih hitung") },
                    minLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_adjust_balance_reason")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalReason = reasonInput.trim().ifBlank { "Penyesuaian saldo kas manual" }
                    onConfirm(parsedTarget, finalReason)
                },
                enabled = parsedTarget >= 0,
                colors = ButtonDefaults.buttonColors(containerColor = BankingBluePrimary),
                modifier = Modifier.testTag("btn_confirm_adjust_balance")
            ) {
                Text("Simpan Perubahan")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancel_adjust_balance")
            ) {
                Text("Batal")
            }
        }
    )
}
