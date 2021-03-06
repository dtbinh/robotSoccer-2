package actions;

import bot.Robot;
import net.sourceforge.jFuzzyLogic.FunctionBlock;

import org.opencv.core.Point;

import data.Coordinate;
import strategy.Action;
import ui.Field;

import java.util.ArrayList;

public class MoveToFacing  extends Action {

    private double error = 2.5;
    private boolean reachFirstSpot = false;
    private boolean reachFinalSpot = false;
    private ArrayList<Point> pointList = new ArrayList<Point>();;
    private int currentPointIndex = 0;
    private boolean once = true;
    private boolean stationary= false;
	private boolean front = true;
	private boolean isCharging = true;
	private int count = 0;
    private double previousX = 0;
    private double previousY = 0;
    
    double m_VcValue = 2.4500000000000002;
    double m_K_Vc = 0.050000000000000003;
    double m_VmaxMin = 0;
    double m_VmaxMax = 0.8;
    double m_K_Vmax = 23.5;
    double m_dVMin = 0;
    double m_dVMax = 425.0;
    double m_K_dV = 43.119999999999997;
    double m_Ak1 = 12.119999999999999;
    double m_Ak2 = 19.940000000000001;
    double m_Ak3 =3600.0000000000000;
	private double pastLinearVelocity = 0;
	private double pastAngularVelocity = 0;
	private Point ballVelocity = new Point();
	private ArrayList<Point> ballPoints = new ArrayList<Point>();
	private String missionText = "kick";
	private long missionBeginTime = 0;
	private int turnDirection;
	private boolean bMoveBackward;
    
    
    //non-static initialiser block
    {
        parameters.put("fixed point1 x", 140);
        parameters.put("fixed point1 y", 30);
       parameters.put("direction facing", 0);
        //parameters.put("error", 2.5);
    }
    @Override
    public void execute() {
    	ballPoints.add(new Point(ballX,ballY));
		if (ballPoints.size() > 7) {
			ballPoints.remove(0);
		}
		calculateBallVelocity();
		Point ballHit = ballSimulationMove(0.2);
		Point A = new Point(bot.getXPosition(),bot.getYPosition());
		Point B;
		
		//if (true) {
			//B = new Point(220.0,90.0);
		//} else {
			double angle = 0;
			double directX = bot.getXPosition() + Math.cos((angle*Math.PI/180)) * 0.20;
			double directY = bot.getYPosition() + Math.sin((angle*Math.PI/180)) * 0.20;
			B = new Point(220.0, 90.0);
		//}
			
		Point targetPos = A;
		double robotSize = 7.5;
		
		if (!missionText.equals("kick.approach.rotate")) {
			
			double disBallToWaitLine = DistanceToLine(A,B,ballHit);
			
			double angleRobotToBall = AngleDegree(ballHit.x-bot.getXPosition(), ballHit.y-bot.getYPosition());
			double angleRobotToB = AngleDegree(B.x-bot.getXPosition(),B.y-bot.getYPosition());
			double angleCheck = angleRobotToBall - angleRobotToB;
			
			while (angleCheck < -180) angleCheck+=360;
			while (angleCheck > 180) angleCheck -= 360;
			
			double orientationError = angleRobotToB - bot.getTheta();
			while (orientationError < -180) orientationError+=360;
			while (orientationError > 180) orientationError -= 360;
			
			if (Math.abs(disBallToWaitLine) < robotSize/2 && Math.abs(angleCheck) < 90 && Math.abs(orientationError) < 10 ) {
				missionText  = "kick";
				
				targetPos = B;
				
				double errorPositionStop =3;
				if (setVelocityToTarget(targetPos.x,targetPos.y,true)  < errorPositionStop) {
					//if( mission.behavior.controlType == StrategyGUI_BehaviorControlType::Once )
						//mission.bComplete = true;
				} else {
					//bActiveMission = true;
				}
				
				
			} else {
				//used simulate ball position for normalize in C++
				Point tmpNormalize = normalize(new Point(ballX-B.x,ballY-B.y));
				Point tmp = new Point(ballHit.x+(tmpNormalize.x*robotSize/2),ballHit.y+(tmpNormalize.y*robotSize/2));
				
				double radBallToB = AngleRadian(B.x-ballHit.x,B.y-ballHit.y);
				turnDirection = -1;
				
				if (ccw(new Point(bot.getXPosition(),bot.getYPosition()),ballHit, B) >= 0) {
					turnDirection = 1;
				}
				
				Point posCenter = new Point();
				posCenter.x = tmp.x + Math.cos(radBallToB +turnDirection * Math.PI/2)* robotSize;
				posCenter.y = tmp.y + Math.sin(radBallToB + Math.PI/2)*robotSize;
				
				Point posCenterPlanB = new Point();
				posCenterPlanB.x = tmp.x + Math.cos(radBallToB -turnDirection * Math.PI/2)*robotSize;
				posCenterPlanB.y = tmp.y + Math.sin(radBallToB +turnDirection * Math.PI/2)*robotSize;
				
				if (posCenter.x < robotSize/2 || posCenter.y < robotSize/2 || posCenter.x > Field.OUTER_BOUNDARY_WIDTH -robotSize/2 || posCenter.y > Field.OUTER_BOUNDARY_HEIGHT - robotSize/2) {
					posCenter = posCenterPlanB;
					turnDirection = -turnDirection;
				}
				
				Point missionTmp = posCenter;
				
				double disRobotToCenter = Distance(bot.getXPosition()-posCenter.x,bot.getYPosition()-posCenter.y);
				double radius = robotSize/2;
				
				if (disRobotToCenter > radius) {
					
					double disRobotToTarget = Math.sqrt(disRobotToCenter*disRobotToCenter - radius*radius);
					double radTarget = Math.atan2(disRobotToTarget, radius);
					
					double radCenterToRobot = AngleRadian(bot.getXPosition()-posCenter.x,bot.getYPosition()-posCenter.y);
					
					targetPos.x = posCenter.x + Math.cos(radCenterToRobot + turnDirection *radTarget)*radius;
					targetPos.y = posCenter.y + Math.sin(radCenterToRobot + turnDirection *radTarget)*radius;
				}
				double errorPositionStop = 1;
				if (setVelocityToTarget(targetPos.x,targetPos.y,true) < errorPositionStop) {
					missionText = "kick.approach.rotate";
					missionBeginTime = System.currentTimeMillis();
				} else {
					missionText = "kick.approach";
				}
			}
			
		}
		
		if (missionText.equals("kick.approach.rotate")) {
			if (missionBeginTime + 500 < System.currentTimeMillis()) {
				missionText = "kick";
				bot.angularVelocity = 0;
				bot.linearVelocity = 0;
			} else {
				if (bMoveBackward) {
					bot.linearVelocity = -0.3;
				} else  {
					bot.linearVelocity = 0.3;
				}
				bot.angularVelocity = -2*(480*turnDirection) * (Math.PI/180);

			}

		}


		double targetX = ballX;
		double targetY = ballY;

		double actualAngleError;
		double distanceToTarget = getDistanceToTarget(bot, targetX, targetY);
		double angleToTarget = getTargetTheta(bot, targetX, targetY); //degrees

		if ((Math.abs(angleToTarget) > 90)) {
			if (angleToTarget < 0) {
				actualAngleError = Math.toRadians(-180 - angleToTarget);
			} else {
				actualAngleError = Math.toRadians(180 - angleToTarget);
			}
		} else {
			actualAngleError =  Math.toRadians(angleToTarget);
		}


		//charge ball into goal
		double range = 10;
		if (isCharging) {
			range = 30;
		}
		//check if positive situation first
//		if (count == 0 && distanceToTarget < 20 && Math.abs(actualAngleError) < Math.PI/10 && bot.getXPosition() - ballX > 5) {
//			//negative situation so reverse
//			count = 20;
//			if (front) {
//				front = false;
//			} else {
//				front = true;
//			}
//			isCharging = false;
//		} else {
			if (distanceToTarget < range && Math.abs(actualAngleError) < Math.PI / 10 && bot.getXPosition() - ballX < 5) {
				bot.linearVelocity = front ? 1 : -1;
				if (targetX > 110) {
					double angleToGoal = angleDifferenceFromGoal(bot.getXPosition(), bot.getYPosition(), bot.getTheta()); //degrees
					//   System.out.println("front is " + front + "     abs(angleTogoal) is " + Math.abs(angleToGoal));
					if ((front && Math.abs(angleToGoal) > 45) || (!front && Math.abs(angleToGoal) < 135)) {
						if (angleToGoal > 0) {
							bot.angularVelocity = front ? 30 : -30;
						} else {
							bot.angularVelocity = front ? -30 : 30;
						}
					}
				}
				isCharging = true;

			} else {
				isCharging = false;
			}

			System.out.println(missionText);
		}
//		System.out.println("isCharing:" + isCharging);
//
//		//System.out.println(missionText);
//    }

