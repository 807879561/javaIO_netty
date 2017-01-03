package netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.nio.charset.Charset;

/**
 *引导服务器配置
 *
 */
public class BootstrapServer {
	public static void main(String[] args) throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		ServerBootstrap b = new ServerBootstrap();
		b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
				.childHandler(new SimpleChannelInboundHandler<ByteBuf>() {
					@Override
					protected void channelRead0(ChannelHandlerContext paramChannelHandlerContext, ByteBuf msg)
							throws Exception {
						System.out.println(msg.toString(Charset.forName("UTF-8")));
						msg.clear();
						ByteBuf buf = Unpooled.copiedBuffer("222sfjsj", Charset.forName("UTF-8"));
						paramChannelHandlerContext.writeAndFlush(buf);
					}

				});
		ChannelFuture f = b.bind(2048);
		f.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {
					System.out.println("Server bound");
				} else {
					System.out.println("bound failed");
					future.cause().printStackTrace();
				}

			}
		});
	}
}
