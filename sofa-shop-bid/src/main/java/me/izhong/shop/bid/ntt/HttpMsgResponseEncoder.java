package me.izhong.shop.bid.ntt;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Slf4j
public class HttpMsgResponseEncoder extends MessageToMessageEncoder<BidMsg> {
    private String charset;
    private int timeout;

    public HttpMsgResponseEncoder(String charset, int timeout) {
        super();
        this.charset = charset;
        this.timeout = timeout;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, BidMsg message,
                          List<Object> out) {
        try {
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                    Unpooled.wrappedBuffer(message.getPayload().getBytes(
                            charset)));
            response.headers().set(HttpHeaders.Names.CONTENT_TYPE,
                    "application/json;charset=" + charset);
            response.headers().set(HttpHeaders.Names.CONTENT_LENGTH,
                    response.content().readableBytes());

            // 强制keep-alive
            response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            response.headers().set("Keep-Alive", "timeout=" + timeout);

            out.add(response);
        } catch (Exception e) {
            log.error("", e);
        }

    }
}
