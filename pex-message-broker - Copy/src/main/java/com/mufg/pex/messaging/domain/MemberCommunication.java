package com.mufg.pex.messaging.domain;

import lombok.Data;
import org.jgroups.Address;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
public class MemberCommunication implements Serializable {

    public enum TYPE {
        MESSAGE_REQUEST,
        MESSAGE_ACK_REQUEST,
        MESSAGE_RESPONSE,
        STATUS,
        QUEUE_CREATED
    }

    private String id = UUID.randomUUID().toString();
    private Address memberId = null;
    private TYPE type;
    private Map<String, Object> parameters = new HashMap<>();
}
