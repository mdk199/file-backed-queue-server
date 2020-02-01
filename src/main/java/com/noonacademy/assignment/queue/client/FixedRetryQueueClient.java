package com.noonacademy.assignment.queue.client;

import com.noonacademy.assignment.queue.client.exception.EnqueueOperationFailed;
import com.noonacademy.assignment.queue.client.exception.PushOperationFailed;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FixedRetryQueueClient extends QueueClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(FixedRetryQueueClient.class);

  private final int retryCount;

  private final long retryInterval;

  private final TimeUnit retryIntervalTimeUnit;

  public FixedRetryQueueClient(String queueServerBaseUrl, int retryCount) {
    super(queueServerBaseUrl);
    this.retryCount = retryCount;
    this.retryInterval = 2;
    this.retryIntervalTimeUnit = TimeUnit.SECONDS;
  }

  public FixedRetryQueueClient(String queueServerBaseUrl, long pollingInterval,
      TimeUnit pollingIntervalTimeUnit, int retryCount) {
    super(queueServerBaseUrl, pollingInterval, pollingIntervalTimeUnit);
    this.retryCount = retryCount;
    this.retryInterval = 2;
    this.retryIntervalTimeUnit = TimeUnit.SECONDS;
  }

  public FixedRetryQueueClient(String queueServerBaseUrl, int retryCount, long retryInterval,
      TimeUnit retryIntervalTimeUnit) {
    super(queueServerBaseUrl);
    this.retryCount = retryCount;
    this.retryInterval = retryInterval;
    this.retryIntervalTimeUnit = retryIntervalTimeUnit;
  }

  public FixedRetryQueueClient(String queueServerBaseUrl, long pollingInterval,
      TimeUnit pollingIntervalTimeUnit, int retryCount, long retryInterval,
      TimeUnit retryIntervalTimeUnit) {
    super(queueServerBaseUrl, pollingInterval, pollingIntervalTimeUnit);
    this.retryCount = retryCount;
    this.retryInterval = retryInterval;
    this.retryIntervalTimeUnit = retryIntervalTimeUnit;
  }

  @Override
  public void push(String queueName, String entry)
      throws PushOperationFailed, InterruptedException {

    long retryIntervalInMillis = retryIntervalTimeUnit.toMillis(retryInterval);

    for (int i = 0; i < retryCount; i++) {
      try {
        this.enqueue(queueName, entry);
        return;
      } catch (EnqueueOperationFailed enqueueOperationFailed) {
        LOGGER.warn(
            "Failed to process push request as Queue Server may be down or unable to process request");
      }

      Thread.sleep(retryIntervalInMillis);
    }

    throw new PushOperationFailed();
  }
}
