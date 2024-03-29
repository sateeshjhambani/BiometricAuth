package com.sateeshjh.biometricauth

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.sateeshjh.biometricauth.BiometricAuthManager.BiometricResult
import com.sateeshjh.biometricauth.ui.theme.BiometricAuthTheme

class MainActivity : AppCompatActivity() {

    private val authManager by lazy {
        BiometricAuthManager(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BiometricAuthTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val biometricResult by authManager.promptResults.collectAsState(initial = null)

                    val enrollLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.StartActivityForResult(),
                        onResult = { activityResult ->
                            println("Activity Result: $activityResult")
                        }
                    )
                    LaunchedEffect(biometricResult) {
                        if (biometricResult is BiometricResult.AuthenticationNotSet && Build.VERSION.SDK_INT >= 30) {
                            val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                                putExtra(
                                    Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                    authManager.authenticators
                                )
                            }
                            enrollLauncher.launch(enrollIntent)
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(onClick = {
                            authManager.showBiometricPrompt(
                                title = "Please validate your biometrics",
                                description = "Use the same credentials you use to unlock your phone"
                            )
                        }) {
                            Text(text = "Authenticate")
                        }

                        biometricResult?.let { result ->
                            Text(
                                text = when (result) {
                                    is BiometricResult.AuthenticationError -> result.error
                                    BiometricResult.AuthenticationFailed -> "Authentication failed"
                                    BiometricResult.AuthenticationNotSet -> "Authentication not set"
                                    BiometricResult.AuthenticationSuccess -> "Authentication success"
                                    BiometricResult.FeatureUnavailable -> "Biometrics unavailable"
                                    BiometricResult.HardwareUnavailable -> "Biometrics unavailable"
                                }
                            )

                        }
                    }
                }
            }
        }
    }
}