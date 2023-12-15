package com.example.qproject

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
//import android.view.View
import android.widget.Button
//import android.widget.DatePicker
import android.widget.TextView
import android.widget.Toast
import com.example.qproject.databinding.SignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: SignupBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database : DatabaseReference
    private var cal = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Calender
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        //Click to show DatePickerDialog -setOnTouchListener
        binding.etBithdate.setOnClickListener {
            val dpd = DatePickerDialog(this,DatePickerDialog.OnDateSetListener { view, mYear, mMonth, mDay ->
                //set the text to the chosen date
                binding.etBithdate.setText("$mDay/${mMonth+1}/$mYear")
                //var age = getAge(mYear,mMonth,mDay)  //age calculated from the date

            },year,month,day)
            //show the dialog
            dpd.show()
        }

        // Register Button -> Create a user account on Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        binding.btnRegister.setOnClickListener {
            val firstName = binding.etFirstName.text.toString()
            val lastName = binding.etLastName.text.toString()
            val email = binding.etEmail.text.toString() //getting the email
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()
            val birthdate = binding.etBithdate.text.toString()

            if (email.isNotEmpty() && firstName.isNotEmpty() && lastName.isNotEmpty() &&
                    password.isNotEmpty() && confirmPassword.isNotEmpty() && birthdate.isNotEmpty()){

                val isValid = isDateStringValid(birthdate)
                if (isValid == false){
                    Toast.makeText(this, "Invalid Date format",Toast.LENGTH_SHORT ).show()
                }
                if (password == confirmPassword ){ //&& isValid
                    firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                        if (it.isSuccessful){
                            // Firebase Database
                            database = FirebaseDatabase.getInstance().getReference("Users")  //doesn't matter if this exists or no if it exist it will add user if not it will be created by default
                            val User = User(firstName, lastName, email, birthdate)  //create object of the user class
                            /* New Code to make the user with the ID*/
                            val uid = firebaseAuth.currentUser?.uid
                            if (uid != null) {
                                database.child(uid).setValue(User).addOnSuccessListener {
                                   binding.etFirstName.text?.clear()
                                    binding.etLastName.text?.clear()
                                    binding.etPassword.text?.clear()
                                    binding.etConfirmPassword.text?.clear()
                                    binding.etEmail.text?.clear()
                                    binding.etBithdate.text?.clear()

                                    Toast.makeText(this, "Successfully Saved ",Toast.LENGTH_SHORT).show()
                                }.addOnFailureListener {
                                    Toast.makeText(this, "Failed !",Toast.LENGTH_SHORT).show()
                                }
                            }
                            // Move to next screen
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()  //closes unused resources and doesn't allow the back
                        } else {
                            Toast.makeText(this,it.exception.toString(),Toast.LENGTH_SHORT).show()
                        }
                    }
                } else if (password.length < 8 ){
                    Toast.makeText(this, "Password must not be less than 8 characters",Toast.LENGTH_SHORT ).show()
                } else {
                    Toast.makeText(this,"Password does not matched",Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this,"Fields cannot be empty",Toast.LENGTH_SHORT).show()
            }



        }

        // Already have an account? LOGIN
        binding.tvLoginRedirect.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    fun isDateStringValid(dateString: String): Boolean {
        val format = SimpleDateFormat("dd/MM/yyyy")
        format.isLenient = false // Disable lenient parsing

        try {
            format.parse(dateString)
            return true // Date string is valid
        } catch (e: ParseException) {
            return false // Date string is invalid
        }
    }


}

