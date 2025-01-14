package com.mufg.pex.messaging.handler;

import com.mufg.pex.messaging.domain.Response;
import com.mufg.pex.messaging.entity.BrokerMessage;
import com.mufg.pex.messaging.util.Cache;
import com.mufg.pex.messaging.util.Database;
import com.sun.net.httpserver.HttpExchange;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class MessageHandler extends BaseHandler {

    @Override
    public Response post(HttpExchange exchange, Map<String, String> params) {

        String queue = params.get("queue");
        String clientId = params.get("clientId");

        String body;
        try {
            body = new String(exchange.getRequestBody().readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Map<String, String> headers = new HashMap<>();

        for (Map.Entry<String, List<String>> entry : exchange.getRequestHeaders().entrySet()) {
            String s = entry.getKey().toUpperCase();
            List<String> strings = entry.getValue();
            if (s.startsWith("PARAMETER_")) {

                headers.put(s.substring(10), strings.get(0));
            }
        }
        headers.put("redeliveryCount", "0");

        Cache.getInstance().createQueue(clientId, queue);
        String messageId = Database.getInstance().createBrokerMessage(queue, body, headers);

        return Response
                .builder()
                .code(200)
                .body("Message sent")
                .addParam("messageId", messageId)
                .build();
    }

    @Override
    public Response get(HttpExchange exchange, Map<String, String> params) {

        String queue = params.get("queue");
        String clientId = params.get("clientId");

        BrokerMessage message = Database.getInstance().getFirstMessageForQueue(queue, "CONSUMER", clientId);
        Cache.getInstance().getConsumers().put(clientId, queue);

        if (message == null) {

            if (!Cache.getInstance().getTransfers().getKeys().contains(queue)) {
                log.info("No messages for queue {} in this broker. Message request from other brokers is queued.", queue);
                Cache.getInstance().getTransfers().put(queue, clientId);
            }

            return Response
                    .builder()
                    .code(204)
                    .build();
        } else {

            return Response
                    .builder()
                    .code(200)
                    .noConvert()
                    .body(message.getPayload())
                    .addAllHeaders(message.getHeadersMap())
                    .addHeader("messageId", message.getId())
                    .build();
        }
    }

    @Override
    public Response delete(HttpExchange exchange, Map<String, String> params) {

        String messageId = params.get("messageId");
        String clientId = params.get("clientId");
        String queue = params.get("queue");

        Cache.getInstance().getConsumers().put(clientId, queue);

        boolean result = Database.getInstance().updateMessageStatus(messageId, "CONSUMER", clientId, "ACK");

        return Response
                .builder()
                .body(result ? "Acknowledged" : "Message not found")
                .code(result ? 200 : 400)
                .build();
    }
}