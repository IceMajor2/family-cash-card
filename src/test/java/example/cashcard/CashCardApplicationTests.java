package example.cashcard;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)  // makes each test start with a clean slate
                                                                                // but should be used for good reason
                                                                                // so rather assign this annotation to a method
class CashCardApplicationTests {

    // @Autowired should be used mainly in tests, not in source code
    @Autowired
    // rest template helper for tests to make HTTP requests
    TestRestTemplate restTemplate;

    @Test
    public void shouldReturnCashCardWhenDataIsSaved() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("sarah1", "")
                .getForEntity("/cashcards/99", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        Number id = documentContext.read("$.id");
        assertThat(id).isEqualTo(99);

        Double amount = documentContext.read("$.amount");
        assertThat(amount).isNotNull();
    }

    @Test
    public void shouldNotReturnCashCardWithAnUnknownId() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("sarah1", "")
                .getForEntity("/cashcards/1000", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isBlank();
    }

    @Test
    @DirtiesContext // here is a post method: good place to put this annotation
    public void shouldAddCashCardToDatabase() {
        CashCard newCashCard = new CashCard(null, 535.00, "sarah1");
        ResponseEntity postResponse = restTemplate
                .withBasicAuth("sarah1", "")
                .postForEntity("/cashcards", newCashCard, Void.class);
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // check if database was updated
        URI location = postResponse.getHeaders().getLocation();
        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("sarah1", "")
                .getForEntity(location, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
        Number id = documentContext.read("$.id");
        Double amount = documentContext.read("$.amount");

        assertThat(id).isNotNull();
        assertThat(amount).isEqualTo(535d);
    }

    @Test
    public void shouldReturnAllCashCardsWhenListIsRequested() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("sarah1", "")
                .getForEntity("/cashcards", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        int cashCardCount = documentContext.read("$.length()");
        assertThat(cashCardCount).isEqualTo(3);

        JSONArray ids = documentContext.read("$..id");
        assertThat(ids).containsExactlyInAnyOrder(99, 100, 101);

        JSONArray amounts = documentContext.read("$..amount");
        assertThat(amounts).containsExactlyInAnyOrder(123.45, 1.00, 150.00);
    }

    @Test
    public void shouldReturnOneCashCardPerPage() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("sarah1", "")
                .getForEntity("/cashcards?page=0&size=1", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        JSONArray page = documentContext.read("$[*]");
        assertThat(page.size()).isEqualTo(1);
        // alternatively:
        // int cardsOnPage = documentContext.read("$.length()");
        // assertThat(cardsOnPage).isEqualTo(1);
    }

    @Test
    public void shouldReturnSingleEntityPageSortedByAmountDesc() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("sarah1", "")
                .getForEntity("/cashcards?page=0&size=1&sort=amount,desc", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        JSONArray page = documentContext.read("$[*]");
        assertThat(page.size()).isEqualTo(1);

        Number id = documentContext.read("$[0].id");
        double amount = documentContext.read("$[0].amount");
        assertThat(id).isEqualTo(101);
        assertThat(amount).isEqualTo(150.00);
    }

    @Test
    public void shouldReturnSortedPageWithoutParametersByDefault() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("sarah1", "")
                .getForEntity("/cashcards", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        JSONArray page = documentContext.read("$[*]");
        // 3 is more than a reasonable number of CashCard
        // objects to be displayed at one page
        assertThat(page.size()).isEqualTo(3);

        JSONArray amounts = documentContext.read("$..amount");
        assertThat(amounts).containsExactly(1.00, 123.45, 150.00);

        JSONArray ids = documentContext.read("$..id");
        assertThat(ids).containsExactly(100, 99, 101);
    }

    @Test
    public void shouldReturnUnauthorizedOnAnyRequest() {
        ResponseEntity<String> getResponse = restTemplate.getForEntity("/cashcards", String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        CashCard newCashCard = new CashCard(null, 100d, "ja");
        ResponseEntity<Void> postResponse = restTemplate.postForEntity("/cashcards", newCashCard, Void.class);
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void nonOwnersShouldBeForbiddenFromGettingCards() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("hank", "")
                .getForEntity("/cashcards", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void nonOwnersShouldBeForbiddenFromGettingSomeonesCards() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("hank", "")
                .getForEntity("/cashcards/99", String.class);
        // ^ user 'hank' tries to get 'sarah1' card
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void differentOwnerShouldNotGetOthersCards() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("mike", "")
                .getForEntity("/cashcards/99", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
