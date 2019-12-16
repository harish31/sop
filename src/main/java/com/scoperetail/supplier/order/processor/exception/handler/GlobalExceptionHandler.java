package com.scoperetail.supplier.order.processor.exception.handler;

import javax.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.scoperetail.commons.ApplicationException;
import com.scoperetail.commons.response.BaseResponse;

@ControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public @ResponseBody BaseResponse handleMethodArgumentNotValidException(
      MethodArgumentNotValidException ex) {
    BindingResult bindingResult = ex.getBindingResult();
    FieldError fieldError = bindingResult.getFieldError();
    return new BaseResponse(BaseResponse.Status.FAILURE, fieldError.getDefaultMessage());
  }

  @ExceptionHandler(InvalidFormatException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public @ResponseBody BaseResponse handleInvalidFormatException(InvalidFormatException ex) {
    return new BaseResponse(BaseResponse.Status.FAILURE, ex.getMessage());
  }

  @ExceptionHandler(ApplicationException.class)
  public @ResponseBody BaseResponse handleApplicationException(ApplicationException ex) {
    return new BaseResponse(BaseResponse.Status.FAILURE, ex.getMessage());
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(ConstraintViolationException.class)
  public @ResponseBody BaseResponse handleConstraintViolationException(
      ConstraintViolationException ex) {
    return new BaseResponse(BaseResponse.Status.FAILURE, ex.getMessage());
  }
}
