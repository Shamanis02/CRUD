package com.crud

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.crud.databinding.UserBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class UserPage : ComponentActivity() {
    private lateinit var binding: UserBinding

    private lateinit var databaseRef: DatabaseReference

    fun onLogin(v: View) {
        var firebase = binding.firebase.text.toString()
        val username = binding.username.text.toString()

        try {
            databaseRef = FirebaseDatabase.getInstance(firebase).getReference(username)
        } catch (e: Exception) {
            Toast.makeText(this, "Invalid firebase url", Toast.LENGTH_SHORT).show()
            return
        }

        val preferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString("username", binding.username.text.toString())
        editor.putString("firebase", binding.firebase.text.toString())
        editor.apply()

        val intent = Intent(this@UserPage, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun onLocal(v: View) {
        val preferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean("local", true)
        editor.apply()

        val intent = Intent(this@UserPage, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = UserBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val preferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean("local", false)
        editor.apply()

        val extraUsername = preferences.getString("username", "")
        val extraFirebase = preferences.getString("firebase", "")

        if (extraUsername != null) {
            binding.username.setText(extraUsername)
        }

        if (extraFirebase != null) {
            binding.firebase.setText(extraFirebase)
        }

    }
}