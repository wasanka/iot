package com.mufg.pex.messaging.handler;

import com.mufg.pex.messaging.domain.Response;
import com.sun.net.httpserver.HttpExchange;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class StaticFileHandler extends BaseHandler {

    private static Map<String, String> contentTypes = new HashMap<>();

    static {
        contentTypes.put("js", "text/javascript");
        contentTypes.put("css", "text/css");
        contentTypes.put("html", "text/html");
    }

    @Override
    public Response get(HttpExchange exchange, Map<String, String> params) throws Exception {
        // Get the requested file path
        String filePath = exchange.getRequestURI().getPath();
        String extension = filePath.substring(filePath.lastIndexOf(".") + 1);
        // Check if the file exists and is readable

        URL url = getClass().getResource(filePath);

//        File file = new File(url.getPath());
        if (url == null) {

            Response.builder().code(404)
                    .noConvert().build();

//            exchange.sendResponseHeaders(404, -1); // 404 Not Found
//            return;
        }

        byte[] bytes = getClass().getResourceAsStream(filePath).readAllBytes();

        // Get the file's content type
        // Send the response headers
//        exchange.getResponseHeaders().set("Content-Type", contentTypes.get(extension));
//        exchange.sendResponseHeaders(200, bytes.length);

        return Response.builder().code(200)
                .body(new String(bytes))
                .noConvert()
                .addHeader("Content-Type", contentTypes.get(extension))
                .build();

//        exchange.getResponseBody().write(bytes);
//        exchange.getResponseBody().flush();
//
//        // Send the file content
//        try (OutputStream os = exchange.getResponseBody(); FileInputStream fis = new FileInputStream(file)) {
//            byte[] buffer = new byte[1024];
//            int bytesRead;
//            while ((bytesRead = fis.read(buffer)) != -1) {
//                os.write(buffer, 0, bytesRead);
//            }
//        }
    }
}