package com.suppresswarnings.osgi.nn.fsm;

public interface S extends State {
	S S0 = new S() {
		@Override
		public void accept(String in, Context context) {
			System.out.println("输入'login'登录");
		}

		@Override
		public S to(String in, Context context) {
			if ("login".equals(in))
				return S1;
			return S0;
		}
	};
	S S1 = new S() {

		@Override
		public void accept(String in, Context context) {
			System.out.println("Username: ");
		}

		@Override
		public S to(String in, Context context) {
			if (in.length() > 5)
				return S2;
			return S1F;
		}
	};
	S S1F = new S() {
		int tried = 0;

		@Override
		public void accept(String in, Context context) {
			System.out.println("Try again(" + (3 - tried) + "): ");
		}

		@Override
		public S to(String in, Context context) {
			if (in.length() > 5)
				return S2;
			if (tried > 1) {
				tried = 0;
				return S0;
			}
			tried++;
			return S1F;
		}
	};
	S S2 = new S() {
		@Override
		public void accept(String in, Context context) {
			context.username = in;
			System.out.println("Passcode: ");
		}

		@Override
		public S to(String in, Context context) {
			String auth = "passcode" + context.username;
			if (auth.equals(in)) {
				return Final;
			}
			return S2F;
		}
	};
	S S2F = new S() {
		int tried = 0;

		@Override
		public void accept(String in, Context context) {
			System.out.println("Try again(" + (3 - tried) + "): ");
		}

		@Override
		public S to(String in, Context context) {
			String auth = "passcode" + context.username;
			if (auth.equals(in)) {
				return Final;
			}
			if (tried > 1) {
				tried = 0;
				return S0;
			}
			tried++;
			return S2F;
		}
	};
	S Final = new S() {
		public String toString(){return "Final";}
		boolean authorized = false;

		@Override
		public void accept(String in, Context context) {
			if (!authorized) {
				context.passcode = in;
				authorized = true;
			}
			System.out.println(context.username + ": Authorized");
		}

		@Override
		public S to(String in, Context context) {
			if (authorized) {
				throw new RuntimeException("stop try");
			}
			return Final;
		}
	};
}