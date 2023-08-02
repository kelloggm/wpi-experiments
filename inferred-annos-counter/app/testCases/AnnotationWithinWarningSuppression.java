import java.lang.SuppressWarnings;
import org.checkerframework.checker.nullness.qual.NonNull;

public class AnnotationWithinWarningSuppression {
	public static void main() {
		@SuppressWarnings("nullness")
 		@NonNull int x;
	}
}

