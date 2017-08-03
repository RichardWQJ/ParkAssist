
public class ParallelParking {
	
	/**
	 * Makes the car park automatically at location x,y, at the required angle.
	 */
	
	final static double SECURITY_DIST = 2*28;
	final static double SPEED = 1;
	final static boolean USE_OLD_ALGORITHM = false;
	public static void parallelPark(Car car, Direction direction) {
		
		
		if (direction == Direction.LEFT) {
			System.out.println("Park Assist does not support left side parallel parking!");
			return;
		}
		if (direction != Direction.LEFT && direction != Direction.RIGHT) {
			System.out.println("Wrong direction!");
			return;
		}
		int x = (int)(car.posX + car.width) + ParkAssist.DIST_BTW_CARS;
		int originalx = (int)car.posX;
		int y = (int)car.posY;
		//Security distance there must be between the cars.
		//Example: if the height of the user car is 40, and the distance is 60,
		//security distance will be 20.
		
		Car[] obstacles = ParkAssist.cars.subList(1, ParkAssist.cars.size()).toArray(new Car[ParkAssist.cars.size()-1]);
		
		System.out.println("Beginning parallel parking");
		
		if (truemod(car.getWheelAngle(), 180) != 0) {
			System.out.println("Wheel angle is not 0 ("+car.getWheelAngle()+"), aborting.");
			return;
		}
		
		Sensor topSensor, bottomSensor;
		if (direction == Direction.LEFT) {
			topSensor = car.getTopLeftSensor();
			bottomSensor = car.getBottomLeftSensor();
		} else {
			topSensor = car.getTopRightSensor();
			bottomSensor = car.getBottomRightSensor();
		}
		
		int neededxpos = (int)(car.posX + car.width + ParkAssist.DIST_BTW_CARS);
		//int neededxpos = 900;
		
		//Check sensors to see if there is somewhere to park
		if (topSensor.detect(car, obstacles) < ParkAssist.DIST_BTW_CARS+car.width ||
				bottomSensor.detect(car, obstacles) < ParkAssist.DIST_BTW_CARS+car.width) {
			System.out.println("Sensors detected obstacles, aborting.");
			return;
		}
		
		//First, figure out the distance between cars
		//If it is inferior to the security distance + height of car, abort
		
		//Distance between top sensor and top of the car, or bottom sensor and bottom of the car
		double distBtwTopSensorAndTopOfCar = topSensor.relativeY;
		double distBtwBottomSensorAndBottomOfCar = car.height-bottomSensor.relativeY;
		
		//Check for bottom obstacle
		moveCar(car, Direction.BACKWARDS, distBtwBottomSensorAndBottomOfCar+SECURITY_DIST/2);
		if (originalx != (int)car.posX) {
			System.out.println("Car somehow moved! Aborting.");
			return;
		}
		if (bottomSensor.detect(car, obstacles) < ParkAssist.DIST_BTW_CARS+car.width) {
			System.out.println("Detected obstacle, please position the car correctly");
			return;
		}
		//check for top obstacle
		moveCar(car, Direction.FORWARDS, distBtwTopSensorAndTopOfCar+distBtwBottomSensorAndBottomOfCar+SECURITY_DIST);
		if (originalx != (int)car.posX) {
			System.out.println("Car somehow moved! Aborting.");
			return;
		}
		if (topSensor.detect(car, obstacles) < ParkAssist.DIST_BTW_CARS+car.width) {
			System.out.println("Detected obstacle, please position the car correctly");
			return;
		}
		
		double yBackOtherCar = car.posY + distBtwTopSensorAndTopOfCar;
		
		if (!USE_OLD_ALGORITHM) {

			//After checking for obstacles, the car moves forwards this distance
			int forwardsDist = 95;
			//Then, it moves backwards this distance
			int backDist = 77;
			//Then, it sets the wheel angle to 0 and moves backwards this distance
			int backDist2 = 25;
			//Then, it sets the wheel angle to -max and moves backwards this distance
			int backDist3 = 25;
			
			//Then, it finishes using a loop with these parameters
			int repositionAngle = (int) car.maxWheelAngle;
			int repositionDist = 25;
			
			//position
			moveCar(car, Direction.FORWARDS, forwardsDist-distBtwTopSensorAndTopOfCar);
			if (originalx != (int)car.posX) {
				System.out.println("Car somehow moved! Aborting.");
				return;
			}
			moveWheelAngle(car, car.maxWheelAngle);
			moveCar(car, Direction.BACKWARDS, backDist);
			if (car.getWheelAngle() != car.maxWheelAngle) {
				System.out.println("Wheel angle moved! Aborting.");
				return;
			}
			moveWheelAngle(car, -2*car.maxWheelAngle);
			
			moveCar(car, Direction.BACKWARDS, backDist);
			if (car.getWheelAngle() != -car.maxWheelAngle) {
				System.out.println("Wheel angle moved! Aborting.");
				return;
			}
			moveWheelAngle(car, car.maxWheelAngle);
			
			//fine tune
			while (car.posX <= neededxpos) {
				moveWheelAngle(car, repositionAngle);
				moveCar(car, Direction.FORWARDS, repositionDist);
				moveWheelAngle(car, -2*repositionAngle);
				moveCar(car, Direction.FORWARDS, repositionDist);
				moveWheelAngle(car, repositionAngle);
				if (car.posX > neededxpos) break;
				moveWheelAngle(car, repositionAngle);
				moveCar(car, Direction.BACKWARDS, repositionDist);
				moveWheelAngle(car, -2*repositionAngle);
				moveCar(car, Direction.BACKWARDS, repositionDist);
				moveWheelAngle(car, repositionAngle);
			}
			
		} else {
			
			int forwardsDist = 120;
			
			moveCar(car, Direction.FORWARDS, forwardsDist);
			moveWheelAngle(car, car.maxWheelAngle);
			moveCarUntilAngle(car, Direction.BACKWARDS, 45);
			moveWheelAngle(car, -car.maxWheelAngle);
			double heightDiff = yBackOtherCar-(car.posY-Math.sin(Math.toRadians(car.getAngle()))*car.width);
			moveCar(car, Direction.BACKWARDS, Math.sqrt(2)*heightDiff);
			moveWheelAngle(car, -car.maxWheelAngle);
			moveCarUntilAngle(car, Direction.BACKWARDS, 0);
			moveWheelAngle(car, car.maxWheelAngle);
		}
		
		//move to middle
		double detectTop = car.getTopSensor().detect(car, obstacles);
		double detectBottom = car.getBottomSensor().detect(car, obstacles);
		if (detectTop > detectBottom) {
			moveCar(car, Direction.FORWARDS, (detectTop-detectBottom)/2);
		} else if (detectTop < detectBottom) {
			moveCar(car, Direction.BACKWARDS, (detectBottom-detectTop)/2);
		}
		
		System.out.println("Finished parallel parking");
		
		
	}
	
