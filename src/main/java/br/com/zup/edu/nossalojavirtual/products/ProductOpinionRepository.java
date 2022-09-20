package br.com.zup.edu.nossalojavirtual.products;

import org.springframework.data.repository.Repository;

interface ProductOpinionRepository extends Repository<ProductOpinion, Long> {

    ProductOpinion save(ProductOpinion productOpinion);

    void deleteAll();
}
