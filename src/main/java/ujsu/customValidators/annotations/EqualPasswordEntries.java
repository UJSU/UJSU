package ujsu.customValidators.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ujsu.customValidators.validators.EqualPasswordEntriesValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EqualPasswordEntriesValidator.class)
public @interface EqualPasswordEntries {

	String message() default "user.password.doesNotMatch";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}