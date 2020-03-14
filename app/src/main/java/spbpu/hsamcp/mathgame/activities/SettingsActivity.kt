package spbpu.hsamcp.mathgame.activities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import spbpu.hsamcp.mathgame.R

class SettingsActivity: AppCompatActivity() {
    private val TAG = "SettingsActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }

    override fun onResume() {
        super.onResume()
    }
}