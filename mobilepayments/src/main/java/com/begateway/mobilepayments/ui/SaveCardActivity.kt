package com.begateway.mobilepayments.ui
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.begateway.mobilepayments.utils.SaveCardSheetDialogFragment

class SaveCardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )

        val token = intent.getStringExtra("cardToken")
        val bottomSheetFragment = SaveCardSheetDialogFragment()
        val args = Bundle()
        args.putString("cardToken", token)
        bottomSheetFragment.arguments = args
        bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
    }
}