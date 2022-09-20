package br.com.zup.edu.nossalojavirtual.purchase;

import br.com.zup.edu.nossalojavirtual.categories.Category;
import br.com.zup.edu.nossalojavirtual.categories.CategoryRepository;
import br.com.zup.edu.nossalojavirtual.exception.MensagemDeErro;
import br.com.zup.edu.nossalojavirtual.products.*;
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
import org.springframework.validation.BindException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@ActiveProfiles("tests")
class PurchaseControllerTest {

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
    private PurchaseRepository purchaseRepository;


    private User user;
    private Category category;

    private List<Photo> photos;

    private Set<Characteristic> characteristicList;
    private PreProduct preProduct;
    private Product product;


    @BeforeEach
    void setup() {
        purchaseRepository.deleteAll();
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

    @DisplayName("Deve realizar uma nova compra com Paypal")
    @Test
    void teste01() throws Exception {

        NewPurchaseRequest newPurchaseRequest = new NewPurchaseRequest(product.getId(), 3, PaymentGateway.PAYPAL);

        String payload = mapper.writeValueAsString(newPurchaseRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/purchase")
                .content(payload).contentType(MediaType.APPLICATION_JSON)
                .with(jwt().jwt(jwt -> {
                    jwt.claim("email", user.getUsername());
                }).authorities(new SimpleGrantedAuthority("SCOPE_purchase:write")));

        String contentAsString = mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        Long id = purchaseRepository.findAll().get(0).getId();

        String url = String.format("{\"paymentUrl\":\"paypal.com/%d?redirectUrl=http://localhost/api/purchases/confirm-payment\"}", id);

        assertEquals(1, purchaseRepository.findAll().size());
        assertEquals(contentAsString, url);

    }

    @DisplayName("Deve realizar uma nova compra com PagSeguro")
    @Test
    void teste02() throws Exception {

        NewPurchaseRequest newPurchaseRequest = new NewPurchaseRequest(product.getId(), 3, PaymentGateway.PAGSEGURO);

        String payload = mapper.writeValueAsString(newPurchaseRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/purchase")
                .content(payload).contentType(MediaType.APPLICATION_JSON)
                .with(jwt().jwt(jwt -> {
                    jwt.claim("email", user.getUsername());
                }).authorities(new SimpleGrantedAuthority("SCOPE_purchase:write")));

        String contentAsString = mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        Long id = purchaseRepository.findAll().get(0).getId();
        String url = String.format("{\"paymentUrl\":\"pagseguro.com?returnId=%d&redirectUrl=http://localhost/api/purchases/confirm-payment\"}", id);

        assertEquals(1, purchaseRepository.findAll().size());
        assertEquals(contentAsString, url);

    }

    @DisplayName("Não deve realizar compra sem autenticação")
    @Test
    void teste03() throws Exception {

        NewPurchaseRequest newPurchaseRequest = new NewPurchaseRequest(product.getId(), 3, PaymentGateway.PAGSEGURO);

        String payload = mapper.writeValueAsString(newPurchaseRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/purchase")
                .content(payload).contentType(MediaType.APPLICATION_JSON);


        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @DisplayName("Não deve realizar compra sem autorização")
    @Test
    void teste04() throws Exception {

        NewPurchaseRequest newPurchaseRequest = new NewPurchaseRequest(product.getId(), 3, PaymentGateway.PAGSEGURO);

        String payload = mapper.writeValueAsString(newPurchaseRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/purchase")
                .content(payload).contentType(MediaType.APPLICATION_JSON)
                .with(jwt());


        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @DisplayName("não deve realizar compra sem quantidade")
    @Test
    void teste05() throws Exception {

        NewPurchaseRequest newPurchaseRequest = new NewPurchaseRequest(product.getId(), -1, PaymentGateway.PAGSEGURO);

        String payload = mapper.writeValueAsString(newPurchaseRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/purchase")
                .content(payload).contentType(MediaType.APPLICATION_JSON)
                .with(jwt().jwt(jwt -> {
                    jwt.claim("email", user.getUsername());
                }).authorities(new SimpleGrantedAuthority("SCOPE_purchase:write")));

        String response = mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        MensagemDeErro mensagemDeErro = mapper.readValue(response, MensagemDeErro.class);

        assertEquals(1, mensagemDeErro.getMensagens().size());
        MatcherAssert.assertThat(mensagemDeErro.getMensagens(), Matchers.containsInAnyOrder(

                "O campo quantity must be greater than or equal to 1"

        ));


    }


    @DisplayName("não deve realizar compra sem estoque")
    @Test
    void teste06() throws Exception {

        NewPurchaseRequest newPurchaseRequest = new NewPurchaseRequest(product.getId(), 10, PaymentGateway.PAGSEGURO);

        String payload = mapper.writeValueAsString(newPurchaseRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/purchase")
                .content(payload).contentType(MediaType.APPLICATION_JSON)
                .with(jwt().jwt(jwt -> {
                    jwt.claim("email", user.getUsername());
                }).authorities(new SimpleGrantedAuthority("SCOPE_purchase:write")));

        Exception resolvedException = mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn().getResolvedException();

        assertEquals(BindException.class, resolvedException.getClass());


    }

}