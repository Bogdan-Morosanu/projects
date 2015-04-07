package model.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;



public abstract class Expression {
	
	protected static int exprCount = 0;
	private static String lastIdent = "";
	
	public static final String CAPTURE_LAST = "_";
	
	
	public static final String RESERVED_WORDS = "((step)|(to))";
	public static final String VALID_IDENT = "[\\w&&[^\\d]][\\w&&[^\\.]]*";	
	
	
	protected static final Pattern INT_PATTERN = Pattern.compile(Literal.INT);
	
	protected static final Pattern PNOM_INST_PATTERN = Pattern.compile( PnomInstance.PNOM_INSTANTIATION );
	protected static final Pattern EVAL_PATTERN = Pattern.compile( Evaluation.EVALUATION );
	
	protected static final Pattern IDENT_PATTERN = Pattern.compile( Expression.VALID_IDENT );
	protected static final Pattern RESERVED_WORDS_PATTERN = Pattern.compile( Expression.RESERVED_WORDS );
	
	protected static final Pattern PNOM_PATTERN = Pattern.compile( Literal.VALID_PNOM );
	protected static final Pattern PNOM_GNRT_PATTERN = Pattern.compile( PnomGenerator.PNOM_GENERATOR );
	
	protected static final Pattern ARRAY_LIT_PATTERN = Pattern.compile( Literal.VALID_ARRAY );
	
	protected static final Pattern LITERAL_PATTERN = Pattern.compile( Literal.VALID_LITERAL );
	
	public static final String DERIV = "\\.deriv";
	public static final String INTEGR = "\\.integr";
	
	public static final String VALID_CALC_OP = "\\s*" + VALID_IDENT + "((" + DERIV + ")|(" + INTEGR + "))\\s*";
	public static final Pattern CALC_PATTERN = Pattern.compile( VALID_CALC_OP );
	
	public static final Pattern ARR_INST_PATTERN = Pattern.compile( ArrayInstance.ARRAY_INST );
	
	public static Expression construct( String raw ) {
		Expression result;
		String msg = raw.replace(CAPTURE_LAST, lastIdent);
		
		Matcher matchPnomInst = PNOM_INST_PATTERN.matcher(msg);
		Matcher matchArrInst = ARR_INST_PATTERN.matcher(msg);
		Matcher matchEval = EVAL_PATTERN.matcher(msg);
		Matcher matchGnrt = PNOM_GNRT_PATTERN.matcher(msg);
		Matcher matchLit = LITERAL_PATTERN.matcher(msg);
		Matcher matchCalc = CALC_PATTERN.matcher(msg);
	
		
		if(matchPnomInst.matches()) {
			
			result = new PnomInstance( matchPnomInst.group() );
			Expression.lastIdent = result.ident();
		
		} else if(matchArrInst.matches()){
			
			result = new ArrayInstance( matchArrInst.group() );
			Expression.lastIdent = result.ident();
		
		} else if(matchGnrt.matches()) {
			
			result = new PnomGenerator( matchGnrt.group() );
			Expression.lastIdent = result.ident();
			
		} else if(matchCalc.matches()){
			
			result = new Calcullus( matchCalc.group() );
			Expression.lastIdent = result.ident();	
		
		} else if(matchLit.matches()){
			
			result = new Literal( matchLit.group() );
			Expression.lastIdent = result.ident();	
			
		
		} else if(matchEval.matches()){
			
			result = new Evaluation( matchEval.group() );
			Expression.lastIdent = result.ident();
			
		} else {
			result = new SyntaxError("not a valid polynomial expression");
		}

		return result;
	}
	
	abstract public Object val();
	abstract public String ident();
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
