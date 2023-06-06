package example.cashcard;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CashCardRepository extends CrudRepository<CashCard, Long> {

    Optional<CashCard> findById(Long id);
}
