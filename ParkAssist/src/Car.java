import java.awt.Color;
import java.util.ArrayList;

public class Car {
	final int FRONT = 0;
	final int BACK = 1;
	double posX, posY;
	double width, height;
	private double angle;
	private double wheelAngle;
	double maxWheelAngle = 30; //close to actual value
	double wheelLength = 10;
	//distance from front/back to center of wheels
	double distanceFromFront = ParkAssist.cmToPx(8.7);
	double distanceFromBack = ParkAssist.cmToPx(9.8);
	private ArrayList<Sensor> sensors = new ArrayList<Sensor>();
   
	public Car() {
	}
   
	void setPos(int x, int y) {
		this.posX = x;
		this.posY = y;
	}
   
	void setDim(double width, double height) {
		this.width = width;
		this.height = height;
		this.distanceFromFront = (int)(0.1*height);
		this.distanceFromBack = (int)(0.1*height);
	}
	void setDim(int width, int height, int distFromFront, int distFromBack) {
		this.width = width;
		this.height = height;
		this.distanceFromFront = distFromFront;
		this.distanceFromBack = distFromBack;
	}
	void setAngle(double angle) {
		this.angle = truemod(angle,360);
		try {
			ParkAssist.sidebar.updateData();
		} catch (NullPointerException e) {}
	}
	
	double getWheelAngle() {
		return this.wheelAngle;
	}
	
	void setWheelAngle(double wheelAngle) {
		if (Math.abs(wheelAngle) <= this.maxWheelAngle) {
			this.wheelAngle = wheelAngle;
		} else {
			this.wheelAngle = this.maxWheelAngle * (wheelAngle < 0 ? -1 : 1);
		}
		try {
			ParkAssist.sidebar.updateData();
		} catch (NullPointerException e) {}
	}
	
	void addSensor(Sensor sensor) {
		this.sensors.add(sensor);
	}
	
	ArrayList<Sensor> getSensors() {
		return this.sensors;
	}
	
	Sensor getTopSensor() {return sensors.get(4);}
	Sensor getBottomSensor() {return sensors.get(5);}
	Sensor getTopRightSensor() {return sensors.get(0);}
	Sensor getBottomRightSensor() {return sensors.get(1);}
	Sensor getTopLeftSensor() {return sensors.get(2);}
	Sensor getBottomLeftSensor() {return sensors.get(3);}
	
