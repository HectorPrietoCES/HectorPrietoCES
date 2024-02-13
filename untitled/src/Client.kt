import java.net.Socket
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.security.MessageDigest
import java.util.*

class Client {
    private val socket: Socket = Socket("localhost", 12345)
    private val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
    private val writer = PrintWriter(socket.getOutputStream(), true)
    private val scanner = Scanner(System.`in`)

    fun start() {
        // autentificacion
        authenticateOrRegister()

        // si la autentificacion sale bien te permite enviar mensajes
        handleServerMessages()
    }

    private fun authenticateOrRegister() {
        // opciones para el usuario
        println("Seleccione una opción:")
        println("1. Iniciar sesión")
        println("2. Registrarse")

        val choice = scanner.nextLine()
        writer.println(choice)

        when (choice) {
            "1" -> login()
            "2" -> register()
        }
    }

    private fun login() {
        // solicitar datos al usuario
        print("Ingrese el nombre de usuario: ")
        val clientUsername = scanner.nextLine()
        writer.println(clientUsername)

        print("Ingrese la contraseña: ")
        val clientPassword = scanner.nextLine()
        writer.println(hashPassword(clientPassword))
    }

    private fun register() {
        // info nombre y pass
        print("Ingrese un nuevo nombre de usuario: ")
        val newUsername = scanner.nextLine()
        writer.println(newUsername)

        print("Ingrese una nueva contraseña: ")
        val newPassword = scanner.nextLine()
        writer.println(hashPassword(newPassword))
    }

    private fun handleServerMessages() {
        // envia los datos
        val response = reader.readLine()
        println(response)

        // permite enviar el mensaje o salir
        while (true) {
            print("Ingrese un mensaje (o 'exit' para salir): ")
            val message = readLine() ?: ""
            writer.println(message)

            if (message.toLowerCase() == "exit") {
                break
            }

            val serverResponse = reader.readLine()
            println("Respuesta del servidor: $serverResponse")

            // ma sopciones al usuario
            println("Seleccione una opción:")
            println("1. Enviar otro mensaje")
            println("2. Ver contraseña (hasheada)")
            println("3. Salir")

            val option = scanner.nextLine()

            when (option) {
                "1" -> continue
                "2" -> println("Contraseña hasheada: ${hashPassword(message)}")
                "3" -> {
                    writer.println("exit") // salinedo del sv
                    break
                }
                else -> println("Opción no válida.")
            }
        }

        socket.close()
    }

    private fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("SHA-512")
        val digest = md.digest(password.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}

fun main() {
    val client = Client()
    client.start()
}
