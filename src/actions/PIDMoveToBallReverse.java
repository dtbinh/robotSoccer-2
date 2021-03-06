package actions;

import data.Coordinate;
import strategy.Action;

/**
 * Created by Wesley on 18/07/2015.
 */
public class PIDMoveToBallReverse extends Action {

    private boolean isPreviousDirectionForward = true;
    private boolean isCharging = true;
    private boolean presetToForward = false;  // if true, robot will definitely go forward
    private boolean presetToBackward = false; //if true, robot will definitely go backwards
    private double behindRange = 5;

    {
        parameters.put("speed", 5);
        parameters.put("kp", 3); //0.5
        parameters.put("ki", 0);  //0.1
    }

    @Override
    public void execute() {

        //reverse if stuck
        if (bot.isStuck(new Coordinate(bot.getXPosition(), bot.getYPosition()))) {
            if (!presetToBackward && ! presetToForward) {
                System.out.println("bot is stuck :(");
                if (isPreviousDirectionForward) {
                    presetToBackward = true;
                } else {
                    presetToForward = true;
                }
            }
        } else {
            presetToBackward = false;
            presetToForward = false;
        }

        double targetX = ballX;
        double targetY = ballY;

        //change target if it's a negative situation
        if (bot.getXPosition() - ballX > behindRange) {
            if (Math.abs(ballY - bot.getYPosition()) > 10 || bot.getXPosition() < ballX) { //bot is not inline with ball
                behindRange = -15;
                targetX = ballX - 20;
            } else {
                behindRange = -15;
                targetX = ballX - 20;
                if (ballY > 110) {
                    targetY = ballY - 20;
                } else {
                    targetY = ballY + 20;
                }
            }
            if (ballX < 20) {
                if (ballY > 45 && ballY < 135) {
                    bot.linearVelocity = 0;
                    return;//don't go for it!
                } else {
                    targetX = ballX;
                    targetY = ballY;
                }
            }
        } else {
            behindRange = 5;
        }

        boolean isCurrentDirectionForward;
        //get angle to ball
        double angleToBall = getTargetTheta(bot, targetX, targetY);
        double actualAngleError;
        double distanceToBall = getDistanceToTarget(bot, targetX, targetY);

        //spin if ball is stuck beside robot on positive side
        if (distanceToBall < 7 && Math.abs((angleToBall-90)%180) < 30 && targetX > bot.getXPosition()) {
            if (angleToBall > 0) {//ie. around 90
                bot.angularVelocity = 30;
            } else {
                bot.angularVelocity = -30;
            }
            return;
        }

        if ((!presetToForward && Math.abs(angleToBall) > 90) || presetToBackward) {
            if (angleToBall < 0) {
                actualAngleError = Math.toRadians(-180 - angleToBall);
            } else {
                actualAngleError = Math.toRadians(180 - angleToBall);
            }
            bot.angularVelocity = actualAngleError * parameters.get("kp") * -1;
            bot.linearVelocity = parameters.get("speed")/10.0 * -1;
            isCurrentDirectionForward = false;
        } else {
            actualAngleError =  Math.toRadians(angleToBall);
            bot.angularVelocity = actualAngleError * parameters.get("kp");
            bot.linearVelocity = parameters.get("speed")/10.0;
            isCurrentDirectionForward = true;
        }
        isPreviousDirectionForward = isCurrentDirectionForward;

        //kick ball into goal
        double angleToGoal = angleDifferenceFromGoal(bot.getXPosition(), bot.getYPosition(), bot.getTheta()); //degrees
        if (actualAngleError < 0.5 && ballX > bot.getXPosition()) { //facing ball
            double m = (ballY-bot.getYPosition()) / (ballX - bot.getXPosition());
            double c = ballY - (m * ballX);
            double x = 220;

            double y = m *x + c;
            if (y > 70 && y < 110) { //intercept with goal line is in goal
                bot.linearVelocity *= 3;
            //    System.out.println("kicking for goal!");
            }
        }

        //charge ball into goal
        double range = 10;
        if (isCharging) {
            range = 30;
        }
        if (distanceToBall < range && Math.abs(actualAngleError) < Math.PI/10 /* radians*/) {
            bot.linearVelocity = isCurrentDirectionForward ? 1 : -1;
            if (targetX > 110) {
                if (Math.abs(angleToGoal) > 45) {
                    if (angleToGoal > 0 && isCurrentDirectionForward || angleToGoal < 0 && !isCurrentDirectionForward) {
                        bot.angularVelocity = 30;
                    } else {
                        bot.angularVelocity = -30;
                    }
                }
            }
            isCharging = true;
        } else {
            isCharging = false;
        }

    }


}