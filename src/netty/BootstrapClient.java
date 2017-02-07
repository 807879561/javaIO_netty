package netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 引导配置客户端
 * 
 */
public class BootstrapClient {
	public static void main(String[] args) {
		EventLoopGroup group = new NioEventLoopGroup();
		Bootstrap b = new Bootstrap();
		b.group(group).channel(NioSocketChannel.class).handler(new HeartbeatHandlerInitializer());
		ChannelFuture f = b.connect("120.27.148.24", 33002);//31002 120.27.149.166  start /min telnet 120.27.149.166 31002
		f.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {
					System.out.println("connection finished");
				} else {
					System.out.println("connection failed");
					future.cause().printStackTrace();
				}

			}

		});
		try {
			Thread.sleep(10000L);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//		Ags.AGSMessageReq.Builder msgBuilder = Ags.AGSMessageReq.newBuilder();
		//		msgBuilder.setRid(1000);
		//		msgBuilder.setUid(2000);
		//		msgBuilder.setContent("test");
		//		msgBuilder.setNick("yuer");
		//		f.channel().writeAndFlush(
		//				Unpooled.copiedBuffer(HeartbeatHandlerInitializer.getReustByte(msgBuilder.build().toByteArray(),
		//						ByteUtils.int2byte(4096))));
		Ags.AGSThirdGiftNotify.Builder notifyBuilder = Ags.AGSThirdGiftNotify.newBuilder();
		notifyBuilder.setRoomid(1000);
		notifyBuilder.setPropid(2000);
		notifyBuilder.setPropnum(1);
		notifyBuilder.setNickname("天天");
		f.channel().writeAndFlush(
				Unpooled.copiedBuffer(HeartbeatHandlerInitializer.getReustByte(notifyBuilder.build().toByteArray(),
						ByteUtils.int2byte(4100))));
		System.out.println("1111111111111111111");
	}
}
