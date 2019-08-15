package de.adorsys.aspsp.xs2a.connector.config.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import de.adorsys.aspsp.xs2a.util.JsonReader;
import de.adorsys.aspsp.xs2a.util.TestConfiguration;
import de.adorsys.ledgers.middleware.api.domain.sca.SCAConsentResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCAResponseTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestConfiguration.class, TokenStorageServiceImpl.class})
public class TokenStorageServiceImplTest {

    @Autowired
    private TokenStorageServiceImpl tokenStorageService;
    @Autowired
    @Qualifier(value = "objectMapper")
    private ObjectMapper mapper;

    private JsonReader jsonReader = new JsonReader();

    @Test
    public void fromBytes_SCAConsentResponseTO_success() throws IOException {
        byte[] tokenBytes = jsonReader.getStringFromFile("json/config/auth/sca-consent-response.json").getBytes();
        SCAResponseTO scaResponseTO = tokenStorageService.fromBytes(tokenBytes);

        assertNotNull(scaResponseTO);
        assertTrue(scaResponseTO instanceof SCAConsentResponseTO);
    }

    @Test(expected = IOException.class)
    public void fromBytes_objectTypesNull() throws IOException {
        tokenStorageService.fromBytes("{}".getBytes());
    }

    @Test(expected = feign.FeignException.class)
    public void fromBytes_nullValue_shouldThrowException() throws IOException {
        tokenStorageService.fromBytes(null);
    }

    @Test(expected = feign.FeignException.class)
    public void fromBytes_emptyArray_shouldThrowException() throws IOException {
        tokenStorageService.fromBytes(new byte[]{});
    }

    @Test
    public void objectType() throws IOException {
        assertEquals("test1", tokenStorageService.objectType(mapper.readTree("{\"objectType\": \"test1\"}")));
        assertNull(tokenStorageService.objectType(new TextNode("")));
        assertNull(tokenStorageService.objectType(new TextNode("{}")));
        assertNull(tokenStorageService.objectType(new TextNode(null)));
    }
}