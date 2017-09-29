package eionet.datadict.web;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import eionet.config.ApplicationTestContext;
import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.Diff;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ApplicationTestContext.class})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class})
@DatabaseSetup(type = DatabaseOperation.CLEAN_INSERT,
        value = "classpath:seed-datasetIT.xml")
@DatabaseTearDown(type = DatabaseOperation.DELETE_ALL,
        value = "classpath:seed-datasetIT.xml")
public class DatasetControllerTestIT {

    private MockMvc mockMvc;

    @Autowired
    DataSetController dataSetController;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(dataSetController).defaultRequest(get("/v2/")).build();
    }

    @Test
    public void testFailToGetDatasetXMLSchemaBecauseOfNotFoundDatasetWithGivenId() throws Exception {
        MockHttpServletRequestBuilder request = get("/dataset/4242/schema-dst-4242.xsd");
        
        request.contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(request).andExpect(status().is3xxRedirection());
        mockMvc.perform(request).andDo(print());
    }

    @Test
    public void testSuccessToGetDatasetXMLSchema() throws Exception {
        MockHttpServletRequestBuilder request = get("/dataset/2827/schema-dst-2827.xsd");
        request.contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(request).andExpect(status().isOk());
        mockMvc.perform(request).andDo(print());
        assertEquals(MediaType.APPLICATION_XML.toString(), mockMvc.perform(request).andReturn().getResponse().getContentType());
        assertEquals("attachment;filename=schema-dst-2827.xsd", mockMvc.perform(request).andReturn().getResponse().getHeader("Content-Disposition"));
        String xmlResult = mockMvc.perform(request).andReturn().getResponse().getContentAsString();
        ClassLoader classLoader = getClass().getClassLoader();
        String expectedXMLResultString = IOUtils.toString(classLoader.getResourceAsStream("datasetXMLSChemaTestIT.xsd"));
        Diff diff = new Diff(expectedXMLResultString, xmlResult);
        assertTrue(diff.similar());
    }
    
    @Test
    public void testFailToGetDatasetTableXMLSchemaBecauseOfNotFoundDatasetTableWithGivenId() throws Exception {
        MockHttpServletRequestBuilder request = get("/dataset/4242/schema-tbl-42222.xsd");
        request.contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(request).andExpect(status().is3xxRedirection());
        mockMvc.perform(request).andDo(print());
    }

    @Test
    public void testSuccessToGetDatasetTableXMLSchema() throws Exception {
        MockHttpServletRequestBuilder request = get("/dataset/2827/schema-tbl-6661.xsd");
        request.contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(request).andExpect(status().isOk());
        mockMvc.perform(request).andDo(print());
        assertEquals(MediaType.APPLICATION_XML.toString(), mockMvc.perform(request).andReturn().getResponse().getContentType());
        assertEquals("attachment;filename=schema-tbl-6661.xsd", mockMvc.perform(request).andReturn().getResponse().getHeader("Content-Disposition"));
        String xmlResult = mockMvc.perform(request).andReturn().getResponse().getContentAsString();
        ClassLoader classLoader = getClass().getClassLoader();
        String expectedXMLResultString = IOUtils.toString(classLoader.getResourceAsStream("datasetTableXMLSchemaTestIT.xsd"));
        Diff diff = new Diff(expectedXMLResultString, xmlResult);
        assertTrue(diff.similar());
    }

   // @Test
    public void testFailToGetDatasetXMLInstanceBecauseOfNotFoundDatasetWithGivenId() throws Exception {
        MockHttpServletRequestBuilder request = get("/dataset/4242/dataset-instance.xml");
        request.contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(request).andExpect(status().is3xxRedirection());
        mockMvc.perform(request).andDo(print());
    }

  //  @Test
    public void testSuccessToGetDatasetXMLInstance() throws Exception {
        MockHttpServletRequestBuilder request = get("/dataset/2827/dataset-instance.xml");
        request.contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(request).andExpect(status().isOk());
        mockMvc.perform(request).andDo(print());
        assertEquals(MediaType.APPLICATION_XML.toString(), mockMvc.perform(request).andReturn().getResponse().getContentType());
        assertEquals("attachment;filename=dataset-instance.xml", mockMvc.perform(request).andReturn().getResponse().getHeader("Content-Disposition"));
        String xmlResult = mockMvc.perform(request).andReturn().getResponse().getContentAsString();
        ClassLoader classLoader = getClass().getClassLoader();
        String expectedXMLResultString = IOUtils.toString(classLoader.getResourceAsStream("datasetXMLInstanceTestIT.xml"));
        Diff diff = new Diff(expectedXMLResultString, xmlResult);
        assertTrue(diff.similar());
    }

  //  @Test
    public void testFailToGetDatasetTableXMLInstanceBecauseOfNotFoundDatasetTableWithGivenId() throws Exception {
        MockHttpServletRequestBuilder request = get("/dataset/4242/table-42222-instance.xml");
        request.contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(request).andExpect(status().is3xxRedirection());
        mockMvc.perform(request).andDo(print());
    }

    //@Test
    public void testSuccessToGetDatasetTableXMLInstance() throws Exception {
        MockHttpServletRequestBuilder request = get("/dataset/2827/table-6661-instance.xml");
        request.contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(request).andExpect(status().isOk());
        mockMvc.perform(request).andDo(print());
        assertEquals(MediaType.APPLICATION_XML.toString(), mockMvc.perform(request).andReturn().getResponse().getContentType());
        assertEquals("attachment;filename=table-6661-instance.xml", mockMvc.perform(request).andReturn().getResponse().getHeader("Content-Disposition"));
        String xmlResult = mockMvc.perform(request).andReturn().getResponse().getContentAsString();
        ClassLoader classLoader = getClass().getClassLoader();
        String expectedXMLResultString = IOUtils.toString(classLoader.getResourceAsStream("datasetTableXMLInstanceTestIT.xml"));
        Diff diff = new Diff(expectedXMLResultString, xmlResult);
        assertTrue(diff.similar());
    }
}
