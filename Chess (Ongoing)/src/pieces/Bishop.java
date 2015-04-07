package pieces;

import board.InvalidMoveException;
import board.Position;
import board.InvalidPositionException;
import pieces.Piece.Color;

public final class Bishop extends Piece {

	public Bishop(Color color, Position pos) {
		super(color,pos);
	}
	
	Bishop(Color color, int row, int col) throws InvalidPositionException {
		super(color,row,col);
	}
	
	public Bishop(Position pos) {
		super(pos);
	}

	@Override
	public void executeMove(Position where) throws InvalidMoveException {
		try {
			if(this.canSlideTo(where) && this.moveTypePermitted(where)) {
				//TODO actual move action
				
				} else {
					throw new InvalidMoveException(
								"position " + where + "unreachable\n"
								+ "path blocked by some other piece"
								+ " or move type not permitted\n"
							);
				}
				
			} catch(InvalidPositionException e) {
				throw new InvalidMoveException(e);
			}
		}
	
	@Override
	public boolean moveTypePermitted(Position where) {
		int rowDiff = where.row - this.cntPos.row;
		int colDiff = where.col - this.cntPos.col;
		
		if(rowDiff == 0 || colDiff == 0) {
			return false;
		}
		
		if( rowDiff/colDiff == 1 || rowDiff/colDiff == -1) {
			/*
			 * we are moving along the second or 
			 * main diagonal (related to current piece 
			 * position as origin), so the move is valid
			 */
			return true;
		} else {
			return false;
		}
		
	}
	
	//Testing purposes
	public static void main(String[] args) throws Exception {
		Bishop bishop = new Bishop(Piece.Color.BLACK,7,2);
		Position pointer = new Position(5,0);
		try {
			bishop.moveTo(pointer);
			System.out.println("bishop arrived safely @ " + pointer);
			
			pointer.translate(1,0);
			bishop.moveTo(pointer);
			System.out.println("bishop arrived illegaly @ " + pointer);
			
		} catch( InvalidMoveException e) {
			System.out.println(e.getMessage());
		}
	}

	@Override
	public char toChar() {
		//Unicode!
		return (this.color == Color.WHITE) ? (char)0x2657 : (char)0x265D ;
	}
}



