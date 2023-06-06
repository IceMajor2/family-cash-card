package example.cashcard;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
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
    private ResponseEntity<CashCard> findById(@PathVariable Long id) {
        Optional<CashCard> optCashCard = cashCardRepository.findById(id);
        if (optCashCard.isPresent()) {
            return ResponseEntity.ok(optCashCard.get());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    private ResponseEntity<CashCard> save(@RequestBody CashCard cashCard) {
        CashCard savedCashCard = cashCardRepository.save(cashCard);
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
    private ResponseEntity<List<CashCard>> findAll(Pageable pageable) {
        Page<CashCard> page = cashCardRepository.findAll(
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.ASC, "amount"))
                )
        );
        return ResponseEntity.ok().body(page.getContent());
    }
}
