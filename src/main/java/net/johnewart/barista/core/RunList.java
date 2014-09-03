package net.johnewart.barista.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;

public class RunList extends LinkedList<String> {
    private final static String ROLE_PATTERN = "role\\[(\\w+)\\]";
    private final static String RECIPE_PATTERN = "\\w+(::\\w+)?(@(\\d+\\.*){2,3})?";
    private final static String FQ_RECIPE_PATTERN = String.format("recipe\\[%s\\]", RECIPE_PATTERN);
    private static final Logger LOG = LoggerFactory.getLogger(RunList.class);

    public RunList(RunList other) {
        super();
        this.add(other);
    }

    public RunList() {
        super();
    }

    @Override
    public boolean add(String element) {
        LOG.debug("Calling custom add: " + element);
        if (element.matches(RECIPE_PATTERN)) {
            element = ("recipe[" + element + "]");
        }

        if ( !(element.matches(ROLE_PATTERN) || element.matches(FQ_RECIPE_PATTERN)) ) {
            LOG.error("Invalid entry: " + element);
            throw new IllegalArgumentException("Invalid run list entry: " + element);
        }

        if(!this.contains(element)) {
            return super.add(element);
        } else {
            return false;
        }
    }

    public void add(RunList other) {
        for(String element : other) {
            this.add(element);
        }
    }

}
