package br.com.zup.edu.nossalojavirtual.products;

import br.com.zup.edu.nossalojavirtual.categories.Category;
import br.com.zup.edu.nossalojavirtual.categories.CategoryRepository;
import br.com.zup.edu.nossalojavirtual.exception.MensagemDeErro;
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

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("tests")
@Transactional
class ProductOpinionControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductOpinionRepository productOpinionRepository;

    private User user;
    private Category category;

    private List<Photo> photos;

    private Set<Characteristic> characteristicList;
    private PreProduct preProduct;
    private Product product;


    @BeforeEach
    void setup() {
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

        characteristicList = Set.of(
                new Characteristic("cor", "branca"),
                new Characteristic("tamanho", "grande"),
                new Characteristic("peso", "500g"));

        preProduct = new PreProduct(user, category, "Toalha", BigDecimal.TEN, 5, "Toalha grande");

        product = new Product(preProduct, photos, characteristicList);
        productRepository.save(product);


    }

    @DisplayName("deve cadastrar uma nova opinião")
    @Test
    void teste01() throws Exception {

        NewOpinionRequest newOpinionRequest = new NewOpinionRequest(4, "Feedback Toalha", "a cor desbotou", product.getId());

        String payload = mapper.writeValueAsString(newOpinionRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/opinions")
                .with(jwt().jwt(jwt -> {
                    jwt.claim("email", user.getUsername());
                }).authorities(new SimpleGrantedAuthority("SCOPE_products:write")))
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("/api/opinions/*"));

        assertEquals(1, productRepository.findAll().size());

    }

    @DisplayName("não deve cadastrar uma nova opinião sem autenticação")
    @Test
    void teste02() throws Exception {

        NewOpinionRequest newOpinionRequest = new NewOpinionRequest(4, "Feedback Toalha", "a cor desbotou", product.getId());

        String payload = mapper.writeValueAsString(newOpinionRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/opinions")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isUnauthorized());


    }

    @DisplayName("não deve cadastrar uma nova opinião sem autorização")
    @Test
    void teste03() throws Exception {

        NewOpinionRequest newOpinionRequest = new NewOpinionRequest(4, "Feedback Toalha", "a cor desbotou", product.getId());

        String payload = mapper.writeValueAsString(newOpinionRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/opinions")
                .with(jwt())
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isForbidden());

    }


    @DisplayName("não deve cadastrar uma nova opinião com dados incorretos")
    @Test
    void teste04() throws Exception {

        NewOpinionRequest newOpinionRequest = new NewOpinionRequest(8, null, gera501caracteres(), product.getId());

        String payload = mapper.writeValueAsString(newOpinionRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/opinions")
                .with(jwt().jwt(jwt -> {
                    jwt.claim("email", user.getUsername());
                }).authorities(new SimpleGrantedAuthority("SCOPE_products:write")))
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON);

        String contentAsString = mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);


        MensagemDeErro mensagemDeErro = mapper.readValue(contentAsString, MensagemDeErro.class);

        assertEquals(3, mensagemDeErro.getMensagens().size());
        MatcherAssert.assertThat(mensagemDeErro.getMensagens(), Matchers.containsInAnyOrder(
                "O campo title must not be blank",
                "O campo rating must be between 1 and 5",
                "O campo description length must be between 0 and 500"
        ));


    }


    @DisplayName("não deve cadastrar uma nova opinião sem produto relacionado")
    @Test
    void teste05() throws Exception {

        NewOpinionRequest newOpinionRequest = new NewOpinionRequest(4, "Feedback Toalha", "a cor desbotou", UUID.randomUUID());

        String payload = mapper.writeValueAsString(newOpinionRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/opinions")
                .with(jwt().jwt(jwt -> {
                    jwt.claim("email", user.getUsername());
                }).authorities(new SimpleGrantedAuthority("SCOPE_products:write")))
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON);

        String contentAsString = mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        MensagemDeErro mensagemDeErro = mapper.readValue(contentAsString, MensagemDeErro.class);

        assertEquals(1, mensagemDeErro.getMensagens().size());
        MatcherAssert.assertThat(mensagemDeErro.getMensagens(), Matchers.containsInAnyOrder(
                "O campo productId Category productId is not registered"
        ));

    }


    private String gera501caracteres() {
        return "a".repeat(501);
    }


}