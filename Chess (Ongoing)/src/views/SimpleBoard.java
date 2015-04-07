package views;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;


import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

import board.InvalidPositionException;
import board.Map;
import board.Position;

import pieces.Piece;
import pieces.PieceList;

import controller.AppRegistry;
import controller.Controller;
import controller.Executor;
import controller.Move;

public class SimpleBoard extends JFrame {
	
	/**
	 * TODO pieces contructed by reflection sometimes throw Illegal number of args exception.
	 * See what that's all about.
	 */
	
	
	public static final SimpleBoard instance = new SimpleBoard();
	private static Controller status = new Controller();
	
	public SimpleBoard() {
		initUI();
	}
	
	public void initUI() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.setLayout(new GridLayout(9,8,0,0));
		
		
		Map map = AppRegistry.getMap();
		Position crawler = AppRegistry.getMutablePosition(7,0);
		
		for(int row = 0; row < 8; row++) {
			for(int col = 0; col < 8; col++) {
				Piece p = map.getPieceAt(crawler);
				
				ChessButton currentBtn = new ChessButton( p != null ? IconFactory.getIcon(p) : null,
											crawler.row, crawler.col
										);
				//create checkered pattern
				currentBtn.setBackground( (crawler.col + crawler.row)% 2 == 0 ? Color.GRAY : Color.WHITE);
				
				currentBtn.addMouseListener(new MoveDispatcher());
		
				panel.add(currentBtn);
				
				try {
					crawler.translate(0,1);
				} catch(InvalidPositionException e) {
					//can't happen
				}
			}
			
			try {
				crawler.translate(-1,-7);
			} catch(InvalidPositionException e) {
				//can't happen
			}
		}
		
		JLabel toMoveIndicator = new JLabel(Controller.getCurrentPlayer().toString());
		panel.add(toMoveIndicator);
		
		for(int i = 0; i < 2; i++) {
			//add blank space to center undo/redo buttons
			panel.add(new JLabel(""));
		}
		
		JButton undoBtn = new JButton("undo");
		
		undoBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				Controller.undoTurnActions();
				SimpleBoard.instance.refreshGUI();
			}
		});
		
		JButton redoBtn = new JButton("redo");
		
		redoBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				Controller.executeTurnActions();
				SimpleBoard.instance.refreshGUI();
			}
		});
		
		
		
		panel.add(undoBtn);
		panel.add(redoBtn);

		
		add(panel);
				

		setTitle("Assisted Chess");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(600,600);
		setLocationRelativeTo(null);
		
	}
	
	
	
	
	public synchronized void refreshGUI() {
		List<Component> allComps = getAllComponents(this);
		for(Component cmp : allComps) {
			if(cmp instanceof ChessButton) {
				ChessButton chBtn = (ChessButton) cmp;
				Piece p = AppRegistry.getMap().getPieceAt(chBtn.row,chBtn.col);
				
				chBtn.setIcon(p == null ? null : IconFactory.getIcon(p));
			}
			
			if(cmp instanceof JLabel && !((JLabel) cmp).getText().equals("")) {
				((JLabel) cmp).setText(Controller.getCurrentPlayer().toString());
			}
		}
		
		//check for wins
		Controller.Player winner = Controller.getWinner();
		
		if(winner != null) {
			JFrame winFrame = new JFrame();
			JPanel winPanel = new JPanel();
			JLabel winLabel = new JLabel(winner.toString().toLowerCase() + " wins!");
			
			winLabel.setSize(150,20);
			winPanel.add(winLabel);
			winFrame.add(winPanel);
			winFrame.setVisible(true);
			winFrame.setLocationRelativeTo(SimpleBoard.instance);
			winFrame.setSize(150, 20);
			winFrame.setAlwaysOnTop(true);
			winFrame.pack();
		}
		
		Piece pawn = Controller.pawnPromotionDue();
		if(pawn != null) {
			JFrame promFrame = new JFrame();
			JPanel promPanel = new JPanel();
			promPanel.setLayout(new GridLayout(1,4,0,0));
			
			ArrayList<ImageIcon> choices = IconFactory.getPromotionSet(pawn.getColor());
			
			for(int i = 0; i < choices.size(); i++) {
				JButton choiceBtn = new JButton();
				choiceBtn.setIcon(choices.get(i));
				choiceBtn.addMouseListener(new PromotionMoveDispatcher());
				promPanel.add(choiceBtn);
				
			}	
			
			promFrame.setTitle("Choose pawn promotion type:");
			promFrame.add(promPanel);
			promFrame.pack();
			promFrame.setLocationRelativeTo(SimpleBoard.instance);
			promFrame.setVisible(true);
			
		}
		
		
	}
	
	public static List<Component> getAllComponents(final Container c) {
	    Component[] comps = c.getComponents();
	    List<Component> compList = new ArrayList<Component>();
	    for (Component comp : comps) {
	        compList.add(comp);
	        if (comp instanceof Container)
	            compList.addAll(getAllComponents((Container) comp));
	    }
	    return compList;
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				SimpleBoard.instance.setVisible(true);
			}
		});

	}

}
