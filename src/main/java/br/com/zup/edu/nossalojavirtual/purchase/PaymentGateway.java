package br.com.zup.edu.nossalojavirtual.purchase;


import br.com.zup.edu.nossalojavirtual.purchase.Payment.PaymentStatus;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotNull;

import static org.springframework.util.Assert.notNull;

enum PaymentGateway {

    PAYPAL {
        @Override
        String paymentUrl(@NotNull Purchase purchase, @URL String redirectUrl) {
            notNull(purchase, "purchase must not be null");
            return String.format("paypal.com/%s?redirectUrl=%s", purchase.getId(), redirectUrl);
        }

        @Override
        public PaymentStatus status(PaymentReturn payment) {
            return payment.payPalStatus();
        }
    },
    PAGSEGURO {
        @Override
        String paymentUrl(@NotNull Purchase purchase, @URL String redirectUrl) {
            notNull(purchase, "purchase must not be null");

            return String.format("pagseguro.com?returnId=%s&redirectUrl=%s", purchase.getId(), redirectUrl);
        }

        @Override
        public PaymentStatus status(PaymentReturn payment) {
            return payment.pagSeguroStatus();
        }
    };

    abstract String paymentUrl(@NotNull Purchase purchase, @URL String redirectUrl);

    public abstract PaymentStatus status(PaymentReturn payment);
}
