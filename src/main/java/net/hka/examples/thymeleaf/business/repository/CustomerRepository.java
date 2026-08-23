package net.hka.examples.thymeleaf.business.repository;

import net.hka.examples.thymeleaf.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
