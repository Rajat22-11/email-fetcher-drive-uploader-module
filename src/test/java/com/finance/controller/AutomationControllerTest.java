package com.finance.controller;

import com.finance.service.EmailFetcherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AutomationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EmailFetcherService emailFetcherService;

    private AutomationController controller;

    @BeforeEach
    void setUp() {
        controller = new AutomationController(emailFetcherService);
        ReflectionTestUtils.setField(controller, "applicationName", "Email Fetcher Drive Uploader");
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void healthEndpointReturnsJsonPayload() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.application").value("Email Fetcher Drive Uploader"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.uptimeMs").value(greaterThanOrEqualTo(0)));
    }

    @Test
    void triggerFetchReturnsSuccessAndCallsService() throws Exception {
        mockMvc.perform(post("/api/cron/fetch-screener"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(emailFetcherService).processAll();
    }

    @Test
    void triggerFetchReturnsServerErrorWhenServiceFails() throws Exception {
        doThrow(new RuntimeException("boom")).when(emailFetcherService).processAll();

        mockMvc.perform(post("/api/cron/fetch-screener"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("ERROR"));
    }
}



