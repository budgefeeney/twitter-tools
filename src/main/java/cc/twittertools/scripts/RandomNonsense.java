package cc.twittertools.scripts;

public class RandomNonsense {

	
	public static interface MyInterface {
		public int someFunction();
	}
	
	public static class MyImpl implements MyInterface {
		private final int a;
		
		public MyImpl(int a) {
			this.a = a;
		}

		public int someFunction() {
			return a + 2;
		}
	}
	
	public static MyInterface genericFunc (int b) {
		return new MyImpl(b);
	}
	
	public static void callGenericFunc () {
		System.out.println (genericFunc(42));
	}
}
