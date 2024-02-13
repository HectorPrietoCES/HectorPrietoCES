import java.net.ServerSocket
import java.net.Socket
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

class ServerThread(private val clientSocket: Socket, private val userDatabase: MutableMap<String, String>) : Thread() {

    override fun run() {
        try {
            val reader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
            val writer = PrintWriter(clientSocket.getOutputStream(), true)

            // autentificacion
            if (authenticateOrRegister(reader, writer)) {
                // si funciona
                handleClientMessages(reader, writer)
            } else {
                // si falla
                println("Autenticación/Registro fallido. Cerrando conexión con ${clientSocket.inetAddress.hostAddress}")
                clientSocket.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun authenticateOrRegister(reader: BufferedReader, writer: PrintWriter): Boolean {
        writer.println("Seleccione una opción:")
        writer.println("1. Iniciar sesión")
        writer.println("2. Registrarse")

        val choice = reader.readLine()

        when (choice) {
            "1" -> return login(reader, writer)
            "2" -> return register(reader, writer)
        }

        return false
    }

    private fun login(reader: BufferedReader, writer: PrintWriter): Boolean {
        writer.println("Ingrese el nombre de usuario:")
        val clientUsername = reader.readLine()

        writer.println("Ingrese la contraseña:")
        val clientPassword = reader.readLine()

        // "verifica"
        if (userDatabase.containsKey(clientUsername) && userDatabase[clientUsername] == hashPassword(clientPassword)) {
            writer.println("Inicio de sesión exitoso. Puede enviar y recibir mensajes.")
            return true
        } else {
            writer.println("Error: Nombre de usuario o contraseña incorrectos.")
            return false
        }
    }

    private fun register(reader: BufferedReader, writer: PrintWriter): Boolean {
        writer.println("Ingrese un nuevo nombre de usuario:")
        val newUsername = reader.readLine()

        // "verifia"
        if (userDatabase.containsKey(newUsername)) {
            writer.println("Error: El nombre de usuario ya está en uso.")
            return false
        }

        writer.println("Ingrese una nueva contraseña:")
        val newPassword = reader.readLine()

        // guarda el nombre de usuario
        userDatabase[newUsername] = hashPassword(newPassword)

        writer.println("Registro exitoso. Puede iniciar sesión ahora.")
        return true
    }

    private fun handleClientMessages(reader: BufferedReader, writer: PrintWriter) {
        writer.println("Puede enviar y recibir mensajes. Escriba 'exit' para salir.")

        while (true) {
            val message = reader.readLine() ?: break

            if (message.toLowerCase() == "exit") {
                writer.println("Sesión finalizada. Cerrando conexión.")
                break
            }

            println("Mensaje del cliente: $message")
            writer.println("Recibido: $message")
        }

        clientSocket.close()
    }

    private fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("SHA-512")
        val digest = md.digest(password.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}

class Server {
    private val serverSocket: ServerSocket = ServerSocket(12345)
    private val userDatabase: MutableMap<String, String> = ConcurrentHashMap()

    fun start() {
        println("Servidor iniciado. Esperando conexiones...")

        while (true) {
            val clientSocket: Socket = serverSocket.accept()
            println("Cliente conectado: ${clientSocket.inetAddress.hostAddress}")

            // hilo para la conexion
            val serverThread = ServerThread(clientSocket, userDatabase)
            serverThread.start()
        }
    }
}

fun main() {
    val server = Server()
    server.start()
}
