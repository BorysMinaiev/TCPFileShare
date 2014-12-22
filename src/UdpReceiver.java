import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.TreeSet;

public class UdpReceiver implements Runnable {
	DatagramSocket socket;
	static TreeSet<Peer> peers;

	UdpReceiver(DatagramSocket socket, TreeSet<Peer> peers) {
		this.socket = socket;
		UdpReceiver.peers = peers;
	}

	@Override
	public void run() {
		while (true) {
			waitForMessages(socket);
		}
	}

	static int genIP(byte[] ip) {
		int result = 0;
		for (int i = 0; i < ip.length; i++) {
			result = result << 8;
			result += ip[i];
		}
		return result;
	}

	private static int readInt(final byte[] arr, int from) {
		int result = 0;
		for (int i = 0; i < 4; i++) {
			int z = arr[i + from] & ((1 << 8) - 1);
			result = (result << 8) | z;
		}
		return result;
	}

	private static long readLong(final byte[] arr, int from) {
		long result = 0;
		for (int i = 0; i < 8; i++) {
			int z = arr[i + from] & ((1 << 8) - 1);
			result = (result << 8) | z;
		}
		return result;
	}

	private static String readStirng(final byte[] arr, int from) {
		String result = "";
		for (int i = 0; i + from < arr.length; i++) {
			int x = arr[i + from];
			if (x == 0) {
				break;
			}
			result += (char) x;
		}
		return result;
	}

	private static void decodeMessage(byte[] message) {
		InetAddress ip = null;
		try {
			ip = InetAddress.getByAddress(Arrays.copyOf(message, 4));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		int filesCount = readInt(message, 4);
		long timestamp = readLong(message, 8);
		Date date = new Date(timestamp);
		String name = readStirng(message, 16);
//		System.err.println(ip + " " + filesCount + " "
//				+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date)
//				+ " " + name);
		Peer p = new Peer(ip, name, filesCount, timestamp);
		synchronized (peers) {
			if (peers.contains(p)) {
				peers.remove(p);
			}
			peers.add(p);
		}
	}

	private static void waitForMessages(DatagramSocket socket) {
		final int bufLength = 1 << 10;
		byte[] buf = new byte[bufLength];
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		try {
			socket.receive(packet);
		} catch (IOException e) {
			System.out.println("fail ");
		}
		//System.err.println("!!!!!");
		decodeMessage(packet.getData());
	}

}
