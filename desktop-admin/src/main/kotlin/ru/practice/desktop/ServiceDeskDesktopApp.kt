package ru.practice.desktop

import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JPasswordField
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.JTextField
import javax.swing.SwingUtilities
import javax.swing.table.DefaultTableModel

data class TicketRow(
    val id: Long,
    val title: String,
    val priority: String,
    val status: String,
    val createdByName: String
)

class ServiceDeskApi(
    private val baseUrl: String = System.getenv("API_URL") ?: "http://localhost:8080/api"
) {
    private val client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build()
    private var token: String? = null

    fun login(username: String, password: String): String {
        val body = """{"username":"${escape(username)}","password":"${escape(password)}"}"""
        val response = send("/auth/login", "POST", body)
        token = Regex("\"token\"\\s*:\\s*\"([^\"]+)\"")
            .find(response)
            ?.groupValues
            ?.get(1)
            ?: error("Backend не вернул JWT-токен")
        return Regex("\"fullName\"\\s*:\\s*\"([^\"]+)\"")
            .find(response)
            ?.groupValues
            ?.get(1)
            ?: username
    }

    fun tickets(): List<TicketRow> {
        val response = send("/tickets", "GET", null)
        return Regex("\\{[^{}]*}")
            .findAll(response)
            .map { match ->
                val json = match.value
                TicketRow(
                    id = field(json, "id").toLong(),
                    title = field(json, "title"),
                    priority = field(json, "priority"),
                    status = field(json, "status"),
                    createdByName = field(json, "createdByName")
                )
            }
            .toList()
    }

    fun changeStatus(ticketId: Long, status: String) {
        val body = """{"status":"$status","comment":"Статус изменен через desktop-админку"}"""
        send("/tickets/$ticketId/status", "PATCH", body)
    }

    private fun send(path: String, method: String, body: String?): String {
        val builder = HttpRequest.newBuilder()
            .uri(URI.create("$baseUrl$path"))
            .timeout(Duration.ofSeconds(12))
            .header("Content-Type", "application/json")

        token?.let { builder.header("Authorization", "Bearer $it") }

        val request = if (body == null) {
            builder.method(method, HttpRequest.BodyPublishers.noBody()).build()
        } else {
            builder.method(method, HttpRequest.BodyPublishers.ofString(body)).build()
        }

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() >= 400) {
            error("Ошибка API ${response.statusCode()}: ${response.body()}")
        }
        return response.body()
    }

    private fun field(json: String, name: String): String {
        val match = Regex("\"$name\"\\s*:\\s*(?:\"([^\"]*)\"|(\\d+)|null)").find(json)
        if (match == null) {
            return ""
        }
        return match.groupValues[1].ifBlank { match.groupValues.getOrElse(2) { "" } }
    }

    private fun escape(value: String): String {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
    }
}

class ServiceDeskDesktopApp : JFrame("Corporate ServiceDesk Desktop Admin") {
    private val api = ServiceDeskApi()
    private val tableModel = object : DefaultTableModel(arrayOf("ID", "Тема", "Приоритет", "Статус", "Автор"), 0) {
        override fun isCellEditable(row: Int, column: Int): Boolean = false
    }
    private val table = JTable(tableModel)
    private val statuses = arrayOf("NEW", "IN_PROGRESS", "WAITING_PARTS", "RESOLVED", "CLOSED")

    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        minimumSize = Dimension(980, 620)
        contentPane = loginPanel()
        pack()
        setLocationRelativeTo(null)
    }

    private fun loginPanel(): JPanel {
        val panel = JPanel()
        panel.layout = BorderLayout(16, 16)
        panel.border = BorderFactory.createEmptyBorder(48, 48, 48, 48)

        val title = JLabel("Corporate ServiceDesk")
        title.font = title.font.deriveFont(Font.BOLD, 32f)
        panel.add(title, BorderLayout.NORTH)

        val form = JPanel(FlowLayout(FlowLayout.LEFT, 12, 12))
        val username = JTextField("admin", 16)
        val password = JPasswordField("admin123", 16)
        val loginButton = JButton("Войти")

        form.add(JLabel("Логин"))
        form.add(username)
        form.add(JLabel("Пароль"))
        form.add(password)
        form.add(loginButton)
        panel.add(form, BorderLayout.CENTER)

        loginButton.addActionListener {
            loginButton.isEnabled = false
            Thread {
                try {
                    val fullName = api.login(username.text, String(password.password))
                    SwingUtilities.invokeLater {
                        contentPane = workspacePanel(fullName)
                        revalidate()
                        repaint()
                        refreshTickets()
                    }
                } catch (ex: Exception) {
                    SwingUtilities.invokeLater {
                        loginButton.isEnabled = true
                        showError(ex.message ?: "Не удалось войти")
                    }
                }
            }.start()
        }

        return panel
    }

    private fun workspacePanel(fullName: String): JPanel {
        val panel = JPanel(BorderLayout(12, 12))
        panel.border = BorderFactory.createEmptyBorder(18, 18, 18, 18)

        val header = JPanel(BorderLayout())
        val title = JLabel("Заявки ServiceDesk")
        title.font = title.font.deriveFont(Font.BOLD, 26f)
        header.add(title, BorderLayout.WEST)
        header.add(JLabel("Пользователь: $fullName"), BorderLayout.EAST)
        panel.add(header, BorderLayout.NORTH)

        table.rowHeight = 30
        panel.add(JScrollPane(table), BorderLayout.CENTER)

        val actions = JPanel(FlowLayout(FlowLayout.LEFT, 10, 10))
        val statusBox = JComboBox(statuses)
        val refreshButton = JButton("Обновить")
        val statusButton = JButton("Изменить статус")
        actions.add(JLabel("Новый статус"))
        actions.add(statusBox)
        actions.add(statusButton)
        actions.add(refreshButton)
        panel.add(actions, BorderLayout.SOUTH)

        refreshButton.addActionListener { refreshTickets() }
        statusButton.addActionListener {
            val selectedRow = table.selectedRow
            if (selectedRow < 0) {
                showError("Выберите заявку в таблице")
                return@addActionListener
            }
            val ticketId = tableModel.getValueAt(selectedRow, 0).toString().toLong()
            val status = statusBox.selectedItem.toString()
            Thread {
                try {
                    api.changeStatus(ticketId, status)
                    SwingUtilities.invokeLater { refreshTickets() }
                } catch (ex: Exception) {
                    SwingUtilities.invokeLater { showError(ex.message ?: "Не удалось изменить статус") }
                }
            }.start()
        }

        return panel
    }

    private fun refreshTickets() {
        Thread {
            try {
                val tickets = api.tickets()
                SwingUtilities.invokeLater {
                    tableModel.setRowCount(0)
                    tickets.forEach { ticket ->
                        tableModel.addRow(arrayOf(ticket.id, ticket.title, ticket.priority, ticket.status, ticket.createdByName))
                    }
                }
            } catch (ex: Exception) {
                SwingUtilities.invokeLater { showError(ex.message ?: "Не удалось загрузить заявки") }
            }
        }.start()
    }

    private fun showError(message: String) {
        JOptionPane.showMessageDialog(this, message, "ServiceDesk", JOptionPane.ERROR_MESSAGE)
    }
}

fun main() {
    SwingUtilities.invokeLater {
        ServiceDeskDesktopApp().isVisible = true
    }
}
