import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.Date;
import java.util.regex.*;
import java.util.Timer;
import java.util.TimerTask;
import java.text.DateFormat;

public class JRatioBoost {
	
	private static JFrame frame;
	private JPanel panel1;
	private JButton openFileButton;
	private JSpinner spinner1;
	private JSpinner spinner2;
	private JButton connectButton;
	private JLabel torrent_name;
	private JLabel tracker;
	private JLabel info_hash;
	private JLabel peer_id;
	private JLabel size;
	private JLabel seeders;
	private JLabel leechers;
	private JLabel update;
	private JLabel uploaded;
	private JLabel downloaded;
	private JLabel date;
	private JPopupMenu menu;
	private JMenuItem[] trackerList;
	private JMenuItem about;
	private JMenuItem updateInterval;
	private JMenu changeTracker;
	
	long upAmount = 0;
	TorrentInfo tInfo;
	TrackerConnect tc;
	Timer timer;


	public JRatioBoost() {

		frame = new JFrame("JRatioBoost");
		frame.setContentPane(panel1);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
		
		//set up menu
		menu = new JPopupMenu();
		about = new JMenuItem("About");
		updateInterval = new JMenuItem("Update Interval");
		changeTracker = new JMenu("Tracker");
		changeTracker.setEnabled(false);
		menu.add(changeTracker);
		menu.add(updateInterval);
		menu.addSeparator();
		menu.add(about);
		
		//attach action listeners to UI widgets
		//open button
		openFileButton.addActionListener(new OpenAction());
		connectButton.addActionListener(new ConnectAction());
		panel1.addMouseListener(new PopupAction());
		about.addActionListener(new AboutAction());
		updateInterval.addActionListener(new UpdateAction());
	}
	
	public static void main(String[] args) {
		
		SwingUtilities.invokeLater(new Runnable() {
			
			public void run() {
				
				new JRatioBoost();
			}
		});
	}
	
	class OpenAction implements ActionListener {
	
		@Override
		public void actionPerformed(ActionEvent e) {
		
			FileDialog fd = new FileDialog(frame, "Choose a file", FileDialog.LOAD);
			fd.setFile("*.torrent");
			fd.setVisible(true);
			changeTracker.setEnabled(false);
		
			if (fd.getFile() != null) {
				
				tInfo = new TorrentInfo(fd.getFiles()[0].getPath());
				//update Labels
				Pattern p = Pattern.compile("\\.\\w+\\.\\w+{2,3}");
				Matcher m = p.matcher(tInfo.announce);
				
				if (m.find()) {
					
					tracker.setText("<html><a href=\"www.google.com\">" + m.group().substring(1) + "</a></html>");
					tracker.setCursor(new Cursor(Cursor.HAND_CURSOR));
					
				} else {
					
					tracker.setText(tInfo.announce);
				}
				
				torrent_name.setText("<html><font size=5>" + tInfo.name + "</font><html>");
				info_hash.setText(tInfo.hexString(tInfo.infoHash));
				peer_id.setText(tInfo.hexString(tInfo.peerId));
				size.setText(new SizeConvert(Long.parseLong(tInfo.size)).toString());
				Date d = new Date(Long.parseLong(tInfo.creationDate) * 1000);
				date.setText(DateFormat.getDateInstance().format(d));
			
				//if torrent has multiple trackers listed, add them to a popumenu list
				int arrSize = tInfo.announceList.size();

				if (arrSize > 0) {
					
					changeTracker.setEnabled(true);
					
					for (String val : tInfo.announceList) {
					
						changeTracker.add(val);
					}
				}
			}
			
		}
	}

	class ConnectAction implements ActionListener {
	
		@Override
		public void actionPerformed(ActionEvent e) {
		
			//connect button was pressed
			if (connectButton.getText().equals("Connect")) {
				
				tc = new TrackerConnect(tInfo);
				connectButton.setText("Stop");
				connectButton.setIcon(new ImageIcon(getClass().getResource("/javax/swing/plaf/metal/icons/ocean/close.gif")));
				upAmount = 0;	
				
				//send request at regular intervals
				timer = new Timer();
				timer.scheduleAtFixedRate(new UpdateTask(), 1000, 1000);
				
			//stop button was pressed
			} else {
				
				connectButton.setText("Connect");
				connectButton.setIcon(new ImageIcon(getClass().getResource("/javax/swing/plaf/metal/icons/ocean/computer.gif")));
				timer.cancel();
			}
		}
	}
	
