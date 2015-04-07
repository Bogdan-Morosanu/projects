package controller;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


import ai.AlphaBeta.GameTree;
import board.Map;
import board.Position;

/**
 * Executor deals with executing moves, undo and redo when controller calls.
 * @author moro
 *
 */
public class Executor {
	private static final String DEF_MOVE_PATH = "./movlist";
	private static ArrayList<Move> moveList = new ArrayList<Move>();	
	private static boolean isVisible = true;
	private static int moveCounter = 0;
	
	static {
		Executor.loadMoveList(DEF_MOVE_PATH);
	}
	
	private static void loadMoveList(String path) {
		BufferedReader conf = null;
		try {
			conf = new BufferedReader(new FileReader(DEF_MOVE_PATH));
			
			String s;
			while((s = conf.readLine()) != null) {
				parseMove(s);
			}		
		
		
		} catch(FileNotFoundException e) {
			//TODO : System.out.println("Can't find move list @ " );;
			
			System.out.println("can't find move list");
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			if(conf != null) {
				try {
					conf.close();
				} catch (IOException e) {
					// nothing to do here
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * parses moves according to move formatted String.
	 * a move format is [0,1] to [0,2]
	 * the only actual needed information is that
	 * the rows and cols must occupy positions 
	 * 1, 3 for start and 10, 12 for destination  
	 * @param move move formatted String
	 */
	
	private static void parseMove(String move) {
		
		int rowFrom = move.charAt(1) - '0';
		int colFrom = move.charAt(3) - '0';
		
		int rowTo = move.charAt(10) - '0';
		int colTo = move.charAt(12) - '0';
		
		
		Position from = AppRegistry.getMutablePosition(rowFrom,colFrom);
		Position to = AppRegistry.getMutablePosition(rowTo,colTo);
		
		moveList.add(new Move(from,to));
	}
	
	public static void executeAll() {
		for(Move mv : moveList) {
			if(isVisible) {
				System.out.println(mv + "...\n");
			}
			
			try {
				mv.execute();
			} catch(NotYourPieceException e) {
				//DEBUG purposes TODO extract when ready
				System.out.println(e.getMessage());
			} catch(ExposedYourSelfToCheckException e) {
				
				System.out.println(e.getMessage() + "\nfatal error, move execution halted");
			}
			
			if(isVisible) {
				System.out.println(AppRegistry.getMap());
			}
		}
	}
	
	
	public static void loadAIMove() {

		GameTree gameTree = new GameTree();
		Move next = gameTree.getNext();
		
		System.out.println(next);
		
		if(next != null) {
			moveList.add(next);		
		}
	}
	
	
	public static boolean executeNext() {
		boolean status = false;
		
		if(moveCounter != moveList.size()) {
			Move mv = moveList.get(moveCounter);
			
			if(isVisible) {
				System.out.println(mv);
			}
			
			try {
				status = mv.execute();
			} catch(NotYourPieceException e) {
				//DEBUG purposes TODO extract when ready
				System.out.println(e.getMessage());
				System.out.println("problematic move follows");
				System.out.println(mv);
			} catch(ExposedYourSelfToCheckException e) {
				//must discard the last move
				discardLast();
			}
			

			moveCounter++;
			
			if(isVisible) {
				System.out.println(AppRegistry.getMap());
			}
		}
		
		return status; 
	}
	
	public static boolean undoLast() {
		boolean status = false;
		if(moveCounter > 0) {
			moveCounter--; //go back first
			
			Move mv = moveList.get(moveCounter);
			
			if(isVisible) {
				System.out.println("undo called on " + mv);
			}

			
			status = mv.undo();

			
			if(isVisible) {
				System.out.println(AppRegistry.getMap());
			}
		}
		
		return status;
	}
	
	private static void discardLast() {
		moveList.remove(moveList.size() - 1);
		
		if(moveCounter > moveList.size()) {
			moveCounter = moveList.size();
		}
	}
	
	public static boolean finishedExecuting() {
		return moveCounter == moveList.size();
	}
	
	public static void showMethod(Map map) {
		//to be use for graphical rendering
		//System.out.println(map);
	}
	
	public static void setChangesVisible(boolean isVisible) {
		Executor.isVisible = isVisible;
	}
	
	public static boolean addMove(Move mv) {
		//first we must clear our undo stack
		
		for(int i = moveList.size() - 1; i >= moveCounter; i--) {
			moveList.remove(i);
		}
		
		return moveList.add(mv);
	}
	
	public static void undoAll() {
		while(undoLast());
	}
	
	public static boolean finishedUndoing() {
		return moveCounter == 0;
	}
	
	public static void main(String[] args) {
		setChangesVisible(true);
		
		
	}

}
