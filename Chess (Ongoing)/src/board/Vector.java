package board;

public class Vector {
	public final int row;
	public final int col;
	
	public Vector(int row, int col){
		this.row = row;
		this.col = col;
	}
	
	
	public boolean equals(Vector that) {
		return this.row == that.row && this.col == that.col;
	}
}
