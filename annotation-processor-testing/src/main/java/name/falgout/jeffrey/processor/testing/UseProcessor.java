package name.falgout.jeffrey.processor.testing;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.annotation.processing.Processor;

@Documented
@Retention(RUNTIME)
@Target(TYPE)
@Repeatable(UseProcessors.class)
public @interface UseProcessor {
  Class<? extends Processor>[] value();
}
