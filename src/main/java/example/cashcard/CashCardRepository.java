package example.cashcard;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface CashCardRepository extends CrudRepository<CashCard, Long>,
        PagingAndSortingRepository<CashCard, Long> {

    Optional<CashCard> findByIdAndOwner(Long id, String owner); // returns a concrete card of a given owner

    Page<CashCard> findByOwner(String owner, PageRequest amount); // returns all cards (in a list) of a given owner
}
