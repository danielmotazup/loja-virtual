package br.com.zup.edu.nossalojavirtual.products;

import br.com.zup.edu.nossalojavirtual.categories.Category;
import br.com.zup.edu.nossalojavirtual.categories.CategoryRepository;
import br.com.zup.edu.nossalojavirtual.users.Password;
import br.com.zup.edu.nossalojavirtual.users.User;
import br.com.zup.edu.nossalojavirtual.users.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("tests")
class ProductDetailsControllerTest {

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

    private User user;
    private Category category;

    private List<Photo> photos;

    private Set<Characteristic> characteristicList;
    private PreProduct preProduct;
    private Product product;

    @BeforeEach
    void setup(){
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

    @DisplayName("deve listar um produto existente")
    @Test
    void teste01()throws Exception{

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/api/products/{id}",product.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwt().jwt(jwt -> {
                    jwt.claim("email", user.getUsername());
                }).authorities(new SimpleGrantedAuthority("SCOPE_products:read")));

        String payload = mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        ProductDetailsResponse response = mapper.readValue(payload,ProductDetailsResponse.class);

        Assertions.assertThat(response).extracting("id","stockQuantity","description")
                .contains(
                        product.getId(),product.getStockQuantity(),product.getDescription()
                );


    }

    @DisplayName("não deve listar um produto sem autenticação")
    @Test
    void teste02()throws Exception {

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/api/products/{id}", product.getId())
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }



    @DisplayName("Não deve listar um produto inexistente")
    @Test
    void teste03()throws Exception {

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/api/products/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwt().jwt(jwt -> {
                    jwt.claim("email", user.getUsername());
                }).authorities(new SimpleGrantedAuthority("SCOPE_products:read")));

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isNotFound());
    }
































}