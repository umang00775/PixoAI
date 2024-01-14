package com.pixoai.android

import android.app.AlertDialog
import android.app.Dialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import com.github.ybq.android.spinkit.style.DoubleBounce
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.github.ybq.android.spinkit.SpinKitView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.firebase.database.FirebaseDatabase
import com.pixoai.android.data.Keys
import com.squareup.picasso.Picasso
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.lang.ref.WeakReference
import kotlin.properties.Delegates
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Timer
import java.util.TimerTask


class Dashboard : AppCompatActivity() {

    private lateinit var level1: RelativeLayout
    private lateinit var level2: LinearLayout
    private lateinit var level3: RelativeLayout
    private lateinit var queryEditText: EditText
    private lateinit var context: Context
    private lateinit var adSpinner: SpinKitView
    private lateinit var database: FirebaseDatabase
    private lateinit var creditView: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var username: String
    private lateinit var vibrator: Vibrator
    private lateinit var resultImageView: ImageView
    private val totalKeys: Int = 8
    private var isConnectedToInternet by Delegates.notNull<Boolean>()
    private var credits by Delegates.notNull<Int>()
    private var apiNumber by Delegates.notNull<Int>()
    private var client = OkHttpClient()
    private var resultImageUrl: String? = null
    private var rewardedAd: RewardedAd? = null

