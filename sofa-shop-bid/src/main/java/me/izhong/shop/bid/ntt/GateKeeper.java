package me.izhong.shop.bid.ntt;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import me.izhong.shop.bid.config.ConfigBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
@Slf4j
public class GateKeeper {

    static private EventLoopGroup bossGroup = new NioEventLoopGroup();
    static private EventLoopGroup workerGroup = new NioEventLoopGroup();

    @Autowired
    private ConfigBean configBean;

    @Autowired
    private NttTaskExecutor nttTaskExecutor;

    @Autowired
    private NettyInvokeService nettyInvokeService;

    @PostConstruct
    public void start() throws Exception {

        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelHandler[] handlers = createHandlers();
                        for (ChannelHandler handler : handlers) {
                            ch.pipeline().addLast(handler);
                        }
                    }
                }).option(ChannelOption.SO_BACKLOG, 128)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.SO_REUSEADDR, true);

        int port = configBean.getPort();
        ChannelFuture cf = b.bind(port).await();
        if (!cf.isSuccess()) {
            log.error("无法绑定端口：" + port);
            throw new Exception("无法绑定端口：" + port);
        }

        log.info("服务启动完毕，监听端口[{}]", port);
    }

    protected ChannelHandler[] createHandlers() {
        return new ChannelHandler[]{
                new IdleStateHandler(0, 0, configBean.getHttpIdleTime()),

                new HttpResponseEncoder(),
                new HttpRequestDecoder(),
                new HttpObjectAggregator(1048576),

                new HttpMsgResponseEncoder(configBean.getCharset(), configBean.getHttpIdleTime()),
                new ResponseLogger(),

                new HttpMsgRequestDecoder(configBean.getCharset()),
                new ContextBuilder( new SecurityChecker(configBean)),
                new RequestLogger(),

                new RequestHandler(nettyInvokeService, nttTaskExecutor)
        };
    }

    @PreDestroy
    public void stop() {
        bossGroup.shutdownGracefully().syncUninterruptibly();
        workerGroup.shutdownGracefully().syncUninterruptibly();
        log.info("服务关闭。");
    }
}
