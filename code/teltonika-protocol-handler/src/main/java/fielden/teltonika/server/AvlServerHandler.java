package fielden.teltonika.server;

import static java.lang.String.format;
import static org.apache.logging.log4j.LogManager.getLogger;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.group.ChannelGroup;

import fielden.teltonika.AvlData;
import fielden.teltonika.IAvlTrackerHandler;

public class AvlServerHandler extends SimpleChannelUpstreamHandler {
    protected final static Logger LOGGER = getLogger(AvlServerHandler.class);

    private static final byte LOGIN_DENY = 0x0;
    private static final byte LOGIN_ALLOW = 0x1;

    private final ChannelGroup allChannels;
    private final ConcurrentHashMap<String, Channel> existingConnections;

    private String imei;
    private final ChannelBuffer ack = ChannelBuffers.buffer(4);
    private final IAvlTrackerHandler avlTrackerHandler;

    public AvlServerHandler(final ConcurrentHashMap<String, Channel> existingConnections, final ChannelGroup allChannels, final IAvlTrackerHandler avlTrackerHandler) {
        this.existingConnections = existingConnections;
        this.allChannels = allChannels;
        this.avlTrackerHandler = avlTrackerHandler;
    }

    @Override
    public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
        final Object msg = e.getMessage();
        if (msg instanceof String) {
            setImei((String) msg);
            final Channel prevChannel = existingConnections.get(getImei());
            if (prevChannel != null && prevChannel != ctx.getChannel()) { // need to close previous channel
                LOGGER.debug(format("Attempting to close previous connection for IMEI[%s]", getImei()));
                try {
                    allChannels.remove(prevChannel);
                    prevChannel.close().awaitUninterruptibly();

                } catch (final Exception ex) {
                    LOGGER.warn(format("Life sucks and previous connection for IMEI %s could not be closed.", getImei()));
                }
            }
            existingConnections.put(getImei(), ctx.getChannel());
            // IMEI
            handleLogin(ctx, getImei()); // process the initial handshake that result is successful or unsuccessful IMEI recognition
        } else if (msg instanceof AvlData[]) { // AVL data array
            handleData(ctx, getImei(), (AvlData[]) msg);
        } else {
            super.messageReceived(ctx, e);
        }
    }

    @Override
    public void channelConnected(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
        super.channelConnected(ctx, e);
        allChannels.add(ctx.getChannel());
        LOGGER.debug("Client channel connected.");
    }

    @Override
    public void channelDisconnected(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
        LOGGER.debug("Originating channel has disconnected.");
        allChannels.remove(ctx.getChannel());
        super.channelDisconnected(ctx, e);
    }

    @Override
    public void channelClosed(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
        handleLogoff(ctx, 0);

        super.channelClosed(ctx, e);
        LOGGER.debug("Client channel closed.");
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final ExceptionEvent e) {
        LOGGER.error("-- Exception!\n");
        LOGGER.error(e.getCause() + "\n");
        final StackTraceElement[] elem = e.getCause().getStackTrace();
        for (final StackTraceElement stackTraceElement : elem) {
            LOGGER.error("\t" + stackTraceElement.toString() + "\n");
        }
        LOGGER.debug("Closing client channel...");
        final Channel channel = e.getChannel();
        channel.close();
    }

    private void handleLogin(final ChannelHandlerContext ctx, final String imei) {
        LOGGER.info("Logging in client [" + imei + "].");
        final Channel channel = ctx.getChannel();
        final ChannelBuffer msg = ChannelBuffers.buffer(1);
        try {
            if (avlTrackerHandler.authorise(imei)) {
                LOGGER.info("Authorised IMEI [" + imei + "].");
                msg.writeByte(LOGIN_ALLOW);
                setImei(imei);
            } else {
                LOGGER.warn("Unrecognised IMEI [" + imei + "].");
                msg.writeByte(LOGIN_DENY);
                //channel.close(); // FIXME relies on multiplexer to close the channel
            }
        } finally {
            channel.write(msg).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(final ChannelFuture future) {
                    if (future.isDone()) {
                        if (future.getCause() != null) {
                            LOGGER.warn("Login response failed [" + imei + "].", future.getCause());
                        } else if (future.isCancelled()) {
                            LOGGER.warn("Login response canceled [" + imei + "].");
                        } else {
                            LOGGER.info("Login response succeeded [" + imei + "].");
                        }
                    }
                }
            });
        }
    }

    private void handleLogoff(final ChannelHandlerContext ctx, final Integer reason) {
        LOGGER.info("Logging off client [" + getImei() + "].");
    }

    private void handleData(final ChannelHandlerContext ctx, final String imei, final AvlData[] data) {
        final Channel channel = ctx.getChannel();
        final int count = data.length;
        LOGGER.info("Received AVL data from IMEI [" + imei + "]. AVL data count = [" + count + "].");

        avlTrackerHandler.handleData(imei, data);

        ack.resetWriterIndex();
        ack.writeInt(count);
        channel.write(ack).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(final ChannelFuture future) {
                if (future.isDone()) {
                    if (future.getCause() != null) {
                        LOGGER.warn("Acknowledgement failed [" + imei + "]. AVL data count = [" + count + "].", future.getCause());
                    } else if (future.isCancelled()) {
                        LOGGER.warn("Acknowledgement canceled [" + imei + "]. AVL data count = [" + count + "].");
                    } else {
                        LOGGER.info("Acknowledgement succeeded [" + imei + "]. AVL data count = [" + count + "].");
                    }
                }
            }
        });
    }

    public String getImei() {
        return imei;
    }

    private void setImei(final String deviceId) {
        this.imei = deviceId;
    }
}
