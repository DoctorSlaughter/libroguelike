/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nuclearunicorn.negame.client.clientIo;

import com.nuclearunicorn.libroguelike.events.Event;
import com.nuclearunicorn.libroguelike.events.IEventListener;
import com.nuclearunicorn.libroguelike.events.network.NetworkEvent;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.Executors;

/*
 * Basic class for every client to server connection, e.g. : charserv, mapserv, chatserv, etc.
 * Implements low-level routine for network communication
 * @author bloodrizer
 */
public abstract class NettyClientLayer implements IEventListener {
    
    ClientBootstrap bootstrap;
    String host;
    int port;

    String name = "undefined client layer";

    //the transport channel we use to write into/read from
    ChannelFuture future;

    final static Logger logger = LoggerFactory.getLogger(NettyClientLayer.class);

    protected ArrayList<String> packetFilter = new ArrayList<String>();
    
    public NettyClientLayer(String host, int port, String name) {

        this.name = name;
    
        bootstrap = new ClientBootstrap(
                new NioClientSocketChannelFactory(
                Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool()));
        
        this.host = host;
        this.port = port;

    }
    
    public void connect() throws Exception{

        for (int i = 0; i< 5; i++){

            logger.info("{} > Connection attempt # {} ...", name, i);
            future = bootstrap.connect(new InetSocketAddress(host, port));

            // Wait until the connection attempt succeeds or fails.
            Channel ioChannel = future.awaitUninterruptibly().getChannel();

            if (!future.isSuccess()) {
                bootstrap.releaseExternalResources();
            }else{
                logger.info("Connection successful");
                return;
            }
        }
        throw new Exception("Unable to connect to "+host+":"+port+" after 5 retries", future.getCause());


    }
    
    public void setPipelineFactory(ChannelPipelineFactory factory){
        bootstrap.setPipelineFactory(factory);
    }

    public void sendMsg(String message){
        if (future == null) {
            logger.warn("Channel future is null, trying to re-acquire resource");
            try {
                connect();
            }catch (Exception ex){
                throw new RuntimeException("failed to re-acquire future, reason:", ex);
            }
        }
        Channel ioChannel = future.awaitUninterruptibly().getChannel();
        
        if (ioChannel==null){
            throw new RuntimeException(host+":"+port+"> Unable to send message, channel is not ready");
        }
        //System.out.println(host+":"+port+"> writing raw message - [" + message + "]");

        ioChannel.write(message + "\r\n");
    }

    private boolean whitelisted(String classname) {
        return packetFilter.contains(classname);
    }


    private void sendNetworkEvent(NetworkEvent event){
        if (!whitelisted(event.classname())){
            return;
        }

        String[] tokens = event.serialize();
        StringBuilder sb = new StringBuilder();

        sb.append(event.classname().concat(" "));

        for (int i = 1; i<tokens.length; i++){
            sb.append(tokens[i].concat(" "));
        }

        sendMsg(sb.toString());
    }


    public void e_on_event(Event event) {
        if (event instanceof NetworkEvent){
            sendNetworkEvent((NetworkEvent)event);
        }
    }

    public void destroy(){
        Channel ioChannel = future.awaitUninterruptibly().getChannel();
        ioChannel.close().awaitUninterruptibly();
        bootstrap.releaseExternalResources();
    }
}
