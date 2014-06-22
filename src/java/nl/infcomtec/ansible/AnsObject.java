/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.ansible;

import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;
import com.esotericsoftware.yamlbeans.tokenizer.CommentToken;
import com.esotericsoftware.yamlbeans.tokenizer.ScalarToken;
import com.esotericsoftware.yamlbeans.tokenizer.Token;
import com.esotericsoftware.yamlbeans.tokenizer.Tokenizer;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import nl.infcomtec.javahtml.JHFragment;

/**
 *
 * @author walter
 */
public class AnsObject {

    public interface AnsElement {

        public void addComment(AnsComment c);

        public void addComment(ArrayList<AnsComment> l);

        public void pushComments();

        public ArrayList<AnsComment> popComments();
        
        public ArrayList<AnsComment> getComments();
        
        public String getString();
        public AnsList getList();
        public AnsMap getMap();
    }

    private static class Comments {

        public final ArrayList<AnsComment> comments = new ArrayList<>();
        private final Stack<ArrayList<AnsComment>> stack = new Stack<>();

        public void addComment(AnsComment c) {
            comments.add(c);
        }

        public void addComment(ArrayList<AnsComment> l) {
            for (AnsComment e : l) {
                addComment(e);
            }
        }

        public void pushComments() {
            stack.push(new ArrayList<>(comments));
            comments.clear();
        }

        public ArrayList<AnsComment> popComments() {
            ArrayList<AnsComment> ret = new ArrayList<>(comments);
            comments.clear();
            comments.addAll(stack.pop());
            return ret;
        }

        @Override
        public String toString() {
            return "\nComments{" + "comments=" + comments + '}';
        }

        public ArrayList<AnsComment> getComments() {
            return comments;
        }

    }

    public static class AnsDocument implements AnsElement {

        public AnsElement root = null;
        private final Comments comments = new Comments();

        @Override
        public void addComment(AnsComment c) {
            comments.addComment(c);
        }

        @Override
        public String toString() {
            return "AnsDocument{" + "comments=" + comments + ", root=" + root + '}';
        }

        @Override
        public void addComment(ArrayList<AnsComment> l) {
            comments.addComment(l);
        }

        @Override
        public void pushComments() {
            comments.pushComments();
        }

        @Override
        public ArrayList<AnsComment> popComments() {
            return comments.popComments();
        }

        @Override
        public ArrayList<AnsComment> getComments() {
            return comments.getComments();
        }

        @Override
        public String getString() {
            return null;
        }

        @Override
        public AnsList getList() {
            return root.getList();
        }

        @Override
        public AnsMap getMap() {
            return root.getMap();
        }

    }

    public static class AnsComment implements AnsElement {

        private final String s;

        public AnsComment(String s) {
            this.s = s;
        }

        @Override
        public String toString() {
            return s;
        }

        @Override
        public void addComment(AnsComment c) {
            throw new RuntimeException("Cannot add a comment to a comment!");
        }

        @Override
        public void addComment(ArrayList<AnsComment> l) {
            throw new RuntimeException("Cannot add a comment to a comment!");
        }

        @Override
        public void pushComments() {
            throw new RuntimeException("Cannot add a comment to a comment!");
        }

        @Override
        public ArrayList<AnsComment> popComments() {
            throw new RuntimeException("Cannot add a comment to a comment!");
        }

        @Override
        public ArrayList<AnsComment> getComments() {
            throw new RuntimeException("Cannot add a comment to a comment!");
        }

        @Override
        public String getString() {
            return s;
        }

        @Override
        public AnsList getList() {
            return null;
        }

        @Override
        public AnsMap getMap() {
            return null;
        }

    }

    public static class AnsString implements AnsElement, Comparable<AnsString> {

        private final String s;
        private final Comments comments = new Comments();

        @Override
        public void addComment(AnsComment c) {
            comments.addComment(c);
        }

        @Override
        public void addComment(ArrayList<AnsComment> l) {
            comments.addComment(l);
        }

        @Override
        public void pushComments() {
            comments.pushComments();
        }

        @Override
        public ArrayList<AnsComment> popComments() {
            return comments.popComments();
        }

        public AnsString(String s) {
            this.s = s;
        }

        @Override
        public String toString() {
            return "\nAnsString{" + "s=" + s + ", comments=" + comments + '}';
        }

        /**
         * @return the s
         */
        @Override
        public String getString() {
            return s;
        }

        @Override
        public int compareTo(AnsString o) {
            return s.compareTo(o.s);
        }

        @Override
        public ArrayList<AnsComment> getComments() {
            return comments.getComments();
        }

        @Override
        public AnsList getList() {
            return null;
        }

        @Override
        public AnsMap getMap() {
            return null;
        }

    }

    public static class AnsList extends ArrayList<AnsElement> implements AnsElement {

