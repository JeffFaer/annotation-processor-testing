package name.falgout.jeffrey.testing.processor.junit;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

import com.google.auto.common.BasicAnnotationProcessor;
import com.google.auto.common.MoreElements;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
public final class FakeProcessor extends BasicAnnotationProcessor {
  @Retention(SOURCE)
  @Target({TYPE, METHOD, PARAMETER})
  public @interface Warning {
    String value() default "";
  }

  @Retention(SOURCE)
  @Target({TYPE, METHOD, PARAMETER})
  public @interface Error {
    String value() default "";
  }

  public FakeProcessor() {}

  @Override
  protected Iterable<? extends ProcessingStep> initSteps() {
    return Collections.singleton(new ProcessingStep() {
      @Override
      public Set<? extends Class<? extends Annotation>> annotations() {
        return ImmutableSet.of(Error.class, Warning.class);
      }

      @Override
      public Set<Element> process(
          SetMultimap<Class<? extends Annotation>, Element> elementsByAnnotation) {
        printMessages(Error.class, elementsByAnnotation.get(Error.class), Diagnostic.Kind.ERROR,
            Error::value);
        printMessages(Warning.class, elementsByAnnotation.get(Warning.class),
            Diagnostic.Kind.WARNING, Warning::value);

        return Collections.emptySet();
      }

      private <A extends Annotation> void printMessages(Class<A> annotationType,
          Set<? extends Element> elements, Diagnostic.Kind kind,
          Function<? super A, String> value) {
        for (Element element : elements) {
          A annotation = element.getAnnotation(annotationType);
          AnnotationMirror mirror = MoreElements.getAnnotationMirror(element, annotationType).get();
          processingEnv.getMessager().printMessage(kind, value.apply(annotation), element, mirror);
        }
      }
    });
  }
}
