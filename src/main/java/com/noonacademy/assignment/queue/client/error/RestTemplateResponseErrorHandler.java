package com.noonacademy.assignment.queue.client.error;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

public class RestTemplateResponseErrorHandler implements ResponseErrorHandler {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(RestTemplateResponseErrorHandler.class);

  @Override
  public boolean hasError(ClientHttpResponse httpResponse) throws IOException {

    return (httpResponse.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR
        || httpResponse.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR);
  }

  @Override
  public void handleError(ClientHttpResponse httpResponse) throws IOException {

    if (httpResponse.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR) {
      LOGGER.error("Some error occured on Queue Server");
    } else if (httpResponse.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR) {
      if (httpResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
        LOGGER.error("Invalid Queue Server URL or Queue Server is Down");
      } else {
        LOGGER.error("Bad Request to Queue Server from Client");
      }
    }
  }
}
