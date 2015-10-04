package uk.co.malbec.bingo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.co.malbec.bingo.model.*;
import uk.co.malbec.bingo.persistence.UsersRepository;

import java.util.*;

@Component
public class GameEngine {

    @Autowired
    private UsersRepository usersRepository;

    private Random randomGenerator = new Random();

    public void draw(Play play) {

        Game game = play.getGame();

        List<TicketWatcher> watchers = new ArrayList<>();
        for (Ticket ticket : play.getTickets()) {
            watchers.add(new TicketWatcher(ticket));
        }

        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= 90; i++) {
            numbers.add(i);
        }

        Collections.shuffle(numbers, randomGenerator);


        Map<String, Integer> winnings = new HashMap<>();
        GameScript gameScript = new GameScript();

        boolean endGame = false;
        while (!endGame && !numbers.isEmpty()) {
            Integer nextNumber = numbers.remove(0);
            Draw draw = new Draw(nextNumber);


            for (TicketWatcher watcher : watchers) {
                watcher.submitNumber(nextNumber);

                if (watcher.hasFourCorners()) {
                    if (!watcher.hasPrize(PrizeType.FOUR_CORNERS)) {
                        draw.add(new Prize(watcher.getTicket().getUsername(), PrizeType.FOUR_CORNERS));
                        assignWinnings(winnings, watcher.getTicket().getUsername(), play, PrizeType.FOUR_CORNERS);
                        watcher.addPrize(PrizeType.FOUR_CORNERS);
                    }
                }

                int lineCount = 0;
                if (watcher.hasFirstLine()) {
                    lineCount++;
                }

                if (watcher.hasSecondLine()) {
                    lineCount++;
                }

                if (watcher.hasThirdLine()) {
                    lineCount++;
                }

                if (lineCount == 1) {
                    if (!watcher.hasPrize(PrizeType.ONE_LINE)) {
                        draw.add(new Prize(watcher.getTicket().getUsername(), PrizeType.ONE_LINE));
                        assignWinnings(winnings, watcher.getTicket().getUsername(), play, PrizeType.ONE_LINE);
                        watcher.addPrize(PrizeType.ONE_LINE);
                    }
                }

                if (lineCount == 2) {
                    if (!watcher.hasPrize(PrizeType.TWO_LINES)) {
                        draw.add(new Prize(watcher.getTicket().getUsername(), PrizeType.TWO_LINES));
                        assignWinnings(winnings, watcher.getTicket().getUsername(), play, PrizeType.TWO_LINES);
                        watcher.addPrize(PrizeType.TWO_LINES);
                    }
                }

                if (lineCount == 3) {
                    draw.add(new Prize(watcher.getTicket().getUsername(), PrizeType.FULL_HOUSE));
                    assignWinnings(winnings, watcher.getTicket().getUsername(), play, PrizeType.FULL_HOUSE);
                    endGame = true;
                }

            }
            gameScript.add(draw);
        }
        play.setGameScript(gameScript);

        int seconds = gameScript.getDraws().size() * 3;
        play.setEndTime(play.getStartTime().plusSeconds(seconds));

