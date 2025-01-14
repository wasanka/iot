package com.mufg.pex.messaging.util;

import com.mufg.pex.messaging.Server;
import com.mufg.pex.messaging.entity.BrokerMessage;
import com.mufg.pex.messaging.entity.BrokerQueue;
import com.mufg.pex.messaging.entity.MemberStatus;
import com.mufg.pex.messaging.entity.QueueStatus;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.File;
import java.math.BigInteger;
import java.util.*;

@Slf4j
public class Database {

    @Getter
    private static Database instance = new Database();
    private static EntityManagerFactory emf = null;

    static {
        Map properties = new HashMap();
        properties.put("javax.persistence.jdbc.url", "jdbc:h2:" + Environment.get("DB_FILE_NAME", "/data/broker-database") + ";CACHE_SIZE=131072");
        emf = Persistence.createEntityManagerFactory("examplePU", properties);
    }

    private Database() {

    }

    public String createBrokerMessage(String queue, String payload, Map<String, String> headers) {

        return runInTraction(em -> {

            BrokerMessage message = new BrokerMessage();

            message.setPayload(payload);
            message.setHeadersMap(headers);
            message.setConsumerType("CONSUMER");
            message.setReceivedTime(new Date());
            message.setStatus("RECEIVED");
            message.setQueue(queue);

            em.persist(message);

            return message.getId();
        });
    }

    public BrokerMessage findMessage(String messageId, String queue) {

        return runInTraction(em -> em.find(BrokerMessage.class, messageId));
    }

    public BrokerMessage getFirstMessageForQueue(String queue, String consumerType, String consumerId) {

        return runInTraction(em -> {
            TypedQuery<BrokerMessage> query = em.createQuery("SELECT bm from BrokerMessage bm WHERE status = 'RECEIVED' and queue = :queue", BrokerMessage.class);
            query.setParameter("queue", queue);
            query.setMaxResults(1);
            BrokerMessage message = null;
            try {
                message = query.getSingleResult();
            } catch (NoResultException nre) {

            }

            if (message != null) {
                message.setConsumedTime(new Date());
                message.setConsumerType(consumerType);
                message.setConsumerId(consumerId);
                message.setStatus("PENDING_ACK");

                em.merge(message);
            }

            return message;
        });
    }

    public boolean updateMessageStatus(String messageId, String consumerType, String consumerId, String status) {

        return Boolean.TRUE.equals(runInTraction(em -> {
            BrokerMessage message = em.find(BrokerMessage.class, messageId);

            if (message != null) {

                message.setStatus(status);
                message.setAckTime(new Date());
                message.setConsumerType(consumerType);
                message.setConsumerId(consumerId);

                em.merge(message);
            }

            return message != null;
        }));
    }

    public void gatherMemberStatus() {

        runInTraction(em -> {

            MemberStatus memberStatus = new MemberStatus();
            memberStatus.setId(Server.ID);
            memberStatus.setConsumers(Cache.getInstance().getConsumers().size());
            memberStatus.setPublishers(Cache.getInstance().getPublishers().size());
            memberStatus.setCreatedDate(new Date());

            long fileLength = new File(Environment.get("DB_FILE_NAME", "/data/broker-database") + ".mv.db").length();

            memberStatus.setDbFileSize(humanReadableByteCountSI(fileLength));

            Runtime runtime = Runtime.getRuntime();
            long usedMemory = runtime.totalMemory() - runtime.freeMemory();
            long totalMemory = runtime.totalMemory();

            memberStatus.setUsedMemoryMb(humanReadableByteCountSI(usedMemory) + " of " + humanReadableByteCountSI(totalMemory));

            em.merge(memberStatus);

            return null;
        });
    }

