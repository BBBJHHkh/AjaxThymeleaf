package net.hka.examples.thymeleaf.business.repository;

import net.hka.examples.thymeleaf.domain.ComputerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComputerOrderRepository extends JpaRepository<ComputerOrder, Long> {
}
