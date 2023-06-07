package example.cashcard;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/cashcards")
public class CashCardController {

    private CashCardRepository cashCardRepository;

    public CashCardController(CashCardRepository cashCardRepository) {
        this.cashCardRepository = cashCardRepository;
    }

    @GetMapping("/{id}")
    private ResponseEntity<CashCard> findById(@PathVariable Long id, Principal principal) {
        String username = principal.getName();
        if(cashCardRepository.existsByIdAndOwner(id, username)) {
            CashCard fetchedCashCard = findCashCard(id, username);
            return ResponseEntity.ok(fetchedCashCard);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping()
    private ResponseEntity<CashCard> save(@RequestBody CashCard cashCard, Principal principal) {
        CashCard newCashCard = new CashCard(null, cashCard.amount(), principal.getName());
        CashCard savedCashCard = cashCardRepository.save(newCashCard);
        return ResponseEntity.created
                        (URI.create("/cashcards/%d".formatted(savedCashCard.id())))
                .body(savedCashCard);
    }

    @GetMapping()
    private ResponseEntity<List<CashCard>> findAll(Pageable pageable, Principal principal) {
        Page<CashCard> page = cashCardRepository.findByOwner(principal.getName(),
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(), // if not provided, default is 20
                        pageable.getSortOr(Sort.by(Sort.Direction.ASC, "amount"))
                )
        );
        return ResponseEntity.ok().body(page.getContent());
    }

    @PutMapping("/{id}")
    private ResponseEntity<CashCard> update(@PathVariable Long id, @RequestBody CashCard cashCardUpdate, Principal principal) {
        String username = principal.getName();
        if(!cashCardRepository.existsByIdAndOwner(id, username)) {
            return ResponseEntity.notFound().build();
        }
        CashCard fetchedCashCard = findCashCard(id, username);
        CashCard updatedCashCard = new CashCard(fetchedCashCard.id(), cashCardUpdate.amount(), principal.getName());
        cashCardRepository.save(updatedCashCard);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    private ResponseEntity<Void> delete(@PathVariable Long id, Principal principal) {
        String username = principal.getName();
        if(!cashCardRepository.existsByIdAndOwner(id, username)) {
            return ResponseEntity.notFound().build();
        }
        CashCard fetchedCashCard = findCashCard(id, username);
        cashCardRepository.delete(fetchedCashCard);
        return ResponseEntity.noContent().build();
    }

    private CashCard findCashCard(Long id, String username) {
        return cashCardRepository.findByIdAndOwner(id, username);
    }
}
