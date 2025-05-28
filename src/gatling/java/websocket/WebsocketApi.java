package websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import model.MessageRequestDto;
import model.Payload;
import org.apache.tomcat.websocket.Constants;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.util.MimeType;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;


public class WebsocketApi {
    private final Logger log = Logger.getLogger("WebsocketApi");

    public void sendMessage(String text,
                            StompSession session,
                            String token,
                            UUID chatId,
                            UUID sender,
                            UUID recipient,
                            LocalDateTime localDateTime) {
        StompHeaders sendHeaders = getStompHeaders(token);
        Payload payload = new Payload().setType("text").setText(text);
        MessageRequestDto message = new MessageRequestDto().setPayload(payload).setChatId(chatId).setSender(sender).setRecipient(recipient).setCreatedAt(localDateTime.toString());
        log.info("Отправляем " + message);
        session.send(sendHeaders, message);
        log.info("Сообщение успешно отправлено");
    }

    public StompSession connect(UUID chatId, final String token) {
        WebSocketStompClient stompClient;
        StompSession session;
        try {
            stompClient = getWebSocketStompClient();
            session = connect(stompClient, token);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        log.info("session = %s, sclient = %s".formatted(session, stompClient));
        session.subscribe("/topic/chat/" + chatId, new StompSessionHandlerAdapter() {
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                log.info("Received message: %s".formatted(payload.toString()));
            }
        });
        return session;
    }

    private StompHeaders getStompHeaders(String token) {
        StompHeaders sendHeaders = new StompHeaders();
        sendHeaders.setDestination("/app/sendMessage");
        sendHeaders.setContentType(MimeType.valueOf("application/json"));
        sendHeaders.add("Authorization", "Bearer " + token);
        return sendHeaders;
    }

    private StompSession connect(WebSocketStompClient stompClient, final String token)
            throws InterruptedException,
            ExecutionException {
        WebSocketHttpHeaders httpHeaders = new WebSocketHttpHeaders();
        httpHeaders.add("Authorization", "Bearer " + token); // Для SockJS HTTP-запросов

        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "Bearer " + token); // Для STOMP
        log.info("Connecting to %s with token: Bearer %s".formatted( "wss://random-walk.ru:44424/chat/ws", token));

        var future = stompClient
                .connect("wss://random-walk.ru:44424/chat/ws", httpHeaders, connectHeaders, new StompSessionHandlerAdapter() {});

        try {
            return future.get();
        } catch (ExecutionException e) {
            log.info("Failed to connect: %s".formatted(e.getCause().getMessage()));
            throw e;
        }
    }

    private WebSocketStompClient getWebSocketStompClient() throws Exception {
        StandardWebSocketClient webSocketClient = getWebSocketClientWithDisabledCertificateVerification();
        WebSocketStompClient stompClient = new WebSocketStompClient(
                new SockJsClient(Collections.singletonList(new WebSocketTransport(webSocketClient))));
        MappingJackson2MessageConverter messageConverter = getMappingJackson2MessageConverter();
        stompClient.setMessageConverter(messageConverter);
        return stompClient;
    }

    private MappingJackson2MessageConverter getMappingJackson2MessageConverter() {
        MappingJackson2MessageConverter messageConverter = new MappingJackson2MessageConverter();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Регистрируем модуль для Java 8 Date/Time
        messageConverter.setObjectMapper(objectMapper);
        return messageConverter;
    }

    private StandardWebSocketClient getWebSocketClientWithDisabledCertificateVerification() throws Exception {
        SSLContext sslContext = SslUtil.disableCertificateVerification();
        StandardWebSocketClient webSocketClient = new StandardWebSocketClient();
        webSocketClient.setUserProperties(Map.of(Constants.SSL_CONTEXT_PROPERTY, sslContext));
        return webSocketClient;
    }
}
