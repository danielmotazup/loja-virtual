package br.com.zup.edu.nossalojavirtual.products;

import br.com.zup.edu.nossalojavirtual.categories.Category;
import br.com.zup.edu.nossalojavirtual.categories.CategoryRepository;
import br.com.zup.edu.nossalojavirtual.exception.MensagemDeErro;
import br.com.zup.edu.nossalojavirtual.shared.email.EmailRepository;
import br.com.zup.edu.nossalojavirtual.users.Password;
import br.com.zup.edu.nossalojavirtual.users.User;
import br.com.zup.edu.nossalojavirtual.users.UserRepository;
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

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@ActiveProfiles("tests")
class QuestionControllerTest {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailRepository emailRepository;

    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private QuestionRepository questionRepository;

    private User user;
    private Category category;

    private List<Photo> photos;

    private Set<Characteristic> characteristics;

    private PreProduct preProduct;

    private Product product;


    @BeforeEach
    void setup() {

        emailRepository.deleteAll();
        questionRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        user = new User("daniel@email.com", Password.encode("123456"));
        userRepository.save(user);

        category = new Category("Banho");
        categoryRepository.save(category);

        photos = List.of(
                new Photo("foto numero 1"),
                new Photo("foto numero 2"));

        characteristics = Set.of(
                new Characteristic("cor", "branca"),
                new Characteristic("tamanho", "grande"),
                new Characteristic("peso", "500g"));

        preProduct = new PreProduct(user, category, "Toalha", new BigDecimal("15.00"), 5, "Toalha grande");

        product = new Product(preProduct, photos, characteristics);
        productRepository.save(product);

    }


    @DisplayName("deve cadastrar uma nova pergunta")
    @Test
    void teste01() throws Exception {


        NewQuestionRequest newQuestionRequest = new NewQuestionRequest("Qual a validade?");

        String payload = mapper.writeValueAsString(newQuestionRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(
                        "/api/products/{id}/questions", product.getId().toString()
                )
                .with(jwt().jwt(jwt -> {
                    jwt.claim("email", user.getUsername());
                }).authorities(new SimpleGrantedAuthority("SCOPE_products:write")))
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON).header("Accept-Language", "pt-br");

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("/api/products/*/questions/*"));

        assertEquals(1, questionRepository.findAll().size());


    }


    @DisplayName("não deve cadastrar uma questão sem autenticação")
    @Test
    void teste02() throws Exception {

        NewQuestionRequest newQuestionRequest = new NewQuestionRequest("Qual a validade?");

        String payload = mapper.writeValueAsString(newQuestionRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(
                        "/api/products/{id}/questions", product.getId().toString())
                .contentType(MediaType.APPLICATION_JSON).content(payload).header("Accept-Language", "pt-br");

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isUnauthorized());


    }

    @DisplayName("não deve cadastrar uma questão sem autorização")
    @Test
    void teste03() throws Exception {

        NewQuestionRequest newQuestionRequest = new NewQuestionRequest("Qual a validade?");

        String payload = mapper.writeValueAsString(newQuestionRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(
                        "/api/products/{id}/questions", product.getId().toString())
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON).content(payload).header("Accept-Language", "pt-br");

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isForbidden());


    }

    @DisplayName("não deve cadastrar uma questão com dados incorretos")
    @Test
    void teste04() throws Exception {

        NewQuestionRequest newQuestionRequest = new NewQuestionRequest(null);

        String payload = mapper.writeValueAsString(newQuestionRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(
                        "/api/products/{id}/questions", product.getId().toString()
                )
                .with(jwt().jwt(jwt -> {
                    jwt.claim("email", user.getUsername());
                }).authorities(new SimpleGrantedAuthority("SCOPE_products:write")))
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON).header("Accept-Language", "pt-br");

        String contentAsString = mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        MensagemDeErro mensagemDeErro = mapper.readValue(contentAsString, MensagemDeErro.class);

        assertEquals(1, mensagemDeErro.getMensagens().size());
        MatcherAssert.assertThat(mensagemDeErro.getMensagens(), Matchers.containsInAnyOrder(
                "O campo title não deve estar em branco"
        ));


    }

    @DisplayName("não deve cadastrar uma questão sem um produto")
    @Test
    void teste05() throws Exception {
        NewQuestionRequest newQuestionRequest = new NewQuestionRequest("Qual a validade?");

        String payload = mapper.writeValueAsString(newQuestionRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(
                        "/api/products/{id}/questions", UUID.randomUUID()
                )
                .with(jwt().jwt(jwt -> {
                    jwt.claim("email", user.getUsername());
                }).authorities(new SimpleGrantedAuthority("SCOPE_products:write")))
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON).header("Accept-Language", "pt-br");

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isNotFound());


    }


}