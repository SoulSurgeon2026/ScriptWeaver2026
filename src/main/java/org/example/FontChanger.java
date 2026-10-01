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
}