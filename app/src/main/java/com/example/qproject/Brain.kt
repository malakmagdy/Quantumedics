package com.example.qproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import com.example.qproject.databinding.BrainBinding

class Brain : AppCompatActivity() {
    private lateinit var binding: BrainBinding
    var item :String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = BrainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.spBrainImageTypes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, position: Int, id: Long) {
                item = adapterView?.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        binding.btnBrainNext.setOnClickListener {
            if (item == "Magnetic Resonance Imaging for brain (MRI)"){
                val intent = Intent(this, Brain_MRI_Instructions::class.java )
                startActivity(intent)
            } else {
                Toast.makeText(this@Brain, "$item is not available at the moment !", Toast.LENGTH_LONG).show()
            }
        }

    }
}