package pieces;

import controller.AppRegistry;
import board.Map;
import board.InvalidMoveException;
import board.InvalidPositionException;
import board.Position;

public final class Pawn extends Piece {
	
	private boolean made2SquareOpen = false;
	private Pawn enpassantCapture;

	public Pawn(Color color, Position pos) {
		super(color,pos);
	}
	
	
	public Pawn(Color color, int row, int col) throws InvalidPositionException {
		super(color,row,col);
		
		if((color == Piece.Color.WHITE && row != 1) || row != 6) {
			throw new InvalidPositionException(
					"can't instantiate " + color + " pawn @ row " + row + "\n"
				);
		}
	}
	
	
	
	public Pawn(Position pos) {
		super(pos);
	}


	@Override
	@Deprecated
	public void executeMove(Position where) throws InvalidMoveException {
		//Black has home row 7 and White has home row 0
		//thus black pawns move downward and white ones upward
		int dir = (this.color == Color.WHITE ? 1 : -1);
		int rowDiff = where.row - this.cntPos.row;
		if(rowDiff != dir) {
			//we are trying to move the pawn the wrong way
			throw new InvalidMoveException(
						"can't move " + this.color + " pawn " 
						+ (dir == -1 ? "down" : "up")
						+ " the board"
					);
		}
		
		//now testing for column movement
		int colDiff =  where.col - this.cntPos.col;
		Map map = AppRegistry.getMap();
		
		switch(colDiff) {
			case 0: //we stay on same column, must be clear ahead to move pawn.
				if( map.getPieceAt(where) == null) {
					//our road is clear
					this.cntPos.goTo(where);
					map.setPieceAt(where,this);
					break;
				} else {
					throw new InvalidMoveException(
								"can't move pawn" + (dir == -1 ? "up" : "down")
								+ " to " + where 
								+ " : road blocked by some other Piece!\n"
							);
				}
			case 1: case -1: //we want to take something with the pawn
				Piece toCapture = map.getPieceAt(where);
				//check for null and King : not candidates for capture
				if(toCapture == null || toCapture instanceof King) {
					throw new InvalidMoveException( 
							toCapture == null ? "nothing to capture" : "can't capture King"
						);
				}
				
				//check for color
				if(toCapture.color == this.color) {
					throw new InvalidMoveException(
							"can't capture your own Pieces!\n"
						);
				}
				
				
				
				//now everything is ok, we can capture!
				this.cntPos.goTo(where);
				map.capture(toCapture,this);
				break;
				
			default:
				throw new InvalidMoveException(
							"can't move pawn from" 
							+ this.cntPos + "to" + where + "\n"
						);
		}
	}
	
	@Override
	public void moveTo(Position where) throws InvalidMoveException {
		
		if(Math.abs(cntPos.rowDist(where)) == 2) {
			this.made2SquareOpen = true;
		}
		
		this.enpassantCapture = getEnpassant(where);
		
		try {
			super.moveTo(where);
			
			if(enpassantCapture != null) {
				System.out.println("enpassanting "  + enpassantCapture); //DEBUG
				Map map = AppRegistry.getMap();
				map.capture(enpassantCapture,null); //we do not want our pawn to be placed on that square
			}
			
			
		} catch(InvalidMoveException e) {
			this.made2SquareOpen = false; //cleanup state
			throw e; //rethrow
		}
		
	}
	
	
	private Pawn getEnpassant(Position where) {
		if((cntPos.row == 3 && color == Color.BLACK) || 
		   (cntPos.row == 4 && color == Color.WHITE)) {
			//might be a possible en-passant capture
			Position pawnPos = where.clone();
			
			try {
				pawnPos.translate(color == Color.WHITE ? -1 : 1, 0); //put where captured pawn should be
			} catch(InvalidPositionException e) {
				//really can't happen... but just in case
				System.out.println("it appears you pawn tries some invalid en-passant moves" + e.getMessage());
			}
			
			Map map = AppRegistry.getMap();
			Piece possiblePawn = map.getPieceAt(pawnPos);
			
			if(possiblePawn instanceof Pawn && 
				possiblePawn.color != this.color &&
				((Pawn)possiblePawn).isEnpassantTarget()) {
				return (Pawn)possiblePawn; //our capture target is here!
			}
					
		}
		return null;
	}


