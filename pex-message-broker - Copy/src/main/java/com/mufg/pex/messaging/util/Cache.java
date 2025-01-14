package com.mufg.pex.messaging.util;

import com.mufg.pex.messaging.domain.MemberCommunication;
import com.mufg.pex.messaging.domain.MessageQueue;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

@Slf4j
public class Cache {

    @Getter
    private static final Cache instance = new Cache();

    private Map<String, MessageQueue> queues = new HashMap<>();

    @Getter
    private TTLList<String, String> publishers = new TTLList<>(Long.parseLong(System.getenv("PUBLISHER_TIMEOUT")));

    @Getter
    private TTLList<String, String> consumers = new TTLList<>(Long.parseLong(System.getenv("PUBLISHER_TIMEOUT")));

    @Getter
    private TTLList<String, String> transfers = new TTLList<>(5000);

    @Getter
    private BlockingDeque<MemberCommunication> communications = new LinkedBlockingDeque<>();

    private Cache() {

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                log.info("  --------------");

                Database.getInstance().gatherQueueStatus();
                Database.getInstance().gatherMemberStatus();
                clearExpiredClients();

                transfers.getKeys().forEach(s -> {

                    Cluster.getInstance().requestTransfer(s);
                });
                log.info("  --------------");
            }
        }, 0, Integer.parseInt(Environment.get("MEMBER_COMMUNICATION_INTERVAL", "5000")));

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Database.getInstance().gatherQueueStatus();
                Database.getInstance().gatherMemberStatus();
                clearExpiredClients();
            }
        }, 0, Integer.parseInt(Environment.get("STATUS_CHECK_INTERVAL", "5000")));

        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                log.info("Moving pending ack messages back to received");
                int records = Database.getInstance().ackTimeout();
                log.info("Moving pending ack messages back to received done. {} messages updated", records);
            }
        }, 0, Integer.parseInt(Environment.get("PENDING_ACK_TIMEOUT", "30000")));

        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                int retensionMinutes = Integer.parseInt(Environment.get("COMPLETED_MESSAGE_RETENSION_MINUTES", "1440"));
                Database.getInstance().cleanExpiredMessages(retensionMinutes);
            }
        }, 0, Integer.parseInt(Environment.get("COMPLETED_MESSAGE_CLEANUP_INTERVAL", "30000")));
    }

    public boolean createQueue(String clientId, String name) {

        publishers.put(clientId, name);

        if (!queues.containsKey(name)) {

            queues.put(name, new MessageQueue());

            MemberCommunication communication = new MemberCommunication();
            communication.setType(MemberCommunication.TYPE.QUEUE_CREATED);
            communication.getParameters().put("QUEUE", name);
            communication.getParameters().put("CLIENT_ID", clientId);

            communications.add(communication);

            return Database.getInstance().createQueue(name);
        }

        return false;
    }

    public void clearExpiredClients() {

        Database.getInstance().clearExpiredStatuses();

        log.info(" | Publishers: {}", publishers.size());
        log.info(" | Consumers: {}", consumers.size());
    }

    @Getter
    private Map<String, Map<String, Map<String, Long>>> status = new HashMap<>();
}
