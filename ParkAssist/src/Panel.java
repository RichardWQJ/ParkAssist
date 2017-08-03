import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
 
public class Panel extends JPanel {
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		for (int i = 0; i < ParkAssist.cars.size(); i++) {
			drawCar(g, ParkAssist.cars.get(i), i);
		}
		
	}
   
	public void drawCar(Graphics g2, Car car, int carNb) {
		Graphics2D g = (Graphics2D) g2;
		//draw the cars
		g.setColor(Color.RED);
		g.setStroke(new BasicStroke(2));
		//(x0,y0) is the top left corner of the car (car facing up)
		double x0 = car.posX;
		double y0 = car.posY;
		//(x0+x1, y0-y1) is the top right corner of the car
		double x1 = Math.cos(Math.toRadians(car.getAngle()))*car.width;
		double y1 = Math.sin(Math.toRadians(car.getAngle()))*car.width;
		//(x0+x2, y0+y2) is the bottom left corner of the car
		double x2 = Math.sin(Math.toRadians(car.getAngle()))*car.height;
		double y2 = Math.cos(Math.toRadians(car.getAngle()))*car.height;
	   
		g.drawLine((int)x0, (int)y0, (int)(x0+x1), (int)(y0-y1));
		g.drawLine((int)x0, (int)y0, (int)(x0+x2), (int)(y0+y2));
		g.drawLine((int)(x0+x2), (int)(y0+y2), (int)(x0+x2+x1), (int)(y0+y2-y1));
		g.drawLine((int)(x0+x1), (int)(y0-y1), (int)(x0+x1+x2), (int)(y0-y1+y2));
	   
		//draw front wheels
		g.setStroke(new BasicStroke(4));
		g.setColor(Color.BLUE);
	   
		//determine coordinates of the center of the wheels
		double xcenter = car.posX+Math.sin(Math.toRadians(car.getAngle()))*(car.distanceFromFront);
		double ycenter = car.posY+Math.cos(Math.toRadians(car.getAngle()))*(car.distanceFromFront);
		//draw top half and bottom half of the wheel
		g.drawLine((int)(xcenter+Math.sin(Math.toRadians(car.getAngle()-car.getWheelAngle()))*(car.wheelLength/2)), 
				(int)(ycenter+Math.cos(Math.toRadians(car.getAngle()-car.getWheelAngle()))*(car.wheelLength/2)), 
				(int)(xcenter-Math.sin(Math.toRadians(car.getAngle()-car.getWheelAngle()))*(car.wheelLength/2)), 
				(int)(ycenter-Math.cos(Math.toRadians(car.getAngle()-car.getWheelAngle()))*(car.wheelLength/2)));
		double xcenter2 = xcenter+x1;
		double ycenter2 = ycenter-y1;
		g.drawLine((int)(xcenter2+Math.sin(Math.toRadians(car.getAngle()-car.getWheelAngle()))*(car.wheelLength/2)), 
				(int)(ycenter2+Math.cos(Math.toRadians(car.getAngle()-car.getWheelAngle()))*(car.wheelLength/2)),
				(int)(xcenter2-Math.sin(Math.toRadians(car.getAngle()-car.getWheelAngle()))*(car.wheelLength/2)), 
				(int)(ycenter2-Math.cos(Math.toRadians(car.getAngle()-car.getWheelAngle()))*(car.wheelLength/2)));
	   
		//draw back wheels, this is easier because of the 0° angle
		g.setColor(Color.GREEN);
		double xBackWheel = car.posX+Math.sin(Math.toRadians(car.getAngle()))*(car.height-car.distanceFromBack-car.wheelLength/2);
		double yBackWheel = car.posY+Math.cos(Math.toRadians(car.getAngle()))*(car.height-car.distanceFromBack-car.wheelLength/2);
		double xBackWheel2 = car.posX+Math.sin(Math.toRadians(car.getAngle()))*(car.height-car.distanceFromBack+car.wheelLength/2);
		double yBackWheel2 = car.posY+Math.cos(Math.toRadians(car.getAngle()))*(car.height-car.distanceFromBack+car.wheelLength/2);
		g.drawLine((int)xBackWheel, (int)yBackWheel, (int)xBackWheel2, (int)yBackWheel2);
		g.drawLine((int)(xBackWheel+x1), (int)(yBackWheel-y1), (int)(xBackWheel2+x1), (int)(yBackWheel2-y1));
		
		if (ParkAssist.debugMovement && carNb == 0) {
			g.setColor(new Color(100,100,100));
			//draw perpendicular line to front wheel
			double xfront, yfront;
			if ((car.getWheelAngle() % 180 + 180) % 180 > 90) { //left wheel is used
				xfront = xcenter;
				yfront = ycenter;
			} else { //right wheel is used
				xfront = xcenter2;
				yfront = ycenter2;
			}
			//calculate equation of line perpendicular to front wheel
			//System.out.println("angle for car " + carNb + " is " + ((car.wheelAngle % 180 + 180) % 180));
			double m = Math.tan(Math.toRadians(car.getWheelAngle()-car.getAngle()));
			double p = yfront-(xfront*m);
			//System.out.println("line equation for car " + carNb + " is y = "+m+"x + "+p);
			if (((car.getWheelAngle()-car.getAngle())%180+180)%180 != 90) { //if true, the wheel isn't perpendicular to the y axis
				g.drawLine(0, (int)p, this.getWidth(), (int)(this.getWidth()*m + p));
				//System.out.println((car.wheelAngle-car.angle)%180);
			} else {
				g.drawLine((int)xfront, 0, (int)xfront, this.getHeight());
			}
			
			//calculate equation of line perpendicular to back wheel
			g.setColor(new Color(0xFF, 0xA7, 0x00));
			//no need to check which wheel is used because they are always parallel to each other
			double xback = car.posX+(Math.sin(Math.toRadians(car.getAngle()))*(car.height-car.distanceFromBack));
			double yback = car.posY+(Math.cos(Math.toRadians(car.getAngle()))*(car.height-car.distanceFromBack));
			double m2 = Math.tan(Math.toRadians(90-car.getAngle()-90));
			double p2 = yback - (xback * m2);
			if (((car.getAngle())%180+180)%180 != 90) {
				g.drawLine(0, (int)p2, this.getWidth(), (int)(this.getWidth()*m2 + p2));
			} else {
				g.drawLine((int)xback, 0, (int)xback, this.getHeight());
			}
			
			//now, find when these lines intersect, if the wheel angle is 0 deg then it is a special case
			g.setColor(Color.green);
			double xinter, yinter;
			if ((car.getWheelAngle()%180+180)%180 != 0) {
				if (((car.getWheelAngle()-car.getAngle())%180+180)%180 != 90) {
					xinter = (p2-p)/(m-m2);
					yinter = m*xinter+p;
				} else {
					xinter = xfront;
					yinter = m2*xinter+p2;
					//System.out.println("Lines intersect at x= " + xinter + ", y= " + yinter);
				}
				g.drawOval((int)xinter, (int)yinter, 3, 3);
				double radius = Math.sqrt(Math.pow((xinter-xfront),2) + Math.pow(yinter-yfront, 2));
				g.drawOval((int)(xinter-radius), (int)(yinter-radius), (int)(radius*2), (int)(radius*2));
				double theta = 10/radius;
				
				//calculate polar coordinates
				double theta2 = truemod(Math.atan2(xfront-xinter, yfront-yinter), 2*Math.PI); //polar coordinates of front wheel when center of circle = (0,0)
				//System.out.println("A' = " + (xfront-xinter) + ", " + (yfront-yinter));
				//System.out.println("A' = " + radius*Math.sin(theta2) + ", " + radius*Math.cos(theta2));
				double xFirstSol = xinter+radius*Math.sin(theta2-theta);
				double yFirstSol = yinter+radius*Math.cos(theta2-theta);
				double xSecondSol = xinter+radius*Math.sin(theta2+theta);
				double ySecondSol = yinter+radius*Math.cos(theta2+theta);
				g.setColor(Color.MAGENTA);
				g.drawOval((int)(xFirstSol), (int)(yFirstSol), 3, 3);
				g.setColor(Color.BLACK);
				g.drawOval((int)(xSecondSol), (int)(ySecondSol), 3, 3);
				//System.out.println("theta = " + Math.toDegrees(theta) + ", theta2 = " + Math.toDegrees(theta2));
			} else {
				//It is really simple: move along the line parallel to the car
				//determine equation of the line
				double m_ = y2/x2; //note that (y2;x2) is a vector
				double p_ = y0-m_*x0;
				//determine the 2 points such that the distance to (x0;y0) is 10
				//solve x^2+y^2=1 with y=mx+p, you will come to this
				//not as simple as I thought tho
				double firstSolution = -(Math.sqrt(100))/(Math.sqrt(Math.pow(m_, 2)+1));
				double secondSolution = (Math.sqrt(100))/(Math.sqrt(Math.pow(m_, 2)+1));
				//now determine which solution it must take
				if (truemod(car.getAngle(), 180) != 0) {
					g.setColor(Color.BLACK);
					g.drawOval((int)(x0+firstSolution), (int)(y0+(firstSolution*m_)), 3, 3);
					g.setColor(Color.MAGENTA);
					g.drawOval((int)(x0+secondSolution), (int)(y0+(secondSolution*m_)), 3, 3);
				}
				//g.setColor(Color.CYAN);
				//g.drawOval((int)(x0+x2), (int)(y0+y2), 3, 3);
			}
		}
		
		if (ParkAssist.debugParking && carNb == 0) {
			for (int i = 0; i < car.getSensors().size(); i++) {
				Sensor sensor = car.getSensors().get(i);
				//(x0+x1, y0-y1) is the top right corner of the car
				double xSensor = x0 + Math.cos(Math.toRadians(car.getAngle()))*sensor.relativeX + Math.sin(Math.toRadians(car.getAngle()))*sensor.relativeY;
				double ySensor = y0 - Math.sin(Math.toRadians(car.getAngle()))*sensor.relativeX + Math.cos(Math.toRadians(car.getAngle()))*sensor.relativeY;
				double xSensor2 = xSensor + Math.cos(Math.toRadians(sensor.relativeAngle-car.getAngle()))*sensor.range;
				double ySensor2 = ySensor + Math.sin(Math.toRadians(sensor.relativeAngle-car.getAngle()))*sensor.range;
				//System.out.println("sensor pos: "+ xSensor + ", " + ySensor);
				g.setColor(Color.CYAN);
				g.drawLine((int)xSensor, (int)ySensor, (int)xSensor2, (int)ySensor2);
				Car[] obstacles = ParkAssist.cars.subList(1, ParkAssist.cars.size()).toArray(new Car[ParkAssist.cars.size()-1]);
				Double detectDist = sensor.detect(car, obstacles);
				g.setColor(Color.BLACK);
				if (detectDist <= sensor.range) {
					g.drawOval((int)(xSensor + Math.cos(Math.toRadians(sensor.relativeAngle-car.getAngle()))*detectDist),
							(int)(ySensor + Math.sin(Math.toRadians(sensor.relativeAngle-car.getAngle()))*detectDist), 3, 3);
				}
			}
		}
		
	}
	
	public double truemod(double d, double mod) {
		return (d%mod+mod)%mod;
	}
   
}