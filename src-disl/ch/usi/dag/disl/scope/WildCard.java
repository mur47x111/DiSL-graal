package ch.usi.dag.disl.scope;

public class WildCard {

    // thx: http://www.adarshr.com/papers/wildcard/
    // modified version

    /**
     * Performs a wildcard matching for the text and pattern
     * provided.
     *
     * @param text the text to be tested for matches.
     *
     * @param pattern the pattern to be matched for.
     * This can contain the wildcard character '*' (asterisk).
     *
     * @return <tt>true</tt> if a match is found, <tt>false</tt>
     * otherwise.
     */

    public static final String WILDCARD_STR = "*";
    private static final String WILDCARD_PATTERN = "\\*";

    public static boolean match(String text, String pattern) {

        // special cases

        if(pattern.equals(WILDCARD_STR)) {
            return true;
        }

        if(pattern.isEmpty()) {
            return text.isEmpty ();
        }

        // Create the cards by splitting using a RegEx. If more speed
        // is desired, a simpler character based splitting can be done.
        String [] cards = pattern.split(WILDCARD_PATTERN);

        if(! pattern.startsWith(WILDCARD_STR)) {

            // first card should be at the beginning
            int firstIdx = text.indexOf(cards[0]);
            if(firstIdx != 0) {
                return false;
            }
        }

        if(! pattern.endsWith(WILDCARD_STR)) {

            // last card should be at the end
            String lastCard = cards[cards.length - 1];
            int lastIdx = text.lastIndexOf(lastCard);
            if(lastIdx != text.length() - lastCard.length()) {
                return false;
            }
        }

        // Iterate over the cards.
        for (String card : cards)
        {
            int idx = text.indexOf(card);

            // Card not detected in the text.
            if(idx == -1) {
                return false;
            }

            // Move ahead, towards the right of the text.
            text = text.substring(idx + card.length());
        }

        return true;
    }

}