    companion object{
        val JSON: MediaType = "application/json; charset=utf-8".toMediaType()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        context = this

//        MobileAds.initialize(this) {}
//        val mAdView = findViewById<AdView>(R.id.banner_ad)
//        val mAdView2 = findViewById<AdView>(R.id.banner_ad2)
//        val adRequest = AdRequest.Builder().build()
//        mAdView.loadAd(adRequest)
//        mAdView2.loadAd(adRequest)


        // Load an ad
//        RewardedAd.load(this,"ca-app-pub-8878970959944694/2204919253", adRequest, object : RewardedAdLoadCallback() {
//            override fun onAdFailedToLoad(adError: LoadAdError) {
//                Toast.makeText(this@Dashboard, "Failed to load the ad.", Toast.LENGTH_SHORT).show()
//                rewardedAd = null
//            }
//            override fun onAdLoaded(ad: RewardedAd) {
//                rewardedAd = ad
//            }
//        })


        initialSetUp()


        val genImage = findViewById<RelativeLayout>(R.id.generate_image)
        genImage.setOnClickListener {

            // Hide key board
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)

            val query = queryEditText.text.toString()
            if (query.isEmpty()){
                Toast.makeText(this, "Input cannot be empty!", Toast.LENGTH_SHORT).show()
            }
            else{
                apiNumber = sharedPreferences.getInt(Keys.API_KEY_NUMBER, 1)

//                if(credits < 5){
//                    showCreditPopup(query)
//                }
//                else{
//                    credits -= 5
//                    editor.putInt(Keys.CREDITS, credits)
//
//                    editor.putInt(Keys.API_KEY_NUMBER, (apiNumber+1)%totalKeys + 1)
//                    editor.apply()
//
//                    changeCredit(username, credits)
//
//                    showLevel2(query)
//
//                }
                showLevel2(query)
            }
        }
    }


    // Initial set up
    private fun initialSetUp(){

        val tag1 = findViewById<TextView>(R.id.tag_1)
        val tag2 = findViewById<TextView>(R.id.tag_2)
        val tag3 = findViewById<TextView>(R.id.tag_3)
        val tag4 = findViewById<TextView>(R.id.tag_4)
        val appName = findViewById<RelativeLayout>(R.id.app_name)

        val opacity = 0.25f

        tag1.alpha = opacity
        tag2.alpha = opacity
        tag3.alpha = opacity
        tag4.alpha = opacity
        appName.alpha = opacity

        level1 = findViewById(R.id.level_1)
        level2 = findViewById(R.id.level_2)
        level3 = findViewById(R.id.level_3)

        showLevel1()

        queryEditText = findViewById(R.id.input_query)

        // Options for generated image
        val download = findViewById<CardView>(R.id.download_image)
        val shareWhatsapp = findViewById<CardView>(R.id.whatsapp_share)
        val shareInstagram = findViewById<CardView>(R.id.instagram_share)
        val moreOptions = findViewById<CardView>(R.id.more_options)

        val whatsAppDescription = "🎨🤖 Get ready to unleash your creativity with PixoAI! Create amazing art with just a tap using our AI-powered art generator. 🌟✨ Express yourself through digital masterpieces and share them with your friends. Download PixoAI now and let your imagination run wild! 🖌️🚀 \n\nhttps://play.google.com/store/apps/details?id=com.pixoai.android"

        val instagramDescription = """
    🎨🌟 Check out this incredible masterpiece I created with PixoAI! 🤩

    🔥✨ Unleash your creativity with our AI art generator app! 🎉🔥

    📱👉 Get the app here: https://play.google.com/store/apps/details?id=com.pixoai.android

    #PixoAI #AIart #CreativityUnleashed #ArtisticExpressions #AIApp #Masterpiece #Innovation #TechArt #CreativeGenius #DigitalArt #ExploreYourImagination
""".trimIndent()

        val description = "Experience the wonders of PixoAI, where art meets technology! 🎨✨ Create incredible artwork effortlessly using our AI art generator app. 🌟🖌️ Explore the endless possibilities of creativity and let your imagination soar. Download the app now: https://play.google.com/store/apps/details?id=com.pixoai.android"




        download.setOnClickListener {
            resultImageUrl?.let { it1 -> saveImageToGallery(it1) }
        }
        shareWhatsapp.setOnClickListener {
            resultImageUrl?.let { shareImageToWhatsApp(context, resultImageUrl!!, whatsAppDescription) }
        }
        shareInstagram.setOnClickListener {
            resultImageUrl?.let { shareImageToInstagram(context, resultImageUrl!!, instagramDescription) }
        }
        moreOptions.setOnClickListener {
            resultImageUrl?.let { shareImage(context, resultImageUrl!!, description) }
        }


        adSpinner = findViewById(R.id.ad_spinner)
        adSpinner.visibility = View.INVISIBLE



        // Initialize database
//        database = FirebaseDatabase.getInstance()

        // Credit view
//        creditView = findViewById(R.id.show_credit)

        // Shared preferences
        sharedPreferences  = getSharedPreferences(Keys.SHARED_PREF, Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()     /* Editor */

        // Credits and username
        username = sharedPreferences.getString(Keys.USERNAME, "Null").toString()
        credits = sharedPreferences.getInt(Keys.CREDITS, 0)


        // Show name and credits
        val firstName = sharedPreferences.getString(Keys.FIRST_NAME, "Null")
        val showFirstName = findViewById<TextView>(R.id.show_first_name)
//        creditView.text = credits.toString()
        showFirstName.text = firstName.toString() + "!"

        // Set vibrator
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        // Initial visibility value
        level1.alpha = 1f

        // ResultImage View
        resultImageView = findViewById(R.id.result_image)
    }

    private fun showLevel1(){
        runOnUiThread {
            level1.visibility = View.VISIBLE
            level2.visibility = View.INVISIBLE
            level3.visibility = View.INVISIBLE
        }
    }

    private fun showLevel2(query: String){
        runOnUiThread {

            val apiKeys = listOf(Keys.API_KEY1, Keys.API_KEY2,
                Keys.API_KEY3, Keys.API_KEY4, Keys.API_KEY5,
                Keys.API_KEY6, Keys.API_KEY7, Keys.API_KEY8)

            val progressBar: ProgressBar = findViewById(R.id.spin_kit)
            val doubleBounce = DoubleBounce()
            progressBar.indeterminateDrawable = doubleBounce

            level1.visibility = View.INVISIBLE
            level2.visibility = View.VISIBLE
            level3.visibility = View.INVISIBLE


            val jsonBody = JSONObject()
            try{
                jsonBody.put("prompt", query)
                jsonBody.put("page", 1)
            }
            catch (e: Exception){
                e.printStackTrace()
            }

            val body = RequestBody.create(JSON, jsonBody.toString())
            val request = Request.Builder()
                .url("https://ai-image-generator3.p.rapidapi.com/generate")
                .addHeader("content-type", "application/json")
                .addHeader("X-RapidAPI-Key", apiKeys[apiNumber-1])
                .addHeader("X-RapidAPI-Host", "ai-image-generator3.p.rapidapi.com")
                .post(body)
                .build()



            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        val responseJson = JSONObject(response.body!!.string())
                        Log.e("RESPONSE_JSON", responseJson.toString())
                        val resultsJson = responseJson.getJSONObject("results")
                        Log.e("RESULT_JSON", resultsJson.toString())
                        val variationsJson = resultsJson.getJSONArray("variaties")
                        Log.e("VARIATION", variationsJson.toString())
                        val firstVariationJson = variationsJson.getJSONObject(0)
                        Log.e("FIRST_VARIATION", firstVariationJson.toString())
                        val urlsJson = firstVariationJson.getJSONArray("urls")
                        Log.e("URLS", urlsJson.toString())
                        val imageUrl = urlsJson.getString(0)
                        Log.e("IMAGE_URL", imageUrl.toString())

                        resultImageUrl = imageUrl

//                        Log.e("URL", imagesArray.toString())
                        showLevel3(imageUrl)
                    }
                    catch (e: Exception){
                        e.printStackTrace()
                    }
                }
            })
        }
    }

    private fun showLevel3(imageUrl: String){
        runOnUiThread {
            level1.visibility = View.INVISIBLE
            level2.visibility = View.INVISIBLE
            level3.visibility = View.VISIBLE

            Picasso.get().load(imageUrl).into(resultImageView)
        }
    }

    // Download image and save it
    private fun saveImageToGallery(imageUrl: String) {
        val request = DownloadManager.Request(Uri.parse(imageUrl))
            .apply {
                setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
                setTitle("PixoAI")
                setDescription("Image downloaded successfully!")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, "${System.currentTimeMillis()}.jpg")
            }

        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    }

    // Show pop up for less credits than required
    private fun showCreditPopup(query: String){

        level1.alpha = 0.05f

        val dialogView = LayoutInflater.from(this).inflate(R.layout.less_credits_pop_up, null)

        val watchAnAd = dialogView.findViewById<RelativeLayout>(R.id.watch_an_ad)

        watchAnAd.setOnClickListener { buyCredits() }


        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)

        val dialog: Dialog = builder.create()
        dialog.show()

        dialog.setOnDismissListener {
            level1.alpha = 1f
        }


        val mLayoutParams = WindowManager.LayoutParams()
        mLayoutParams.width =  975 /* 975 */
        mLayoutParams.height = 600   /* 600 */
        dialog.window?.attributes = mLayoutParams

        watchAnAd.setOnClickListener {
            adSpinner.visibility = View.VISIBLE
            level1.alpha = 0.05f
            showRewardedAd(query)
            dialog.dismiss()
        }
    }

    private fun showRewardedAd(query: String){
        rewardedAd?.let { ad ->
            ad.show(this, OnUserEarnedRewardListener { rewardItem ->
                val rewardAmount = rewardItem.amount
                val rewardType = rewardItem.type
                adSpinner.visibility = View.GONE

                level1.alpha = 1f
                changeCredit(username, credits+5)
                showLevel2(query)
            })
        } ?: run {
            level1.alpha = 1f
            adSpinner.visibility = View.GONE
        }

    }

    private fun buyCredits(){
        Toast.makeText(this, "Buy credits", Toast.LENGTH_SHORT).show()
    }

    private fun changeCredit(username: String, newCredit: Int) {

        creditView.text = newCredit.toString()

        credits =  newCredit
        editor.putInt(Keys.CREDITS, credits)
        editor.apply()

        val userId = username

        val usersRef = database.getReference("PixoAI/Users")
        val userRef = usersRef.child(userId)
        val creditRef = userRef.child("credit")

        creditRef.setValue(newCredit)
            .addOnSuccessListener {
            }
            .addOnFailureListener { exception ->
            }
    }


    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        initialSetUp()
    }



    private fun shareImage(context: Context, imageUrl: String, description: String) {
        // Convert the image URL to a Bitmap
        val bitmap = getBitmapFromUrl(imageUrl)

        val file = saveBitmapToFile(context, bitmap)

        val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_STREAM, contentUri)
        intent.putExtra(Intent.EXTRA_TEXT, description)

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        // Start the activity to share the image
        context.startActivity(Intent.createChooser(intent, "Share Image"))
    }

    private fun shareImageToInstagram(context: Context, imageUrl: String, description: String) {
        // Convert the image URL to a Bitmap
        val bitmap = getBitmapFromUrl(imageUrl)

        val file = saveBitmapToFile(context, bitmap)

        val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_STREAM, contentUri)
        intent.putExtra(Intent.EXTRA_TEXT, description)

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        intent.setPackage("com.instagram.android")

        // Start the activity to share the image
        context.startActivity(Intent.createChooser(intent, "Share Image"))
    }


    private fun shareImageToWhatsApp(context: Context, imageUrl: String, description: String) {
        // Convert the image URL to a Bitmap
        val bitmap = getBitmapFromUrl(imageUrl)

        val file = saveBitmapToFile(context, bitmap)

        val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_STREAM, contentUri)
        intent.putExtra(Intent.EXTRA_TEXT, description)

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        intent.setPackage("com.whatsapp")

        // Start the activity to share the image
        context.startActivity(Intent.createChooser(intent, "Share Image"))
    }

    private fun getBitmapFromUrl(imageUrl: String): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            bitmap = Glide.with(context)
                .asBitmap()
                .load(imageUrl)
                .submit()
                .get()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bitmap
    }

    private fun saveBitmapToFile(context: Context, bitmap: Bitmap?): File {
        val file = File(context.cacheDir, "art_pixo_ai.png")

        try {
            val fileOutputStream = FileOutputStream(file)
            bitmap?.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return file
    }
}
