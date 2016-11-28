package sample;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static com.sun.org.apache.xml.internal.resolver.Catalog.URI;

/**
 * Created by Owner1-kat_lab on 2016/11/18.
 */
public class SearchBooksInformation {
    private Document document;
    private String title = "";
    private String creator = "";
    private String isbn = "";
    private String publisher = "";
    private String issuedFromYear = "";
    private String issuedUntilYear = "";
    private String searchedInformation = "";
    private String numberOfRecord = "";

    private boolean noIsbnBooksIsOutput = false;
    private boolean isIsbn = false;
    private int outputRecordsCount = 0;
    private int maximumRecordsNumber = 200;
    private boolean answerTitle = false;
    private boolean answerCreator = false;
    private boolean answerIsbn = false;
    private boolean answerPrice = false;
    private boolean answerPublisher = false;
    private boolean answerIssued = false;
    private boolean answerUriFromNdl = false;
    private boolean answerDescription = false;
    private boolean answerUriFromAmazonSearch = false;
    private String uriFromAmazonSearch = "";
    private final String AND = "%20AND%20";

    // Setter
    public void setTitle(String _title) {
        title = "title%3d" + _title;
    }
    public void setCreator(String _creator) {
        creator = "creator%3d" + _creator;
    }
    public void setIsbn(String _isbn) {
        isbn = "isbn%3d" + _isbn;
    }
    public void setPublisher(String _publisher) {
        publisher = "publisher%3d" + _publisher;
    }
    public void setIssuedFromYear(String _issued) {
        issuedFromYear = "from%3d" + _issued;
    }
    public void setIssuedUntilYear(String _issued) {
        issuedUntilYear = "until%3d" + _issued;
    }
    public void setNoIsbnBookIsOutput(boolean b) {
        noIsbnBooksIsOutput = b;
    }
    public void setAnswerTitle(boolean b) {
        answerTitle = b;
    }
    public void setAnswerCreator(boolean b) {
        answerCreator = b;
    }
    public void setAnswerIsbn(boolean b) {
        answerIsbn = b;
    }
    public void setAnswerPrice(boolean b) {
        answerPrice = b;
    }
    public void setAnswerPublisher(boolean b) {
        answerPublisher = b;
    }
    public void setAnswerIssued(boolean b) {
        answerIssued = b;
    }
    public void setAnswerUriFromNdl(boolean b) {
        answerUriFromNdl = b;
    }
    public void setAnswerDescription(boolean b) {
        answerDescription = b;
    }
    public void setAnswerUriFromAmazonSearch(boolean b) {
        answerUriFromAmazonSearch = b;
    }
    public void setMaximumRecords(int i) {
        maximumRecordsNumber = i;
    }

    // Getter
    public String getSearchedInformation() {
        return searchedInformation;
    }
    public String getNumberOfRecord() {
        return numberOfRecord;
    }
    public int getOutputRecordCount() {
        return outputRecordsCount;
    }
    public int getMaximumRecords() {
        return maximumRecordsNumber;
    }

    //----------------------------------------------------------------//


    public void isSearchedFromNationalDietLibrary() {
        String[] queryArray = {title, creator, isbn, publisher, issuedFromYear, issuedUntilYear};
        String[] dcItemArray = {"title", "creator", "isbn", "price", "publisher", "issued", "uri", "description"};
        String query = "";
        String recordSchema = "&recordSchema=dcndl_simple";
        String maximumRecords = "&maximumRecords=" + maximumRecordsNumber;
        String uri;

        outputRecordsCount = 0;
        numberOfRecord = "0";
        query = setQuery(queryArray, "&query=");

        // uri = "http://iss.ndl.go.jp/api/sru?operation=searchRetrieve&query=title%3d\"perl\"%20AND%20creator%3d\"phoenix\"&recordSchema=dcndl_simple";
        uri = "http://iss.ndl.go.jp/api/sru?operation=searchRetrieve" + query + recordSchema + maximumRecords;
        try {
            System.out.println("Encoded URL: " + uri);
            System.out.println("Decoded URL: " + URLDecoder.decode(uri, "UTF-8"));
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(uri);
        } catch (IOException e) {
            System.out.println("IOError");
            e.printStackTrace();
        } catch (SAXException e) {
            System.out.println("SAXError");
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            System.out.println("ParserError");
            e.printStackTrace();
        } finally {
            searchedInformation = setSearchedInformation(document.getDocumentElement(), dcItemArray);
        }

    }

