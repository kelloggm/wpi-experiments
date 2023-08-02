package gov.fda.nctr.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class RequestDataValidationFailure extends BadRequestException
{
  public RequestDataValidationFailure(String message)
  {
    super(message);
  }

  public RequestDataValidationFailure(String message, Throwable cause)
  {
    super(message, cause);
  }
}
