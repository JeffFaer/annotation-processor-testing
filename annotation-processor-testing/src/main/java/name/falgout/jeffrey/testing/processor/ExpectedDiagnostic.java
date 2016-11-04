package name.falgout.jeffrey.testing.processor;

import static com.google.testing.compile.CompilationSubject.assertThat;

import java.util.regex.Pattern;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import com.google.auto.value.AutoValue;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.CompilationSubject.DiagnosticInFile;

@AutoValue
public abstract class ExpectedDiagnostic {
  protected ExpectedDiagnostic() {}

  public abstract Diagnostic.Kind getKind();

  public abstract Pattern getMessageMatcher();

  public abstract JavaFileObject getSource();

  public abstract long getLineNumber();

  public abstract String getTestName();

  public final void checkCompilation(Compilation compilation) {
    DiagnosticInFile inFile;
    switch (getKind()) {
      case ERROR:
        inFile = assertThat(compilation).hadErrorContainingMatch(getMessageMatcher());
        break;
      case WARNING:
        inFile = assertThat(compilation).hadWarningContainingMatch(getMessageMatcher());
        break;
      case NOTE:
        inFile = assertThat(compilation).hadNoteContainingMatch(getMessageMatcher());
        break;
      default:
        throw new UnsupportedOperationException(getKind().toString());
    }

    inFile.inFile(getSource()).onLine(getLineNumber());
  }
}
