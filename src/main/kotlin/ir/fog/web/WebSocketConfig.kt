package ir.fog.web
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

/**
 * @author mohsen on 12/20/21
 */


//@Configuration
//@EnableWebSocket
//open class WebSocketConfig : WebSocketConfigurer {
//    @Autowired
//    lateinit var socketHandler: SocketHandler
//
//    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
//        registry.addHandler(socketHandler, "/app").setAllowedOrigins("*")
//    }
//}


@Configuration
@EnableWebSocketMessageBroker
open class WebSocketConfig : WebSocketMessageBrokerConfigurer {
    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        config.enableSimpleBroker("/fog")
        config.setApplicationDestinationPrefixes("/app")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/gs-guide-websocket")
            .setAllowedOriginPatterns("*")
            .setAllowedOrigins("localhost")
            .withSockJS()

    }
}
