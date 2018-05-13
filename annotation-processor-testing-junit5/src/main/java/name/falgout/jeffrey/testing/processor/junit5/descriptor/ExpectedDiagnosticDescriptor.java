package name.falgout.jeffrey.testing.processor.junit5.descriptor;

import static org.hamcrest.MatcherAssert.assertThat;

import name.falgout.jeffrey.testing.processor.ExpectedDiagnostic;
import name.falgout.jeffrey.testing.processor.junit5.AnnotationProcessorContext;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.FilePosition;
import org.junit.platform.engine.support.hierarchical.Node;

final class ExpectedDiagnosticDescriptor
    extends AbstractTestDescriptor implements Node<AnnotationProcessorContext> {
  static ExpectedDiagnosticDescriptor create(
      TestDescriptor parent,
      ExpectedDiagnostic<?> expectedDiagnostic) {
    UniqueId uniqueId = createUniqueId(parent.getUniqueId(), expectedDiagnostic);
    ExpectedDiagnosticDescriptor descriptor =
        new ExpectedDiagnosticDescriptor(uniqueId, expectedDiagnostic);

    return descriptor;
  }

  private static UniqueId createUniqueId(
      UniqueId parentId, ExpectedDiagnostic<?> expectedDiagnostic) {
    String pattern;
    if (expectedDiagnostic.getExpectDiagnostic().regex()) {
      pattern = "/" + expectedDiagnostic.getExpectDiagnostic().message() + "/";
    } else {
      pattern = expectedDiagnostic.getExpectDiagnostic().message();
    }

    return parentId.append(
        "diagnostic",
        String.format(
            "%s@%d:%s",
            expectedDiagnostic.getExpectDiagnostic().value(),
            expectedDiagnostic.getExpectedLineNumber(),
            pattern));
  }

  private final ExpectedDiagnostic<?> expectedDiagnostic;

  private ExpectedDiagnosticDescriptor(
      UniqueId uniqueId, ExpectedDiagnostic<?> expectedDiagnostic) {
    super(uniqueId,
        expectedDiagnostic.getExpectDiagnostic().testName(),
        createTestSource(expectedDiagnostic));

    this.expectedDiagnostic = expectedDiagnostic;
  }

  private static TestSource createTestSource(ExpectedDiagnostic<?> expectedDiagnostic) {
    return ClassSource.from(
        expectedDiagnostic.getEnclosingClassName(),
        FilePosition.from((int) expectedDiagnostic.getOriginalLineNumber()));
  }

  @Override
  public Type getType() {
    return Type.TEST;
  }

  @Override
  public AnnotationProcessorContext execute(
      AnnotationProcessorContext context,
      DynamicTestExecutor dynamicTestExecutor) {
    assertThat(context.getActualDiagnostics().poll(), expectedDiagnostic.asMatcher());
    return context;
  }
}
