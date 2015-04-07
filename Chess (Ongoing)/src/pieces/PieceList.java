package pieces;

import java.util.ArrayList;

import board.Position;

public class PieceList extends ArrayList<Piece> {
	public boolean add(Piece e) {
		return super.add(e);
	}
	
	public String toString() {
		String str = "PieceList Follows :\n"; 
		for(Piece p : this) {
			str += (p != null) ? p.toString() + "\n" : "empty\n";
		}
		
		return str;		
	}
	
	public boolean containsColor(Piece.Color color) {
		for(Piece p : this) {
			if(p.getColor() == color) {
				return true;
			}
		}
		
		return false;
	}
		
}
