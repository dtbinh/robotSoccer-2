package data;

import org.opencv.core.Point;
import utils.Image;

public class RobotData {
	private Point[] teamRectPoint;
	private Point teamCenterPoint;
	private PairPoint shortPair;
	private PairPoint longPair;
	private PairPoint greenPatch1 = null;
	private PairPoint greenPatch2 = null;
	private double thresholdDistance;
	private final static double THRESHOLDANGLE = 20;
    private int robotNum;
    private double theta;
	
	public RobotData(Point[] teamRectPoint, Point teamCenterPoint) {
		this.teamRectPoint = teamRectPoint;
		this.teamCenterPoint = teamCenterPoint;
		assignPairPoints();
		assignThresholdDistance();
	}
	
	private void assignPairPoints() {
		Point referencePoint = teamRectPoint[0];
		
		// find the distance between this reference point and every other point.
		double d1 = Image.euclideanDistance(referencePoint, teamRectPoint[1]);
		double d2 = Image.euclideanDistance(referencePoint, teamRectPoint[2]);
		double d3 = Image.euclideanDistance(referencePoint, teamRectPoint[3]);
		
		// find the angle between this reference point and every other point.
		double a1 = Image.angleBetweenTwoPoints(referencePoint, teamRectPoint[1]);
		double a2 = Image.angleBetweenTwoPoints(referencePoint, teamRectPoint[2]);
		double a3 = Image.angleBetweenTwoPoints(referencePoint, teamRectPoint[3]);
		
		// find the short pair.
		double shortPair = Math.min(d1, Math.min(d2, d3));
		
		if (shortPair == d1) {
			this.shortPair = new PairPoint(referencePoint, teamRectPoint[1], d1, a1);
		} else if (shortPair == d2) {
			this.shortPair = new PairPoint(referencePoint, teamRectPoint[2], d2, a2);
		} else {
			this.shortPair = new PairPoint(referencePoint, teamRectPoint[3], d3, a3);
		}
		
		double longPair = Math.max(Math.min(d1,d2), Math.min(Math.max(d1,d2), d3));
		
		if (longPair == d1) {
			this.longPair = new PairPoint(referencePoint, teamRectPoint[1], d1, a1);
		} else if (longPair == d2) {
			this.longPair = new PairPoint(referencePoint, teamRectPoint[2], d2, a2);
		} else {
			this.longPair = new PairPoint(referencePoint, teamRectPoint[3], d3, a3);
		}
	}
	
	private void assignThresholdDistance() {
		double endX = shortPair.second.x + shortPair.euclideanDistance * Math.cos(Math.toRadians(shortPair.theta));
		double endY = shortPair.second.y + shortPair.euclideanDistance * Math.sin(Math.toRadians(shortPair.theta));
		Point endPoint = new Point(endX, endY);
		thresholdDistance = Image.euclideanDistance(teamCenterPoint, endPoint);
	}
	
	public void addGreenPatch(Point greenCenterPoint) {
		
		double distance = Image.euclideanDistance(teamCenterPoint, greenCenterPoint);
		
		if (distance > thresholdDistance) {
			return;
		}
		
		if (greenPatch1 == null) {
			greenPatch1 = new PairPoint(teamCenterPoint, greenCenterPoint, distance, Image.angleBetweenTwoPoints(teamCenterPoint, greenCenterPoint));
		} else if (greenPatch2 == null) {
			greenPatch2 = new PairPoint(teamCenterPoint, greenCenterPoint, distance, Image.angleBetweenTwoPoints(teamCenterPoint, greenCenterPoint));
		}
		
	}
	
	public boolean isLongPatch(PairPoint greenPatch) {
		
		double longPairTheta = longPair.getTheta();
		
		double differenceTheta = Math.abs(longPairTheta - Image.angleBetweenTwoPoints(teamCenterPoint, greenPatch.second));
		
		if ((differenceTheta % 90) > 90 - THRESHOLDANGLE && (differenceTheta % 90) < THRESHOLDANGLE + 90) {
			return true;
		}
		
		return false;
	}
	
	public PairPoint getShortPair() {
		return shortPair;
	}
	
	public PairPoint getLongPair() {
		return longPair;
	}
	
