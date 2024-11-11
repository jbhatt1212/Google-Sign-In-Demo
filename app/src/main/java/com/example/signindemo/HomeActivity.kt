package com.example.signindemo

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.example.signindemo.databinding.ActivityHomeBinding
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {
    lateinit var binding: ActivityHomeBinding
    private val Context.userPreferencesDataStore : DataStore<Preferences> by preferencesDataStore(
        name = "user"
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        val credentialManager = CredentialManager.create(this)

//        binding.btnSignOut.setOnClickListener {
//            lifecycleScope.launch{
//      userPreferencesDataStore.edit { it.clear() }
//
//                val request = ClearCredentialStateRequest()
//                credentialManager.clearCredentialState(request)
//                Log.e("success", "logout")
//            }




    }
}