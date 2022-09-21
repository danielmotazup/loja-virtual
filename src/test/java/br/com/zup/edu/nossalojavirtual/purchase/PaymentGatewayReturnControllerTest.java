package br.com.zup.edu.nossalojavirtual.purchase;

import br.com.zup.edu.nossalojavirtual.categories.Category;
import br.com.zup.edu.nossalojavirtual.categories.CategoryRepository;
import br.com.zup.edu.nossalojavirtual.products.*;
import br.com.zup.edu.nossalojavirtual.users.Password;
import br.com.zup.edu.nossalojavirtual.users.User;
import br.com.zup.edu.nossalojavirtual.users.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("tests")
class PaymentGatewayReturnControllerTest {

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

    private Purchase purchase;


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

        preProduct = new PreProduct(user, category, "Toalha", BigDecimal.TEN, 50, "Toalha grande");

        product = new Product(preProduct, photos, characteristicList);
        productRepository.save(product);

        purchase = new Purchase(user,product,10,PaymentGateway.PAYPAL);
        purchaseRepository.save(purchase);


    }

    @DisplayName("deve confirmar pagamento com status sucesso")
    @Test
    void teste01()  throws Exception{

        PaymentReturn paymentReturn = new PaymentReturn(purchase.getId(),"1","SUCESSO");

        String payload = mapper.writeValueAsString(paymentReturn);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/purchases/confirm-payment")
                .contentType(MediaType.APPLICATION_JSON).content(payload)
                .with(jwt().jwt(jwt -> {
                    jwt.claim("email", user.getUsername());
                }).authorities(new SimpleGrantedAuthority("SCOPE_products:write")));

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());

    }

    @DisplayName("deve confirmar pagamento com status error")
    @Test
    void teste02()  throws Exception{

        PaymentReturn paymentReturn = new PaymentReturn(purchase.getId(),"1","ERROR");

        String payload = mapper.writeValueAsString(paymentReturn);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/purchases/confirm-payment")
                .contentType(MediaType.APPLICATION_JSON).content(payload)
                .with(jwt().jwt(jwt -> {
                    jwt.claim("email", user.getUsername());
                }).authorities(new SimpleGrantedAuthority("SCOPE_products:write")));

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());

    }








}