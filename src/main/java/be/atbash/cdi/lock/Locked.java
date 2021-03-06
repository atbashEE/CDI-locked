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

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The access to the method is protected by a read/write lock.
 */
@InterceptorBinding
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface Locked {

    /**
     * @return Returns the name of the lock that is required.
     */
    @Nonbinding
    String name() default "generic";

    /**
     * @return if the lock is fair.
     */
    @Nonbinding
    boolean fair() default false;

    /**
     * @return the operation used on the lock, default to read but you can use write.
     */
    @Nonbinding
    Operation operation() default Operation.READ;


    /**
     * @return the access timeout for this method. Ignored by default since it is 0.
     */
    @Nonbinding
    long timeout() default 0L;

    /**
     * @return the timeout unit (default ms).
     */
    @Nonbinding
    TimeUnit timeoutUnit() default TimeUnit.MILLISECONDS;

    enum Operation {
        READ, WRITE
    }
}
