package name.falgout.jeffrey.testing.processor.junit5.descriptor;

import static name.falgout.jeffrey.testing.processor.ActualDiagnostics.getSourceFile;
import static name.falgout.jeffrey.testing.processor.ExpectedDiagnostics.getExpectedDiagnostics;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import name.falgout.jeffrey.testing.processor.ActualDiagnostics;
import name.falgout.jeffrey.testing.processor.ExpectedDiagnostic;
import name.falgout.jeffrey.testing.processor.junit5.AnnotationProcessorContext;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.hierarchical.Node;

public final class AnnotatedClassDescriptor
    extends AbstractTestDescriptor implements Node<AnnotationProcessorContext> {
  public static AnnotatedClassDescriptor addChild(TestDescriptor parent, Class<?> clazz)
      throws ClassNotFoundException {
    UniqueId uniqueId = createUniqueId(parent.getUniqueId(), clazz);
    AnnotatedClassDescriptor rootDescriptor = new AnnotatedClassDescriptor(uniqueId, clazz);

    Map<Class<?>, TestDescriptor> testDescriptors = new HashMap<>();
    testDescriptors.put(clazz, rootDescriptor);

    List<ExpectedDiagnostic<?>> expectedDiagnostics = getExpectedDiagnostics(getSourceFile(clazz));
    for (ExpectedDiagnostic<?> expectedDiagnostic : expectedDiagnostics) {
      TestDescriptor descriptor =
          createTestDescriptor(expectedDiagnostic.getEnclosingClass(), testDescriptors);
      ExpectedDiagnosticDescriptor.addChild(descriptor, expectedDiagnostic);
    }

    parent.addChild(rootDescriptor);

    return rootDescriptor;
  }

  static UniqueId createUniqueId(UniqueId parentId, Class<?> clazz) {
    return parentId.append("class", clazz.getName());
  }

  private static TestDescriptor createTestDescriptor(
      Class<?> clazz, Map<Class<?>, TestDescriptor> testDescriptors) {
    if (testDescriptors.containsKey(clazz)) {
      return testDescriptors.get(clazz);
    }
    
    TestDescriptor parent = createTestDescriptor(clazz.getEnclosingClass(), testDescriptors);
    TestDescriptor child = AnnotatedNestedClassDescriptor.addChild(parent, clazz);
    testDescriptors.put(clazz, child);

    return child;
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
