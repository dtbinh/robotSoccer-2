package utils;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

/**
 * <p>Consists of static utility classes when working with images</p>
 * @author Chang Kon, Wesley, John
 *
 */

public class Image {

	/**
	 * <p>Takes a source image and resizes it to the given width and height value</p>
	 * @param image
	 * @param width
	 * @param height
	 * @return resized bufferedimage
	 */

	public static BufferedImage resize(BufferedImage image, int width, int height) {
		BufferedImage resize = new BufferedImage(width, height, image.getType());
		Graphics2D g2d = resize.createGraphics();
		g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
		g2d.drawImage(image, 0, 0, width, height, null);
		g2d.dispose();
		return resize;
	}

	/**
	 * <p>Finds the euclidean distance between two points and returns it</p>
	 * @param p1
	 * @param p2
	 * @return euclidean distance between two points
	 */
	
    public static double euclideanDistance(org.opencv.core.Point p1, org.opencv.core.Point p2) {
        return Math.sqrt(Math.pow((p2.x - p1.x), 2) + Math.pow((p2.y - p1.y), 2));
    }

    /**
     * <p>Calculates the angle between the two points, respect to the x axis in degrees</p>
     * @param p1
     * @param p2
     * @return angle between two points in degrees, respect to x axis. <strong>Note:</strong> range -180 to 180.
     */
    
    public static double angleBetweenTwoPoints(org.opencv.core.Point p1, org.opencv.core.Point p2) {
        return Math.toDegrees(Math.atan2(p2.y - p1.y, p2.x - p1.x));
    }
    
    /**
     * <p>Transforms mat matrix into bufferedImage</p>
     * @param Mat matrix
     * @return bufferedImage
     */
    
    public static BufferedImage toBufferedImage(Mat matrix) {
        int type = BufferedImage.TYPE_BYTE_GRAY;

        if ( matrix.channels() > 1 ) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }

        int bufferSize = matrix.channels()*matrix.cols()*matrix.rows();
        byte [] b = new byte[bufferSize];

        matrix.get(0, 0, b); // get all the pixels
        BufferedImage image = new BufferedImage(matrix.cols(), matrix.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

        System.arraycopy(b, 0, targetPixels, 0, b.length);
        return image;
    }
    
    /**
     * <p>Transforms bufferedimage to Mat</p>
     * @param BufferedImage image
     * @return Mat
     */
    
    public static Mat toMat(BufferedImage image) {
    	byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
    	Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
    	mat.put(0, 0, data);
    	return mat;
    }
    
}
