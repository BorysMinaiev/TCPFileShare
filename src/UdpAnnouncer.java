import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Logger;


public class UdpAnnouncer {
	private static Logger log = Logger.getLogger(UdpAnnouncer.class.getName());
	
	private static byte[] genIP(final String ip) {
		byte[] res = new byte[4];
		int it = 0;
		for (String x : ip.split("\\.")) {
			res[it++] = (byte) Integer.parseInt(x);
		}
		return res;
	}
	
	public static void announce(final String ip, int filesCount, long timestamp, final String name) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.write(genIP(ip));
        dos.writeInt(filesCount);
        dos.writeLong(timestamp);
        dos.write((name + '\000').getBytes());
        dos.flush();
        byte[] bytes = bos.toByteArray();
        DatagramPacket packet = new DatagramPacket(bytes,
                                bytes.length,
                                InetAddress.getByName("255.255.255.255"), Main.PORT);
        DatagramSocket socket = new DatagramSocket(null);
        try {
			socket.send(packet);
		} catch (IOException e) {
			log.severe(e.toString());
		}
        socket.close();
	}
}