    /**
     * テキストエリアに出力する情報を選択し、整地する。
     *
     * @param root Element型のドキュメント要素
     * @param dcItemArray searchDataItemList関数内におけるif文の真偽値判定で用いる。
     * @return info テキストエリアに出力する新情報
     */
    private String setSearchedInformation(Element root, String[] dcItemArray) {
        String information = "";
        NodeList rootChildren = root.getChildNodes();
        NodeList recordsNodeList = searchChildrenNode(rootChildren, "records");
        NodeList numberOfRecordNodeList = searchChildrenNode(rootChildren, "numberOfRecords");
        if(numberOfRecordNodeList == null) {
            return "Error, There queries are invalid.";
        }
        numberOfRecord = numberOfRecordNodeList.item(0).getTextContent();
        if (numberOfRecord.equals("0")) {
            return "Error, There are not any records.";
        }

        for(int i = 0; i < recordsNodeList.getLength(); i++) {
            String info = "";
            uriFromAmazonSearch = "";
            isIsbn = false;
            Node recordNode = recordsNodeList.item(i);
            NodeList recordDataNodeList = searchChildrenNode(recordNode.getChildNodes(), "recordData");
            if(recordDataNodeList == null) {
                continue;
            }
            for(int j = 0; j < recordDataNodeList.getLength(); j++) {
                Node dcndlSimpleDcNode = recordDataNodeList.item(j);
                for(String dcItem: dcItemArray) {
                    String recordDataItemString = searchDataItemList(dcndlSimpleDcNode.getChildNodes(), dcItem);
                    if (recordDataItemString.isEmpty()) {
                        continue;
                    }
                    //System.out.println(recordDataItemString);
                    info += recordDataItemString + "\n";
                }
            }
            if(answerUriFromAmazonSearch) {
                info += "Amazon：" + uriFromAmazonSearch;
            }
            info += "\n";

            if(noIsbnBooksIsOutput || isIsbn) {
                information += info;
                outputRecordsCount++;
            }
        }

        return information;
    }

    /**
     * クエリとして入力されているものを選び、uriに貼り付けるためのquery文字列変数に格納する。
     * クエリとクエリの間にはAND文字列定数を繋げる。
     * 幾つかのクエリは複数検索が可能である。
     *
     * @param array
     * @param _query
     * @return _query
     */
    private String setQuery(String[] array, String _query) {
        boolean firstQueryFlag = true;

        for (String q : array) {
            String[] queryElement = q.split("%3d", 0);
            int queryElementCount = 0;
            // 検索文字列中に"%3d"という文字列が含まれていた場合の対処法
            for(int i = 2; i < queryElement.length; i++) {
                queryElement[1] += queryElement[i];
                queryElementCount++;
            }

            // 検索文字列がそもそも存在しなければ、このfor文内は無視
            for(int i = 1; i < queryElement.length - queryElementCount; i++) {
                queryElement[1] = queryElement[1].replaceAll("[-()~^|_{}:;@`/<>,.]", " "); // 記号消去
                for(String qItem: queryElement[1].split(" ", 0)) {
                    // 文字コードを変換しないと、jarファイルやexeファイル上では正しく動作しない
                    try {
                        qItem = URLEncoder.encode(qItem, "UTF-8");
                    } catch (UnsupportedEncodingException e) {}
                    if(qItem.isEmpty()) { // 文字分割でスペース2つも分割されてしまうため、その間の空っぽ文字列を無視
                        continue;
                    }

                    if (firstQueryFlag) {
                        _query = _query + queryElement[0] + "%3d\"" + qItem + "\"";
                        firstQueryFlag = false;
                    } else {
                        _query = _query + AND + queryElement[0] + "%3d\"" + qItem + "\"";
                    }
                }
            }
        }

        return _query;
    }

