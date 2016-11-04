package name.falgout.jeffrey.testing.processor;

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
  private final Map<UUID, Annotation> expectedDiagnostics;
  private final Map<Annotation, ExpectDiagnostic> mapped;

  ExpectDiagnosticProcessor() {
    expectedDiagnostics = new LinkedHashMap<>();
    mapped = new LinkedHashMap<>();
  }

  public List<ExpectedDiagnostic<?>> getExpectedDiagnostics(Compilation compilation) {
    List<ExpectedDiagnostic<?>> expectations = new ArrayList<>();
    for (int i = 0; i < compilation.diagnostics().size(); i++) {
      Diagnostic<? extends JavaFileObject> diagnostic = compilation.diagnostics().get(i);
      if (diagnostic.getKind() != DIAGNOSTIC_KIND) {
        continue;
      }

      try {
        UUID id = UUID.fromString(diagnostic.getMessage(null));

        Annotation originalAnnotation = expectedDiagnostics.get(id);
        ExpectDiagnostic expectDiagnostic = mapped.get(originalAnnotation);
        JavaFileObject originalSource = diagnostic.getSource();
        long originalLineNumber = diagnostic.getLineNumber();

        String testName = expectDiagnostic.testName();
        if (testName.isEmpty()) {
          testName = getDefaultTestName(expectDiagnostic.value(), i + 1, originalLineNumber);
        }

        expectations.add(new AutoValue_ExpectedDiagnostic<>(expectDiagnostic, originalAnnotation,
            originalSource, originalLineNumber));
      } catch (IllegalArgumentException e) {}
    }

    return expectations;
  }

  private String getDefaultTestName(Diagnostic.Kind kind, int index, long lineNumber) {
    return String.format("%s#%d@%d", kind, index, lineNumber);
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
      process(ExpectDiagnostic.class, elementsByAnnotation.get(ExpectDiagnostic.class),
          Function.identity());
      process(ExpectError.class, elementsByAnnotation.get(ExpectError.class),
          ExpectedDiagnostics::createExpectDiagnostic);

      return Collections.emptySet();
    }

    private <A extends Annotation> void process(Class<A> annotationType,
        Set<Element> annotatedElements, Function<? super A, ExpectDiagnostic> mapper) {
      for (Element element : annotatedElements) {
        A annotation = element.getAnnotation(annotationType);
        AnnotationMirror mirror = MoreElements.getAnnotationMirror(element, annotationType).get();

        UUID id = UUID.randomUUID();
        expectedDiagnostics.put(id, annotation);
        mapped.put(annotation, mapper.apply(annotation));
        processingEnv.getMessager().printMessage(DIAGNOSTIC_KIND, id.toString(), element, mirror);
      }
    }
  }
}
