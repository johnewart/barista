package net.johnewart.barista.core.runlist;

public class RunListEntry {
    private final static String ROLE_PATTERN = "role\\[(\\w+)\\]";
    private final static String RECIPE_PATTERN = "\\w+(::\\w+)?(@(\\d+\\.*){2,3})?";
    private final static String FQ_RECIPE_PATTERN = String.format("recipe\\[%s\\]", RECIPE_PATTERN);

    public RunListEntry() { }

    public static RunListEntry parse(String entry) {
        if(entry.matches(ROLE_PATTERN)) {
            return new RunListRole(entry);
        } else {
            if(entry.matches(RECIPE_PATTERN) || entry.matches(FQ_RECIPE_PATTERN)) {
                return new RunListRecipe(entry);
            } else {
                throw new IllegalArgumentException("Invalid run list entry: " + entry);
            }
        }
    }

}
