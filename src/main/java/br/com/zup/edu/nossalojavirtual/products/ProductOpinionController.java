package br.com.zup.edu.nossalojavirtual.products;

import br.com.zup.edu.nossalojavirtual.shared.validators.ObjectIsRegisteredValidator;
import br.com.zup.edu.nossalojavirtual.users.User;
import br.com.zup.edu.nossalojavirtual.users.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.net.URI;

import static org.springframework.http.ResponseEntity.created;

@RestController
@RequestMapping("/api/opinions")
class ProductOpinionController {

    private final ProductOpinionRepository productOpinionRepository;
    private final ProductRepository productRepository;

    private final UserRepository userRepository;

    public ProductOpinionController(ProductOpinionRepository productOpinionRepository,
                                    ProductRepository productRepository, UserRepository userRepository) {
        this.productOpinionRepository = productOpinionRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    ResponseEntity<?> create(@RequestBody @Valid NewOpinionRequest newOpinion,
                             @AuthenticationPrincipal Jwt jwt) {

        User user = userRepository.findByEmail(jwt.getClaim("email")).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED,"usuario não autenticado"));


        var opinion = newOpinion.toProductOpinion(productRepository::findById, user);
        productOpinionRepository.save(opinion);

        URI location = URI.create("/api/opinions/" + opinion.getId());
        return created(location).build();
    }

    @InitBinder(value = {"newOpinionRequest"})
    void initBinder(WebDataBinder binder) {
        binder.addValidators(new ObjectIsRegisteredValidator<>("productId",
                "product.id.dontExist",
                NewOpinionRequest.class,
                productRepository::existsById));
    }

}
