package controller;

import board.Position;
import board.Map;
import board.InvalidMoveException;
import pieces.King;
import pieces.Piece;

/**
 * Stores all information needed to execute or undo a move.
 * @author moro
 *
 */
public class Move {

	/** 
	 * instantiate once, undo/redo forever
	 */
	
	private Piece what = null;
	protected Position to = null;
	
	private Piece captured = null;
	protected Position from = null;
	
	public Move(Position from, Position to) {
		this.to = to;
		this.from = from;
	}
	
	public boolean execute() throws NotYourPieceException, ExposedYourSelfToCheckException {
		try {

			Map map = AppRegistry.getMap();
			
			this.what = map.getPieceAt(this.from);
			
			Piece.Color playerColor = Controller.getCurrentPlayer().toPieceColor();
			Piece.Color pieceColor = this.what.getColor();
			
			if(playerColor != pieceColor) {
				throw new NotYourPieceException("player " + playerColor + " can't move " + this.what);
			}
			
			//set up move for undo
			//cache results for further "undoings/redoings"
			if(captured == null) {
				this.captured = map.getPieceAt(to);
			}
			
			what.moveTo(to);
			map.setPieceAt(this.from,null);
			
			
			//now we must check if we have exposed ourselves to check
			King ourKing = AppRegistry.getKingOfColor(what.getColor());
			if(Controller.computeCheckStatus(ourKing)) {
				this.undo();
				throw new ExposedYourSelfToCheckException("can't expose one's self to check!");
			}
			
		} catch(InvalidMoveException e) {
			//DEBUG purposes, TODO extract
			System.out.println(e.getMessage());
			
			return false;
		}
		
		return true;
	}
	
	public boolean executeNoThrow() {
		try {
			this.execute();
		} catch(Exception e) {
			return false;
		}
		
		return true;
	}
	
	public boolean undo() {
		try {
			
			what.moveBackwardsTo(from);
			if(captured != null) captured.replaceOnBoard(to);
			
		} catch(InvalidMoveException e) {
			//can't normally happen, we already came from there!
			return false;
		}
		
		return true;
	}
	
	@Override
	public String toString() {
		return "move - details : \nwhat: " + this.what + "\nfrom : " + this.from + "\nto :" + to;
	}
	
	
	//Testing purposes
	public static void main(String[] args) throws Exception {
		Map map = AppRegistry.getMap();
		System.out.println(map);
		
		for(int i = 0; i < 3; i++) {
			
			
			
			Move mv = new Move(
						AppRegistry.getConstPosition(1 + i,0),
						AppRegistry.getConstPosition(2 + i, 0)
					);
			mv.execute();
			
			System.out.println(map);
		}
		
		Position to = AppRegistry.getConstPosition(4,0);
		Position from = AppRegistry.getConstPosition(5,1);
						
		
		Move mv = new Move(to,from);
		mv.execute();
		System.out.println(map);
		
		to.goTo(from);
		from.goTo(6,0);
		mv = new Move(to,from);
		mv.execute();
		System.out.println(map);
		
		to.goTo(from);
		from.goTo(7,0);
		mv = new Move(to,from);
		mv.execute();
		
		System.out.println(map);
	}

}









