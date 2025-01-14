package com.mufg.pex.messaging.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Entity
@Data
public class BrokerQueue {

    @Id
    private String name;

    private Date createdDate;

    private boolean queue;
}
