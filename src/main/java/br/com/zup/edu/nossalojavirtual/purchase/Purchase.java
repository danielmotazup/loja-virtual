package br.com.zup.edu.nossalojavirtual.purchase;

import br.com.zup.edu.nossalojavirtual.products.Product;
import br.com.zup.edu.nossalojavirtual.purchase.Payment.PaymentStatus;
import br.com.zup.edu.nossalojavirtual.users.User;
import org.hibernate.validator.constraints.URL;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.IDENTITY;
import static org.springframework.util.Assert.notNull;

@Table(name = "purchase")
@Entity
public
class Purchase {

    @GeneratedValue(strategy = IDENTITY)
    @Id
    @Column(name = "purchase_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "buyer_id")
    private User buyer;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Min(1)
    @Column(name = "purchase_quantity")
    private int quantity;

    @Enumerated(value = STRING)
    @Column(name = "purchase_payment_gateway")
    private PaymentGateway paymentGateway;

    @Enumerated(value = STRING)
    @Column(name = "purchase_status")
    private Status status;

    @Column(name = "purchase_total")
    private BigDecimal total;

    @ElementCollection
    @CollectionTable(name = "purchase_payment_attempts",
            joinColumns = @JoinColumn(name = "purchase_id"))
    private Set<Payment> paymentAttempts = new HashSet<>();


    /**
     * @deprecated hibernate eyes only
     */
    @Deprecated
    private Purchase() {
    }

    Purchase(@NotNull User buyer,
             @NotNull Product product,
             @Min(1) int quantity,
             @NotNull PaymentGateway paymentGateway) {

        notNull(buyer, "user must not be null");
        notNull(product, "product must not be null");
        if (quantity < 1) {
            throw new IllegalArgumentException("quantity must not be less than 0");
        }
        notNull(paymentGateway, "paymentGateway must not be null");

        this.buyer = buyer;
        this.product = product;
        this.quantity = quantity;
        this.paymentGateway = paymentGateway;
        this.status = Status.INICIADA;
        this.total = product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    public Long getId() {
        return id;
    }

    public int getQuantity() {
        return quantity;
    }

    public String paymentUrl(@URL String redirectUrl) {
        return paymentGateway.paymentUrl(this, redirectUrl);
    }

    public PostPaymentProcessedPurchase process(PaymentReturn paymentReturn) {
        if (isPaymentSuccessful()) {
            throw new IllegalStateException("A finished Purchase cannot be paid again");
        }

        PaymentStatus paymentStatus = paymentGateway.status(paymentReturn);
        paymentAttempts.add(new Payment(paymentReturn.getPaymentId(), paymentStatus));

        return new PostPaymentProcessedPurchase(this);
    }

    public boolean isPaymentSuccessful() {
        return paymentAttempts.stream().anyMatch(Payment::isSuccessful);
    }

    public String buyerEmail() {
        return buyer.getUsername();
    }

    public String sellerEmail() {
        return product.sellerEmail();
    }

    public Product getProduct() {
        return product;
    }

    public LocalDateTime paymentConfirmedTime() {
        if (!isPaymentSuccessful()) {
            throw new IllegalStateException("An unfinished Purchase does not have a payment confirmation timestamp");
        }

        Payment successPayment = paymentAttempts.stream()
                .filter(Payment::isSuccessful)
                .findFirst()
                .get();

        return successPayment.getReturnedAt();
    }
}
