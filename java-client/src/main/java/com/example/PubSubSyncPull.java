package com.example;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.google.cloud.pubsub.v1.stub.GrpcSubscriberStub;
import com.google.cloud.pubsub.v1.stub.SubscriberStub;
import com.google.cloud.pubsub.v1.stub.SubscriberStubSettings;
import com.google.pubsub.v1.AcknowledgeRequest;
import com.google.pubsub.v1.ModifyAckDeadlineRequest;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PullRequest;
import com.google.pubsub.v1.PullResponse;
import com.google.pubsub.v1.ReceivedMessage;

public class PubSubSyncPull {
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private static final long FAKE_WORKER_DURATION_IN_MILLISECONDS = 15 * 60 * 1_000L; // 15 min.
    private static final int ACK_DEADLINE_IN_SECONDS = 120;
    private static final int EXTEND_ACK_DEADLINE_SAFETY_MARGIN_IN_SECONDS = 10;
    private static final TimeUnit EXTEND_ACK_DEADLINE_UNIT = TimeUnit.SECONDS;
    private static final String SUBSCRIPTION = ProjectSubscriptionName.format("gca-st-pubsub-0", "subscription-009");

    public void init() {
        final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        final ScheduledExecutorService ackDeadlineModifier = Executors
                .newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

        try {
            logger.info("Setting up synchronous pull...");

            SubscriberStubSettings subscriberStubSettings = SubscriberStubSettings.newBuilder().build();

            try (SubscriberStub subscriber = GrpcSubscriberStub.create(subscriberStubSettings)) {
                PullRequest pullRequest = PullRequest.newBuilder()
                    .setMaxMessages(1)
                    .setReturnImmediately(false)
                    .setSubscription(SUBSCRIPTION)
                    .build();

                while (true) {
                    PullResponse pullResponse = subscriber.pullCallable().call(pullRequest);

                    if (pullResponse.getReceivedMessagesList().size() == 0) {
                        continue;
                    }

                    logger.info(String.format("Got %d message(s)...", pullResponse.getReceivedMessagesList().size()));

                    for (ReceivedMessage message : pullResponse.getReceivedMessagesList()) {
                        final Runnable modAckDeadlineTask = new ModAckDeadlineRunnable(subscriber, message);
                        final ScheduledFuture<?> modAckDeadLineFuture = ackDeadlineModifier.scheduleAtFixedRate(
                                modAckDeadlineTask,
                                ACK_DEADLINE_IN_SECONDS - EXTEND_ACK_DEADLINE_SAFETY_MARGIN_IN_SECONDS,
                                ACK_DEADLINE_IN_SECONDS - EXTEND_ACK_DEADLINE_SAFETY_MARGIN_IN_SECONDS,
                                EXTEND_ACK_DEADLINE_UNIT);

                        final Runnable worker = new WorkerRunnable(subscriber, modAckDeadLineFuture, message);
                        executor.submit(worker);
                    }
                }
            }

        } catch (Exception e) {
            logger.warning("Error");
            e.printStackTrace();
        }
    }

    class WorkerRunnable implements Runnable {
        private final SubscriberStub subscriber;
        private final ScheduledFuture<?> modAckDeadLineFuture;
        private final ReceivedMessage message;

        public WorkerRunnable(final SubscriberStub subscriber, final ScheduledFuture<?> modAckDeadLineFuture,
                final ReceivedMessage message) {
            this.subscriber = subscriber;
            this.modAckDeadLineFuture = modAckDeadLineFuture;
            this.message = message;
        }

        @Override
        public void run() {
            try {
                String id = message.getMessage().getMessageId();

                logger.info(String.format("Handling task %s using thread: %s", id, Thread.currentThread().getName()));

                try {
                    Thread.sleep(FAKE_WORKER_DURATION_IN_MILLISECONDS);

                } catch (Exception e) {
                    logger.warning("Thread error");
                    e.printStackTrace();
                }

                logger.info(String.format("Finishing task: %s", id));

                AcknowledgeRequest request = AcknowledgeRequest.newBuilder()
                    .setSubscription(SUBSCRIPTION)
                    .addAckIds(message.getAckId())
                    .build();

                modAckDeadLineFuture.cancel(true);
                subscriber.acknowledgeCallable().call(request);

            } catch (Exception e) {
                logger.warning("Error");
                e.printStackTrace();
            }
        }
    }

    class ModAckDeadlineRunnable implements Runnable {
        private final SubscriberStub subscriber;
        private final ReceivedMessage message;

        public ModAckDeadlineRunnable(final SubscriberStub subscriber, final ReceivedMessage message) {
            this.subscriber = subscriber;
            this.message = message;
        }

        @Override
        public void run() {
            try {
                logger.info(String.format("Extending ack deadline for task %s", message.getMessage().getMessageId()));
                ModifyAckDeadlineRequest request = ModifyAckDeadlineRequest.newBuilder()
                    .setSubscription(SUBSCRIPTION)
                    .setAckDeadlineSeconds(ACK_DEADLINE_IN_SECONDS)
                    .addAckIds(message.getAckId())
                    .build();

                subscriber.modifyAckDeadlineCallable().call(request);

            } catch (Exception e) {
                logger.warning("Error");
                e.printStackTrace();
            }
        }
    }
}