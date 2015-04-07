package pieces;

import controller.AppRegistry;
import controller.Controller;
import board.InvalidMoveException;
import board.InvalidPositionException;
import board.Map;
import board.NotALineException;
import board.Position;
import board.PositionList;
import board.Vector;


public final class King extends Piece {
	
	/**
	 * flag used in castling. 
	 * toggled by overridden moveTo.
	 * we need to check if the king is not loaded from some configuration file in a position
	 * that is not the starting position. it is still safe to refer to protected members since
	 * our Piece super constructor has already run and initialized the reference before this
	 * initializer is run.
	 * @see moveTo 
	 */
	private boolean castlingAvailable = 
			(this.cntPos.col == 4) &&
			(this.cntPos.row == (this.color == Color.WHITE ? 0 : 7));
	
	
	public King(Color color, Position pos) {
		super(color,pos);
	
	}
	
	King(Color color) throws InvalidPositionException {
		//can't actually throw Position Exception since King Position is hardcoded
		super(color,(color == Color.WHITE ? 0 : 7),5);
	}
	
	public King(Position pos) {
		super(pos);
	}

	@Override
	public void moveTo(Position where) throws InvalidMoveException {
		if(castlingAvailable) {
			Position whereClone = where.clone();
			if(castlingUnderway(whereClone)) {
				
				Position toUndo = this.cntPos.clone();
				
				super.moveTo(whereClone);
				
				try {
					this.swipeRookArround(whereClone);
				} catch(InvalidMoveException e) {
					//we now have a corrupted map, since we casstled without a rook!
					//we must undo and rethrow the exception
					
					Map map = AppRegistry.getMap();
					map.movePiece(this, toUndo);
					this.cntPos = toUndo;
					
					throw e.newFromExisting("castling failed... but cleanup successfull"); 
					//we must inform our overlords of our failure
				}
				
				
				this.castlingAvailable = false; // only executed if no exceptions are thrown
			
			} else { //just your normal, run-of-the-mill king move
				//this style looks better than the fall-through assembly-like type
				super.moveTo(where);
			}
		
		} else { 
			super.moveTo(where); //just your normal, run-of-the-mill king move
			//this style looks better than the fall-through assembly-like type
		}
	}
	
	@Override
	public void moveBackwardsTo(Position destination) throws InvalidMoveException {
		int colDist = destination.colDist(cntPos);
		
		
		if(Math.abs(colDist) > 1) {
			// DEBUG System.out.println("undo called on castle!");
			
			Map map = AppRegistry.getMap();
			//must undo a Castling!
			Position rookPos = cntPos.clone();
			try {
				Vector step = map.getStepVector(cntPos.clone(), destination);
				rookPos.translate(step);
				Piece rook = map.getPieceAt(rookPos);
				
				if(rook instanceof Rook) {
					((Rook)rook).castleArround(cntPos);
				} else {
					//either null or some other piece
					throw new InvalidMoveException("can't undo castling, no Rook found!");
				}
				
				map.movePiece(this, destination);
				this.cntPos.goTo(destination);
				this.castlingAvailable = true; // toggle castling flag
				
			} catch(InvalidPositionException e) {
				throw (new InvalidMoveException(e)).newFromExisting("can't undo King move -- details follow");
			} catch(NotALineException e) {
				throw (new InvalidMoveException(e)).newFromExisting("can't undo King move -- details follow");
			}
		} else {
			//just a normal move
			this.moveTo(destination);
		}
	}
	
	private void swipeRookArround(Position destination) throws InvalidMoveException {
		
		//get rook position from propprer corner of map
		Position rookPos = AppRegistry.getMutablePosition(this.cntPos.row, this.cntPos.col == 6 ? 7 : 0); 
		
		//DEBUG System.out.println("Searching for rook @ " + rookPos);
		
		Map map = AppRegistry.getMap();
		Piece shouldBeRook = map.getPieceAt(rookPos);
		if(shouldBeRook instanceof Rook) {
			try {
				((Rook)shouldBeRook).castleArround(destination);
			} catch (InvalidPositionException e) {
				throw new InvalidMoveException(e);
			}
		} else {
			throw new InvalidMoveException("No rook to castle with!");
			//this means we have failed in rook-moving and are now left with an invalid map
			//we must undo the King's last move!!
		}
	}
	
	@Override
	public void executeMove(Position where) throws InvalidMoveException {
		// TODO Auto-generated method stub

	}
	
	@Override
	public boolean canAttack(Position where) {
		int rowDiff = where.row - this.cntPos.row;
		int colDiff = where.col - this.cntPos.col;
		
		rowDiff = Math.abs(rowDiff);
		colDiff = Math.abs(colDiff);
		
		
		if(rowDiff > 1 || colDiff > 1) {
			return false; //check for distance boundaries
		}
		
		Map map = AppRegistry.getMap();
		
		/*
		if(rowDiff == 0 && colDiff == 0) {
			return false;
		}
		*/
		
		if((rowDiff == 0 && colDiff != 0) ||
		   (rowDiff != 0 && colDiff == 0)){ //Rook-type move

			return true; 
		
		} else if(rowDiff/colDiff == 1 || rowDiff/colDiff == -1) { //Bishop type move
			//TODO will sometimes throw Arithmetic exceptions on Undo
			return true; 

		} else {
		
			return false;
		
		}
		
	}
	
