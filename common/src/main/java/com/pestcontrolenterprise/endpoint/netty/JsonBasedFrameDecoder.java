package com.pestcontrolenterprise.endpoint.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

/**
 * @author myzone
 * @date 4/26/14
 */
public class JsonBasedFrameDecoder extends MessageToMessageDecoder<CharSequence> {

    private StringBuilder stringBuilder = new StringBuilder();
    private int level = 0;

    @Override
    protected void decode(ChannelHandlerContext ctx, CharSequence msg, List<Object> out) throws Exception {
        for (int i = 0; i < msg.length(); i++) {
            char c = msg.charAt(i);

            stringBuilder.append(c);

            switch (c) {
                case '{':
                    level++;
                    break;
                case '}':
                    level--;
                    break;
            }

            if (level == 0) {
                out.add(stringBuilder.toString());

                stringBuilder = new StringBuilder();
            }
        }
    }
}
