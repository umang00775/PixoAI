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
import kotlin.random.Random

class Login : Fragment(R.layout.fragment_login) {
    lateinit var database: DatabaseReference
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val welcomeImg = view.findViewById<ImageView>(R.id.welcome)
        val opacity: Float = 0.15f
        welcomeImg.alpha = opacity

        val loginBtn = view.findViewById<RelativeLayout>(R.id.login_button)
        val inputEmail = view.findViewById<TextInputEditText>(R.id.email_input)
        val inputPassword = view.findViewById<TextInputEditText>(R.id.password_input)

        loginBtn.setOnClickListener {
            val email = inputEmail.text.toString()
            val password = inputPassword.text.toString()

            val validateEmail = isValidEmail(email)

            if(email.isBlank()) Toast.makeText(requireContext(), "Email cannot be empty!", Toast.LENGTH_LONG).show()
            else if(email.isNotBlank() && !validateEmail) Toast.makeText(requireContext(), "Please enter valid email!", Toast.LENGTH_LONG).show()
            else if(password.isBlank()) Toast.makeText(requireContext(), "Password cannot be empty!", Toast.LENGTH_LONG).show()
            else login(email, password)
        }
    }

    // Login
    private fun login(email: String, password: String){

        database = FirebaseDatabase.getInstance().getReference("PixoAI/Users")

        val username = getEmailPrefix(email)

        database.child(username)
            .get()
            .addOnSuccessListener {
                if (it.exists()){

                    val sharedPref = requireContext().getSharedPreferences(Keys.SHARED_PREF, Context.MODE_PRIVATE)
                    val editor = sharedPref.edit()

                    val firstName = it.child("first_name").value.toString()
                    val lastName = it.child("last_name").value.toString()
                    val credits = it.child("credit").value as Long
                    val passwordFromDB = it.child("password").value.toString()
                    val apiNumber = generateRandomNumber()

                    if (password == passwordFromDB){

                        editor.putString(Keys.FIRST_NAME, firstName)
                        editor.putString(Keys.LAST_NAME, lastName)
                        editor.putInt(Keys.CREDITS, credits.toInt())
                        editor.putString(Keys.EMAIL, email)
                        editor.putString(Keys.HAS_LOGGED_IN, "YES")
                        editor.putString(Keys.USERNAME, username)
                        editor.putInt(Keys.API_KEY_NUMBER, apiNumber)

                        editor.apply()

                        startActivity(Intent(requireContext(), Dashboard::class.java))
                        Animatoo.animateFade(requireContext());

                        Toast.makeText(requireContext(), "Welcome $firstName!", Toast.LENGTH_SHORT).show()

                    }
                    else{
                        Toast.makeText(requireContext(), "Wrong password!", Toast.LENGTH_SHORT).show()
                    }
                }
                else{
                    Toast.makeText(requireContext(), "No such user exists!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { Toast.makeText(requireContext(), "Login failed!", Toast.LENGTH_SHORT).show() }
    }


    //  get User name
    fun getEmailPrefix(email: String): String {
        return email.substringBefore("@")
    }

    // Validate email
    private fun isValidEmail(email: String): Boolean {
        val pattern = """^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$""".toRegex()
        return pattern.matches(email)
    }

    // Random number
    private fun generateRandomNumber(): Int {
        return Random.nextInt(1, 9)
    }
}