public class AnnotationWithArgument {
	public static void main() {
		@EnsuresNonNull("tz") int x;
		@EnsuresCalledMethods(value="this", methods="close") int y;
		@EnsuresNonNull("tz") int z;
		@EnsuresCalledMethods(value="this", methods="close") int a;
		@StringVal("},{") int b;
	}
}



