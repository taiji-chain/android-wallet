package io.taiji.wallet.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.networknt.taiji.crypto.CipherException
import com.networknt.taiji.crypto.ECKeyPair
import com.networknt.taiji.crypto.Keys
import io.taiji.wallet.R
import io.taiji.wallet.data.FullWallet
import io.taiji.wallet.utils.AddressNameConverter
import io.taiji.wallet.utils.OwnWalletUtils
import io.taiji.wallet.utils.WalletStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.security.InvalidAlgorithmParameterException
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException

class WalletGenRunnerService : Service() {

    companion object {
        const val notificationId = 93430
        var isRunning = false
        var password = ""
        var bankId = ""
    }

    private val notification by lazy {
        NotificationCompat.Builder(this, "progress_channel")
                .setSmallIcon(R.drawable.ic_progress)
                .setContentTitle("Processing your images ...")
                .setProgress(100, 0, true)
                .build()
    }

    // for android version Oreo and above, we first need to create a notification channel
    private fun createNotificationChannel() {
        // you can create multiple channels and deliver different type of notifications through different channels
        val notificationChannel = NotificationChannel("progress_channel", "Progress", NotificationManager.IMPORTANCE_DEFAULT)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
    }

    override fun onCreate() {
        super.onCreate()
        Log.i("TAG", "onCreate is called")
        isRunning = true
        var count = 0
        var keyPair : ECKeyPair
        CoroutineScope(Dispatchers.Default).launch {
            while(true) {
                count++
                // set the progressBar to the current Progress
                val notification = NotificationCompat.Builder(baseContext, "progress_channel")
                        .setSmallIcon(R.drawable.ic_progress)
                        .setContentTitle("Scanning address space count : $count")
                        .setProgress(100, 50, false)
                        // setting the notification as ongoing prevents the user from dismissing it
                        .setOngoing(true)
                        .build()
                // show a new notification with the same ID, it essentially cancels the older one and shows the new notification
                val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(notificationId, notification)
                keyPair = generateKeyPair()
                //Log.i("TAG", "generate address: " + Keys.getAddress(keyPair))
                if(Keys.getAddress(keyPair).startsWith(bankId)) {
                    break;
                }
            }
            Log.i("TAG", "found the address and the loop is broken")

            try {
                val walletAddress = OwnWalletUtils.generateWalletFile(password, keyPair, Keys.createCipherKeyPair(), File(getFilesDir(), ""), true)
                Log.i("TAG", "walletAddress = " + walletAddress);
                WalletStorage.getInstance(this@WalletGenRunnerService).add(FullWallet(walletAddress, walletAddress), this@WalletGenRunnerService)
                AddressNameConverter.getInstance(this@WalletGenRunnerService).put(walletAddress, "Wallet " + walletAddress.substring(0, 6), this@WalletGenRunnerService)
            } catch (e: CipherException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: InvalidAlgorithmParameterException) {
                e.printStackTrace()
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            } catch (e: NoSuchProviderException) {
                e.printStackTrace()
            }
            Log.i("TAG", "wallet file is generated!")
            // stop the foreground service and remove notification
            stopForeground(true)
            stopSelf()
        }
    }

    private suspend fun generateKeyPair() : ECKeyPair = withContext(Dispatchers.Default) {
        Keys.createEcKeyPairUnsafe();
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int) : Int {
        password = intent.getStringExtra("PASSWORD").toString()
        bankId = intent.getStringExtra("BANK_ID").toString()
        Log.i("TAG", password + " " + bankId)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        isRunning = false
        Log.e("TAG", "onDestroy called")
        super.onDestroy()
    }
}