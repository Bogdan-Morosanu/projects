package board;

import java.io.*;

import controller.AppRegistry;

import pieces.Bishop;
import pieces.King;
import pieces.Knight;
import pieces.Piece;
import pieces.Pawn;
import pieces.Piece.Color;
import pieces.Queen;
import pieces.Rook;
import pieces.PieceList;

public class Map implements Cloneable {
	public static final String DEF_PATH = "./conf-full";
	private Piece[][] map = new Piece[8][8];
	
	public Piece getPieceAt(Position where) {
		return map[where.row][where.col]; 
	}
	
	public Piece getPieceAt(int row, int col) {
		return map[row][col];
	}
	
	/**
	 * Checked Access to map - returns null instead of throwing out 
	 * of bounds exceptions
	 * @param row
	 * @param col
	 * @return piece from map or null in case of out of bounds request
	 */
	public Piece getPieceOrNull(int row, int col) {
		Piece p = null;
		
		if(row >= 0 && row < 8 && col >=0 && col < 8) {
			p = this.getPieceAt(row,col);
		}
		
		return p;
	}
	
		
	
	public void setPieceAt(Position where, Piece what) {
		map[where.row][where.col] = what;
	}
	
	public void capture(Piece victim, Piece attacker) {
		this.setPieceAt(victim.getPosition(), attacker);
		victim.delete();
	}
	
	/**
	 * Returns a step vector that will translate start to destination, and visit all Positions in between.
	 * @param start
	 * @param destination
	 * @return step vector
	 * @throws NotALineException will be thrown in case start --> destination is not a valid Queen Move
	 */
	
	public Vector getStepVector(Position start, Position destination) throws NotALineException {
		
		int rowDiff = destination.row - start.row;
		int colDiff = destination.col - start.col;
		
		if(Math.abs(rowDiff) != Math.abs(colDiff) && rowDiff != 0 && colDiff != 0) {
			return null;
		} //a line must be straight! - no straight path, no line!
		
		int rowStep = (rowDiff == 0) ? 0 :
							((rowDiff > 0) ? 1 : -1);

		int colStep = (colDiff == 0) ? 0 :
							((colDiff > 0) ? 1 : -1);
		
		return new Vector(rowStep,colStep);
	}
	
	
	/**
	 * Returns a line of pieces between two positions.
	 * @param from
	 * @param to
	 * @return PieceList list of all pieces in positions [from;to]
	 * @throws InvalidPositionException
	 */
	
	public PieceList getPieceLine(Position from, Position to) throws InvalidPositionException {
		PieceList list = new PieceList();
		Position crawler = from.clone();
			
		int rowDiff = to.row - from.row;
		int colDiff = to.col - from.col;
		
		if(Math.abs(rowDiff) != Math.abs(colDiff) && rowDiff != 0 && colDiff != 0) {
			return null;
		} //a line must be straight! - no straight path, no line!
		
		
		int rowStep = (rowDiff == 0) ? 0 :
							((rowDiff > 0) ? 1 : -1);

		int colStep = (colDiff == 0) ? 0 :
							((colDiff > 0) ? 1 : -1);
		
		
		while(!crawler.equals(to)) {
			try {
				crawler.translate(rowStep,colStep);
				Piece current = this.getPieceAt(crawler);
				list.add(current);
				
			} catch (InvalidPositionException e) {
				throw e.newFromExisting("crawler walker off the board");
			}
		}
		
		
		return list;
	}
	
	public PieceList getPieceLinePlusOrigin(Position from, Position to) throws InvalidPositionException {
		//TODO Efficientization opportunity, execute in reverse order
		PieceList list = getPieceLine(from,to);
		list.add(0,this.getPieceAt(from));
		return list;
	}
	
