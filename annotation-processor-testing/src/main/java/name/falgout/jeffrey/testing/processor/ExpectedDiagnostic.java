package name.falgout.jeffrey.testing.processor;

import static name.falgout.jeffrey.testing.processor.DiagnosticMatchers.hasMessage;
import static name.falgout.jeffrey.testing.processor.DiagnosticMatchers.hasSource;
import static name.falgout.jeffrey.testing.processor.DiagnosticMatchers.is;
import static name.falgout.jeffrey.testing.processor.DiagnosticMatchers.isOnLine;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.describedAs;

import java.lang.annotation.Annotation;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import org.hamcrest.Matcher;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ExpectedDiagnostic<A extends Annotation> {
  public abstract ExpectDiagnostic getExpectDiagnostic();

  public abstract A getOriginalAnnotation();

  public abstract JavaFileObject getOriginalSource();

  public abstract long getOriginalLineNumber();

  public abstract @Nullable String getEnclosingClassName();

  public final Class<?> getEnclosingClass() throws ClassNotFoundException {
    return Class.forName(getEnclosingClassName());
  }

  public final long getExpectedLineNumber() {
    return getOriginalLineNumber() + getExpectDiagnostic().lineOffset();
  }

  public final Matcher<? super Diagnostic<? extends JavaFileObject>> asMatcher() {
    Matcher<? super Diagnostic<? extends JavaFileObject>> messageMatcher;
    if (getExpectDiagnostic().regex()) {
      messageMatcher = hasMessage(Pattern.compile(getExpectDiagnostic().message()));
    } else {
      messageMatcher = hasMessage(getExpectDiagnostic().message());
    }
    Matcher<? super Diagnostic<? extends JavaFileObject>> matcher =
        allOf(hasSource(getOriginalSource()), isOnLine(getExpectedLineNumber()), messageMatcher,
            is(getExpectDiagnostic().value()));

    return describedAs(String.format("%s:%d\n%s", getOriginalSource().getName(),
        getOriginalLineNumber(), getOriginalAnnotation()), matcher);
  }
}
