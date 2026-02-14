package com.example.fintechapp.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.fintechapp.R
import com.example.fintechapp.data.model.Profile
import com.example.fintechapp.data.network.SupabaseClient
import com.google.android.material.textfield.TextInputEditText
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val nameInput = findViewById<TextInputEditText>(R.id.fullName)
        val emailInput = findViewById<TextInputEditText>(R.id.regEmail)
        val passInput = findViewById<TextInputEditText>(R.id.regPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        btnRegister.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passInput.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnRegister.isEnabled = false
            progressBar.visibility = View.VISIBLE

            lifecycleScope.launch {
                try {
                    // Register user via auth plugin
                    SupabaseClient.client.auth.signUpWith(Email) {
                        this.email = email
                        this.password = password
                    }

                    // Try to obtain the newly created user's id from the current session
                    val userId = SupabaseClient.client.auth.currentUserOrNull()?.id

                    if (userId != null) {
                        // Create profile for the user if we have an id
                        createUserProfile(userId, name, email)
                    }

                    Toast.makeText(
                        this@RegisterActivity,
                        "Registration successful! Please check your email for verification.",
                        Toast.LENGTH_LONG
                    ).show()

                    finish() // Go back to login

                } catch (e: Exception) {
                    val errorMessage = when {
                        e.message?.contains("User already registered") == true ->
                            "Email already registered"
                        e.message?.contains("Password should be at least") == true ->
                            "Password too weak"
                        else -> e.message ?: "Registration failed"
                    }

                    Toast.makeText(this@RegisterActivity, errorMessage, Toast.LENGTH_LONG).show()

                } finally {
                    btnRegister.isEnabled = true
                    progressBar.visibility = View.GONE
                }
            }
        }
    }

    private suspend fun createUserProfile(userId: String, name: String, email: String) {
        try {
            val profile = Profile(
                userId = userId,
                fullName = name,
                email = email
            )

            SupabaseClient.client.postgrest["profiles"].insert(profile)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
