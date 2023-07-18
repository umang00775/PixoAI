package com.pixoai.android.authentication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.pixoai.android.Dashboard
import com.pixoai.android.R
import com.pixoai.android.data.Keys
import com.pixoai.android.modal.User
import kotlin.random.Random

class Signup: Fragment(R.layout.fragment_signup) {
    private lateinit var database: DatabaseReference
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val helloImg = view.findViewById<ImageView>(R.id.hello)
        val opacity = 0.15f
        helloImg.alpha = opacity

        val signupBtn = view.findViewById<RelativeLayout>(R.id.signup_button)
        val inputFirstName = view.findViewById<TextInputEditText>(R.id.first_name)
        val inputLastName = view.findViewById<TextInputEditText>(R.id.last_name)
        val inputEmail = view.findViewById<TextInputEditText>(R.id.email_input)
        val inputPassword = view.findViewById<TextInputEditText>(R.id.password_input)

        signupBtn.setOnClickListener {
            val firstName = inputFirstName.text.toString()
            val lastName = inputLastName.text.toString()
            val email = inputEmail.text.toString()
            val password = inputPassword.text.toString()

            val validateEmail = isValidEmail(email)

            if(firstName.isBlank()) Toast.makeText(requireContext(), "First name cannot be empty!", Toast.LENGTH_LONG).show()
            else if(lastName.isBlank()) Toast.makeText(requireContext(), "Last name cannot be empty!", Toast.LENGTH_LONG).show()
            else if(email.isBlank()) Toast.makeText(requireContext(), "Email cannot be empty!", Toast.LENGTH_LONG).show()
            else if(email.isNotBlank() && !validateEmail) Toast.makeText(requireContext(), "Please enter valid email!", Toast.LENGTH_LONG).show()
            else if(password.isBlank()) Toast.makeText(requireContext(), "Password cannot be empty!", Toast.LENGTH_LONG).show()
            else signupUser(firstName, lastName, email, password)
        }


    }

    // Signup user
    private fun signupUser(firstName: String, lastName: String, email: String, password: String){
        database = FirebaseDatabase.getInstance().getReference("PixoAI/Users")

        val username = getEmailPrefix(email)
        val sharedPref = requireContext().getSharedPreferences(Keys.SHARED_PREF, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        val userData = User(firstName, lastName, email, password, 10)

        val apiNumber = generateRandomNumber()

        val userExists = userExists(username)

        if(userExists){
            Toast.makeText(requireContext(), "User already exists", Toast.LENGTH_SHORT).show()
        }
        else{
            database.child(username).setValue(userData)
                .addOnSuccessListener {
                    editor.putString(Keys.FIRST_NAME, firstName)
                    editor.putString(Keys.LAST_NAME, lastName)
                    editor.putInt(Keys.CREDITS, 10)
                    editor.putString(Keys.EMAIL, email)
                    editor.putString(Keys.HAS_LOGGED_IN, "YES")
                    editor.putString(Keys.USERNAME, username)
                    editor.putInt(Keys.API_KEY_NUMBER, apiNumber)
                    editor.apply()

                    startActivity(Intent(requireContext(), Dashboard::class.java))
                    Animatoo.animateFade(requireContext());

                    Toast.makeText(requireContext(), "Welcome $firstName", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Something went wrong!", Toast.LENGTH_LONG).show()
                }
        }

    }

    //  get User name
    fun getEmailPrefix(email: String): String {
        return email.substringBefore("@")
    }

    // Random number
    fun generateRandomNumber(): Int {
        return Random.nextInt(1, 9)
    }


    // Validate email
    private fun isValidEmail(email: String): Boolean {
        val pattern = """^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$""".toRegex()
        return pattern.matches(email)
    }

    // check if user already exists
    private fun userExists(username: String): Boolean {
        var exists = false

        database.child(username)
            .get()
            .addOnSuccessListener { dataSnapshot ->
                exists = dataSnapshot.exists()
            }

        return exists
    }


}