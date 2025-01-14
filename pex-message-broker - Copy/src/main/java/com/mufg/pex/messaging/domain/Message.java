package com.mufg.pex.messaging.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
@AllArgsConstructor
public class Message implements Comparable<Message>, Serializable {

    private String id;
    private String body;
    private Map<String, String> headers;

    @Override
    public int compareTo(Message o) {
        return this.id.compareTo(o.id);
    }
}
