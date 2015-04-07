package model.parser;

import java.util.regex.Matcher;

import model.Polynomial;
import controller.Controller;


public class Calcullus extends Expression {
	
	private String ident;
	private Object val;
	
	public Calcullus(String calcStr) {
		ident = "res" + exprCount;
		exprCount++;
		
		val = Calcullus.parsePolynomial( calcStr );
		
	}
	
	@Override
	public Object val() {
		return val;
	}

	@Override
	public String ident() {
		return ident;
	}

	public static Polynomial parsePolynomial( String expr ) {
		Matcher identMatcher = Expression.IDENT_PATTERN.matcher(expr);
		
		identMatcher.find(); //no need to check, we've already matched pattern before
		String ident = identMatcher.group();
		
		Polynomial base = (Polynomial)Controller.get( ident );
		
		if( expr.contains(".deriv") ) {
			return base.getDerivative();
		} else {		
			return base.getIntegral();
		}
		
	}
}
