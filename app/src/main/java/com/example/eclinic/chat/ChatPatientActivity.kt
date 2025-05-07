package com.example.eclinic.chat

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.button.MaterialButton
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import com.example.eclinic.R

class ChatPatientActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var inputEditText: TextInputEditText
    private lateinit var sendButton: MaterialButton

    private val client = OkHttpClient()
    private val apiKey = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat_patient)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.recyclerView)
        inputEditText = findViewById(R.id.inputEditText)
        sendButton = findViewById(R.id.sendButton)

        adapter = ChatAdapter(messages)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        sendButton.setOnClickListener {
            val userText = inputEditText.text.toString()
            if (userText.isNotBlank()) {
                addMessage(userText, isUser = true)
                inputEditText.text?.clear()
                sendMessageToOpenAI(userText)
            }
        }
    }

    private fun addMessage(text: String, isUser: Boolean) {
        messages.add(ChatMessage(text, isUser))
        adapter.notifyItemInserted(messages.size - 1)
        recyclerView.scrollToPosition(messages.size - 1)
    }

    private fun sendMessageToOpenAI(userMessage: String) {
        val json = JSONObject().apply {
            put("model", "gpt-4.1")
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", userMessage)
                })
            })
        }

        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(RequestBody.create(MediaType.get("application/json"), json.toString()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    addMessage("Błąd połączenia: ${e.localizedMessage}", isUser = false)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body()?.string()

                if (response.isSuccessful) {
                    try {
                        val jsonResponse = JSONObject(body)
                        val content = jsonResponse.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content")
                        runOnUiThread {
                            addMessage(content.trim(), isUser = false)
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            addMessage("Błąd parsowania odpowiedzi", isUser = false)
                        }
                    }
                } else {
                    try {
                        val errorJson = JSONObject(body)
                        val errorMessage = errorJson.getJSONObject("error").getString("message")
                        runOnUiThread {
                            addMessage("Błąd API OpenAI: $errorMessage", isUser = false)
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            addMessage("Wystąpił błąd API", isUser = false)
                        }
                    }
                }
            }
        })
    }
}
