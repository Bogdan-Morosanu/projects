package board;

public class InvalidPositionException extends Exception {
	public InvalidPositionException(String s) {
		super(s);
	}
	
	public InvalidPositionException newFromExisting(String message) {
		
		InvalidPositionException newExcept =  new InvalidPositionException(
					this.getMessage() + "\n" + message
				);
		
		newExcept.setStackTrace(this.getStackTrace());
		
		return newExcept;
	}
}