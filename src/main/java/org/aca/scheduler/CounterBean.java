package org.aca.scheduler;

import java.util.concurrent.atomic.AtomicInteger;
import javax.enterprise.context.ApplicationScoped;
// import io.quarkus.scheduler.Scheduled;

@ApplicationScoped
public class CounterBean {
    private AtomicInteger counter = new AtomicInteger();

    public int get() {
        return counter.get();
    }

    // @Scheduled(every = "10s")
    public void increment() {
        counter.incrementAndGet();
    }
}
