package name.falgout.jeffrey.testing.processor.junit5.descriptor;

import static name.falgout.jeffrey.testing.processor.ActualDiagnostics.getSourceFile;
import static name.falgout.jeffrey.testing.processor.ExpectedDiagnostics.getExpectedDiagnostics;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import name.falgout.jeffrey.testing.processor.ActualDiagnostics;
import name.falgout.jeffrey.testing.processor.ExpectedDiagnostic;
import name.falgout.jeffrey.testing.processor.junit5.AnnotationProcessorContext;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.hierarchical.Node;

public final class AnnotatedClassDescriptor
    extends AbstractTestDescriptor implements Node<AnnotationProcessorContext> {
  public static AnnotatedClassDescriptor create(TestDescriptor parent, Class<?> clazz) {
    UniqueId uniqueId = createUniqueId(parent.getUniqueId(), clazz);
    AnnotatedClassDescriptor descriptor = new AnnotatedClassDescriptor(uniqueId, clazz);

    List<ExpectedDiagnostic<?>> expectedDiagnostics = getExpectedDiagnostics(getSourceFile(clazz));
    for (ExpectedDiagnostic<?> expectedDiagnostic : expectedDiagnostics) {
      descriptor.addChild(ExpectedDiagnosticDescriptor.create(descriptor, expectedDiagnostic));
    }

    return descriptor;
  }

  private static UniqueId createUniqueId(UniqueId parentId, Class<?> clazz) {
    return parentId.append("class", clazz.getName());
  }

  private final Class<?> clazz;

  private AnnotatedClassDescriptor(UniqueId uniqueId, Class<?> clazz) {
    super(uniqueId, clazz.getName());

    this.clazz = clazz;
  }

  @Override
  public Type getType() {
    return Type.CONTAINER;
  }

  @Override
  public AnnotationProcessorContext prepare(AnnotationProcessorContext context) throws Exception {
    return new AnnotationProcessorContext(ActualDiagnostics.getActualDiagnostics(clazz));
  }

  @Override
  public void after(AnnotationProcessorContext context) {
    assertTrue(
        context.getActualDiagnostics().isEmpty(),
        () -> "Extra diagnostics: " + context.getActualDiagnostics());
  }
}
