package br.com.zup.edu.nossalojavirtual.users;

import br.com.zup.edu.nossalojavirtual.categories.CategoryRepository;
import br.com.zup.edu.nossalojavirtual.exception.MensagemDeErro;
import br.com.zup.edu.nossalojavirtual.products.ProductRepository;
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
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;



    @BeforeEach
    void setup() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
        userRepository.deleteAll();
    }

    @DisplayName("deve cadastrar um novo usuario")
    @Test
    void teste01() throws Exception {

        NewUserRequest newUserRequest = new NewUserRequest("user@email.com", "123456");

        String payload = mapper.writeValueAsString(newUserRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/users").with(
                        jwt().authorities(new SimpleGrantedAuthority("SCOPE_users:write")))


                .contentType(MediaType.APPLICATION_JSON).content(payload).header("Accept-Language", "pt-br");

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("/api/users/*"));

        assertEquals(1,userRepository.findAll().size());


    }

    @DisplayName("não deve cadastrar um usuario não autenticado")
    @Test
    void teste02() throws Exception {

        NewUserRequest newUser = new NewUserRequest("user@email.com", "112233");
        Password password = Password.encode(newUser.getPassword());
        User user = new User(newUser.getLogin(), password);
        userRepository.save(user);


        NewUserRequest newUserRequest = new NewUserRequest("user@email.com", "987445");

        String payload = mapper.writeValueAsString(newUserRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/users")
                .content(payload).contentType(MediaType.APPLICATION_JSON).header("Accept-Language", "pt-br");

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isUnauthorized());

    }

    @DisplayName("não deve cadastrar um usuario nao autorizado")
    @Test
    void teste03() throws Exception {

        NewUserRequest newUser = new NewUserRequest("user@email.com", "112233");
        Password password = Password.encode(newUser.getPassword());
        User user = new User(newUser.getLogin(), password);
        userRepository.save(user);


        NewUserRequest newUserRequest = new NewUserRequest("user@email.com", "987445");

        String payload = mapper.writeValueAsString(newUserRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/users").with(
                        jwt())
                .content(payload).contentType(MediaType.APPLICATION_JSON).header("Accept-Language", "pt-br");

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isForbidden());

    }

    @DisplayName("não deve cadastrar um usuario com dados incorretos")
    @Test
    void teste04() throws Exception {

        NewUserRequest newUserRequest = new NewUserRequest(null, "12");

        String payload = mapper.writeValueAsString(newUserRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/users").with(
                        jwt().authorities(new SimpleGrantedAuthority("SCOPE_users:write")))
                .content(payload).contentType(MediaType.APPLICATION_JSON).header("Accept-Language", "pt-br");

        String payloadResponse = mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        MensagemDeErro mensagemDeErro = mapper.readValue(payloadResponse, MensagemDeErro.class);

        assertEquals(2, mensagemDeErro.getMensagens().size());

        MatcherAssert.assertThat(mensagemDeErro.getMensagens(), Matchers.containsInAnyOrder(
                "O campo password tamanho deve ser entre 6 e 2147483647",
                "O campo login não deve estar vazio"

        ));

    }

    @DisplayName("não deve cadastrar um usuario com email invalido")
    @Test
    void teste05() throws Exception {

        NewUserRequest newUserRequest = new NewUserRequest("user", "123456");

        String payload = mapper.writeValueAsString(newUserRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/users").with(
                        jwt().authorities(new SimpleGrantedAuthority("SCOPE_users:write")))
                .content(payload).contentType(MediaType.APPLICATION_JSON).header("Accept-Language", "pt-br");

        String payloadResponse = mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        MensagemDeErro mensagemDeErro = mapper.readValue(payloadResponse, MensagemDeErro.class);

        assertEquals(1, mensagemDeErro.getMensagens().size());

        MatcherAssert.assertThat(mensagemDeErro.getMensagens(), Matchers.containsInAnyOrder(
                "O campo login deve ser um endereço de e-mail bem formado"

        ));

    }

    @DisplayName("não deve cadastrar um usuario com mesmo email")
    @Test
    void teste06() throws Exception {

        NewUserRequest newUser = new NewUserRequest("user@email.com", "112233");
        Password password = Password.encode(newUser.getPassword());
        User user = new User(newUser.getLogin(), password);
        userRepository.save(user);


        NewUserRequest newUserRequest = new NewUserRequest("user@email.com", "987445");

        String payload = mapper.writeValueAsString(newUserRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/users").with(
                        jwt().authorities(new SimpleGrantedAuthority("SCOPE_users:write")))
                .content(payload).contentType(MediaType.APPLICATION_JSON).header("Accept-Language", "pt-br");

        String payloadResponse = mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        MensagemDeErro mensagemDeErro = mapper.readValue(payloadResponse, MensagemDeErro.class);

        assertEquals(1, mensagemDeErro.getMensagens().size());

        MatcherAssert.assertThat(mensagemDeErro.getMensagens(), Matchers.containsInAnyOrder(
                "O campo login login is already registered"

        ));

    }



}