	public double getTargetTheta(Point target) {
		Robot r = bot;
		double targetTheta = Math.atan2(r.getYPosition() - target.y, target.x - r.getXPosition());
		double difference = targetTheta - Math.toRadians(r.getTheta());

		if (difference > Math.PI) {
			difference -= (2 * Math.PI);
		} else if (difference < -Math.PI) {
			difference += (2 * Math.PI);
		}
		difference = Math.toDegrees(difference);
		targetTheta = difference;

		return targetTheta;
	}

    public double setVelocityToTarget(double x, double y, boolean reverse) {
        Robot r = bot;
        front  = true;
        double targetDist = Math.sqrt(Math.pow((x-r.getXPosition()),2) + Math.pow((y-r.getYPosition()),2));
        double targetTheta = Math.atan2(r.getYPosition() - y, x - r.getXPosition());
		double difference = targetTheta - Math.toRadians(r.getTheta());
		
		if (difference > Math.PI) {
			difference -= (2 * Math.PI);
		} else if (difference < -Math.PI) {
			difference += (2 * Math.PI);
		}
		difference = Math.toDegrees(difference);
		targetTheta = difference;
		if (targetTheta > 90 || targetTheta < -90) {
			front = false;
		}

		if (!front && reverse) {
			if (targetTheta < 0) {
				targetTheta = -180 - targetTheta;
			} else if (targetTheta > 0) {
				targetTheta = 180 - targetTheta;
			}
		}
        FunctionBlock fb = loadFuzzy("fuzzy/fastFuzzy.fcl");

        fb.setVariable("targetTheta", targetTheta);
        fb.setVariable("targetDist", targetDist);
        
        // Evaluate
        fb.evaluate();

        //     JFuzzyChart.get().chart(fb);


        // Show output variable's chart
        fb.getVariable("linearVelocity").defuzzify();
        fb.getVariable("angularVelocity").defuzzify();
        //    JFuzzyChart.get().chart(fb.getVariable("linearVelocity"), fb.getVariable("linearVelocity").getDefuzzifier(), true);
        //     JFuzzyChart.get().chart(fb.getVariable("angularVelocity"), fb.getVariable("angularVelocity").getDefuzzifier(), true);
        //    JOptionPane.showMessageDialog(null, "nwa"); 
        double linear  = fb.getVariable("linearVelocity").getValue();
        double angular = fb.getVariable("angularVelocity").getValue();
        //    System.out.println(" raw right :" + fb.getVariable("rightWheelVelocity").getValue() + " raw left " + fb.getVariable("leftWheelVelocity").getValue());

        //    System.out.println("right :" + right + "left " + left);

        r.linearVelocity = linear;
        r.angularVelocity = angular*-1;
        

//            System.out.println("linear: " + linear + " angular:" + angular*-1
//            					+ " x: " + r.getXPosition() + " y: " + r.getYPosition()
//            					+ " r theta: " + r.getTheta() + " t theta: " + targetTheta
//            					+ " t dist" + targetDist + " time: " + System.currentTimeMillis());

        if (!front &&reverse) {
            r.linearVelocity *= -1;
            r.angularVelocity *= -1;
        }

      return targetDist;
    }
   
