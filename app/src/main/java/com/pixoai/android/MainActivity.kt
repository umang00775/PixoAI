package com.pixoai.android

import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.pixoai.android.authentication.Authentication
import com.pixoai.android.data.Keys
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.view.animation.Animation
import android.view.animation.AnimationUtils


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        animator()

        val isConnected = checkInternetConnection(applicationContext)


        Handler().postDelayed({
            val sharedPrefs = this.getSharedPreferences(Keys.SHARED_PREF, Context.MODE_PRIVATE)
            val hasLoggedIn = sharedPrefs.getString(Keys.HAS_LOGGED_IN, "NO")

            if (isConnected) {
                if (hasLoggedIn == "YES"){
                    startActivity(Intent(this, Dashboard::class.java))
                    Animatoo.animateFade(this);
                }
                else{
                    startActivity(Intent(this, Authentication::class.java))
                    Animatoo.animateFade(this)
                }
            } else {
                startActivity(Intent(this, NoInternet::class.java))
                Animatoo.animateFade(this);
            }


        },2500)
    }


    // Applying animation
    private fun animator(){
        val animate: Animation = AnimationUtils.loadAnimation(this, R.anim.logo_animation)
        val appLogo = findViewById<TextView>(R.id.logo_text)
        appLogo.startAnimation(animate)
    }

    private fun checkInternetConnection(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }
}