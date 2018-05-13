package name.falgout.jeffrey.testing.processor.junit5;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import org.junit.platform.engine.support.hierarchical.EngineExecutionContext;

public final class AnnotationProcessorContext implements EngineExecutionContext {
  private final Queue<Diagnostic<? extends JavaFileObject>> actualDiagnostics;

  public AnnotationProcessorContext() {
    this(Collections.emptySet());
  }

  public AnnotationProcessorContext(
      Collection<? extends Diagnostic<? extends JavaFileObject>> actualDiagnostics) {
    this.actualDiagnostics = new LinkedList<>(actualDiagnostics);
  }

  public Queue<Diagnostic<? extends JavaFileObject>> getActualDiagnostics() {
    return actualDiagnostics;
  }
}
