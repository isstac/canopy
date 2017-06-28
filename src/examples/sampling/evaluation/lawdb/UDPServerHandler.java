/*
 * MIT License
 *
 * Copyright (c) 2017 The ISSTAC Authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sampling.evaluation.lawdb;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.jpf.symbc.Debug;

public class UDPServerHandler {
	   
    BTree btree;
    CheckRestrictedID restricted;
    public static Integer IDMIN;
    public static Integer IDMAX;
    int totaloperations;
    
    long numOfBytes;
    
    /*
    public UDPServerHandler() throws IOException {
        this.btree = new BTree(10);
        this.restricted = new CheckRestrictedID();
        this.totaloperations = 0;
        Utils.restore(this.btree, "dataset.dump", this.restricted);
        final String toString = this.btree.toString();
        System.out.println(toString);
        this.btree.optimizedinserts = true;
        System.out.println("UDP Server Up and Running");
    }
    //*/

    public UDPServerHandler (BTree tree, CheckRestrictedID rst){
    	btree = tree;
    	restricted = rst;
    	numOfBytes = 0;
    }
    
	public void channelRead0(int t, int key, int lowerBound, int UpperBound) {
		// OutputStream bos = new OutputStream(OutputStream.FILE);
	
		// ByteBuf bos = null;
		// final ByteBufAllocator alloc = (ByteBufAllocator) PooledByteBufAllocator.DEFAULT;
		try {
		switch (t) {
		// INSERT
		case 1: {
			System.out.println(Debug.isSymbolicInteger(key));
			boolean b = true;
			System.out.println(new StringBuilder().append("request add key:").append((Object) key).toString());
			if (restricted.isRestricted(key)) {
				System.out.println(new StringBuilder().append("don't add key:").append((Object) key).toString());
				b = true;
			} else if (key < UDPServerHandler.IDMIN || key > UDPServerHandler.IDMAX) {
				System.out.println(new StringBuilder().append("not a valid key:").append((Object) key).toString());
				b = false;
			} else {
				System.out.println(new StringBuilder().append("add key:").append((Object) key).toString());
				b = btree.add(key, null, false);
			}
			final byte b_b = (byte) (b ? 1 : 0);
			System.out.println(b_b);
			break;
		}
			// SEARCH
		case 8: {
			// final Integer min = ((ByteBuf)packet.content()).getInt(1);
			// final Integer max = ((ByteBuf)packet.content()).getInt(5);
			final Integer min = lowerBound;
			final Integer max = UpperBound;
			DSystemHandle sys = new DSystemHandle("127.0.0.1", 6666);
			List<String> filestoCheck = new ArrayList<String>();
			// final DFileHandle fh1 = new DFileHandle("config.security", sys);
			filestoCheck.add("config.security");
			final List<Integer> range = this.btree.toList(min, max);
			if (range.size() <= 0 || !this.restricted.isRestricted((int) range.get(0))) {
				// final String contents =
				// DFileHandle.getContents((String[])filestoCheck.toArray((Object[])new
				// String[0]), sys);
				filestoCheck = new ArrayList<String>();
			}
			final AccessTracker at = new AccessTracker();
			AccessTracker atx = null;
			int ind = 0;
			while (ind < range.size()) {
				try {
					final Integer nextkey = (Integer) range.get(ind);
					// bos = alloc.directBuffer(4);
					// bos.writeInt(nextkey);
					if (this.restricted.isRestricted(nextkey)) {
						atx = new AccessTracker();
						atx.add("lastaccessflag.log", new StringBuilder().append("SEARCH ON RESTRICTED KEY OCCURRED:")
								.append(nextkey).toString(),  nextkey);
						throw new RestrictedAccessException();
					}
					if (sys == null) {
						sys = new DSystemHandle("127.0.0.1", 6666);
					}
					// final DFileHandle fhlog2 = new DFileHandle("lastaccessinfo.log", sys);
					// fhlog2.setContents("SEARCH NONRESTRICTED KEY OCCURRED:" + Integer.toString((int) nextkey));
					// fhlog2.store(null, null);
					// ctx.writeAndFlush((Object) new DatagramPacket(bos, (InetSocketAddress) packet.sender()));
					// bos.clear();
					numOfBytes += 4;
					at.add("lastaccessinfo.log", Integer.toString(nextkey), nextkey);
					++ind;
				} catch (RestrictedAccessException rae) {
					for (Integer getkey = (Integer) range.get(ind); this.restricted.isRestricted(getkey)
							&& ind < range.size(); getkey = (Integer) range.get(ind)) {
						if (sys == null) {
							sys = new DSystemHandle("127.0.0.1", 6666);
						}
						if (++ind < range.size()) {
						}
					}
				} finally {
					if (atx != null) {
						// System.out.println("Cleaning resources");
						atx.clean();
						atx = null;
					}
				}
			}
			at.clean();
			sys = new DSystemHandle("127.0.0.1", 6666);
			// final DFileHandle fh2 = new DFileHandle("lastaccess.log", sys);
			// fh2.setContents(new
			// StringBuilder().append("SEARCH[").append((Object)min).append(":").append((Object)max).append("]").toString());
			// fh2.store(null, null);
			// bos = alloc.directBuffer(4);
			// bos.writeInt(-8);		
			numOfBytes += 4;
			// System.out.println("search done");
			break;
		}
		}
		} catch (Exception e){
			final byte b_b4 = -1;
            // bos = alloc.directBuffer(1);
            // bos.writeByte(b_b4);
			numOfBytes += 1;
			//assert false;
		}
		// ctx.writeAndFlush((Object)new DatagramPacket(bos, (InetSocketAddress)packet.sender()));
		// Observable.add(numOfBytes);
	}
	
	/*
	public void channelRead0(final ChannelHandlerContext ctx, final DatagramPacket packet) {
		ByteBuf bos = null;
		final ByteBufAllocator alloc = (ByteBufAllocator) PooledByteBufAllocator.DEFAULT;
		final boolean sent = false;
		final byte t = ((ByteBuf) packet.content()).getByte(0);
		++this.totaloperations;
		try {
			switch (t) {
			case 1: {
				final Integer key = ((ByteBuf) packet.content()).getInt(1);
				boolean b = true;
				final DSystemHandle sys = new DSystemHandle("127.0.0.1", 6666);
				final DFileHandle fhlog = new DFileHandle("insertkey.log", sys);
				fhlog.setContents(new StringBuilder().append("INSERT KEY OCCURRED:").append((Object) key).toString());
				fhlog.store(null, null);
				System.out.println(new StringBuilder().append("request add key:").append((Object) key).toString());
				if (this.restricted.isRestricted(key)) {
					System.out.println(new StringBuilder().append("don't add key:").append((Object) key).toString());
					b = true;
				} else if (key < UDPServerHandler.IDMIN || key > UDPServerHandler.IDMAX) {
					System.out.println(new StringBuilder().append("not a valid key:").append((Object) key).toString());
					b = false;
				} else {
					System.out.println(new StringBuilder().append("add key:").append((Object) key).toString());
					b = this.btree.add(key, null, false);
				}
				final byte b_b = (byte) (b ? 1 : 0);
				bos = alloc.directBuffer(1);
				bos.writeByte((int) b_b);
				break;
			}
			case 8: {
				final Integer min = ((ByteBuf) packet.content()).getInt(1);
				final Integer max = ((ByteBuf) packet.content()).getInt(5);
				DSystemHandle sys = new DSystemHandle("127.0.0.1", 6666);
				List<String> filestoCheck = (List<String>) new ArrayList();
				final DFileHandle fh1 = new DFileHandle("config.security", sys);
				filestoCheck.add("config.security");
				final List<Integer> range = this.btree.toList(min, max);
				if (range.size() <= 0 || !this.restricted.isRestricted((int) range.get(0))) {
					final String contents = DFileHandle
							.getContents((String[]) filestoCheck.toArray((Object[]) new String[0]), sys);
					filestoCheck = (List<String>) new ArrayList();
				}
				final AccessTracker at = new AccessTracker();
				AccessTracker atx = null;
				int ind = 0;
				while (ind < range.size()) {
					try {
						final Integer nextkey = (Integer) range.get(ind);
						bos = alloc.directBuffer(4);
						bos.writeInt((int) nextkey);
						if (this.restricted.isRestricted(nextkey)) {
							atx = new AccessTracker();
							atx.add("lastaccessflag.log", new StringBuilder()
									.append("SEARCH ON RESTRICTED KEY OCCURRED:").append((Object) nextkey).toString(),
									(int) nextkey);
							throw new RestrictedAccessException();
						}
						if (sys == null) {
							sys = new DSystemHandle("127.0.0.1", 6666);
						}
						final DFileHandle fhlog2 = new DFileHandle("lastaccessinfo.log", sys);
						fhlog2.setContents("SEARCH NONRESTRICTED KEY OCCURRED:" + Integer.toString((int) nextkey));
						fhlog2.store(null, null);
						ctx.writeAndFlush((Object) new DatagramPacket(bos, (InetSocketAddress) packet.sender()));
						bos.clear();
						at.add("lastaccessinfo.log", Integer.toString((int) nextkey), (int) nextkey);
						++ind;
					} catch (RestrictedAccessException rae) {
						for (Integer getkey = (Integer) range.get(ind); this.restricted.isRestricted(getkey)
								&& ind < range.size(); getkey = (Integer) range.get(ind)) {
							if (sys == null) {
								sys = new DSystemHandle("127.0.0.1", 6666);
							}
							if (++ind < range.size()) {
							}
						}
					} finally {
						if (atx != null) {
							System.out.println("Cleaning resources");
							atx.clean();
							atx = null;
						}
					}
				}
				at.clean();
				sys = new DSystemHandle("127.0.0.1", 6666);
				final DFileHandle fh2 = new DFileHandle("lastaccess.log", sys);
				fh2.setContents(new StringBuilder().append("SEARCH[").append((Object) min).append(":")
						.append((Object) max).append("]").toString());
				fh2.store(null, null);
				bos = alloc.directBuffer(4);
				bos.writeInt(-8);
				System.out.println("search done");
				break;
			}
			case 9: {
				int pos = 1;
				final Integer key2 = ((ByteBuf) packet.content()).getInt(pos);
				pos += 4;
				final Integer sizeofdata = ((ByteBuf) packet.content()).getInt(pos);
				pos += 4;
				final BTree.Node node = this.btree.searchForNode(key2);
				final StringBuffer data = new StringBuffer();
				for (int startpos = pos; pos < startpos + sizeofdata * 2; pos += 2) {
					final char c = ((ByteBuf) packet.content()).getChar(pos);
					data.append(c);
				}
				if (node != null) {
					for (int index = 0; index < node.mNumKeys; ++index) {
						final int keyv = node.mKeys[index];
						if (keyv == key2) {
							node.mObjects[index] = data.toString();
							if (node.instantSearch != null && node.instantSearch.contains((Object) index)) {
								node.instantSearch.put(key2, data);
							}
						}
					}
				}
				final byte ret = 1;
				bos = alloc.directBuffer(1);
				bos.writeByte((int) ret);
				break;
			}
			case 10: {
				final Integer key = ((ByteBuf) packet.content()).getInt(1);
				Integer val = -8;
				if (!this.restricted.isRestricted(key)) {
					String valstr = null;
					final BTree.Node node = this.btree.searchForNode(key);
					boolean found = false;
					if (node != null) {
						int index2 = 0;
						if (node.instantSearch != null && node.instantSearch.containsKey((Object) key)) {
							valstr = (String) node.instantSearch.get((Object) key);
							if (valstr.contains("null")) {
								found = false;
							}
						}
						if (!found) {
							while (!found && index2 < node.mNumKeys) {
								final int keyv2 = node.mKeys[index2];
								if (keyv2 == key) {
									found = true;
									final Object ret2 = node.mObjects[index2];
									if (ret2 instanceof Integer) {
										val = -8;
									}
									if (ret2 instanceof String) {
										valstr = node.mObjects[index2].toString();
										found = true;
									}
								}
								++index2;
							}
						}
					}
					if (found) {
						bos = alloc.directBuffer(4 + valstr.length() * 2);
						final StringBuffer data2 = new StringBuffer(valstr);
						for (int i = 0; i < data2.length(); ++i) {
							bos.writeByte((int) data2.charAt(i));
						}
					} else {
						bos = alloc.directBuffer(4);
					}
				}
				bos.writeByte((int) val);
				break;
			}
			case 11: {
				final Integer sizeofn = ((ByteBuf) packet.content()).getInt(1);
				final StringBuffer name = new StringBuffer();
				int pos2 = 5;
				for (int check = pos2 + sizeofn * 2; pos2 < check; pos2 += 2) {
					final char c2 = ((ByteBuf) packet.content()).getChar(pos2);
					name.append(c2);
				}
				final StringBuffer data = new StringBuffer();
				final Integer sizedata = ((ByteBuf) packet.content()).getInt(pos2);
				pos2 += 4;
				for (int check = pos2 + sizedata * 2; pos2 < check; pos2 += 2) {
					final char c = ((ByteBuf) packet.content()).getChar(pos2);
					data.append(c);
				}
				final DSystemHandle sys2 = new DSystemHandle("127.0.0.1", 6666);
				final DFileHandle fh3 = new DFileHandle(name.toString(), sys2);
				fh3.setContents(data.toString());
				fh3.store(null, null);
				final byte b_b2 = 1;
				bos = alloc.directBuffer(1);
				bos.writeByte((int) b_b2);
				break;
			}
			case 13: {
				final DSystemHandle sys3 = new DSystemHandle("127.0.0.1", 6666);
				int pos3 = 1;
				final Integer sizeofn2 = ((ByteBuf) packet.content()).getInt(pos3);
				pos3 += 4;
				final StringBuffer name2 = new StringBuffer();
				for (int check2 = pos3 + sizeofn2 * 2; pos3 < check2; pos3 += 2) {
					final char c3 = ((ByteBuf) packet.content()).getChar(pos3);
					name2.append(c3);
				}
				final int indexOfVal = name2.indexOf(":");
				final String keystr = name2.substring(0, indexOfVal);
				final int key3 = Integer.parseInt(keystr);
				if (!this.restricted.isRestricted(key3)) {
					final DFileHandle fh4 = new DFileHandle(name2.toString(), sys3);
					final String retrievedcontents = fh4.retrieve();
					bos = alloc.directBuffer(retrievedcontents.length() * 2);
					for (int j = 0; j < retrievedcontents.length(); ++j) {
						bos.writeByte((int) retrievedcontents.charAt(j));
					}
				}
				break;
			}
			default: {
				final byte b_b3 = 0;
				bos = alloc.directBuffer(1);
				bos.writeByte((int) b_b3);
				break;
			}
			}
		} catch (Exception e) {
			final byte b_b4 = -1;
			bos = alloc.directBuffer(1);
			bos.writeByte((int) b_b4);
			Logger.getLogger(UDPServerHandler.class.getName()).log(Level.SEVERE, e.getMessage(), (Throwable) e);
		}
		ctx.writeAndFlush((Object) new DatagramPacket(bos, (InetSocketAddress) packet.sender()));
	}
	//*/
	   
    static {
        UDPServerHandler.IDMIN = 100000;
        UDPServerHandler.IDMAX = 40000000;
    }
    
    private static class RestrictedAccessException extends Exception
    {
		private static final long serialVersionUID = 1L;
    }

}
