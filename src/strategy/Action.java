package strategy;

import Paths.Path;
import actions.MoveToSpot;
import bot.Robot;
import bot.Robots;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Wesley on 21/01/2015.
 */
public abstract class Action {
	protected Robot bot;
	protected Robots opponentRobots;
	protected Robots teamRobots;
	protected double ballX;
	protected double ballY;
	protected double predX;
	protected double predY;
	protected Path path;
    protected HashMap<String, Integer> parameters = new HashMap<String, Integer>();
//	protected static KalmanFilter kFilter = new KalmanFilter();

	public String getName() {
        return getClass().getSimpleName();
    }

	public void addRobot (Robot bot) {
		this.bot = bot;
	}
	
	public void addTeamRobots (Robots team) {
		this.teamRobots = team;
        MoveToSpot.addTeamRobotsToMoveToSpot(team);
	}

	public void addOpponentRobots (Robots opponent) {
        this.opponentRobots = opponent;
        MoveToSpot.addOpponentRobotsToMoveToSpot(opponent);
    }

	public void setBallPosition(double x, double y) {
		this.ballX = x;
		this.ballY = y;
	}

    public Set<String> getParameters() {
        return parameters.keySet();
    }

    public Collection<Integer> getValues() {
        return parameters.values();
    }

    public void updateParameters(String key, int value) {
        parameters.put(key, value);
    }

	@Override
	public String toString() {
		return getName();
	}

	public abstract void execute();

	public void setPredBallPosition(double predictedBallX, double predictedBallY) {
		this.predX = predictedBallX;
		this.predY = predictedBallY;
	}

	/**
	 * <p>Loads fuzzy file and returns FunctionBlock object.</p>
	 * <p>When FunctionBlock is returned</p>
	 * @param filename
	 * @return FunctionBlock object
	 */
	protected FunctionBlock loadFuzzy(String filename) {
		FIS fis = FIS.load(filename);

		if (fis == null) {
			System.err.println("Can't load file: '" + filename + "'");
			System.exit(1);
		}

		// Get default function block
		FunctionBlock fb = fis.getFunctionBlock(null);

		return fb;
	}

	/**
	 *
	 * @param r
	 * @param x
	 * @param y
	 * @return theta in degrees
	 */
    protected static double getTargetTheta(Robot r, double x, double y) {
        double targetTheta = Math.atan2(r.getYPosition() - y, x - r.getXPosition());
        double difference = targetTheta - Math.toRadians(r.getTheta());
        //some hack to make the difference -Pi < theta < Pi
        if (difference > Math.PI) {
            difference -= (2 * Math.PI);
        } else if (difference < -Math.PI) {
            difference += (2 * Math.PI);
        }
        return Math.toDegrees(difference);
    }

	protected double getDistanceToTarget(Robot r, double targetX, double targetY) {
		return Math.sqrt(squared(targetX - r.getXPosition()) + squared(targetY - r.getYPosition()));
	}

	protected double angleDifferenceFromGoal(double x, double y, double theta) {
		double targetTheta = Math.atan2(y - 90, 220 - x);
		double difference = targetTheta - Math.toRadians(theta);
		//some hack to make the difference -Pi < theta < Pi
		if (difference > Math.PI) {
			difference -= (2 * Math.PI);
		} else if (difference < -Math.PI) {
			difference += (2 * Math.PI);
		}
		return Math.toDegrees(difference);
	}

    protected static double squared (double x) {
        return x * x;
    }

	public void draw(Graphics2D g) {};

	public void react(MouseEvent e) {};
}