        private final Comments comments = new Comments();

        @Override
        public void addComment(AnsComment c) {
            comments.addComment(c);
        }

        @Override
        public void addComment(ArrayList<AnsComment> l) {
            comments.addComment(l);
        }

        @Override
        public void pushComments() {
            comments.pushComments();
        }

        @Override
        public ArrayList<AnsComment> popComments() {
            return comments.popComments();
        }

        @Override
        public String toString() {
            StringBuilder ret = new StringBuilder("\nAnsList{");
            ret.append(comments.toString());
            for (AnsElement e : this) {
                ret.append(", ");
                ret.append(e);
            }
            return ret.append("}").toString();
        }

        @Override
        public ArrayList<AnsComment> getComments() {
            return comments.getComments();
        }

        @Override
        public String getString() {
            return null;
        }

        @Override
        public AnsList getList() {
            return this;
        }

        @Override
        public AnsMap getMap() {
            return null;
        }

    }

    public static class AnsMap extends TreeMap<AnsString, AnsElement> implements AnsElement {

        private final Comments comments = new Comments();

        @Override
        public void addComment(AnsComment c) {
            comments.addComment(c);
        }

        @Override
        public void addComment(ArrayList<AnsComment> l) {
            comments.addComment(l);
        }

        @Override
        public void pushComments() {
            comments.pushComments();
        }

        @Override
        public ArrayList<AnsComment> popComments() {
            return comments.popComments();
        }

        @Override
        public String toString() {
            StringBuilder ret = new StringBuilder("\nAnsMap{");
            ret.append(comments.toString());
            for (Entry<AnsString, AnsElement> e : this.entrySet()) {
                ret.append(", ");
                ret.append(e.getKey());
                ret.append("=");
                ret.append(e.getValue());
            }
            return ret.toString();
        }

        @Override
        public ArrayList<AnsComment> getComments() {
            return comments.getComments();
        }

        @Override
        public String getString() {
            return null;
        }

        @Override
        public AnsList getList() {
            return null;
        }

        @Override
        public AnsMap getMap() {
            return this;
        }

    }

    @SuppressWarnings("empty-statement")
    private static AnsElement extract(AnsElement parent, Iterator toker) throws IOException {
        if (parent == null) {
            throw new RuntimeException("This would be a huge problem were we to find a comment!");
        }
        if (!toker.hasNext()) {
            return null;
        }
        Token tok = (Token) toker.next();
        switch (tok.type) {
            case STREAM_START:
                if (parent instanceof AnsDocument) {
                    return extract(parent, toker);
                } else {
                    throw new RuntimeException("STREAM_START for " + parent);
                }
            case DOCUMENT_START:
                if (parent instanceof AnsDocument) {
                    return extract(parent, toker);
                } else {
                    throw new RuntimeException("DOCUMENT_START for " + parent);
                }
            case BLOCK_MAPPING_START: {
                AnsMap map = new AnsMap();
                if (parent instanceof AnsDocument) {
                    ((AnsDocument) parent).root = map;
                }
                while (extract(map, toker) != null);
                return map;
            }
            case BLOCK_SEQUENCE_START: {
                AnsList list = new AnsList();
                if (parent instanceof AnsDocument) {
                    ((AnsDocument) parent).root = list;
                }
                while (extract(list, toker) != null);
                return list;
            }
            case KEY: {
                if (parent instanceof AnsMap) {
                    parent.pushComments();
                    AnsString key = (AnsString) extract(parent, toker);
                    if (key != null) {
                        key.addComment(parent.popComments());
                        parent.pushComments();
                        AnsElement val = extract(parent, toker);
                        val.addComment(parent.popComments());
                        ((AnsMap) parent).put(key, val);
                    } else {
                        parent.popComments();
                    }
                    return parent;
                } else {
                    throw new RuntimeException("Cannot insert KEY in a " + parent);
                }
            }
            case SCALAR: {
                ScalarToken s = (ScalarToken) tok;
                if (!s.getPlain()) {
                    throw new RuntimeException("Cannot handle non-plain scalars " + s);
                }
                if (s.getStyle() != 0) {
                    throw new RuntimeException("Cannot handle style " + (int) s.getStyle());
                }
                return new AnsString(s.getValue());
            }
            case VALUE: {
                return extract(parent, toker);
            }
            case BLOCK_ENTRY: {
                if (parent instanceof AnsList) {
                    parent.pushComments();
                    AnsElement elm = extract(parent, toker);
                    if (elm != null) {
                        elm.addComment(parent.popComments());
                        ((AnsList) parent).add(elm);
                    } else {
                        parent.popComments();
                    }
                    return parent;
                } else {
                    throw new RuntimeException("Cannot append BLOCK_ENTRY to a " + parent);
                }
            }
            case BLOCK_END:
                return null;
            case COMMENT:
                parent.addComment(new AnsComment(((CommentToken) tok).comment));
                return extract(parent, toker);
            default:
                throw new RuntimeException("Can't handle " + tok);
        }
    }

