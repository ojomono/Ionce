package com.ojomono.ionce.ui.splashscreen

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ojomono.ionce.MainActivity
import com.ojomono.ionce.R
import com.ojomono.ionce.utils.Constants
import com.ojomono.ionce.utils.FirebaseProxy


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Open sign-in screen
        startActivityForResult(
            FirebaseProxy.buildSignInIntent(packageName, intent),
            Constants.RC_SIGN_IN
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Constants.RC_SIGN_IN) {

            if (resultCode == Activity.RESULT_OK) {

                // Successfully signed in
//                val user = FirebaseAuth.getInstance().currentUser

                // Open home screen
                startMainActivity()

            } else if (FirebaseProxy.handleSignInFailed(data)) finish()    // Sign in failed.
        }
    }

    private fun startMainActivity(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}