	//Move by length units the car
	void move(Direction direction, double length) {
		//We're basically gonna redo the calculations in the Panel class
		
		//(x0,y0) is the top left corner of the car (car facing up)
		double x0 = this.posX;
		double y0 = this.posY;
		//(x0+x1, y0-y1) is the top right corner of the car
		double x1 = Math.cos(Math.toRadians(this.getAngle()))*this.width;
		double y1 = Math.sin(Math.toRadians(this.getAngle()))*this.width;
		//(x0+x2, y0+y2) is the bottom left corner of the car
		double x2 = Math.sin(Math.toRadians(this.getAngle()))*this.height;
		double y2 = Math.cos(Math.toRadians(this.getAngle()))*this.height;
		
		
		if ((this.wheelAngle%180+180)%180 != 0) {
			//determine coordinates of the center of the wheels
			double xcenter = this.posX+Math.sin(Math.toRadians(this.getAngle()))*(this.distanceFromFront);
			double ycenter = this.posY+Math.cos(Math.toRadians(this.getAngle()))*(this.distanceFromFront);
			//determine coordinates of the other front wheel
			double xcenter2 = xcenter+x1;
			double ycenter2 = ycenter-y1;
			//determine which front wheel is used
			double xfront, yfront;
			if ((this.wheelAngle % 180 + 180) % 180 > 90) { //left wheel is used
				xfront = xcenter;
				yfront = ycenter;
			} else { //right wheel is used
				xfront = xcenter2;
				yfront = ycenter2;
			}
			//calculate equation of line perpendicular to front wheel
			double m = Math.tan(Math.toRadians(this.wheelAngle-this.getAngle()));
			double p = yfront-(xfront*m);
			//calculate equation of line perpendicular to back wheel
			//no need to check which wheel is used because they are always parallel to each other
			double xback = this.posX+(Math.sin(Math.toRadians(this.getAngle()))*(this.height-this.distanceFromBack));
			double yback = this.posY+(Math.cos(Math.toRadians(this.getAngle()))*(this.height-this.distanceFromBack));
			double m2 = Math.tan(Math.toRadians(90-this.getAngle()-90));
			double p2 = yback - (xback * m2);
			//now, find when these lines intersect, if the wheel angle is 0 deg then it is a special case
			double xinter, yinter;
			if (((this.wheelAngle-this.getAngle())%180+180)%180 != 90) {
				xinter = (p2-p)/(m-m2);
				yinter = m*xinter+p;
			} else {
				xinter = xfront;
				yinter = m2*xinter+p2;
			}
			double radius = Math.sqrt(Math.pow((xinter-xfront),2) + Math.pow(yinter-yfront, 2));
			double theta = length/radius;
			
			//calculate polar coordinates
			double theta2 = truemod(Math.atan2(xfront-xinter, yfront-yinter), 2*Math.PI); //polar coordinates of front wheel when center of circle = (0,0)
			//System.out.println("A' = " + (xfront-xinter) + ", " + (yfront-yinter));
			//System.out.println("A' = " + radius*Math.sin(theta2) + ", " + radius*Math.cos(theta2));
			double xFirstSol = xinter+radius*Math.sin(theta2-theta);
			double yFirstSol = yinter+radius*Math.cos(theta2-theta);
			double xSecondSol = xinter+radius*Math.sin(theta2+theta);
			double ySecondSol = yinter+radius*Math.cos(theta2+theta);

			if ((truemod(this.wheelAngle, 180) > 90) ^ (direction == direction.FORWARDS)) {
				this.moveCarRelativeToFrontWheel(xFirstSol, yFirstSol, this.getAngle() - Math.toDegrees(theta));
				//this.setAngle(this.getAngle() - Math.toDegrees(theta));
			} else {
				this.moveCarRelativeToFrontWheel(xSecondSol, ySecondSol, this.getAngle() + Math.toDegrees(theta));
				//this.setAngle(this.getAngle() + Math.toDegrees(theta));
			}
		} else {
			//It is really simple: move along the line parallel to the car
			//not as simple as I thought tho
			//double p_ = 0;
			//double firstSolution = (-Math.sqrt(Math.pow(m, 2)-Math.pow(p_, 2)+1)-m*p_)/(Math.pow(m, 2)+1);
			//double secondSolution = (Math.sqrt(Math.pow(m, 2)-Math.pow(p_, 2)+1)-m*p_)/(Math.pow(m, 2)+1);
			double firstSolution, secondSolution;
			double m = y2/x2; //note that (x2;y2) is a vector
			double p = y0-m*x0;
			if (truemod(this.getAngle(), 180) != 0) {
				//determine equation of the line
				//determine the 2 points such that the distance to (x0;y0) is 1
				//solve x^2+y^2=1 with y=mx+p, you will come to this
			} else {
				//firstSolution = -length;
				//secondSolution = length;
			}
			//System.out.println(m + " " + p + " " + firstSolution + " " + secondSolution);
			//now determine which solution it must take
			if (direction == Direction.FORWARDS && (this.getAngle()%360+360)%360 < 180 ||
					direction == Direction.BACKWARDS && (this.getAngle()%360+360)%360 >= 180) {
				if (truemod(this.getAngle(), 180) != 0) {
					firstSolution = -(Math.sqrt(length))/(Math.sqrt(Math.pow(m, 2)+1));
					this.posX += firstSolution;
					this.posY += firstSolution*m;
				} else {
					this.posY -= length;
				}
			} else if (direction == Direction.BACKWARDS && (this.getAngle()%360+360)%360 < 180 ||
					direction == Direction.FORWARDS && (this.getAngle()%360+360)%360 >= 180) {
				if (truemod(this.getAngle(), 180) != 0) {
					secondSolution = (Math.sqrt(length))/(Math.sqrt(Math.pow(m, 2)+1));
					this.posX += secondSolution;
					this.posY += secondSolution*m;
				} else {
					this.posY += length;
				}
			}
		}
		try {
			ParkAssist.sidebar.updateData();
		} catch (NullPointerException e) {}
	}
	
	public double truemod(double d, double mod) {
		return (d%mod+mod)%mod;
	}
	
	//Move the front wheel to the specified coordinates, along with the rest of the car obviously.
	public void moveCarRelativeToFrontWheel(double x, double y, double angle) {

		this.setAngle(angle);
		double x1 = Math.cos(Math.toRadians(this.getAngle()))*this.width;
		double y1 = Math.sin(Math.toRadians(this.getAngle()))*this.width;
		if (truemod(this.wheelAngle, 180) <= 90) {
			x -= x1;
			y += y1;
		}
		this.posX = x-Math.sin(Math.toRadians(this.getAngle()))*(this.distanceFromFront);
		this.posY = y-Math.cos(Math.toRadians(this.getAngle()))*(this.distanceFromFront);
		if (truemod(this.wheelAngle, 180) <= 90) {
			this.posX = x-Math.sin(Math.toRadians(this.getAngle()))*(this.distanceFromFront);
			this.posY = y-Math.cos(Math.toRadians(this.getAngle()))*(this.distanceFromFront);
		}
		double xcenter = this.posX+Math.sin(Math.toRadians(this.getAngle()))*(this.distanceFromFront);
		double ycenter = this.posY+Math.cos(Math.toRadians(this.getAngle()))*(this.distanceFromFront);
		//determine coordinates of the other front wheel
		double xcenter2 = xcenter+x1;
		double ycenter2 = ycenter-y1;
		
		
	}

	public double getAngle() {
		return this.angle;
	}
}