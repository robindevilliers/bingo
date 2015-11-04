package uk.co.malbec.hound.reporter.machinery;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class DynamicCategoryGroup<CATEGORY, COLLECTOR> implements CategoryGroup<CATEGORY, COLLECTOR> {

    private Map<CATEGORY, COLLECTOR> categories = new HashMap<>();

    private COLLECTOR activeCollector;

    private Supplier<COLLECTOR> creator;

    public DynamicCategoryGroup(Supplier<COLLECTOR> creator){
        this.creator = creator;
    }

    @Override
    public Set<CATEGORY> getKeys() {
        return categories.keySet();
    }

    @Override
    public boolean apply(CATEGORY key) {
        activeCollector = categories.get(key);
        if (activeCollector == null){
            activeCollector = creator.get();
            categories.put(key, activeCollector);
        }

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
