package spaceinvaders;

/**
 *
 * @author Texhnolyze
 */
public class TextOutputInParts {

    private final String text;
    
    public TextOutputInParts(String text) {
        this.text = text;
    }
    
    private int charIdx = 0;
    
    public String getPart() {
        String part = text.substring(0, charIdx);
        return part;
    }
    
    public String getSrc() {
        return text;
    }
    
    public void next(boolean ignoreSpace) {
        charIdx = (charIdx + 1) % (text.length() + 1);
        if (ignoreSpace)
            while (charIdx != text.length() && text.charAt(charIdx) == ' ') 
                charIdx = (charIdx + 1) % (text.length() + 1);
    }
    
    public void reset() {
        charIdx = 0;
    }
    
}
