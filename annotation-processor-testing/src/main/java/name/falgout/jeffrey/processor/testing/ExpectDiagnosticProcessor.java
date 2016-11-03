package name.falgout.jeffrey.processor.testing;

import static java.util.function.Function.identity;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import com.google.auto.common.BasicAnnotationProcessor;
import com.google.auto.common.MoreElements;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;
import com.google.testing.compile.Compilation;

final class ExpectDiagnosticProcessor extends BasicAnnotationProcessor {
  private final Map<Integer, ExpectDiagnostic> expectedDiagnostics;

  ExpectDiagnosticProcessor() {
    expectedDiagnostics = new LinkedHashMap<>();
  }

  public List<ExpectedDiagnostic> getExpectedDiagnostics(Compilation compilation) {
    List<ExpectedDiagnostic> expectations = new ArrayList<>();
    for (Diagnostic<? extends JavaFileObject> diagnostic : compilation.diagnostics()) {
      try {
        int id = Integer.parseInt(diagnostic.getMessage(null));
        ExpectDiagnostic expected = expectedDiagnostics.get(id);

        String message = expected.regex() ? Pattern.quote(expected.message()) : expected.message();
        JavaFileObject source = diagnostic.getSource();
        long actualLine = diagnostic.getLineNumber() + expected.lineOffset();
        expectations.add(new AutoValue_ExpectedDiagnostic(expected.kind(), Pattern.compile(message),
            source, actualLine));
      } catch (NumberFormatException e) {}
    }

    return expectations;
  }

  @Override
  protected Iterable<? extends ProcessingStep> initSteps() {
    return Collections.singleton(new ExpectDiagnosticProcessingStep());
  }

  private final class ExpectDiagnosticProcessingStep implements ProcessingStep {
    private int nextId = 1;

    private ExpectDiagnosticProcessingStep() {}

    @Override
    public Set<? extends Class<? extends Annotation>> annotations() {
      return ImmutableSet.of(ExpectDiagnostic.class, ExpectError.class);
    }

    @Override
    public Set<Element> process(
        SetMultimap<Class<? extends Annotation>, Element> elementsByAnnotation) {
      process(ExpectDiagnostic.class, elementsByAnnotation.get(ExpectDiagnostic.class), identity());
      process(ExpectError.class, elementsByAnnotation.get(ExpectError.class),
          ExpectedDiagnostics::createExpectDiagnostic);

      return Collections.emptySet();
    }

    private <A extends Annotation> void process(Class<A> annotationType,
        Set<Element> annotatedElements,
        Function<? super A, ExpectDiagnostic> diagnosticMapper) {
      for (Element element : annotatedElements) {
        A annotation = element.getAnnotation(annotationType);
        AnnotationMirror mirror = MoreElements.getAnnotationMirror(element, annotationType).get();

        ExpectDiagnostic diagnostic = diagnosticMapper.apply(annotation);
        int id = getNextId();

        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, String.valueOf(id), element,
            mirror);
        expectedDiagnostics.put(id, diagnostic);
      }
    }

    private int getNextId() {
      int id = nextId;
      nextId++;
      return id;
    }
  }
}