    private NodeList searchChildrenNode(NodeList node, String string) {
        NodeList childNodeList = null;
        for(int i = 0; i < node.getLength(); i++) {
            Node childNode = node.item(i);
            if(childNode.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element)childNode;
                if(element.getNodeName().equals(string)) {
                    childNodeList = childNode.getChildNodes();
                }
            }
        }
        return childNodeList;
    }

    private String searchDataItemList(NodeList node, String _string) {
        List<String> itemList;
        String outputString = "";

        if(_string.equals("title")) {
            String fieldKeywords = "";
            itemList = searchDataItem(node, "dcndl:volume");
            for(String str: searchDataItem(node, "dc:title")) {
                itemList.add(str);
            }
            for(String str: searchDataItem(node, "dcndl:edition")) {
                itemList.add(str);
            }

            if(answerTitle) {
                outputString = setSearchedDataItemList(itemList, "タイトル：", " ");
                fieldKeywords = setSearchedDataItemList(itemList, "&field-keywords=", " ");
            }
            if(answerUriFromAmazonSearch && !fieldKeywords.isEmpty()) {
                uriFromAmazonSearch = "https://www.amazon.co.jp/s/ref=nb_sb_noss?url=search-alias%3Dstripbooks";
                uriFromAmazonSearch += fieldKeywords + "\n";
            }

        } else if(answerCreator && _string.equals("creator")) {
            itemList = searchDataItem(node, "dc:creator");
            outputString = setSearchedDataItemList(itemList, "著者：", " / ");

        } else if(_string.equals("isbn")) {
            itemList = searchDataItem(node, "dc:identifier", "xsi:type", "dcndl:ISBN");
            if(!itemList.isEmpty()) {
                isIsbn = true;
            }
            if(answerIsbn) {
                for(String str: itemList) {
                    outputString = "ISBN-"+ str.length() +"：" + str;
                }
            }

        } else if(answerPrice && _string.equals("price")) {
            itemList = searchDataItem(node, "dcndl:price");
            outputString = setSearchedDataItemList(itemList, "価格：", " / ");

        } else if(answerPublisher && _string.equals("publisher")) {
            itemList = searchDataItem(node, "dc:publisher");
            outputString = setSearchedDataItemList(itemList, "出版社：", " / ");

        } else if(answerIssued && _string.equals("issued")) {
            itemList = searchDataItem(node, "dcterms:issued");
            outputString = setSearchedDataItemList(itemList, "出版年：", " / ");

        } else if(answerUriFromNdl && _string.equals("uri")) {
            itemList = searchDataItem(node, "dc:identifier", "xsi:type", "dcterms:URI");
            outputString = setSearchedDataItemList(itemList, "国会図書館：", "\n ");

        } else if(answerDescription && _string.equals("description")) {
            itemList = searchDataItem(node, "dcterms:description");
            outputString = setSearchedDataItemList(itemList, "詳細：", "\n ");

        }

        return outputString;
    }

    private List<String> searchDataItem(NodeList node, String string) {
        List<String> itemList = new ArrayList<>();
        for(int i = 0; i < node.getLength(); i++) {
            Node childNode = node.item(i);
            if(childNode.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element)childNode;
                if(element.getNodeName().equals(string)) {
                    itemList.add(childNode.getTextContent());
                }
            }
        }
        return itemList;
    }

    private List<String> searchDataItem(NodeList node, String string, String id, String idAns) {
        List<String> itemList = new ArrayList<>();
        for(int i = 0; i < node.getLength(); i++) {
            Node childNode = node.item(i);
            if(childNode.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element)childNode;
                if(element.getNodeName().equals(string)) {
                    if (element.getAttribute(id).equals(idAns)) {
                        itemList.add(childNode.getTextContent());
                    }
                }
            }
        }
        return itemList;
    }

    private String setSearchedDataItemList(List<String> node, String titleString, String splitStr) {
        if(node.isEmpty()) {
            return "";
        }

        String outputString = titleString + node.get(0);
        for(int i = 1; i < node.size(); i++) {
            outputString += splitStr + node.get(i);
        }
        return outputString;
    }

}
