package com.example.varushopretailer.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.varushopretailer.activity.LanguageSelectionActivity
import com.example.varushopretailer.databinding.ActivityOnBoardingBinding

class OnBoardingActivity : AppCompatActivity() {
        private lateinit var binding: ActivityOnBoardingBinding

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivityOnBoardingBinding.inflate(layoutInflater)
            setContentView(binding.root)

            binding.illustrationContainer.alpha = 0f
            binding.illustrationContainer.animate().alpha(1f).setDuration(1000).start()

            binding.btnNext.setOnClickListener {
                val intent = Intent(this, LanguageSelectionActivity::class.java)
                startActivity(intent)

                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            }
        }
    }
