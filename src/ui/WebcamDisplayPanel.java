package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.opencv.core.Mat;

import utils.ColorSpace;
import utils.Image;
import controllers.VisionController;

/**
 * <p>Displays the webcam on the JPanel.</p>
 * <p>{@link ui.RobotSoccerMain}</p>
 * <p>{@link controllers.WebcamController}</p>
 * @author Chang Kon, Wesley, John
 *
 */

@SuppressWarnings("serial")
public class WebcamDisplayPanel extends JPanel {

    private final Color DETECTDISPLAYCOLOR = Color.CYAN;
	private ViewState currentViewState;
	private JLabel webcamImageLabel = new JLabel();
    private ArrayList<WebcamDisplayPanelListener> wdpListeners;
    private SamplingPanel samplingPanel;
    private boolean isFiltering = false;
    private BufferedImage zoomCursorImg;
    private Cursor zoomCursor;
    
	public WebcamDisplayPanel() {
		super();
		
		// Initially not connected to anything.
		currentViewState = ViewState.UNCONNECTED;
		wdpListeners = new ArrayList<WebcamDisplayPanelListener>();
		setLayout(new BorderLayout());
		setBackground(Color.BLACK);
		
		try {
			zoomCursorImg = ImageIO.read(getClass().getClassLoader().getResourceAsStream("zoom.png"));
			zoomCursor = Toolkit.getDefaultToolkit().createCustomCursor(zoomCursorImg, new Point(zoomCursorImg.getWidth() / 2, zoomCursorImg.getHeight() / 2), "Zoom cursor");
		} catch (IOException e1) {
			e1.printStackTrace();
			System.err.println("Could not find zoom.png file");
		}

        webcamImageLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                for (WebcamDisplayPanelListener listener : wdpListeners) {
                    if (listener instanceof ColourPanel) {
                        ColourPanel cp = (ColourPanel) listener;
                        
                        // Get the current bufferedimage.
                        BufferedImage image = getWebcamBufferedImage();

                        int x = e.getX() - zoomCursorImg.getWidth() / 2;
                        int y = e.getY() - zoomCursorImg.getHeight() / 2;

                        // Crop the image
                        BufferedImage crop = image.getSubimage(x, y, zoomCursorImg.getWidth(), zoomCursorImg.getHeight());
                        cp.setZoomLabelIcon(crop);
                        
                        if (cp.getIsGettingRobotDimension()) {
                            cp.setRobotDimension(e.getX(), e.getY());
                        }
                        
                    } else if (listener instanceof VisionPanel) {
                        VisionPanel panel = (VisionPanel) listener;
                        if (panel.isSelectedTab()) {
             //               panel.updateMousePoint(e.getX(), e.getY(), img.getBufferedImage());
                        	System.out.println(VisionController.imagePosToActualPos(new org.opencv.core.Point(e.getX(), e.getY())));
                        }
                    }
                }
            }
        });
	}
	
	/**
	 * <p>Receives webcam and updates view.</p>
	 * <p>Notifies all listeners of view state change</p>
	 * <p>{@link ui.WebcamDisplayPanelListener}</p>
	 * <p>{@link controllers.WebcamController}</p>
	 * @param webcam
	 */
	
	public void update(Mat mat) {
		
		ViewState oldViewState = currentViewState;
		
		if (mat == null) {
			
			/* 
			 * This assumes that you cannot have a connection fail if you're already connected hence you are disconnecting.
			 * If you are unconnected and you get a null image, connection has failed.
			 */
			
			if (currentViewState == ViewState.UNCONNECTED) {
				currentViewState = ViewState.connectionFail();
			} else if (currentViewState == ViewState.CONNECTED) {
				removeAll();
				currentViewState = ViewState.disconnect();
			}
			
		} else {
			currentViewState = ViewState.connectionSuccess();

            final BufferedImage image = Image.toBufferedImage(mat);

            if (isFiltering) {
                //old stuff
                for (int j = 0; j < image.getHeight(); j++) {
                    for (int i = 0; i < image.getWidth(); i++) {
                        Color color = new Color(image.getRGB(i, j));

                        float[] hsv = ColorSpace.RGBToHSV(color.getRed(), color.getGreen(), color.getBlue());

                        if (isDetected((int)hsv[0], (int)hsv[1], (int)hsv[2])) {
                            image.setRGB(i, j, DETECTDISPLAYCOLOR.getRGB());
                        }
                    }
                }
            }

            /*
             * This method is not being called EDT thread so to update the GUI use invokeLater.
             */
            SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					// Update the image.
					webcamImageLabel.setIcon(new ImageIcon(image));
					
					if (webcamImageLabel.getParent() == null) {
						add(webcamImageLabel, BorderLayout.CENTER);
					}
				}
            	
            });
            
            notifyImageUpdate(image);
		}
		
		if (oldViewState != currentViewState) {
			notifyViewStateChange(currentViewState);
		}

		// Thread safe call.
		repaint();
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		// Get the current state of the displayPanel. Draw text onto screen.
		switch(currentViewState) {
		case CONNECTED:
			break;
		default:
			g.setColor(Color.WHITE);

			// Find width and height of the display panel.
			int width = getWidth();
			int height = getHeight();
			
			String displayMessage = currentViewState.getMessage();
			
			FontMetrics fm = g.getFontMetrics();
			int displayMessageX = (width - fm.stringWidth(displayMessage)) / 2;
			int displayMessageY = (fm.getAscent() + (height - (fm.getAscent() + fm.getDescent())) / 2);
			
			g.drawString(displayMessage, displayMessageX, displayMessageY);
		}
	}

    public boolean isDetected(int h, int s, int v) {
        if (!((h >= samplingPanel.getLowerBoundForH()) && (h <= samplingPanel.getUpperBoundForH()))) {
            return false;
        }

        if (!((s >= samplingPanel.getLowerBoundForS()) && (s <= samplingPanel.getUpperBoundForS()))) {
            return false;
        }

        if (!((v >= samplingPanel.getLowerBoundForV()) && (v <= samplingPanel.getUpperBoundForV()))) {
            return false;
        }

        return true;
    }

    public void setIsFiltering(boolean bool) {
        isFiltering = bool;
    }

    public void setSamplingPanel(SamplingPanel sp) {
        this.samplingPanel = sp;
    }
    
    /**
     * <p>Gets the bufferedimage used in display</p>
     * @return BufferedImage of webcam
     */
    
    public BufferedImage getWebcamBufferedImage() {
    	ImageIcon icon = (ImageIcon)webcamImageLabel.getIcon();
    	return (BufferedImage)icon.getImage();
    }
    
	/**
	 * <p>Add instance to be an observer</p>
	 * @param WebcamDisplayPanelListener instance
	 */
	
	public void addWebcamDisplayPanelListener(WebcamDisplayPanelListener l) {
		wdpListeners.add(l);
	}
	
	/**
	 * <p>Remove the instance from observer list</p>
	 * @param WebcamDisplayPanelListener instance
	 */
	
	public void removeWebcamDisplayPanelListener(WebcamDisplayPanelListener l) {
		wdpListeners.remove(l);
	}
	
	/**
	 * <p>Notify all observers of view state change</p>
	 */
	
	public void notifyViewStateChange(ViewState currentViewState) {
		for (WebcamDisplayPanelListener l : wdpListeners) {
			l.viewStateChanged(currentViewState);
		}
	}

	/**
	 * <p>Notify all observers of image update on webcamImageLabel</p>
	 * @param image
	 */
	
	public void notifyImageUpdate(BufferedImage image) {
		for (WebcamDisplayPanelListener l : wdpListeners) {
			l.imageUpdated(image);
		}
	}
	
	public void setZoomCursor() {
		setCursor(zoomCursor);
	}
	
	public void setDefaultCursor() {
		setCursor(Cursor.getDefaultCursor());
	}
	
	/**
	 * <p>Returns the current view state of the WebcamDisplayPanel</p>
	 * @return currentViewState
	 */
	
    public ViewState getViewState() {
        return currentViewState;
    }
    
    /**
	 * <p>Defines the <strong>state</strong> of the display.</p>
	 * <p>Each state has a <strong>display message</strong></p>
	 * @author Chang Kon, Wesley, John
	 *
	 */
	
	public enum ViewState {
		
		UNCONNECTED("Software is not connected to a webcam device"),
		CONNECTED("Connection success"),
		ERROR("An error has occurred! Please fix");
		
		private String displayMessage;
		
		private ViewState(String displayMessage) {
			this.displayMessage = displayMessage;
		}
		
		private String getMessage() {
			return displayMessage;
		}
		
		private static ViewState connectionSuccess() {
			return CONNECTED;
		}
		
		private static ViewState connectionFail() {
			return ERROR;
		}
		
		private static ViewState disconnect() {
			return UNCONNECTED;
		}
		
	}
	
}
