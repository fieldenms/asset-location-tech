package fielden.teltonika.server;

import static org.apache.logging.log4j.LogManager.getLogger;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

import fielden.teltonika.IAvlTrackerHandler;

public class AvlServer implements Runnable {

    protected final static Logger LOGGER = getLogger(AvlServer.class);

    private final String host;
    private final int port;
    public final ChannelGroup allChannels;
    private ServerBootstrap bootstrap;
    private Channel serverChannel;
    private final ConcurrentHashMap<String, Channel> existingConnections;
    private final IAvlTrackerHandler avlTrackerHandler;

    public AvlServer(final String host, final int port, final IAvlTrackerHandler avlTrackerHandler) {
        this.host = host;
        this.port = port;
        this.avlTrackerHandler = avlTrackerHandler;
        this.allChannels = new DefaultChannelGroup("fielden-avl-server");
        this.existingConnections = new ConcurrentHashMap<>();
    }

    @Override
    public void run() {
        if (bootstrap != null) {
            throw new IllegalArgumentException("Server has already been run.");
        }
        LOGGER.info("\tNetty AVL server starting...");
        // Configure the server.
        bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
        bootstrap.setOption("child.keepAlive", false);

        // Set up the pipeline factory.
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            @Override
            public ChannelPipeline getPipeline() throws Exception {
                LOGGER.info("New pipe is requested.");
                return Channels.pipeline(new TransparentEncoder(), new AvlFrameDecoder(), new AvlServerHandler(existingConnections, allChannels, avlTrackerHandler));
            }
        });

        // Bind and start to accept incoming connections.
        serverChannel = bootstrap.bind(new InetSocketAddress(host, port));
        allChannels.add(serverChannel);
        LOGGER.info("\tNetty AVL server started on " + host + ":" + port);
    }

    public void shutdown() {
        LOGGER.info("Shutdown initiated...");
        existingConnections.clear();
        serverChannel.close().awaitUninterruptibly();
        allChannels.close().awaitUninterruptibly();
        LOGGER.info("Channels closed.");
        if (bootstrap != null) {
            bootstrap.releaseExternalResources();
        }
        LOGGER.info("External resources released.");
    }

    public class TransparentEncoder extends OneToOneEncoder {
        @Override
        protected Object encode(final ChannelHandlerContext ctx, final Channel channel, final Object msg) throws Exception {
            return msg;
        }
    }

}