    private void calculateBallVelocity() {
		Point mean = new Point(0,0);
		
		for (int i = 0; i<ballPoints.size(); i++) {
			mean.x += ballPoints.get(i).x;
			mean.y += ballPoints.get(i).y;
		}
		
		mean.x /= ballPoints.size();
		mean.y/= ballPoints.size();
		
		double a = 0;
		double b = 0;
		double c = 0;
		
		for (int i = 0; i<ballPoints.size(); i++) {
			
			a += (ballPoints.get(i).x - mean.x)*(ballPoints.get(i).x - mean.x);
			b += 2*  (ballPoints.get(i).x - mean.x) *  (ballPoints.get(i).y - mean.y);
			c +=  (ballPoints.get(i).y - mean.y)  *  (ballPoints.get(i).y - mean.y);
		}
		
		if (a==c || b == 0) {
			ballVelocity.x = 0;
			ballVelocity.y = 0;
		}
		
		double theta = Math.atan2(b, a-c)/2;
		
		Point dis = new Point(ballPoints.get((ballPoints.size()-1)).x-ballPoints.get(0).x,
				ballPoints.get(ballPoints.size()-1).y-ballPoints.get(0).y);
		
		if (Math.abs(theta) < Math.PI/4) {
			if (dis.x < 0) theta+=Math.PI;
		} else {
			if (dis.y > 0) {
				if (theta < 0 ) theta += Math.PI;
			} else {
				if (theta > 0) theta -= Math.PI;
			}
		}
		
		ballVelocity.x = Math.cos(theta)*Distance(dis.x,dis.y) / ballPoints.size() / (1.0/30.0);
		ballVelocity.y = Math.sin(theta)*Distance(dis.x,dis.y) / ballPoints.size() / (1.0/30.0);
	}
	
