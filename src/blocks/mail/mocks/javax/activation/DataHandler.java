package javax.activation;
/**
 * Mock class providing the declarations required to compile the Cocoon code when
 * the actual library is not present.
 * 
 * @version CVS $Id: DataHandler.java,v 1.1 2003/04/17 20:22:57 haul Exp $
 */

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.activation.DataSource;

public class DataHandler implements Transferable {

    public DataHandler(DataSource ds) {
    }

    public DataHandler(java.lang.Object obj, java.lang.String mimeType) {
    }

    public DataFlavor[] getTransferDataFlavors() {
        return null;
    }

    public DataHandler(java.net.URL url) {
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return false;
    }

    public Object getTransferData(DataFlavor flavor)
        throws UnsupportedFlavorException, IOException {
        return null;
    }

}
