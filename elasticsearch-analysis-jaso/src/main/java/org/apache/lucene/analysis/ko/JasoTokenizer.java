package org.apache.lucene.analysis.ko;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.ko.morph.MorphException;
import org.apache.lucene.analysis.ko.utils.SyllableUtil;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.analysis.util.CharacterUtils;
import org.apache.lucene.util.AttributeFactory;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JasoTokenizer extends Tokenizer {
    private ESLogger LOG = Loggers.getLogger(JasoTokenizer.class);

    // {'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'}
    private static final char[] CHOSUNG =
            {0x3131, 0x3132, 0x3134, 0x3137, 0x3138, 0x3139, 0x3141, 0x3142, 0x3143, 0x3145, 0x3146, 0x3147, 0x3148, 0x3149, 0x314a, 0x314b, 0x314c, 0x314d, 0x314e};

    // {'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ', 'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ'}
    private static final char[] JUNGSUNG =
            {0x314f, 0x3150, 0x3151, 0x3152, 0x3153, 0x3154, 0x3155, 0x3156, 0x3157, 0x3158, 0x3159, 0x315a, 0x315b, 0x315c, 0x315d, 0x315e, 0x315f, 0x3160, 0x3161, 0x3162, 0x3163};

    // {' ', 'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ', 'ㄵ', 'ㄶ', 'ㄷ', 'ㄹ', 'ㄺ', 'ㄻ', 'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ', 'ㅀ', 'ㅁ', 'ㅂ', 'ㅄ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'}
    private static final char[] JONGSUNG =
            {0x0000, 0x3131, 0x3132, 0x3133, 0x3134, 0x3135, 0x3136, 0x3137, 0x3139, 0x313a, 0x313b, 0x313c, 0x313d, 0x313e, 0x313f, 0x3140, 0x3141, 0x3142, 0x3144, 0x3145, 0x3146, 0x3147, 0x3148, 0x314a, 0x314b, 0x314c, 0x314d, 0x314e};

    private static final String[] CHOSUNG_EN = { "r", "R", "s", "e", "E", "f", "a", "q", "Q", "t", "T", "d", "w", "W", "c", "z", "x", "v", "g" };

    private static final String[] JUNGSUNG_EN = { "k", "o", "i", "O", "j", "p", "u", "P", "h", "hk", "ho", "hl", "y", "n", "nj", "np", "nl", "b", "m", "ml", "l" };

    private static String[] JONGSUNG_EN = { "", "r", "R", "rt", "s", "sw", "sg", "e", "f", "fr", "fa", "fq", "ft", "fx", "fv", "fg", "a", "q", "qt", "t", "T", "d", "w", "c", "z", "x", "v", "g" };

    private static String[] LETTER_EN = { "r", "R", "rt", "s", "sw", "sg", "e","E" ,"f", "fr", "fa", "fq", "ft", "fx", "fv", "fg", "a", "q","Q", "qt", "t", "T", "d", "w", "W", "c", "z", "x", "v", "g" };

    private static final char CHOSUNG_BEGIN_UNICODE = 12593;
    private static final char CHOSUNG_END_UNICODE = 12622;
    private static final char HANGUEL_BEGIN_UNICODE = 44032;
    private static final char HANGUEL_END_UNICODE = 55203;
    private static final char NUMBER_BEGIN_UNICODE = 48;
    private static final char NUMBER_END_UNICODE = 57;
    private static final char ENGLISH_LOWER_BEGIN_UNICODE = 65;
    private static final char ENGLISH_LOWER_END_UNICODE = 90;
    private static final char ENGLISH_UPPER_BEGIN_UNICODE = 97;
    private static final char ENGLISH_UPPER_END_UNICODE = 122;

    private static boolean isPossibleCharacter(char c){
        if ((   (c >= NUMBER_BEGIN_UNICODE && c <= NUMBER_END_UNICODE)
                || (c >= ENGLISH_UPPER_BEGIN_UNICODE && c <= ENGLISH_UPPER_END_UNICODE)
                || (c >= ENGLISH_LOWER_BEGIN_UNICODE && c <= ENGLISH_LOWER_END_UNICODE)
                || (c >= HANGUEL_BEGIN_UNICODE && c <= HANGUEL_END_UNICODE)
                || (c >= CHOSUNG_BEGIN_UNICODE && c <= CHOSUNG_END_UNICODE))
                ){
            return true;
        }else{
            return false;
        }
    }

    private int offset = 0;
    private int bufferIndex = 0;
    private int dataLen = 0;
    private int finalOffset = 0;
    private static final int MAX_WORD_LEN = 255;
    private static final int IO_BUFFER_SIZE = 4096;

    private final CharacterUtils charUtils = CharacterUtils.getInstance();
    private final CharacterUtils.CharacterBuffer ioBuffer = CharacterUtils.newCharacterBuffer(IO_BUFFER_SIZE);

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);

    private static Map<Integer,Integer> pairmap = new HashMap<Integer,Integer>();
    private List<Integer> pairstack = new ArrayList<Integer>();

    private volatile static JasoTokenizer jasoTokenizer;

    private JasoTokenizer() {
    }

    public JasoTokenizer(AttributeFactory factory) {
        super(factory);
    }

    public static JasoTokenizer getInstance() {
        if ( jasoTokenizer == null ) {
            synchronized ( JasoTokenizer.class ) {
                if ( jasoTokenizer == null ) {
                    jasoTokenizer = new JasoTokenizer();
                }
            }
        }
        return jasoTokenizer;
    }

    public String tokenizer(String source, JasoType jasoType) {
        String jaso = "";

        switch ( jasoType ) {
            case CHOSUNG:
                jaso = chosungTokenizer(source);
                break;
            case JUNGSUNG:
                jaso = jungsungTokenizer(source);
                break;
            case JONGSUNG:
                jaso = jongsungTokenizer(source);
                break;
            default:
                jaso = chosungTokenizer(source);
        }

        return jaso;
    }

    public String jasoTokenizer(String source) {
        String str = "";
        int criteria;
        char sourceChar;
        char choIdx;
        char jungIdx;
        char jongIdx;

        for(int i = 0 ; i < source.length(); i++) {
            sourceChar = source.charAt(i);

            if(sourceChar >= 0xAC00 && sourceChar <= 0xD7AF) {
                criteria = (sourceChar - 0xAC00);
                choIdx = (char)(criteria/28/21);
                jungIdx = (char)(criteria%(21*28)/28);
                jongIdx = (char)(criteria%28);

                str = str + CHOSUNG[choIdx];
                str = str + JUNGSUNG[jungIdx];
                if(jongIdx>0) {
                    str = str + JONGSUNG[jongIdx];
                }
            } else {
                if ( isPossibleCharacter(sourceChar) ) {
                    str = str + sourceChar;
                }
            }
        }
        return str;
    }

    public String chosungTokenizer(String source) {
        String chosung = "";
        int criteria;
        char sourceChar;
        char choIdx;

        for(int i = 0 ; i < source.length(); i++) {
            sourceChar = source.charAt(i);

            if(sourceChar >= 0xAC00 && sourceChar <= 0xD7AF) {
                criteria = (sourceChar - 0xAC00);
                choIdx = (char)(((criteria - (criteria%28))/28)/21);

                chosung = chosung + CHOSUNG[choIdx];
            } else {
                if ( isPossibleCharacter(sourceChar) ) {
                    chosung = chosung + sourceChar;
                }
            }
        }

        return chosung;
    }

    public String jungsungTokenizer(String source) {
        String jungsung = "";
        int criteria;
        char sourceChar;
        char jungIdx;

        for(int i = 0 ; i < source.length(); i++) {
            sourceChar = source.charAt(i);

            if(sourceChar >= 0xAC00 && sourceChar <= 0xD7AF) {
                criteria = (sourceChar - 0xAC00);
                jungIdx = (char)(((criteria - (criteria%28))/28)%21);

                jungsung = jungsung + JUNGSUNG[jungIdx];
            } else {
                if ( isPossibleCharacter(sourceChar) ) {
                    jungsung = jungsung + sourceChar;
                }
            }
        }

        return jungsung;
    }

    public String jongsungTokenizer(String source) {
        String jongsung = "";
        char sourceChar;
        char jongIdx;

        for(int i = 0 ; i < source.length(); i++) {
            sourceChar = source.charAt(i);

            if(sourceChar >= 0xAC00 && sourceChar <= 0xD7AF) {
                jongIdx = (char)((sourceChar - 0xAC00)%28);

                jongsung = jongsung + JONGSUNG[jongIdx];
            } else {
                if (isPossibleCharacter(sourceChar) ) {
                    jongsung = jongsung + sourceChar;
                }
            }
        }

        return jongsung;
    }

    protected int normalize(int c) {
        return c;
    }

    @Override
    public final boolean incrementToken() throws IOException {

        clearAttributes();
        char[] buffer = termAtt.buffer();

        int length = 0;
        int start = -1; // this variable is always initialized
        int end = -1;
        int pos = posIncrAtt.getPositionIncrement();

        while (true) {
            if (bufferIndex >= dataLen) {
                offset += dataLen;
                charUtils.fill(ioBuffer, input); // read supplementary char aware with CharacterUtils
                if (ioBuffer.getLength() == 0) {
                    dataLen = 0; // so next offset += dataLen won't decrement offset
                    if (length > 0) {
                        break;
                    } else {
                        finalOffset = correctOffset(offset);
                        return false;
                    }
                }
                dataLen = ioBuffer.getLength();
                bufferIndex = 0;
            }

            // use CharacterUtils here to support < 3.1 UTF-16 code unit behavior if the char based methods are gone
            final int c = charUtils.codePointAt(ioBuffer.getBuffer(), bufferIndex, ioBuffer.getLength());
            final int charCount = Character.charCount(c);
            bufferIndex += charCount;

            char inspect_c = (char)c;

            int closechar = getPairChar(c);

            if(closechar!=0 &&
                    (pairstack.isEmpty() ||
                            (!pairstack.isEmpty() && pairstack.get(0)!=c))) {
                if(start==-1) {
                    start=offset + bufferIndex - charCount;
                    end=start;
                }
                end += charCount;
                length += Character.toChars(c, buffer, length); // buffer it
                pairstack.add(0,closechar);

                break;
            } else if (isTokenChar(c) ||
                    (pairstack.size()>0 && pairstack.get(0)==c)) {               // if it's a token char
                if (length == 0) {                // start of token
                    assert start == -1;
                    start = offset + bufferIndex - charCount;
                    end = start;
                } else if (length >= buffer.length - 1) { // check if a supplementary could run out of bounds
                    buffer = termAtt.resizeBuffer(2 + length); // make sure a supplementary fits in the buffer
                }
                end += charCount;
                length += Character.toChars(c, buffer, length); // buffer it

                // delimited close character


//                // check if next token is parenthesis.
                if(isDelimitPosition(length, c)) {
                    if(!pairstack.isEmpty() && pairstack.get(0)==c) {
                        pairstack.remove(0);
                    }
                    break;
                }

                if(!pairstack.isEmpty() && pairstack.get(0)==c) {
                    pairstack.remove(0);
                }

                if (length >= MAX_WORD_LEN)
                    break; // buffer overflow! make sure to check for >= surrogate pair could break == test
            } else if (length > 0) {           // at non-Letter w/ chars
                break;
            }// return 'em

        }

        String type = TokenUtilities.getType(buffer, length);

        termAtt.setLength(length);
        assert start != -1;
        offsetAtt.setOffset(correctOffset(start), finalOffset = correctOffset(end));
        typeAtt.setType(type);
        return true;
    }

    /**
     * @return
     */
    private boolean isDelimitPosition(int length, int c) {
        if(bufferIndex>=dataLen ||
                (length==1 && !pairstack.isEmpty() && pairstack.get(0)==c)) return true;

        int next_c = charUtils.codePointAt(ioBuffer.getBuffer(), bufferIndex, ioBuffer.getLength());
        if(isTokenChar(next_c)) return false;

        if(pairstack.size()==0) return true;

        int next_closechar = getPairChar(next_c);
        if(next_closechar!=0 && pairstack.get(0)!=next_closechar)
            return true;

        int size = pairstack.size();
        if((ioBuffer.getLength()-bufferIndex)<size) size = ioBuffer.getLength()-bufferIndex;

        if(next_c!=pairstack.get(0)) return false; // if next character is not close parenthesis

        for(int i=1;i<size;i++) {
            next_c = charUtils.codePointAt(ioBuffer.getBuffer(), bufferIndex+i, ioBuffer.getLength());
            if(next_c!=pairstack.get(i)) return true;
        }


        try {


            int start = bufferIndex+size;
            int end = Math.min(ioBuffer.getLength(), start + 2);

            boolean hasParticle = false;
            for(int i=start;i<end;i++) {
                int space_c = charUtils.codePointAt(ioBuffer.getBuffer(), i, ioBuffer.getLength());

                if(space_c==32) { // 32 is space ascii code
                    if(i==start)
                        return true;
                    else
                        return false;
                }

                char[] feature =  SyllableUtil.getFeature((char)space_c);

                if(i==start && !(feature[SyllableUtil.IDX_JOSA1]=='1' || feature[SyllableUtil.IDX_EOMI1]=='1')) {
                    return true;
                } else if(i==start+1 && !(feature[SyllableUtil.IDX_JOSA2]=='1' || feature[SyllableUtil.IDX_EOMI2]=='1')) {
                    return true;
                }

                hasParticle = true;
            }

            return !hasParticle;

        } catch (MorphException e) {
            throw new RuntimeException("Error occured while reading a josa");
        }

    }

    private boolean isTokenChar(int c) {
        if(Character.isLetterOrDigit(c) || isPreserveSymbol((char)c)) return true;
        return false;
    }

    private int getPairChar(int c) {
        Integer p = pairmap.get(c);
        return p==null ? 0 : p;
    }


    private boolean isPreserveSymbol(char c) {
        return (c=='#' || c=='+' || c=='-' || c=='/' || c=='·' || c == '&' || c == '_');
    }

    public final void end() throws IOException {
        super.end();
        this.offsetAtt.setOffset(this.finalOffset, this.finalOffset);
    }

    public void reset() throws IOException {
        super.reset();
        this.bufferIndex = 0;
        this.offset = 0;
        this.dataLen = 0;
        this.finalOffset = 0;
        this.ioBuffer.reset();
    }
}