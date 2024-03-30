# BiometricAuth

This sample app describes the basic usage of the Biometric API to incorporate on-device authentication use cases, validating the user's fingerprint, face, PIN, pattern, or password.

## Usage

Before showing the biometric verification prompt, we must check whether the biometrics are available on this device. We're using two kinds of authenticating methods:

• BIOMETRIC_STRONG (fingerprint/face auth)

• DEVICE_CREDENTIAL (PIN, pattern, or password)

For consistency's sake, the biometric prompt has a system-defined UI, but some customization is available. Stuff like the title, subtitle, description, and negative button text can be personalized.

We receive various errors and add these events to a results channel, which our activity collects and shows an error message accordingly. 

```kotlin
val authenticators = if (Build.VERSION.SDK_INT >= 30) {
        BIOMETRIC_STRONG or DEVICE_CREDENTIAL
    } else BIOMETRIC_STRONG

val promptInfo = PromptInfo.Builder()
    .setTitle(title)
    .setDescription(description)
    .setNegativeButtonText("Cancel")
    .setAllowedAuthenticators(authenticators)

val manager = BiometricManager.from(activity)

when (manager.canAuthenticate(authenticators)) {
    BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
        resultChannel.trySend(BiometricResult.HardwareUnavailable)
        return
    }

    BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
        resultChannel.trySend(BiometricResult.FeatureUnavailable)
        return
    }

    BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
        resultChannel.trySend(BiometricResult.AuthenticationNotSet)
        return
    }

    else -> Unit
}
```

If we 'canAuthenticate', we show our biometric auth prompt and receive the auth results with the implemented callback.

```kotlin
val prompt = BiometricPrompt(
    activity,
    object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            resultChannel.trySend(BiometricResult.AuthenticationError(errString.toString()))
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            resultChannel.trySend(BiometricResult.AuthenticationSuccess)

        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            resultChannel.trySend(BiometricResult.AuthenticationFailed)
        }
    }
)
prompt.authenticate(promptInfo.build())
```



## References

[Docs](https://developer.android.com/training/sign-in/biometric-auth)

[Android Developers Blog](https://android-developers.googleblog.com/2019/10/one-biometric-api-over-all-android.html)
