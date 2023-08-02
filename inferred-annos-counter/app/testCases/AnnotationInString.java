public class AnnotationInString {
	public static void main() {
 		@org.checkerframework.dataflow.qual.Pure int x;
 		//some methods here...//
 		String y = "@NonNull";
 		@org.checkerframework.dataflow.qual.SideEffectFree int z;
	}

} 

