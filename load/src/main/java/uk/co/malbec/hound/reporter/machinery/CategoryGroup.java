package uk.co.malbec.hound.reporter.machinery;

import java.util.Set;

public interface CategoryGroup<CATEGORY, COLLECTOR> extends Referenceable<COLLECTOR> {

    Set<CATEGORY> getKeys();

    public boolean apply(CATEGORY key);

    public COLLECTOR get(CATEGORY key);
}
