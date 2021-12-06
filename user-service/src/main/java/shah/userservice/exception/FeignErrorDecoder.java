package shah.userservice.exception;

import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import feign.Response;
import feign.codec.ErrorDecoder;

public class FeignErrorDecoder implements ErrorDecoder {

  @Override
  public Exception decode(String methodKey, Response response) {
    System.out.println("status:" + response.status());
    switch (response.status()) {
      case 400:
        break;
      case 302:
        return new ResponseStatusException(HttpStatus.valueOf(response.status()),
            "Feign Client only accept status 2xx, else will trigger FeignException");
      case 404:
        return new ResponseStatusException(HttpStatus.valueOf(response.status()),
            "Check your service uri");
      case 500:
        return new ResponseStatusException(HttpStatus.valueOf(response.status()),
            "Server error");
      default:
        return new Exception(response.reason());
    }
    return null;
  }
}