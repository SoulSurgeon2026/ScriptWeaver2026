package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {

    // 配色
    static final Color BG = new Color(244, 246, 249);
    static final Color CARD = Color.WHITE;
    static final Color ACCENT = new Color(47, 111, 235);
    static final Color TEXT = new Color(31, 41, 55);
    static final Color TEXT_MUTED = new Color(107, 114, 128);
    static final Color BORDER = new Color(209, 213, 219);

    // 字体列表
    static final String[] FONTS_EAST = {
            "宋体", "微软雅黑", "楷体", "黑体", "仿宋", "幼圆",
            "思源宋体", "思源黑体", "华文宋体", "华文楷体", "华文仿宋",
            "华文中宋", "华文细黑", "方正舒体", "方正姚体", "隶书", "舒体",
    };
    static final String[] FONTS_CS = {
            "Arial", "Tahoma", "Times New Roman", "Segoe UI",
            "Simplified Arabic", "Traditional Arabic", "Microsoft Sans Serif",
            "David", "Narkisim", "FrankRuehl", "Miriam", "Gisha",
    };
    static final String[] FONTS_ASCII = {
            "Times New Roman", "Arial", "Calibri", "Verdana",
            "Georgia", "Courier New", "Consolas", "Cambria",
            "Trebuchet MS", "Tahoma", "Segoe UI", "Book Antiqua",
            "Century Gothic", "Franklin Gothic Medium", "Garamond",
            "Palatino Linotype", "Rockwell", "Lucida Console",
    };
    static final String[] SIZES = {
            "8", "10", "12", "14", "16", "18", "20", "22",
            "24", "26", "28", "30", "32",
    };

    static final String[] WORD_FONTS = {
            "宋体", "微软雅黑", "楷体", "黑体", "仿宋", "幼圆",
            "Times New Roman", "Arial", "Calibri", "Verdana", "Georgia",
            "Courier New", "Consolas", "Cambria", "Tahoma", "Segoe UI",
    };

    static final String[][] PRESET_COLORS = {
            {"黑", "#000000"},
            {"红", "#e11d48"},
            {"橙", "#ea580c"},
            {"黄", "#ca8a04"},
            {"绿", "#16a34a"},
            {"蓝", "#2563eb"},
            {"紫", "#7c3aed"},
            {"灰", "#6b7280"},
    };

    // 文件列表
    private JList<String> fileList;
    private DefaultListModel<String> fileListModel;
    private JLabel statusLabel;
    private List<String> files = new ArrayList<>();
    private String outputDir = "";

    // 语言页 —— 东亚互斥
    private JCheckBox cbChinese, cbJapanese, cbKorean;
    private boolean adjustingCJK = false;
    private JComboBox<String> eastFontCombo, eastSizeCombo;
    private JCheckBox cbArabic, cbHebrew, cbRussian;
    private JComboBox<String> csFontCombo, csSizeCombo;
    private JCheckBox cbEnglish, cbSpanish, cbGreek;
    private JComboBox<String> asciiFontCombo, asciiSizeCombo;

    // 词汇页
    private JTextField wordField;
    private JRadioButton rbExact, rbContains, rbRegex;
    private JCheckBox cbAttrFont, cbAttrSize, cbAttrColor, cbAttrBold, cbAttrItalic, cbAttrUnderline;
    private JComboBox<String> wordFontCombo, wordSizeCombo;
    private JLabel colorSwatch;
    private Color selectedColor = new Color(225, 29, 72);

    public MainFrame() {
        setTitle("ScriptWeaver");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 880);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.setBackground(BG);
        body.setBorder(new EmptyBorder(12, 15, 8, 15));
        body.add(buildFileCard(), BorderLayout.NORTH);
        body.add(buildTopTabs(), BorderLayout.CENTER);

        add(body, BorderLayout.CENTER);
        add(buildBottom(), BorderLayout.SOUTH);
    }

    // ---------- 顶部标题栏 ----------
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ACCENT);
        header.setPreferredSize(new Dimension(0, 56));

        JLabel title = new JLabel("ScriptWeaver");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 18));
        title.setBorder(new EmptyBorder(0, 20, 0, 0));
        header.add(title, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        right.setOpaque(false);
        JLabel langLabel = new JLabel("界面语言：");
        langLabel.setForeground(Color.WHITE);
        langLabel.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 12));

        JComboBox<String> langCombo = new JComboBox<>(
                new String[]{"中文", "English", "日本語", "한국어", "Русский", "Español", "العربية"});
        langCombo.setPreferredSize(new Dimension(120, 28));

        right.add(langLabel);
        right.add(langCombo);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    // ---------- 文件卡片 ----------
    private JPanel buildFileCard() {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(12, 12, 12, 12)));

        JLabel title = new JLabel("选择 Word 文档");
        title.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 13));
        title.setForeground(ACCENT);
        card.add(title, BorderLayout.NORTH);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setBackground(CARD);
        JButton addBtn = new JButton("添加文件");
        JButton clearBtn = new JButton("清空");
        addBtn.addActionListener(e -> onAddFiles());
        clearBtn.addActionListener(e -> onClearFiles());
        btnRow.add(addBtn);
        btnRow.add(clearBtn);

        JPanel outRow = new JPanel(new BorderLayout(5, 0));
        outRow.setBackground(CARD);
        JLabel outLabel = new JLabel("输出位置：");
        JTextField outField = new JTextField();
        outField.setEditable(false);
        outField.setBackground(new Color(249, 250, 251));
        JButton outBtn = new JButton("选择文件夹");
        JButton outClearBtn = new JButton("默认");
        outBtn.addActionListener(e -> onChooseOutput(outField));
        outClearBtn.addActionListener(e -> {
            outputDir = "";
            outField.setText("");
        });

        JPanel outLeft = new JPanel(new BorderLayout(5, 0));
        outLeft.setBackground(CARD);
        outLeft.add(outLabel, BorderLayout.WEST);
        outLeft.add(outField, BorderLayout.CENTER);

        JPanel outRight = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        outRight.setBackground(CARD);
        outRight.add(outBtn);
        outRight.add(outClearBtn);

        outRow.add(outLeft, BorderLayout.CENTER);
        outRow.add(outRight, BorderLayout.EAST);

        fileListModel = new DefaultListModel<>();
        fileList = new JList<>(fileListModel);
        fileList.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 12));
        fileList.setBackground(new Color(249, 250, 251));
        JScrollPane scroll = new JScrollPane(fileList);
        scroll.setPreferredSize(new Dimension(0, 80));
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));

        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.setBackground(CARD);
        top.add(btnRow, BorderLayout.NORTH);
        top.add(outRow, BorderLayout.CENTER);

        card.add(top, BorderLayout.CENTER);
        card.add(scroll, BorderLayout.SOUTH);

        return card;
    }

    // ---------- 顶层两个标签页 ----------
    private JTabbedPane buildTopTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 13));

        tabs.addTab("按语言修改", buildLangPage());
        tabs.addTab("按词汇修改", buildWordPage());

        return tabs;
    }

    // ---------- 语言页 ----------
    private JPanel buildLangPage() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(CARD);

        JTabbedPane inner = new JTabbedPane();
        inner.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 12));

        inner.addTab("东亚（中日韩）", buildEastAsiaPanel());
        inner.addTab("复杂文种（阿/希/俄）", buildCsPanel());
        inner.addTab("西文（英/西/希）", buildAsciiPanel());

        page.add(inner, BorderLayout.CENTER);
        return page;
    }

    // ---------- 东亚面板 ----------
    private JPanel buildEastAsiaPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(CARD);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 10, 6, 10);
        g.anchor = GridBagConstraints.NORTHWEST;
        g.weightx = 0;
        g.weighty = 0;

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBackground(CARD);
        JLabel langTitle = new JLabel("语言：");
        langTitle.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 12));
        left.add(langTitle);

        cbChinese = new JCheckBox("中文");
        cbJapanese = new JCheckBox("日文");
        cbKorean = new JCheckBox("韩文");
        for (JCheckBox cb : new JCheckBox[]{cbChinese, cbJapanese, cbKorean}) {
            cb.setBackground(CARD);
            cb.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 12));
            left.add(cb);
        }
        cbChinese.addActionListener(e -> onCJK(cbChinese));
        cbJapanese.addActionListener(e -> onCJK(cbJapanese));
        cbKorean.addActionListener(e -> onCJK(cbKorean));

        g.gridx = 0; g.gridy = 0; g.gridheight = 4;
        p.add(left, g);

        g.gridheight = 1;
        g.gridx = 1; g.gridy = 0;
        JLabel fLabel = new JLabel("字体：");
        fLabel.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 12));
        p.add(fLabel, g);

        g.gridx = 2;
        eastFontCombo = new JComboBox<>(FONTS_EAST);
        eastFontCombo.setPreferredSize(new Dimension(220, 28));
        p.add(eastFontCombo, g);

        g.gridx = 1; g.gridy = 1;
        JLabel sLabel = new JLabel("字号：");
        sLabel.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 12));
        p.add(sLabel, g);

        g.gridx = 2;
        eastSizeCombo = new JComboBox<>(SIZES);
        eastSizeCombo.setSelectedItem("12");
        eastSizeCombo.setPreferredSize(new Dimension(80, 28));
        p.add(eastSizeCombo, g);

        return p;
    }

    // ---------- 复杂文种面板 ----------
    private JPanel buildCsPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(CARD);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 10, 6, 10);
        g.anchor = GridBagConstraints.NORTHWEST;
        g.weightx = 0;
        g.weighty = 0;

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBackground(CARD);
        JLabel langTitle = new JLabel("语言：");
        langTitle.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 12));
        left.add(langTitle);

        cbArabic = new JCheckBox("阿拉伯语");
        cbHebrew = new JCheckBox("希伯来语");
        cbRussian = new JCheckBox("俄语");
        for (JCheckBox cb : new JCheckBox[]{cbArabic, cbHebrew, cbRussian}) {
            cb.setBackground(CARD);
            cb.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 12));
            left.add(cb);
        }

        g.gridx = 0; g.gridy = 0; g.gridheight = 4;
        p.add(left, g);

        g.gridheight = 1;
        g.gridx = 1; g.gridy = 0;
        JLabel fLabel = new JLabel("字体：");
        fLabel.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 12));
        p.add(fLabel, g);

        g.gridx = 2;
        csFontCombo = new JComboBox<>(FONTS_CS);
        csFontCombo.setPreferredSize(new Dimension(220, 28));
        p.add(csFontCombo, g);

        g.gridx = 1; g.gridy = 1;
        JLabel sLabel = new JLabel("字号：");
        sLabel.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 12));
        p.add(sLabel, g);

        g.gridx = 2;
        csSizeCombo = new JComboBox<>(SIZES);
        csSizeCombo.setSelectedItem("12");
        csSizeCombo.setPreferredSize(new Dimension(80, 28));
        p.add(csSizeCombo, g);

        return p;
    }

    // ---------- 西文面板 ----------
    private JPanel buildAsciiPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(CARD);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 10, 6, 10);
        g.anchor = GridBagConstraints.NORTHWEST;
        g.weightx = 0;
        g.weighty = 0;

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBackground(CARD);
        JLabel langTitle = new JLabel("语言：");
        langTitle.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 12));
        left.add(langTitle);

        cbEnglish = new JCheckBox("英语");
        cbSpanish = new JCheckBox("西班牙语");
        cbGreek = new JCheckBox("希腊语");
        for (JCheckBox cb : new JCheckBox[]{cbEnglish, cbSpanish, cbGreek}) {
            cb.setBackground(CARD);
            cb.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 12));
            left.add(cb);
        }

        g.gridx = 0; g.gridy = 0; g.gridheight = 4;
        p.add(left, g);

        g.gridheight = 1;
        g.gridx = 1; g.gridy = 0;
        JLabel fLabel = new JLabel("字体：");
        fLabel.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 12));
        p.add(fLabel, g);

        g.gridx = 2;
        asciiFontCombo = new JComboBox<>(FONTS_ASCII);
        asciiFontCombo.setPreferredSize(new Dimension(220, 28));
        p.add(asciiFontCombo, g);

        g.gridx = 1; g.gridy = 1;
        JLabel sLabel = new JLabel("字号：");
        sLabel.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 12));
        p.add(sLabel, g);

        g.gridx = 2;
        asciiSizeCombo = new JComboBox<>(SIZES);
        asciiSizeCombo.setSelectedItem("12");
        asciiSizeCombo.setPreferredSize(new Dimension(80, 28));
        p.add(asciiSizeCombo, g);

        return p;
    }

    // ---------- 中日韩互斥 ----------
    private void onCJK(JCheckBox changed) {
        if (adjustingCJK) return;
        adjustingCJK = true;

        JCheckBox[] all = {cbChinese, cbJapanese, cbKorean};
        if (changed.isSelected()) {
            for (JCheckBox cb : all) {
                if (cb != changed) {
                    cb.setSelected(false);
                    cb.setEnabled(false);
                }
            }
        } else {
            boolean anyOther = false;
            for (JCheckBox cb : all) {
                if (cb != changed && cb.isSelected()) anyOther = true;
            }
            if (!anyOther) {
                for (JCheckBox cb : all) {
                    if (cb != changed) cb.setEnabled(true);
                }
            }
        }

        adjustingCJK = false;
    }

    // ---------- 词汇页 ----------
    private JPanel buildWordPage() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(CARD);
        page.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(CARD);

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        row1.setBackground(CARD);
        JLabel wordLabel = new JLabel("要修改的词汇：");
        wordLabel.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 12));
        wordField = new JTextField(30);
        row1.add(wordLabel);
        row1.add(wordField);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        row2.setBackground(CARD);
        JLabel matchLabel = new JLabel("匹配方式：");
        matchLabel.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 12));
        rbExact = new JRadioButton("精确匹配");
        rbContains = new JRadioButton("包含匹配", true);
        rbRegex = new JRadioButton("正则匹配");
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbExact);
        bg.add(rbContains);
        bg.add(rbRegex);
        for (JRadioButton rb : new JRadioButton[]{rbExact, rbContains, rbRegex}) {
            rb.setBackground(CARD);
            rb.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 12));
        }
        row2.add(matchLabel);
        row2.add(rbExact);
        row2.add(rbContains);
        row2.add(rbRegex);

        JPanel attrs = new JPanel();
        attrs.setLayout(new BoxLayout(attrs, BoxLayout.Y_AXIS));
        attrs.setBackground(CARD);
        attrs.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(BORDER), "要修改的属性"),
                new EmptyBorder(8, 12, 8, 12)));

        JPanel rFont = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3));
        rFont.setBackground(CARD);
        cbAttrFont = new JCheckBox("字体");
        cbAttrFont.setBackground(CARD);
        wordFontCombo = new JComboBox<>(WORD_FONTS);
        wordFontCombo.setPreferredSize(new Dimension(200, 26));
        rFont.add(cbAttrFont);
        rFont.add(wordFontCombo);

        JPanel rSize = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3));
        rSize.setBackground(CARD);
        cbAttrSize = new JCheckBox("字号");
        cbAttrSize.setBackground(CARD);
        wordSizeCombo = new JComboBox<>(SIZES);
        wordSizeCombo.setSelectedItem("12");
        wordSizeCombo.setPreferredSize(new Dimension(80, 26));
        rSize.add(cbAttrSize);
        rSize.add(wordSizeCombo);

        JPanel rColor = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 3));
        rColor.setBackground(CARD);
        cbAttrColor = new JCheckBox("颜色");
        cbAttrColor.setBackground(CARD);
        rColor.add(cbAttrColor);

        for (String[] pc : PRESET_COLORS) {
            final Color c = Color.decode(pc[1]);
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(22, 22));
            b.setBackground(c);
            b.setBorder(BorderFactory.createLineBorder(BORDER));
            b.setFocusPainted(false);
            b.setToolTipText(pc[0]);
            b.addActionListener(e -> {
                selectedColor = c;
                colorSwatch.setBackground(c);
            });
            rColor.add(b);
        }

        colorSwatch = new JLabel();
        colorSwatch.setOpaque(true);
        colorSwatch.setBackground(selectedColor);
        colorSwatch.setPreferredSize(new Dimension(28, 22));
        colorSwatch.setBorder(BorderFactory.createLineBorder(BORDER));
        rColor.add(Box.createHorizontalStrut(10));
        rColor.add(colorSwatch);

        JButton customColorBtn = new JButton("自定义…");
        customColorBtn.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "选择颜色", selectedColor);
            if (c != null) {
                selectedColor = c;
                colorSwatch.setBackground(c);
            }
        });
        rColor.add(customColorBtn);

        JPanel rStyle = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 3));
        rStyle.setBackground(CARD);
        cbAttrBold = new JCheckBox("加粗");
        cbAttrItalic = new JCheckBox("斜体");
        cbAttrUnderline = new JCheckBox("下划线");
        for (JCheckBox cb : new JCheckBox[]{cbAttrBold, cbAttrItalic, cbAttrUnderline}) {
            cb.setBackground(CARD);
            rStyle.add(cb);
        }

        attrs.add(rFont);
        attrs.add(rSize);
        attrs.add(rColor);
        attrs.add(rStyle);

        JLabel hint = new JLabel("勾选要修改的属性，不勾的项保持原样。");
        hint.setForeground(TEXT_MUTED);
        hint.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));

        top.add(row1);
        top.add(row2);
        top.add(Box.createVerticalStrut(10));
        top.add(attrs);
        top.add(Box.createVerticalStrut(8));
        top.add(hint);

        page.add(top, BorderLayout.NORTH);
        return page;
    }

    // ---------- 底部按钮 + 状态栏 ----------
    private JPanel buildBottom() {
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(BG);
        bottom.setPreferredSize(new Dimension(0, 80));

        JPanel action = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 12));
        action.setBackground(BG);
        JButton startBtn = new JButton("开始批量处理");
        startBtn.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 13));
        startBtn.setForeground(Color.WHITE);
        startBtn.setBackground(ACCENT);
        startBtn.setOpaque(true);
        startBtn.setContentAreaFilled(true);
        startBtn.setBorderPainted(false);
        startBtn.setFocusPainted(false);
        startBtn.setPreferredSize(new Dimension(160, 36));
        JButton quitBtn = new JButton("退出");
        startBtn.addActionListener(e -> onStartBatch());
        quitBtn.addActionListener(e -> System.exit(0));
        action.add(startBtn);
        action.add(quitBtn);

        statusLabel = new JLabel("  就绪");
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(229, 231, 235));
        statusLabel.setForeground(TEXT_MUTED);
        statusLabel.setPreferredSize(new Dimension(0, 28));

        bottom.add(action, BorderLayout.CENTER);
        bottom.add(statusLabel, BorderLayout.SOUTH);

        return bottom;
    }

    // ---------- 事件方法 ----------
    private void onAddFiles() {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Word 文档 (*.docx)", "docx"));
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            for (File f : chooser.getSelectedFiles()) {
                String path = f.getAbsolutePath();
                if (!files.contains(path)) {
                    files.add(path);
                    fileListModel.addElement(path);
                }
            }
        }
    }

    private void onClearFiles() {
        files.clear();
        fileListModel.clear();
    }

    private void onChooseOutput(JTextField outField) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            outputDir = chooser.getSelectedFile().getAbsolutePath();
            outField.setText(outputDir);
        }
    }

    private void onStartBatch() {
        if (files.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先添加至少一个 Word 文档。",
                    "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 收集语言页设置
        List<String[]> langConfigs = new ArrayList<>();
        // 东亚
        if (cbChinese.isSelected())  langConfigs.add(new String[]{"eastAsia", (String) eastFontCombo.getSelectedItem(), (String) eastSizeCombo.getSelectedItem()});
        if (cbJapanese.isSelected()) langConfigs.add(new String[]{"eastAsia", (String) eastFontCombo.getSelectedItem(), (String) eastSizeCombo.getSelectedItem()});
        if (cbKorean.isSelected())   langConfigs.add(new String[]{"eastAsia", (String) eastFontCombo.getSelectedItem(), (String) eastSizeCombo.getSelectedItem()});
        // 复杂文种
        if (cbArabic.isSelected())   langConfigs.add(new String[]{"cs", (String) csFontCombo.getSelectedItem(), (String) csSizeCombo.getSelectedItem()});
        if (cbHebrew.isSelected())   langConfigs.add(new String[]{"cs", (String) csFontCombo.getSelectedItem(), (String) csSizeCombo.getSelectedItem()});
        if (cbRussian.isSelected())  langConfigs.add(new String[]{"cs", (String) csFontCombo.getSelectedItem(), (String) csSizeCombo.getSelectedItem()});
        // 西文
        if (cbEnglish.isSelected())  langConfigs.add(new String[]{"ascii", (String) asciiFontCombo.getSelectedItem(), (String) asciiSizeCombo.getSelectedItem()});
        if (cbSpanish.isSelected())  langConfigs.add(new String[]{"ascii", (String) asciiFontCombo.getSelectedItem(), (String) asciiSizeCombo.getSelectedItem()});
        if (cbGreek.isSelected())    langConfigs.add(new String[]{"ascii", (String) asciiFontCombo.getSelectedItem(), (String) asciiSizeCombo.getSelectedItem()});

        // 收集词汇页设置
        String word = wordField.getText().trim();
        boolean hasWord = !word.isEmpty() && hasAnyAttrChecked();

        if (langConfigs.isEmpty() && !hasWord) {
            JOptionPane.showMessageDialog(this, "请至少在一个标签页里设置修改内容。",
                    "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 进度条
        JProgressBar progress = new JProgressBar(0, files.size());
        progress.setStringPainted(true);
        JDialog progressDialog = new JDialog(this, "处理中", false);
        progressDialog.setLayout(new BorderLayout());
        progressDialog.add(progress, BorderLayout.CENTER);
        progressDialog.setSize(360, 80);
        progressDialog.setLocationRelativeTo(this);

        final int[] totalCount = {0};
        final int totalFiles = files.size();

        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                for (int i = 0; i < totalFiles; i++) {
                    String path = files.get(i);
                    try {
                        int cnt = processOneFile(path, langConfigs, word, hasWord);
                        totalCount[0] += cnt;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    publish(i + 1);
                }
                return null;
            }

            @Override
            protected void process(List<Integer> chunks) {
                int last = chunks.get(chunks.size() - 1);
                progress.setValue(last);
                statusLabel.setText("  正在处理 " + last + "/" + totalFiles + "…");
            }

            @Override
            protected void done() {
                progressDialog.dispose();
                statusLabel.setText("  完成：共 " + totalFiles + " 个文件，" + totalCount[0] + " 处修改");
                JOptionPane.showMessageDialog(MainFrame.this,
                        "处理完成！\n\n共处理 " + totalFiles + " 个文件。\n修改 " + totalCount[0] + " 处。\n\n新文件已保存。",
                        "成功", JOptionPane.INFORMATION_MESSAGE);
            }
        };

        worker.execute();
        progressDialog.setVisible(true);
    }

    private boolean hasAnyAttrChecked() {
        return cbAttrFont.isSelected() || cbAttrSize.isSelected() || cbAttrColor.isSelected()
                || cbAttrBold.isSelected() || cbAttrItalic.isSelected() || cbAttrUnderline.isSelected();
    }

    private int processOneFile(String path, List<String[]> langConfigs, String word, boolean hasWord) throws Exception {
        try (java.io.FileInputStream fis = new java.io.FileInputStream(path);
             org.apache.poi.xwpf.usermodel.XWPFDocument doc =
                     new org.apache.poi.xwpf.usermodel.XWPFDocument(fis)) {

            int count = 0;

            // ---- 第一步：语言页设置（按语言拆分 run）----
            if (!langConfigs.isEmpty()) {
                java.util.List<org.apache.poi.xwpf.usermodel.XWPFParagraph> allParas =
                        FontChanger.allParagraphs(doc);

                for (org.apache.poi.xwpf.usermodel.XWPFParagraph para : allParas) {
                    java.util.List<org.apache.poi.xwpf.usermodel.XWPFRun> runs =
                            new java.util.ArrayList<>(para.getRuns());
                    for (org.apache.poi.xwpf.usermodel.XWPFRun run : runs) {
                        String text = run.text();
                        if (text == null || text.isEmpty()) continue;

                        // 先看这个 run 有没有命中任何目标语言
                        boolean hitAny = false;
                        for (int i = 0; i < text.length() && !hitAny; i++) {
                            char c = text.charAt(i);
                            for (String[] cfg : langConfigs) {
                                String attr = cfg[0];
                                if (attr.equals("eastAsia") && FontChanger.isCJK(c)) { hitAny = true; break; }
                                if (attr.equals("cs") && (FontChanger.isArabic(c) || FontChanger.isHebrew(c) || FontChanger.isCyrillic(c))) { hitAny = true; break; }
                                if (attr.equals("ascii") && (FontChanger.isLatin(c) || FontChanger.isGreek(c))) { hitAny = true; break; }
                            }
                        }
                        if (!hitAny) continue;

                        // 拆分 run
                        java.util.List<org.apache.poi.xwpf.usermodel.XWPFRun> segments;
                        try {
                            segments = FontChanger.splitRunByLanguage(run, para, langConfigs);
                        } catch (Exception ex) {
                            segments = new java.util.ArrayList<>();
                            segments.add(run);
                        }

                        // 对每一小段分别设字体和字号
                        for (org.apache.poi.xwpf.usermodel.XWPFRun segRun : segments) {
                            String segText = segRun.text();
                            if (segText == null || segText.isEmpty()) continue;

                            // 找出这一小段属于哪一类
                            String matchedAttr = null;
                            String matchedFont = null;
                            int matchedSize = 12;

                            for (int i = 0; i < segText.length(); i++) {
                                char c = segText.charAt(i);
                                for (String[] cfg : langConfigs) {
                                    String attr = cfg[0];
                                    boolean hit = false;
                                    if (attr.equals("eastAsia") && FontChanger.isCJK(c)) hit = true;
                                    else if (attr.equals("cs") && (FontChanger.isArabic(c) || FontChanger.isHebrew(c) || FontChanger.isCyrillic(c))) hit = true;
                                    else if (attr.equals("ascii") && (FontChanger.isLatin(c) || FontChanger.isGreek(c))) hit = true;
                                    if (hit) {
                                        matchedAttr = attr;
                                        matchedFont = cfg[1];
                                        try { matchedSize = Integer.parseInt(cfg[2]); } catch (Exception ignored) {}
                                        break;
                                    }
                                }
                                if (matchedAttr != null) break;
                            }

                            if (matchedAttr == null) continue;

                            if (matchedAttr.equals("eastAsia")) FontChanger.setEastAsiaFont(segRun, matchedFont);
                            else if (matchedAttr.equals("cs")) FontChanger.setCsFont(segRun, matchedFont);
                            else if (matchedAttr.equals("ascii")) FontChanger.setAsciiFont(segRun, matchedFont);

                            segRun.setFontSize(matchedSize);
                            count++;
                        }
                    }
                }
            }

            // ---- 第二步：词汇页设置 ----
            if (hasWord) {
                // 先简单实现：只处理完全在单个 run 里的词，跨 run 暂不处理
                String mode = rbExact.isSelected() ? "exact" : rbContains.isSelected() ? "contains" : "regex";
                List<org.apache.poi.xwpf.usermodel.XWPFRun> runs = FontChanger.allRuns(doc);
                for (org.apache.poi.xwpf.usermodel.XWPFRun run : runs) {
                    String text = run.text();
                    if (text == null || !text.contains(word)) continue;
                    if (mode.equals("exact") && !text.equals(word)) continue;
                    if (mode.equals("regex") && !text.matches(".*" + word + ".*")) continue;

                    applyWordAttrs(run);
                    count++;
                }
            }

            // ---- 保存 ----
            java.io.File src = new java.io.File(path);
            String baseName = src.getName().replaceAll("\\.docx$", "");
            java.io.File outDirFile = outputDir.isEmpty() ? src.getParentFile() : new java.io.File(outputDir);
            java.io.File outFile = new java.io.File(outDirFile, baseName + "_已修改.docx");

            int n = 1;
            while (outFile.exists()) {
                outFile = new java.io.File(outDirFile, baseName + "_已修改_" + n + ".docx");
                n++;
            }

            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(outFile)) {
                doc.write(fos);
            }

            return count;
        }
    }

    private void applyWordAttrs(org.apache.poi.xwpf.usermodel.XWPFRun run) {
        if (cbAttrFont.isSelected()) {
            String font = (String) wordFontCombo.getSelectedItem();
            FontChanger.setEastAsiaFont(run, font);
            FontChanger.setAsciiFont(run, font);
            FontChanger.setCsFont(run, font);
        }
        if (cbAttrSize.isSelected()) {
            try {
                run.setFontSize(Integer.parseInt((String) wordSizeCombo.getSelectedItem()));
            } catch (Exception ignored) {}
        }
        if (cbAttrColor.isSelected()) {
            run.setColor(String.format("%02X%02X%02X",
                    selectedColor.getRed(), selectedColor.getGreen(), selectedColor.getBlue()));
        }
        if (cbAttrBold.isSelected()) run.setBold(true);
        if (cbAttrItalic.isSelected()) run.setItalic(true);
        if (cbAttrUnderline.isSelected()) {
            run.setUnderline(org.apache.poi.xwpf.usermodel.UnderlinePatterns.SINGLE);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}