package com.mufg.pex.messaging.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(indexes = @Index(name="IDX_STATUS", columnList = "queue,status"), name = "BROKERMESSAGE")
@Data
public class BrokerMessage implements Serializable {

    @Id
    private String id = UUID.randomUUID().toString();

    @Lob
    private String payload;

    @Lob
    private String headers;

    private String status;

    private String queue;

    private String consumerType;

    private String consumerId;

    private Date receivedTime;

    private Date consumedTime;

    private Date ackTime;

    @Transient
    private Map<String, String> headersMap;

    @PostLoad
    private void postLoad() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            headersMap = objectMapper.readValue(headers, HashMap.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @PrePersist
    @PreUpdate
    private void prePersist() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            headers = objectMapper.writeValueAsString(headersMap);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
