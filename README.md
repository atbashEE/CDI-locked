# Named locks for CDI

Read and Write locks for CDI.  

## Why this library?

The Jakarta EJB specification has an annotation `@Lock` to handle the concurrent access to method within a Singleton bean.  In general, sometimes it required that access to some data structures is guarded by some lock to protect the integrity of it. To make sure the data can safely be updated without the risk that some other threads read the partially updated data.

This can be achieved rather easily achieved by using the `ReentrantReadWriteLock` class from the JVM itself. This library brings _named locks_ to CDI as annotation, similar to the Jakarta EJB specification.

- The _read_ lock can be obtained by several threads.
- The _write_ lock can be obtained only by one thread when no other threads have a read lock.
- Lock are _named_ so that you can have multiple within your application.
- Locks are not limited to one CDI bean but can be used on several beans.

A similar functionality can be found within DeltaSpike Core but is part of the larger framework there.

## Configuration

Add the following Maven dependency to your project.

```
    <dependency>
        <groupId>be.atbash.cdi</groupId>
        <artifactId>locked</artifactId>
        <version>1.0.0</version>
    </dependency>
```

## Usage

On CDI methods you can use the `@Locked` annotation with the following members.

- String name() 

The name of the lock. The default value is _generic_ and doesn't need to be specified if you only need one lock in your application.

- boolean fair()

Do you want to activate the _fair_ mechanism of the JVM functionality.  When set to true, default is false, an approximately arrival-order policy is applied. Setting to true can have a performance impact.

- Operation operation()

Determines the type of the lock. By default, it is a _read_ lock but a _write_ lock can be specified (`Operation.WRITE`)
    
- long timeout()

When a non-zero value is specified, a time-out is applied to acquire the lock. This works in combination with the `timeoutUnit` member. When the lock cannot be granted within the specified time-out, an `IllegalStateException` exception will be thrown.

- TimeUnit timeoutUnit()

Specified the timescale for the `timeout` member, by default it is milliseconds.
