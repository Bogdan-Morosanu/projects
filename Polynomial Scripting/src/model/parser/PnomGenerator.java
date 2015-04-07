package model.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.Polynomial;
import controller.Controller;

public class PnomGenerator extends Expression {
	
	public static final String PNOM_OP = "\\s*(\\+|\\-|/|\\*|%)\\s*";
	public static final String PNOM_TERM = "((" + VALID_IDENT + ")|(" + Literal.VALID_PNOM + "))";
	public static final String PNOM_GENERATOR = PNOM_TERM + PNOM_OP + PNOM_TERM;
	public static final Pattern PNOM_TERM_PATTERN = Pattern.compile( PNOM_TERM );
	public static final Pattern OP_PATTERN = Pattern.compile(PNOM_OP);
	
	private String ident;
	private Object val;
	
	public PnomGenerator(String generatorStr) {
		ident = "res" + exprCount;
		exprCount++;

		Polynomial first = getPolynomialTerm(generatorStr, 0);
		Polynomial second = getPolynomialTerm(generatorStr, 1);
		
		//now resolve operand
		Matcher opMatcher = OP_PATTERN.matcher(generatorStr);
		opMatcher.find();
		String opStr = opMatcher.group();
		
		if(opStr.contains("+"))	 {
			val = first.add(second);

		} else if(opStr.contains("-")) {
			val = first.sub(second);

		} else if(opStr.contains("/")) {
			val = first.div(second);
			
		} else if(opStr.contains("*")) {
			val = first.mul(second);
			
		} else if(opStr.contains("%")) {
			val = first.rem(second);
			
		} else {
			val = new SyntaxError("unkown operand " + opStr);
		}
	}
	
	private Polynomial getPolynomialTerm(String str, int index) {
		
		Matcher termMatcher = PNOM_TERM_PATTERN.matcher(str);
		//skip leading terms
		do {
			termMatcher.find();
			index--;
		} while(index >= 0);
			
		
		String pNom = termMatcher.group();
		//check if pNom is litral or identifier
		
		Polynomial p;
		Matcher identMatcher = IDENT_PATTERN.matcher( pNom );
		if(identMatcher.find()) {
			//get from identifier
			p = (Polynomial)Controller.get( identMatcher.group() );
		} else {
			//must instantiate polinomial from string
			p = Literal.parsePolynomial( pNom );
		}
		
		return p;
	}
	
	@Override
	public Object val() {
		// TODO Auto-generated method stub
		return val;
	}

	@Override
	public String ident() {
		// TODO Auto-generated method stub
		return ident;
	}

}
