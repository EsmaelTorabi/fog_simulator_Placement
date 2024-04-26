package ir.fog.web
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.io.IOException
/**
 * @author mohsen on 12/20/21
 */


@Component
class SocketHandler : TextWebSocketHandler() {
    @Throws(InterruptedException::class, IOException::class)
    public override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val message = message.payload + " World"
        session.sendMessage(TextMessage(message))
    }

    @Throws(Exception::class)
    override fun afterConnectionEstablished(session: WebSocketSession) { //the messages will be broadcasted to all users.
        println("received connection")
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        super.afterConnectionClosed(session, status)
        println("Connection closed by client")
    }
}