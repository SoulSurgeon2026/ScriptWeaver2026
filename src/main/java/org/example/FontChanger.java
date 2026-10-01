package org.example;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

public class FontChanger {

    // ---------- 字符判断 ----------
    public static boolean isCJK(char c) {
        return (c >= 0x4E00 && c <= 0x9FFF)
                || (c >= 0x3040 && c <= 0x30FF)
                || (c >= 0xAC00 && c <= 0xD7AF);
    }

    public static boolean isArabic(char c) {
        return c >= 0x0600 && c <= 0x06FF;
    }

    public static boolean isHebrew(char c) {
        return c >= 0x0590 && c <= 0x05FF;
    }

    public static boolean isCyrillic(char c) {
        return c >= 0x0400 && c <= 0x04FF;
    }

    public static boolean isLatin(char c) {
        return (c >= 0x0041 && c <= 0x007A) || (c >= 0x00C0 && c <= 0x024F);
    }

    public static boolean isGreek(char c) {
        return c >= 0x0370 && c <= 0x03FF;
    }

    // ---------- 遍历正文 + 表格 ----------
    public static List<XWPFRun> allRuns(XWPFDocument doc) {
        List<XWPFRun> list = new ArrayList<>();
        for (XWPFParagraph p : doc.getParagraphs()) {
            list.addAll(p.getRuns());
        }
        for (XWPFTable table : doc.getTables()) {
            collectTableRuns(table, list);
        }
        return list;
    }