    private static String humanReadableByteCountSI(long bytes) {
        if (bytes < 1000) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(1000));
        String pre = "kMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1000, exp), pre);
    }

    public void gatherQueueStatus() {

        runInTraction(em -> {

            Query q = em.createNativeQuery("SELECT Q.NAME, M.STATUS, COUNT(M.QUEUE)\n" +
                    "FROM BROKERQUEUE Q LEFT OUTER JOIN BROKERMESSAGE M ON Q.NAME = M.QUEUE\n" +
                    "GROUP BY Q.NAME, M.STATUS");

            List results = q.getResultList();

            Map<String, QueueStatus> queues = new HashMap<>();

            for (Object o : results) {

                Object[] values = (Object[]) o;
                QueueStatus status = queues.get((String) values[0]);

                if (status == null) {

                    status = new QueueStatus();
                    queues.put((String) values[0], status);
                    status.setId(Server.ID + "_" + values[0]);
                    status.setQueue(values[0].toString());
                    status.setCreatedDate(new Date());
                    status.setMemberId(Server.ID);
                    status.setDepth(0);
                    status.setCompleted(0);
                    status.setPending(0);
                }

                if (values[1] == null) {
                    continue;
                }

                if (values[1].toString().equals("ACK")) {

                    status.setCompleted(((BigInteger) values[2]).longValue());
                } else if (values[1].toString().equals("PENDING_ACK")) {

                    status.setPending(((BigInteger) values[2]).longValue());
                } else if (values[1].toString().equals("RECEIVED")) {

                    status.setDepth(((BigInteger) values[2]).longValue());
                }
            }

            queues.values().forEach(em::merge);

            return null;
        });
    }

    public List<QueueStatus> getQueueStatusForThisBroker() {

        return runInTraction(em -> {
            TypedQuery<QueueStatus> query = em.createQuery("SELECT QS FROM QueueStatus QS WHERE memberId = :memberId", QueueStatus.class);
            query.setParameter("memberId", Server.ID);
            List<QueueStatus> statusList = query.getResultList();

            return statusList;
        });
    }

    public void persistQueueStatusesForOtherMembers(List<QueueStatus> statusList) {

        runInTraction(em -> {
            statusList.forEach(em::merge);
            return null;
        });
    }

    public List<QueueStatus> getQueueStatusForAllBrokers() {

        return runInTraction(em -> {
            TypedQuery<QueueStatus> query = em.createQuery("SELECT QS FROM QueueStatus QS", QueueStatus.class);
            return query.getResultList();
        });
    }

    public List<MemberStatus> getMemberStatusForAllBrokers() {

        return runInTraction(em -> {
            TypedQuery<MemberStatus> query = em.createQuery("SELECT QS FROM MemberStatus QS", MemberStatus.class);
            return query.getResultList();
        });
    }

    public void clearExpiredStatuses() {

        runInTraction(em -> {
            em.createNativeQuery("DELETE FROM QUEUESTATUS WHERE createddate < DATEADD('MINUTE', -1, CURRENT_TIMESTAMP)").executeUpdate();
            return null;
        });
    }

    private <T> T runInTraction(DatabaseOperation<T> operation) {

        EntityManager em = emf.createEntityManager();
        T t = null;
        try {
            em.getTransaction().begin();
            t = operation.run(em);
            em.getTransaction().commit();
        } catch (Exception e) {
            log.error("Database Error", e);
            em.getTransaction().rollback();
        } finally {
            em.close();
        }

        return t;
    }

    public List<MemberStatus> getMemberStatusForThisBroker() {

        return runInTraction(em -> {
            TypedQuery<MemberStatus> query = em.createQuery("SELECT QS FROM MemberStatus QS WHERE id = :memberId", MemberStatus.class);
            query.setParameter("memberId", Server.ID);
            List<MemberStatus> statusList = query.getResultList();

            return statusList;
        });
    }

    public void persistMemberStatusesForOtherMembers(List<MemberStatus> memberStatuses) {
        runInTraction(em -> {
            memberStatuses.forEach(em::merge);
            return null;
        });
    }

    public Integer ackTimeout() {

        return runInTraction(em -> {

            TypedQuery<BrokerMessage> query = em.createQuery("SELECT bm from BrokerMessage bm WHERE status = 'PENDING_ACK'", BrokerMessage.class);
            List<BrokerMessage> messages = query.getResultList();

            messages.forEach(brokerMessage -> {

                int redeliveryCount = Integer.parseInt(brokerMessage.getHeadersMap().get("redeliveryCount"));
                redeliveryCount++;

                brokerMessage.getHeadersMap().put("possibleDuplicate", "true");
                brokerMessage.getHeadersMap().put("redeliveryCount", redeliveryCount + "");
                brokerMessage.setStatus("RECEIVED");
            });

            return messages.size();
        });
    }

    public void cleanExpiredMessages(int retensionMinutes) {

        runInTraction(em -> {

            Query q = em.createNativeQuery("DELETE FROM QUEUESTATUS WHERE ID = :id");
            q.setParameter("id", Server.ID);
            q.executeUpdate();

            Query query = em.createNativeQuery("DELETE FROM BROKERMESSAGE WHERE STATUS = 'ACK' AND ackTime < DATEADD('MINUTE', :retensionMinutes, CURRENT_TIMESTAMP)");
            query.setParameter("retensionMinutes", -retensionMinutes);
            int count = query.executeUpdate();
            log.info("Removed {} messages due to expiration", count);
            return null;
        });
    }

    public Boolean createQueue(String name) {

        return runInTraction(em -> {

            BrokerQueue brokerQueue = em.find(BrokerQueue.class, name);

            if (null == brokerQueue) {
                brokerQueue = new BrokerQueue();
                brokerQueue.setName(name);
                brokerQueue.setCreatedDate(new Date());
                brokerQueue.setQueue(true);
                em.merge(brokerQueue);

                return Boolean.TRUE;
            } else {

                return Boolean.FALSE;
            }
        });
    }

    public List<BrokerMessage> browseMessages(int page, int pageSize) {

        return runInTraction(em -> {

            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<BrokerMessage> cq = cb.createQuery(BrokerMessage.class);
            Root<BrokerMessage> root = cq.from(BrokerMessage.class);
            cq.select(root);

            TypedQuery<BrokerMessage> query = em.createQuery(cq);
            query.setFirstResult(page); // Set the first result to 0 (first page)
            query.setMaxResults(pageSize); // Set the maximum results (page size)

            return query.getResultList();
        });
    }
}
