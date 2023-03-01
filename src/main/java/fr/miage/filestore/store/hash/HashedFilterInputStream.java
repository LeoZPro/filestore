package fr.miage.filestore.store.hash;

import javax.resource.spi.RetryableUnavailableException;
import java.io.FilterInputStream;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

/**
 * @author Jerome Blanchard (jerome.blanchard@fairandsmart.com)
 * @version 1.0
 */
public abstract class HashedFilterInputStream extends FilterInputStream {

	protected HashedFilterInputStream(InputStream in) {
		super(in);
	}
	
	public abstract String getHash();

	public static HashedFilterInputStream SHA256(InputStream is) throws NoSuchAlgorithmException {
		return new SHA256FilterInputStream(is);
	}

}
