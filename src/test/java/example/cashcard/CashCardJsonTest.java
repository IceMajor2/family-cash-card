package example.cashcard;


import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class CashCardJsonTest {

    @Autowired
    // class for testing object-mapping to JSON. Here the object are of CashCard class
    private JacksonTester<CashCard> json;

    // and here it's an array of CashCards
    @Autowired
    private JacksonTester<CashCard[]> jsonList;

    private CashCard[] cashCards;

    // before each test an array of cashcards will be initialized de novo
    @BeforeEach
    void setUp() {
        cashCards = Arrays.array(
                new CashCard(99L, 123.45, "sarah1"),
                new CashCard(100L, 1.00, "sarah1"),
                new CashCard(101L, 150.00, "sarah1"),
                new CashCard(1L, 500.00, "mike"));
    }

    @Test
    public void cashCardSerializationTest() throws IOException {
        CashCard cashCard = cashCards[0];
        // map cashCard to Json and assert it is equal to the
        // response body contained in 'single.json' file
        assertThat(json.write(cashCard)).isStrictlyEqualToJson("single.json");
        assertThat(json.write(cashCard)).hasJsonPathNumberValue("@.id");
        assertThat(json.write(cashCard)).extractingJsonPathNumberValue("@.id")
                .isEqualTo(99);
        assertThat(json.write(cashCard)).hasJsonPathNumberValue("@.amount");
        assertThat(json.write(cashCard)).extractingJsonPathNumberValue("@.amount")
                .isEqualTo(123.45);
    }

    @Test
    public void cashCardDeserializationTest() throws IOException {
        String expected = """
                {
                    "id": 99,
                    "amount": 123.45,
                    "owner": "sarah1"
                }
                """;
        // check if the above json body (that matches CashCard records)
        // is equal to the below object after mapping
        assertThat(json.parse(expected))
                .isEqualTo(new CashCard(99L, 123.45, "sarah1"));
        assertThat(json.parseObject(expected).id()).isEqualTo(99);
        assertThat(json.parseObject(expected).amount()).isEqualTo(123.45);
    }

    @Test
    public void cashCardListSerializationTest() throws IOException {
        assertThat(jsonList.write(cashCards)).isStrictlyEqualToJson("list.json");
    }

    @Test
    public void cashCardListDeserializationTest() throws IOException {
        String expected = """
                [
                    {
                        "id": 99,
                        "amount": 123.45,
                        "owner": "sarah1"
                    },
                    {
                        "id": 100,
                        "amount": 1.00,
                        "owner": "sarah1"
                    },
                    {
                        "id": 101,
                        "amount": 150.00,
                        "owner": "sarah1"
                    },
                    {
                        "id": 1,
                        "amount": 500.00,
                        "owner": "mike"
                    }
                ]
                """;
        assertThat(jsonList.parse(expected))
                .isEqualTo(cashCards);
    }
}
