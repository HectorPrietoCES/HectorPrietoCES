fun main() {
    println("1. Iniciar servidor")
    println("2. Iniciar cliente")
    print("Seleccione la opción (1/2): ")

    val choice = readLine()?.toIntOrNull() ?: 0

    when (choice) {
        1 -> {
            val server = Server()
            server.start()
        }
        2 -> {
            val client = Client()
            client.start()
        }
        else -> {
            println("Opción no válida.")
        }
    }
}
