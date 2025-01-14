package com.mufg.pex.messaging.handler;

import com.mufg.pex.messaging.domain.Response;
import com.mufg.pex.messaging.entity.QueueStatus;
import com.mufg.pex.messaging.util.Database;
import com.sun.net.httpserver.HttpExchange;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class StatusHandler extends BaseHandler {

    @Override
    public Response get(HttpExchange exchange, Map<String, String> params) {

        List<QueueStatus> statusList = Database.getInstance().getQueueStatusForAllBrokers();

        Map<String, String> members = new HashMap<>();

        AtomicInteger i = new AtomicInteger(1);
        statusList.forEach(queueStatus -> {

            if (!members.containsKey(queueStatus.getMemberId())) {
                members.put(queueStatus.getMemberId(), "M" + i.getAndIncrement());
            }
        });

        Map<String, Object> response = new HashMap<>();
        response.put("QUEUE_STATUS", statusList);
        response.put("MEMBERS", members);
        response.put("NETWORK", Database.getInstance().getMemberStatusForAllBrokers());

        return Response
                .builder()
                .code(200)
                .addHeader("Content-Type", "application/json")
                .body(response)
                .build();
    }
}
