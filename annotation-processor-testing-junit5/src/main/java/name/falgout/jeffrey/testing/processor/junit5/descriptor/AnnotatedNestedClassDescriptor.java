package name.falgout.jeffrey.testing.processor.junit5.descriptor;

import name.falgout.jeffrey.testing.processor.junit5.AnnotationProcessorContext;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.hierarchical.Node;

final class AnnotatedNestedClassDescriptor
    extends AbstractTestDescriptor implements Node<AnnotationProcessorContext> {
  static AnnotatedNestedClassDescriptor addChild(
      TestDescriptor parent, Class<?> nestedClass) {
    AnnotatedNestedClassDescriptor descriptor =
        new AnnotatedNestedClassDescriptor(
            AnnotatedClassDescriptor.createUniqueId(parent.getUniqueId(), nestedClass),
            nestedClass);

    parent.addChild(descriptor);
    return descriptor;
  }

  private AnnotatedNestedClassDescriptor(UniqueId uniqueId, Class<?> nestedClass) {
    super(uniqueId, nestedClass.getName());
  }

  @Override
  public Type getType() {
    return Type.CONTAINER;
  }
}
