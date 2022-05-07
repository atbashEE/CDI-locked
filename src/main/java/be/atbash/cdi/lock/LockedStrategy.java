/*
 * Copyright 2022 Rudy De Busscher (https://www.atbash.be)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package be.atbash.cdi.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.interceptor.InvocationContext;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@ApplicationScoped
public class LockedStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(LockedStrategy.class);
    private static boolean FIRST_TIME = true;

    private final ConcurrentMap<String, ReadWriteLock> locks = new ConcurrentHashMap<>();

    public Object execute(InvocationContext ic) throws Exception {
        if (FIRST_TIME) {
            LOG.info("Locked Interceptor is active");
            FIRST_TIME = false;
        }
        Lock lock = getLock(ic);
        try {
            return ic.proceed();
        } finally {
            lock.unlock();
        }
    }

    protected Lock getLock(InvocationContext ic) {
        Method method = ic.getMethod();
        Locked config = getLocked(method);

        String key = config.name();

        ReadWriteLock readWriteLock = locks.computeIfAbsent(key, n -> new ReentrantReadWriteLock(config.fair()));

        long timeout = config.timeoutUnit().toMillis(config.timeout());
        Lock lock = config.operation() == Locked.Operation.READ ? readWriteLock.readLock() : readWriteLock.writeLock();

        if (timeout > 0) {
            try {
                if (!lock.tryLock(timeout, TimeUnit.MILLISECONDS)) {
                    throw new IllegalStateException("Can't lock for " + key + " in " + timeout + "ms");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Locking interrupted", e);
            }

        } else {
            lock.lock();
        }
        return lock;

    }

    private Locked getLocked(Method method) {
        Locked locked = method.getAnnotation(Locked.class);
        if (locked == null) {

            locked = method.getDeclaringClass().getAnnotation(Locked.class);
        }
        return locked;
    }

}
