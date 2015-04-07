package model.parser;

import java.util.regex.Matcher;

import controller.Controller;

public class PnomInstance extends Expression {

	public static final String PNOM_INSTANTIATION = VALID_IDENT + "\\s*=\\s*(("  + Literal.VALID_PNOM + ")|(" 
																			+ Calcullus.VALID_CALC_OP + ")|(" 
																			+ VALID_IDENT + "))";  
	
	private String ident;
	private Object val;
	
	public PnomInstance( String instMatch ) {
		
		Matcher matchIdent = IDENT_PATTERN.matcher(instMatch);
		matchIdent.find();
		this.ident = matchIdent.group();
		
		Matcher checkWords = RESERVED_WORDS_PATTERN.matcher( ident );
		if( checkWords.find() ) {
			//check for reserved words being used
			ident = "invalid";
			val = new SyntaxError( checkWords.group() + " is a reserved identifier");
		} else {
			
			Matcher matchLiteral = PNOM_PATTERN.matcher(instMatch);
			Matcher matchCalcullus = Calcullus.CALC_PATTERN.matcher( instMatch );
			
			if( matchLiteral.find() ) {
	
				val = Literal.parsePolynomial( matchLiteral.group() );
				
			} else if( matchCalcullus.find() ) {
	
				val = Calcullus.parsePolynomial( matchCalcullus.group() );
			
			} else if( matchIdent.find() ) {
				//there is a second identifier!
				//just copy values
				String second = matchIdent.group();
				val = Controller.get( second );
					
			} else {
				val = new SyntaxError( instMatch + " is not a valid instantiation ");
			}
		
		}		
	}
	
	
	
	@Override
	public Object val() {
		return val;
	}

	@Override
	public String ident() {
		return ident;
	}
	
	public static boolean isValid(String msg) {
		//Dummy
		return false;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
