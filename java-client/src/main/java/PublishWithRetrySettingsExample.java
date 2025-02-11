
import com.google.api.core.ApiFuture;
import com.google.api.gax.retrying.RetrySettings;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.threeten.bp.Duration;

public class PublishWithRetrySettingsExample {
  public static void main(String... args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "gca-st-pubsub-0";
    String topicId = "topic-010";

    for(int i=0; i<10000; i++) {
      publishWithRetrySettingsExample(projectId, topicId);
    }
  }

  public static void publishWithRetrySettingsExample(String projectId, String topicId)
      throws IOException, ExecutionException, InterruptedException {
    TopicName topicName = TopicName.of(projectId, topicId);
    Publisher publisher = null;

    try {
      // Retry settings control how the publisher handles retry-able failures
      Duration initialRetryDelay = Duration.ofMillis(1); // default: 100 ms
      double retryDelayMultiplier = 1.0; // back off for repeated failures, default: 1.3
      Duration maxRetryDelay = Duration.ofMillis(1); // default : 60 seconds
      Duration initialRpcTimeout = Duration.ofMillis(10); // default: 5 seconds
      double rpcTimeoutMultiplier = 1.0; // default: 1.0
      Duration maxRpcTimeout = Duration.ofMillis(11); // default: 600 seconds
      Duration totalTimeout = Duration.ofSeconds(10); // default: 600 seconds

      RetrySettings retrySettings =
          RetrySettings.newBuilder()
              .setInitialRetryDelay(initialRetryDelay)
              .setRetryDelayMultiplier(retryDelayMultiplier)
              .setMaxRetryDelay(maxRetryDelay)
              .setInitialRpcTimeout(initialRpcTimeout)
              .setRpcTimeoutMultiplier(rpcTimeoutMultiplier)
              .setMaxRpcTimeout(maxRpcTimeout)
              .setTotalTimeout(totalTimeout)
              .build();

      // Create a publisher instance with default settings bound to the topic
      publisher = Publisher.newBuilder(topicName).setRetrySettings(retrySettings).build();

      String message = "first message";
      ByteString data = ByteString.copyFromUtf8(message);
      PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();

      // Once published, returns a server-assigned message id (unique within the topic)
      for(int i=0; i<10000; i++){
        ApiFuture<String> messageIdFuture = publisher.publish(pubsubMessage);
        String messageId = messageIdFuture.get();
        System.out.println("Published a message with retry settings: " + messageId);
      }

    } finally {
      if (publisher != null) {
        // When finished with the publisher, shutdown to free up resources.
        publisher.shutdown();
        publisher.awaitTermination(1, TimeUnit.MINUTES);
      }
    }
  }
}