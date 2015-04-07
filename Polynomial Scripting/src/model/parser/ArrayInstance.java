package model.parser;

import java.util.regex.Matcher;

import controller.Controller;

public class ArrayInstance extends Expression {
	
	public static final String ARRAY_INST = "\\s*" + VALID_IDENT + "\\s*=\\s*" + Literal.VALID_ARRAY + "\\s*";
	
	
	private Object val;
	private String ident;
	
	public ArrayInstance( String arrayInst ) {
		
		Matcher matchIdent = IDENT_PATTERN.matcher(arrayInst);
		matchIdent.find();
		this.ident = matchIdent.group();
		
		Matcher checkWords = RESERVED_WORDS_PATTERN.matcher( ident );
		if( checkWords.find() ) {
			//check for reserved words being used
			ident = "invalid";
			val = new SyntaxError( checkWords.group() + " is a reserved identifier");
		} else {
			
			Matcher matchLiteral = ARRAY_LIT_PATTERN.matcher(arrayInst);
			
			
			if( matchLiteral.find() ) {
	
				val = Literal.parseArray( matchLiteral.group() );				
			
			} else if( matchIdent.find() ) {
				//there is a second identifier!
				//just copy values
				String second = matchIdent.group();
				val = Controller.get( second );
					
			} else {
				val = new SyntaxError( arrayInst + " is not a valid array instantiation ");
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

}
