package ui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import communication.NetworkSocket;
import communication.Receiver;
import communication.Sender;
import communication.SerialPortCommunicator;
import bot.Robots;
import controllers.BallController;
import controllers.FieldController;
import game.Tick;
import net.miginfocom.swing.MigLayout;
import strategy.CurrentStrategy;
import strategy.Role;

public class RobotSoccerMain extends JPanel
                             implements ActionListener {

	public static final int DEFAULT_PORT_NUMBER = 31000;
    private JButton startButton;
    private JButton saveButton = new JButton("Save to File");
    private JButton openButton = new JButton("Open");
    private JTextArea taskOutput;

    private NetworkSocket serverSocket;
    private FieldController fieldController;
    private BallController ballController;
    private Field field;
    private Ball ball;
    private JTextField portField;
    private RobotInfoPanel[] robotInfoPanels;
    private TestComPanel testComPanel;
    private SerialPortCommunicator serialCom;
    private Robots bots;
    
    private Tick gameTick;
    
    private SituationPanel situationPanel;
    private PlaysPanel playsPanel;
    private RolesPanel rolesPanel;
    private CurrentStrategy currentStrategy;
    
    private JTabbedPane tabPane;
	private DrawAreaGlassPanel glassPanel;

    public RobotSoccerMain() {
        super(new BorderLayout());
        
        //Create the demo's UI.
        //create start button and text field for port number
        startButton = new JButton("Start");
        startButton.setActionCommand("start");
        startButton.addActionListener(this);
        portField = new JTextField(10);
        
        JPanel panel = new JPanel(new MigLayout());
        panel.add(startButton, "gapx 250");
        panel.add(portField);
        panel.add(saveButton);
        panel.add(openButton);
        
        taskOutput = new JTextArea(5, 20);
        taskOutput.setMargin(new Insets(5,5,5,5));
        taskOutput.setEditable(false); 
       
        
        //create serial port communicator;
        serialCom = new SerialPortCommunicator();
        bots = new Robots(serialCom);
        bots.makeRealRobots();

        ball = new Ball();
        field = new Field(bots, ball);
        ballController = new BallController(ball);
        fieldController = new FieldController(field, bots, ball);
        
        //creating panel holding robot informations
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new FlowLayout());
        robotInfoPanels = new RobotInfoPanel[5];
        
        testComPanel = new TestComPanel(serialCom, bots);
        fieldController.setComPanel(testComPanel);
        infoPanel.add(testComPanel);
        
        for (int i = 0; i<5; i++) {
        	robotInfoPanels[i] = new RobotInfoPanel(bots.getRobot(i), i);
        	infoPanel.add(robotInfoPanels[i]);
        }

        currentStrategy = new CurrentStrategy(fieldController);
        field.setCurrentStrategy(currentStrategy);
        situationPanel = new SituationPanel(fieldController, currentStrategy);
        playsPanel = new PlaysPanel(currentStrategy);
        rolesPanel = new RolesPanel(currentStrategy);

        glassPanel = new DrawAreaGlassPanel(field, situationPanel);
		glassPanel.setVisible(false);
		field.add(glassPanel);
		situationPanel.setGlassPanel(glassPanel);
		
        //create tab pane
        tabPane = new JTabbedPane();
        
        tabPane.addTab("Output", new JScrollPane(taskOutput));
        tabPane.addTab("Situation", situationPanel);
        tabPane.addTab("Plays", playsPanel);
        tabPane.addTab("Roles", rolesPanel);

        tabPane.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				if (tabPane.getTitleAt(tabPane.getSelectedIndex()).equals("Situation")){
					fieldController.showArea(true);
				}
				else {
					fieldController.showArea(false);
				}
				fieldController.repaintField();
			}
			
        	
        });
         
        add(panel, BorderLayout.PAGE_START);
        
        add(tabPane, BorderLayout.LINE_END);
        
        add(field, BorderLayout.CENTER);
        
        add(infoPanel,BorderLayout.SOUTH);
        
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentStrategy.saveToFile();
            }
        });

        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentStrategy.readFromFile();
            }
        });
        
        gameTick = new Tick(field, bots, testComPanel);
        setUpGame();
    }

    public void setUpGame() {
        java.util.Timer timer = new java.util.Timer();
        timer.schedule(gameTick, 0, 5);
    }
    /**
     * Invoked when the user presses the start button.
     */
    public void actionPerformed(ActionEvent evt) {
        //Instances of javax.swing.SwingWorker are not reusuable, so
        //we create new instances as needed.
    	if (startButton.getText() == "Start") {
	    	int portNumber;
	    	try {
	    		portNumber = Integer.parseInt(portField.getText());
	    	}	catch (NumberFormatException e) {
	    		portNumber = DEFAULT_PORT_NUMBER;
	    		taskOutput.append("\nIncorrect character, will use default port: 31000\n");
	    	}
	    	

	    	serverSocket = new NetworkSocket(portNumber, taskOutput, startButton);
	    	System.out.println("created new socket");
	    	serverSocket.execute();
	    	serverSocket.addReceiverListener(fieldController);
	    	serverSocket.addSenderListener(gameTick);
	    	serverSocket.addSenderListener(testComPanel);
    	} else {
    		//tell the serverSocket to begin the closing procedure;
    		serverSocket.close();
    	}
    }

    /**
     * Create the GUI and show it. As with all GUI code, this must run
     * on the event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("Robot Soccer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        JComponent newContentPane = new RobotSoccerMain();
        frame.add(newContentPane);
        
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}