	public static void moveCar(Car car, Direction direction, double length) {
		for (int i = 0; i < length; i++) {
			car.move(direction, 1);
			ParkAssist.window.repaint();
			try {
				Thread.sleep((long) (10/SPEED));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Moves the car in the given direction until (int)car.angle is equal to the given angle. 
	 */
	public static void moveCarUntilAngle(Car car, Direction direction, int angle) {
		while ((int)car.getAngle() != angle) {
			car.move(direction, 1);
			ParkAssist.window.repaint();
			try {
				Thread.sleep((long) (10/SPEED));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		car.setAngle(angle);
		ParkAssist.window.repaint();
	}
	
	public static void moveWheelAngle(Car car, double degrees) {
		int angleDir = (degrees < 0 ? -1 : 1);
		degrees = Math.abs(degrees);
		for (int i = 0; i < degrees; i++) {
			car.setWheelAngle(car.getWheelAngle()+angleDir);
			ParkAssist.window.repaint();
			try {
				Thread.sleep((long) (5/SPEED));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void parkInson(Car car) {
		Thread thread = new Thread(new Runnable() {
			public void run() {
				System.out.println("Moar park inson");
								
				while (true) {
					
					int rand = (int)(Math.random()*4);
					if (rand == 0) {
						car.posX += 1;
					} else if (rand == 1) {
						car.posX -= 1;
						car.setWheelAngle(car.getWheelAngle()+1);
					} else if (rand == 2) {
						car.posY += 1;
					} else if (rand == 3) {
						car.posY -= 1;
						car.setWheelAngle(car.getWheelAngle()-1);
					}
					ParkAssist.window.repaint();
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		thread.start();
	}
	
	public static double truemod(double d, int mod) {
		return (d%mod+mod)%mod;
	}
	
}
