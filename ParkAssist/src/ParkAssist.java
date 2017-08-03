import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
 
enum Direction {
	FORWARDS,
	BACKWARDS,
	LEFT,
	RIGHT
}

public class ParkAssist {
	
	
	//The width between the cars.
	public static final int DIST_BTW_CARS = (int) cmToPx(6);
	public static final double USER_CAR_WIDTH = cmToPx(19.8);
	public static final double USER_CAR_HEIGHT = cmToPx(44);
	public static int selectedImg = 0;
	static JFrame window = new JFrame();
	static Sidebar sidebar;
	static ArrayList<Car> cars = new ArrayList<Car>();
	static Car pavement = new Car(); //pavement is modelized by a car
	static int movingSpeed = 5;
	public static boolean debugMovement = false;
	public static boolean debugParking = true;
	//public static double xpark = 100;
	//public static double ypark = 200;
	
	public static void main(String[] args) {
		//System.out.println("Fix the algorithm");
		//System.exit(0);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
				
		Car car1 = new Car();
		System.out.println(car1.distanceFromFront);
		Car car2 = new Car();
		car2.setPos(500, 200);
		car2.setAngle(0);
		car2.setWheelAngle(0);
		Car car3 = new Car();
		car3.setPos(500, (int)(USER_CAR_HEIGHT+ParallelParking.SECURITY_DIST+2+300));
		car3.setAngle(0);
		car3.setWheelAngle(0);
		
		cars.add(car1);
		cars.add(car2);
		cars.add(car3);
	   
		for (int i = 1; i < cars.size(); i++) {
			cars.get(i).setDim(40, 100);
		}
		init();
		
		pavement.setPos((int)(500+USER_CAR_WIDTH+10), 0);
		pavement.setDim(50, 1000);
		pavement.setAngle(0);
		pavement.setWheelAngle(0);
		cars.add(pavement);
		
		//Sensors on right side
		getControllableCar().addSensor(new Sensor(getControllableCar().width, getControllableCar().distanceFromFront, 0));
		getControllableCar().addSensor(new Sensor(getControllableCar().width, getControllableCar().height-getControllableCar().distanceFromBack, 0));
		//Sensors on left side
		getControllableCar().addSensor(new Sensor(0, getControllableCar().distanceFromFront, 180));
		getControllableCar().addSensor(new Sensor(0, getControllableCar().height-getControllableCar().distanceFromBack, 180));
		//Top sensor
		getControllableCar().addSensor(new Sensor(getControllableCar().width/2, 0, -90));
		//Bottom sensor
		getControllableCar().addSensor(new Sensor(getControllableCar().width/2, getControllableCar().height, 90));
		

		Panel drawPanel = new Panel();
		window.setTitle("Park assist simulator");
		window.setVisible(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setSize(1200, 800);
		window.setLocationRelativeTo(null);
		sidebar = new Sidebar(new Dimension((int)(300), window.getHeight()));
		window.add(sidebar, BorderLayout.EAST);
		//window.setContentPane(mainPanel);
		//window.setResizable(false);
		drawPanel.setPreferredSize(new Dimension((int)(window.getWidth()), window.getHeight()));
		//window.add(panel, BorderLayout.WEST);
		//window.setContentPane(panel);
		window.add(drawPanel, BorderLayout.CENTER);
		//sidebar.setBackground(Color.LIGHT_GRAY);
		//System.out.println(drawPanel.getPreferredSize());
		ParkAssist.sidebar.updateData();

		window.setSize(window.getWidth()+10, window.getHeight()+10);
		window.setSize(window.getWidth()-10, window.getHeight()-10); //to force redraw
		
		char[] keys = {KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_NUMPAD8, KeyEvent.VK_NUMPAD2,  KeyEvent.VK_NUMPAD4,  KeyEvent.VK_NUMPAD6};
		String[] actions = {"moveForwards", "moveBackwards", "turnLeft", "turnRight", "moveUp", "moveDown", "moveLeft", "moveRight"};
		for (int i = 0; i < keys.length; i++) {
			drawPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keys[i], 0), actions[i]);
		}
		
		drawPanel.getActionMap().put("moveForwards", new AbstractAction() {
			@Override public void actionPerformed(ActionEvent e) {
				getControllableCar().move(Direction.FORWARDS, movingSpeed);
				window.repaint();
		}});
		drawPanel.getActionMap().put("moveBackwards", new AbstractAction() {
			@Override public void actionPerformed(ActionEvent e) {
				getControllableCar().move(Direction.BACKWARDS, movingSpeed);
				window.repaint();
		}});
		drawPanel.getActionMap().put("turnLeft", new AbstractAction() {
			@Override public void actionPerformed(ActionEvent e) {
				getControllableCar().setWheelAngle(getControllableCar().getWheelAngle() - 15);
				window.repaint();
		}});
		drawPanel.getActionMap().put("turnRight", new AbstractAction() {
			@Override public void actionPerformed(ActionEvent e) {
				getControllableCar().setWheelAngle(getControllableCar().getWheelAngle() + 15);
				window.repaint();
		}});
		drawPanel.getActionMap().put("moveUp", new AbstractAction() {
			@Override public void actionPerformed(ActionEvent e) {
				getControllableCar().posY -= 5;
				sidebar.updateData();
				window.repaint();
		}});
		drawPanel.getActionMap().put("moveDown", new AbstractAction() {
			@Override public void actionPerformed(ActionEvent e) {
				getControllableCar().posY += 5;
				sidebar.updateData();
				window.repaint();
		}});
		drawPanel.getActionMap().put("moveLeft", new AbstractAction() {
			@Override public void actionPerformed(ActionEvent e) {
				getControllableCar().posX -= 5;
				sidebar.updateData();
				window.repaint();
		}});
		drawPanel.getActionMap().put("moveRight", new AbstractAction() {
			@Override public void actionPerformed(ActionEvent e) {
				getControllableCar().posX += 5;
				sidebar.updateData();
				window.repaint();
		}});
		
		/*while (true) {
			for (int i = 0; i < 200; i++) {
				
				try {
					Thread.sleep(50);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				if (i < 100) {
					cars.get(0).move(Direction.FORWARDS, 5);
					//System.out.println("moving forwards");
				} else {
					cars.get(0).move(Direction.FORWARDS, 5);
					//System.out.println("moving backwards");
				}
				//cars.get(0).wheelAngle += 1;
				//System.out.println(truemod(cars.get(0).angle, 360));
				window.repaint();
			}
				
		}*/
	}
	public static double truemod(double d, int mod) {
		return (d%mod+mod)%mod;
	}
	
	public static Car getControllableCar() {
		return cars.get(0);
		//return pavement;
	}
	
	public static double cmToPx(double cm) {
		return cm*2.1;
	}
	
	public static void init() {
		getControllableCar().setPos((int)(500-USER_CAR_WIDTH-DIST_BTW_CARS), (int)(300+(ParallelParking.SECURITY_DIST+2)/2));
		getControllableCar().setDim(USER_CAR_WIDTH, USER_CAR_HEIGHT);
		getControllableCar().setAngle(0);
		getControllableCar().setWheelAngle(0);
		try {
			sidebar.output.setText("");
		} catch (NullPointerException e) {}
		window.repaint();
	}
 
}