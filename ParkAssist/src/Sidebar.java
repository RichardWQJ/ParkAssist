import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;

public class Sidebar extends JPanel {
	
	JLabel angle = new JLabel();
	JLabel posX = new JLabel();
	JLabel posY = new JLabel();
	JLabel wheelAngle = new JLabel();
	JLabel carWidth = new JLabel("Car height: " + ParkAssist.getControllableCar().width);
	JLabel carHeight = new JLabel("Car width: " + ParkAssist.getControllableCar().height);
	JLabel securityDist = new JLabel("Security distance: " + ParallelParking.SECURITY_DIST);
	JLabel distBtwCars = new JLabel("Distance between cars: " + ParkAssist.DIST_BTW_CARS);
	JPanel[] panels = new JPanel[10];
	JTextArea output = new JTextArea();
	
	public Sidebar(Dimension dimension) {
		this.setSize(dimension);
		this.setPreferredSize(dimension);
		this.setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, Color.GRAY));
		for (int i = 0; i < panels.length; i++) {
			//this.add(Box.createRigidArea(new Dimension(0, 10)));
			panels[i] = new JPanel();
			//panels[i].setBackground(new Color(255*i/panels.length, 255, 0));
			//panels[i].add(new JLabel("test" + i));
			panels[i].setPreferredSize(new Dimension(this.getWidth()-5, 30));
			panels[i].setOpaque(false);
			this.add(panels[i]);
		}
		
		JCheckBox checkBox1 = new JCheckBox("Debug movement");
		checkBox1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ParkAssist.debugMovement = ((JCheckBox)e.getSource()).isSelected();
				ParkAssist.window.repaint();
			}
		});
		panels[0].add(checkBox1);
		checkBox1.setSelected(ParkAssist.debugMovement);
		
		JCheckBox checkBox2 = new JCheckBox("Debug parking");
		checkBox2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ParkAssist.debugParking = ((JCheckBox)e.getSource()).isSelected();
				ParkAssist.window.repaint();
			}
		});
		panels[1].add(checkBox2);
		checkBox2.setSelected(ParkAssist.debugParking);
		
		JButton resetButton = new JButton("Reset angle");
		resetButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				ParkAssist.getControllableCar().setAngle(0);
				ParkAssist.getControllableCar().setWheelAngle(0);
				ParkAssist.window.repaint();
			}});
		panels[2].add(resetButton);
		
		JButton parkButton = new JButton("Start Park Assist");
		parkButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				Thread thread = new Thread(new Runnable() {
					public void run() {
						ParallelParking.parallelPark(ParkAssist.getControllableCar(), Direction.RIGHT);
					}
				});
				thread.start();
			}
		});
		panels[3].add(parkButton);
		
				
		panels[6].setLayout(new BoxLayout(panels[6], BoxLayout.Y_AXIS));
		panels[6].add(posX);
		panels[6].add(posY);
		panels[6].add(angle);
		panels[6].add(wheelAngle);
		panels[6].add(carWidth);
		panels[6].add(carHeight);
		panels[6].add(distBtwCars);
		panels[6].add(securityDist);
		panels[6].setPreferredSize(new Dimension(this.getWidth()-5, 150));
		
		JButton initButton = new JButton("Reset all");
		initButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent arg0) {
				ParkAssist.init();
			}
		});
		panels[7].add(initButton);
		
		//output.setEditable(false);
		output.setLineWrap(true);
		output.setWrapStyleWord(true);
		output.setBackground(Color.ORANGE);
		TextAreaOutputStream out = new TextAreaOutputStream(output);
        System.setOut(new PrintStream(out));
        panels[8].setPreferredSize(new Dimension(this.getWidth(), 300));
		output.setPreferredSize(new Dimension(this.getWidth(), 300));
		JScrollPane jsp = new JScrollPane(output);
		jsp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		panels[8].add(jsp);
		
		//panels[0].add(new JLabel("Activer le mode débug"));
	}
	
	public void updateData() {
		this.angle.setText("Car angle: " + ParkAssist.getControllableCar().getAngle());
		this.wheelAngle.setText("Wheel angle: " + ParkAssist.getControllableCar().getWheelAngle());
		this.posX.setText("x = " + ParkAssist.getControllableCar().posX);
		this.posY.setText("y = " + ParkAssist.getControllableCar().posY);
	}
}

class TextAreaOutputStream extends OutputStream {
    private JTextArea textControl;

    /**
     * Creates a new instance of TextAreaOutputStream which writes
     * to the specified instance of javax.swing.JTextArea control.
     *
     * @param control   A reference to the javax.swing.JTextArea
     *                  control to which the output must be redirected
     *                  to.
     */
    public TextAreaOutputStream( JTextArea control ) {
        textControl = control;
    }

    /**
     * Writes the specified byte as a character to the
     * javax.swing.JTextArea.
     *
     * @param   b   The byte to be written as character to the
     *              JTextArea.
     */
    public void write( int b ) throws IOException {
        // append the data as characters to the JTextArea control
        textControl.append( String.valueOf( ( char )b ) );
        textControl.setCaretPosition(textControl.getText().length());
    }  
}
