package uk.co.malbec.bingo.persistence;


import org.joda.time.DateTime;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

import static org.joda.time.DateTime.now;

@Repository
public class LoginFailureCountRepository {

    private Map<String, List<DateTime>> failures = new HashMap<>();

    public synchronized int countFailures(String username) {
        List<DateTime> counts = failures.get(username);
        if (counts == null) {
            return 0;
        }

        DateTime cutoffTime = now().minusMinutes(10);
        counts.removeIf(dateTime -> dateTime.isBefore(cutoffTime));
        return counts.size();
    }

    public synchronized void addFailure(String username) {
        List<DateTime> counts = failures.get(username);
        if (counts == null) {
            counts = new ArrayList<>();
            failures.put(username, counts);
        }

        counts.add(now());
    }

    public synchronized void purge() {
        DateTime cutoffTime = now().minusMinutes(10);
        failures.values().stream().forEach(counts -> counts.removeIf(dateTime -> dateTime.isBefore(cutoffTime)));
        failures = failures.entrySet()
                .stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}
