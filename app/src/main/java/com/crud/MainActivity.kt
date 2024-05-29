package com.crud

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.crud.databinding.MainBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson


class MainActivity : ComponentActivity() {

    private lateinit var databaseRef: DatabaseReference

    private lateinit var binding: MainBinding

    private var todos: Map<String, TODOData> = mutableMapOf()

    private var fromOldest = false

    private var search: String = ""

    private fun updateRecycledView() {
        var todoslist = todos.toList().sortedBy { it.second.date }

        if (!fromOldest) {
            todoslist = todoslist.reversed()
        }

        todos = todoslist.toMap()
        binding.recyclerView.setAdapter(MyAdapter(todos.filter {
            val text = it.value.text;(text is String) && text.contains(search)
        }, this))
    }

    fun onSearch(v: View) {
        search = binding.searchTodo.text.toString()
        updateRecycledView()
    }


    private fun readTODOS(local: Boolean) {
        if (!local) {
            val todosListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    val t: GenericTypeIndicator<MutableMap<String, TODOData>> =
                        object : GenericTypeIndicator<MutableMap<String, TODOData>>() {}
                    val data = dataSnapshot.getValue(t);

                    if (data != null) {
                        this@MainActivity.todos = data
                    } else {
                        this@MainActivity.todos = mutableMapOf()
                    }
                    updateRecycledView()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("TAG", "load:onCancelled", databaseError.toException())
                }
            }
            databaseRef.addValueEventListener(todosListener)
        } else {
//            https://stackoverflow.com/questions/7145606/how-do-you-save-store-objects-in-sharedpreferences-on-android
            val gson = Gson()
            val preferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
            val localTodos = preferences.getStringSet("localTodos", setOf())

            val newTodos = mutableMapOf<String, TODOData>()
            localTodos?.forEach {
                val todo = preferences.getString(it, "");
                val obj: TODOData = gson.fromJson(todo, TODOData::class.java)
                newTodos[it] = obj
            }
            todos = newTodos

            updateRecycledView()
        }
    }

    fun addTodo(v: View) {
        val intent = Intent(this@MainActivity, AddOrEdit::class.java)
        startActivity(intent)
        finish()
    }

    fun toggleSwitch(v: View) {
        fromOldest = binding.filterSwitch.isChecked

        updateRecycledView()
    }

    fun onEdit(key: String) {
        val intent = Intent(this@MainActivity, AddOrEdit::class.java)
        intent.putExtra("todoKey", key)
        startActivity(intent)
        finish()
    }

    fun onDelete(key: String) {
        val preferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val local = preferences.getBoolean("local", false)

        if (!local) {
            databaseRef.child(key).removeValue().addOnSuccessListener {
                Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()
            }
        } else {
            val editor = preferences.edit()
            val localTodos = preferences.getStringSet("localTodos", setOf())
            val newSet = localTodos?.toMutableSet()
            newSet?.remove(key)
            editor.putStringSet("localTodos", newSet)
            editor.remove(key)
            editor.apply()

            readTODOS(local)
            Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()
        }
    }

    fun onUserClick(v: View) {
        val intent = Intent(this@MainActivity, UserPage::class.java)
        startActivity(intent)
        finish()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = MainBinding.inflate(layoutInflater)

        val preferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val extraUsername = preferences.getString("username", "")
        val extraFirebase = preferences.getString("firebase", "")
        val local = preferences.getBoolean("local", false)

        if (!local) {
            if (extraFirebase != null && extraUsername != null) {
                try {
                    databaseRef =
                        FirebaseDatabase.getInstance(extraFirebase).getReference(extraUsername)
                    Toast.makeText(this, extraUsername, Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    val intent = Intent(this@MainActivity, UserPage::class.java)
                    startActivity(intent)
                    finish()
                }

            }
        } else {
            Toast.makeText(this, "Local", Toast.LENGTH_SHORT).show()
        }

        readTODOS(local)

        setContentView(binding.root)
    }
}