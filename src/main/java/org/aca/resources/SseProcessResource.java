package org.aca.resources;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
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
import javax.ws.rs.sse.SseEventSink;

import org.aca.scheduler.CounterBean;
import org.aca.resources.response.Progress;;

@Path("/process")
@ApplicationScoped
public class SseProcessResource {

    private Logger logger = Logger.getLogger(SseProcessResource.class.getName());

    private Sse sse;
    private SseEventSink sseEventSink = null;
    private boolean started = false;

    @Inject
    CounterBean counter;

    @GET
    @Path("/start")
    @Produces(MediaType.APPLICATION_JSON)
    public Response start() {
        if (!started) {
            CompletableFuture.runAsync(() -> process());
            started = true;
            return Response.ok(new Progress("started", 0)).build();
        }
        return Response.ok(new Progress("in progress", counter.get())).build();
    }

    @GET
    @Path("/status")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void status(@Context SseEventSink sink, @Context Sse sse) {
        this.sse = sse;
        this.sseEventSink = sink;
    }

    private void process() {
        while (counter.get() < 100) {
            this.counter.increment();
            this.sleep();
            if (this.counter.get() <= 100) {
                // The event name has to be message by default so the onmessage works on the
                // client
                // otherwise the client needs to use the addEventListener function to subscribe
                // to different events
                this.sendProgressEvent("message", new Progress("in progress", counter.get()));
                if (this.counter.get() == 100) {
                    this.started = false;
                }
            }
        }
    }

    private void sleep() {
        try {
            TimeUnit.SECONDS.sleep(new Random().nextInt(5) + 1);
        } catch (InterruptedException e) {
            logger.fine(e.getLocalizedMessage());
        }
    }

    private void sendProgressEvent(String name, Object data) {
        if (this.sseEventSink != null && this.sse != null) {
            OutboundSseEvent event = sse.newEventBuilder()
                    .name(name)
                    .data(data.getClass(), data)
                    .mediaType(MediaType.APPLICATION_JSON_TYPE)
                    .reconnectDelay(3000)
                    .build();
            this.sseEventSink.send(event);
        } else {
            logger.info("Unable to send SSE. Broadcaster context is not set up.");
        }
    }
}
