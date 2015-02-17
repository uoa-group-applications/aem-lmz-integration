package nz.ac.auckland.aem.beans.aem;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by gregkw on 11/12/14.
 */
public abstract class AbstractJCRNode {

    public Node getThisNode() {
        return thisNode;
    }

    public void setThisNode(Node thisNode) {
        this.thisNode = thisNode;
    }

    public Node getParentNode() {
        return parentNode;
    }

    public void setParentNode(Node parentNode) {
        this.parentNode = parentNode;
    }

    Node thisNode;
    Node parentNode;

    public Node getNewNode(Node parent, AbstractJCRNode widget) {
        Node retVal = null;
        try {
            retVal = parent.addNode(widget.getName(), widget.getPrimaryTypeValue());
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        return retVal;
    }

    public abstract String getPrimaryTypeValue();

    private String name;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, AbstractJCRNode> widgets = new HashMap<String, AbstractJCRNode>();

    public Map<String, JCRProperty> jcrProperties = new HashMap<String, JCRProperty>();

    public Map<String, JCRProperty> getJcrProperties() {
        return jcrProperties;
    }

    public void setJcrProperties(Map<String, JCRProperty> jcrProperties) {
        this.jcrProperties = jcrProperties;
    }

    public Map<String, AbstractJCRNode> getWidgets() {
        return widgets;
    }

    public void setWidgets(Map<String, AbstractJCRNode> widgets) {
        this.widgets = widgets;
    }

    public JCRProperty getPrimaryType() {
        return new JCRProperty("jcr:primaryType", "Name", getPrimaryTypeValue());
    }

    public Node addChildWidget(AbstractJCRNode widget) {
        widgets.put(widget.getName(), widget);
        Node retVal = getNewNode(thisNode,widget);
        widget.setThisNode(retVal);
        widget.cloneJCRProperties();
//            addJCRProperties(widget, retVal);
        return retVal;
    }

    public void cloneJCRProperties() {

        for (Map.Entry<String, JCRProperty> prop : getJcrProperties().entrySet()) {
            JCRProperty jcrProp = prop.getValue();
            try {
                thisNode.setProperty(jcrProp.getName(), jcrProp.getValue(), jcrProp.getJCRMappedType());
            } catch (RepositoryException e) {
                e.printStackTrace();
            }
        }
    }
    public void addJCRProperties(AbstractJCRNode jcrNode, Node node) throws RepositoryException {

        for (Map.Entry<String, JCRProperty> prop : jcrNode.getJcrProperties().entrySet()) {
            JCRProperty jcrProp = prop.getValue();
            node.setProperty(jcrProp.getName(), jcrProp.getValue(), jcrProp.getJCRMappedType());
        }
    }

    public void addProperty(String name, String type, String value) {
        this.addProperty(name, new JCRProperty(name, type, value));
    }

    public void addProperty(JCRProperty property) {
        this.addProperty(property.getName(), property);
    }

    public void addProperty(String name, JCRProperty property) {
        if (property.isValid()) {
            jcrProperties.put(name, property);
        }
    }


}
