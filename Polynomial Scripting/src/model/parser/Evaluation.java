package model.parser;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.Polynomial;
import controller.Controller;

public class Evaluation extends Expression {
	
	
	public static final String INT_ARGS = "\\s*\\(\\s*" + Literal.INT + "\\s*\\)";
	public static final String IDENT_ARGS = "\\s*\\(\\s*" + Expression.VALID_IDENT + "\\s*\\)";
	
	public static final String VALID_ARGS = "((" + INT_ARGS + ")|(" + IDENT_ARGS + "))";
	
	public static final String EVALUATION = "(" + VALID_IDENT + VALID_ARGS + ")|(" + VALID_IDENT + ")";
	public static final Pattern ARGS = Pattern.compile( Evaluation.VALID_ARGS );
	public static final Pattern INT_ARG_PATTERN = Pattern.compile( Evaluation.INT_ARGS );
	public static final Pattern IDENT_ARG_PATTERN = Pattern.compile( Evaluation.IDENT_ARGS );

	
	private String ident;
	private Object val;
	
	public Evaluation( String evalMatch ) {
		//will identify this result
		this.ident = "res" + exprCount;
		exprCount++;
		
		Matcher matchArgs = ARGS.matcher(evalMatch);
		if(matchArgs.find()) {
			//Polynomial evaluation !
			Matcher identMatcher = IDENT_PATTERN.matcher(evalMatch);
			identMatcher.find();
			String pNomName = identMatcher.group();
			
			Matcher intMatcher = INT_PATTERN.matcher(evalMatch);
			
			String args;
		 if( identMatcher.find() ) {

			//must get another identifier, polynomial mapped over array
			args = identMatcher.group();

			Polynomial pNom = (Polynomial)Controller.get( pNomName );
			this.val = pNom.eval( (ArrayList<Integer>)Controller.get( args ) ); 
			
			
		 } else if( intMatcher.find()) {
				// found direct integer args 
				args = intMatcher.group();

				Polynomial pNom = (Polynomial)Controller.get( pNomName );
				this.val = new Integer(pNom.eval( Integer.parseInt(args) ));
				
			} else {
				
				this.val = new SyntaxError("It seems you're trying to evaluate the polynomial over some illegal pattern");
			}
			
		} else {
			//simple name call, must reset ident to remain the same object
			Matcher matchIdent = IDENT_PATTERN.matcher(evalMatch);
			matchIdent.find();
			this.ident = matchIdent.group();

			this.val = Controller.get( ident );

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
	


	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
