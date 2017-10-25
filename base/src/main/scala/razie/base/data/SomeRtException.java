/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.base.data;

/**
 * unknown exception - used temp in development when i can't find a better one
 * 
 * @author razvanc99
 */
public class SomeRtException extends RuntimeException {

    public SomeRtException() {
    }

    public SomeRtException(String arg0) {
        super(arg0);
    }

    public SomeRtException(Throwable arg0) {
        super(arg0);
    }

    public SomeRtException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

}
