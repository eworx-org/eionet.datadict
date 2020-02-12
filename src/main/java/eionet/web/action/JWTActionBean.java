package eionet.web.action;

import eionet.datadict.services.JWTService;
import eionet.meta.service.ServiceException;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.integration.spring.SpringBean;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@UrlBinding("/generateJWTToken")
public class JWTActionBean extends AbstractActionBean{

    private static final Logger LOGGER = LoggerFactory.getLogger(JWTActionBean.class);

    private String token;

    /**
     * jwt service.
     */
    @SpringBean
    private JWTService jwtService;

    public static final String GENERATE_TOKEN_PAGE = "/pages/generateJWTToken.jsp";

    @DefaultHandler
    public Resolution view() {
        return new ForwardResolution(GENERATE_TOKEN_PAGE);
    }

    /**
     * Generates a valid JWT token for DD api for vocabulary update via rdf upload
     *
     * @return
     */

    public Resolution generateToken() throws ServiceException {
        LOGGER.info("generateJWTToken - Began process for jwt token generation.");
        StopWatch timer = new StopWatch();
        timer.start();
        String generatedToken = this.getJwtService().generateJWTToken();
        timer.stop();
        LOGGER.info("generateJWTToken - Generation of token was completed, total time of execution: " + timer.toString());
        return new ForwardResolution(GENERATE_TOKEN_PAGE).addParameter("generated_token", generatedToken);
    }

    public JWTService getJwtService() {
        return jwtService;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