	private Point ballSimulationMove(double sec) {
		
		double radius = 2.3500000000000000;
		Point ball = new Point(ballX+ballVelocity.x*sec,ballY+ballVelocity.y*sec);
		double BALL_WALL_K = 0.5;
		
		if (ball.x < radius && ballVelocity.x < 0) {
			ball.x = radius - BALL_WALL_K*(ball.x-radius);
			ballVelocity.x *= -BALL_WALL_K;
		}
		
		if (ball.x > Field.OUTER_BOUNDARY_WIDTH-radius && ballVelocity.x > 0) {
			ball.x = (Field.OUTER_BOUNDARY_WIDTH-radius) - BALL_WALL_K*(ball.x-(Field.OUTER_BOUNDARY_WIDTH-radius));
		}
		
		if (ball.y < radius && ballVelocity.y < 0) {
			ball.y = radius - BALL_WALL_K*(ball.y-radius);
			ballVelocity.y *= -BALL_WALL_K;
		}
		
		if (ball.y > Field.OUTER_BOUNDARY_HEIGHT-radius && ballVelocity.y > 0) {
			ball.y = (Field.OUTER_BOUNDARY_HEIGHT-radius) - BALL_WALL_K*(ball.y-(Field.OUTER_BOUNDARY_HEIGHT-radius));
		}
		
		double k = 5;
		double g = 9.8;
		double mass = 0.1;
		
		Point friction = normalize(ballVelocity);
		//ballVelocity.x = ballVelocity.x - friction.x * k * mass * g * sec;
		//ballVelocity.y = ballVelocity.x - friction.x * k * mass * g * sec;
		
		
		return ball;
	}

	private double Distance(Point p) {
		return Distance(p.x,p.y);
	}

	public Point normalize(Point a) {
		Point result = new Point();
		double dis = Distance(a.x,a.y);
		if (dis!=0) {
			result.x = a.x / dis;
			result.y = a.y / dis;
		}
		
		return result;
		
	}
	
	public double Distance(double x, double y) {
		return Math.sqrt((x*x) + (y*y));
	}
	
    
    private double ExecuteMission_OrientationTo(int VP2M_id, int orientation, boolean bMoveForwardOnly) {		
    	double targetDist = 0;
    	double targetTheta = orientation;
        double difference = Math.toRadians(targetTheta) - Math.toRadians(bot.getTheta());
        if (difference > Math.PI) {
            difference -= (2 * Math.PI);
        } else if (difference < -Math.PI) {
            difference += (2 * Math.PI);
        }
        difference = Math.toDegrees(difference);
        targetTheta = difference;
        
        bMoveBackward = false;
        
        if (!bMoveForwardOnly && Math.abs(targetTheta) > 90) {
            if (targetTheta < 0) {
                targetTheta = -180 - targetTheta;
            }
            else if (targetTheta > 0) {
                targetTheta = 180 - targetTheta;
            }
        }
        
        GenerateVelocity_VP2(VP2M_id, targetTheta, targetDist, bot);
        
        
        if (bMoveBackward) {
       	 bot.linearVelocity *= -1;
        }
        
        SmoothVelocity(VP2M_id);
		return Math.abs(targetTheta);
	}


	public Point ExecuteMission_MoveTo(int VP2M_id, Point pos, boolean bMoveForwardOnly) {
    	 Robot r = bot;
    	 double tempX = r.getXPosition() + (r.linearVelocity*0.100);
    	 
    	 double  targetDist = Math.sqrt(Math.pow((pos.x-r.getXPosition()),2) + Math.pow((pos.y-r.getYPosition()),2));
         double targetTheta = Math.atan2(r.getYPosition() - pos.y, pos.x - r.getXPosition());
         double difference = targetTheta - Math.toRadians(r.getTheta());
         if (difference > Math.PI) {
             difference -= (2 * Math.PI);
         } else if (difference < -Math.PI) {
             difference += (2 * Math.PI);
         }
         difference = Math.toDegrees(difference);
         targetTheta = difference;
         
         boolean bMoveBackward = false;
         
         if (!bMoveForwardOnly && Math.abs(targetTheta) > 90) {
             if (targetTheta < 0) {
                 targetTheta = -180 - targetTheta;
             }
             else if (targetTheta > 0) {
                 targetTheta = 180 - targetTheta;
             }
         }
         
         GenerateVelocity_VP2(VP2M_id, targetTheta, targetDist, r);
         
         
         if (bMoveBackward) {
        	 r.linearVelocity *= -1;
         }
         
         SmoothVelocity(VP2M_id);
         
         return new Point(pos.x - r.getXPosition(), pos.y - r.getYPosition());
    }

