package view;

import javax.print.attribute.AttributeSet;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

@Deprecated
public class SimpleView extends JFrame {
	
	private JTextArea prompt = new JTextArea(15,60);
	
	public SimpleView() {
		initUI();
	}
	
	private void initUI() {
		JPanel panel = new JPanel();

		prompt.setText("Welcome to the Polynomial Interpreter 0.1\n>>");
		prompt.setCaretPosition( prompt.getText().length() );
		panel.add(prompt);
		
		add(panel);
		
		setTitle("Polyonomial Interpreter");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		setLocationRelativeTo(null);
		
		pack();
		
		setVisible(true);
		
		
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SimpleView v = new SimpleView();
	}
	


}
