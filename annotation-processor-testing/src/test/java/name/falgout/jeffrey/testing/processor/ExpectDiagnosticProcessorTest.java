package name.falgout.jeffrey.testing.processor;

import static com.google.common.truth.Truth.assertThat;

import com.google.testing.compile.JavaFileObjects;
import java.util.List;
import javax.tools.Diagnostic;
import org.junit.Test;

public class ExpectDiagnosticProcessorTest {
  @Test
  public void expectations() {
    List<ExpectedDiagnostic<?>> expected = ExpectedDiagnostics
        .getExpectedDiagnostics(JavaFileObjects.forResource("expect/Expectations.java"));
    assertThat(expected).hasSize(1);

    ExpectedDiagnostic<?> diagnostic = expected.get(0);
    assertThat(diagnostic.getExpectDiagnostic().value()).isSameAs(Diagnostic.Kind.ERROR);
    assertThat(diagnostic.getExpectedLineNumber()).isEqualTo(9);
    assertThat(diagnostic.getOriginalLineNumber()).isEqualTo(4);
    assertThat(diagnostic.getExpectDiagnostic().message()).isEqualTo("foo");
  }
}
