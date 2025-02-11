
import com.google.api.core.ApiFuture;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class PublisherExample {
  public static void main(String... args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "gca-st-pubsub-0";
    String topicId = "topic-010";

    for (int i=0; i<700; i++) {
      publisherExample(projectId, topicId);
      publisherExample(projectId, topicId);
  }
  }

  public static void publisherExample(String projectId, String topicId)
      throws IOException, ExecutionException, InterruptedException {
    TopicName topicName = TopicName.of(projectId, topicId);

    Publisher publisher = null;
    try {
      // Create a publisher instance with default settings bound to the topic
      publisher = Publisher.newBuilder(topicName).build();
      // Infinite loop to Publish
        String message = "Hello World!";
        ByteString data = ByteString.copyFromUtf8(message);
        PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();
  
        // Once published, returns a server-assigned message id (unique within the topic)
        for(int i=0; i<20000; i++) {
          ApiFuture<String> messageIdFuture = publisher.publish(pubsubMessage);
          String messageId = messageIdFuture.get();
          System.out.println("Published message ID: " + messageId);
          ApiFuture<String> messageIdFuture2 = publisher.publish(pubsubMessage);
          String messageId2 = messageIdFuture2.get();
          System.out.println("Published message ID: " + messageId2);
          ApiFuture<String> messageIdFuture3 = publisher.publish(pubsubMessage);
          String messageId3 = messageIdFuture3.get();
          System.out.println("Published message ID: " + messageId3);
          ApiFuture<String> messageIdFuture4 = publisher.publish(pubsubMessage);
          String messageId4 = messageIdFuture4.get();
          System.out.println("Published message ID: " + messageId4);
          ApiFuture<String> messageIdFuture5 = publisher.publish(pubsubMessage);
          String messageId5 = messageIdFuture5.get();
          System.out.println("Published message ID: " + messageId5);
          ApiFuture<String> messageIdFuture6 = publisher.publish(pubsubMessage);
          String messageId6 = messageIdFuture6.get();
          System.out.println("Published message ID: " + messageId6);
          ApiFuture<String> messageIdFuture7 = publisher.publish(pubsubMessage);
          String messageId7 = messageIdFuture7.get();
          ApiFuture<String> messageIdFuture8 = publisher.publish(pubsubMessage);
          System.out.println("Published message ID: " + messageId7);
          String messageId8 = messageIdFuture8.get();
          System.out.println("Published message ID: " + messageId8);
          ApiFuture<String> messageIdFuture9 = publisher.publish(pubsubMessage);
          String messageId9 = messageIdFuture9.get();
          System.out.println("Published message ID: " + messageId9);
          ApiFuture<String> messageIdFuture10 = publisher.publish(pubsubMessage);
          String messageId10 = messageIdFuture10.get();
          System.out.println("Published message ID: " + messageId10);
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