package name.falgout.jeffrey.testing.processor;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.*;

import java.util.List;

import javax.tools.Diagnostic;

import org.junit.Test;

import com.google.testing.compile.JavaFileObjects;

import name.falgout.jeffrey.testing.processor.ExpectedDiagnostic;
import name.falgout.jeffrey.testing.processor.ExpectedDiagnostics;

public class ExpectDiagnosticProcessorTest {

  @Test
  public void expectations() {
    List<ExpectedDiagnostic> expected = ExpectedDiagnostics
        .getExpectedDiagnostics(JavaFileObjects.forResource("expect/Expectations.java"));
    assertThat(expected).hasSize(1);

    ExpectedDiagnostic diagnostic = expected.get(0);
    assertThat(diagnostic.getKind()).isSameAs(Diagnostic.Kind.ERROR);
    assertThat(diagnostic.getLineNumber()).isEqualTo(9);
    assertTrue(diagnostic.getMessageMatcher().matcher("foo").matches());
  }
}
