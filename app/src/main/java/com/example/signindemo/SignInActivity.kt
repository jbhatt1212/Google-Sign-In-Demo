package com.example.signindemo

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.preferencesDataStore
import androidx.credentials.exceptions.GetCredentialException
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.lifecycleScope
import com.example.signindemo.databinding.ActivitySignInBinding
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding
    private lateinit var googleOption: GetGoogleIdOption
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
        name = "user"
    )

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge()
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // There are no request codes
                    val data: Intent? = result.data


                }
            }
        val credentialManager = CredentialManager.create(this)
        lifecycleScope.launch {
            val data = getData("email")
            if (data != null) {
                Toast.makeText(this@SignInActivity, "user sign in", Toast.LENGTH_LONG).show()
                startActivity(Intent(this@SignInActivity, HomeActivity::class.java))
            }
        }
        binding.btnSignOut.setOnClickListener {
            lifecycleScope.launch {
                userPreferencesDataStore.edit { it.clear() }

                val request = ClearCredentialStateRequest()
                credentialManager.clearCredentialState(request)
                Log.e("success", "logout")
            }
        }
        binding.btnSign.setOnClickListener {
            signInWithGoogle(credentialManager)
        }

    }

    @SuppressLint("NewApi")
    private fun signInWithGoogle(credentialManager: CredentialManager) {
        // Generate a secure nonce for the request
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

        // Set up Google ID options for the request
        googleOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId("your_client_id")
            .setAutoSelectEnabled(true)
            .setNonce(hashedNonce)
            .build()

        // Create the GetCredentialRequest with Google ID option
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleOption)
            .build()

        lifecycleScope.launch {
            try {
                // Request credentials
                val result: GetCredentialResponse = credentialManager.getCredential(
                    request = request,
                    context = this@SignInActivity
                )
                handleSignIn(result)
            } catch (e: GetCredentialException) {
                Log.e("SignIn", "Error getting credential: ", e)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun handleSignIn(result: GetCredentialResponse) {

        val credential = result.credential
        when (credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential =
                            GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken
                        val personId = googleIdTokenCredential.id
                        Log.e("persionid", "id :${personId}")
                        val displayName = googleIdTokenCredential.displayName
                        Log.e("display", "name ${displayName}")

                        // Save sign-in status to SharedPreferences
                        val intent = Intent(this, HomeActivity::class.java)
                        activityResultLauncher.launch(intent)
                        lifecycleScope.launch {
                            saveData("email", personId)
                        }
                        // Use idToken on your backend server for verification
                        validateGoogleIdToken(idToken)
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Received an invalid Google ID token response", e)
                    }
                } else {
                    Log.e(TAG, "Unexpected type of custom credential")
                }
            }

            else -> {
                Log.e(TAG, "Unexpected type of credential")
            }
        }
    }

    private suspend fun saveData(key: String, text: String) {
        userPreferencesDataStore.edit { preferences ->
            preferences[stringPreferencesKey(key)] = text
        }
    }

    private suspend fun getData(key: String): String? {
        val value = userPreferencesDataStore.data
            .map { it ->
                it[stringPreferencesKey(key)]
            }
        return value.firstOrNull()
    }

    private fun validateGoogleIdToken(idToken: String?) {
        if (idToken != null) {
            Log.e(TAG, "Received ID Token: $idToken")
            // Send this ID token to your backend server for verification
        } else {
            Log.e(TAG, "Invalid Google ID token received.")
        }

    }

}
