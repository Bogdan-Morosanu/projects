package board;

import java.util.ArrayList;

import pieces.Piece;

public class PositionList extends ArrayList<Position> {
	public boolean add(Position e) {
		return super.add(e);
	}
	
	public String toString() {
		String str = "PositionList Follows :\n"; 
		for(Position p : this) {
			str += (p != null) ? p.toString() + "\n" : "empty\n";
		}
		
		return str;		
	}
	
	public void addIfNotNull(Position e) {
		if(e != null) this.add(e);
	}
	
	public PositionList filterNull() {
		PositionList finalList = new PositionList();
		
		for(Position candidate : this) {
			if(candidate != null) finalList.add(candidate);
		}

		return finalList;
	}
}
