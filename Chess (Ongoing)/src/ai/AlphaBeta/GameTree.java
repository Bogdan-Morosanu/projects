package ai.AlphaBeta;

import java.util.ArrayList;

import controller.ExposedYourSelfToCheckException;
import controller.Move;
import controller.NotYourPieceException;

import ai.AiRegistry;
import ai.Evaluator;
import ai.GameState;
import ai.BitModel.BitBoard;
import ai.BitModel.BitMoveEngine;
import ai.BitModel.BitTranslator;
import ai.BitModel.WeightedCount;

public class GameTree {
	
	private static boolean DEBUG = true;	
	private static boolean DEBUG_CHECK = false;
	private static int MAX_LEVEL = 4;
	
	private static final double MINVAL_UNSET = -Double.MAX_VALUE;
	private static final double MAXVAL_UNSET = Double.MAX_VALUE;
	
	/**
	 * our placeholder for min player loss value should 
	 * be so huge that it is way above any
	 * conceivable board value, yet smaller than MAXVALL_UNSET, so that it
	 * is registered by our algorithm as a valid board value.
	 */
	private static final double MIN_LOSS_VAL = MAXVAL_UNSET / 10;
	
	/**
	 * our placeholder for max player loss value should 
	 * be so negative that it is way under any
	 * conceivable board value, yet bigger than MINVALL_UNSET, so that it
	 * is registered by our algorithm as a valid board value.
	 */
	private static final double MAX_LOSS_VAL = MINVAL_UNSET / 10;
	
	private GameState root;
	private Evaluator brains;
	
	public GameTree() {
		this(BitTranslator.getStateOfMap(), new WeightedCount());
	}
	
	public GameTree(GameState root, Evaluator brains) {
		this.root = root;
		this.brains = brains;
	}
	
	public Move getNext() {
		
		long start = System.currentTimeMillis();
		this.expand(MAX_LEVEL);
		if(DEBUG) System.out.println("game tree expansion done in " + 
							(System.currentTimeMillis() - start) + " miliseconds");
		
		start = System.currentTimeMillis();
		StateList list = this.step(MAX_LEVEL, true, this.root, 0, 0);
		if(DEBUG) System.out.println("game state evaluation done in " + 
				(System.currentTimeMillis() - start) + " miliseconds");

		
		return list.toMoveList().get(0);
		
	}
	
	private StateList step(int depth, boolean isMaxTurn, 
							GameState state, double alpha, double beta) {
		if(depth == 1) {
			//base case -- leaf evaluation 
			GameState finalState = (isMaxTurn) ? getMax(state.children()) : getMin(state.children());
			
			double value;
			if(finalState != null) {
			 value = brains.eval(finalState);
			} else {
				
			if(DEBUG) {
				System.out.println("checkmate detected @ " + state);
			}
			 value = (isMaxTurn) ? MAX_LOSS_VAL : MIN_LOSS_VAL;
			//if there is no state.children, current player has lost!
			}
			
			StateList list;
			
			if(finalState != null) {
				list = new StateList(finalState, value);
				list.add(state);
			} else {
				list = new StateList(state, value);				
			}
			
			return list;
			
		} else {
			
			//recursive case, evaluate lists created so far
			StateList[] candidates = new StateList[state.children().length];
			
			if(state.children().length == 0) {
				//apparently we have reached a leaf node quicker than expected
				if(DEBUG_CHECK) {
					System.out.println("@ depth" + depth + "\n" + state);
					System.out.println("this better be a checkmate : " +
							BitMoveEngine.isBitBoardInCheck(((BitBoard)state).sameReversePlayer()));
				}
				//return leaf evaluation 
				return step(1,isMaxTurn,state,alpha,beta);
			}
			
			for(int i = 0; i < state.children().length; i++) {
				candidates[i] = step(depth-1,!isMaxTurn,state.children()[i],alpha,beta);
			}
			
			StateList selectedList = (isMaxTurn) ? getMax(candidates) : getMin(candidates);
			selectedList.add(state);
			
			return selectedList;
		}
		
	}
	
	private StateList getMax(StateList[] stateLists) {
		double maxVal = -Double.MAX_VALUE;
		int maxPos = -1;
		
		
		
		for(int i = 0; i < stateLists.length; i++) {
			
			double cntVal = stateLists[i].finalStateValue;
			if(maxVal < cntVal) {
				maxPos = i;
				maxVal = cntVal;
			}
		}
		
		return stateLists[maxPos];
	}
	
	private GameState getMax(GameState[] states) {
		double maxVal = -Double.MAX_VALUE;
		int maxPos = -1;
		
		if(states.length == 0) {
			return null; //Max has lost
		}
		
		for(int i = 0; i < states.length; i++) {
			
			double cntVal = brains.eval(states[i]);

			if(maxVal < cntVal) {
				maxPos = i;
				maxVal = cntVal;
			}
		}
		
		return states[maxPos];
	}
	
	
	private StateList getMin(StateList[] stateLists) {
		double minVal = Double.MAX_VALUE;
		int minPos = -1;
		
		for(int i = 0; i < stateLists.length; i++) {
			
			double cntVal = stateLists[i].finalStateValue;
			if(minVal > cntVal) {
				minPos = i;
				minVal = cntVal;
			}
		}
		
		return stateLists[minPos];
	}
	
	
	private GameState getMin(GameState[] states) {
		double minVal = Double.MAX_VALUE;
		int minPos = -1;
		
		if(states.length == 0) {
			return null; //Min has lost
		}
		
		for(int i = 0; i < states.length; i++) {
			
			double cntVal = brains.eval(states[i]);
			if(minVal > cntVal) {
				minPos = i;
				minVal = cntVal;
			}
		}
		
		return states[minPos];
	}
	
	
	private void expand(int toLevel) {
		doExpansion(root,toLevel);
	}
	
	private void doExpansion(GameState current, int toLevel) {
		if(toLevel == 0) {
			//base case
			return;
		}
		
		//recursion
		for(GameState nextState : current.children()) {
			doExpansion(nextState, toLevel - 1);
		}
	}
	
	public static void main(String[] args) {
		GameTree game = new GameTree(BitTranslator.getStateOfMap(), new WeightedCount());
		
		long start = System.currentTimeMillis();
		game.expand(5);
		System.out.println("game tree expansion done in " + 
							(System.currentTimeMillis() - start) + " miliseconds");
		
		
		StateList list = game.step(5, true, game.root, 0, 0);
		
		System.out.println(list.size());
		
		ArrayList<Move> moveList = list.toMoveList();
		
		for(Move m : moveList) {
			System.out.println("Executing :\n" + m);
			/*
			try {
				m.execute();
			} catch(ExposedYourSelfToCheckException e) {
				System.out.println(e.getMessage());
				break;
			} catch(NotYourPieceException e) {
				System.out.println(e.getMessage());
				break;
			}
			*/
		}
	}

}




















