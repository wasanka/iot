package com.mufg.pex.messaging.handler;

import com.mufg.pex.messaging.Server;
import com.mufg.pex.messaging.domain.Response;
import com.mufg.pex.messaging.entity.BrokerMessage;
import com.mufg.pex.messaging.util.Database;
import com.mufg.pex.messaging.util.Environment;
import com.mufg.pex.messaging.util.HTML;
import com.mufg.pex.messaging.util.URLContentFetcher;
import com.sun.net.httpserver.HttpExchange;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class IndexHandler extends BaseHandler {

    @Override
    public Response get(HttpExchange exchange, Map<String, String> params) {

        String type = params.get("type");
        String html = null;

        if (type == null) {
            try {
                html = new String(getClass().getResourceAsStream("/index.html").readAllBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            switch (type) {
                case "MEMBER_DASHBOARD": {
                    String memberId = params.get("memberId");

                    if (!Server.ID.equals(memberId)) {
                        String newLocation = "http://" + memberId + ":" + Environment.get("BROKER_PORT", "8000") + exchange.getRequestURI();

                        return URLContentFetcher.fetchContentAndHeaders(newLocation);

                        // Set the response headers for redirection
//                        exchange.getResponseHeaders().set("Location", newLocation);
//                        try {
//                            exchange.sendResponseHeaders(302, -1); // 302 Found for redirection
//                            return null;
//                        } catch (IOException e) {
//                            throw new RuntimeException(e);
//                        }
                        // Close the exchange
//                        exchange.close();
                    }


                    int page = Integer.parseInt(params.get("page"));
                    int pageSize = Integer.parseInt(params.get("pageSize"));

                    HTML root = HTML
                            .builder("table")
                            .attribute("width", "100%");


                    HTML tbody = root
                            .element("tr")
                            .element("th", "Message Id").up()
                            .element("th", "Status").up()
                            .element("th", "Content").up()
                            .element("th", "").up()
                            .element("tbody");

                    List<BrokerMessage> messages = Database.getInstance().browseMessages(page, pageSize);
                    messages.forEach(bm -> {

                        String messageSnippet = bm.getPayload().replaceAll("\n", "");
                        messageSnippet = messageSnippet.length() > 100 ? messageSnippet.substring(0, 100) : messageSnippet;
                        messageSnippet = escapeHtml(messageSnippet);

                        tbody.element("tr")
                                .element("td", bm.getId()).up()
                                .element("td", bm.getStatus()).up()
                                .element("td", messageSnippet).up()
                                .element("td")
                                .element("button", "...")
                                .attribute("hx-get", "/index?type=GET_MESSAGE&messageId=" + bm.getId() + "&queue=" + bm.getQueue())
                                .attribute("hx-target", "body")
                                .attribute("hx-swap", "beforeend");
                    });

                    html = root.build();
                    break;
                }
                case "GET_MESSAGE": {

                    String messageId = params.get("messageId");
                    String queue = params.get("queue");

                    BrokerMessage message = Database.getInstance().findMessage(messageId, queue);
                    String payload = escapeHtml(message.getPayload());
                    payload = payload.replaceAll("\n", "<br>");
                    payload = payload.replaceAll(" ", "&nbsp;");

                    html = """
                            <div id="modal" _="on closeModal add .closing then wait for animationend then remove me">
                            	<div class="modal-underlay" _="on click trigger closeModal"></div>
                            	<div class="modal-content">
                            		<h1>Modal Dialog</h1>
                            """
                            + payload +
                            """
                                    		<br>
                                    		<br>
                                    		<button class="btn danger" _="on click trigger closeModal">Close</button>
                                    	</div>
                                    </div>
                                    """;
                    break;
                }
            }
        }

        return Response
                .builder()
                .code(200)
                .noConvert()
                .body(html)
                .build();
    }

    public static String escapeHtml(String input) {
        if (input == null) {
            return null;
        }
        StringBuilder escaped = new StringBuilder();
        for (char c : input.toCharArray()) {
            switch (c) {
                case '<':
                    escaped.append("&lt;");
                    break;
                case '>':
                    escaped.append("&gt;");
                    break;
                case '&':
                    escaped.append("&amp;");
                    break;
                case '"':
                    escaped.append("&quot;");
                    break;
                case '\'':
                    escaped.append("&#39;");
                    break;
                default:
                    escaped.append(c);
                    break;
            }
        }
        return escaped.toString();
    }
}
