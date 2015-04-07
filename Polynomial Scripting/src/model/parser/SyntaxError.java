package model.parser;

public class SyntaxError extends Expression {
	
	private String val;
	
	public SyntaxError(String msg) {
		this.val = msg;
	}
	
	@Override
	public Object val() {
		return val;
	}

	@Override
	public String ident() {
		// TODO Auto-generated method stub
		return "annonymous";
	}
	
	@Override 
	public String toString() {
		return val;
	}
}
