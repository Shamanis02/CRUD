package com.crud

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.crud.databinding.AddoreditBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

class AddOrEdit : ComponentActivity() {
    private lateinit var binding: AddoreditBinding

    private lateinit var databaseRef: DatabaseReference

    private var dbkey: String = ""

    private val dateFormat: DateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")

    private var username: String = ""

    private var firebase: String = ""

    private fun goToMain() {
        val intent = Intent(this@AddOrEdit, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun save(v: View) {
        val text = binding.todo.text.toString()

        val date = Date()
        val strDate: String = dateFormat.format(date).toString()

        val todoData = TODOData(text, strDate)


        val preferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val local = preferences.getBoolean("local", false)

        if (!local) {
            if (dbkey == "") {
                dbkey = databaseRef.push().key.toString()
            }
            databaseRef.child(dbkey).setValue(todoData).addOnSuccessListener {
                binding.todo.text.clear()

                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
                goToMain()
            }.addOnFailureListener {
                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()

            }
        } else {
            val gson = Gson()
            val editor = preferences.edit()
            val json = gson.toJson(todoData)

            if (dbkey == "") {
                val localTodos = preferences.getStringSet("localTodos", setOf())
                val newSet = localTodos?.toMutableSet()
                newSet?.add(strDate);
                editor.putStringSet("localTodos", newSet)
                editor.putString(strDate, json)
            } else {
                editor.putString(dbkey, json)
            }
            editor.apply()
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
            goToMain()
        }
    }

    fun onBack(v: View) {
        goToMain()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = AddoreditBinding.inflate(layoutInflater)

        val extraKey = intent.extras?.getString("todoKey")

        // https://clouddevs.com/android/storing-and-retrieving-data/
        val preferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val extraUsername = preferences.getString("username", "")
        val extraFirebase = preferences.getString("firebase", "")
        val local = preferences.getBoolean("local", false)

        if (extraUsername != null) {
            username = extraUsername
        }

        if (extraFirebase != null) {
            firebase = extraFirebase
        }

        if (!local) {
            try {
                databaseRef = FirebaseDatabase.getInstance(firebase).getReference(username)
            } catch (e: Exception) {
                val intent = Intent(this@AddOrEdit, UserPage::class.java)
                startActivity(intent)
                Toast.makeText(this, "Invalid firebase url " + firebase, Toast.LENGTH_SHORT).show()
                finish()
            }

            if (extraKey != null) {
                databaseRef.child(extraKey).get().addOnSuccessListener {
                    if (it.exists()) {
                        binding.todo.setText(
                            it.child("text").value.toString(),
                            TextView.BufferType.EDITABLE
                        )
                        dbkey = extraKey;
                    }
                }.addOnFailureListener {
                    goToMain()
                }
            }
        } else {
            if (extraKey != null) {

                val gson = Gson()
                val localTodo = preferences.getString(extraKey, "")
                val todo = gson.fromJson(localTodo, TODOData::class.java)

                binding.todo.setText(todo.text, TextView.BufferType.EDITABLE)
                dbkey = extraKey;
            }
        }

        setContentView(binding.root)

        if (binding.todo.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }
}