	public Point getTeamCenterPoint() {
		return teamCenterPoint;
	}
	
	/**
	 * <p>Returns robot identification. 1-5. Returns -1 if not identifiable.</p>
	 * @return
	 */
	
	public int robotIdentification() {

        if (greenPatch1 == null && greenPatch2 == null) {
            return -1;
        }

        boolean isLongPatchPresent = false;
        boolean isRobotNumThree = false;
        Point shortMidPoint;

		if (greenPatch1 != null && greenPatch2 != null) {
			// Must be either 5, 4, 3
		//	if (!isLongPatch(greenPatch1) && !isLongPatch(greenPatch2)) {
            if (Math.abs(greenPatch1.getEuclideanDistance() - greenPatch2.getEuclideanDistance()) < 1) { //change 1 if needed
				isRobotNumThree = true;
                shortMidPoint = greenPatch1.getSecond(); //any green patch will work
			} else {
                // Must be either 5, 4
                isLongPatchPresent = true;
                if (greenPatch1.getEuclideanDistance() - greenPatch2.getEuclideanDistance() > 0) {
                    shortMidPoint = greenPatch2.getSecond();
                } else {
                    shortMidPoint = greenPatch1.getSecond();
                }
			}
			
		} else {
            // Must be either 2, 1
            shortMidPoint = greenPatch1.getSecond();
        }

        Point robotMidPoint = getTeamCenterPoint();
        double robotOrientation = getLongPair().getTheta();
        double normAngle  = Math.toDegrees(Math.atan2(shortMidPoint.y - robotMidPoint.y, shortMidPoint.x - robotMidPoint.x));
        double shortAngleToRobot = normAngle - robotOrientation;

        //some hack to make the difference -180 < theta < 180
        if (shortAngleToRobot > 180) {
            shortAngleToRobot -= (2 * 180);
        } else if (shortAngleToRobot < -180) {
            shortAngleToRobot += (2 * 180);
        }

        int quadrant = -1;

        for (int i = 0; i < 4; i++) {
            if (-180+90*i < shortAngleToRobot && shortAngleToRobot < -180+90*(i+1)) {
                quadrant = i;
                break;
            }
        }

        if (isRobotNumThree) {
            robotNum = 3;
            if (quadrant == 0 || quadrant == 3) {
                theta = robotOrientation + 180;
            } else {
                theta = robotOrientation;
            }
        } else if (!isLongPatchPresent) {
            if (quadrant == 0) {
                robotNum = 2;
                theta = robotOrientation + 180;
            } else if (quadrant == 1) {
                robotNum = 1;
                theta = robotOrientation;
            } else if (quadrant == 2) {
                robotNum = 2;
                theta = robotOrientation;
            } else if (quadrant == 3) {
                robotNum = 1;
                theta = robotOrientation + 180;
            }
        } else {
            if (quadrant == 0) {
                robotNum = 4;
                theta = robotOrientation + 180;
            } else if (quadrant == 1) {
                robotNum = 5;
                theta = robotOrientation;
            } else if (quadrant == 2) {
                robotNum = 4;
                theta = robotOrientation;
            } else if (quadrant == 3) {
                robotNum = 5;
                theta = robotOrientation + 180;
            }
        }

//			System.out.println("quadrant: " + quadrant);
//			System.out.println("\nrobot O: " + robotOrientation);
//			System.out.println("normA: " + normAngle);
//			System.out.println("angleToRobot: " + shortAngleToRobot);
//            System.out.println("robot num: " + robotNum);
//            System.out.println("theta: " + theta);
        return robotNum;
	}
	
	public double getTheta() {
        return Math.toRadians(-theta);
    }
	public class PairPoint {
		
		private Point first;
		private Point second;
		private double euclideanDistance;
		private double theta;
		
		public PairPoint(Point first, Point second, double euclideanDistance, double theta) {
			this.first = first;
			this.second = second;
			this.euclideanDistance = euclideanDistance;
			this.theta = theta;
		}
		
		public Point getFirst() {
			return first;
		}
		
		public Point getSecond() {
			return second;
		}
		
		public double getEuclideanDistance() {
			return euclideanDistance;
		}
		
		public double getTheta() {
			return theta;
		}
	}
	
}
