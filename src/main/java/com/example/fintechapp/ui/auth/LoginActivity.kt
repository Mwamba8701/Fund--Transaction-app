package com.example.fintechapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.fintechapp.R
import com.example.fintechapp.data.network.SupabaseClient
import com.example.fintechapp.ui.DashboardActivity
import com.google.android.material.textfield.TextInputEditText
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailInput = findViewById<TextInputEditText>(R.id.email)
        val passwordInput = findViewById<TextInputEditText>(R.id.password)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val goToRegister = findViewById<TextView>(R.id.goToRegister)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        btnLogin.setOnClickListener {
            val emailText = emailInput.text.toString().trim()
            val passText = passwordInput.text.toString().trim()

            if (emailText.isEmpty() || passText.isEmpty()) {
                Toast.makeText(this, getString(R.string.please_enter_email_password), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnLogin.isEnabled = false
            progressBar.visibility = android.view.View.VISIBLE

            lifecycleScope.launch {
                try {
                    SupabaseClient.client.auth.signInWith(Email) {
                        email = emailText
                        password = passText
                    }

                    Log.d("LoginActivity", "Login successful")

                    val intent = Intent(this@LoginActivity, DashboardActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()

                } catch (e: Exception) {
                    Log.e("LoginActivity", "Login failed", e)

                    val errorMessage = when {
                        e.message?.contains("Invalid login credentials") == true ->
                            "Invalid email or password"
                        e.message?.contains("Email not confirmed") == true ->
                            "Please verify your email first"
                        else -> e.message ?: "Login failed"
                    }

                    Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_LONG).show()

                } finally {
                    btnLogin.isEnabled = true
                    progressBar.visibility = android.view.View.GONE
                }
            }
        }

        goToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}