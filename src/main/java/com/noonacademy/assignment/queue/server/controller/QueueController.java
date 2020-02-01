package com.noonacademy.assignment.queue.server.controller;

import com.noonacademy.assignment.queue.server.service.QueueService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "queue")
public class QueueController {

  private final QueueService queueService;

  public QueueController(QueueService queueService) {
    this.queueService = queueService;
  }

  @RequestMapping(method = RequestMethod.POST, path = "/{queueName}/enqueue")
  public void enqueue(@RequestBody String entry, @PathVariable("queueName") String queueName) {
    queueService.enqueue(entry, queueName);
  }

  @RequestMapping(method = RequestMethod.GET, path = "/{queueName}/dequeue")
  public String dequeue(@PathVariable("queueName") String queueName) {
    return queueService.dequeue(queueName);
  }
}
