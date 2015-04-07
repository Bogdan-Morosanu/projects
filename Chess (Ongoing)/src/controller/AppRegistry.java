package controller;

import pieces.King;
import pieces.Piece;
import board.InvalidPositionException;
import board.Map;
import board.Position;

/**
 * Global registry storing main information like Common position objects and Map
 * @author moro
 *
 */
public class AppRegistry {
	private static Map map = new Map();
	private static final Position[][] posBuffer = new Position[8][];
	private static King whiteKing;
	private static King blackKing;
	private static Controller statusFlags = new Controller();
	
	static {
		map.loadConfFile(Map.DEF_PATH);
		
		for(int row = 0; row < posBuffer.length; row++) {
			posBuffer[row] = new Position[8];
			for(int col = 0; col < posBuffer.length; col++ ) {
				try{ 
					posBuffer[row][col] = new Position(row,col);
				} catch(InvalidPositionException e) {
					//can't happen
				}
			}
		}
	}
	
	/**
	 * returns read only reference to position buffer, or null for out of bounds
	 * @param row
	 * @param col
	 * @return
	 */
	
	public static Position getConstPosition(int row, int col) {
		if(row < 0 || row > 7 || col < 0 || col > 7) return null;
		return posBuffer[row][col];
	}
	
	/**
	 * returns mutable copy of position in centralized buffer or null for out of bounds
	 * @param row
	 * @param col
	 * @return
	 */
	
	public static Position getMutablePosition(int row, int col) {
		if(row < 0 || row > 7 || col < 0 || col > 7) return null;
		return posBuffer[row][col].clone();
	}
	
	public static King getWhiteKing() {
		
		if(AppRegistry.whiteKing != null) return AppRegistry.whiteKing;
		
		outer: for(int row = 0; row < 8; row++) {
			for(int col = 0; col < 8; col++) {
				Piece p = map.getPieceAt(row,col);
				
				if(p instanceof King && p.getColor() == Piece.Color.WHITE) {
					AppRegistry.whiteKing = (King)p;
					break outer;
				}
			}
		}
		
		return AppRegistry.whiteKing;
	}
	
	public static King getBlackKing() {
		
		if(AppRegistry.blackKing != null) return AppRegistry.blackKing;
		
		outer: for(int row = 0; row < 8; row++) {
			for(int col = 0; col < 8; col++) {
				Piece p = map.getPieceAt(row,col);
				
				if(p instanceof King && p.getColor() == Piece.Color.BLACK) {
					AppRegistry.blackKing = (King)p;
					break outer;
				}
			}
		}
		
		return AppRegistry.blackKing;
	}
	
	public static King getKingOfColor(Piece.Color color) {
		if(color == Piece.Color.WHITE) {
			return getWhiteKing();
		} else {
			return getBlackKing();
		}
	}
	
	public static String getStringStatus() {
		return statusFlags.toString();
	}

	public static Map getMap() { return AppRegistry.map; }
	

}
