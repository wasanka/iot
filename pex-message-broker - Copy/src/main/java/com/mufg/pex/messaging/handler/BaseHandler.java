package com.mufg.pex.messaging.handler;

import com.mufg.pex.messaging.domain.Response;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseHandler implements HttpHandler {

    private static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        Response response = null;

        try {

            Map<String, String> params = new HashMap<>();
            if (exchange.getRequestURI().getQuery() != null) {
                params = Arrays.stream(exchange.getRequestURI().getQuery().split("&")).map(pair -> pair.split("=")).filter(keyValue -> keyValue.length == 2).collect(Collectors.toMap(keyValue -> keyValue[0], keyValue -> keyValue[1]));
            }

            switch (exchange.getRequestMethod()) {

                case "GET":
                    response = get(exchange, params);
                    break;
                case "PUT":
                    response = put(exchange, params);
                    break;
                case "POST":
                    response = post(exchange, params);
                    break;
                case "DELETE":
                    response = delete(exchange, params);
                    break;
                case "PATCH":
                    response = patch(exchange, params);
                    break;
                case "OPTIONS":
                    response = options(exchange, params);
                    break;
                case "HEAD":
                    response = head(exchange, params);
                    break;
            }

            response.getHeaders().forEach((s, s2) -> {
                exchange.getResponseHeaders().add(s, s2);
            });
            response.getHeaders().clear();
        }catch (Exception e){

            log.error("Handler threw error", e);
        }

        String res = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);

        if(response.isNoConvert()){

            res = (String) response.getBody();
        }

        int length = response.getCode() == 204 ? -1 : res.length();

        exchange.sendResponseHeaders(response.getCode(), length);

        OutputStream os = exchange.getResponseBody();
        os.write(res.getBytes());
        os.close();
    }

    public Response get(HttpExchange exchange, Map<String, String> params) throws Exception {

        return getDefaultResponse();
    }

    private static Response getDefaultResponse() {
        return Response
                .builder()
                .code(405)
                .body("Method not supported")
                .build();
    }

    public Response put(HttpExchange exchange, Map<String, String> params) throws Exception {

        return getDefaultResponse();
    }

    public Response post(HttpExchange exchange, Map<String, String> params) throws Exception {

        return getDefaultResponse();
    }

    public Response delete(HttpExchange exchange, Map<String, String> params) throws Exception {

        return getDefaultResponse();
    }

    public Response patch(HttpExchange exchange, Map<String, String> params) throws Exception {

        return getDefaultResponse();
    }

    public Response options(HttpExchange exchange, Map<String, String> params) throws Exception {

        return getDefaultResponse();
    }

    public Response head(HttpExchange exchange, Map<String, String> params) throws Exception {

        return getDefaultResponse();
    }
}
