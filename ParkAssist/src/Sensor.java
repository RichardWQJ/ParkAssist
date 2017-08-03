import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Sensor {
	
	//Position relative to top left corner of the car
	double relativeX, relativeY;
	
	//Angle relative to the car when car.angle == 0
	double relativeAngle;
	//Range, in px
	double range = ParkAssist.cmToPx(50);
	
	public Sensor(double relativeX, double relativeY, double relativeAngle) {
		this.relativeX = relativeX;
		this.relativeY = relativeY;
		this.relativeAngle = relativeAngle;
	}
	
	public Sensor(double relativeX, double relativeY, double relativeAngle, double dist) {
		this.relativeX = relativeX;
		this.relativeY = relativeY;
		this.relativeAngle = relativeAngle;
		this.range = dist;
	}
	
	/**
	 * Detect nearest obstacle of the sensor.
	 * 
	 * @param car - The car containing the sensor
	 * @param obstacles - A list of cars that the sensor must check
	 * @return range+1 if the sensor doesn't detect anything in its range.
	 * Else, the distance (in px) between the sensor and the nearest obstacle.
	 */
	public Double detect(Car car, Car[] obstacles) {
		
		double xSensor = car.posX + Math.cos(Math.toRadians(car.getAngle()))*this.relativeX + Math.sin(Math.toRadians(car.getAngle()))*this.relativeY;
		double ySensor = car.posY - Math.sin(Math.toRadians(car.getAngle()))*this.relativeX + Math.cos(Math.toRadians(car.getAngle()))*this.relativeY;
		double xSensor2 = xSensor + Math.cos(Math.toRadians(this.relativeAngle-car.getAngle()))*ParkAssist.cmToPx(this.range);
		double ySensor2 = ySensor + Math.sin(Math.toRadians(this.relativeAngle-car.getAngle()))*ParkAssist.cmToPx(this.range);
		//System.out.println("xsensor2 = "+xSensor2 + ", ysensor2 = " + ySensor2);
		
		ArrayList<Double> distances = new ArrayList<Double>();
		
		for (int i = 0; i < obstacles.length; i++) {
			double x0 = obstacles[i].posX;
			double y0 = obstacles[i].posY;
			double x1 = Math.cos(Math.toRadians(obstacles[i].getAngle()))*obstacles[i].width;
			double y1 = Math.sin(Math.toRadians(obstacles[i].getAngle()))*obstacles[i].width;
			double x2 = Math.sin(Math.toRadians(obstacles[i].getAngle()))*obstacles[i].height;
			double y2 = Math.cos(Math.toRadians(obstacles[i].getAngle()))*obstacles[i].height;
			double[] segments = {
					x0, y0, x0+x1, y0-y1,
					x0, y0, x0+x2, y0+y2,
					x0+x1, y0-y1, x0+x1+x2, y0+y2-y1,
					x0+x2, y0+y2, x0+x1+x2, y0+y2-y1,
			};
			for (int j = 0; j < segments.length; j+=4) {
				double[] inter = calcIntersection(xSensor, ySensor, xSensor2, ySensor2, segments[j], segments[j+1], segments[j+2], segments[j+3]);
				if (inter != null) {
					distances.add(Math.sqrt(Math.pow(xSensor-inter[0], 2)+(Math.pow(ySensor-inter[1], 2))));
				}
			}
		}
		//System.out.println(distances);
		
		if (distances.size() == 0) {
			return this.range+1;
		} else {
			Collections.sort(distances);
			//System.out.println(distances.get(0));
			return distances.get(0);
		}
		
	}
	
	/**
	 * Given two segments [AB] and [CD], calculate and return their intersection E.
	 * Return null if they don't intersect.
	 */
	
	public double[] calcIntersection(double xa, double ya, double xb, double yb, double xc, double yc, double xd, double yd) {
		

		//Calculate equations of (AB) and (CD) of the form y=mx+p
		Double m1 = null, p1 = null, m2 = null, p2 = null;
		if (xb != xa) {
			m1 = (yb-ya)/(xb-xa);
			p1 = ya-m1*xa;
		}
		if (xd != xc) {
			m2 = (yd-yc)/(xd-xc);
			p2 = yc-m2*xc;
		}
		
		//Find their intersection
		Double xinter = null, yinter = null;
		if (m1 == null && m2 == null) {
			//Both lines are parallel to the y-axis so there is either no intersection, or a line intersection
			return null;
		} else if (m1 == null) {
			xinter = xa;
			yinter = m2*xinter+p2;
		} else if (m2 == null) {
			xinter = xc;
			yinter = m1*xinter+p1;
		} else {
			if (m1 == m2) {
				//segments (and lines) are parallel, there is no intersection
				return null;
			}
			xinter = (p2-p1)/(m1-m2);
			yinter = (yc == yd ? yc : m1*xinter+p1);
		}
		
		//System.out.println("lines intersect at "+xinter+", "+yinter);
		
		//Now, check if the intersection is on both segments
		if ((xa <= xinter && xinter <= xb || xa >= xinter && xinter >= xb) &&
				(ya <= yinter && yinter <= yb || ya >= yinter && yinter >= yb) &&
				(xc <= xinter && xinter <= xd || xc >= xinter && xinter >= xd) &&
				(yc <= yinter && yinter <= yd || yc >= yinter && yinter >= yd)) {
			return new double[]{xinter, yinter};
		} else {
			return null;
		}
	}
	
	
	
}
