package ru.practice.servicedesk.mobile

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : Activity() {
    private val apiBase = "http://10.0.2.2:8080/api"
    private var token: String? = null
    private lateinit var root: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showLogin()
    }

    private fun showLogin() {
        root = verticalRoot()
        val title = title("ServiceDesk Mobile")
        val subtitle = text("Мобильный клиент техника. Для эмулятора Android используется адрес backend: $apiBase")
        val username = input("Логин", "tech")
        val password = input("Пароль", "tech123")
        val login = button("Войти") {
            background {
                val payload = JSONObject()
                    .put("username", username.text.toString())
                    .put("password", password.text.toString())
                    .toString()
                val response = api("/auth/login", "POST", payload)
                token = JSONObject(response).getString("token")
                runOnUiThread { showTickets() }
            }
        }

        root.addView(title)
        root.addView(subtitle)
        root.addView(username)
        root.addView(password)
        root.addView(login)
        setContentView(root)
    }

    private fun showTickets() {
        root = verticalRoot()
        root.addView(title("Заявки"))
        root.addView(text("Быстрый просмотр и смена статуса заявок."))
        root.addView(button("Обновить") { loadTickets() })
        val scroll = ScrollView(this)
        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 18, 0, 0)
        }
        scroll.addView(list)
        root.addView(scroll, LinearLayout.LayoutParams(match(), 0, 1f))
        setContentView(root)
        loadTickets(list)
    }

    private fun loadTickets(container: LinearLayout? = null) {
        val target = container ?: ((root.getChildAt(root.childCount - 1) as ScrollView).getChildAt(0) as LinearLayout)
        background {
            val tickets = JSONArray(api("/tickets", "GET", null))
            runOnUiThread {
                target.removeAllViews()
                for (index in 0 until tickets.length()) {
                    target.addView(ticketCard(tickets.getJSONObject(index)))
                }
            }
        }
    }

    private fun ticketCard(ticket: JSONObject): LinearLayout {
        val id = ticket.getLong("id")
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            setBackgroundColor(Color.rgb(248, 243, 231))
            val params = LinearLayout.LayoutParams(match(), wrap())
            params.setMargins(0, 0, 0, 18)
            layoutParams = params

            addView(text("#$id · ${ticket.getString("title")}"))
            addView(text("Приоритет: ${ticket.getString("priority")}"))
            addView(text("Статус: ${ticket.getString("status")}"))
            addView(text("Автор: ${ticket.optString("createdByName", "не указан")}"))

            val actions = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                addView(button("В работу") { changeStatus(id, "IN_PROGRESS") })
                addView(button("Решено") { changeStatus(id, "RESOLVED") })
            }
            addView(actions)
        }
    }

    private fun changeStatus(ticketId: Long, status: String) {
        background {
            val payload = JSONObject()
                .put("status", status)
                .put("comment", "Статус изменен из Android-приложения")
                .toString()
            api("/tickets/$ticketId/status", "PATCH", payload)
            runOnUiThread {
                toast("Статус обновлен")
                loadTickets()
            }
        }
    }

    private fun api(path: String, method: String, body: String?): String {
        val connection = (URL("$apiBase$path").openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 7000
            readTimeout = 12000
            setRequestProperty("Content-Type", "application/json")
            token?.let { setRequestProperty("Authorization", "Bearer $it") }
        }

        if (body != null) {
            connection.doOutput = true
            connection.outputStream.bufferedWriter().use { writer -> writer.write(body) }
        }

        val responseCode = connection.responseCode
        val stream = if (responseCode >= 400) connection.errorStream else connection.inputStream
        val response = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
        if (responseCode >= 400) {
            error(response.ifBlank { "HTTP $responseCode" })
        }
        return response
    }

    private fun verticalRoot(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 48, 32, 32)
            setBackgroundColor(Color.rgb(247, 241, 228))
        }
    }

    private fun title(value: String): TextView {
        return TextView(this).apply {
            text = value
            textSize = 28f
            setTextColor(Color.rgb(23, 33, 27))
            setPadding(0, 0, 0, 18)
        }
    }

    private fun text(value: String): TextView {
        return TextView(this).apply {
            text = value
            textSize = 16f
            setTextColor(Color.rgb(49, 82, 66))
            setPadding(0, 0, 0, 12)
        }
    }

    private fun input(hintValue: String, defaultValue: String): EditText {
        return EditText(this).apply {
            hint = hintValue
            setText(defaultValue)
            setSingleLine(true)
        }
    }

    private fun button(label: String, onClick: () -> Unit): Button {
        return Button(this).apply {
            text = label
            setOnClickListener { onClick() }
        }
    }

    private fun background(action: () -> Unit) {
        Thread {
            try {
                action()
            } catch (ex: Exception) {
                runOnUiThread { toast(ex.message ?: "Ошибка") }
            }
        }.start()
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun match() = ViewGroup.LayoutParams.MATCH_PARENT
    private fun wrap() = ViewGroup.LayoutParams.WRAP_CONTENT
}

