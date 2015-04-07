package pieces;

import board.InvalidMoveException;
import board.InvalidPositionException;
import board.Position;
import pieces.Piece.Color;

public final class Queen extends Piece {

	public Queen(Color color, Position pos) {
		super(color,pos);
	}
	
	Queen(Color color, int row, int col) throws InvalidPositionException {
		super(color,row,col);
	}
	
	public Queen(Position pos) {
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
		
		if(rowDiff == 0 ^ colDiff == 0) {
			return true; //Rook-type move
		} else if(rowDiff/colDiff == 1 || rowDiff/colDiff == -1) {
			return true; //Bishop-type move
		} else {
			return false;
		}
		
	}
	
	public static void main(String[] args) throws Exception {
		Queen q = new Queen(Piece.Color.BLACK,7,4);
		Position pointer = new Position(7,0);
		try {
			q.moveTo(pointer);
			System.out.println("Queen arrived safely @ " + pointer);
			
			pointer.translate(-3,4);
			q.moveTo(pointer);
			System.out.println("Queen arrived illegaly @ " + pointer);
			
		} catch( InvalidMoveException e) {
			System.out.println(e.getMessage());
		}
	}

	@Override
	public char toChar() {
		//Unicode
		return (this.color == Color.WHITE) ? (char)0x2655 : (char)0x265B;
	}
	
}
