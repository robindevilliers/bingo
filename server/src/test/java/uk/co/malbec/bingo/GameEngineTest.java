package uk.co.malbec.bingo;

import org.joda.time.DateTime;
import org.junit.Test;
import uk.co.malbec.bingo.model.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

public class GameEngineTest {

    @Test
    public void testGenerateTicket() throws Exception {

        for (int c = 0; c < 1000000; c++) {
            Ticket ticket = new GameEngine().generateTicket("robin", 0);

            assertEquals(ticket.getUsername(), "robin");
            assertEquals(ticket.getIndex(), 0);
            Integer[] numbers = ticket.getNumbers();

            Integer lastNumber;
            Integer count = 0;

            lastNumber = numbers[0];
            count++;

            for (int i = 1; i < 27; i++) {
                if (numbers[i] != null) {
                    assertTrue(lastNumber < numbers[i]);
                    lastNumber = numbers[i];
                    count++;

                }
            }
            assertNotNull(numbers[0]);
            assertNotNull(numbers[2]);
            assertNotNull(numbers[24]);
            assertNotNull(numbers[26]);

            assertEquals(count.intValue(), 15);
        }
    }


    @Test
    public void testDraw() {

        Map<PrizeType, Integer> counts = new HashMap<>();
        counts.put(PrizeType.FOUR_CORNERS, 0);
        counts.put(PrizeType.ONE_LINE, 0);
        counts.put(PrizeType.TWO_LINES, 0);
        counts.put(PrizeType.FULL_HOUSE, 0);
        int drawCount = 0;

        for (int c = 0; c < 500; c++) {


            Play play = new Play(UUID.randomUUID(), new Game(UUID.randomUUID(), "title", 50, 50, 50), DateTime.now());

            for (int i = 0; i < 100; i++) {
                Ticket ticket = new GameEngine().generateTicket("robin" + i, 0);
                play.addTicket(ticket.getKey(), ticket);
            }

            new GameEngine().draw(play);

            GameScript gameScript = play.getGameScript();
            //System.out.println("----------------------------------------------");
            for (Draw draw : gameScript.getDraws()) {
                drawCount++;
                //System.out.println("Number: " + draw.getNumber());
                for (Prize prize : draw.getPrizes()) {
                  //  System.out.println("\tPrize: " + prize.getUsername() + " " + prize.getPrizeType());
                    counts.put(prize.getPrizeType(), counts.get(prize.getPrizeType()) + 1);
                }
            }


        }


        System.out.println(PrizeType.FOUR_CORNERS + " " + counts.get(PrizeType.FOUR_CORNERS)/500);
        System.out.println(PrizeType.ONE_LINE + " " + counts.get(PrizeType.ONE_LINE)/500);
        System.out.println(PrizeType.TWO_LINES + " " + counts.get(PrizeType.TWO_LINES)/500);
        System.out.println(PrizeType.FULL_HOUSE + " " + counts.get(PrizeType.FULL_HOUSE)/500);
        System.out.println("draw count " + (drawCount/500));

    }
}