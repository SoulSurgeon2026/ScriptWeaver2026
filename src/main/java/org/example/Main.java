package org.example;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import java.io.FileOutputStream;

public class Main {
    public static void main(String[] args) throws Exception {
        XWPFDocument doc = new XWPFDocument();
        doc.createParagraph().createRun().setText("Hello, ScriptWeaver!");

        try (FileOutputStream out = new FileOutputStream("test.docx")) {
            doc.write(out);
        }

        System.out.println("生成成功！");
    }
}