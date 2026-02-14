package com.example.fintechapp.ui.transaction

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.fintechapp.R
import com.example.fintechapp.data.model.TransactionDto
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter(
    private var transactions: List<TransactionDto>,
    private val onItemClick: (TransactionDto) -> Unit = {}
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    // Multiple date formats to handle different Supabase timestamp formats
    private val dateFormats = listOf(
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSX", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    )

    // Updated format to show Date and Time
    private val outputFormat = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.bind(transaction)
    }

    override fun getItemCount(): Int = transactions.size

    fun updateData(newTransactions: List<TransactionDto>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val typeText: TextView = itemView.findViewById(R.id.txnType)
        private val dateText: TextView = itemView.findViewById(R.id.txnDate)
        private val amountText: TextView = itemView.findViewById(R.id.txnAmount)
        private val iconView: ImageView = itemView.findViewById(R.id.iconType)

        fun bind(transaction: TransactionDto) {
            typeText.text = transaction.type.replaceFirstChar { it.uppercase() }
            amountText.text = formatAmount(transaction)
            dateText.text = formatDate(transaction.createdAt)

            // Set icon and color based on transaction type
            if (transaction.type.equals("deposit", ignoreCase = true)) {
                iconView.setImageResource(R.drawable.ic_deposit)
                amountText.setTextColor(ContextCompat.getColor(itemView.context, R.color.green))
            } else {
                iconView.setImageResource(R.drawable.ic_withdraw)
                amountText.setTextColor(ContextCompat.getColor(itemView.context, R.color.red))
            }

            itemView.setOnClickListener { onItemClick(transaction) }
        }

        private fun formatAmount(transaction: TransactionDto): String {
            val sign = if (transaction.type.equals("deposit", ignoreCase = true)) "+" else "-"
            return String.format(Locale.getDefault(), "%sKES %.2f", sign, transaction.amount)
        }

        private fun formatDate(dateString: String?): String {
            if (dateString.isNullOrEmpty()) return "Date not available"

            return try {
                var date: Date? = null
                for (format in dateFormats) {
                    try {
                        format.timeZone = TimeZone.getTimeZone("UTC")
                        date = format.parse(dateString)
                        if (date != null) break
                    } catch (e: Exception) {
                        // Try next format
                    }
                }

                if (date != null) {
                    outputFormat.timeZone = TimeZone.getDefault() // Display in local time
                    outputFormat.format(date)
                } else {
                    // Try to extract just the date part
                    dateString.substringBefore("T")
                }
            } catch (e: Exception) {
                "Invalid Date"
            }
        }
    }
}