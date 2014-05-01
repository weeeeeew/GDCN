package network;

import javax.crypto.*;
import java.io.IOException;
import java.io.Serializable;
import java.security.*;

/**
 * Created by Leif on 2014-03-24.
 *
 * General purpose class used for any message passing.
 */
public class NetworkMessage implements Serializable {

    private final Serializable object;
    private final Type type;

    public NetworkMessage(Serializable object, Type type) {
        this.object = object;
        this.type = type;
    }

    public Object getObject() {
        return object;
    }

    public Type getType() {
        return type;
    }

    public SealedObject encrypt(SecretKey key) throws InvalidKeyException {
        try {
            return Crypto.encrypt(this, key);
        } catch (IOException|IllegalBlockSizeException e) {
            e.printStackTrace();
            System.out.println("Encryption failed! Message: "+this);
            return null;
        }
    }

    public static NetworkMessage decrypt(SealedObject sealedData, SecretKey key) throws InvalidKeyException {
        Serializable data;

        try {
            data = Crypto.decrypt(sealedData, key);
        } catch (BadPaddingException|IOException|IllegalBlockSizeException e) {
            e.printStackTrace();
            System.out.println("Decryption failed!");
            return null;
        }

        if (data instanceof NetworkMessage) {
            return (NetworkMessage) data;
        } else {
            throw new InvalidParameterException("The encrypted object was not a NetworkMessage");
        }
    }

    @Override
    public String toString() {
        return "NetwM{ " + type +
                ", " + object +
                '}';
    }

    public static enum Type {
        REQUEST,
        NO_REPLY
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof NetworkMessage ? this.object.equals( ((NetworkMessage) other).object ) && this.type == ((NetworkMessage) other).type : false;
    }

}
