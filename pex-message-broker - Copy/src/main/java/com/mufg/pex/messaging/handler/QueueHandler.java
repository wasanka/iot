package com.mufg.pex.messaging.handler;

import com.mufg.pex.messaging.domain.Response;
import com.mufg.pex.messaging.util.Cache;
import com.sun.net.httpserver.HttpExchange;

import java.util.Map;

public class QueueHandler extends BaseHandler {

    @Override
    public Response put(HttpExchange exchange, Map<String, String> params) {

        String name = params.get("name");
        String clientId = params.get("clientId");

        boolean result = Cache.getInstance().createQueue(clientId, name);

        String response = "Queue " + name + (result ? " created" : " already existing");

        return Response
                .builder()
                .body(response)
                .code(200)
                .build();
    }
}
