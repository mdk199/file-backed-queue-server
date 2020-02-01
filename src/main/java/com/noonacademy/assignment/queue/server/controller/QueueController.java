package com.noonacademy.assignment.queue.server.controller;

import com.noonacademy.assignment.queue.server.service.QueueManagerService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "queue")
public class QueueController {

  private final QueueManagerService queueManagerService;

  public QueueController(QueueManagerService queueManagerService) {
    this.queueManagerService = queueManagerService;
  }

  @RequestMapping(method = RequestMethod.POST, path = "enqueue/{queueName}")
  public void enqueue(@RequestBody String entry, @PathVariable("queueName") String queueName) {
    queueManagerService.enqueue(entry, queueName);
  }

  @RequestMapping(method = RequestMethod.GET, path = "dequeue/{queueName}")
  public String dequeue(@PathVariable("queueName") String queueName) {
    return queueManagerService.dequeue(queueName);
  }
}
