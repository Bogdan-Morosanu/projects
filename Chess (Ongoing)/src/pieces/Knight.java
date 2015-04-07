package pieces;

import board.InvalidMoveException;
import board.InvalidPositionException;
import board.Position;
import pieces.Piece.Color;

public final class Knight extends Piece {
	
	public Knight(Color color, Position pos) {
		super(color,pos);
	}
	
	Knight(Color color, int row, int col) throws InvalidPositionException {
		super(color,row,col);
	}
	
	public Knight(Position pos) {
		super(pos);
	}

	@Override
	public void executeMove(Position where) throws InvalidMoveException {
		// TODO Auto-generated method stub

	}
	
	@Override
	public boolean moveTypePermitted(Position where) {
		int rowDiff = where.row - this.cntPos.row;
		int colDiff = where.col - this.cntPos.col;
		
		//make positive
		rowDiff = rowDiff < 0 ? -rowDiff : rowDiff;
		colDiff = colDiff < 0 ? -colDiff : colDiff;
		
		if( (rowDiff == 1 && colDiff == 2) || (rowDiff == 2 && colDiff == 1) ) {
			return true;
		} else {
			return false;
		}
	
	}
	
	@Override
	public boolean canSlideTo(Position where) {
		return true; 
	} //Knights can jump over any piece
	
	//Testing purposes 
	public static void main(String[] args) throws Exception {
		Knight k = new Knight(Piece.Color.BLACK,7,1);
		Position pointer = new Position(5,2);
		try { 
			k.moveTo(pointer);
			System.out.println("knight arrived to pointer safely @ " + k.cntPos);
			
			pointer.translate(0,1);
			System.out.println("trying to move to " + pointer);
			k.moveTo(pointer);
			
			System.out.println("knight arrived illegally @ " + k.cntPos);
			
		} catch(Exception e) {
			System.out.println("error : " +  e.getMessage());
		}
		
	}

	@Override
	public char toChar() {
		//Unicode!
		return (this.color == Color.WHITE) ? (char)0x2658 : (char)0x265E;
	}

}