    /**
     *
     * @author walter
     */
    private static class MyWriter extends Writer {

        private final StringBuilder out;

        public MyWriter() {
            out = new StringBuilder();
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            out.append(cbuf, off, len);
        }

        @Override
        public void flush() throws IOException {
        }

        @Override
        public void close() throws IOException {
            for (int dash = out.indexOf("\n-"); dash != -1; dash = out.indexOf("\n-", dash)) {
                out.insert(dash, '\n');
                dash += 3;
            }
        }

        @Override
        public String toString() {
            return out.toString();
        }

    }

    public static Map<Object, Object> getMap(Object object) {
        if (object instanceof Map) {
            return (Map) object;
        }
        return null;
    }

    private static void toHtml(JHFragment frag, List l) {
        for (Object e : l) {
            frag.push("li");
            toHtml(frag, e);
            frag.pop();
        }
    }

    private static void toHtml(JHFragment frag, Set<Map.Entry> set) {
        for (Map.Entry e : set) {
            frag.push("li");
            frag.appendText(e.getKey().toString()).appendText(": ");
            toHtml(frag, e.getValue());
            frag.pop();
        }
    }

    public static void toHtml(JHFragment frag, Object obj) {
        if (obj instanceof Map) {
            frag.push("ul");
            toHtml(frag, (Set<Map.Entry>) ((Map) obj).entrySet());
            frag.pop();
        } else if (obj instanceof List) {
            frag.push("ul");
            toHtml(frag, (List) obj);
            frag.pop();
        } else if (obj instanceof String) {
            frag.appendP(obj.toString());
        } else {
            throw new RuntimeException("What is this? " + obj.getClass().getName());
        }
    }

    public final Object object;
    public final AnsElement root;
    public final File inFile;

    public AnsObject(final PlayBooks books, final File inFile, final FileReader fileReader) throws YamlException {
        YamlReader reader = new YamlReader(fileReader);
        this.object = reader.read();
        this.inFile = inFile;
        try (FileReader again = new FileReader(inFile)) {
            root = new AnsDocument();
            extract(root, new Tokenizer(again, true).iterator());
        } catch (IOException e) {
            throw new YamlException(e);
        }
        if (books != null) {
            books.scanForVars(inFile, object);
        }
    }

    public AnsObject(final PlayBooks books, final File inFile, final String yaml) throws YamlException {
        YamlReader reader = new YamlReader(yaml);
        this.object = reader.read();
        this.inFile = inFile;
        try (Reader again = new StringReader(yaml)) {
            root = new AnsDocument();
            extract(root, new Tokenizer(again, true).iterator());
        } catch (IOException e) {
            throw new YamlException(e);
        }
        if (books != null) {
            books.scanForVars(inFile, object);
        }
    }

    public Map<Object, Object> getMap() {
        return getMap(object);
    }

    private boolean removeElement(Object object, String element) {
        if (object instanceof List) {
            List l = (List) object;
            if (l.remove(element)) {
                return true;
            }
            for (Iterator it = l.iterator(); it.hasNext();) {
                Object sub = it.next();
                if (removeElement(sub, element)) {
                    it.remove();
                    return true;
                }
            }
        } else if (object instanceof Map) {
            Map<Object, Object> map = getMap(object);
            if (map.containsValue(element)) {
                map.clear();
                return true;
            }
            for (Object sub : map.values()) {
                return removeElement(sub, element);
            }
        }
        return false;
    }

    public boolean removeElement(String element) {
        return removeElement(object, element);
    }

    public void toHtml(JHFragment frag) {
        toHtml(frag, object);
    }

    /**
     * Serialize any object to YAML. Special feature: adds blank lines to
     * top-level list elements.
     *
     * @param obj The object to serialize.
     * @return A String with the serialized object.
     * @throws YamlException If YamlWriter does.
     */
    public static String makeString(Object obj) throws YamlException {
        MyWriter myw = new MyWriter();
        YamlConfig config = new YamlConfig();
        config.writeConfig.setWrapColumn(Integer.MAX_VALUE);
        config.writeConfig.setExplicitFirstDocument(true);
        YamlWriter writer = new YamlWriter(myw, config);
        writer.write(obj);
        writer.close();
        return myw.toString();

    }

    /**
     * Serialize this object to YAML. Special feature: adds blank lines to
     * top-level list elements.
     *
     * @return A String with the serialized object.
     * @throws YamlException If YamlWriter does.
     */
    public String makeString() throws YamlException {
        return makeString(object);
    }
}
