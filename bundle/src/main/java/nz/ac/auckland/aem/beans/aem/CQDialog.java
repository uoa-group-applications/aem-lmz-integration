package nz.ac.auckland.aem.beans.aem;

import javax.jcr.Node;

/**
 * Created by gregkw on 11/12/14.
 */
public class CQDialog extends AbstractJCRNode {

    private CQWidgetCollection items;

    public CQDialog(Node node) {
        setName("dialog");
        thisNode = node;
    }

    @Override
    public String getPrimaryTypeValue() {
       return "cq:Dialog";
    }

    protected JCRProperty getXType() {
        return new JCRProperty("xtype","String","dialog");
    }
    
    protected CQWidgetCollection getItems() {
        return items;
    }
}
