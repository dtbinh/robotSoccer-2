package actions;

import strategy.Action;

/**
 * Created by Wesley on 11/07/2015.
 */
public class FreeBall extends Action {

    {
        parameters.put("speed", 3);
    }


    @Override
    public void execute() {
//        int speed = parameters.get("speed");
//
//        if (speed > 100) {
//            speed = -(speed - 100);
//        }
//        bot.linearVelocity = speed;
//        bot.angularVelocity = 10;

        double distanceToBall = getDistanceToTarget(bot, ballX, ballY);

        if (distanceToBall < 20) {
  //          System.out.println("spinning");
            if (bot.getYPosition() > 90) {
                bot.angularVelocity = 30;
                bot.linearVelocity = 0;
            } else {
                bot.angularVelocity = -30;
                bot.linearVelocity = 0;
            }
        } else {
            bot.linearVelocity = 5;
            bot.angularVelocity = 0;
        }

    }

}
