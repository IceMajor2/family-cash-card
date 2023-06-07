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
import java.util.Optional;

@RestController
@RequestMapping("/cashcards")
public class CashCardController {

    private CashCardRepository cashCardRepository;

    public CashCardController(CashCardRepository cashCardRepository) {
        this.cashCardRepository = cashCardRepository;
    }

    @GetMapping("/{id}")
    private ResponseEntity<CashCard> findById(@PathVariable Long id, Principal principal) {
        Optional<CashCard> optCashCard = cashCardRepository.findByIdAndOwner(id, principal.getName());
        if (optCashCard.isPresent()) {
            return ResponseEntity.ok(optCashCard.get());
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
        /* alternatively:
        with parameter in method: 'UriComponentsBuilder ucb'
        URI locationOfNewCashCard = ucb
                .path("cashcards/{id}")
                .buildAndExpand(savedCashCard.id())
                .toUri();
         */
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
    private ResponseEntity<CashCard> update(@PathVariable Long id, CashCard cashCard, Principal principal) {
//        if(cashCardRepository.findByIdAndOwner(id, principal.getName()).isEmpty()) {
//
//        }
        return ResponseEntity.noContent().build();
    }
}