	public PositionList getPositionLine(Position from, Position to) throws InvalidPositionException {
			PositionList list = new PositionList();
			Position crawler = from.clone();
				
			int rowDiff = to.row - from.row;
			int colDiff = to.col - from.col;
			
			if(Math.abs(rowDiff) != Math.abs(colDiff) && rowDiff != 0 && colDiff != 0) {
				return null;
			} //a line must be straight! - no straight path, no line!
			
			
			int rowStep = (rowDiff == 0) ? 0 :
								((rowDiff > 0) ? 1 : -1);

			int colStep = (colDiff == 0) ? 0 :
								((colDiff > 0) ? 1 : -1);
			
			
			while(!crawler.equals(to)) {
				try {
					crawler.translate(rowStep,colStep);
					list.add(crawler.clone());
					
				} catch (InvalidPositionException e) {
					throw e.newFromExisting("crawler walker off the board");
				}
			}
			
			return list;
	}
	
	public PositionList getPositionLinePlusOrigin(Position from, Position to) throws InvalidPositionException {
		PositionList list = getPositionLinePlusOrigin(from,to);
		list.add(from.clone());
		return list;
	}
		
	
	public void movePiece(Piece what, Position where) {
		Position from = what.getPosition();
		int row = from.row;
		int col = from.col;
		
		Piece toCapture = this.map[where.row][where.col];
		
		if(toCapture != null) {
			//TODO CaptureList.add()
		}
		
		this.map[row][col] = null; //leave
		this.map[where.row][where.col] = what; //arrive
	}
	
	/**
	 * loads map from configuration file.
	 * pieces are expressed in color-pieceType format
	 * 
	 * pieceType resolution is as follows:
	 * R - Rook 
	 * N - kNight
	 * B - Bishop
	 * K - Knight
	 * Q - Queen
	 * P - Pawn
	 * 
	 * color resolution is as follows:
	 * w - White
	 * b - Black
	 * 
	 * so, for instance white pawns will be wP
	 */
	public void loadConfFile(String path) {
		try {
			BufferedReader conf = new BufferedReader(new FileReader(path));
			
			
			for(int row = 0; row < 8; row++) {
				Position crawler = new Position(row,0);
				String line = conf.readLine();
				
				
				int tokenNumber = 0;
				for(int col = 0; col < line.length() && tokenNumber < 8;col++) {
					char colorToken = line.charAt(col);
					
					//won't process whitespace
					if(!Character.isWhitespace(colorToken)) {
						//put piece
						char pieceToken = line.charAt(col + 1);
						pieceFromToken(crawler,colorToken,pieceToken);
											
						//update column counter 
						//because we have processed two tokens
						col++;
						
						//update position
						if(tokenNumber != 7) crawler.translate(0,1);
						tokenNumber++;
						
					}
				}
				
				//back to beggining of line
				//if(row != 7) crawler.translate(1,-7);
			}
			
			conf.close();
			
		} catch(FileNotFoundException e) {
			System.out.println("conf file not found!");
		} catch(IOException e) {
			e.printStackTrace();
		} catch(InvalidPositionException e) {
			//
		} 
	}
	
	
	private void pieceFromToken(Position pos, char colorToken, char pieceToken) {
		switch(pieceToken) {
			case 'P': 
				setPieceAt(pos,
						new Pawn((colorToken == 'w') ? Color.WHITE : Color.BLACK ,pos)
				);
				return;
			case 'R':
				setPieceAt(pos,
						new Rook((colorToken == 'w') ? Color.WHITE : Color.BLACK ,pos)
				);
				return;
			case 'N':
				setPieceAt(pos,
						new Knight((colorToken == 'w') ? Color.WHITE : Color.BLACK ,pos)
				);
				return;
			case 'B':
				setPieceAt(pos,
						new Bishop((colorToken == 'w') ? Color.WHITE : Color.BLACK ,pos)
				);
				return;
			case 'Q':
				setPieceAt(pos,
						new Queen((colorToken == 'w') ? Color.WHITE : Color.BLACK ,pos)
				);
				return;
			case 'K':
				setPieceAt(pos,
						new King((colorToken == 'w') ? Color.WHITE : Color.BLACK ,pos)
				);			
				return;
		}
	}
	
