package br.com.zup.edu.nossalojavirtual.purchase;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@FeignClient(url = "localhost:8080/sellerRanking", name = "sellerRankingSystem")
interface SellersRankingClient {

    @RequestMapping(method = POST, value = "/newPurchase/", produces = "application/json")
    void requestInvoice(SellersRankingRequest request);

    class SellersRankingRequest {

        private Long purchaseId;
        private String sellerId;

        /**
         * @deprecated frameworks eyes only
         */
        @Deprecated
        SellersRankingRequest() {
        }

        public SellersRankingRequest(Long purchaseId, String sellerId) {
            this.purchaseId = purchaseId;
            this.sellerId = sellerId;
        }

        public Long getPurchaseId() {
            return purchaseId;
        }

        public String getSellerId() {
            return sellerId;
        }
    }
}
