package com.noonacademy.assignment.queue.server.service;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class QueueManagerService {

  private static final Logger LOGGER = LoggerFactory.getLogger(QueueManagerService.class);

  private final String queuePersistenceDirectory;

  private final Integer queueUpperCapacityLimitAlert;

  private final FilePersistenceService filePersistenceService;

  private Map<String, QueueIndex> queueStart;

  public QueueManagerService(
      @Value("${queue.persistence.directory}") String queuePersistenceDirectory,
      @Value("${queue.upper.capacity.limit.alert}") Integer queueUpperCapacityLimitAlert,
      FilePersistenceService filePersistenceService) {
    this.queuePersistenceDirectory = queuePersistenceDirectory;
    this.filePersistenceService = filePersistenceService;
    this.queueUpperCapacityLimitAlert = queueUpperCapacityLimitAlert;
    this.queueStart = new ConcurrentHashMap<>();
  }

  /**
   * If server had restarted due to any reason Then this method will reload the previous state of
   * the Queue
   */
  @PostConstruct
  public void init() {

    File directory = new File(queuePersistenceDirectory);
    try {
      if (!directory.exists()) {
        FileUtils.forceMkdir(directory);
      }

      for (File queue : directory.listFiles()) {
        String[] listOfFileNames = queue.list();
        if (listOfFileNames == null || listOfFileNames.length == 0) {
          queueStart.putIfAbsent(queue.getName(), new QueueIndex(0, 0));
        } else {
          List<Integer> sortedList = Arrays.stream(listOfFileNames)
              .map(Integer::parseInt)
              .sorted(Integer::compare)
              .collect(Collectors.toList());
          queueStart.putIfAbsent(queue.getName(),
              new QueueIndex(sortedList.get(0), sortedList.get(sortedList.size() - 1)));
        }
      }
    } catch (IOException e) {
      LOGGER.error("Failed to create Queue Persistence Directory: " + queuePersistenceDirectory, e);
      throw new RuntimeException("Failed to create Queue Persistence Directory");
    }
  }

  public synchronized void enqueue(String entry, String queueName) {
    LOGGER.debug("Enqueue operation. Entry: " + entry);
    QueueIndex queueIndex = getQueueIndex(queueName);
    int endIndex = queueIndex.getEndIndex();
    File queueDir = new File(queuePersistenceDirectory + "/" + queueName);
    try {
      if (!queueDir.exists()) {
        FileUtils.forceMkdir(queueDir);
      }
      String entryFileName = queueDir + "/" + endIndex;

      filePersistenceService.saveEntryToFile(entryFileName, entry);
    } catch (IOException e) {
      LOGGER.error("Failed to persist entry at index: " + endIndex, e);
      throw new RuntimeException("Failed to persist entry");
    }

    queueIndex.setEndIndex(++endIndex);
    if ((endIndex - queueIndex.getStartIndex()) > queueUpperCapacityLimitAlert) {
      LOGGER.warn(
          "Queue has breached upper capacity limit. There may be a production outage preventing consumption from queue.");
    }
  }

  public synchronized String dequeue(String queueName) {
    QueueIndex queueIndex = getQueueIndex(queueName);
    int startIndex = queueIndex.getStartIndex();
    if (++startIndex == queueIndex.getEndIndex()) {
      return null;
    }

    String entryFileName = new StringBuilder(queuePersistenceDirectory)
        .append("/")
        .append(queueName)
        .append("/")
        .append(startIndex).toString();
    String entry;
    try {
      entry = filePersistenceService.readEntryAndDeleteFile(entryFileName);
      queueIndex.setStartIndex(++startIndex);
    } catch (IOException e) {
      LOGGER.error("Failed to read entry from index: " + startIndex, e);
      throw new RuntimeException("Failed to read entry");
    }
    LOGGER.debug("Dequeue operation. Entry: " + entry);
    return entry;
  }

  private QueueIndex getQueueIndex(String queueName) {
    queueStart.putIfAbsent(queueName, new QueueIndex(-1, 0));
    return queueStart.get(queueName);
  }


  class QueueIndex {

    private int startIndex;
    private int endIndex;

    public QueueIndex(int startIndex, int endIndex) {
      this.startIndex = startIndex;
      this.endIndex = endIndex;
    }

    public int getStartIndex() {
      return startIndex;
    }

    public void setStartIndex(int startIndex) {
      this.startIndex = startIndex;
    }

    public int getEndIndex() {
      return endIndex;
    }

    public void setEndIndex(int endIndex) {
      this.endIndex = endIndex;
    }

    public boolean isEndReached() {
      return startIndex >= endIndex;
    }
  }

}
