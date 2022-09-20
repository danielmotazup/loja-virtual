package br.com.zup.edu.nossalojavirtual.purchase;

import br.com.zup.edu.nossalojavirtual.products.ProductRepository;
import br.com.zup.edu.nossalojavirtual.shared.validators.ObjectIsRegisteredValidator;
import br.com.zup.edu.nossalojavirtual.users.User;
import br.com.zup.edu.nossalojavirtual.users.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.BindException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Optional;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/purchase")
class PurchaseController {

    private final ProductRepository productRepository;
    private final PurchaseRepository purchaseRepository;

    private final UserRepository userRepository;

    PurchaseController(ProductRepository productRepository,
                       PurchaseRepository purchaseRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.purchaseRepository = purchaseRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> buy(@RequestBody @Valid NewPurchaseRequest newPurchase,
                                 @AuthenticationPrincipal Jwt jwt,
                                 UriComponentsBuilder uriBuilder) throws BindException {

        User buyer = userRepository.findByEmail(jwt.getClaim("email")).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "usuario não autenticao"));

        var product = productRepository.findById(newPurchase.getProductId()).get();

        Optional<Purchase> possiblePurchase = product.reserveQuantityFor(newPurchase, buyer);

        if (possiblePurchase.isEmpty()) {
            BindException bindException = new BindException(new Object(), "");
            bindException.reject("purchase.product.outOfStock", "This product is out of stock");

            throw bindException;
        }

        Purchase purchase = possiblePurchase.get();
        purchaseRepository.save(purchase);

        var redirectUrl = uriBuilder.path("/api/purchases/confirm-payment")
                .buildAndExpand(purchase.getId())
                .toString();

        String paymentUrl = purchase.paymentUrl(redirectUrl);


        var response = new HashMap<>();
        response.put("paymentUrl", paymentUrl);

        return ok(response);
    }

    @InitBinder(value = {"newPurchaseRequest"})
    void initBinder(WebDataBinder binder) {

        binder.addValidators(
                new ObjectIsRegisteredValidator<>("productId",
                        "product.id.dontExist",
                        NewPurchaseRequest.class,
                        productRepository::existsById));
    }
}
