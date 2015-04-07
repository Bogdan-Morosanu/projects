package board;

public class InvalidMoveException extends Exception {
	public InvalidMoveException(String s) {
		super(s);
	}
	
	public InvalidMoveException(InvalidPositionException e) {
		super("Move invalid because of underlying Position Exception\n" 
				+ "posException member details will follow ");
		
		this.posException = e;
	}
	
	public InvalidMoveException(NotALineException e) {
		super("can't move because path should be a line and isn't \n" + 
				"nlineException member details will follow");
		
		this.nlineException = e;
	}
	
	public InvalidMoveException newFromExisting(String message) {
		InvalidMoveException newExcept = new InvalidMoveException(
				this.getMessage() + "\n" + message
				);
		newExcept.setStackTrace(this.getStackTrace());
		
		return newExcept;
	}
	
	@Override
	public String getMessage() {
		return super.getMessage() + ((posException == null) ? "" :
				 "\nunderlying position exception details follow : \n " + posException.getMessage() 
				 + ((nlineException == null) ? "" : "\nunderlying line exception details follow\n"));
	}
	
	public InvalidPositionException posException;
	public NotALineException nlineException;
	
}

