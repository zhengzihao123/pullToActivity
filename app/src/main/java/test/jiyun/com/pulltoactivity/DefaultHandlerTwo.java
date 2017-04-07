package test.jiyun.com.pulltoactivity;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class DefaultHandlerTwo extends DefaultHandler {
    private String mNameStr;
    private String url;

    public String getUrl() {
        return url;
    }

    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {
        mNameStr = qName;
    }

    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        mNameStr = "";
    }

    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (mNameStr.equals("url")) {
            String url = new String(ch, start, length);
            this.url = url;
        }
    }
}
