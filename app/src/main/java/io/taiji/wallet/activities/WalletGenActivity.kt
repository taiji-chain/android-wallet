package io.taiji.wallet.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import io.taiji.wallet.R
import io.taiji.wallet.services.WalletGenRunnerService
import io.taiji.wallet.utils.BankSpinnerAdapter
import io.taiji.wallet.utils.Dialogs
import io.taiji.wallet.utils.LabelValue

class WalletGenActivity : SecureAppCompatActivity() {
    private var password: EditText? = null
    private var passwordConfirm: EditText? = null
    private var coord: CoordinatorLayout? = null
    private var walletGenText: TextView? = null
    private var toolbar_title: TextView? = null
    private var privateKeyProvided: String? = null
    private var bankId: String? = null
    var labelValues = arrayOf(
            LabelValue("Americas", "0000"),
            LabelValue("Asia, Oceania", "0001"),
            LabelValue("Europe, Africa", "0002")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet_gen)
        password = findViewById<View>(R.id.password) as EditText
        passwordConfirm = findViewById<View>(R.id.passwordConfirm) as EditText
        walletGenText = findViewById<View>(R.id.walletGenText) as TextView
        toolbar_title = findViewById<View>(R.id.toolbar_title) as TextView
        val bankIdSpinner = findViewById<View>(R.id.bankId) as Spinner
        val adapter = BankSpinnerAdapter(this, android.R.layout.simple_spinner_item, labelValues)
        bankIdSpinner.adapter = adapter
        bankIdSpinner.onItemSelectedListener = onItemSelectedListener
        coord = findViewById<View>(R.id.main_content) as CoordinatorLayout
        val mEmailSignInButton = findViewById<View>(R.id.email_sign_in_button) as Button
        mEmailSignInButton.setOnClickListener { genCheck() }
        if (intent.hasExtra("PRIVATE_KEY")) {
            privateKeyProvided = intent.getStringExtra("PRIVATE_KEY")
            walletGenText!!.text = resources.getText(R.string.import_text)
            toolbar_title!!.setText(R.string.import_title)
            mEmailSignInButton.setText(R.string.import_button)
        }
    }

    var onItemSelectedListener: OnItemSelectedListener = object : OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View,
                                    position: Int, id: Long) {
            val obj = parent.getItemAtPosition(position) as LabelValue
            bankId = obj.value
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }

    private fun genCheck() {
        if (passwordConfirm!!.text.toString() != password!!.text.toString()) {
            snackError(resources.getString(R.string.error_incorrect_password))
            return
        }
        if (!isPasswordValid(passwordConfirm!!.text.toString())) {
            snackError(resources.getString(R.string.error_invalid_password))
            return
        }
        if (bankId == null) {
            snackError("Please select a region")
            return
        }
        Dialogs.writeDownPassword(this)
    }

    /*
    fun gen() {
        Settings.walletBeingGenerated = true // Lock so a user can only generate one wallet at a time
        val data = Intent()
        data.putExtra("PASSWORD", passwordConfirm!!.text.toString())
        data.putExtra("BANK_ID", bankId)
        if (privateKeyProvided != null) data.putExtra("PRIVATE_KEY", privateKeyProvided)
        setResult(Activity.RESULT_OK, data)
        finish()
    }
    */

    fun startWalletGenRunnerSerivce() {
        val intent = Intent(this, WalletGenRunnerService::class.java)
        intent.putExtra("PASSWORD", passwordConfirm!!.text.toString())
        intent.putExtra("BANK_ID", bankId)
        Log.i("TAG", "Service status: ${WalletGenRunnerService.isRunning}")
        if (!WalletGenRunnerService.isRunning)
            ContextCompat.startForegroundService(this, intent)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    fun snackError(s: String?) {
        if (coord == null) return
        val mySnackbar = Snackbar.make(coord!!, s!!, Snackbar.LENGTH_SHORT)
        mySnackbar.show()
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length >= 9
    }

    companion object {
        const val REQUEST_CODE = 401
    }
}