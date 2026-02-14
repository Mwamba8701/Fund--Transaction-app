package com.example.fintechapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fintechapp.R
import com.example.fintechapp.data.model.Profile
import com.example.fintechapp.data.model.TransactionDto
import com.example.fintechapp.data.network.SupabaseClient
import com.example.fintechapp.ui.auth.LoginActivity
import com.example.fintechapp.ui.transaction.AddTransactionActivity
import com.example.fintechapp.ui.transaction.TransactionAdapter
import com.example.fintechapp.ui.transaction.TransactionsActivity
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.Locale

class DashboardActivity : AppCompatActivity() {

    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var progressBar: ProgressBar
    private val client = SupabaseClient.client
    private var currentProfile: Profile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        progressBar = findViewById(R.id.progressBar)
        setupViews()
        setupRecyclerView()
        
        // Start listening for realtime updates
        observeChanges()
    }

    override fun onResume() {
        super.onResume()
        fetchDashboardData()
    }

    private fun setupViews() {
        findViewById<Button>(R.id.btnAddTransaction).setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java))
        }

        findViewById<Button>(R.id.btnHistory).setOnClickListener {
            startActivity(Intent(this, TransactionsActivity::class.java))
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            lifecycleScope.launch {
                try {
                    client.auth.signOut()
                    val intent = Intent(this@DashboardActivity, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                } catch (e: Exception) {
                    // Handle logout error
                }
            }
        }
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recentTransactionsRecycler)
        recyclerView.layoutManager = LinearLayoutManager(this)
        transactionAdapter = TransactionAdapter(emptyList())
        recyclerView.adapter = transactionAdapter
    }

    private fun observeChanges() {
        val user = client.auth.currentUserOrNull() ?: return
        
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                val channel = client.realtime.channel("dashboard_updates")
                
                // Observe profile changes
                channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                    table = "profiles"
                    filter = "user_id=eq.${user.id}"
                }.onEach { action ->
                    when (action) {
                        is PostgresAction.Update -> {
                            currentProfile = action.decodeRecord<Profile>()
                            currentProfile?.let { updateProfileUI(it) }
                        }
                        is PostgresAction.Insert -> {
                            currentProfile = action.decodeRecord<Profile>()
                            currentProfile?.let { updateProfileUI(it) }
                        }
                        else -> {}
                    }
                }.launchIn(this)

                // Observe transaction changes to refresh balance
                channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                    table = "transactions"
                    filter = "user_id=eq.${user.id}"
                }.onEach {
                    fetchDashboardData()
                }.launchIn(this)

                channel.subscribe()
            }
        }
    }

    private fun updateProfileUI(profile: Profile) {
        val userNameText = findViewById<TextView>(R.id.userName)
        runOnUiThread {
            userNameText.text = getString(R.string.hello_user_format, profile.fullName)
        }
    }

    private fun updateBalanceUI(balance: Double) {
        val balanceText = findViewById<TextView>(R.id.totalBalance)
        runOnUiThread {
            balanceText.text = getString(R.string.currency_format, String.format(Locale.getDefault(), "%.2f", balance))
        }
    }

    private fun fetchDashboardData() {
        val user = client.auth.currentUserOrNull() ?: run {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val balanceText = findViewById<TextView>(R.id.totalBalance)
        val userNameText = findViewById<TextView>(R.id.userName)

        progressBar.visibility = android.view.View.VISIBLE

        lifecycleScope.launch {
            try {
                // Fetch all transactions to calculate balance
                val allTransactions = client.postgrest["transactions"]
                    .select {
                        filter { eq("user_id", user.id) }
                    }.decodeList<TransactionDto>()

                val totalBalance = allTransactions.sumOf { 
                    if (it.type == "deposit") it.amount else -it.amount 
                }
                updateBalanceUI(totalBalance)

                // Update recent transactions list (top 5)
                val recentTransactions = allTransactions
                    .sortedByDescending { it.createdAt }
                    .take(5)
                transactionAdapter.updateData(recentTransactions)

                // Fetch current profile data if not already cached
                if (currentProfile == null) {
                    currentProfile = client.postgrest["profiles"]
                        .select(columns = Columns.list("user_id", "full_name", "email")) {
                            filter { eq("user_id", user.id) }
                        }.decodeSingleOrNull<Profile>()
                }

                currentProfile?.let {
                    updateProfileUI(it)
                } ?: run {
                    // Create profile if it doesn't exist
                    createProfileForUser(user.id, user.email ?: "User")
                    userNameText.text = user.email ?: "User"
                }

            } catch (e: Exception) {
                // Fallback UI
                userNameText.text = user.email ?: "User"
                balanceText.text = getString(R.string.currency_format, "0.00")
            } finally {
                progressBar.visibility = android.view.View.GONE
            }
        }
    }

    private suspend fun createProfileForUser(userId: String, email: String) {
        try {
            val newProfile = Profile(
                userId = userId,
                fullName = email.substringBefore("@"),
                email = email
            )
            client.postgrest["profiles"].insert(newProfile)
            currentProfile = newProfile
            updateProfileUI(newProfile)
        } catch (e: Exception) {
            // Profile creation failed
        }
    }
}
