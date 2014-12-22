import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class GUI implements Runnable {
	private static final long SLEEP_TIME = 10000;
	private static final long WAIT_FOR_PING = 10000_000;
	static TreeSet<Peer> peers;

	GUI(TreeSet<Peer> peers) {
		GUI.peers = peers;

	}

	private Peer nowViewPeer = null;
	private DefaultListModel<Peer> model;
	private DefaultListModel<String> model2;
	private JList<Peer> list;
	private JList<String> list2;
	final String dirName = Main.DIRECTORY;

	private void updateViews() {
		synchronized (peers) {
			boolean ok = peers.size() > 0;
			model.clear();
			model2.clear();
			if (nowViewPeer != null) {
				if (!peers.contains(nowViewPeer)) {
					nowViewPeer = null;
				}
			}
			if (nowViewPeer != null) {
				model2.addElement("..");
				if (nowViewPeer.ip.equals(Main.ip)) {
					File dir = new File(dirName);
					File[] filesList = dir.listFiles();
					for (File file : filesList) {
						if (file.isFile()) {
							model2.addElement(file.getName());
						}
					}
				} else {
					try {
						Socket socket = new Socket(nowViewPeer.ip, Main.PORT);
						InputStream is = socket.getInputStream();
						OutputStream os = socket.getOutputStream();
						os.write(Utils.writeByte(TCPServer.LIST));
						int x = is.read();
						if (x == TCPServer.RET_LIST) {
							int count = Utils.readInt(is);
							for (int i = 0; i < count; i++) {
								byte[] md5 = Utils.readMD5(is);
								String fileName = Utils.readString(is);
								model2.addElement(fileName);
							}
						}
						os.close();
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			List<Peer> remove = new ArrayList<Peer>();
			for (Peer p : peers) {
				if (p.updateTime + WAIT_FOR_PING < System.currentTimeMillis()) {
					remove.add(p);
				} else {
					model.addElement(p);
					if (nowViewPeer != null && p.compareTo(nowViewPeer) == 0) {
						list.setSelectedIndex(model.getSize() - 1);
					}
				}
			}
			if (model.getSize() == 0 && ok) {
				throw new AssertionError();
			}
			for (Peer p : remove) {
				peers.remove(p);
			}
		}
	}

	@Override
	public void run() {
		JFrame frame = new JFrame("All users");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel panel = new JPanel();
		model = new DefaultListModel<>();
		model2 = new DefaultListModel<>();
		list = new JList<Peer>(model);
		list2 = new JList<String>(model2);
		JScrollPane pane = new JScrollPane(list);
		JScrollPane pane2 = new JScrollPane(list2);
		panel.setLayout(new GridLayout(2, 1));
		panel.add(pane);
		panel.add(pane2);
		frame.setContentPane(panel);
		frame.setSize(600, 400);
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);

		list.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					@Override
					public void valueChanged(ListSelectionEvent e) {
						if (list.getSelectedValue() != null
								&& (nowViewPeer == null || list
										.getSelectedValue().compareTo(
												nowViewPeer) != 0)) {
							nowViewPeer = list.getSelectedValue();
							updateViews();
						}
					}
				});

		list2.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				JList<String> list = (JList<String>) evt.getSource();
				if (evt.getClickCount() == 2) {
					int index = list.locationToIndex(evt.getPoint());
					if (index == 0) {
						nowViewPeer = null;
						updateViews();
					} else {
						final String fileName = list.getSelectedValue();
						if (nowViewPeer.ip.equals(Main.ip)) {
							synchronized (peers) {
								final Peer[] ipsList = new Peer[peers.size()];
								int it = 0;
								for (Peer p : peers) {
									ipsList[it++] = p;
								}
								final JComboBox<Peer> comboBox = new JComboBox<>(
										ipsList);
								final JFrame frame = new JFrame(
										"Choose user to submit!");
								frame.setVisible(true);
								frame.setSize(600, 100);
								frame.setLocationRelativeTo(null);
								JPanel pan = new JPanel();
								frame.add(pan);
								pan.add(comboBox);
								JButton okBtn = new JButton("OK");
								pan.add(okBtn);
								okBtn.addActionListener(new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent e) {
										Peer p = (Peer) comboBox
												.getSelectedItem();
										submitFile(fileName, p);
										frame.setVisible(false);
									}
								});
							}
						} else {
							Socket socket;
							try {
								socket = new Socket(nowViewPeer.ip, Main.PORT);
								OutputStream os = socket.getOutputStream();
								os.write(Utils.writeByte(TCPServer.GET));
								os.write(Utils.writeString(fileName));
								InputStream is = socket.getInputStream();
								int x = is.read();
								if (x == TCPServer.RET_GET) {
									long size = Utils.readLong(is);
									byte[] md5 = Utils.readMD5(is);
									BufferedOutputStream o = new BufferedOutputStream(new FileOutputStream(
											new File(dirName + "/" + fileName)));
									while (true) {
										int xx = is.read();
//										System.err.println(xx + ");
										if (xx == -1) {
											break;
										}
										o.write(xx);
									}
									o.close();
								}
								System.err.println("DONE DOWNLOADING.");
								os.close();
								is.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		});

		while (true) {
			updateViews();
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void submitFile(String fileName, Peer p) {
		if (p.ip.equals(nowViewPeer.ip)) {
			return;
		}
		Socket socket;
		File file = new File(dirName + "/" + fileName);
		try {
			socket = new Socket(p.ip, Main.PORT);
			OutputStream os = socket.getOutputStream();
			os.write(Utils.writeByte(TCPServer.PUT));
			os.write(Utils.writeString(fileName));
			os.write(Utils.writeLong(file.length()));
			InputStream is = new FileInputStream(file);
			while (true) {
				int x = is.read();
				if (x == -1) {
					break;
				}
				os.write(x);
			}
			is.close();
			os.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
