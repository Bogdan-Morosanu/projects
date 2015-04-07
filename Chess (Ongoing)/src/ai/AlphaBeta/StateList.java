package ai.AlphaBeta;

import java.util.ArrayList;

import controller.Move;

import ai.GameState;

public class StateList extends ArrayList<GameState> {

	public final double finalStateValue;
	
	StateList(GameState finalState, double finalValue) {
		super();
		add(finalState);
		this.finalStateValue = finalValue;
	}
	
	public ArrayList<Move> toMoveList() {
		ArrayList<Move> moveList = new ArrayList<Move>();
		
		for(int i = this.size() - 1; i > 0; i--) {
			//transition between consecutive states
			moveList.add(
				this.get(i).getTransitionMove(this.get(i-1))
			);
		}
		
		return moveList;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
