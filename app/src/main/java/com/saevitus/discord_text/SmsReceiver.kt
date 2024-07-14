package com.saevitus.discord_text

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.telephony.SmsMessage

class SmsReceiver : BroadcastReceiver() {
  private val jda
    get() = DiscordApplication.jda

  private val isJDAAvailable
    get() = DiscordApplication.isJDAAvailable

  // Function to send SMS
  fun sendSMS(phoneNumber: String, message: String) {
    val smsManager = SmsManager.getDefault()
    smsManager.sendTextMessage(phoneNumber, null, message, null, null)
  }

  override fun onReceive(context: Context, intent: Intent) {
    if (isJDAAvailable) {
      val bundle = intent.extras
      val format = bundle!!.getString("format")
      val pdus = bundle["pdus"] as Array<*>
      val messages = arrayOfNulls<SmsMessage>(pdus.size)

      val sharedPref = context.getSharedPreferences("MyApp", Context.MODE_PRIVATE)
      val defaultValue = ""
      val userId = sharedPref.getString("discord_user_id", defaultValue)

      for (i in pdus.indices) {
        messages[i] = SmsMessage.createFromPdu(pdus[i] as ByteArray, format)
      }

      val sender = messages[0]?.originatingAddress ?: return
      val body = messages.joinToString(separator = "") { it?.messageBody ?: "" }
      if (userId != null) {

        jda.retrieveUserById(userId).queue { user ->
          user.openPrivateChannel().queue { privateChannel ->
            privateChannel.sendMessage("Received SMS from $sender: $body").queue()
          }
        }
      }
    }
  }
}
