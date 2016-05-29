package Client.smclient;

import Client.common.Message;
import Client.common.MessageEnvelope;
import Client.exception.ClientNotInitializedException;
import Client.exception.InvalidEndPointException;
import akka.actor.ActorPath;
import akka.actor.ActorPaths;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.cluster.client.ClusterClient;
import akka.cluster.client.ClusterClientSettings;
import akka.dispatch.OnFailure;
import akka.dispatch.OnSuccess;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by rahul.ka on 20/05/16.
 */
public class SMClient {
    private String clientId;
    private ActorSystem system;
    private int timeoutSec;
    private ActorRef client;

    public SMClient(String clientId) {
        this.clientId = clientId;
    }

    public void initialize(String clientId, Collection<String> endpoints) {
        initialize(clientId, endpoints, Constants.DEFAULT_TIMEOUT);
    }

    public void initialize(String clientId, Collection<String> endpoints, int timeoutSec) {
        if (endpoints == null || endpoints.isEmpty()) {
            throw new InvalidEndPointException("Endpoints can't be null or empty");
        }
        Set<ActorPath> actorPaths = initialContacts(new HashSet<String>(endpoints));
        if (actorPaths == null || actorPaths.isEmpty()) {
            throw new InvalidEndPointException("Invalid Endpoints");
        }

        this.timeoutSec = timeoutSec;
        system = ActorSystem.create("SMClient-" + clientId);
        client = system.actorOf(ClusterClient.props(
                ClusterClientSettings.create(system).withInitialContacts(actorPaths)),
                "client");
    }

    public void sendMessage(Request request) {
        if (client == null || system == null) {
            throw new ClientNotInitializedException("System or Client Not initialized");
        }
        sendMessage(system, client, request);
    }

    private void sendMessage(ActorSystem system, ActorRef client, Request request) {
        final Timeout timeout = new Timeout(Duration.create(timeoutSec, TimeUnit.SECONDS));
        Message message = new Message(request.getRequestId(), request.getEvent(), request.getEventTimestamp());
        final ExecutionContext ec = system.dispatcher();
        MessageEnvelope envelope = new MessageEnvelope(request.getEntityId(), message);
        Future<Object> future = Patterns.ask(client, new ClusterClient.Send(
                Constants.ACTOR_PATH_PREFIX+request.getEntityType().getEntityType(), envelope), timeout);
        future.onSuccess(new OnSuccess<Object>() {
            public void onSuccess(Object result) {
                System.out.println(result);
            }
        }, ec);
        future.onFailure(new OnFailure() {
            @Override
            public void onFailure(Throwable throwable) throws Throwable {
                System.out.println(throwable.getCause() + " " + throwable.getMessage());
            }
        }, ec);
    }

    public void terminate() {
        if (system != null) {
            system.terminate();
        }
        system = null;
        client = null;
    }


    private Set<ActorPath> initialContacts(Set<String> endPoints) {
        HashSet<ActorPath> set = new HashSet();
        for (String endPoint : endPoints) {
            set.add(ActorPaths.fromString(Constants.CLUSTER_PREFIX + endPoint + Constants.CLUSTER_POSTFIX));
        }
        return set;
    }

}
