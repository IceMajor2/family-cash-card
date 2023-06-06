package example.cashcard;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
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
    private ResponseEntity save(@RequestBody CashCard cashCard) {
        CashCard savedCashCard = cashCardRepository.save(cashCard);
        return ResponseEntity.created
                (URI.create("/cashcards/%d".formatted(savedCashCard.id())))
                .body(savedCashCard);
    }

    @GetMapping()
    private ResponseEntity findAll() {
        var cashCards = cashCardRepository.findAll();
        return ResponseEntity.ok().body(cashCards);
    }

    // alternative approach:
    /**
     * Spring academy
    @PostMapping
    private ResponseEntity<Void> add(@RequestBody CashCard newCashCardRequest, UriComponentsBuilder ucb) {
        CashCard savedCashCard = cashCardRepository.save(newCashCardRequest);
        URI locationOfNewCashCard = ucb
                .path("cashcards/{id}")
                .buildAndExpand(savedCashCard.id())
                .toUri();
        return ResponseEntity.created(locationOfNewCashCard).build();
    }
     */
}
