import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Peer implements Comparable<Peer> {
	InetAddress ip;
	String name;
	int filesCount;
	long timestamp;
	long updateTime;
	
	

	@Override
	public String toString() {
		return "[ip=" + ip + ", name=" + name + ", filesCount="
				+ filesCount + ", timestamp=" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timestamp)) + "]";
	}

	public Peer(InetAddress ip, String name, int filesCount, long timestamp) {
		super();
		this.ip = ip;
		this.name = name;
		this.filesCount = filesCount;
		this.timestamp = timestamp;
		this.updateTime = System.currentTimeMillis();
	}

	@Override
	public int compareTo(Peer o) {
		if (name.equals(o.name)) {
			return ip.toString().compareTo(o.ip.toString());
		}
		return name.compareTo(o.name);
	}

}
