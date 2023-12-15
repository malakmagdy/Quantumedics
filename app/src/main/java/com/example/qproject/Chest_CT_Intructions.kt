package com.example.qproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.qproject.databinding.ChestCtIntructionsBinding

class Chest_CT_Intructions : AppCompatActivity() {
    private lateinit var binding: ChestCtIntructionsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ChestCtIntructionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ctInstruction1.setOnCheckedChangeListener { _, _ ->
            updateButtonVisibility()
        }
        binding.ctInstruction2.setOnCheckedChangeListener { _, _ ->
            updateButtonVisibility()
        }
        binding.btnNext.setOnClickListener {
            startActivity(Intent(this,UploadChestActivity::class.java))
        }
    }

    private fun updateButtonVisibility() {
        if (binding.ctInstruction1.isChecked && binding.ctInstruction2.isChecked) {
            binding.btnNext.visibility = Button.VISIBLE
        } else {
            binding.btnNext.visibility = Button.GONE
        }
    }

}