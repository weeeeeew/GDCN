package se.chalmers.gdcn.network;

import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerMaker;
import net.tomp2p.p2p.builder.DiscoverBuilder;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

/**
 * Created by HalfLeif on 2014-04-09.
 */
public class DeceitfulNetworkUtils {
    public static Peer createPeer(int port){
        KeyPairGenerator generator = null;
        try {
            generator = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        assert generator != null;
        KeyPair keyPair = generator.generateKeyPair();

        try {
            return new PeerMaker(keyPair).setPorts(port).makeAndListen();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void bootstrap(final Peer peer, String address, final int port, final OnReplyCommand finished){
        try {
            final InetAddress inetAddress = InetAddress.getByName(address);

            DiscoverBuilder discoverBuilder = peer.discover().setInetAddress(inetAddress).setPorts(port);
            discoverBuilder.start().addListener(new BaseFutureAdapter<FutureDiscover>() {
                @Override
                public void operationComplete(FutureDiscover future) throws Exception {
                    if (!future.isSuccess()) {
                        System.out.println("Bootstrap insuccessful");
                        finished.execute(null);
                        return;
                    }

                    finished.execute(future.getReporter());
                }
            });
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
