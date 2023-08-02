public class MultiLineAnnotation {
	public static void main() {
		@EnsuresCalledMethods(
        		value = "this",
        		methods = "close"
		)       int x;
	}
}


