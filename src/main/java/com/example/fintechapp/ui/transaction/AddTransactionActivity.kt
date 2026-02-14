package com.example.fintechapp.ui.transaction

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.fintechapp.R
import com.example.fintechapp.data.model.TransactionDto
import com.example.fintechapp.data.network.SupabaseClient
import com.google.android.material.textfield.TextInputEditText
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class AddTransactionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        val amountInput = findViewById<TextInputEditText>(R.id.amount)
        val typeGroup = findViewById<RadioGroup>(R.id.typeGroup)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        btnSave.setOnClickListener {
            val amountText = amountInput.text.toString()
            val amountValue = amountText.toDoubleOrNull()

            if (amountValue == null || amountValue <= 0) {
                Toast.makeText(this, getString(R.string.please_enter_valid_amount), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedId = typeGroup.checkedRadioButtonId
            val type = if (selectedId == R.id.deposit) "deposit" else "withdraw"

            // Disable UI during save
            btnSave.isEnabled = false
            btnSave.text = getString(R.string.saving)
            progressBar.visibility = View.VISIBLE

            lifecycleScope.launch {
                try {
                    val user = SupabaseClient.client.auth.currentUserOrNull()

                    if (user == null) {
                        Toast.makeText(this@AddTransactionActivity,
                            getString(R.string.user_not_logged_in), Toast.LENGTH_SHORT).show()
                        finish()
                        return@launch
                    }

                    // Generate current timestamp in ISO 8601 format for Supabase
                    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }
                    val currentTimestamp = sdf.format(Date())

                    // 1. Create transaction with timestamp
                    val transaction = TransactionDto(
                        userId = user.id,
                        type = type,
                        amount = amountValue,
                        createdAt = currentTimestamp
                    )

                    // 2. Insert transaction
                    SupabaseClient.client.postgrest["transactions"].insert(transaction)

                    Toast.makeText(this@AddTransactionActivity,
                        getString(R.string.saved_success), Toast.LENGTH_SHORT).show()

                    // Return to dashboard
                    setResult(RESULT_OK)
                    finish()

                } catch (e: Exception) {
                    Toast.makeText(this@AddTransactionActivity,
                        getString(R.string.error_saving_format, e.message), Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                    resetUI(btnSave, progressBar)
                }
            }
        }
    }

    private fun resetUI(btnSave: Button, progressBar: ProgressBar) {
        btnSave.isEnabled = true
        btnSave.text = getString(R.string.save_transaction)
        progressBar.visibility = View.GONE
    }
}