        //assign winnings.
        for (Map.Entry<String, Integer> entry: winnings.entrySet()){
            usersRepository.get(entry.getKey()).addWinnings(new Winnings(entry.getValue(), play.getEndTime()));
        }

    }

    private void assignWinnings(Map<String, Integer> winnings, String username, Play play, PrizeType prizeType) {
        int prizeAmount = getPrizeAmount(prizeType, play);
        if (winnings.get(username) == null) {
            winnings.put(username, prizeAmount);
        } else {
            winnings.put(username,winnings.get(username) + prizeAmount);
        }
    }

    public int getPrizeAmount(PrizeType prizeType, Play play) {
        switch (prizeType) {
            case FOUR_CORNERS:
                return play.getFourCornersPrize();
            case ONE_LINE:
                return play.getOneLinePrize();
            case TWO_LINES:
                return play.getTwoLinesPrize();
            case FULL_HOUSE:
                return play.getFullHousePrize();
        }
        return 0;
    }


    public Ticket generateTicket(String username, int index) {

        Integer[] numbers = new Integer[27];

        List<Boolean> tupleOneInner = new ArrayList<>();
        tupleOneInner.add(true);
        tupleOneInner.add(true);
        tupleOneInner.add(true);
        tupleOneInner.add(false);
        tupleOneInner.add(false);
        tupleOneInner.add(false);
        tupleOneInner.add(false);
        Collections.shuffle(tupleOneInner, randomGenerator);
        List<Boolean> tupleOne = new ArrayList<>();
        tupleOne.add(true);
        tupleOne.addAll(tupleOneInner);
        tupleOne.add(true);

        List<Boolean> tupleTwo = new ArrayList<>();
        tupleTwo.add(true);
        tupleTwo.add(true);
        tupleTwo.add(true);
        tupleTwo.add(true);
        tupleTwo.add(true);
        tupleTwo.add(false);
        tupleTwo.add(false);
        tupleTwo.add(false);
        tupleTwo.add(false);
        Collections.shuffle(tupleTwo, randomGenerator);

        List<Boolean> tupleThreeInner = new ArrayList<>();
        tupleThreeInner.add(true);
        tupleThreeInner.add(true);
        tupleThreeInner.add(true);
        tupleThreeInner.add(false);
        tupleThreeInner.add(false);
        tupleThreeInner.add(false);
        tupleThreeInner.add(false);

        Collections.shuffle(tupleTwo, randomGenerator);
        List<Boolean> tupleThree = new ArrayList<>();
        tupleThree.add(true);
        tupleThree.addAll(tupleThreeInner);
        tupleThree.add(true);

        for (int i = 0; i < 9; i++) {
            Set<Integer> values = new TreeSet<>();

            if (tupleOne.get(i)) {
                Integer value = Math.abs(randomGenerator.nextInt()) % 10 + 1;
                while (!values.add(value)) {
                    value = Math.abs(randomGenerator.nextInt()) % 10 + 1;
                }
            }

            if (tupleTwo.get(i)) {
                Integer value = Math.abs(randomGenerator.nextInt()) % 10 + 1;
                while (!values.add(value)) {
                    value = Math.abs(randomGenerator.nextInt()) % 10 + 1;
                }
            }

            if (tupleThree.get(i)) {
                Integer value = Math.abs(randomGenerator.nextInt()) % 10 + 1;
                while (!values.add(value)) {
                    value = Math.abs(randomGenerator.nextInt()) % 10 + 1;
                }
            }


            Iterator<Integer> iterator = values.iterator();

            if (tupleOne.get(i)) {
                numbers[i * 3] = iterator.next() + (i * 10);
            }

            if (tupleTwo.get(i)) {
                numbers[i * 3 + 1] = iterator.next() + (i * 10);
            }

            if (tupleThree.get(i)) {
                numbers[i * 3 + 2] = iterator.next() + (i * 10);
            }
        }
        return new Ticket(UUID.randomUUID(), username, index, numbers);

    }


    public static class TicketWatcher {


        private Ticket ticket;

        private Set<Integer> first = new HashSet<>();
        private Set<Integer> second = new HashSet<>();
        private Set<Integer> third = new HashSet<>();
        private Set<Integer> fourCorners = new HashSet<>();


        private Set<PrizeType> prizesWon = new HashSet<>();


        public TicketWatcher(Ticket ticket) {
            this.ticket = ticket;

            for (int i = 0; i < 27; i++) {

                int remainder = i % 3;
                switch (remainder) {
                    case 0:
                        first.add(ticket.getNumbers()[i]);
                        break;
                    case 1:
                        second.add(ticket.getNumbers()[i]);
                        break;
                    case 2:
                        third.add(ticket.getNumbers()[i]);
                        break;
                }
            }

            first.remove(null);
            second.remove(null);
            third.remove(null);

            fourCorners.add(ticket.getNumbers()[0]);
            fourCorners.add(ticket.getNumbers()[2]);
            fourCorners.add(ticket.getNumbers()[24]);
            fourCorners.add(ticket.getNumbers()[26]);
        }

        public void submitNumber(Integer number) {
            first.remove(number);
            second.remove(number);
            third.remove(number);
            fourCorners.remove(number);
        }

        public boolean hasFirstLine() {
            return first.isEmpty();
        }

        public boolean hasSecondLine() {
            return second.isEmpty();
        }

        public boolean hasThirdLine() {
            return third.isEmpty();
        }

        public boolean hasFourCorners() {
            return fourCorners.isEmpty();
        }

        public boolean hasPrize(PrizeType prizeType) {
            return prizesWon.contains(prizeType);
        }

        public void addPrize(PrizeType prizeType) {
            prizesWon.add(prizeType);
        }


        public Ticket getTicket() {
            return ticket;
        }
    }
}