	public String toString() {
		String strMap = "";
		
		for(int row = 7; row >= 0; row--) {
			for(int col = 0; col < 8; col++) {
				if(map[row][col] != null) {
					
					strMap += map[row][col].toChar() + " ";
				} else {
					strMap += "." + " ";
				}	
			}
			strMap += '\n';
		}
		
		return strMap;
	}
	
	public PieceList getSurroundingPieces(Position pos) {
		PieceList list = new PieceList();
		
		PositionList positionList = getSurroundingConstPositions(pos);
		
		for(Position surrounding : positionList) {
			list.add(this.getPieceAt(surrounding));
		}
				
		return list;
	}
	
	

	public PositionList getSurroundingConstPositions(Position pos) {
		PositionList list = new PositionList();

		
		boolean hasLeft = pos.col > 0;
		boolean hasRight = pos.col < 7;
		boolean hasUnder = pos.row > 0;
		boolean hasAbove = pos.row < 7;
		
		if(hasUnder) {
			list.add(AppRegistry.getConstPosition(pos.row - 1, pos.col));
			
			if(hasRight) {
				list.add(AppRegistry.getConstPosition(pos.row - 1, pos.col + 1));
			}
			
			if(hasLeft) {
				list.add(AppRegistry.getConstPosition(pos.row - 1, pos.col - 1));
			}
		}
		
		if(hasRight) {
			list.add(AppRegistry.getConstPosition(pos.row,pos.col + 1));
		}
		
		if(hasLeft) {
			list.add(AppRegistry.getConstPosition(pos.row,pos.col - 1));
		}
		
		if(hasAbove) {
			list.add(AppRegistry.getConstPosition(pos.row + 1,pos.col));
			
			if(hasRight) {
				list.add(AppRegistry.getConstPosition(pos.row + 1, pos.col + 1));
			}
			
			if(hasLeft) {
				list.add(AppRegistry.getConstPosition(pos.row + 1, pos.col - 1));
			}
		}
				
		return list;
	}
	
	
	public PieceList getPossibleAttackers(Piece target) {
		Position targetPos = target.getPosition();
		return getPossibleAttackers(targetPos);
	}
	
