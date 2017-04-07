package test.jiyun.com.pulltoactivity;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

import test.jiyun.com.pulltoactivity.bean.News;

public class MyDefaultHandler extends DefaultHandler {
    private String mNameStr;
    public News mNews;
    private ArrayList<News> list = new ArrayList<>();

    public ArrayList<News> getList() {
        return list;
    }

    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {
        mNameStr = qName;
        if (mNameStr.equals("news")) {
            if (mNews == null) {
                mNews = new News();
            }
        }
    }

    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (qName.equals("news")) {
            list.add(mNews);
            mNews = null;
        }
        mNameStr = "";
    }

    public void characters(char[] ch, int start, int length)
            throws SAXException {

        if (mNameStr.equals("id")) {
            String id = new String(ch, start, length);
            mNews.setId(id);
        } else if (mNameStr.equals("title")) {
            String title = new String(ch, start, length);
            mNews.setTitle(title);
        } else if (mNameStr.equals("body")) {
            String body = new String(ch, start, length);
            mNews.setBody(body);
        } else if (mNameStr.equals("pubDate")) {
            String pubDate = new String(ch, start, length);
            mNews.setPubDate(pubDate);
        }
    }
}
