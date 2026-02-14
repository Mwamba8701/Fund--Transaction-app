package com.example.fintechapp.ui.transaction

import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fintechapp.R
import com.example.fintechapp.data.model.TransactionDto
import com.example.fintechapp.data.network.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.launch

class TransactionsActivity : AppCompatActivity() {
    private lateinit var adapter: TransactionAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyStateText: TextView
    private val transactionList = mutableListOf<TransactionDto>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_list)

        progressBar = findViewById(R.id.progressBar)
        emptyStateText = findViewById(R.id.emptyStateText)
        val recyclerView = findViewById<RecyclerView>(R.id.transactionsRecycler)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TransactionAdapter(transactionList)
        recyclerView.adapter = adapter

        loadTransactions()
    }

    private fun loadTransactions() {
        val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: run {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        progressBar.visibility = android.view.View.VISIBLE
        emptyStateText.visibility = android.view.View.GONE

        lifecycleScope.launch {
            try {
                val result = SupabaseClient.client.postgrest["transactions"]
                    .select {
                        filter { eq("user_id", userId) }
                        order("created_at", Order.DESCENDING)
                    }.decodeList<TransactionDto>()

                transactionList.clear()
                transactionList.addAll(result)
                adapter.notifyDataSetChanged()

                if (result.isEmpty()) {
                    emptyStateText.visibility = android.view.View.VISIBLE
                }

            } catch (e: Exception) {
                Toast.makeText(this@TransactionsActivity,
                    "Failed to load transactions: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            } finally {
                progressBar.visibility = android.view.View.GONE
            }
        }
    }
}
