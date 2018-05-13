package name.falgout.jeffrey.testing.processor;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.annotation.processing.Processor;
import org.junit.platform.commons.annotation.Testable;

@Documented
@Retention(RUNTIME)
@Target(TYPE)
@Repeatable(UseProcessors.class)
@Testable
public @interface UseProcessor {
  Class<? extends Processor>[] value();
}
