import org.checkerframework.checker.nullness.qual.NonNull;

public class AnnotationNotSurroundedBySpace{
	public static void main() {
		int@NonNull[] x;
		long@NonNull[] y;
		String@NonNull[] z;
	}
}
 
