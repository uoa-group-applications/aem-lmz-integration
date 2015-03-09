package nz.ac.auckland.aem.beans.aem;

import com.day.cq.security.util.AclPolicy;
import com.day.cq.security.util.CRXPolicyManager;
import com.day.cq.wcm.api.NameConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.security.Privilege;

/**
 * Created by gregkw on 11/12/14.
 */
public class CQPanel extends AbstractJCRNode {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(CQPanel.class);

    public CQPanel(String name) {
        setName(name);
    }

    @Override
    public String getPrimaryTypeValue() {
        return "cq:Panel";
    }

}