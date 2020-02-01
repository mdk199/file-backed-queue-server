package com.noonacademy.assignment.queue.client;

import com.noonacademy.assignment.queue.client.error.RestTemplateResponseErrorHandler;
import com.noonacademy.assignment.queue.client.exception.EnqueueOperationFailed;
import com.noonacademy.assignment.queue.client.exception.PushOperationFailed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class QueueClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(QueueClient.class);

  private final String queueServerBaseUrl;

  private final long pollingInterval;

  private final TimeUnit pollingIntervalTimeUnit;

  private final RestTemplate restTemplate =
      new RestTemplateBuilder()
          .errorHandler(new RestTemplateResponseErrorHandler())
          .build();

  public QueueClient(String queueServerBaseUrl) {
    this.queueServerBaseUrl = queueServerBaseUrl;
    this.pollingInterval = 2;
    this.pollingIntervalTimeUnit = TimeUnit.SECONDS;
  }

  public QueueClient(String queueServerBaseUrl, long pollingInterval,
      TimeUnit pollingIntervalTimeUnit) {
    this.queueServerBaseUrl = queueServerBaseUrl;
    this.pollingInterval = pollingInterval;
    this.pollingIntervalTimeUnit = pollingIntervalTimeUnit;
  }

  public void push(String queueName, String entry)
      throws PushOperationFailed, InterruptedException {
    try {
      this.enqueue(queueName, entry);
    } catch (EnqueueOperationFailed enqueueOperationFailed) {
      throw new PushOperationFailed();
    }
  }

  protected void enqueue(String queueName, String entry) throws EnqueueOperationFailed {

    String enqueueApiUrl = this.queueServerBaseUrl + "/queue/enqueue/" + queueName;

    try {
      ResponseEntity<String> entity = restTemplate
          .postForEntity(enqueueApiUrl, entry, String.class);

      if (!entity.getStatusCode().is2xxSuccessful()) {
        throw new EnqueueOperationFailed();
      }
    } catch (Exception e) {
      LOGGER.error("Failed to process enqueue request", e);
      throw new EnqueueOperationFailed();
    }
  }

  public String poll(String queueName, long timeoutInterval, TimeUnit timeoutIntervalTimeUnit)
      throws InterruptedException, TimeoutException {

    try {
      long pollingIntervalInMillis = pollingIntervalTimeUnit.toMillis(pollingInterval);
      long timeoutIntervalInMillis = timeoutIntervalTimeUnit.toMillis(timeoutInterval);

      long waitingTimeTillNow = 0;

      while (true) {
        String returnValue = this.dequeue(queueName);

        if (returnValue != null) {
          return returnValue;
        }

        LOGGER.debug("Going in sleep for " + pollingIntervalInMillis + "ms");
        Thread.sleep(pollingIntervalInMillis);

        waitingTimeTillNow += pollingIntervalInMillis;

        if (waitingTimeTillNow >= timeoutIntervalInMillis) {
          throw new TimeoutException("Polling timed out");
        }
      }
    } catch (InterruptedException e) {
      LOGGER.error("Current Thread got inturpted while polling", e);
      throw e;
    }
  }

  public String poll(String queueName) throws InterruptedException {

    try {
      long pollingIntervalInMillis = pollingIntervalTimeUnit.toMillis(pollingInterval);

      while (true) {
        String returnValue = this.dequeue(queueName);

        if (returnValue != null) {
          return returnValue;
        }

        LOGGER.debug("Going in sleep for " + pollingIntervalInMillis + "ms");
        Thread.sleep(pollingIntervalInMillis);
      }
    } catch (InterruptedException e) {
      LOGGER.error("Current thread got inturpted while polling", e);
      throw e;
    }
  }

  private String dequeue(String queueName) {

    String dequeueApiUrl = this.queueServerBaseUrl + "/queue/dequeue/" + queueName;

    try {
      ResponseEntity<String> entity = restTemplate.getForEntity(dequeueApiUrl, String.class);

      if (entity.getStatusCode().is2xxSuccessful()) {
        return entity.getBody();
      }
    } catch (Exception e) {
      LOGGER.error("Failed to process dequeue request", e);
    }

    return null;
  }
}
