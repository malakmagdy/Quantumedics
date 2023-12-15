package com.example.qproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import com.example.qproject.databinding.ActivityMainBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database : DatabaseReference

    var chest :Boolean = false
    var brain :Boolean = false
    var breast :Boolean = false
    var item :String = ""

    // Store the original dimensions when the activity is created or fragment is initialized


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val originalLayoutParams = binding.ivChest.layoutParams
        var originalWidth: Int = originalLayoutParams.width
        var originalHeight:Int = originalLayoutParams.height
        //FirebaseDatabase.getInstance().getReference("Users")

        FirebaseApp.initializeApp(this)
        val database = FirebaseDatabase.getInstance().getReference("Users")
        val currentUser = FirebaseAuth.getInstance().currentUser
        if(currentUser != null){
            val userId = currentUser.uid
            // Get a reference to the "firstName" field for the user
            val firstNameRef = database.child(userId).child("firstName")
            // Add a ValueEventListener to the "firstName" field reference
            firstNameRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Get the value of the "firstName" field
                    val firstName = snapshot.getValue(String::class.java)
                    binding.tvHelloUserName.text = "Hello, $firstName!"
                }
                override fun onCancelled(error: DatabaseError) {
                    // Handle the error case appropriately
                    Log.w(TAG, "Failed to read value.", error.toException())
                }
                })
        }else {// If the user is not signed in, redirect them to the login screen
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
            finish()
        }

        binding.btnBrain.setOnClickListener {
            val intent = Intent(this, Brain::class.java)
            startActivity(intent)
            /*
            binding.llBreast.visibility=View.GONE
            binding.llChest.visibility=View.GONE

            val layoutParams: ViewGroup.LayoutParams = binding.ivBrain.layoutParams
            layoutParams.width = 600
            layoutParams.height = 600
            // Set the new layout parameters to the image
            binding.ivBrain.layoutParams = layoutParams

            binding.tvChooseScanTypes.visibility = View.VISIBLE
            binding.btnNext.visibility = View.VISIBLE

            binding.spBrainImageTypes.visibility = View.VISIBLE
            binding.spChestImageTypes.visibility = View.GONE
            binding.spBreastImageTypes.visibility = View.GONE
            brain = true
            chest = false
            breast = false

             */

        }

        binding.btnChest.setOnClickListener {
            val intent = Intent(this, Chest::class.java)
            startActivity(intent)

            /*
            //make the view of brain and chest disappear
            binding.llBrain.visibility=View.GONE
            binding.llBreast.visibility=View.GONE
            val layoutParams: ViewGroup.LayoutParams = binding.ivChest.layoutParams
            layoutParams.width = 600
            layoutParams.height = 600
            // Set the new layout parameters to the image
            binding.ivChest.layoutParams = layoutParams

            //make the corresponding drop down menu type of images appear
            binding.tvChooseScanTypes.visibility = View.VISIBLE
            binding.btnNext.visibility = View.VISIBLE

            binding.spChestImageTypes.visibility = View.VISIBLE
            binding.spBrainImageTypes.visibility = View.GONE
            binding.spBreastImageTypes.visibility = View.GONE
            chest = true
            brain = false
            breast = false*/

        }

        binding.btnBreast.setOnClickListener {
            val intent = Intent(this, Breast::class.java)
            startActivity(intent)/*
            //make the view of brain and chest disappear
            binding.llBrain.visibility=View.GONE
            binding.llChest.visibility=View.GONE
            val layoutParams: ViewGroup.LayoutParams = binding.ivBreast.layoutParams
            layoutParams.width = 600
            layoutParams.height = 600

            // Set the new layout parameters to the image
            binding.ivBreast.layoutParams = layoutParams
            binding.tvChooseScanTypes.visibility = View.VISIBLE
            binding.btnNext.visibility = View.VISIBLE
            //make the corresponding drop down menu type of images appear
            binding.spBreastImageTypes.visibility = View.VISIBLE
            binding.spChestImageTypes.visibility = View.GONE
            binding.spBrainImageTypes.visibility = View.GONE
            chest = false
            brain = false
            breast = true*/
        }

        binding.spBrainImageTypes.onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, position: Int, id: Long) {
                item = adapterView?.getItemAtPosition(position).toString()
                Toast.makeText(this@MainActivity, "You selected ${adapterView?.getItemAtPosition(position).toString()}", Toast.LENGTH_SHORT).show()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        binding.spBreastImageTypes.onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, position: Int, id: Long) {
                item = adapterView?.getItemAtPosition(position).toString()
                Toast.makeText(this@MainActivity, "You selected ${adapterView?.getItemAtPosition(position).toString()}", Toast.LENGTH_SHORT).show()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        binding.spChestImageTypes.onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, position: Int, id: Long) {
                item = adapterView?.getItemAtPosition(position).toString()
                Toast.makeText(this@MainActivity, "You selected ${adapterView?.getItemAtPosition(position).toString()}", Toast.LENGTH_SHORT).show()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
/*
        binding.btnNext.setOnClickListener {
         if (item == "Histopathology"){
             val intent = Intent(this, instructionsCT::class.java)
             startActivity(intent)
         }else if(item == " Magnetic Resonance Imaging for brain (MRI)"){
             val intent = Intent(this, Brain_MRI_Instructions::class.java)
             startActivity(intent)
         }else if(item == "Computed Tomography for chest (CT)"){
             val intent = Intent(this, Chest_CT_Intructions::class.java)
             startActivity(intent)
         }
        }

 */

    }

    companion object {
        const val TAG = "MainActivity"
    }

    /*
    override fun onBackPressed() {
        if (brain){
            val layoutParams: ViewGroup.LayoutParams = binding.ivBrain.layoutParams
            layoutParams.width = originalWidth
            layoutParams.height = originalHeight
            binding.llChest.visibility = View.VISIBLE
            binding.llBreast.visibility = View.VISIBLE
        }else if(chest){
            val layoutParams: ViewGroup.LayoutParams = binding.ivChest.layoutParams
            layoutParams.width = originalWidth
            layoutParams.height = originalHeight
            binding.llBrain.visibility = View.VISIBLE
            binding.llBreast.visibility = View.VISIBLE

        } else if(breast){
            val layoutParams: ViewGroup.LayoutParams = binding.ivBreast.layoutParams
            layoutParams.width = originalWidth
            layoutParams.height = originalHeight
            binding.llChest.visibility = View.VISIBLE
            binding.llBrain.visibility = View.VISIBLE
        }

        // Call super.onBackPressed() to perform the default back button behavior
        super.onBackPressed()
    }

     */
}