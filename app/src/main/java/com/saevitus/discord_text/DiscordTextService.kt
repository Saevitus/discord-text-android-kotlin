package com.saevitus.discord_text

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.provider.Telephony
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.slash
import dev.minn.jda.ktx.interactions.commands.updateCommands
import dev.minn.jda.ktx.jdabuilder.intents
import dev.minn.jda.ktx.jdabuilder.light
import java.util.concurrent.Executors
import java.util.concurrent.ForkJoinPool
import kotlin.concurrent.thread
import kotlin.math.max
import kotlin.time.Duration.Companion.minutes
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.GatewayIntent

class DiscordTextService : Service() {
  private val channelId = "discord_text_channel"
  private val notificationId = 1
  private lateinit var smsReceiver: SmsReceiver

  private val jda
    get() = DiscordApplication.jda

  private val isJDAAvailable
    get() = DiscordApplication.isJDAAvailable

  private lateinit var token: String

  fun getThreadCount(): Int = max(2, ForkJoinPool.getCommonPoolParallelism())

  private val pool =
      Executors.newScheduledThreadPool(getThreadCount()) {
        thread(start = false, name = "Worker-Thread", isDaemon = true, block = it::run)
      }

  override fun onCreate() {
    super.onCreate()
    Log.d("DiscordTextService", "Service created")

    createNotificationChannel()
    val notification =
        NotificationCompat.Builder(this, channelId)
            .setContentTitle("Discord Text Service")
            .setContentText("Service is running in the background")
            .setSmallIcon(R.drawable.ic_notification) // replace with your icon
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

    startForeground(notificationId, notification)

    // initializeJDA(token)
  }

  private fun initializeJDA(token: String?) {
    if (token == null) {
      Log.e("DiscordTextService", "Token not provided!")
      return
    }

    Log.d("DiscordTextService", "Token provided: $token")

    if (!isJDAAvailable) {
      Log.d("DiscordTextService", "Initializing JDA.")
      try {
        DiscordApplication.jda =
            light(token, enableCoroutines = true) {
                  intents +=
                      listOf(
                          GatewayIntent.DIRECT_MESSAGES,
                          GatewayIntent.MESSAGE_CONTENT,
                          GatewayIntent.GUILD_MESSAGES,
                          GatewayIntent.GUILD_MEMBERS,
                          GatewayIntent.GUILD_PRESENCES)
                }
                .awaitReady()
       // DiscordApplication.isJDAAvailable = true
        Log.d("DiscordTextService", "JDA initialized successfully.")
      } catch (e: Exception) {
        Log.e("DiscordTextService", "Error initializing JDA", e)
      }
    }

    if (isJDAAvailable) {
      Log.d("DiscordTextService", "JDA is available!")
      try {
        Log.d("DiscordTextService", "Initializing SMS receiver.")
        smsReceiver = SmsReceiver()
        val filter = IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)
        registerReceiver(smsReceiver, filter)

        val toast = Toast.makeText(applicationContext, "Connected to Discord", Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 0)
        toast.show()
        print("Connected to the Discord API")

        jda.listener<MessageReceivedEvent> {
          print(it.message.contentRaw)

          // if (!it.author.isBot)
          // smsReceiver.sendSMS("+61420545596", it.message.contentRaw)
        }

        jda.updateCommands {
              slash("text", "Send a text message") {
                option<String>("number", "Number to send text to")
                option<String>("message", "The message to send")
              }
            }
            .queue()

        jda.updateCommands {
              slash("sms", "Send a text message") {
                option<String>("number", "Number to send text to")
                option<String>("message", "The message to send")
              }
            }
            .queue()

        /*jda.updateCommands().addCommands().slash("text", "Send text to specified number.") {
            option<String>("number", "The number to send a text to", true)
            option<String>("message", "the message you're sending", true)
        }*/

        jda.onCommand("text", timeout = 2.minutes) { event ->
          event.deferReply(true).queue()
          val number = event.getOption("number")?.asString ?: ""
          val message = event.getOption("message")?.asString ?: ""
          if (number.isNotEmpty() && message.isNotEmpty()) {
            smsReceiver.sendSMS(number, message)
            // event.reply("Text message sent.").queue()
            event.hook.sendMessage("SMS message sent to $number with message: $message").queue()
          } else {
            print("command not working")
          }
        }

        jda.onCommand("sms", timeout = 2.minutes) { event ->
          event.deferReply(true).queue()
          val number = event.getOption("number")?.asString ?: ""
          val message = event.getOption("message")?.asString ?: ""
          if (number.isNotEmpty() && message.isNotEmpty()) {
            smsReceiver.sendSMS(number, message)
            // event.reply("Text message sent.").queue()
            event.hook.sendMessage("SMS message sent to $number with message: $message").queue()
          } else {
            print("command not working")
          }
        }

        print("Finished Discord setup.")
      } catch (e: Exception) {
        Log.e("DiscordTextService", "Error Set-Up SMS receiver and Discord Commands", e)
      }
    } else {
      Log.e(
          "DiscordTextService",
          "Failed to Initialize JDA. SMS receiver and Discord Commands Set-Up skipped.")
    }
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    val token = intent?.getStringExtra("TOKEN")
    Log.d("DiscordTextService", "Service started")
    if (token != null) {
      Log.d("DiscordTextService", "Token is good")
      initializeJDA(token)
    } else {
      Log.e("DiscordTextService", "Token not provided!")
    }
    return START_STICKY
  }

  override fun onDestroy() {
    super.onDestroy()
    Log.d("DiscordTextService", "Service destroyed")
    jda.shutdown()
  }

  override fun onBind(intent: Intent?): IBinder? {
    return null
  }

  private fun createNotificationChannel() {
    val name = "Your Service Channel"
    val descriptionText = "Channel for your background service"
    val importance = NotificationManager.IMPORTANCE_LOW
    val channel =
        NotificationChannel(channelId, name, importance).apply { description = descriptionText }
    val notificationManager = getSystemService(NotificationManager::class.java)
    notificationManager?.createNotificationChannel(channel)
  }

  fun print(text: String) {
    // Save logs to SharedPreferences
    val sharedPreferences = getSharedPreferences("MyApp", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putString("logs", text)
    editor.apply()

    // Broadcast Intent
    val intent = Intent("logUpdate")
    intent.putExtra("log", text)
    LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
  }
}
