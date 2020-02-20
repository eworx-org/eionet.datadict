package eionet.web.action;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eionet.datadict.services.JWTService;
import eionet.meta.service.ServiceException;
import eionet.util.Props;
import eionet.util.PropsIF;
import net.sf.json.JSONObject;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import org.apache.commons.lang.time.StopWatch;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * JWT Api Action Bean
 *
 * @author enver
 */
@UrlBinding("/api/jwt")
public class JWTApiActionBean extends AbstractActionBean{

    private static final Logger LOGGER = LoggerFactory.getLogger(JWTApiActionBean.class);

    /**
     * jwt service.
     */
    @SpringBean
    private JWTService jwtService;

    /**
     * Json output format.
     */
    public static final String JSON_FORMAT = "application/json";

    private String SSO_LOGIN_PAGE_URI = Props.getProperty(PropsIF.SSO_LOGIN_PAGE_URI);


    /**
     * Generates a valid JWT token for DD api for vocabulary update via rdf upload
     *
     * @return
     */
    @DefaultHandler
    public Resolution generateJWTToken(){
        StopWatch timer = new StopWatch();
        timer.start();
        LOGGER.info("generateJWTToken API - Began process for jwt token generation.");
        JSONObject jsonObject = new JSONObject();
        try {

            /* The request method should be POST*/
            if (!isPostRequest()) {
                throw new ServiceException("The request method was not POST.");
            }

            HttpServletRequest request = getContext().getRequest();
            String username = request.getParameter("username");
            String password = request.getParameter("password");

            if (username == null || password == null) {
                throw new ServiceException("Credentials were missing.");
            }

            LOGGER.info(String.format("generateJWTToken API - User %s has requested generation of a JWT token.", username));

            if (authenticateUser(username, password) == false) {
                throw new ServiceException("Wrong credentials were retrieved.");
            }

            //TODO authorization
        /*if (!user.isUserInRole("dd_admin")) {
            throw new ServiceException("User is not admin.");
        }*/

            String generatedToken = this.getJwtService().generateJWTToken();

            jsonObject = new JSONObject();
            jsonObject.put("token", generatedToken);
            String outputMsg = new ObjectMapper().writeValueAsString(jsonObject);
            Resolution resolution = new StreamingResolution(JSON_FORMAT, outputMsg);

            timer.stop();
            LOGGER.info(String.format("generateJWTToken API - Generation of token for user %s was completed, total time of execution: %s", username, timer.toString()));
            return resolution;
        }
        catch(Exception e){
            jsonObject.put("Error", e.getMessage());
            String outputMsg = null;
            try {
                outputMsg = new ObjectMapper().writeValueAsString(jsonObject);
            } catch (JsonProcessingException ex) {
                jsonObject.put("Error", ex.getMessage());
            }

            Resolution resolution = new StreamingResolution(JSON_FORMAT, outputMsg);
            LOGGER.info(String.format("generateJWTToken API - %s", e.getMessage()));
            return resolution;
        }
    }

    public JWTService getJwtService() {
        return jwtService;
    }

    public String getSSO_LOGIN_PAGE_URI() {
        return SSO_LOGIN_PAGE_URI;
    }

    public String getExecutionValueFromSSOPage() throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        String pageHtml = restTemplate.getForObject(this.getSSO_LOGIN_PAGE_URI(), String.class);
        Document doc = Jsoup.parse(pageHtml);
        Element execution = doc.select("input[name=execution]").first();
        if(execution == null){
            String errorMsg = String.format("The execution input type from the %s page does not exist.", this.getSSO_LOGIN_PAGE_URI());
            throw new Exception(errorMsg);
        }
        if(execution.val() == null || execution.val().length()==0){
            String errorMsg = String.format("The execution input type from the %s page has empty value.", this.getSSO_LOGIN_PAGE_URI());
            throw new Exception(errorMsg);
        }
        return execution.val();
    }

    public Boolean authenticateUser(String username, String password) throws Exception {

        String executionParam = getExecutionValueFromSSOPage();

        HttpPost post = new HttpPost(this.getSSO_LOGIN_PAGE_URI());

        // add request parameter, form parameters
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("username", username));
        urlParameters.add(new BasicNameValuePair("password", password));
        urlParameters.add(new BasicNameValuePair("execution", executionParam));
        urlParameters.add(new BasicNameValuePair("_eventId", "submit"));

        post.setEntity(new UrlEncodedFormEntity(urlParameters));

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(post)) {

            Integer statusCode = response.getStatusLine().getStatusCode();
            if(statusCode == HttpStatus.SC_OK){
                return true;
            }
            return false;
        }
        catch (Exception e){
            throw (e);
        }
    }

}
