package netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;

public class HeartbeatHandlerInitializer extends ChannelInitializer<Channel> {
	private static final int READ_IDEL_TIME_OUT = 100; // 读超时
	private static final int WRITE_IDEL_TIME_OUT = 100;// 写超时
	private static final int ALL_IDEL_TIME_OUT = 100; // 所有超时

	@Override
	protected void initChannel(Channel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		//		pipeline.addLast(new IdleStateHandler(READ_IDEL_TIME_OUT, WRITE_IDEL_TIME_OUT, ALL_IDEL_TIME_OUT,
		//				TimeUnit.SECONDS)); // 1
		//		pipeline.addLast(new HeartbeatServerHandler()); // 2
		//pipeline.addLast(new FixedLengthFrameDecoder(16));
		pipeline.addLast(new ChannelOutboundHandlerAdapter());
		pipeline.addLast(new SimpleChannelInboundHandler<ByteBuf>() {
			@Override
			protected void channelRead0(ChannelHandlerContext paramChannelHandlerContext, ByteBuf msg) throws Exception {
				//				System.out.println(msg.toString(Charset.forName("UTF-8")));
				//				System.out.println(msg.copy().capacity());
				msg = msg.copy();
				int capacity = msg.capacity();
				System.out.println("capacity:" + capacity);
				//				byte[] array = new byte[msg.capacity()];//接收到的数据
				//				for (int i = 0; i < msg.capacity(); i++) {
				//					array[i] = msg.getByte(i);
				//					//System.out.print((char) array[i]);
				//				}
				//				ByteBuf bb = Unpooled.buffer(msg.capacity());
				//				bb.order(ByteOrder.LITTLE_ENDIAN);
				//				bb.writeBytes(array).array();
				//
				//				int length = 0;
				//				for (int i = 0; i < 4; i++) {//长度
				//					System.out.print(array[i]);
				//					length += array[i];
				//				}
				//				System.out.println(length);
				//				for (int i = 4; i < 8; i++) {//命令
				//					System.out.printf("%x", array[i]);
				//					//System.out.print((char) array[i]);
				//				}
				//				byte[] info = new byte[8];
				//				for (int i = 8; i < 16; i++) {//命令
				//					info[i - 8] = array[i];
				//				}
				byte[] all = new byte[capacity];
				for (int i = 0; i < capacity; i++) {
					all[0] = msg.getByte(i);
				}
				int command = msg.getByte(4);
				System.out.println("command:" + command);
				byte[] info = new byte[capacity - 8];
				for (int i = 8; i < capacity; i++) {
					info[i - 8] = msg.getByte(i);
				}
				if (command == 1) {//心跳
					Ags.AGSHealthCheck heart = Ags.AGSHealthCheck.parseFrom(info);
					System.out.println("appid:" + heart.getAppid());
					Ags.AGSHealthCheck.Builder builder = Ags.AGSHealthCheck.newBuilder();
					builder.setAppid("imfun");
					paramChannelHandlerContext.channel()
							.writeAndFlush(
									Unpooled.copiedBuffer(getReustByte(builder.build().toByteArray(),
											ByteUtils.int2byte(4097))));
					Ags.AGSMessageReq.Builder msgBuilder = Ags.AGSMessageReq.newBuilder();
					msgBuilder.setRid(1000);
					msgBuilder.setUid(2000);
					msgBuilder.setContent("test");
					msgBuilder.setNick("yuer");
					paramChannelHandlerContext.channel().writeAndFlush(
							Unpooled.copiedBuffer(getReustByte(msgBuilder.build().toByteArray(),
									ByteUtils.int2byte(4096))));

				} else if (command == 2) {//支付宝发送道具通知
					Ags.AGSAlipayGiftReq giftReq = Ags.AGSAlipayGiftReq.parseFrom(info);
					System.out.println(giftReq.getNick());
					System.out.println(giftReq.toString());
					Ags.AGSAlipayGiftRes.Builder builder = Ags.AGSAlipayGiftRes.newBuilder();
					builder.setRetcode(0);
					builder.setTransid("1111");
					paramChannelHandlerContext.channel()
							.writeAndFlush(
									Unpooled.copiedBuffer(getReustByte(builder.build().toByteArray(),
											ByteUtils.int2byte(4099))));
					//					byte[] infoByte = builder.build().toByteArray();
					//					ArrayUtils.reverse(infoByte);
					//					int infoLength = infoByte.length;
					//					byte[] lengthByte = ByteUtils.int2byte(infoLength);
					//					ArrayUtils.reverse(lengthByte);
					//					byte[] commandByte = ByteUtils.int2byte(4099);
					//					ArrayUtils.reverse(commandByte);
					//					byte[] result = Unpooled.copiedBuffer(lengthByte, commandByte, infoByte).array();
					//					ByteBuf buf = Unpooled.copiedBuffer(result);
					//					paramChannelHandlerContext.channel().writeAndFlush(buf);
				} else if (command == 0) {//支付宝聊天
					Ags.AGSMessageReq msgReq = Ags.AGSMessageReq.parseFrom(info);
					System.out.println("内容:" + msgReq.getContent());
					System.out.println(msgReq.toString());
				}
				msg.clear();

			}

		});
		//		pipeline.addLast(new LengthFieldBasedFrameDecoder(1342177320, 0, 4, 0, 4));
		//		//2-进行消息解码
		//		pipeline.addLast(new ProtobufDecoder(Ags.AGSAlipayGiftReq.getDefaultInstance()));
		//		//encoder					
		//		pipeline.addLast(new LengthFieldPrepender(4));
		//		pipeline.addLast(new ProtobufEncoder());
	}

	public static byte[] getReustByte(byte[] infoByte, byte[] commandByte) {
		int infoLength = infoByte.length;
		byte[] lengthByte = ByteUtils.int2byte(infoLength);
		return Unpooled.copiedBuffer(lengthByte, commandByte, infoByte).array();
	}
}