	public PieceList getPossibleAttackers(Position target){
		
		PieceList list = new PieceList();
		
		Position north = AppRegistry.getConstPosition(target.row + 1, target.col);
		Piece pieceNorth = (north != null) ? getFirstInDirection(target,north) : null;
		
		Position south = AppRegistry.getConstPosition(target.row - 1, target.col);
		Piece pieceSouth = (south != null) ? getFirstInDirection(target,south) : null;
		
		Position west = AppRegistry.getConstPosition(target.row, target.col - 1);
		Piece pieceWest = (west != null) ? getFirstInDirection(target,west) : null;
		
		Position east = AppRegistry.getConstPosition(target.row, target.col + 1);
		Piece pieceEast = (east != null) ? getFirstInDirection(target,east) : null;
		
		Position northEast = AppRegistry.getConstPosition(target.row + 1, target.col + 1);
		Piece pieceNE = (northEast != null) ? getFirstInDirection(target,northEast) : null;
		
		Position northWest = AppRegistry.getConstPosition(target.row + 1, target.col - 1);
		Piece pieceNW = (northWest != null) ? getFirstInDirection(target,northWest) : null;
		
		Position southEast = AppRegistry.getConstPosition(target.row - 1, target.col + 1);
		Piece pieceSE = (southEast != null) ? getFirstInDirection(target,southEast) : null;
		
		Position southWest = AppRegistry.getConstPosition(target.row - 1, target.col - 1);
		Piece pieceSW = (southWest != null) ? getFirstInDirection(target,southWest) : null;
		
		
		if(pieceNorth != null && pieceNorth.canAttack(target)) list.add(pieceNorth);
		if(pieceSouth != null && pieceSouth.canAttack(target)) list.add(pieceSouth);
		if(pieceWest != null && pieceWest.canAttack(target)) list.add(pieceWest);
		if(pieceEast != null && pieceEast.canAttack(target)) list.add(pieceEast);
		if(pieceNW != null && pieceNW.canAttack(target)) list.add(pieceNW);
		if(pieceNE != null && pieceNE.canAttack(target)) list.add(pieceNE);
		if(pieceSW != null && pieceSW.canAttack(target)) list.add(pieceSW);
		if(pieceSE != null && pieceSE.canAttack(target)) list.add(pieceSE);
		
		Piece horseNNE = getPieceOrNull(target.row + 2, target.col + 1);
		Piece horseENE = getPieceOrNull(target.row + 1, target.col + 2);
		Piece horseNNW = getPieceOrNull(target.row + 2, target.col - 1);
		Piece horseWNW = getPieceOrNull(target.row + 1, target.col - 2);
		Piece horseSSE = getPieceOrNull(target.row - 2, target.col + 1);
		Piece horseESE = getPieceOrNull(target.row - 1, target.col + 2);
		Piece horseSSW = getPieceOrNull(target.row - 2, target.col - 1);
		Piece horseWSW = getPieceOrNull(target.row - 1, target.col - 2);
		
		
		//instanceof is a filter for null aswell
		if(horseNNE instanceof Knight) list.add(horseNNE);
		if(horseENE instanceof Knight) list.add(horseENE);
		if(horseNNW instanceof Knight) list.add(horseNNW);
		if(horseWNW instanceof Knight) list.add(horseWNW);
		if(horseSSE instanceof Knight) list.add(horseSSE);
		if(horseESE instanceof Knight) list.add(horseESE);
		if(horseSSW instanceof Knight) list.add(horseSSW);
		if(horseWSW instanceof Knight) list.add(horseWSW);
		
		
		return list;
	}
	
	public PieceList getPossibleAttackersOfColor(Position target,Piece.Color color){
		
		PieceList list = new PieceList();
		
		Position north = AppRegistry.getConstPosition(target.row + 1, target.col);
		Piece pieceNorth = (north != null) ? getFirstInDirection(target,north) : null;
		
		Position south = AppRegistry.getConstPosition(target.row - 1, target.col);
		Piece pieceSouth = (south != null) ? getFirstInDirection(target,south) : null;
		
		Position west = AppRegistry.getConstPosition(target.row, target.col - 1);
		Piece pieceWest = (west != null) ? getFirstInDirection(target,west) : null;
		
		Position east = AppRegistry.getConstPosition(target.row, target.col + 1);
		Piece pieceEast = (east != null) ? getFirstInDirection(target,east) : null;
		
		Position northEast = AppRegistry.getConstPosition(target.row + 1, target.col + 1);
		Piece pieceNE = (northEast != null) ? getFirstInDirection(target,northEast) : null;
		
		Position northWest = AppRegistry.getConstPosition(target.row + 1, target.col - 1);
		Piece pieceNW = (northWest != null) ? getFirstInDirection(target,northWest) : null;
		
		Position southEast = AppRegistry.getConstPosition(target.row - 1, target.col + 1);
		Piece pieceSE = (southEast != null) ? getFirstInDirection(target,southEast) : null;
		
		Position southWest = AppRegistry.getConstPosition(target.row - 1, target.col - 1);
		Piece pieceSW = (southWest != null) ? getFirstInDirection(target,southWest) : null;
		
		
		if(pieceNorth != null && pieceNorth.getColor() == color 
				&& pieceNorth.canAttack(target)) list.add(pieceNorth);
		if(pieceSouth != null && pieceSouth.getColor() == color
				&& pieceSouth.canAttack(target)) list.add(pieceSouth);
		if(pieceWest != null && pieceWest.getColor() == color
				&& pieceWest.canAttack(target)) list.add(pieceWest);
		if(pieceEast != null && pieceEast.getColor() == color 
				&& pieceEast.canAttack(target)) list.add(pieceEast);
		if(pieceNW != null && pieceNW.getColor() == color
				&& pieceNW.canAttack(target)) list.add(pieceNW);
		if(pieceNE != null && pieceNE.getColor() == color
				&& pieceNE.canAttack(target)) list.add(pieceNE);
		if(pieceSW != null && pieceSW.getColor() == color
				&& pieceSW.canAttack(target)) list.add(pieceSW);
		if(pieceSE != null && pieceSE.getColor() == color
				&& pieceSE.canAttack(target)) list.add(pieceSE);
		
		Piece horseNNE = getPieceOrNull(target.row + 2, target.col + 1);
		Piece horseENE = getPieceOrNull(target.row + 1, target.col + 2);
		Piece horseNNW = getPieceOrNull(target.row + 2, target.col - 1);
		Piece horseWNW = getPieceOrNull(target.row + 1, target.col - 2);
		Piece horseSSE = getPieceOrNull(target.row - 2, target.col + 1);
		Piece horseESE = getPieceOrNull(target.row - 1, target.col + 2);
		Piece horseSSW = getPieceOrNull(target.row - 2, target.col - 1);
		Piece horseWSW = getPieceOrNull(target.row - 1, target.col - 2);
		

		//instanceof is a filter for null aswell
		if(horseNNE instanceof Knight && horseNNE.getColor() == color) list.add(horseNNE);
		if(horseENE instanceof Knight && horseENE.getColor() == color) list.add(horseENE);
		if(horseNNW instanceof Knight && horseNNW.getColor() == color) list.add(horseNNW);
		if(horseWNW instanceof Knight && horseWNW.getColor() == color) list.add(horseWNW);
		if(horseSSE instanceof Knight && horseSSE.getColor() == color) list.add(horseSSE);
		if(horseESE instanceof Knight && horseESE.getColor() == color) list.add(horseESE);
		if(horseSSW instanceof Knight && horseSSW.getColor() == color) list.add(horseSSW);
		if(horseWSW instanceof Knight && horseWSW.getColor() == color) list.add(horseWSW);
		
		
		return list;
	}
	