	private void SmoothVelocity(int vP2M_id) {
		Robot r = bot;
		double LinearVelocityMaxAcc  = m_Ak1 * 1.0 / 30.0;
		double LinearVelocityMaxDec  = m_Ak2 * 1.0 / 30.0;
		double AngularVelocityMaxAcc = m_Ak3 * Math.PI / 180.0 * 1.0 / 30.0;
		
		double pre_lv = pastLinearVelocity;
		double pre_av = pastAngularVelocity;

		if( pre_lv * r.linearVelocity < 0 )
		{
			r.linearVelocity = 0;
		}
		else
		{
			double d_lv = r.linearVelocity - pre_lv;
			if( d_lv > 0  )  {
				d_lv = Math.min( d_lv, LinearVelocityMaxAcc );

				//if( r.linearVelocity > 0 )
					r.linearVelocity = pre_lv + d_lv;
				//else
				//	r.linearVelocity = pre_lv - d_lv;
			}
			else 
			{
				d_lv = -Math.min( -d_lv, LinearVelocityMaxDec );

				r.linearVelocity = pre_lv + d_lv;
			}
		}

		if( pre_av * r.angularVelocity < 0 )
		{
			r.angularVelocity = 0;
		}
		else
		{
			double d_av = r.angularVelocity - pre_av;
			if( d_av > 0  ) 
			{
				d_av = Math.min( d_av, AngularVelocityMaxAcc );

				//if( r.angularVelocity > 0 )
					r.angularVelocity = pre_av + d_av;
				//else
				//	r.angularVelocity = pre_av - d_av;
			}
			else 
			{
				d_av = -Math.min( -d_av, AngularVelocityMaxAcc );

				//if( r.angularVelocity > 0 )
					r.angularVelocity = pre_av + d_av;
				//else
				//	r.angularVelocity = pre_av - d_av;
			}
		}
	}

	private void GenerateVelocity_VP2(int vP2M_id, double targetTheta,
			double targetDist, Robot r) {
		
		double expEquation =  -(targetTheta/m_K_dV)*(targetTheta/m_K_dV); 
		double AngularVelocityDegree = (m_dVMin +  (m_dVMax - m_dVMin)*(1.0 - Math.exp(expEquation)));
		
		if (targetTheta < 0 ) AngularVelocityDegree *= -1;
		double expEquation2 = -(targetTheta/m_K_Vmax)*(targetTheta/m_K_Vmax);  
		double Vmax = (m_VmaxMin +  (m_VmaxMax-m_VmaxMin)*(Math.exp(expEquation2)));
		
		double expEquation3 = -(targetDist/m_K_Vc)*(targetDist/m_K_Vc); 
		double V = (Vmax*(1.0 -Math.exp(expEquation3)));
		
		r.angularVelocity = AngularVelocityDegree *Math.PI/180;
		r.linearVelocity = V;
	}
    
	public double AngleRadian(double x, double y) {
		double angle = Math.atan2(y, x);
		while(angle < -Math.PI) angle+=(Math.PI*2);
		while(angle > Math.PI) angle -=(Math.PI*2);
		return angle;
	}
	
	public double AngleDegree(double x, double y) {
		double angle = Math.atan2(y, x) * 180/Math.PI;
		while(angle < -180) angle+=360;
		while(angle > 180) angle -=360;
		return angle;
	}
	
	private double DistanceToLine(Point P1, Point P2, Point P) {
		double angle1 = AngleRadian((P1.x-P2.x),(P1.y-P2.y));
		double angle2 = AngleRadian((P.x-P2.x), (P.y-P2.y));
		
		double distance = Distance((P.x-P2.x),(P.y-P2.y))* Math.sin(angle2-angle1);
		
		return distance;
	}
	private double ccw(Point p1, Point p2, Point p3) {
		return p1.x*p2.y + p2.x*p3.y + p3.x*p1.y - p1.x*p3.y - p2.x*p1.y  - p3.x*p2.y;
	}
	
}