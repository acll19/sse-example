package org.aca.resources;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseBroadcaster;
import javax.ws.rs.sse.SseEventSink;

import org.aca.scheduler.CounterBean;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;

@Path("/process")
@ApplicationScoped
public class SseProcessResource {

    private Logger logger = Logger.getLogger(SseProcessResource.class.getName());

    private Sse sse;
    private SseBroadcaster broadcaster;

    @Inject
    CounterBean counter;

    @GET
    @Path("/start")
    @Produces(MediaType.APPLICATION_JSON)
    public Response start() {
        if (counter.get() == 0) {
            // Do not wait for process to end
            Uni.createFrom()
                    .nullItem()
                    .emitOn(Infrastructure.getDefaultWorkerPool())
                    .subscribe()
                    .with(item -> process(), Throwable::printStackTrace);
            return Response.ok(String.format("{\"status\":\"started\", \"progress\":%d}", 0)).build();
        }
        return Response.ok(String.format("{\"status\":\"in progress\", \"progress\":%d}", counter.get())).build();
    }

    @GET
    @Path("/status")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void subscribeToProcessStatus(@Context SseEventSink sink, @Context Sse sse) {
        if (this.sse == null || this.broadcaster == null) {
            this.sse = sse;
            this.broadcaster = sse.newBroadcaster();
        }

        this.broadcaster.register(sink);
        logger.info("New sink registered to broadcaster.");
    }

    private void process() {
        while (counter.get() < 100) {
            counter.increment();
            sleep();
            if (counter.get() < 100) {
                // The event name has to be message by default so the onmessage works on the
                // client
                // otherwise the client needs to use the addEventListener function to subscribe
                // to different events
                broadcastData("message",
                        String.format("{\"status\":\"in progress\", \"progress\":%d}", counter.get()));
            }
        }
        if (counter.get() == 100) {
            broadcastData("message", String.format("{\"status\":\"done\", \"progress\":%d}", counter.get()));
        }
    }

    private void sleep() {
        try {
            TimeUnit.SECONDS.sleep(new Random().nextInt(5) + 1);
        } catch (InterruptedException e) {
            logger.fine(e.getLocalizedMessage());
        }
    }

    private void broadcastData(String name, Object data) {
        if (broadcaster != null) {
            OutboundSseEvent event = sse.newEventBuilder()
                    .name(name)
                    .data(data.getClass(), data)
                    .mediaType(MediaType.APPLICATION_JSON_TYPE)
                    .build();
            broadcaster.broadcast(event);
        } else {
            logger.info("Unable to send SSE. Broadcaster context is not set up.");
        }
    }
}
