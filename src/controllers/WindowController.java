package controllers;

import config.ConfigPreviousFile;
import strategy.CurrentStrategy;
import ui.WebcamDisplayPanel.ViewState;
import vision.VisionSettingFile;

import javax.swing.*;
import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class WindowController implements WindowListener, KeyListener, KeyEventDispatcher {
	private static WindowController windowController;

	private WebcamController webcamController;
	private HashMap<Integer,Boolean> keyMapping = new HashMap<Integer,Boolean>();
	private CurrentStrategy currentStrategy;
	private VisionSettingFile visionSetting;
    List<WindowControllerListener> listeners = new ArrayList<WindowControllerListener>();

    public static WindowController getWindowController() {
        if (windowController == null) {
            windowController = new WindowController();
        }
        return windowController;
    }

    public void setFieldsForWindowController(WebcamController wc, CurrentStrategy strategy, VisionSettingFile vision) {
        currentStrategy = strategy;
        webcamController = wc;
        visionSetting = vision;
    }

    public void addListener(WindowControllerListener wcl) {
        listeners.add(wcl);
    }

	@Override
	public void windowActivated(WindowEvent arg0) {}

	@Override
	public void windowClosed(WindowEvent arg0) {}

	@Override
	public void windowClosing(WindowEvent arg0) {
		ViewState webcamStatus = webcamController.getWebcamStatus();
		if (webcamStatus == ViewState.CONNECTED) {
			webcamController.disconnect();
		}
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {}

	@Override
	public void windowDeiconified(WindowEvent e) {}

	@Override
	public void windowIconified(WindowEvent e) {}

	@Override
	public void windowOpened(WindowEvent e) {}

	@Override
	public void keyPressed(KeyEvent arg0) {
		System.out.println(arg0.getKeyCode());
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		
		 if (e.getID() == KeyEvent.KEY_PRESSED) {
             //System.out.println(e.getKeyCode());
             keyMapping.put(e.getKeyCode(), true);
             if (keyMapping.get(17) != null && keyMapping.get(83) != null) {
                 if (currentStrategy.openedStratFile) {
                     visionSetting.save(ConfigPreviousFile.getInstance().getPreviousVisionFile());
                     currentStrategy.save(ConfigPreviousFile.getInstance().getPreviousStratFile());
                     JOptionPane.showMessageDialog(null,"Strat and Vision File Saved");
                 }
             } else if (keyMapping.get(KeyEvent.VK_UP) != null) {
                 for (WindowControllerListener wcl : listeners) {
                     wcl.windowKeyPressed("up");
                 }
             } else if (keyMapping.get(KeyEvent.VK_DOWN) != null) {
                 for (WindowControllerListener wcl : listeners) {
                     wcl.windowKeyPressed("down");
                 }
             } else if (keyMapping.get(KeyEvent.VK_LEFT) != null) {
                 for (WindowControllerListener wcl : listeners) {
                     wcl.windowKeyPressed("left");
                 }
             } else if (keyMapping.get(KeyEvent.VK_RIGHT) != null) {
                 for (WindowControllerListener wcl : listeners) {
                     wcl.windowKeyPressed("right");
                 }
             }

         } else if (e.getID() == KeyEvent.KEY_RELEASED) {
        	 keyMapping.put(e.getKeyCode(), null);
             for (WindowControllerListener wcl : listeners) {
                 wcl.windowKeyPressed("release");
             }
         }
		return false;
	}

}
