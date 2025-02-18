
import com.google.cloud.pubsub.v1.stub.GrpcSubscriberStub;
import com.google.cloud.pubsub.v1.stub.SubscriberStub;
import com.google.cloud.pubsub.v1.stub.SubscriberStubSettings;
import com.google.pubsub.v1.AcknowledgeRequest;
import com.google.pubsub.v1.ModifyAckDeadlineRequest;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PullRequest;
import com.google.pubsub.v1.PullResponse;
import com.google.pubsub.v1.ReceivedMessage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SubscribeSyncWithLeaseExample {
  public static void main(String... args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "gca-st-pubsub-0";
    String subscriptionId = "subscription-007";
    Integer numOfMessages = 10;

    subscribeSyncWithLeaseExample(projectId, subscriptionId, numOfMessages);
  }

  public static void subscribeSyncWithLeaseExample(
      String projectId, String subscriptionId, Integer numOfMessages)
      throws IOException, InterruptedException {
    SubscriberStubSettings subscriberStubSettings =
        SubscriberStubSettings.newBuilder()
            .setTransportChannelProvider(
                SubscriberStubSettings.defaultGrpcTransportProviderBuilder()
                    .setMaxInboundMessageSize(20 << 20) // 20 MB
                    .build())
            .build();

    try (SubscriberStub subscriber = GrpcSubscriberStub.create(subscriberStubSettings)) {

      String subscriptionName = ProjectSubscriptionName.format(projectId, subscriptionId);

      PullRequest pullRequest =
          PullRequest.newBuilder()
              .setMaxMessages(numOfMessages)
              .setSubscription(subscriptionName)
              .build();

      // Use pullCallable().futureCall to asynchronously perform this operation.
      PullResponse pullResponse = subscriber.pullCallable().call(pullRequest);

      // Stop the program if the pull response is empty to avoid acknowledging
      // an empty list of ack IDs.
      if (pullResponse.getReceivedMessagesList().isEmpty()) {
        System.out.println("No message was pulled. Exiting.");
        return;
      }

      List<String> ackIds = new ArrayList<>();
      for (ReceivedMessage message : pullResponse.getReceivedMessagesList()) {
        System.out.println("Pulled message");
        ackIds.add(message.getAckId());

        // Modify the ack deadline of each received message from the default 10 seconds to 30.
        // This prevents the server from redelivering the message after the default 10 seconds
        // have passed.
        System.out.println("Sleeping for 61 seconds");
        TimeUnit.SECONDS.sleep(61);
        System.out.println("Modifying ack deadline now");
        ModifyAckDeadlineRequest modifyAckDeadlineRequest =
            ModifyAckDeadlineRequest.newBuilder()
                .setSubscription(subscriptionName)
                .addAckIds(message.getAckId())
                .setAckDeadlineSeconds(30)
                .build();

        subscriber.modifyAckDeadlineCallable().call(modifyAckDeadlineRequest);
      }

      // Acknowledge received messages.
      AcknowledgeRequest acknowledgeRequest =
          AcknowledgeRequest.newBuilder()
              .setSubscription(subscriptionName)
              .addAllAckIds(ackIds)
              .build();

      // Use acknowledgeCallable().futureCall to asynchronously perform this operation.
      subscriber.acknowledgeCallable().call(acknowledgeRequest);
      System.out.println(pullResponse.getReceivedMessagesList());
    }
  }
}