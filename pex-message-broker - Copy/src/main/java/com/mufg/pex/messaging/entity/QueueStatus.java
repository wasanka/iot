package com.mufg.pex.messaging.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name="QUEUESTATUS")
@Data
public class QueueStatus implements Serializable {

    @Id
    private String id;
    private String memberId;
    private String queue;
    private long depth;
    private long pending;
    private long completed;
    private Date createdDate;
}
