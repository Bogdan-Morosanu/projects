package controller;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.parser.Expression;
import view.NonEditableView;

public class Controller {
	
	public static final String COMMENT = "\\s*#";
	public static final Pattern COMMENT_PATTERN = Pattern.compile( COMMENT );
	public static final boolean ECHO = true;
	
	private static HashMap<String,Object> nameSpace = new HashMap<String,Object>();
	
	
	public static String processMessage( String msg ) {
	
		Matcher commentMatch = COMMENT_PATTERN.matcher(msg);
		String exprStr;
		
		//cut out comments
		if(commentMatch.find()) {
			exprStr = msg.substring(0, commentMatch.start());
		} else {
			exprStr = msg;
		}
		
		
		//if line is only comment, return
		if(exprStr.length() == 0) {
			return "";
		}
		
		try { Expression expr = Expression.construct(exprStr);
		
			String ident = expr.ident();
			Object val = expr.val();

		
			nameSpace.put( ident , val );
	
			return "\n" + ident + " --> " + val;
		
		} catch (ClassCastException e) {
			
			String response  = "\n" + e.toString();
			
			response = response.replace("java.lang.ClassCastException:", "It seems you're telling me to cast");
			response = response.replace("cannot be cast to", "to");
			
			return response + "\nI don't quite know what to do with that request...";
		}
	}
	
	public static String script( String filePath, boolean echoCommands ) throws IOException {
		BufferedReader scriptFile = new BufferedReader( new FileReader(filePath) );
		String response = "";
		String command = "";
		
		while( (command = scriptFile.readLine()) != null ) {
			if(echoCommands) {
				response += "\n" + NonEditableView.PROMPT + " " + command + "";
			}
			
			String result = processMessage( command ); 
			
			//guard agains to many new lines in our script
			//yet still format it somewhat clearly
			if(result.equals("")) {
				continue;
			} else {
				response += "\n" + result + "\n";
			}
			
			//response += processMessage( command );
		}
		
		scriptFile.close();
		return response + "\n" + NonEditableView.PROMPT;
	}
	
	public static void clearNameSpace() {
		nameSpace = new HashMap<String, Object> ();
	}
	
	public static Object get(String ident) {
		return nameSpace.get(ident);
	}
	
	public static void main(String[] args) throws IOException {
		
		System.out.println( script("basic.pnom", ECHO) );
		
	}

}
