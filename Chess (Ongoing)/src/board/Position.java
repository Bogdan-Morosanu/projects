package board;


public class Position implements Cloneable {
	//TODO : make row/col final + refactor everything via colDist and rowDist methods
	public int row;
	public int col;
	
	
	public Position(int row, int col) throws InvalidPositionException {
		ensurePosition(row,col);
		this.row = row;
		this.col = col;
	}
	
	public static void ensurePosition(int row, int col) throws InvalidPositionException{
		//makes sure position is within chess board
		if(row < 0 || row > 7 || col < 0 || col > 7) {
			throw new InvalidPositionException(
					"invalid position @ [" + row + "][" + col + "]"
			);
		}
	}
	
	public void goTo(Position pos) {
		this.col = pos.col;
		this.row = pos.row;
	}
	
	public void goTo(int row, int col) throws InvalidPositionException {
		ensurePosition(row,col);
		this.row = row;
		this.col = col;
	}
	
	public void translate(Position offset) throws InvalidPositionException {
		ensurePosition(this.row + offset.row, this.col + offset.col);
		this.row += offset.row;
		this.col += offset.col;
	}
	
	public void translate(int offrow, int offcol ) throws InvalidPositionException {
		ensurePosition(this.row + offrow, this.col + offcol);
		this.row += offrow;
		this.col += offcol;
	}
	
	public void translate(Vector offset) throws InvalidPositionException {
		translate(offset.row,offset.col);
	}
	
	/**
	 * returns distance to other position.
	 * the sign of the distance if such that after we call
	 * pos.translate(pos.rowDist(other),pos.colDist(other))
	 * we are guaranteed that pos.equals(other) is true;
	 * @see colDist
	 * @param other 
	 * @return distance to other position such that other.row + distance == this.row
	 */
	public int rowDist(Position other) {
		return other.row - this.row;
	}
	
	/**
	 * returns distance to other position.
	 * the sign of the distance if such that after we call
	 * pos.translate(pos.rowDist(other),pos.colDist(other))
	 * we are guaranteed that pos.equals(other) is true;
	 * @see rowDist
	 * @param other 
	 * @return distance to other position such that other.col + distance == this.col
	 */
	public int colDist(Position other) {
		return other.col - this.col;
	}
	
	public String toString() {
		return "[" + this.row + "][" + this.col + "]";
	}
	
	public boolean equals(Position pos) {
		return (this.col == pos.col) && (this.row == pos.row);
	}
	
	@Override
	public Position clone() {
		try {
			return (Position) super.clone();
			//provides type safety for extending classes
		} catch(CloneNotSupportedException e) {
			//can't happen, Object supports Clone
			return null;
		}
	}
	
	public static void main (String[] args) throws Exception {
		Position pos = new Position(1,1);
		System.out.println(pos);
	}
}