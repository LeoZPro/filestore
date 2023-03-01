package fr.miage.filestore.store.hash;

import org.apache.commons.codec.binary.Hex;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA256FilterInputStream extends HashedFilterInputStream {

    private MessageDigest digest;

    protected SHA256FilterInputStream(InputStream in) throws NoSuchAlgorithmException {
        super(in);
        digest = MessageDigest.getInstance("SHA-256");
    }

    @Override
    public int read() throws IOException {
        int c = in.read();
        if (c == -1) {
            return -1;
        }
        digest.update((byte)(c & 0xff));
        return c;
    }

    @Override
    public int read(byte[] bytes, int offset, int length) throws IOException {
        int r;
        if ((r = in.read(bytes, offset, length)) == -1) {
            return r;
        }
        digest.update(bytes, offset, r);
        return r;
    }

    @Override
    public String getHash(){
        return Hex.encodeHexString(digest.digest());
    }
}
