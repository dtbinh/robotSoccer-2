package bot;
import org.jbox2d.collision.shapes.PolygonShape;

public class SimRobot extends Robot {

	public SimRobot(double x, double y, double theta, int id) {
		super(x, y, theta, id);
	}
	
	@Override
	public void moveLinear() {
		setX(getXPosition() + (51.39*2 / 18.52 * linearVelocity * Math.cos(Math.toRadians(getTheta()))));
		setY(getYPosition() + (51.39*2 / 18.52 * linearVelocity * -Math.sin(Math.toRadians(getTheta()))));
	}

	@Override
	public void moveAngular() {
		
		//actual  (34.39sec/10 rotation)
		//simulation(57.16 sec/5 rotation) 
		setTheta(getTheta() + ((57.16/5)/(34.39/10))*angularVelocity);
	}

}