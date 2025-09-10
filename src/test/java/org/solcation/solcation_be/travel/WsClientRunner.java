package org.solcation.solcation_be.travel;

import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class WsClientRunner {

    public static void main(String[] args) throws Exception {
        String host = System.getProperty("host", "localhost");
        String port = System.getProperty("port", "8080");
        long groupId = Long.parseLong(System.getProperty("groupId", "1"));
        long travelId = Long.parseLong(System.getProperty("travelId", "1"));
        int day = Integer.parseInt(System.getProperty("day", "1"));

        String jwt = System.getProperty("token",
                "YOUR_TOKEN");
        if (jwt == null || jwt.isBlank() || jwt.startsWith("PASTE_")) {
            throw new IllegalStateException("JWT 토큰이 없습니다. -Dtoken=... 으로 넘겨주세요.");
        }

        String wsUrl    = "ws://" + host + ":" + port + "/ws";
        String topic    = "/topic/travel/" + travelId;
        String sendDest = "/app/group/" + groupId + "/travel/" + travelId + "/edit/op";
        WebSocketStompClient stomp = new WebSocketStompClient(new StandardWebSocketClient());
        stomp.setMessageConverter(new MappingJackson2MessageConverter());
        stomp.setTaskScheduler(new ConcurrentTaskScheduler());
        stomp.setDefaultHeartbeat(new long[]{10000, 10000});

        BlockingQueue<Object> inbox = new ArrayBlockingQueue<>(10);

        StompSessionHandler handler = new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                System.out.println("[CLIENT] Connected. headers=" + connectedHeaders);

                // 구독
                StompHeaders subHeaders = new StompHeaders();
                subHeaders.setDestination(topic);
                session.subscribe(subHeaders, new StompFrameHandler() {
                    @Override public Type getPayloadType(StompHeaders headers) { return Map.class; }
                    @Override public void handleFrame(StompHeaders headers, Object payload) {
                        System.out.println("<< BROADCAST: " + payload);
                        inbox.offer(payload);
                    }
                });

                Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                    var op = Map.of(
                            "type", "insert",
                            "opId", UUID.randomUUID().toString(),
                            "clientId", "ws-test",
                            "opTs", System.currentTimeMillis(),
                            "day", day,
                            "payload", Map.of(
                                    "pdDay", day,
                                    "prevCrdtId", null,
                                    "nextCrdtId", null,
                                    "pdPlace", "제주시(WS)",
                                    "pdAddress", "제주공항",
                                    "pdCost", 0
                            )
                    );
                    StompHeaders sendHeaders = new StompHeaders();
                    sendHeaders.setDestination(sendDest);
                    sendHeaders.setContentType(MimeTypeUtils.APPLICATION_JSON);
                    session.send(sendHeaders, op);
                    System.out.println(">> SENT insert op");
                }, 100, TimeUnit.MILLISECONDS);
            }

            @Override
            public void handleException(StompSession session, StompCommand command,
                                        StompHeaders headers, byte[] payload, Throwable exception) {
                System.err.println("[CLIENT] handleException: cmd=" + command + ", headers=" + headers);
                exception.printStackTrace();
            }

            @Override
            public void handleTransportError(StompSession session, Throwable exception) {
                System.err.println("[CLIENT] Transport error");
                exception.printStackTrace();
            }
        };

        WebSocketHttpHeaders httpHeaders = new WebSocketHttpHeaders();
        httpHeaders.add("Authorization", "Bearer " + jwt);

        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "Bearer " + jwt);

        CompletableFuture<StompSession> cf = stomp.connectAsync(wsUrl, httpHeaders, connectHeaders, handler);
        StompSession session = cf.get(100, TimeUnit.SECONDS);

        // 10초까지 대기
        Object msg = inbox.poll(100, TimeUnit.SECONDS);
        if (msg == null) throw new RuntimeException("no broadcast received");
        System.out.println("OK: got broadcast.");

        session.disconnect();
        stomp.stop();
    }
}