    private static void collectTableRuns(XWPFTable table, List<XWPFRun> list) {
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                for (XWPFParagraph p : cell.getParagraphs()) {
                    list.addAll(p.getRuns());
                }
                for (XWPFTable nested : cell.getTables()) {
                    collectTableRuns(nested, list);
                }
            }
        }
    }

    /** 收集所有段落（含表格），用于按段落遍历拆分 run */
    public static List<XWPFParagraph> allParagraphs(XWPFDocument doc) {
        List<XWPFParagraph> list = new ArrayList<>();
        for (XWPFParagraph p : doc.getParagraphs()) {
            list.add(p);
        }
        for (XWPFTable table : doc.getTables()) {
            collectTableParagraphs(table, list);
        }
        return list;
    }

    private static void collectTableParagraphs(XWPFTable table, List<XWPFParagraph> list) {
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                for (XWPFParagraph p : cell.getParagraphs()) {
                    list.add(p);
                }
                for (XWPFTable nested : cell.getTables()) {
                    collectTableParagraphs(nested, list);
                }
            }
        }
    }

    // ---------- 只改东亚字体 ----------
    public static void setEastAsiaFont(XWPFRun run, String fontName) {
        setFontAttr(run, "eastAsia", fontName);
    }

    // ---------- 只改复杂文种字体 ----------
    public static void setCsFont(XWPFRun run, String fontName) {
        setFontAttr(run, "cs", fontName);
    }

    // ---------- 只改西文字体（ascii + hAnsi 一起） ----------
    public static void setAsciiFont(XWPFRun run, String fontName) {
        setFontAttr(run, "ascii", fontName);
        setFontAttr(run, "hAnsi", fontName);
    }

    // ---------- 底层：设置 rFonts 的某个属性 ----------
    private static void setFontAttr(XWPFRun run, String attr, String value) {
        XmlObject rObj = run.getCTR();
        Node rNode = rObj.getDomNode();

        Node rPrNode = findChild(rNode, "rPr");
        if (rPrNode == null) {
            rPrNode = rNode.getOwnerDocument().createElementNS(
                    "http://schemas.openxmlformats.org/wordprocessingml/2006/main", "w:rPr");
            rNode.insertBefore(rPrNode, rNode.getFirstChild());
        }

        Node rFontsNode = findChild(rPrNode, "rFonts");
        if (rFontsNode == null) {
            rFontsNode = rNode.getOwnerDocument().createElementNS(
                    "http://schemas.openxmlformats.org/wordprocessingml/2006/main", "w:rFonts");
            rPrNode.insertBefore(rFontsNode, rPrNode.getFirstChild());
        }

        ((org.w3c.dom.Element) rFontsNode).setAttributeNS(
                "http://schemas.openxmlformats.org/wordprocessingml/2006/main",
                "w:" + attr, value);
    }

    private static Node findChild(Node parent, String localName) {
        Node child = parent.getFirstChild();
        while (child != null) {
            if (child.getLocalName() != null && child.getLocalName().equals(localName)) {
                return child;
            }
            child = child.getNextSibling();
        }
        return null;
    }

    // ================== 按语言拆分 run ==================

    /**
     * 把一个 run 按“语言边界”拆成多个小 run。
     * langKeys: 每个元素是 [属性名("eastAsia"/"cs"/"ascii"), 字体名, 字号字符串]
     * 返回拆分后的 run 列表（可能只有一个，就是原 run）。
     */
    public static List<XWPFRun> splitRunByLanguage(
            XWPFRun run, XWPFParagraph para, List<String[]> langKeys) throws Exception {

        String text = run.text();
        if (text == null || text.isEmpty()) {
            List<XWPFRun> single = new ArrayList<>();
            single.add(run);
            return single;
        }

        // 逐字符判断 key。key = "属性名|字号"
        List<int[]> boundaries = new ArrayList<>();
        List<String> keys = new ArrayList<>();

        String curKey = null;
        int curStart = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            String key = null;
            for (String[] lk : langKeys) {
                String attr = lk[0];
                String size = lk[2];
                boolean hit = false;
                if (attr.equals("eastAsia") && isCJK(c)) hit = true;
                else if (attr.equals("cs") && (isArabic(c) || isHebrew(c) || isCyrillic(c))) hit = true;
                else if (attr.equals("ascii") && (isLatin(c) || isGreek(c))) hit = true;
                if (hit) {
                    key = attr + "|" + size;
                    break;
                }
            }
            if (curKey == null) {
                curKey = key;
                curStart = 0;
            } else if (!safeEquals(key, curKey)) {
                boundaries.add(new int[]{curStart, i});
                keys.add(curKey);
                curKey = key;
                curStart = i;
            }
        }
        if (curKey != null) {
            boundaries.add(new int[]{curStart, text.length()});
            keys.add(curKey);
        }

        // 不用拆
        if (boundaries.size() <= 1) {
            List<XWPFRun> single = new ArrayList<>();
            single.add(run);
            return single;
        }

        // 需要拆：用 para.createRun() 建新 run，再把它移到正确位置
        List<XWPFRun> result = new ArrayList<>();

        // 先把原 run 设成第一段
        setRunText(run, text.substring(boundaries.get(0)[0], boundaries.get(0)[1]));
        result.add(run);

        for (int idx = 1; idx < boundaries.size(); idx++) {
            int s = boundaries.get(idx)[0];
            int e = boundaries.get(idx)[1];
            String segText = text.substring(s, e);

            // 在段落里新建一个 run
            XWPFRun newRun = para.createRun();
            setRunText(newRun, segText);

            // 把新 run 的 XML 节点从段落末尾移到前一个 run 后面
            Node newNode = newRun.getCTR().getDomNode();
            Node prevNode = result.get(result.size() - 1).getCTR().getDomNode();
            newNode.getParentNode().removeChild(newNode);
            prevNode.getParentNode().insertBefore(newNode, prevNode.getNextSibling());

            result.add(newRun);
        }
        return result;
    }

    private static boolean safeEquals(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    /** 把 run 里的文本全部替换成 text */
    public static void setRunText(XWPFRun run, String text) {
        Node rNode = run.getCTR().getDomNode();
        // 删除所有 w:t
        org.w3c.dom.NodeList tList = ((org.w3c.dom.Element) rNode)
                .getElementsByTagNameNS(
                        "http://schemas.openxmlformats.org/wordprocessingml/2006/main", "t");
        for (int i = tList.getLength() - 1; i >= 0; i--) {
            Node t = tList.item(i);
            t.getParentNode().removeChild(t);
        }
        // 新增一个 w:t
        org.w3c.dom.Element t = rNode.getOwnerDocument().createElementNS(
                "http://schemas.openxmlformats.org/wordprocessingml/2006/main", "w:t");
        t.setAttributeNS("http://www.w3.org/XML/1998/namespace", "xml:space", "preserve");
        t.setTextContent(text);
        rNode.appendChild(t);
    }
}