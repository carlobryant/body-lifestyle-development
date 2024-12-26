package com.example.bold_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.bold_app.databinding.ActivityLogInBinding
import com.example.bold_app.google_services.GoogleAuthClient
import com.example.bold_app.viewmodel.LogInViewModel
import kotlinx.coroutines.launch

class LogInActivity : AppCompatActivity() {

        private lateinit var binding: ActivityLogInBinding
        private val viewModel: LogInViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel.updateSignInStatus(false)
        val googleAuthClient = GoogleAuthClient(applicationContext)
        //var isSignedIn = googleAuthClient.isSignedIn()

        binding.button.setOnClickListener {
            binding.loadingPanel.visibility = View.VISIBLE
            lifecycleScope.launch {
                var isSignedIn = googleAuthClient.signIn(this@LogInActivity)

                if (isSignedIn) {
                    viewModel.updateSignInStatus(true)
                    startActivity(Intent(this@LogInActivity, MainActivity::class.java))
                    finish()
                } else {
                    binding.loadingPanel.visibility = View.GONE
                }
            }
        }
    }
}