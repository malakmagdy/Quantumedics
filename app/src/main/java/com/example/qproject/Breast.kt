package com.example.qproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import com.example.qproject.databinding.BreastBinding

class Breast : AppCompatActivity() {
    private lateinit var binding: BreastBinding
    var item :String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = BreastBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.spBreastImageTypes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, position: Int, id: Long) {
                item = adapterView?.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        binding.btnBreastNext.setOnClickListener {
            if (item == "Histopathology"){
                val intent = Intent(this, UploadBreastActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this@Breast, "$item is not available at the moment !", Toast.LENGTH_LONG).show()
            }

        }
    }
}