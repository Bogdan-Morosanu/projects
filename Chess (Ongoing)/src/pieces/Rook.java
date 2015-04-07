package pieces;

import controller.AppRegistry;
import board.InvalidMoveException;
import board.InvalidPositionException;
import board.Map;
import board.Position;

public final class Rook extends Piece {
	
	private boolean canCastle = true;
	
	public Rook(Color color, Position pos) {
		super(color,pos);
	}
	
	public Rook(Color color, int row, int col) throws InvalidPositionException {
		super(color,row,col);
	}
	
	public Rook(Position pos) {
		super(pos);
	}

	@Override
	public boolean moveTypePermitted(Position where) {
		int rowDiff = where.row - this.cntPos.row;
		int colDiff = where.col - this.cntPos.col;

		
		if(rowDiff == colDiff)  {
			return false;
		} //bishop type move
		
		if(rowDiff == 0 || colDiff == 0) {
			return true;
		} else {
			return false;
		}
		
	}
	
	public boolean canCastle() {
		return this.canCastle;
	}
	
	
	@Override
	public void moveTo(Position where) throws InvalidMoveException {
		super.moveTo(where);
		
		//can only castle on first move
		//not accessed in case super detects invalid move and throws Exception
		if(canCastle) { 
			canCastle = false;
		}
	}
	
	/**
	 * Warning : do not call this method unless you want to irreversibly corrupt the map data.
	 * only called by King.moveTo.
	 * assumes King class has checked everything is in order and will just move rook into place.
	 * this is not a moveTo since castling is a King move, not a Rook move
	 * @see King.moveTo
	 */
	void castleArround(Position kingPosition) throws InvalidPositionException {
		//TODO
		int colDist = lookupCastleDistance();
		Position destination = this.cntPos.clone();
		
		try {
			destination.translate(0,colDist);
		} catch(InvalidPositionException e) {
			throw e.newFromExisting("castling failed because rook can't move in place ");
		}
		Map map = AppRegistry.getMap();
		map.movePiece(this,destination);
		this.cntPos = destination;
		this.canCastle = !canCastle; //toggle flag with castling
		
	}
	
	private int lookupCastleDistance() {
		//distance to do castling
		switch(cntPos.col) {
			case 0: return 3;
			case 3: return -3;
			case 7: return -2;
			case 5: return 2;
			default: throw new IllegalStateException("can't be looking to castle when not in castle position");
		}
	
	}
	
	@Override
	public void executeMove(Position where) throws InvalidMoveException {
		// TODO Auto-generated method stub

	}

	
	public static void main(String[] args) throws Exception {
		Rook rook = new Rook(Piece.Color.BLACK,7,0);
		Position pointer = new Position(7,4);
		try {
			rook.moveTo(pointer);
			System.out.println("Rook arrived safely @ " + rook.cntPos);
			
			pointer.translate(-3,0);
			rook.moveTo(pointer);
			System.out.println("Rook arrived safely @ " + pointer);
			
			pointer.translate(-1,1);
			rook.moveTo(pointer);
			System.out.println("Rook arrived illegaly @ " + pointer);
			
		} catch( InvalidMoveException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public char toChar() {
		//Unicode
		return (this.color == Color.WHITE) ? (char)0x2656 : (char)0x265C;
	}
}
