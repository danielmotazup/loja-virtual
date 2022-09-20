package br.com.zup.edu.nossalojavirtual.purchase;

import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

interface PurchaseRepository extends Repository<Purchase, Long> {

    Purchase save(Purchase purchase);

    Optional<Purchase> findById(Long id);

    boolean existsById(Long id);

    void deleteAll();

    List<Purchase> findAll();
}
