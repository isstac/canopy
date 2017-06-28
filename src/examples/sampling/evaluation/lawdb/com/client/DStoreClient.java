package sampling.evaluation.lawdb.com.client;

import java.nio.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class DStoreClient
{
    public static final int PORT = 7689;
    static HashMap<Integer, Long> out_Times;
    public static HashMap<Integer, Long> finish_times;
    public static HashMap<Integer, Boolean> node_split;
    public static HashMap<Integer, Boolean> beenUsed;
    private DatagramSocket clientSocket;
    private InetAddress IPAddress;
    
    public void connect(final String ipAddress, final int port) throws SocketException, UnknownHostException {
        final String serverHostname = new String(ipAddress);
        this.clientSocket = new DatagramSocket();
        this.IPAddress = InetAddress.getByName(serverHostname);
    }
    
    public byte insertnewkey(final int key) throws IOException {
        final byte[] receiveData = new byte[5];
        final ByteBuffer b = ByteBuffer.allocate(5);
        b.put((byte)1);
        b.putInt(key);
        final byte[] recv = this.send(b, receiveData);
        final ByteBuffer contents = ByteBuffer.wrap(recv, 0, recv.length);
        final byte status = contents.get();
        return status;
    }
    
    public byte insertnewkeyOverflow(final int key) throws IOException {
      final byte[] receiveData = new byte[5];
      final ByteBuffer b = ByteBuffer.allocate(9);
      b.put((byte)1);
      b.putInt(key);
      b.putInt(key);
      final byte[] recv = this.send(b, receiveData);
      final ByteBuffer contents = ByteBuffer.wrap(recv, 0, recv.length);
      final byte status = contents.get();
      return status;
  }
    
    public byte delete(final int key) throws IOException {
        final byte[] receiveData = new byte[5];
        final ByteBuffer b = ByteBuffer.allocate(5);
        b.put((byte)12);
        b.putInt(key);
        final byte[] recv = this.send(b, receiveData);
        final ByteBuffer contents = ByteBuffer.wrap(recv, 0, recv.length);
        final byte status = contents.get();
        return status;
    }
    
    public void update(final int key, final String rname) throws IOException {
        final byte[] receiveData = { 0 };
        final ByteBuffer b = ByteBuffer.allocate(9 + rname.length() * 2);
        b.put((byte)9);
        b.putInt(key);
        b.putInt(rname.length());
        for (int i = 0; i < rname.length(); ++i) {
            b.putChar(rname.charAt(i));
        }
        final byte[] recv = this.send(b, receiveData);
    }
    
    private byte[] send(final ByteBuffer b, final byte[] receiveData) throws IOException {
        final byte[] array = b.array();
        final DatagramPacket sendPacket = new DatagramPacket(array, array.length, this.IPAddress, 7689);
        this.clientSocket.send(sendPacket);
        final DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        this.clientSocket.receive(receivePacket);
        final byte[] recv = receivePacket.getData();
        return recv;
    }
    
    private void sendget(final ByteBuffer b, final byte[] buf, final ArrayList<Integer> vals) throws IOException {
        final byte[] array = b.array();
        final DatagramPacket sendPacket = new DatagramPacket(array, array.length, this.IPAddress, 7689);
        this.clientSocket.send(sendPacket);
        boolean keepGoing = true;
        while (keepGoing) {
            final DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);
            this.clientSocket.receive(receivePacket);
            final byte[] data = receivePacket.getData();
            if (data == null) {
                break;
            }
            if (data.length == 0) {
                break;
            }
            final ByteBuffer contents = ByteBuffer.wrap(data, 0, data.length);
            final int v = contents.getInt();
            if (v == -8) {
                keepGoing = false;
            }
            else {
                vals.add(v);
            }
        }
    }
    
    private String sendget(final ByteBuffer b, final byte[] buf) throws IOException {
        final byte[] array = b.array();
        final DatagramPacket sendPacket = new DatagramPacket(array, array.length, this.IPAddress, 7689);
        this.clientSocket.send(sendPacket);
        final DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);
        this.clientSocket.receive(receivePacket);
        final int length = receivePacket.getLength();
        final byte[] data = receivePacket.getData();
        final String datastr = new String(data, 0, length, "UTF-8");
        return datastr;
    }
    
    public void beginTransaction() throws IOException {
        final byte[] receiveData = { 0 };
        final ByteBuffer b = ByteBuffer.allocate(1);
        b.put((byte)3);
        final byte[] recv = this.send(b, receiveData);
        final ByteBuffer contents = ByteBuffer.wrap(recv, 0, recv.length);
        final byte status = contents.get();
    }
    
    public boolean add(final int k, final int d, final boolean b) throws IOException {
        final byte ret = this.insertnewkey(k);
        return true;
    }
    
    public void commit() throws IOException {
        final byte[] receiveData = { 0 };
        final ByteBuffer b = ByteBuffer.allocate(1);
        b.put((byte)4);
        final byte[] recv = this.send(b, receiveData);
        final ByteBuffer contents = ByteBuffer.wrap(recv, 0, recv.length);
        final byte status = contents.get();
    }
    
    public void rollback() throws IOException {
        final byte[] receiveData = { 0 };
        final ByteBuffer b = ByteBuffer.allocate(1);
        b.put((byte)5);
        final byte[] recv = this.send(b, receiveData);
        final ByteBuffer contents = ByteBuffer.wrap(recv, 0, recv.length);
        final byte status = contents.get();
    }
    
    public void fastInsert(final boolean onoff) throws IOException {
        final byte[] receiveData = { 0 };
        final ByteBuffer b = ByteBuffer.allocate(2);
        b.put((byte)2);
        b.put((byte)(byte)(onoff ? 1 : 0));
        final byte[] recv = this.send(b, receiveData);
        final ByteBuffer contents = ByteBuffer.wrap(recv, 0, recv.length);
        final byte status = contents.get();
    }
    
    public ArrayList<Integer> search(final int min, final int max) throws IOException {
        final byte[] receiveData = new byte[4];
        final ByteBuffer b = ByteBuffer.allocate(9);
        b.put((byte)8);
        b.putInt(min);
        b.putInt(max);
        final ArrayList<Integer> vals = (ArrayList<Integer>)new ArrayList();
        this.sendget(b, receiveData, vals);
        return vals;
    }
    
    public void storefile(final String rname, final String fcontents) throws IOException {
        final byte[] receiveData = { 0 };
        final ByteBuffer b = ByteBuffer.allocate(5 + rname.length() * 2 + 4 + fcontents.length() * 2);
        b.put((byte)11);
        b.putInt(rname.length());
        for (int i = 0; i < rname.length(); ++i) {
            b.putChar(rname.charAt(i));
        }
        b.putInt(fcontents.length());
        for (int i = 0; i < fcontents.length(); ++i) {
            b.putChar(fcontents.charAt(i));
        }
        final byte[] recv = this.send(b, receiveData);
        final ByteBuffer contents = ByteBuffer.wrap(recv, 0, recv.length);
        final byte status = contents.get();
    }
    
    public String getfile(final String rname) throws IOException {
        final byte[] receiveData = new byte[1024];
        final ByteBuffer b = ByteBuffer.allocate(5 + rname.length() * 2);
        b.put((byte)13);
        b.putInt(rname.length());
        for (int i = 0; i < rname.length(); ++i) {
            b.putChar(rname.charAt(i));
        }
        final String str = this.sendget(b, receiveData);
        return str;
    }
    
    public String getval(final int key) throws IOException {
        final byte[] receiveData = new byte[1024];
        final ByteBuffer b = ByteBuffer.allocate(5);
        b.put((byte)10);
        b.putInt(key);
        final String str = this.sendget(b, receiveData);
        return str;
    }
    
    static {
        DStoreClient.out_Times = (HashMap<Integer, Long>)new HashMap();
        DStoreClient.finish_times = (HashMap<Integer, Long>)new HashMap();
        DStoreClient.node_split = (HashMap<Integer, Boolean>)new HashMap();
        DStoreClient.beenUsed = (HashMap<Integer, Boolean>)new HashMap();
    }
}
