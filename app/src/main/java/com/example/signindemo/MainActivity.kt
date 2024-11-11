package com.example.signindemo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.signindemo.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var mGoogleSignInClient: GoogleSignInClient
    val Req_Code: Int = 123
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FirebaseApp.initializeApp(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.btnSign.setOnClickListener { view: View? ->
            Toast.makeText(this, "Logging In", Toast.LENGTH_SHORT).show()
            signInGoogle()
        }
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
    }

    private fun signInGoogle() {
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, Req_Code)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Req_Code) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleResult(task)
        }
    }

    private fun handleResult(completedTask: Task<GoogleSignInAccount>) {

        try {
            val account = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                Log.d("MainActivity", "Google Sign-In successful: Account Email = ${account.email}")
                updateUI(account)
            } else {
                Log.e("MainActivity", "Google Sign-In failed: Account is null")
                Toast.makeText(this, "Sign-In failed: Account is null", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            Log.e("MainActivity", "Google Sign-In error code: ${e.message}, message: ${e.message}")
            Toast.makeText(this, "Sign-In error: ${e.statusCode}", Toast.LENGTH_SHORT).show()
        }
        }


    private fun updateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.e("MainActivity", "Firebase authentication successful")
                Toast.makeText(this, "Authentication Successful", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, DashboardActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Log.e("MainActivity", "Firebase authentication failed: ${task.exception?.message}")
                Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            Log.e("MainActivity", "Firebase sign-in failed with error: ${e.message}")
            Toast.makeText(this, "Sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onStart() {
        super.onStart()
        if (GoogleSignIn.getLastSignedInAccount(this) != null) {
            startActivity(
                Intent(
                    this, DashboardActivity
                    ::class.java
                )
            )
            finish()
        }
    }

}