	class UpdateAction implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			
			UpdateAmount ua = new UpdateAmount(tc);
			ua.pack();
			ua.setLocationByPlatform(true);			
			ua.setVisible(true);
		}
	}
	
	class AboutAction implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			
			About a = new About();
			a.pack();
			a.setLocationByPlatform(true);			
			a.setVisible(true);
		}
	}
	
	class PopupAction extends MouseAdapter {
		
		@Override
		public void mousePressed(MouseEvent me) {
			
			if (me.isPopupTrigger()) {
					
				menu.show(me.getComponent(), me.getX(), me.getY());
			}
		}
	}
	
	class UpdateTask extends TimerTask {
		
		@Override
		public void run() {
			
			int upSpeed = (Integer) spinner1.getValue();
			upAmount += SizeConvert.KBToB(upSpeed);
			
			//update GUI label with tracker response info
			seeders.setText(tc.seeders);
			leechers.setText(tc.leechers);
			update.setText(tc.interval);
			downloaded.setText("100%");
			uploaded.setText(new SizeConvert(upAmount).toString());
			
			int nextUpdate = Integer.parseInt(update.getText());
			
			if (nextUpdate <= 0) {
				
				//stop the timer thread from executing to prevent the timer task
				//from continuing to execute while the http request is being made
				//by the TrackerConnect instance
				timer.cancel();
				
				//send get request to tracker with upload/download data
				tc.connect(String.format("%d", upAmount), "0");
				nextUpdate = Integer.parseInt(tc.interval);
				
				//the new http request has been made, start a new timer task
				timer = new Timer();
				timer.scheduleAtFixedRate(new UpdateTask(), 1000, 1000);
			}
			
			nextUpdate--;
			
			tc.interval = String.format("%d", nextUpdate);
		}
	}
	
	{
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
		$$$setupUI$$$();
	}
	
	/**
	 * Method generated by IntelliJ IDEA GUI Designer
	 * >>> IMPORTANT!! <<<
	 * DO NOT edit this method OR call it in your code!
	 *
	 * @noinspection ALL
	 */
	private void $$$setupUI$$$() {
		panel1 = new JPanel();
		panel1.setLayout(new GridBagLayout());
		panel1.setMinimumSize(new Dimension(450, 400));
		panel1.setPreferredSize(new Dimension(450, 400));
		panel1.setRequestFocusEnabled(false);
		final JPanel panel2 = new JPanel();
		panel2.setLayout(new GridBagLayout());
		GridBagConstraints gbc;
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(5, 5, 5, 5);
		panel1.add(panel2, gbc);
		openFileButton = new JButton();
		openFileButton.setIcon(new ImageIcon(getClass().getResource("/javax/swing/plaf/metal/icons/ocean/directory.gif")));
		openFileButton.setText("Open File");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.ipadx = 5;
		gbc.ipady = 5;
		panel2.add(openFileButton, gbc);
		torrent_name = new JLabel();
		torrent_name.setText("");
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 5, 0, 5);
		panel2.add(torrent_name, gbc);
		final JPanel panel3 = new JPanel();
		panel3.setLayout(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(5, 5, 5, 5);
		panel1.add(panel3, gbc);
		panel3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Torrent Info"));
		final JLabel label1 = new JLabel();
		label1.setText("<html><b>Tracker:</b></html>");
		label1.putClientProperty("html.disable", Boolean.FALSE);
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(5, 5, 0, 5);
		panel3.add(label1, gbc);
		final JLabel label2 = new JLabel();
		label2.setText("<html><b>Info Hash:</b></html>");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 5, 0, 0);
		panel3.add(label2, gbc);
		final JLabel label3 = new JLabel();
		label3.setText("<html><b>Size:</b></html>");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 5, 0, 0);
		panel3.add(label3, gbc);
		final JLabel label4 = new JLabel();
		label4.setText("<html><b>Peer ID:</b></html>");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 5, 0, 0);
		panel3.add(label4, gbc);
		tracker = new JLabel();
		tracker.setText("");
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.insets = new Insets(0, 5, 0, 0);
		panel3.add(tracker, gbc);
		info_hash = new JLabel();
		info_hash.setText("");
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.insets = new Insets(0, 5, 0, 0);
		panel3.add(info_hash, gbc);
		peer_id = new JLabel();
		peer_id.setText("");
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.insets = new Insets(0, 5, 0, 0);
		panel3.add(peer_id, gbc);
		size = new JLabel();
		size.setText("");
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(0, 5, 0, 0);
		panel3.add(size, gbc);
		final JLabel label5 = new JLabel();
		label5.setText("<html><b>Date:</b></html>");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(0, 5, 5, 0);
		panel3.add(label5, gbc);
		date = new JLabel();
		date.setText("");
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 4;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(0, 5, 5, 0);
		panel3.add(date, gbc);
		final JPanel panel4 = new JPanel();
		panel4.setLayout(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(5, 5, 5, 5);
		panel1.add(panel4, gbc);
		panel4.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Tracker Input/Ouput"));
		final JLabel label6 = new JLabel();
		label6.setText("<html><b>Seeders:</b></html>");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(5, 5, 0, 5);
		panel4.add(label6, gbc);
		final JLabel label7 = new JLabel();
		label7.setText("<html><b>Leechers:</b></html>");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 5, 0, 5);
		panel4.add(label7, gbc);
		seeders = new JLabel();
		seeders.setText("");
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		panel4.add(seeders, gbc);
		leechers = new JLabel();
		leechers.setText("");
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		panel4.add(leechers, gbc);
		final JLabel label8 = new JLabel();
		label8.setText("<html><b>Downloaded:</b></html>");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 5, 0, 5);
		panel4.add(label8, gbc);
		final JLabel label9 = new JLabel();
		label9.setText("<html><b>Uploaded:</b></html>");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 5, 0, 5);
		panel4.add(label9, gbc);
		final JLabel label10 = new JLabel();
		label10.setText("<html><b>Update Interval:</b></html>");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 5, 5, 5);
		panel4.add(label10, gbc);
		uploaded = new JLabel();
		uploaded.setText("");
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.anchor = GridBagConstraints.WEST;
		panel4.add(uploaded, gbc);
		update = new JLabel();
		update.setText("");
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 4;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel4.add(update, gbc);
		downloaded = new JLabel();
		downloaded.setText("");
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.WEST;
		panel4.add(downloaded, gbc);
		final JPanel panel5 = new JPanel();
		panel5.setLayout(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.anchor = GridBagConstraints.SOUTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(5, 5, 5, 5);
		panel1.add(panel5, gbc);
		final JLabel label11 = new JLabel();
		label11.setIcon(new ImageIcon(getClass().getResource("/javax/swing/plaf/metal/icons/sortUp.png")));
		label11.setText("<html><b>Upload Speed:</b></html>");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		panel5.add(label11, gbc);
		final JLabel label12 = new JLabel();
		label12.setIcon(new ImageIcon(getClass().getResource("/javax/swing/plaf/metal/icons/sortDown.png")));
		label12.setText("<html><b>Download Speed:</b></html>");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		panel5.add(label12, gbc);
		connectButton = new JButton();
		connectButton.setIcon(new ImageIcon(getClass().getResource("/javax/swing/plaf/metal/icons/ocean/computer.gif")));
		connectButton.setText("Connect");
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.gridheight = 2;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		panel5.add(connectButton, gbc);
		spinner1 = new JSpinner();
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 5, 5, 5);
		panel5.add(spinner1, gbc);
		spinner2 = new JSpinner();
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 5, 0, 5);
		panel5.add(spinner2, gbc);
	}
	
	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return panel1;
	}
}
