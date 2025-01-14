package com.mufg.pex.messaging.util;

import com.mufg.pex.messaging.domain.MemberCommunication;
import com.mufg.pex.messaging.entity.BrokerMessage;
import com.mufg.pex.messaging.entity.MemberStatus;
import com.mufg.pex.messaging.entity.QueueStatus;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jgroups.*;

import java.util.*;

import static com.mufg.pex.messaging.Server.ID;

@Slf4j
public final class Cluster {

    private JChannel ch;

    @Getter
    private static Cluster instance = new Cluster();

    private Optional<Address> getAddress(String name) {
        View view = ch.view();
        return view.getMembers().stream()
                .filter(address -> name.equals(address.toString()))
                .findAny();
    }

    private Cluster() {

        try {
            ch = new JChannel("tcp.xml").name(ID)
                    .setReceiver(new MyReceiver(ID))
                    .connect("demo-cluster");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ch.setDiscardOwnMessages(true);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                while (!Cache.getInstance().getCommunications().isEmpty()) {

                    MemberCommunication communication = Cache.getInstance().getCommunications().poll();
                    Address address = communication.getMemberId();
                    communication.setMemberId(null);
                    ObjectMessage bm = new ObjectMessage();
                    bm.setObject(communication);
                    try {
                        if (null != address) {
                            ch.send(getAddress(address.toString()).get(), communication);
                        } else {
                            ch.send(bm);
                        }
                        log.info("Communication sent for type {} to {}", communication.getType(), address == null ? "all" : address);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

                MemberCommunication communication = new MemberCommunication();
                communication.setType(MemberCommunication.TYPE.STATUS);
                communication.getParameters().put("STATUS", Database.getInstance().getQueueStatusForThisBroker());
                communication.getParameters().put("NETWORK", Database.getInstance().getMemberStatusForThisBroker());
                ObjectMessage bm = new ObjectMessage();
                bm.setObject(communication);

                try {
                    ch.send(bm);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }, 0, Integer.parseInt(Environment.get("MEMBER_COMMUNICATION_INTERVAL", "5000")));
    }

    public void requestTransfer(String queue) {

        MemberCommunication request = new MemberCommunication();
        request.setType(MemberCommunication.TYPE.MESSAGE_REQUEST);
        request.getParameters().put("QUEUE", queue);

        Cache.getInstance().getCommunications().add(request);
    }

    public void transferMessagesToMember(List<BrokerMessage> messages, String queue, Address member) {

        MemberCommunication request = new MemberCommunication();
        request.setType(MemberCommunication.TYPE.MESSAGE_RESPONSE);
        request.getParameters().put("QUEUE", queue);
        request.getParameters().put("MESSAGES", messages);
        request.setMemberId(member);

        Cache.getInstance().getCommunications().add(request);
    }

    protected static class MyReceiver implements Receiver {
        protected final String name;

        protected MyReceiver(String name) {
            this.name = name;
        }

        public void receive(Message msg) {

            MemberCommunication request = msg.getObject();

            switch (request.getType()) {

                case MESSAGE_REQUEST -> {

                    log.info("Received MESSAGE_REQUEST");
                    String queue = (String) request.getParameters().get("QUEUE");

                    List<BrokerMessage> messages = new ArrayList<>();
                    for (int i = 0; i < 100; i++) {

                        BrokerMessage brokerMessage = Database.getInstance().getFirstMessageForQueue(queue, "MEMBER", msg.src().toString());
                        if (brokerMessage == null) {
                            break;
                        }
                        messages.add(brokerMessage);
                    }

                    if (!messages.isEmpty()) {

                        Cluster.getInstance().transferMessagesToMember(messages, queue, msg.src());
                    }
                }
                case MESSAGE_RESPONSE -> {

                    log.info("Received MESSAGE_RESPONSE");

                    String queue = (String) request.getParameters().get("QUEUE");

                    List<BrokerMessage> messages = (List<BrokerMessage>) request.getParameters().get("MESSAGES");
                    for (BrokerMessage message : messages) {

                        Database.getInstance().createBrokerMessage(queue, message.getPayload(), message.getHeadersMap());

                        MemberCommunication mc = new MemberCommunication();
                        mc.setType(MemberCommunication.TYPE.MESSAGE_ACK_REQUEST);
                        mc.getParameters().put("MESSAGE_ID", message.getId());
                        mc.getParameters().put("QUEUE", queue);
                        mc.setMemberId(msg.src());
                        Cache.getInstance().getCommunications().add(mc);
                    }
                }
                case MESSAGE_ACK_REQUEST -> {

                    log.info("Received MESSAGE_ACK_REQUEST");

                    String messageId = (String) request.getParameters().get("MESSAGE_ID");
                    Database.getInstance().updateMessageStatus(messageId, "MEMBER", msg.src().toString(), "ACK");
                }
                case STATUS -> {

                    List<QueueStatus> statusList = (List<QueueStatus>) request.getParameters().get("STATUS");
                    List<MemberStatus> memberStatuses = (List<MemberStatus>) request.getParameters().get("NETWORK");

                    Database.getInstance().persistQueueStatusesForOtherMembers(statusList);
                    Database.getInstance().persistMemberStatusesForOtherMembers(memberStatuses);
                }
                case QUEUE_CREATED -> {

                    String queue = (String) request.getParameters().get("QUEUE");
                    String clientId = (String) request.getParameters().get("CLIENT_ID");

                    Cache.getInstance().createQueue(clientId, queue);
                }
            }
        }

        public void viewAccepted(View v) {
            log.info("-- [{}]  new view: {}", name, v);
        }
    }
}