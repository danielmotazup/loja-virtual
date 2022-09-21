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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@ActiveProfiles("tests")
class ProductControllerTest {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;

    private List<NewCharacteristicRequest> characteristicList = new ArrayList<>();

    private List<String> photos;


    private Category category;


    private User user;


    @BeforeEach
    void setup() {

        productRepository.deleteAll();
        userRepository.deleteAll();
        categoryRepository.deleteAll();

        user = new User("daniel.mota@email.com.br", Password.encode("123456"));
        userRepository.save(user);

        category = new Category("Banho");
        categoryRepository.save(category);

        photos = List.of(
                "foto numero 1",
                "foto numero 2"
        );

        characteristicList = List.of(
                new NewCharacteristicRequest("peso", "dois quilos"),
                new NewCharacteristicRequest("tamanho", "um metro"),
                new NewCharacteristicRequest("cor", "azul")
        );


    }

    @DisplayName("deve cadastrar um novo produto")
    @Test
    @Transactional
    void teste01() throws Exception {

        NewProductRequest newProductRequest = new NewProductRequest("Toalha", new BigDecimal("15.00"), 5, photos,
                characteristicList, "toalha macia", category.getId());


        String payload = mapper.writeValueAsString(newProductRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/products")
                .with(jwt().jwt(jwt -> {
                    jwt.claim("email", user.getUsername());
                }).authorities(new SimpleGrantedAuthority("SCOPE_products:write")))

                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Accept-Language", "pt-br");


        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("/api/products/*"));


        assertEquals(1, productRepository.findAll().size());

    }

    @DisplayName("não deve cadastrar um produto sem autenticação")
    @Test
    void teste02() throws Exception {

        NewProductRequest newProductRequest = new NewProductRequest("Toalha", new BigDecimal("15.00"), 5, photos,
                characteristicList, "toalha macia", category.getId());


        String payload = mapper.writeValueAsString(newProductRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/products")

                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Accept-Language", "pt-br");


        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isUnauthorized());


    }

    @DisplayName("não deve cadastrar um produto sem autorização")
    @Test
    void teste03() throws Exception {

        NewProductRequest newProductRequest = new NewProductRequest("Toalha", new BigDecimal("15.00"), 5, photos,
                characteristicList, "toalha macia", category.getId());


        String payload = mapper.writeValueAsString(newProductRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/products")
                .with(jwt())
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Accept-Language", "pt-br");


        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isForbidden());

    }

    @DisplayName("não deve cadastrar um produto com dados inválidos")
    @Test
    void test04() throws Exception {

        List<NewCharacteristicRequest> newCharacteristicRequest = List.of(
                new NewCharacteristicRequest("Durabilidade", "Alta"),
                new NewCharacteristicRequest("Cor", "Cinza")

        );


        NewProductRequest newProductRequest = new NewProductRequest(null, new BigDecimal("0.00"), 0, photos,
                newCharacteristicRequest, gera1001Carecteres(), category.getId());

        String payload = mapper.writeValueAsString(newProductRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/products")
                .with(jwt().jwt(jwt -> {
                    jwt.claim("email", user.getUsername());
                }).authorities(new SimpleGrantedAuthority("SCOPE_products:write")))
                .content(payload).contentType(MediaType.APPLICATION_JSON)
                .header("Accept-Language", "pt-br");

        String response = mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse().getContentAsString(StandardCharsets.UTF_8);

        MensagemDeErro mensagemDeErro = mapper.readValue(response, MensagemDeErro.class);


        assertEquals(4, mensagemDeErro.getMensagens().size());
        MatcherAssert.assertThat(mensagemDeErro.getMensagens(), Matchers.containsInAnyOrder(
                "O campo description o comprimento deve ser entre 0 e 1000",
                "O campo characteristics tamanho deve ser entre 3 e 2147483647",
                "O campo price deve ser maior que ou igual a 0.01",
                "O campo name não deve estar em branco"));
    }

    @DisplayName("não deve cadastrar um produto sem categoria")
    @Test
    void test05() throws Exception {

        NewProductRequest newProductRequest = new NewProductRequest("Toalha", new BigDecimal("15.00"), 5, photos,
                characteristicList, "toalha macia", Long.MAX_VALUE);

        String payload = mapper.writeValueAsString(newProductRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/products")
                .with(jwt().jwt(jwt -> {
                    jwt.claim("email", user.getUsername());
                }).authorities(new SimpleGrantedAuthority("SCOPE_products:write")))
                .content(payload).contentType(MediaType.APPLICATION_JSON)
                .header("Accept-Language", "pt-br");

        String contentAsString = mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse().getContentAsString(StandardCharsets.UTF_8);

        MensagemDeErro mensagemDeErro = mapper.readValue(contentAsString, MensagemDeErro.class);

        assertEquals(1, mensagemDeErro.getMensagens().size());
        MatcherAssert.assertThat(mensagemDeErro.getMensagens(), Matchers.containsInAnyOrder(
                "O campo categoryId Category categoryId is not registered"
        ));


    }






    private String gera1001Carecteres() {
        return "a".repeat(1001);
    }


}