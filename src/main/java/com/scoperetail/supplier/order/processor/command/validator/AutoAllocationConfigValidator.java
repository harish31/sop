package com.scoperetail.supplier.order.processor.command.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.stereotype.Component;

import com.scoperetail.supplier.order.processor.query.model.AutoAllocationConfigCreateRequest;
import com.scoperetail.supplier.order.processor.query.model.AutoAllocationConfigUpdateRequest;

@Component
public class AutoAllocationConfigValidator
    implements ConstraintValidator<AutoAllocationConfigValid, Object> {

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    Boolean isValid = Boolean.TRUE;
    if (value instanceof AutoAllocationConfigCreateRequest) {
      AutoAllocationConfigCreateRequest autoAllocationConfigCreateRequest =
          (AutoAllocationConfigCreateRequest) value;
      if ((autoAllocationConfigCreateRequest.getReleaseOffset() == null
              && autoAllocationConfigCreateRequest.getCutoffOffset() == null)
          || (autoAllocationConfigCreateRequest.getReleaseOffset() != null
              && autoAllocationConfigCreateRequest.getCutoffOffset() != null)) {
        context
            .buildConstraintViolationWithTemplate(
                "Either Release Offset or Cutoffset is mandatory!")
            .addConstraintViolation();
        isValid = Boolean.FALSE;
      }
    }
    if (value instanceof AutoAllocationConfigUpdateRequest) {
      AutoAllocationConfigUpdateRequest autoAllocationConfigUpdateRequest =
          (AutoAllocationConfigUpdateRequest) value;
      if ((autoAllocationConfigUpdateRequest.getReleaseOffset() == null
              && autoAllocationConfigUpdateRequest.getCutoffOffset() == null)
          || (autoAllocationConfigUpdateRequest.getReleaseOffset() != null
              && autoAllocationConfigUpdateRequest.getCutoffOffset() != null)) {
        context
            .buildConstraintViolationWithTemplate(
                "Either Release Offset or Cutoffset is mandatory!")
            .addConstraintViolation();
        isValid = Boolean.FALSE;
      }
    }
    return isValid;
  }
}
