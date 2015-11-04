package uk.co.malbec.hound.reporter.machinery;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class LiteralCategoryGroup<CATEGORY, COLLECTOR> implements CategoryGroup<CATEGORY, COLLECTOR> {

    private Map<CATEGORY, COLLECTOR> categories = new HashMap<>();

    private COLLECTOR activeCollector;

    public LiteralCategoryGroup(Supplier<COLLECTOR> creator, CATEGORY... breakpoints){
        for (CATEGORY breakpoint : breakpoints){
            categories.put(breakpoint, creator.get());
        }
    }

    @Override
    public Set<CATEGORY> getKeys() {
        return categories.keySet();
    }

    @Override
    public boolean apply(CATEGORY key) {
        activeCollector = categories.get(key);
        return activeCollector != null;
    }

    @Override
    public COLLECTOR current() {
        return activeCollector;
    }

    public COLLECTOR get(CATEGORY key){
        return categories.get(key);
    }
}
