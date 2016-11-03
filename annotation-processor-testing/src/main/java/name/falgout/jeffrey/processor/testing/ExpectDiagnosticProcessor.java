package name.falgout.jeffrey.processor.testing;

import static java.util.function.Function.identity;
import static javax.lang.model.SourceVersion.RELEASE_8;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import com.google.auto.common.BasicAnnotationProcessor;
import com.google.auto.common.MoreElements;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;
import com.google.testing.compile.Compilation;

@SupportedSourceVersion(RELEASE_8)
final class ExpectDiagnosticProcessor extends BasicAnnotationProcessor {
  private static final Diagnostic.Kind DIAGNOSTIC_KIND = Diagnostic.Kind.NOTE;
  private final Map<UUID, ExpectDiagnostic> expectedDiagnostics;

  ExpectDiagnosticProcessor() {
    expectedDiagnostics = new LinkedHashMap<>();
  }

  public List<ExpectedDiagnostic> getExpectedDiagnostics(Compilation compilation) {
    List<ExpectedDiagnostic> expectations = new ArrayList<>();
    for (int i = 0; i < compilation.diagnostics().size(); i++) {
      Diagnostic<? extends JavaFileObject> diagnostic = compilation.diagnostics().get(i);
      if (diagnostic.getKind() != DIAGNOSTIC_KIND) {
        continue;
      }

      try {
        UUID id = UUID.fromString(diagnostic.getMessage(null));
        ExpectDiagnostic expected = expectedDiagnostics.get(id);

        Pattern message = Pattern
            .compile(expected.regex() ? expected.message() : Pattern.quote(expected.message()));
        JavaFileObject source = diagnostic.getSource();
        long actualLine = diagnostic.getLineNumber() + expected.lineOffset();
        String testName = expected.testName();
        if (testName.isEmpty()) {
          testName = String.format("%s#%d@%d", expected.value(), i + 1, diagnostic.getLineNumber());
        }

        expectations.add(new AutoValue_ExpectedDiagnostic(expected.value(), message, source,
            actualLine, testName));
      } catch (IllegalArgumentException e) {}
    }

    return expectations;
  }

  @Override
  protected Iterable<? extends ProcessingStep> initSteps() {
    return Collections.singleton(new ExpectDiagnosticProcessingStep());
  }

  private final class ExpectDiagnosticProcessingStep implements ProcessingStep {
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
        UUID id = UUID.randomUUID();

        processingEnv.getMessager().printMessage(DIAGNOSTIC_KIND, id.toString(), element, mirror);
        expectedDiagnostics.put(id, diagnostic);
      }
    }
  }
}
