package uk.co.malbec.bingo;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import uk.co.malbec.bingo.present.response.ErrorCode;
import uk.co.malbec.bingo.present.response.ErrorResponse;

import java.io.ObjectStreamClass;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handle(MethodArgumentNotValidException e) {

        List<Map<String, Object>> errorsResponse = new ArrayList<>();
        for (ObjectError error : e.getBindingResult().getAllErrors()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("errorType", error.getCode());

            List<Object> arguments = new ArrayList<>();
            for (Object o : error.getArguments()) {
                if (o instanceof DefaultMessageSourceResolvable) {
                    DefaultMessageSourceResolvable arg = (DefaultMessageSourceResolvable) o;
                    arguments.add(arg.getCode());
                } else {
                    arguments.add(o);
                }
            }

            errorResponse.put("arguments", arguments);
            errorsResponse.add(errorResponse);
        }

        return new ResponseEntity<>(new ErrorResponse(ErrorCode.CLIENT_INVALID_INPUT, errorsResponse ), HttpStatus.BAD_REQUEST);

    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handle(Exception e) {
        logger.error("Unknown error thrown", e);
        return new ResponseEntity<>(new ErrorResponse(ErrorCode.SERVER_UNKNOWN_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);

    }

}