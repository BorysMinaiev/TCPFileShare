import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Main {

	static final int PORT = 7777;
	static String DIRECTORY = "shared_folder";
	static String NAME = "Borys Minaiev";

	private static Logger log = Logger.getLogger(Main.class.getName());

	private static void showChooseIpDialog() throws SocketException {
		List<InetAddress> ips = new ArrayList<>();
		Enumeration<NetworkInterface> e = NetworkInterface
				.getNetworkInterfaces();
		while (e.hasMoreElements()) {
			NetworkInterface n = (NetworkInterface) e.nextElement();
			Enumeration<InetAddress> ee = n.getInetAddresses();
			while (ee.hasMoreElements()) {
				InetAddress i = (InetAddress) ee.nextElement();
				if (i.getAddress().length == 4) {
					ips.add(i);
				}
			}
		}
		final String[] ipsList = new String[ips.size()];
		for (int i = 0; i < ipsList.length; i++) {
			ipsList[i] = ips.get(i).getHostAddress().toString();
		}
		final JComboBox<String> comboBox = new JComboBox<>(ipsList);
		final JFrame frame = new JFrame("Choose your ip address!");
		frame.setVisible(true);
		frame.setSize(250, 80);
		frame.setLocationRelativeTo(null);
		JPanel pan = new JPanel();
		frame.add(pan);
		pan.add(comboBox);
		JButton okBtn = new JButton("OK");
		pan.add(okBtn);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		okBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String ip = (String) comboBox.getSelectedItem();
				ipChosen(ip);
				frame.setVisible(false);
			}
		});
	}
	
	static InetAddress ip;

	private static void ipChosen(String s) {
		log.info("used ip = " + s);
		try {
			Main.ip = InetAddress.getByName(s);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		new Thread(new MyFilesManager(s)).start();
		TreeSet<Peer> peers = new TreeSet<>();
		try {
			new Thread(new UdpReceiver(new DatagramSocket(PORT), peers)).start();
			new Thread(new GUI(peers)).start();
			new Thread(new TCPServer()).start();
		} catch (SocketException e) {
			log.severe(e.toString());
		}
	}

	public static void main(String[] args) {
		try {
			showChooseIpDialog();
		} catch (SocketException e) {
			log.log(Level.SEVERE, "exception: ", e);
		}
	}
}
