package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.ui.theme.BankingBluePrimary
import com.example.ui.theme.BlueReceivable
import com.example.ui.theme.ExpenseRed
import com.example.util.CurrencyUtils

@Composable
fun AddDebtDialog(
    onConfirm: (
        personName: String,
        type: String,
        amount: Long,
        dueDate: Long?,
        notes: String
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var personName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(Constants.DEBT_PIUTANG) }
    var amountInput by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val parsedAmount = CurrencyUtils.parseRupiahInput(amountInput)
    val isValid = personName.isNotBlank() && parsedAmount > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Handshake,
                    contentDescription = null,
                    tint = if (selectedType == Constants.DEBT_PIUTANG) BlueReceivable else ExpenseRed,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = if (selectedType == Constants.DEBT_PIUTANG) "Tambah Aset Piutang" else "Tambah Tanggungan Hutang",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Aset piutang tidak langsung masuk ke saldo kas utama sampai orang tersebut melunasinya.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedType == Constants.DEBT_PIUTANG,
                        onClick = { selectedType = Constants.DEBT_PIUTANG },
                        label = { Text("Piutang (Aset di Orang)") },
                        modifier = Modifier.weight(1f).testTag("chip_debt_piutang")
                    )
                    FilterChip(
                        selected = selectedType == Constants.DEBT_HUTANG,
                        onClick = { selectedType = Constants.DEBT_HUTANG },
                        label = { Text("Hutang (Tanggungan Saya)") },
                        modifier = Modifier.weight(1f).testTag("chip_debt_hutang")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = personName,
                    onValueChange = { personName = it },
                    label = { Text(if (selectedType == Constants.DEBT_PIUTANG) "Nama Peminjam*" else "Nama Pemberi Pinjaman*") },
                    placeholder = { Text("Misal: Budi Santoso / Toko Sejahtera") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_debt_person")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Nominal Pinjaman (Rp)*") },
                    placeholder = { Text("Misal: 500000 atau 500rb") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    supportingText = {
                        Text("Terbaca: ${CurrencyUtils.formatRupiah(parsedAmount)}")
                    },
                    modifier = Modifier.fillMaxWidth().testTag("input_debt_amount")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Keterangan / Keperluan") },
                    placeholder = { Text("Misal: Pinjam uang bengkel / beli bahan") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth().testTag("input_debt_notes")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        personName.trim(),
                        selectedType,
                        parsedAmount,
                        null,
                        notes.trim()
                    )
                },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedType == Constants.DEBT_PIUTANG) BankingBluePrimary else ExpenseRed
                ),
                modifier = Modifier.testTag("btn_save_debt")
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancel_debt")
            ) {
                Text("Batal")
            }
        }
    )
}
