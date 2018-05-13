package name.falgout.jeffrey.testing.processor;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.junit.platform.commons.annotation.Testable;

@Documented
@Retention(RUNTIME)
@Target(TYPE)
@Testable
public @interface UseProcessors {
  UseProcessor[] value();
}
