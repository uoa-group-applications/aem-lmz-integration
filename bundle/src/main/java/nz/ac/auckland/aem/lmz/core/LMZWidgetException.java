package nz.ac.auckland.aem.lmz.core;

/**
 * Created by gregkw on 18/11/14.
 */
public class LMZWidgetException extends RuntimeException {

    public LMZWidgetException() {
        super();
    }

    public LMZWidgetException(String msg, Throwable t) {
        super(msg, t);
    }

    public LMZWidgetException(String msg) {
        super(msg);
    }

    public LMZWidgetException(Throwable t) {
        super(t);
    }

}