	@Override
	public boolean moveTypePermitted(Position where) {
		Map map = AppRegistry.getMap();
		
		boolean asksForCastling = this.castlingAvailable && castlingUnderway(where); 
		//method short-circuited -- less efficiency overhead
	
		if(asksForCastling) {
			//can't castle out of check!
			if(Controller.isInCheck(this)) {
				//TODO might want to remind our user of this via some message of some sort
				return false;
			}

			PositionList lineToDestination;
			
			try {
				 lineToDestination = map.getPositionLine(this.getPosition(),where);
			} catch(InvalidPositionException e) {
				//technically can't happen
				System.out.println("king move type not permitted. exception thrown follows :");
				System.out.println(e.getMessage());
				return false;
			}
			
			for(Position passThrough : lineToDestination) {
				if(map.isUnderFireFrom(passThrough, this.color.getAdversary())) {
					//can't castle through or into check!
					return false;
				}
			}
			
			//it seems like we can castle if we have a rook to swipe arround us
			return rookAvailable(where);
			
		} else {
			
			
			//normal move -- still have to check that we are not walking into check
			//even if square is not under fire, we might just be moving our King backwards
			//along the line of fire (and thus square seems safe because our King is protecting
			//it from our attacker). must check for this case as well!
			boolean seemsOk = canAttack(where) && !map.isUnderFireFrom(where, this.color.getAdversary())
							&& (map.getPieceAt(where) == null || map.getPieceAt(where).color != this.color);
				//can attack, not under fire, null target or valid capture target
			
			boolean safeAfterMove = true;
			
			PieceList attackerList = map.getPossibleAttackersOfColor(this, this.getColor().getAdversary());
			for(Piece attacker : attackerList) {
				try {
					Position attackerPos = attacker.getPosition();
					Vector directionToKing = map.getStepVector(attackerPos,this.getPosition());
					Vector directionToDestination = map.getStepVector(attackerPos, where); 
					//the last line might throw a NotALinException!
					
					if(directionToKing == null || directionToDestination == null) {
						safeAfterMove = true; //we are lucky -- no direction to attacker
					} else {
						if(directionToKing.equals(directionToDestination)) {
							safeAfterMove = false;
						}
						//looks like we are still under fire! -- tough luck
					}
					
				} catch(NotALineException e) {
					//we either have a Knight attacking us
					//no line of fire here -- ergo no problem
					//or there is no line between our attacker and our destination
					//still no problem here!
					safeAfterMove = true;
				}
				
			}
			
			
			return seemsOk && safeAfterMove; 
		}
	}
	
	private boolean castlingUnderway(Position destination) {
		int colDist = this.cntPos.colDist(destination);
		int rowDist = this.cntPos.rowDist(destination);
		
		return rowDist == 0 && (colDist == 2 || colDist == -2);
		
	}
	
	private boolean rookAvailable(Position destination) {
		Position rookPos = destination.clone();	
		try {
			rookPos.translate(0,(destination.col == 2) ? -2 : 1); //select rook -- should be next to king
			Map map = AppRegistry.getMap();
			Piece shouldBeRook = map.getPieceAt(rookPos);
			
			if(shouldBeRook instanceof Rook) {
				//handles null references as well. null instanceof anything yields false
				boolean canCastle = ((Rook) shouldBeRook).canCastle();
				boolean isOfValidColor = shouldBeRook.getColor().equals(this.color);
				//the second check is needed in case we load a map in some other position
				//than the initial start position and a rook is placed on the map maybe
				//on the same row with the king of the other color
				
				return isOfValidColor && canCastle;
			}
			
			return false; //no rook there
			
		} catch(InvalidPositionException e) {
			System.out.println("no rook to castle with :\n" + e.getMessage());
			return false; 
		}
		
	}
	
	
	public static void main (String[] args) throws Exception {
		King king = new King(Piece.Color.BLACK);
		Position pointer = new Position(6,5);
		try {
			king.moveTo(pointer);
			System.out.println("king arrived safely @ " + king.cntPos);
		
		 	pointer.translate(-2,-2);
			king.moveTo(pointer);
			king.moveTypePermitted(pointer);
			System.out.println("king arrived illegaly @ " + pointer);
		} catch(InvalidMoveException e) {
			System.out.println(e.getMessage());
		}
		
	}

	@Override
	public char toChar() {
		//Unicode!
		return (this.color == Color.WHITE) ? (char)0x2654 : (char)0x265A;
	}

}
