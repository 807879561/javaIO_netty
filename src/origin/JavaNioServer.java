package origin;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * java NIO
 *
 */
public class JavaNioServer {
	public void server(int port) throws Exception {
		System.out.println("listening for connections on port" + port);
		Selector selector = Selector.open();
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		ServerSocket serverSocket = serverChannel.socket();
		serverSocket.bind(new InetSocketAddress(port));
		serverChannel.configureBlocking(false);
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		//final ByteBuffer msg = ByteBuffer.wrap("HI!\r\n".getBytes());//将 byte 数组包装到缓冲区中。 
		while (true) {
			int n = selector.select();//择一组键，其相应的通道已为 I/O 操作准备就绪。 
			if (n > 0) {
				Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
				while (iter.hasNext()) {
					SelectionKey key = iter.next();
					iter.remove();
					try {
						if (key.isAcceptable()) {
							ServerSocketChannel server = (ServerSocketChannel) key.channel();
							SocketChannel client = server.accept();
							System.out.println("Accepted connection from" + client);
							client.configureBlocking(false);
							client.register(selector, SelectionKey.OP_READ);//duplicate创建共享此缓冲区内容的新的字节缓冲区
						} else if (key.isReadable()) {
							SocketChannel channel = (SocketChannel) key.channel();
							channel.configureBlocking(false);
							String receive = receive(channel);
							BufferedReader b = new BufferedReader(new StringReader(receive));

							String s = b.readLine();
							while (s != null) {
								System.out.println(s);
								s = b.readLine();
							}
							b.close();
							channel.register(selector, SelectionKey.OP_WRITE, ByteBuffer.wrap(receive.getBytes()));
						} else if (key.isWritable()) {
							SocketChannel client = (SocketChannel) key.channel();
							ByteBuffer buff = (ByteBuffer) key.attachment();//获取当前的附加对象
							while (buff.hasRemaining()) {//当且仅当此缓冲区中至少还有一个元素时返回 true
								if (client.write(buff) == 0)//将字节序列从给定的缓冲区写入此通道。 写入的字节数，可能为零
									break;
							}
							client.close();
						}
					} catch (Exception e) {
						key.cancel();
						key.channel().close();
					}
				}
			}
		}
	}

	private String receive(SocketChannel socketChannel) throws Exception {
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		byte[] bytes = null;
		int size = 0;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while ((size = socketChannel.read(buffer)) > 0) {
			buffer.flip();
			bytes = new byte[size];
			buffer.get(bytes);
			baos.write(bytes);
			buffer.clear();
		}
		bytes = baos.toByteArray();

		return new String(bytes);
	}

	public static void main(String[] args) {
		JavaNioServer server = new JavaNioServer();
		try {
			server.server(7000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
