public class dotInParathense {
	public static void main() {
  		@EnsuresCalledMethods(value = { "this.sock" }, methods = { "close" }) String y;
	}
}

  