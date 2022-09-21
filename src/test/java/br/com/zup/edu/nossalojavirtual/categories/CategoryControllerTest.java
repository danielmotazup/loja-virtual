package br.com.zup.edu.nossalojavirtual.categories;

import br.com.zup.edu.nossalojavirtual.exception.MensagemDeErro;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;


@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@ActiveProfiles("tests")
class CategoryControllerTest {

    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        categoryRepository.deleteAll();
    }

    @DisplayName("deve cadastrar uma nova categoria sem supercategoria")
    @Test
    void teste01() throws Exception {

        NewCategoryRequest newCategoryRequest = new NewCategoryRequest("Celulares", null);


        String payload = mapper.writeValueAsString(newCategoryRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/categories")
                .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_categories:write")))
                .contentType(MediaType.APPLICATION_JSON).content(payload).header("Accept-Language", "pt-br");

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("/api/categories/*"));

        assertEquals(1,categoryRepository.findAll().size());
    }
    @DisplayName("deve cadastrar uma nova categoria com supercategoria")
    @Test
    void teste02() throws Exception {

        Category category = new Category("Banho");
        categoryRepository.save(category);

        NewCategoryRequest newCategoryRequest = new NewCategoryRequest("Celulares", category.getId());


        String payload = mapper.writeValueAsString(newCategoryRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/categories")
                .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_categories:write")))
                .contentType(MediaType.APPLICATION_JSON).content(payload).header("Accept-Language", "pt-br");

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("/api/categories/*"));

        assertEquals(2,categoryRepository.findAll().size());

    }

    @DisplayName("não deve cadastrar uma categoria sem autenticacao")
    @Test
    void teste03() throws Exception {

        NewCategoryRequest newCategoryRequest = new NewCategoryRequest("Banho", null);

        String payload = mapper.writeValueAsString(newCategoryRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/categories")
                .content(payload).contentType(MediaType.APPLICATION_JSON).header("Accept-Language", "pt-br");

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isUnauthorized());

    }

    @DisplayName("não deve cadastrar uma categoria sem autorizacao")
    @Test
    void teste04() throws Exception {

        NewCategoryRequest newCategoryRequest = new NewCategoryRequest("Banho", null);

        String payload = mapper.writeValueAsString(newCategoryRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/categories")
                .with(jwt())
                .content(payload).contentType(MediaType.APPLICATION_JSON).header("Accept-Language", "pt-br");

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isForbidden());

    }




    @DisplayName("não deve cadastrar uma categoria sem nome")
    @Test
    void teste05() throws Exception {
        NewCategoryRequest newCategoryRequest = new NewCategoryRequest(null, null);

        String payload = mapper.writeValueAsString(newCategoryRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/categories")
                .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_categories:write")))
                .content(payload).contentType(MediaType.APPLICATION_JSON).header("Accept-Language", "pt-br");

        String payloadResponse = mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        MensagemDeErro mensagemDeErro = mapper.readValue(payloadResponse, MensagemDeErro.class);



        assertEquals(1, mensagemDeErro.getMensagens().size());
        MatcherAssert.assertThat(mensagemDeErro.getMensagens(), Matchers.containsInAnyOrder(
                "O campo name não deve estar vazio"));

    }

    @DisplayName("não deve cadastrar uma categoria com mesmo nome")
    @Test
    void teste06() throws Exception {
        Category category = new Category("Banho");
        categoryRepository.save(category);

        NewCategoryRequest newCategoryRequest = new NewCategoryRequest("Banho", null);

        String payload = mapper.writeValueAsString(newCategoryRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/categories")
                .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_categories:write")))
                .content(payload).contentType(MediaType.APPLICATION_JSON).header("Accept-Language", "pt-br");

        String payloadResponse = mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        MensagemDeErro mensagemDeErro = mapper.readValue(payloadResponse, MensagemDeErro.class);

        assertEquals(1, mensagemDeErro.getMensagens().size());
        MatcherAssert.assertThat(mensagemDeErro.getMensagens(), Matchers.containsInAnyOrder(
                "O campo name name is already registered"
        ));
    }


}