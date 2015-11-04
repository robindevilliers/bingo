package uk.co.malbec.hound.reporter.machinery;

public interface Referenceable<COLLECTOR> {
    COLLECTOR current();
}
