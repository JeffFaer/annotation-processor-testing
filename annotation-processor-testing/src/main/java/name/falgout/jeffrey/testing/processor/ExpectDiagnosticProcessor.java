package name.falgout.jeffrey.testing.processor;

import static javax.lang.model.SourceVersion.RELEASE_8;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import javax.annotation.Nullable;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import com.google.auto.common.BasicAnnotationProcessor;
import com.google.auto.common.MoreElements;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;
import com.google.testing.compile.Compilation;

@SupportedSourceVersion(RELEASE_8)
final class ExpectDiagnosticProcessor extends BasicAnnotationProcessor {
  private static final Diagnostic.Kind DIAGNOSTIC_KIND = Diagnostic.Kind.NOTE;
  private final Map<UUID, DiscoveredAnnotation> expectedDiagnostics;

  ExpectDiagnosticProcessor() {
    expectedDiagnostics = new LinkedHashMap<>();
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

        DiscoveredAnnotation discovered = expectedDiagnostics.get(id);
        ExpectDiagnostic expectDiagnostic = discovered.getExpectDiagnostic();
        JavaFileObject originalSource = diagnostic.getSource();
        long originalLineNumber = diagnostic.getLineNumber();

        String testName = expectDiagnostic.testName();
        if (testName.isEmpty()) {
          testName = getDefaultTestName(expectDiagnostic.value(), i + 1, originalLineNumber);
          expectDiagnostic = ExpectedDiagnostics.replaceTestName(expectDiagnostic, testName);
        }

        expectations.add(
            new AutoValue_ExpectedDiagnostic<>(expectDiagnostic, discovered.getOriginalAnnotation(),
                originalSource, originalLineNumber, discovered.getEnclosingClassName()));
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

        Optional<TypeElement> enclosingTypeElement = findEnclosingTypeElement(element);
        @Nullable String binaryName = enclosingTypeElement.map(this::getBinaryName).orElse(null);

        UUID id = UUID.randomUUID();
        expectedDiagnostics.put(id, DiscoveredAnnotation.create(annotation, mapper, binaryName));
        processingEnv.getMessager().printMessage(DIAGNOSTIC_KIND, id.toString(), element, mirror);
      }
    }

    private Optional<TypeElement> findEnclosingTypeElement(Element element) {
      if (element == null) {
        return Optional.empty();
      }

      if (element instanceof TypeElement && ((TypeElement) element).getQualifiedName() != null) {
        return Optional.of((TypeElement) element);
      }

      return findEnclosingTypeElement(element.getEnclosingElement());
    }

    private String getBinaryName(TypeElement typeElement) {
      return findEnclosingTypeElement(typeElement.getEnclosingElement())
          .map(type -> getBinaryName(type) + "$" + typeElement.getSimpleName().toString())
          .orElseGet(() -> typeElement.getQualifiedName().toString());
    }
  }

  @AutoValue
  abstract static class DiscoveredAnnotation {
    public static <A extends Annotation> DiscoveredAnnotation create(A originalAnnotation,
        Function<? super A, ExpectDiagnostic> mapper, @Nullable String enclosingClassName) {
      return new AutoValue_ExpectDiagnosticProcessor_DiscoveredAnnotation(originalAnnotation,
          mapper.apply(originalAnnotation), enclosingClassName);
    }

    abstract Annotation getOriginalAnnotation();

    abstract ExpectDiagnostic getExpectDiagnostic();

    abstract @Nullable String getEnclosingClassName();
  }
}