	public PieceList getPossibleAttackersOfColor(Piece p, Piece.Color color) {
		return getPossibleAttackersOfColor(p.getPosition(),color);
	}
	
	
	/** 
	 * Returns first piece pointed to by vector [from , to ], otherwise null
	 * @param from
	 * @param to
	 * @return Piece or null if there is no piece in that direction
	 */
	public Piece getFirstInDirection(Position from, Position to){
		Position crawler = from.clone();

		Vector stepV;
		try {
			stepV = getStepVector(from,to);
			
			do {
				crawler.translate(stepV.row,stepV.col);
				
			} while(this.getPieceAt(crawler) == null);
			
		} catch (NotALineException e) {
			return null; //can't get to us
		} catch (InvalidPositionException e) {
			return null; //walked off the board, no piece
		} 
		
		return this.getPieceAt(crawler);
	}
	
	public boolean isUnderFireFrom(Position pos, Piece.Color attackerColor) {
		
		return getPossibleAttackersOfColor(pos,attackerColor).size() > 0;
	}
	
	
	public PieceList toPieceList() {
		PieceList list = new PieceList();
		
		for(Piece[] row : this.map) {
			for(Piece p : row) {
				list.add(p);
			}
		}
		
		return list;
	}
	
	public static void main(String[] args) throws Exception {
		Map map = AppRegistry.getMap();
		map.loadConfFile(DEF_PATH);
		System.out.println(map);
		
	}
	
	@Override
	public Map clone() {
		//may be of use to the AI enginge
		Map clone = null;
		try {
			clone = (Map)super.clone();
		
			for(int row = 0; row < 8; row++) {
				for(int col = 0; col < 8; col++) {		
					clone.map[row][col] = (clone.map[row][col] != null) ? clone.map[row][col].clone() : null;
				}
			}
		} catch(CloneNotSupportedException e) {
			//can't happen
		}
		return clone;
	}
	
}


