package eionet.datadict.services.acl;

import eionet.meta.DDUser;
import eionet.datadict.errors.UserAuthorizationException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

public interface AclService {

    boolean hasPermission(DDUser user, AclEntity entity, Permission prm);
    
    boolean hasPermission(DDUser user, AclEntity entity, String entityId, Permission prm);
    
    void grantAccess(DDUser user, AclEntity entity, String entityId, String description) 
            throws UserAuthorizationException;
    
    void removeAccessRightsForDeletedEntity(AclEntity entity, String entityId);

    void addUserToAclGroup(String username,String groupName) throws ParserConfigurationException, IOException, SAXException, TransformerException;
}
