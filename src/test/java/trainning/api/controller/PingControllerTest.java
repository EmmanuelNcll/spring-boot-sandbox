package trainning.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class PingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private final String pingEndpoint = "/ping";

    @Test
    public void testPingEndpoint() throws Exception {
        ResultActions resultActions = mockMvc.perform(get(pingEndpoint).accept(MediaType.TEXT_PLAIN));

        resultActions.andExpect(status().isOk()).andExpect(content().string("OK"));
    }
}
