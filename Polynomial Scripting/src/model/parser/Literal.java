package model.parser;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.Polynomial;

public class Literal extends Expression {
	
	
	
	public static final String INT = "\\-?\\d+";
	public static final String LIST_SEP = "[\\s*,?\\s*]";
	public static final String PNOM_START = "\\[\\s*";
	public static final String PNOM_END = "\\s*\\]";
	public static final String ARRAY_START = "\\{\\s*";
	public static final String ARRAY_END = "\\s*\\}";
	public static final String ARRAY_GENERATOR = "\\s*" + INT + "\\s*to\\s*" + INT + "\\s*(step\\s*" + INT + "\\s*)?";
	
	public static final String VALID_PNOM = 
			 PNOM_START + INT + "[" + LIST_SEP + INT + "]*" + PNOM_END  ;
	
	public static final String VALID_ARRAY = 
			"((" + ARRAY_START + INT + "[" + LIST_SEP + INT + "]*" + ARRAY_END + 
			")|(" + ARRAY_GENERATOR + "))"  ;
	
	public static final String VALID_LITERAL = "(" + VALID_PNOM + ")|(" + VALID_ARRAY + ")";
	
	private Object val;
	private String ident;
	
	public Literal(String litStr) {
		ident = "res" + exprCount;
		exprCount++;
		
		Matcher pNom = PNOM_PATTERN.matcher( litStr );
		Matcher array = ARRAY_LIT_PATTERN.matcher( litStr );
		
		if( pNom.find() ) {
			val = parsePolynomial( pNom.group() );
		} else if( array.find() ) {
			val = parseArray( array.group() );
		} else {
			val = new SyntaxError("invalid literal pattern");
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


	public static Polynomial parsePolynomial(String pNom ) {
		Pattern integer = Pattern.compile(INT);
		Matcher intMatch = integer.matcher(pNom);
		
		ArrayList<Integer> coeffs = new ArrayList<Integer>();
		
		while(intMatch.find()) {
			coeffs.add( Integer.parseInt(intMatch.group()) );
		}
		
		return new Polynomial(coeffs);
	}
	
	public static ArrayList<Integer> parseArray(String array) {
		Pattern generator = Pattern.compile( ARRAY_GENERATOR );
		Matcher gnrtMatch = generator.matcher(array);
		
		
		
		if( gnrtMatch.find() ) {
			return parseArrayGeneration( array );
		} else {
			return parseArrayEnumeration( array );
		}
		
	}
	
	public static ArrayList<Integer> parseArrayEnumeration(String enumer ) {

		Matcher intMatch = INT_PATTERN.matcher(enumer);
		
		ArrayList<Integer> arr = new ArrayList<Integer>();
		
		while(intMatch.find()) {
			arr.add( Integer.parseInt(intMatch.group()) );
		}
		
		return arr;
	}
	
	public static ArrayList<Integer> parseArrayGeneration(String gener) {
		Matcher intMatch = INT_PATTERN.matcher(gener);
		
		intMatch.find();
		int start = Integer.parseInt( intMatch.group() );
		
		intMatch.find();
		int end = Integer.parseInt( intMatch.group() );
		
		int step = 1;
		if( intMatch.find() ) { //step speciffication is optional
			step = Integer.parseInt( intMatch.group() );
		}
		
		ArrayList<Integer> res = new ArrayList<Integer>();
		
		res.add(start);
		//test if array is non-empty
		while( (end - start) / step > 0 ) {
			start += step;
			res.add(start);
		}
		
		//a negative value means we must wrapparound to reach end.
		//we won't do that, rather we'll return an array containing only start
		
		return res;
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
