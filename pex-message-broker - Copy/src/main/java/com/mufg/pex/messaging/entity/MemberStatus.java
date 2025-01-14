package com.mufg.pex.messaging.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "MEMBERSTATUS")
@Data
public class MemberStatus implements Serializable {

    @Id
    private String id;
    private Date createdDate;
    private int consumers;
    private int publishers;
    private String usedMemoryMb;
    private String dbFileSize;
}
