package nz.ac.auckland.aem.beans.aem;

/**
 * Created by gregkw on 11/12/14.
 */
public class NTUnstructured extends AbstractJCRNode {

//    Node thisNode;
//    Node parentNode;

    public NTUnstructured(String name) {
        setName(name);
//        thisNode = getNewNode(parentNode,getName());
    }

    @Override
    public String getPrimaryTypeValue() {
        return "nt:unstructured";
    }
}