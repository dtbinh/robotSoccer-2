package criteria;

import bot.Robot;
import strategy.Criteria;

/**
 * Created by Wesley on 21/01/2015.
 */
public class closestToBall extends Criteria{
    @Override
    public String getName() {
        return "Closest to Ball";
    }

    @Override
    public boolean isMet() {
        Robot r = bots.getRobot(index);
        double x = r.getXPosition();
        double y = r.getYPosition();
        double distance = Math.sqrt(squared(x-ballX) + squared(y-ballY));

        for (int i = 0; i < 5; i++) {
            if (i == index) {
                continue;
            } else {
                Robot r2 = bots.getRobot(index);
                double x2 = r2.getXPosition();
                double y2 = r2.getYPosition();
                double distanceOther = Math.sqrt(squared(x2-ballX) + squared(y2-ballY));
                if (distanceOther < distance) {
                    return false;
                }
            }
        }
        return true;
    }
}