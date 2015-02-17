package nz.ac.auckland.aem.beans.aem;

/**
 * Created by gregkw on 11/12/14.
 */
public class CQWidgetCollection extends AbstractJCRNode {

//    Node thisNode;
//    Node parentNode;

    public CQWidgetCollection(String name) {
        setName(name);
//        thisNode = getNewNode(parentNode,getName());
    }

    @Override
    public String getPrimaryTypeValue() {
        return "cq:WidgetCollection";
    }
}