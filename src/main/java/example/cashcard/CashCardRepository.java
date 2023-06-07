package example.cashcard;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface CashCardRepository extends CrudRepository<CashCard, Long>,
        PagingAndSortingRepository<CashCard, Long> {

    CashCard findByIdAndOwner(Long id, String owner); // returns a concrete card of a given owner

    boolean existsByIdAndOwner(Long id, String owner);

    Page<CashCard> findByOwner(String owner, PageRequest amount); // returns all cards (in a list) of a given owner
}
