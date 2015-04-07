package ai.AlphaBeta;

import ai.BitModel.BitBoard;

public class TestTree {
	
	public long stateNum = 0;
	public long height = 0;
	
	private BitBoard root;
	
	public TestTree(BitBoard board) {
		this.root = board;
	}
	
	public void expandToLevel(BitBoard board, int lvl) {
		if(lvl == 0){
			return;
		}
		
		for(BitBoard child : board.children()) {
			
			expandToLevel(child,lvl - 1);
		}
		
		stateNum += board.children().length;
	}
	
	
	
	public BitBoard generateDFSCheck() {
		BitBoard cntBoard = root;
		
		while(cntBoard.children().length > 0) {
			height++;
			cntBoard = cntBoard.children()[
			             (int)(Math.random() * cntBoard.children().length)
			           ];
		}
		
		return cntBoard;
	}
	
	
	
	
	public static void main(String[] args) {
		
	}

}
