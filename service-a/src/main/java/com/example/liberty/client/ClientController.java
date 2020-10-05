package com.example.liberty.client;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.Asynchronous;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("/client")
@ApplicationScoped
public class ClientController {

    @Inject
    @RestClient
    private Service service;

    @Inject @ConfigProperty(name = "conference") String conf;
    @GET
    @Path("/test/{parameter}")
    @Retry
    @Timeout(100)
    @Asynchronous
    @Fallback(fallbackMethod="myFallback")
    @Timed(name="invoke")
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "404",
                description = "Backend failure",
                content = @Content(mediaType = "text/plain")),
            @APIResponse(
                responseCode = "200",
                description = "Got response from backend",
                content = @Content(mediaType = "text/plain")) })
    @Operation(summary= "Calling doSomething()",
                description="Calling the backend and post back the response")

    public CompletionStage<String> onClientSide(@PathParam("parameter") String parameter) {
        return CompletableFuture.completedFuture("We are live @" + conf + " !" + service.doSomething(parameter));
    }

    public CompletionStage<String> myFallback(@PathParam("parameter") String parameter) {
        return CompletableFuture.completedFuture("We are live @" + conf + " ! This is my fallback!");
    }
}
