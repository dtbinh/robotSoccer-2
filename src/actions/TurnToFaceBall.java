package actions;

import Paths.StraightLinePath;
import bot.Robot;
import strategy.Action;

/**
 * Created by Wesley on 21/01/2015.
 */
public class TurnToFaceBall extends Action{
    @Override
    public String getName() {
        return "Turn to ball (trial)";
    }

    @Override
    public void execute() {
        Robot r = bots.getRobot(index);

        double ballTheta = Math.atan2(r.getYPosition() - ballY, ballX - r.getXPosition());
        double difference = ballTheta - Math.toRadians(r.getTheta());
        //some hack to make the difference -Pi < theta < Pi
        if (difference > Math.PI) {
            difference -= (2 * Math.PI);
        } else if (difference < -Math.PI) {
            difference += (2 * Math.PI);
        }

        double errorMargin = 0.8;
        if (Math.abs(difference) >= errorMargin) {
            if (difference > 0) {
                r.angularVelocity = 2*Math.PI;
            } else {
                r.angularVelocity = -2*Math.PI;
            }
        } else if (Math.abs(difference) >= errorMargin/2) {
            if (difference > 0) {
                r.angularVelocity = Math.PI;
            } else {
                r.angularVelocity = Math.PI;
            }
        } else if (Math.abs(difference) >= errorMargin/4) {
            if (difference > 0) {
                r.angularVelocity = Math.PI/2;
            } else {
                r.angularVelocity = Math.PI/2;
            }
            r.linearVelocity = 0;
        } else {
            r.angularVelocity = 0;
        }
        r.linearVelocity = 0;


    //    System.out.println(System.currentTimeMillis());

    }

    protected double squared (double x) {
        return x * x;
    }
}