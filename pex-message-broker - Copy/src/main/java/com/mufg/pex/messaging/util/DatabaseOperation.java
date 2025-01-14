package com.mufg.pex.messaging.util;

import javax.persistence.EntityManager;

public interface DatabaseOperation<T> {

    T run(EntityManager em);
}
