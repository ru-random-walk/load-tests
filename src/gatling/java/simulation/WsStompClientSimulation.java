package simulation;

import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import websocket.WebsocketApi;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;

public class WsStompClientSimulation extends Simulation {
    private final WebsocketApi api = new WebsocketApi();

    HttpProtocolBuilder httpProtocol = http
            .baseUrl("https://random-walk.ru:44424/chat")
            .acceptHeader("application/json");

    FeederBuilder<Object> feeder = jsonFile("chatUsers.json").circular();

    ScenarioBuilder scn = scenario("WebSocket Chat Scenario")
            .feed(feeder)
            .exec(
                http("Получаем количество сообщений до отправки")
                        .get("/message/list")
                        .queryParam("chatId", "#{chatId}")
                        .queryParam("size", 100)
                        .header("Authorization", "Bearer #{token}")
                        .check(jsonPath("$.page.totalElements").ofInt().saveAs("initialMessageCount")))
            .pause(2)
            .repeat(3).on(exec(session -> {
                var chatId = UUID.fromString(session.getString("chatId"));
                var token = session.getString("token");
                var sender = UUID.fromString(session.getString("sender"));
                var recipient = UUID.fromString(session.getString("recipient"));
                var wsSession = api.connect(chatId, token);
                api.sendMessage("Hi from gatling!", wsSession, token, chatId, sender, recipient, LocalDateTime.now());
                return session;
            }))
            .pause(2)
            .exec(
                http("Проверяем количество после отправки")
                        .get("/message/list")
                        .queryParam("chatId", "#{chatId}")
                        .queryParam("size", 100)
                        .header("Authorization", "Bearer #{token}")
                        .check(jsonPath("$.page.totalElements").ofInt()
                                .is(session -> session.getInt("initialMessageCount") + 3)));


    {
        setUp(
                scn.injectOpen(
                        rampUsersPerSec(1).to(100).during(Duration.ofMinutes(1))
                )
        ).protocols(httpProtocol)
                .maxDuration(Duration.ofMinutes(5))
                .assertions(
                        global().successfulRequests().percent().gt(99.0),
                        global().responseTime().mean().lt(3000),
                        forAll().failedRequests().count().lt(10L)
                );
    }
}
