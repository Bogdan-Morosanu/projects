package pieces;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ListIterator;

import controller.AppRegistry;
import board.Position;
import board.Map;
import board.InvalidMoveException;
import board.InvalidPositionException;

public abstract class Piece implements Cloneable {
	public enum Color { WHITE, BLACK;
		public String toString() {
			return (this == Color.WHITE ? "White" : "Black");
		}
		
		public Color getAdversary() {
			return (this == Color.WHITE ? BLACK : WHITE);
		}
	};

	protected Position cntPos;
	protected Color color;
	
	Piece(Color color, int row, int col) throws InvalidPositionException {
			this.cntPos = new Position(row,col);
			this.color = color;
	}
	
	public Piece(Color color, Position pos) {
		this.cntPos = pos.clone();
		this.color = color;
	}
	
	
	/**
	 * used for inferring colors on board setup
	 * @param pos
	 */
	public Piece(Position pos) { 
		this.cntPos = pos.clone();
		this.color = (pos.row < 3 ? Color.WHITE : Color.BLACK);
	}
	
	public Color getColor() { return this.color; }
	public Color getAdversaryColor() { return this.color.getAdversary(); }
	public Position getPosition() { return this.cntPos.clone(); }
	public void delete() { 
		//dummy
	}
	
	public boolean canMoveTo(Position pos) {
		boolean canMove = false; 
		try {
			canMove = canSlideTo(pos) && isValidTarget(pos) && moveTypePermitted(pos);
		} catch(InvalidPositionException e) {
			return canMove = false;
		}
		return canMove;
	}
	
	protected boolean canSlideTo(Position destination) throws InvalidPositionException {
		/**
		 * checks if road to destination is clear
		 * the last cell may be occupied by a piece
		 * of opposing color
		 * so long as that piece is not a King
		 * (kings cannot be taken)
		 * //canSlide to can only go horizontally or diagonally 
		 *	//Knight overrides this and returns always true, as it has no
		 *	//sliding problems. Thus we must rely on movePermitted to check
		 *	//for moves that try to move unequal amounts of rows and columns
		 */
		
		Map map = AppRegistry.getMap();
		try { 
			/** 
			 * we must ensure we do not include destination
			 * in the list of squares we slide over. 
			 * the destination square is handled by the @see isValidTarget() method
			 */
		
			
			PieceList list = map.getPieceLine(this.cntPos, destination);
			
			if(list == null) return true; //moves to adjacent
			
		
			
			for(int i = 0; i < list.size() - 1; i++) {
				if( list.get(i) != null)  {
					
					return false;
				}
			}
			
			
			
			
		} catch(InvalidPositionException e) {
			throw e.newFromExisting("slide-type piece movement line exceeds board\n");
		}
		
		//no exception, no pieces got, return true
		return true;
	}
	
	public boolean isValidTarget(Position where) {
		Map map = AppRegistry.getMap();
		Piece adversary = map.getPieceAt(where);
		
		if(adversary == null) {
			return true;
		} //nothing to capture
		
		if(adversary instanceof King) {
			return false;
		} //can't capture King
		
		if(adversary.color == this.color) {
			return false;
		} //can't capture own pieces
		
		return true;
	}
	
	public void moveTo(Position where) throws InvalidMoveException {
		try {
			boolean validTarget = isValidTarget(where);
			boolean movePermitted = moveTypePermitted(where);
			boolean canSlide = canSlideTo(where);
			//canSlide to can only go horizontally or diagonally 
			//Knight overrides this and returns always true, as it has no
			//sliding problems. 
			
			
			if(validTarget && 
				canSlide && 
				movePermitted) {
				
				Map map = AppRegistry.getMap();
				map.movePiece(this, where);
				
				this.cntPos.goTo(where);
				
				
			} else {
				throw new InvalidMoveException(
						((validTarget) ? "" : ("invalid target @" + where + '\n')) +
						((canSlide) ? "" : ("can't slide to " + where + '\n')) +
						((movePermitted) ? "" : ("can't legally move to " + where + '\n'))
						);
			}
		} catch(InvalidPositionException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Used to Undo moves.
	 * this method is overriden by pawns which cannot move backwards
	 * @param from
	 * @throws InvalidMoveException
	 */
	public void moveBackwardsTo(Position from) throws InvalidMoveException { 
		this.moveTo(from);
	}
	
	public void replaceOnBoard(Position where) {
		//TODO : pop from captured stack
		Map map = AppRegistry.getMap();
		map.movePiece(this, where);
	}
	
	public String toString() {
		return this.toChar() + "@" + cntPos.toString();
	}
	
	/**
	 * Is overriden only by King
	 * @param where
	 * @return
	 */
	public boolean canAttack(Position where) {
		return moveTypePermitted(where);
	}
	
	
	public static Piece createPiece(Class<? extends Piece> type, Color color, Position pos) {
		try {
			
			Constructor<? extends Piece> constructor = 
					type.getDeclaredConstructor(color.getClass(), pos.getClass());
			
			return constructor.newInstance(color,pos);
		
			
		} catch(NoSuchMethodException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public Piece clone() {
		try {
			
			Piece clone = (Piece)super.clone();
			clone.cntPos = cntPos.clone();
			
			return clone;
			//provides type safety for extending classes
		} catch(CloneNotSupportedException e) {
			//can't happen, Object supports Clone
			return null;
		}
	}
	
	abstract public boolean moveTypePermitted(Position where);
	abstract public void executeMove(Position where) throws InvalidMoveException;	
	abstract public char toChar(); //returns Unicode char representation of itself
	
}
