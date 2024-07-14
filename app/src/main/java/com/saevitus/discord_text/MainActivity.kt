package com.saevitus.discord_text

import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.dv8tion.jda.api.JDA

class MainActivity : AppCompatActivity() {
  private val READ_SMS_PERMISSION_CODE: Int = 1
  private lateinit var consoleAdapter: ConsoleAdapter
  private val consoleLines = mutableListOf<ConsoleLine>()
  private lateinit var jda: JDA
  private lateinit var smsReceiver: SmsReceiver

  lateinit var tokenEditText: EditText

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val consoleRecyclerView: RecyclerView = findViewById(R.id.consoleRecyclerView)

    consoleAdapter = ConsoleAdapter(consoleLines)

    consoleRecyclerView.layoutManager = LinearLayoutManager(this)
    consoleRecyclerView.adapter = consoleAdapter

    consoleAdapter.addLine(ConsoleLine("App started", level = "INFO"))
    consoleRecyclerView.smoothScrollToPosition(consoleAdapter.itemCount - 1)

    tokenEditText = findViewById(R.id.tokenEditText)

    val startServiceButton: Button = findViewById(R.id.startServiceButton)
    // val tokenEditText: EditText = findViewById(R.id.tokenEditText)

    tokenEditText = findViewById(R.id.tokenEditText)

    // get SharedPreferences instance
    val sharedPref = getSharedPreferences("MyApp", Context.MODE_PRIVATE)

    // retrieve the token
    val defaultValue = ""
    val token = sharedPref.getString("discord_bot_token", defaultValue)

    // set the token as the text for EditText
    tokenEditText.setText(token)

    // adding a TextWatcher to save changes
    tokenEditText.addTextChangedListener(
        object : TextWatcher {
          override fun afterTextChanged(s: Editable) {
            val editor = sharedPref.edit()
            editor.putString("discord_bot_token", s.toString())
            editor.apply()
          }

          override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

          override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

    var userIdEditText: EditText

    // Add this inside onCreate function after tokenEditText is set
    userIdEditText = findViewById(R.id.userIdEditText)

    // Retrieve the userId
    val userId = sharedPref.getString("discord_user_id", defaultValue)

    // Set the userId as the text for EditText
    userIdEditText.setText(userId)

    // Adding a TextWatcher to save changes
    userIdEditText.addTextChangedListener(
        object : TextWatcher {
          override fun afterTextChanged(s: Editable) {
            val editor = sharedPref.edit()
            editor.putString("discord_user_id", s.toString())
            editor.apply()
          }

          override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

          override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

    startServiceButton.setOnClickListener {
      val token = tokenEditText.text.toString().trim()
      println("Token: $token")
      if (token.isEmpty()) {
        Toast.makeText(this, "Bot token should not be empty", Toast.LENGTH_SHORT).show()
      } else {
        startYourService(token) // Here you start your service with the token
        // tokenEditText.text.clear()
      }
    }

    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_SMS) !=
        PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECEIVE_SMS) !=
            PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) !=
            PackageManager.PERMISSION_GRANTED) {

      val permissions =
          arrayOf(
              android.Manifest.permission.READ_SMS,
              android.Manifest.permission.RECEIVE_SMS,
              android.Manifest.permission.SEND_SMS)

      val requestCode = READ_SMS_PERMISSION_CODE
      ActivityCompat.requestPermissions(this, permissions, requestCode)
    }
  }

  override fun onDestroy() {
    super.onDestroy()

    unregisterReceiver(smsReceiver)
  }

  override fun onRequestPermissionsResult(
      requestCode: Int,
      permissions: Array<String?>,
      grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == READ_SMS_PERMISSION_CODE) {
      val permissionsResult = permissions.zip(grantResults.toList()).toMap()
      if (permissionsResult[android.Manifest.permission.READ_SMS] ==
          PackageManager.PERMISSION_GRANTED &&
          permissionsResult[android.Manifest.permission.RECEIVE_SMS] ==
              PackageManager.PERMISSION_GRANTED &&
          permissionsResult[android.Manifest.permission.SEND_SMS] ==
              PackageManager.PERMISSION_GRANTED) {

        // All SMS permissions granted
        // Optionally read SMS or perform other actions
      } else {
        // At least one of the permissions was not granted. Handle the error.
        // This is where you can display a message to the user explaining why you need
        // the permission and how to manually enable it in the settings
      }
    }
  }

  private fun startYourService(token: String) {
    val serviceIntent =
        Intent(this, DiscordTextService::class.java).apply { putExtra("TOKEN", token) }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      startForegroundService(serviceIntent)
    } else {
      startService(serviceIntent)
    }
  }

  private val logUpdateReceiver =
      object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
          val consoleRecyclerView: RecyclerView = findViewById(R.id.consoleRecyclerView)
          val sharedPreferences = getSharedPreferences("MyApp", Context.MODE_PRIVATE)
          val log = sharedPreferences.getString("logs", "")

          log?.let { entry ->
            consoleAdapter.addLine(ConsoleLine(entry, level = "INFO"))
            consoleRecyclerView.scrollToPosition(
                consoleAdapter.itemCount - 1) // Scroll to the bottom of the RecyclerView
          }
        }
      }

  override fun onResume() {
    super.onResume()
    LocalBroadcastManager.getInstance(this)
        .registerReceiver(logUpdateReceiver, IntentFilter("logUpdate"))
  }

  override fun onPause() {
    super.onPause()
    LocalBroadcastManager.getInstance(this).unregisterReceiver(logUpdateReceiver)
  }
}
