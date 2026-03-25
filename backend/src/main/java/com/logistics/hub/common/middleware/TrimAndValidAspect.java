package com.logistics.hub.common.middleware;

import com.logistics.hub.common.middleware.annotation.TrimAndValid;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Set;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class TrimAndValidAspect {

  private final Validator validator;

  @Before("@within(com.logistics.hub.common.middleware.annotation.TrimAndValid) || @annotation(com.logistics.hub.common.middleware.annotation.TrimAndValid)")
  public void beforeMethod(JoinPoint joinPoint) throws IllegalAccessException {
    Object[] args = joinPoint.getArgs();
    for (Object arg : args) {
      if (arg != null && !isPrimitiveOrWrapper(arg.getClass())) {
        trimStrings(arg);
        validate(arg);
      }
    }
  }

  private boolean isPrimitiveOrWrapper(Class<?> type) {
    return type.isPrimitive() ||
        type.getCanonicalName().startsWith("java.lang") ||
        type.getCanonicalName().startsWith("java.util");
  }

  private void trimStrings(Object obj) throws IllegalAccessException {
    if (obj == null)
      return;

    Class<?> clazz = obj.getClass();
    while (clazz != null && !clazz.equals(Object.class)) {
      Field[] fields = clazz.getDeclaredFields();
      for (Field field : fields) {
        if (field.getType().equals(String.class)) {
          field.setAccessible(true);
          String value = (String) field.get(obj);
          if (value != null) {
            field.set(obj, value.trim());
          }
        }
      }
      clazz = clazz.getSuperclass();
    }
  }

  private void validate(Object obj) {
    Set<ConstraintViolation<Object>> violations = validator.validate(obj);
    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }
  }
}
