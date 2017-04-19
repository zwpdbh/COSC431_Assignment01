import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zw on 16/04/2017.
 */
public class Util {
    public static ArrayList<String> getToken(String word) {
        String[] tmp = removeTags(word).split("[\\p{Punct}\\s']+");
        ArrayList<String> results = new ArrayList<>();
        String token;

        for (int i = 0; i < tmp.length; i++) {
            token = removeMeaninglessString(tmp[i]);
            token = token.replaceAll("[,\"().]", "");
            if (!token.trim().isEmpty()) {
                results.add(token);
            }
        }

        return results;
    }


    public static String removeTags(String str) {
        String result = str;

        Pattern pat;
        Matcher mat;

        // need to get the document number
        pat = Pattern.compile("^wsj[0-9]*-[0-9]*");
        mat = pat.matcher(result);
        if (mat.find()) {
            return result;
        }

        if (!result.isEmpty()) {
            // to handle: "&lt/docno&gt<w:rfonts"
            pat = Pattern.compile(".*\\/.+<.*");
            mat = pat.matcher(result);
            if (mat.find()) {
                result = "";
            }
        }

        if (!result.isEmpty()) {
            // to handle: xmlns:w="http://schemas.microsoft.com/office/word/2003/wordml"
            pat = Pattern.compile(".+:[a-z0-9-]+=");
            mat = pat.matcher(result);
            if (mat.find()) {
                result = "";
            }
        }

        if (!result.isEmpty()) {
            // to handle: <?xml
            pat = Pattern.compile("^<.+[a-z]$");
            mat = pat.matcher(result);
            if (mat.find()) {
                result = "";
            }
        }

        if (!result.isEmpty()) {
            // to handle: progid="word.document"?>
            pat = Pattern.compile("[a-z0-9]+=\"[a-z0-9.]+\".*>$");
            mat = pat.matcher(result);
            if (mat.find()) {
                result = "";
            }
        }


        if (!result.isEmpty()) {
            // to handle &amp
            pat = Pattern.compile("&amp");
            mat = pat.matcher(result);
            if (mat.find()) {
                result = "";
            }
        }

        if (!result.isEmpty()) {
            // to handle : encoding="utf-8"
            pat = Pattern.compile("[a-z0-9]+=\"");
            mat = pat.matcher(result);
            if (mat.find()) {
                result = "";
            }
        }

        result = result.replaceAll("<[/]?.+>", "");

        return result;
    }

    public static String removeMeaninglessString(String str) {
        if (isNumeric(str)) {
            return str;
        } else if (str.length() > 1) {
            return str;
        } else {
            return "";
        }
    }

    public static boolean isNumeric(String str) {
        try {
            double d = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }


    public static boolean isDocumentNumber(String s) {
        Pattern pat = Pattern.compile("^wsj[0-9]*-[0-9]*");
        Matcher mat = pat.matcher(s);
        if (mat.find()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isEndOfDoc(String s) {
        Pattern pat = Pattern.compile("</doc>");
        Matcher mat = pat.matcher(s);
        if (mat.find()) {
            return true;
        } else {
            return false;
        }
    }

    public static String stripNonDigits(final CharSequence input) {
        final StringBuilder sb = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            final char c = input.charAt(i);
            if (c > 47 && c < 58) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static int sizeof(Object obj) throws IOException {
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteOutputStream);

        objectOutputStream.writeObject(obj);
        objectOutputStream.flush();
        objectOutputStream.close();

        return byteOutputStream.toByteArray().length;
    }

}
