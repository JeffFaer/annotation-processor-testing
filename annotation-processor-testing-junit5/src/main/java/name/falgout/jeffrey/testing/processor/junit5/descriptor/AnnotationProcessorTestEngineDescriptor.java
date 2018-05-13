package name.falgout.jeffrey.testing.processor.junit5.descriptor;

import name.falgout.jeffrey.testing.processor.junit5.AnnotationProcessorContext;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.engine.support.hierarchical.Node;

public final class AnnotationProcessorTestEngineDescriptor
    extends EngineDescriptor implements Node<AnnotationProcessorContext> {
  public AnnotationProcessorTestEngineDescriptor(UniqueId uniqueId) {
    super(uniqueId, "Annotation Processor Testing");
  }
}
