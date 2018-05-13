package name.falgout.jeffrey.testing.processor.junit5;

import static org.junit.platform.commons.util.ReflectionUtils.findAllClassesInClasspathRoot;
import static org.junit.platform.commons.util.ReflectionUtils.findAllClassesInPackage;
import static org.junit.platform.engine.support.filter.ClasspathScanningSupport.buildClassFilter;

import com.google.auto.service.AutoService;
import com.google.common.collect.Streams;
import java.util.Collection;
import name.falgout.jeffrey.testing.processor.UseProcessor;
import name.falgout.jeffrey.testing.processor.junit5.descriptor.AnnotatedClassDescriptor;
import name.falgout.jeffrey.testing.processor.junit5.descriptor.AnnotationProcessorTestEngineDescriptor;
import org.junit.platform.commons.util.ClassFilter;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine;

@AutoService(TestEngine.class)
public final class AnnotationProcessorTestEngine extends
    HierarchicalTestEngine<AnnotationProcessorContext> {
  @Override
  public String getId() {
    return getClass().getName();
  }

  @Override
  public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
    TestDescriptor rootDescriptor = new AnnotationProcessorTestEngineDescriptor(uniqueId);

    ClassFilter filter =
        buildClassFilter(
            discoveryRequest,
            clazz -> clazz.getAnnotationsByType(UseProcessor.class).length > 0);

    Streams.concat(
        discoveryRequest.getSelectorsByType(ClasspathRootSelector.class)
            .stream()
            .map(classpath -> findAllClassesInClasspathRoot(classpath.getClasspathRoot(), filter))
            .flatMap(Collection::stream),
        discoveryRequest.getSelectorsByType(PackageSelector.class)
            .stream()
            .map(pack -> findAllClassesInPackage(pack.getPackageName(), filter))
            .flatMap(Collection::stream),
        discoveryRequest.getSelectorsByType(ClassSelector.class)
            .stream()
            .map(ClassSelector::getJavaClass))
        .filter(filter)
        .forEach(
            clazz ->
                rootDescriptor.addChild(AnnotatedClassDescriptor.create(rootDescriptor, clazz)));

    return rootDescriptor;
  }

  @Override
  protected AnnotationProcessorContext createExecutionContext(ExecutionRequest request) {
    return new AnnotationProcessorContext();
  }
}
