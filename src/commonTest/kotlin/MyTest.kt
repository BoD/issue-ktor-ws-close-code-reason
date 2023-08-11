import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.close
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class MyTest {
  private fun Application.webSocketServer() {
    install(WebSockets)
    routing {
      webSocket("/") {
        close(CloseReason(4242, "Bye"))
      }
    }
  }

  @Test
  fun test() = runTest {
    println("Hello, world!")
    val server = embeddedServer(CIO, 8080) { webSocketServer() }.start(wait = false)

    val client = HttpClient {
      install(io.ktor.client.plugins.websocket.WebSockets)
    }
    client.webSocket("ws://localhost:8080"){
      println("Connected")
      try {
        incoming.receive()
      } catch (e: Exception) {
        println("WebSocket was closed code=${closeReason.await()?.code} reason=${closeReason.await()?.message}")
        server.stop(0, 0)
      }
    }
  }
}

