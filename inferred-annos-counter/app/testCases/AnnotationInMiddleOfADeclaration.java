public class AnnotationInMiddleOfADeclaration {

	public static void main() {
 		@org.checkerframework.dataflow.qual.Pure int x;
 		/* some methods here...*/
 		ArrayList<@NonNull CalendarComponent> clist = calendar.getComponents();

 		@org.checkerframework.dataflow.qual.SideEffectFree String z;
	}
}