	@Override
	public boolean moveTypePermitted(Position where) {
		//Black has home row 7 and White has home row 0
		//thus black pawns move downward and white ones upward
		//we store this information in the dir variable
		int dir = (this.color == Color.WHITE ? 1 : -1);
		
		//set up calculations
		int targetRow = this.cntPos.row + dir; 
		int colDist = cntPos.colDist(where);
		colDist = Math.abs(colDist); //make positive
		
		
		
		//opening 2-square move for white pawn
		if(where.row - this.cntPos.row == 2) {
			if(colDist == 0 &&
				this.color == Color.WHITE && this.cntPos.row == 1) {
				return true;
			} else {
				return false;
			}
		}
		
		//opening 2-square move for black pawn
		if(where.row - this.cntPos.row == -2) {
			if(colDist == 0 &&
				this.color == Color.BLACK && this.cntPos.row == 6) {
				return true;
			} else {
				return false;
			}
		}
		
		
		if(colDist <= 1 && targetRow == where.row) {
			
			Map map = AppRegistry.getMap();
			
			switch(colDist) {
			case 0: //we stay on same column, must be clear ahead to move pawn.
				if( map.getPieceAt(where) == null) {
					//our road is clear
					return true;
					
				} else {
					return false; //our road is blocked
				}
			case 1:  //we want to capture something with the pawn
				Piece toCapture = map.getPieceAt(where);
				//check for null and King : not candidates for capture
				if(toCapture == null) {
					if(toCapture instanceof King) return false;
					
					//might be en-passant capture
					if((cntPos.row == 3 && color == Color.BLACK) || 
					   (cntPos.row == 4 && color == Color.WHITE)) {
						//might be a possible en-passant
						Position pawnPos = where.clone();
						
						try {
							pawnPos.translate(color == Color.WHITE ? -1 : 1, 0); //put where captured pawn should be
						} catch(InvalidPositionException e) {
							//really can't happen... but just in case
							System.out.println("it appears you pawn tries some invalid en-passant moves" + e.getMessage());
						}
						
						Piece possiblePawn = map.getPieceAt(pawnPos);
						
						if(possiblePawn instanceof Pawn) {
							return ((Pawn) possiblePawn).isEnpassantTarget();
						}
					}
				}
				
				//check for color
				if(toCapture.color == this.color) {
					return false;
				}
			}
			
			
			
			return true;
		} else {
			return false;
		}
	
	}
	
	private boolean isEnpassantTarget() {
		return this.made2SquareOpen;
	}


	public boolean backwardsMoveTypePermitted(Position from) {
				//Black has home row 7 and White has home row 0
				//thus black pawns move downward and white ones upward
				//we store this information in the dir variable
				//but the undo move is done in opposite directions
				int dir = (this.color == Color.WHITE ? -1 : 1);
				
				//set up calculations
				int targetRow = this.cntPos.row + dir; 
				int colDist = from.col - this.cntPos.col;
				colDist = Math.abs(colDist); //make positive
				
				
				
				//opening 2-square move for white pawn
				if(from.row - this.cntPos.row == -2) {
					if(colDist == 0 &&
						this.color == Color.WHITE && this.cntPos.row == 3) {
						return true;
					} else {
						return false;
					}
				}
				
				//opening 2-square move for black pawn
				if(from.row - this.cntPos.row == 2) {
					if(colDist == 0 &&
						this.color == Color.BLACK && this.cntPos.row == 4) {
						return true;
					} else {
						return false;
					}
				}
				
				
				if(colDist <= 1 && targetRow == from.row) {
					
					Map map = AppRegistry.getMap();
					
					switch(colDist) {
					case 0: //we stay on same column, must be clear ahead to move pawn.
						if( map.getPieceAt(from) == null) {
							//our road is clear
							return true;
							
						} else {
							return false; //our road is blocked
						}
					
					default:
							//coldist == 0
							//we have captured something
							//road should be clear
						return true;
					}
					
					
				} else {
					return false;
				}
	}
	
	
	public void moveBackwardsTo(Position from) throws InvalidMoveException {
		try {
			boolean validTarget = isValidTarget(from);
			boolean movePermitted = backwardsMoveTypePermitted(from);
			boolean canSlide = canSlideTo(from);
			//canSlide to can only go horizontally or diagonally 
			//Knight overrides this and returns always true, as it has no
			//sliding problems. 
			
			
			if(validTarget && 
				canSlide && 
				movePermitted) {
				
				Map map = AppRegistry.getMap();
				map.movePiece(this, from);
				
				if(Math.abs(cntPos.rowDist(from)) == 2) {
					this.made2SquareOpen = false; //undo flag
				}
				
				this.cntPos.goTo(from);
				
				if(enpassantCapture != null) {
					enpassantCapture.replaceOnBoard(enpassantCapture.cntPos);
				}
								
			} else {
				throw new InvalidMoveException(
						((validTarget) ? "" : ("invalid target @" + from + '\n')) +
						((canSlide) ? "" : ("can't slide to " + from + '\n')) +
						((movePermitted) ? "" : ("can't legally move to " + from + '\n'))
						);
			}
		} catch(InvalidPositionException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		//test instantiation
		try {
			Pawn pawn = new Pawn(Piece.Color.BLACK,6,0);
			Position pointer = new Position(5,1);
			pawn.moveTo(pointer); //move pawn forward 
			System.out.println("Pawn safely arrived @ " + pawn.cntPos);
			
		} catch( Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * overrides super.canAttack because our pawn cannot move where it can attack if there is no piece to attack
	 * implicit super implementation is to call this.moveTypePermitted. 
	 */
	@Override 
	public boolean canAttack(Position where) {
		int rowDir = (this.color == Piece.Color.WHITE) ? 1 : -1; //move up or down the board?
		

		return rowDir == this.cntPos.rowDist(where) 
				&& Math.abs(this.cntPos.colDist(where)) == 1; //pawn capture type move
	
	}

	@Override
	public char toChar() {
		//Unicode 
		return (this.color == Color.WHITE) ? (char)0x2659 : (char)0x265F;
	}
	
	@Override
	public Pawn clone() {
			
			Pawn clone = (Pawn) super.clone();
			clone.enpassantCapture = (enpassantCapture != null) ? enpassantCapture.clone() : null;
			//pawn is either on board or captured via enpassant. no duplicate pawns created.s
				
			return clone;
			
		}
	
}
