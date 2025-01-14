package com.mufg.pex.messaging.domain;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

@Data
public class MessageQueue {

    private Queue<Message> pendingMessages = new PriorityQueue<>();
    private Map<String, Message> unAcknowledgedMessages = new HashMap<>();
    private Map<String, Message> transferred = new HashMap<>();
    private long processedMessages = 0;

    public Message poll() {

        Message message = pendingMessages.poll();

        if (message != null) {
            unAcknowledgedMessages.put(message.getId(), message);
        }

        return message;
    }

    public Message transfer() {

        Message message = pendingMessages.poll();

        if (message != null) {
            transferred.put(message.getId(), message);
        }

        return message;
    }

    public boolean transferAck(String messageId) {

        return transferred.remove(messageId) != null;
    }

    public boolean acknowledge(String messageId) {

        Message message = unAcknowledgedMessages.remove(messageId);

        if (message != null) {

            processedMessages++;
        }

        return message != null;
    }

    public Map<String, Long> getStatus() {

        Map<String, Long> status = new HashMap<>();
        status.put("PENDING", (long) pendingMessages.size());
        status.put("NO_ACK", (long) unAcknowledgedMessages.size());
        status.put("PROCESSED", processedMessages);

        return status;
    }
}
