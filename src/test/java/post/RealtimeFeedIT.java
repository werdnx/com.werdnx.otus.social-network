package post;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.WebSocket;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Интеграционный тест «подписчик получает событие о новом посте в реальном времени».
 *
 * Предполагается, что стек (DB, RabbitMQ, app) уже поднят локально.
 *
 * Системные свойства / переменные окружения:
 *   - baseUrl        (default: http://localhost:8080)
 *   - wsUrl          (default: ws://localhost:8080)
 *   - authorId       (Long)   — ID автора A
 *   - subscriberId   (Long)   — ID подписчика B
 *   - password       (String) — пароль обоих пользователей, default: secret
 */
public class RealtimeFeedIT {

    private static final Duration WS_CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration EVENT_TIMEOUT = Duration.ofSeconds(10);

    private static String BASE_URL;
    private static String WS_URL;
    private static long AUTHOR_ID;
    private static long SUBSCRIBER_ID;
    private static String PASSWORD;

    private final ObjectMapper om = new ObjectMapper();
    private HttpClient http;

    @BeforeAll
    static void readConfig() {
        BASE_URL = getPropOrEnv("baseUrl", "http://localhost:8080");
        WS_URL   = getPropOrEnv("wsUrl",   "ws://localhost:8080");

        AUTHOR_ID = getPropLongOrEnv("authorId", 3L);
        SUBSCRIBER_ID = getPropLongOrEnv("subscriberId", 2L);
        PASSWORD = getPropOrEnv("password", "secret");
    }

    @BeforeEach
    void setUp() {
        http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Test
    @DisplayName("Подписчик B получает событие PostCreatedEvent при создании поста автором A")
    void subscriberReceivesPostEvent() throws Exception {
        // 0) Получаем токены через /login (POST /login?id=&password=)
        String authorToken = loginAndGetAccessToken(AUTHOR_ID, PASSWORD);
        String subscriberToken = loginAndGetAccessToken(SUBSCRIBER_ID, PASSWORD);

        // 1) Подключаемся к WebSocket от имени подписчика B
        String wsUri = WS_URL + "/post/feed/posted?token=" +
                URLEncoder.encode(subscriberToken, StandardCharsets.UTF_8);
        var received = new ArrayBlockingQueue<String>(8);
        var openLatch = new CountDownLatch(1);

        WebSocket.Listener listener = new WebSocket.Listener() {
            @Override
            public void onOpen(WebSocket webSocket) {
                openLatch.countDown();
                WebSocket.Listener.super.onOpen(webSocket);
            }

            @Override
            public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                received.offer(data.toString());
                return WebSocket.Listener.super.onText(webSocket, data, last);
            }
        };

        WebSocket ws = http.newWebSocketBuilder()
                .connectTimeout(WS_CONNECT_TIMEOUT)
                // если Security требует, можно дополнительно передать Authorization заголовок:
                // .header("Authorization", "Bearer " + subscriberToken)
                .buildAsync(URI.create(wsUri), listener)
                .get(WS_CONNECT_TIMEOUT.toSeconds(), TimeUnit.SECONDS);

        assertTrue(openLatch.await(WS_CONNECT_TIMEOUT.toSeconds(), TimeUnit.SECONDS),
                "WebSocket не успел установиться");

        // 2) (Опционально) убедиться, что B подписан на A
        ensureFriendshipIfPossible(SUBSCRIBER_ID, AUTHOR_ID);

        // 3) Отправляем создание поста от имени A
        String content = "test-" + System.currentTimeMillis();
        createPost(AUTHOR_ID, content, Optional.of(authorToken));

        // 4) Ждём событие в WebSocket и валидируем payload
        String msg = received.poll(EVENT_TIMEOUT.toSeconds(), TimeUnit.SECONDS);
        assertNotNull(msg, "Не пришло событие PostCreatedEvent в течение " + EVENT_TIMEOUT.toSeconds() + " сек.");

        JsonNode event = om.readTree(msg);
        assertEquals(AUTHOR_ID, event.path("authorId").asLong(), "authorId в событии некорректен");
        assertEquals(content, event.path("content").asText(), "content в событии не совпадает");
        assertTrue(event.hasNonNull("postId"), "В событии нет postId");
        assertTrue(event.hasNonNull("createdAt"), "В событии нет createdAt");

        ws.sendClose(WebSocket.NORMAL_CLOSURE, "done").join();
    }

    // --- helpers ---

    /** Логин через POST /login?id=&password= и возврат accessToken из AuthResponse */
    private String loginAndGetAccessToken(long userId, String password) throws Exception {
        String rawUrl = BASE_URL + "/login?id=" + userId + "&password=" +
                URLEncoder.encode(password, StandardCharsets.UTF_8);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(rawUrl))
                .timeout(Duration.ofSeconds(5))
                .POST(HttpRequest.BodyPublishers.noBody())
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            fail("Login failed for userId=" + userId + ", status=" + resp.statusCode() + ", body=" + resp.body());
        }
        JsonNode json = om.readTree(resp.body());
        String token = json.path("accessToken").asText(null);
        assertNotNull(token, "Login response did not contain accessToken");
        return token;
    }

    private void createPost(long authorId, String content, Optional<String> authorTokenOpt) throws Exception {
        String body = "{\"userId\":" + authorId + ",\"content\":\"" + escape(content) + "\"}";

        var reqBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/post/create"))
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body));

        authorTokenOpt.filter(s -> !s.isBlank())
                .ifPresent(tok -> reqBuilder.header("Authorization", "Bearer " + tok));

        HttpResponse<String> resp = http.send(reqBuilder.build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode(), "POST /post/create вернул не 200: " + resp.body());

        JsonNode post = om.readTree(resp.body());
        assertEquals(authorId, post.path("userId").asLong(), "userId в ответе create не совпадает");
        assertEquals(content, post.path("content").asText(), "content в ответе create не совпадает");
        assertTrue(post.hasNonNull("id"), "create не вернул id");
    }

    /**
     * Пытается добавить дружбу B->A, если эндпоинт доступен без строгой авторизации.
     * Если требует токен/уже дружба есть — не падаем.
     */
    private void ensureFriendshipIfPossible(long userId, long friendId) {
        try {
            String url = BASE_URL + "/friend/add?userId=" + userId + "&friendId=" + friendId;
            var req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = http.send(req, HttpResponse.BodyHandlers.ofString());
            System.out.println(response);
        } catch (Exception ignored) {
        }
    }

    private static String getPropOrEnv(String key, String def) {
        String v = System.getProperty(key);
        if (v == null || v.isBlank()) v = System.getenv(key);
        return (v == null || v.isBlank()) ? def : v;
    }
    private static Long getPropLongOrEnv(String key, Long def) {
        String v = System.getProperty(key);
        if (v == null || v.isBlank()) v = System.getenv(key);
        return (v == null || v.isBlank()) ? def : Long.parseLong(v);
    }

    private static String require(String key, String messageIfMissing) {
        String v = System.getProperty(key);
        if (v == null || v.isBlank()) v = System.getenv(key);
        if (v == null || v.isBlank()) throw new IllegalArgumentException(messageIfMissing);
        return v;
    }

    private static String escape(String s) {
        return Objects.requireNonNull(s).replace("\"", "\\\"");
    }
}
