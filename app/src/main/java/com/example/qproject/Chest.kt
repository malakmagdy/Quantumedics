package com.example.qproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import com.example.qproject.databinding.ChestBinding

class Chest : AppCompatActivity() {
    private lateinit var binding: ChestBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ChestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var item = ""

        binding.spChestImageTypes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, position: Int, id: Long) {
                item = adapterView?.getItemAtPosition(position).toString()

            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        binding.btnChestNext.setOnClickListener {
            if (item == "Chest X-Ray"){
                val intent = Intent(this, Chest_CT_Intructions::class.java )
                startActivity(intent)
            } else {
                Toast.makeText(this@Chest, "$item is not available at the moment !", Toast.LENGTH_LONG).show()
            }


        }


    }
}