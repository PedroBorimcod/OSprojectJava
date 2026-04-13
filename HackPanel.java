import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.security.MessageDigest;
import java.io.*;

public class HackPanel extends JFrame {

    // ===== CORES - TEMA LIGHT PROFISSIONAL =====
    private static final Color BG_MAIN = new Color(245, 247, 250);
    private static final Color BG_SIDEBAR = new Color(15, 15, 35);
    private static final Color BG_SIDEBAR_HOVER = new Color(30, 30, 55);
    private static final Color BG_CARD = Color.WHITE;
    private static final Color BG_INPUT = Color.WHITE;
    private static final Color BORDER = new Color(220, 225, 235);
    private static final Color ACCENT = new Color(79, 70, 229);
    private static final Color SUCCESS = new Color(16, 185, 129);
    private static final Color WARNING = new Color(245, 158, 11);
    private static final Color DANGER = new Color(239, 68, 68);
    private static final Color INFO = new Color(59, 130, 246);
    private static final Color PURPLE = new Color(139, 92, 246);
    private static final Color CYAN = new Color(6, 182, 212);
    private static final Color ORANGE = new Color(249, 115, 22);
    private static final Color TEXT_PRIMARY = new Color(15, 15, 35);
    private static final Color TEXT_SECONDARY = new Color(100, 110, 130);
    private static final Color TEXT_MUTED = new Color(150, 155, 170);

    // ===== MODELO DE DADOS =====
    static class OS {
        String id, dataAbertura, dataConclusao, cliente, telefone, email;
        String servico, tecnico, equipamento, descricao;
        String status, prioridade, valor, garantia;

        OS(String cliente, String telefone, String email, String servico,
           String tecnico, String equipamento, String descricao,
           String status, String prioridade, String valor, String garantia) {
            this.id = String.format("OS-%04d", System.currentTimeMillis() % 10000);
            this.dataAbertura = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
            this.dataConclusao = "";
            this.cliente = cliente;
            this.telefone = telefone;
            this.email = email;
            this.servico = servico;
            this.tecnico = tecnico;
            this.equipamento = equipamento;
            this.descricao = descricao;
            this.status = status;
            this.prioridade = prioridade;
            this.valor = valor;
            this.garantia = garantia;
        }

        Object[] toRow() {
            return new Object[]{id, dataAbertura, dataConclusao, cliente, servico, tecnico, status, prioridade, valor, garantia};
        }
    }

    // ===== COMPONENTES =====
    private JPanel contentPanel, sidebar, headerPanel;
    private CardLayout cardLayout;
    private JTable osTable, finTable, clientTable, techTable, userTable;
    private DefaultTableModel osModel, finModel, clientModel, techModel, userModel;
    private JTextArea detailsArea, activityLog;
    private JTextField searchOS;
    private JComboBox<String> filterStatus, filterPriority;
    private ArrayList<OS> osList = new ArrayList<>();
    private String currentUser = "";
    private boolean isAdmin = false;
    private JLabel headerTitle;

    private static final String[] OS_COLS = {"OS#", "Abertura", "Conclusão", "Cliente", "Serviço", "Técnico", "Status", "Prioridade", "Valor", "Garantia"};
    private static final String[] CLIENT_COLS = {"ID", "Nome", "Telefone", "Email", "Desde"};
    private static final String[] TECH_COLS = {"ID", "Nome", "Especialidade", "Ativas", "Finalizadas"};
    private static final String[] FIN_COLS = {"OS#", "Cliente", "Serviço", "Valor", "Pgto", "Conclusão"};
    private static final String[] USER_COLS = {"Usuário", "Nome", "Nível", "Status"};
    private static final String[] STATUS = {"ABERTA", "EM ANDAMENTO", "AGUARDANDO PEÇA", "FINALIZADA"};
    private static final String[] PRIORIDADES = {"BAIXA", "MÉDIA", "ALTA", "URGENTE"};
    private static final String[] EQUIPS = {"Notebook", "Desktop", "Impressora", "Servidor", "Rede", "Smartphone", "Outro"};

    // ===== CONSTRUTOR =====
    public HackPanel(String user, boolean admin) {
        this.currentUser = user;
        this.isAdmin = admin;
        initFolders();
        setupFrame();
        loadSampleData();
    }

    private void initFolders() {
        new File(System.getProperty("user.home") + "/.techsuite").mkdirs();
    }

    // ===== SETUP DO FRAME =====
    private void setupFrame() {
        setTitle("TechSuite Pro - " + currentUser);
        setSize(1450, 880);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_MAIN);

        sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        headerPanel = createHeader();
        add(headerPanel, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BG_MAIN);

        contentPanel.add("dashboard", buildDashboard());
        contentPanel.add("ordens", buildOSPanel());
        contentPanel.add("clientes", buildClientPanel());
        contentPanel.add("equipe", buildTechPanel());
        contentPanel.add("financeiro", buildFinancePanel());
        contentPanel.add("relatorios", buildReportPanel());
        if (isAdmin) contentPanel.add("admin", buildAdminPanel());

        add(contentPanel, BorderLayout.CENTER);
    }

    // ===== SIDEBAR =====
    private JPanel createSidebar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_SIDEBAR);
        panel.setPreferredSize(new Dimension(250, 0));

        // Logo
        JPanel logo = new JPanel(new BorderLayout());
        logo.setBackground(BG_SIDEBAR);
        logo.setBorder(BorderFactory.createEmptyBorder(25, 20, 25, 20));

        JLabel title = new JLabel(" TechSuite Pro");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        logo.add(title, BorderLayout.CENTER);

        JLabel sub = new JLabel(" Gestão de Serviços v2.0");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(new Color(140, 140, 190));
        logo.add(sub, BorderLayout.SOUTH);
        panel.add(logo, BorderLayout.NORTH);

        // Menu
        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBackground(BG_SIDEBAR);
        menu.setBorder(BorderFactory.createEmptyBorder(15, 12, 15, 12));

        addMenuItem(menu, "📊  Dashboard", true, e -> { showPage("dashboard"); updateHeader("Dashboard"); });
        addMenuItem(menu, "📋  Ordens de Serviço", false, e -> { showPage("ordens"); updateHeader("Ordens de Serviço"); });
        addMenuItem(menu, "👥  Clientes", false, e -> { showPage("clientes"); updateHeader("Clientes"); });
        addMenuItem(menu, "🔧  Equipe Técnica", false, e -> { showPage("equipe"); updateHeader("Equipe Técnica"); });
        addMenuItem(menu, "💰  Financeiro", false, e -> { showPage("financeiro"); updateHeader("Financeiro"); });
        addMenuItem(menu, "📈  Relatórios", false, e -> { showPage("relatorios"); updateHeader("Relatórios"); });
        if (isAdmin) {
            menu.add(Box.createVerticalStrut(8));
            addMenuItem(menu, "⚙️  Administração", false, e -> { showPage("admin"); updateHeader("Administração"); });
        }
        panel.add(menu, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(10, 10, 25));
        footer.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel uLbl = new JLabel("  " + currentUser);
        uLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        uLbl.setForeground(Color.WHITE);
        footer.add(uLbl, BorderLayout.CENTER);

        JLabel rLbl = new JLabel("  " + (isAdmin ? "Administrador" : "Usuário"));
        rLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        rLbl.setForeground(new Color(140, 140, 190));
        footer.add(rLbl, BorderLayout.SOUTH);
        panel.add(footer, BorderLayout.SOUTH);

        return panel;
    }

    private void addMenuItem(JPanel menu, String text, boolean active, ActionListener al) {
        JPanel item = new JPanel(new BorderLayout());
        item.setBackground(active ? BG_SIDEBAR_HOVER : BG_SIDEBAR);
        item.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 3, 0, 0, active ? ACCENT : new Color(0,0,0,0)),
            BorderFactory.createEmptyBorder(13, 18, 13, 18)));
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));
        item.setMaximumSize(new Dimension(230, 46));

        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(active ? Color.WHITE : new Color(170, 170, 200));
        item.add(lbl, BorderLayout.CENTER);

        item.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { if (!active) item.setBackground(BG_SIDEBAR_HOVER); }
            public void mouseExited(MouseEvent e) { if (!active) item.setBackground(BG_SIDEBAR); }
            public void mouseClicked(MouseEvent e) { al.actionPerformed(null); }
        });

        menu.add(item);
        menu.add(Box.createVerticalStrut(2));
    }

    // ===== HEADER =====
    private JPanel createHeader() {
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        headerPanel.setPreferredSize(new Dimension(0, 60));

        headerTitle = new JLabel("  Dashboard");
        headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerTitle.setForeground(TEXT_PRIMARY);
        headerPanel.add(headerTitle, BorderLayout.CENTER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        right.setBackground(Color.WHITE);

        JLabel dateLbl = new JLabel(new SimpleDateFormat("dd/MM/yyyy  HH:mm").format(new Date()));
        dateLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateLbl.setForeground(TEXT_SECONDARY);
        right.add(dateLbl);

        JButton logoutBtn = new JButton("  Sair");
        styleBtn(logoutBtn, DANGER);
        logoutBtn.setPreferredSize(new Dimension(80, 32));
        logoutBtn.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Deseja sair do sistema?", "Logout", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
                System.exit(0);
        });
        right.add(logoutBtn);

        headerPanel.add(right, BorderLayout.EAST);
        return headerPanel;
    }

    private void updateHeader(String title) {
        headerTitle.setText("  " + title);
    }

    private void showPage(String page) {
        cardLayout.show(contentPanel, page);
    }

    // ===== DASHBOARD =====
    private JPanel buildDashboard() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(BG_MAIN);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // Welcome
        JPanel welcome = new JPanel(new BorderLayout(5, 5));
        welcome.setBackground(BG_MAIN);

        JLabel wLbl = new JLabel("Bom dia, " + currentUser + "! 👋");
        wLbl.setFont(new Font("Segoe UI", Font.BOLD, 24));
        wLbl.setForeground(TEXT_PRIMARY);
        welcome.add(wLbl, BorderLayout.CENTER);

        JLabel dLbl = new JLabel(new SimpleDateFormat("EEEE, dd 'de' MMMM 'de' yyyy").format(new Date()));
        dLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dLbl.setForeground(TEXT_SECONDARY);
        welcome.add(dLbl, BorderLayout.SOUTH);
        panel.add(welcome, BorderLayout.NORTH);

        // Stats
        JPanel statsPanel = new JPanel(new GridLayout(1, 5, 15, 0));
        statsPanel.setBackground(BG_MAIN);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JPanel s1 = statCard("OS Abertas", String.valueOf(countOS("ABERTA")), WARNING, "📋");
        JPanel s2 = statCard("Em Andamento", String.valueOf(countOS("EM ANDAMENTO")), INFO, "🔧");
        JPanel s3 = statCard("Finalizadas Hoje", String.valueOf(countOS("FINALIZADA")), SUCCESS, "✅");
        JPanel s4 = statCard("Faturamento", totalFaturamento(), SUCCESS, "💰");
        JPanel s5 = statCard("Clientes", String.valueOf(clientModel.getRowCount()), PURPLE, "👥");

        statsPanel.add(s1); statsPanel.add(s2); statsPanel.add(s3); statsPanel.add(s4); statsPanel.add(s5);
        panel.add(statsPanel, BorderLayout.CENTER);

        // Bottom
        JPanel bottom = new JPanel(new GridLayout(1, 2, 20, 0));
        bottom.setBackground(BG_MAIN);

        JPanel actions = card("Ações Rápidas", ACCENT);
        JPanel actGrid = new JPanel(new GridLayout(2, 2, 10, 10));
        actGrid.setBackground(BG_CARD);
        actGrid.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        actGrid.add(qBtn("➕ Nova OS", ACCENT, e -> { showPage("ordens"); openOSDialog(); }));
        actGrid.add(qBtn("👤 Novo Cliente", SUCCESS, e -> { showPage("clientes"); addClient(); }));
        actGrid.add(qBtn("🔧 Novo Técnico", PURPLE, e -> { showPage("equipe"); addTech(); }));
        actGrid.add(qBtn("📊 Relatório", CYAN, e -> { showPage("relatorios"); }));
        actions.add(actGrid, BorderLayout.CENTER);

        JPanel act = card("Atividades Recentes", SUCCESS);
        activityLog = new JTextArea();
        activityLog.setFont(new Font("Consolas", Font.PLAIN, 11));
        activityLog.setForeground(TEXT_SECONDARY);
        activityLog.setBackground(Color.WHITE);
        activityLog.setEditable(false);
        activityLog.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        act.add(new JScrollPane(activityLog), BorderLayout.CENTER);

        bottom.add(actions);
        bottom.add(act);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel statCard(String title, String value, Color accent, String icon) {
        JPanel card = new JPanel(new BorderLayout(10, 5));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(new CardBorder(accent), BorderFactory.createEmptyBorder(18, 18, 18, 18)));

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(BG_CARD);
        JLabel ic = new JLabel(icon);
        ic.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        top.add(ic, BorderLayout.WEST);

        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 22));
        v.setForeground(accent);
        v.setHorizontalAlignment(SwingConstants.RIGHT);
        top.add(v, BorderLayout.EAST);
        card.add(top, BorderLayout.CENTER);

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.setForeground(TEXT_SECONDARY);
        card.add(t, BorderLayout.SOUTH);

        return card;
    }

    private JPanel card(String title, Color accent) {
        JPanel c = new JPanel(new BorderLayout(0, 10));
        c.setBackground(BG_CARD);
        c.setBorder(BorderFactory.createCompoundBorder(new CardBorder(accent), BorderFactory.createEmptyBorder(15, 18, 15, 18)));

        JLabel t = new JLabel(" " + title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 14));
        t.setForeground(accent);
        c.add(t, BorderLayout.NORTH);
        return c;
    }

    private JButton qBtn(String text, Color accent, ActionListener al) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setForeground(Color.WHITE);
        b.setBackground(accent);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addActionListener(al);
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(accent.brighter()); }
            public void mouseExited(MouseEvent e) { b.setBackground(accent); }
        });
        return b;
    }

    // ===== ORDENS DE SERVIÇO =====
    private JPanel buildOSPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_MAIN);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("📋 Gerenciar Ordens de Serviço");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_PRIMARY);
        panel.add(title, BorderLayout.NORTH);

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(BorderFactory.createCompoundBorder(new CardBorder(ACCENT), BorderFactory.createEmptyBorder(12, 15, 12, 15)));

        toolbar.add(lbl("🔍 Buscar:"));
        searchOS = new JTextField(15);
        searchOS.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchOS.setBackground(BG_INPUT);
        searchOS.setBorder(BorderFactory.createLineBorder(BORDER));
        searchOS.addKeyListener(new KeyAdapter() { public void keyReleased(KeyEvent e) { filterOS(); }});
        toolbar.add(searchOS);

        toolbar.add(Box.createHorizontalStrut(8));
        toolbar.add(lbl("Status:"));
        filterStatus = new JComboBox<>(new String[]{"TODOS", "ABERTA", "EM ANDAMENTO", "AGUARDANDO PEÇA", "FINALIZADA"});
        filterStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        filterStatus.setBackground(Color.WHITE);
        filterStatus.addActionListener(e -> filterOS());
        toolbar.add(filterStatus);

        toolbar.add(Box.createHorizontalStrut(8));
        toolbar.add(lbl("Prioridade:"));
        filterPriority = new JComboBox<>(new String[]{"TODAS", "BAIXA", "MÉDIA", "ALTA", "URGENTE"});
        filterPriority.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        filterPriority.setBackground(Color.WHITE);
        filterPriority.addActionListener(e -> filterOS());
        toolbar.add(filterPriority);

        JButton newBtn = btnPrimary("➕ Nova OS", ACCENT);
        newBtn.addActionListener(e -> openOSDialog());
        toolbar.add(newBtn);

        panel.add(toolbar, BorderLayout.CENTER);

        // Table
        osModel = new DefaultTableModel(OS_COLS, 0) { public boolean isCellEditable(int r, int c) { return false; }};
        osTable = createTable(osModel);
        setColWidths(osTable, new int[]{75, 105, 105, 130, 140, 100, 100, 75, 75, 65});
        osTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && osTable.getSelectedRow() != -1) showDetails(osTable.getSelectedRow());
        });

        JScrollPane scroll = new JScrollPane(osTable);
        scroll.setBorder(BorderFactory.createCompoundBorder(new CardBorder(ACCENT), null));

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setBackground(BG_MAIN);

        // Details
        JPanel det = card("📄 Detalhes da Ordem", ACCENT);
        detailsArea = new JTextArea();
        detailsArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        detailsArea.setForeground(TEXT_SECONDARY);
        detailsArea.setBackground(Color.WHITE);
        detailsArea.setEditable(false);
        detailsArea.setLineWrap(true);
        detailsArea.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        det.add(new JScrollPane(detailsArea), BorderLayout.CENTER);
        det.setPreferredSize(new Dimension(0, 110));

        center.add(scroll, BorderLayout.CENTER);
        center.add(det, BorderLayout.SOUTH);
        panel.add(center, BorderLayout.CENTER);

        // Actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBackground(BG_MAIN);
        actions.add(btnSecondary("✏️ Editar", WARNING, e -> editSelectedOS()));
        actions.add(btnSecondary("✅ Finalizar", SUCCESS, e -> finalizeSelectedOS()));
        actions.add(btnSecondary("🗑️ Excluir", DANGER, e -> deleteSelectedOS()));
        actions.add(btnSecondary("🖨️ Imprimir", INFO, e -> printSelectedOS()));
        actions.add(btnSecondary("📤 Exportar CSV", PURPLE, e -> exportData(osModel, OS_COLS, "ordens.csv")));
        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    // ===== CLIENTES =====
    private JPanel buildClientPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_MAIN);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("👥 Gestão de Clientes");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_PRIMARY);
        panel.add(title, BorderLayout.NORTH);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(new CardBorder(SUCCESS), BorderFactory.createEmptyBorder(12, 15, 12, 15)));

        JButton addBtn = btnPrimary("➕ Novo Cliente", SUCCESS);
        addBtn.addActionListener(e -> addClient());
        topBar.add(addBtn, BorderLayout.EAST);
        panel.add(topBar, BorderLayout.CENTER);

        clientModel = new DefaultTableModel(CLIENT_COLS, 0) { public boolean isCellEditable(int r, int c) { return false; }};
        panel.add(new JScrollPane(createTable(clientModel)), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBackground(BG_MAIN);
        actions.add(btnSecondary("🗑️ Excluir", DANGER, e -> deleteClient()));
        actions.add(btnSecondary("📤 Exportar", PURPLE, e -> exportData(clientModel, CLIENT_COLS, "clientes.csv")));
        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    // ===== EQUIPE =====
    private JPanel buildTechPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_MAIN);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("🔧 Equipe Técnica");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_PRIMARY);
        panel.add(title, BorderLayout.NORTH);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(new CardBorder(PURPLE), BorderFactory.createEmptyBorder(12, 15, 12, 15)));

        JButton addBtn = btnPrimary("➕ Novo Técnico", PURPLE);
        addBtn.addActionListener(e -> addTech());
        topBar.add(addBtn, BorderLayout.EAST);
        panel.add(topBar, BorderLayout.CENTER);

        techModel = new DefaultTableModel(TECH_COLS, 0) { public boolean isCellEditable(int r, int c) { return false; }};
        panel.add(new JScrollPane(createTable(techModel)), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBackground(BG_MAIN);
        actions.add(btnSecondary("🗑️ Excluir", DANGER, e -> deleteTech()));
        actions.add(btnSecondary("📤 Exportar", CYAN, e -> exportData(techModel, TECH_COLS, "equipe.csv")));
        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    // ===== FINANCEIRO =====
    private JPanel buildFinancePanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_MAIN);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("💰 Painel Financeiro");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_PRIMARY);
        panel.add(title, BorderLayout.NORTH);

        JPanel stats = new JPanel(new GridLayout(1, 4, 12, 0));
        stats.setBackground(BG_MAIN);
        stats.add(statCard("Receita Total", totalFaturamento(), SUCCESS, "💵"));
        stats.add(statCard("A Receber", totalPendente(), WARNING, "⏳"));
        stats.add(statCard("Recebido", totalRecebido(), INFO, "✅"));
        stats.add(statCard("Ticket Médio", ticketMedio(), PURPLE, "📊"));
        panel.add(stats, BorderLayout.CENTER);

        finModel = new DefaultTableModel(FIN_COLS, 0) { public boolean isCellEditable(int r, int c) { return false; }};
        finTable = createTable(finModel);
        JScrollPane scroll = new JScrollPane(finTable);
        scroll.setBorder(BorderFactory.createCompoundBorder(new CardBorder(INFO), null));
        panel.add(scroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBackground(BG_MAIN);
        actions.add(btnSecondary("📊 Relatório", SUCCESS, e -> showFinanceReport()));
        actions.add(btnSecondary("📤 Exportar", PURPLE, e -> exportData(finModel, FIN_COLS, "financeiro.csv")));
        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    // ===== RELATÓRIOS =====
    private JPanel buildReportPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 3, 15, 15));
        panel.setBackground(BG_MAIN);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        panel.add(reportCard("📋 Relatório de Ordens", "Todas as ordens com detalhes e filtros", ACCENT, e -> showFullReport()));
        panel.add(reportCard("👥 Relatório de Clientes", "Lista completa de clientes cadastrados", SUCCESS, e -> showClientReport()));
        panel.add(reportCard("🔧 Relatório da Equipe", "Desempenho dos técnicos", WARNING, e -> showTechReport()));
        panel.add(reportCard("💰 Relatório Financeiro", "Receitas, despesas e lucros", PURPLE, e -> showFinanceReport()));
        panel.add(reportCard("💾 Backup de Dados", "Backup completo do sistema", ORANGE, e -> doBackup()));
        panel.add(reportCard("ℹ️ Info do Sistema", "Dados do sistema e hardware", CYAN, e -> showSystemInfo()));

        return panel;
    }

    private JPanel reportCard(String title, String desc, Color accent, ActionListener al) {
        JPanel c = new JPanel(new BorderLayout(12, 10));
        c.setBackground(BG_CARD);
        c.setBorder(BorderFactory.createCompoundBorder(new CardBorder(accent), BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        c.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 15));
        t.setForeground(TEXT_PRIMARY);
        c.add(t, BorderLayout.NORTH);

        JLabel d = new JLabel("<html><body style='width:260px'><span style='color:#646E82'>" + desc + "</span></body></html>");
        d.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        c.add(d, BorderLayout.CENTER);

        JButton b = new JButton("Abrir");
        styleBtn(b, accent);
        b.addActionListener(al);
        c.add(b, BorderLayout.SOUTH);
        return c;
    }

    // ===== ADMIN =====
    private JPanel buildAdminPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_MAIN);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("⚙️ Administração de Usuários");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_PRIMARY);
        panel.add(title, BorderLayout.NORTH);

        userModel = new DefaultTableModel(USER_COLS, 0) { public boolean isCellEditable(int r, int c) { return false; }};
        userTable = createTable(userModel);
        panel.add(new JScrollPane(userTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBackground(BG_MAIN);
        actions.add(btnPrimary("➕ Novo Usuário", ACCENT, e -> addUserDialog()));
        actions.add(btnSecondary("🔄 Atualizar", INFO, e -> loadUsers()));
        panel.add(actions, BorderLayout.SOUTH);

        loadUsers();
        return panel;
    }

    // ===== DIÁLOGO DE OS =====
    private void openOSDialog() {
        JDialog dlg = new JDialog(this, "➕ Nova Ordem de Serviço", true);
        dlg.setSize(580, 680);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(Color.WHITE);
        dlg.setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ACCENT);
        header.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        JLabel hLbl = new JLabel("➕ Nova Ordem de Serviço");
        hLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        hLbl.setForeground(Color.WHITE);
        header.add(hLbl, BorderLayout.CENTER);
        dlg.add(header, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 0, 4, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JTextField fCliente = inputField(), fTelefone = inputField(), fEmail = inputField();
        JTextField fServico = inputField(), fTecnico = inputField(), fValor = inputField();
        JComboBox<String> cEquip = new JComboBox<>(EQUIPS);
        cEquip.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cEquip.setBackground(Color.WHITE);
        JComboBox<String> cStatus = new JComboBox<>(new String[]{"ABERTA", "EM ANDAMENTO", "AGUARDANDO PEÇA"});
        cStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cStatus.setBackground(Color.WHITE);
        JComboBox<String> cPrior = new JComboBox<>(PRIORIDADES);
        cPrior.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cPrior.setBackground(Color.WHITE);
        JTextField fGarantia = inputField();
        fGarantia.setText("90");
        JTextArea fDesc = new JTextArea(3, 15);
        fDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        fDesc.setLineWrap(true);
        fDesc.setBackground(Color.WHITE);
        fDesc.setBorder(BorderFactory.createLineBorder(BORDER));

        int r = 0;
        addFormField(form, gbc, r++, "Cliente:", fCliente);
        addFormField(form, gbc, r++, "Telefone:", fTelefone);
        addFormField(form, gbc, r++, "Email:", fEmail);
        addFormField(form, gbc, r++, "Serviço:", fServico);
        addFormField(form, gbc, r++, "Técnico:", fTecnico);
        addFormField(form, gbc, r++, "Equipamento:", cEquip);
        addFormField(form, gbc, r++, "Descrição:", new JScrollPane(fDesc));
        addFormField(form, gbc, r++, "Status:", cStatus);
        addFormField(form, gbc, r++, "Prioridade:", cPrior);
        addFormField(form, gbc, r++, "Valor (R$):", fValor);
        addFormField(form, gbc, r++, "Garantia (dias):", fGarantia);

        // Buttons
        JPanel btns = new JPanel(new GridLayout(1, 2, 12, 0));
        btns.setBackground(Color.WHITE);
        btns.setBorder(BorderFactory.createEmptyBorder(10, 25, 20, 25));

        JButton saveBtn = new JButton("💾 Salvar OS");
        styleBtn(saveBtn, ACCENT);
        saveBtn.addActionListener(e -> {
            String cliente = fCliente.getText().trim();
            String servico = fServico.getText().trim();
            String tecnico = fTecnico.getText().trim();
            if (cliente.isEmpty() || servico.isEmpty() || tecnico.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Preencha: Cliente, Serviço e Técnico", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String valor = fValor.getText().trim();
            if (valor.isEmpty()) valor = "0,00";

            OS os = new OS(
                cliente, fTelefone.getText().trim(), fEmail.getText().trim(),
                servico, tecnico, (String)cEquip.getSelectedItem(),
                fDesc.getText().trim(), (String)cStatus.getSelectedItem(),
                (String)cPrior.getSelectedItem(), valor, fGarantia.getText().trim() + "d"
            );

            osList.add(os);
            refreshOSTable();
            refreshFinance();
            addLog("Nova OS: " + os.id + " - " + cliente);
            dlg.dispose();

            // Tela de sucesso
            showSuccessDialog("OS Criada com Sucesso!", os.id + "\nCliente: " + cliente + "\nServiço: " + servico);
        });
        btns.add(saveBtn);

        JButton cancelBtn = new JButton("❌ Cancelar");
        styleBtn(cancelBtn, DANGER);
        cancelBtn.addActionListener(e -> dlg.dispose());
        btns.add(cancelBtn);

        dlg.add(new JScrollPane(form), BorderLayout.CENTER);
        dlg.add(btns, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // Diálogo de edição de OS
    private void editSelectedOS() {
        int row = osTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Selecione uma OS para editar.", "Atenção", JOptionPane.WARNING_MESSAGE); return; }

        OS os = getOSByRow(row);
        if (os == null) return;

        JDialog dlg = new JDialog(this, "✏️ Editar " + os.id, true);
        dlg.setSize(580, 680);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(Color.WHITE);
        dlg.setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(WARNING);
        header.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        JLabel hLbl = new JLabel("✏️ Editar " + os.id);
        hLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        hLbl.setForeground(Color.WHITE);
        header.add(hLbl, BorderLayout.CENTER);
        dlg.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 0, 4, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JTextField fCliente = inputField(os.cliente);
        JTextField fTelefone = inputField(os.telefone);
        JTextField fEmail = inputField(os.email);
        JTextField fServico = inputField(os.servico);
        JTextField fTecnico = inputField(os.tecnico);
        JTextField fValor = inputField(os.valor);
        JComboBox<String> cEquip = new JComboBox<>(EQUIPS);
        cEquip.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cEquip.setBackground(Color.WHITE);
        cEquip.setSelectedItem(os.equipamento);
        JComboBox<String> cStatus = new JComboBox<>(STATUS);
        cStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cStatus.setBackground(Color.WHITE);
        cStatus.setSelectedItem(os.status);
        JComboBox<String> cPrior = new JComboBox<>(PRIORIDADES);
        cPrior.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cPrior.setBackground(Color.WHITE);
        cPrior.setSelectedItem(os.prioridade);
        JTextField fGarantia = inputField(os.garantia.replace("d", ""));
        JTextArea fDesc = new JTextArea(3, 15);
        fDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        fDesc.setLineWrap(true);
        fDesc.setBackground(Color.WHITE);
        fDesc.setBorder(BorderFactory.createLineBorder(BORDER));
        fDesc.setText(os.descricao);

        int ri = 0;
        addFormField(form, gbc, ri++, "Cliente:", fCliente);
        addFormField(form, gbc, ri++, "Telefone:", fTelefone);
        addFormField(form, gbc, ri++, "Email:", fEmail);
        addFormField(form, gbc, ri++, "Serviço:", fServico);
        addFormField(form, gbc, ri++, "Técnico:", fTecnico);
        addFormField(form, gbc, ri++, "Equipamento:", cEquip);
        addFormField(form, gbc, ri++, "Descrição:", new JScrollPane(fDesc));
        addFormField(form, gbc, ri++, "Status:", cStatus);
        addFormField(form, gbc, ri++, "Prioridade:", cPrior);
        addFormField(form, gbc, ri++, "Valor (R$):", fValor);
        addFormField(form, gbc, ri++, "Garantia (dias):", fGarantia);

        JPanel btns = new JPanel(new GridLayout(1, 2, 12, 0));
        btns.setBackground(Color.WHITE);
        btns.setBorder(BorderFactory.createEmptyBorder(10, 25, 20, 25));

        JButton saveBtn = new JButton("💾 Salvar");
        styleBtn(saveBtn, WARNING);
        saveBtn.addActionListener(e -> {
            os.cliente = fCliente.getText().trim();
            os.telefone = fTelefone.getText().trim();
            os.email = fEmail.getText().trim();
            os.servico = fServico.getText().trim();
            os.tecnico = fTecnico.getText().trim();
            os.equipamento = (String)cEquip.getSelectedItem();
            os.descricao = fDesc.getText().trim();
            os.status = (String)cStatus.getSelectedItem();
            os.prioridade = (String)cPrior.getSelectedItem();
            os.valor = fValor.getText().trim();
            os.garantia = fGarantia.getText().trim() + "d";

            refreshOSTable();
            refreshFinance();
            addLog("OS editada: " + os.id);
            dlg.dispose();
            showSuccessDialog("OS Atualizada!", os.id + " atualizada com sucesso.");
        });
        btns.add(saveBtn);

        JButton cancelBtn = new JButton("Cancelar");
        styleBtn(cancelBtn, DANGER);
        cancelBtn.addActionListener(e -> dlg.dispose());
        btns.add(cancelBtn);

        dlg.add(new JScrollPane(form), BorderLayout.CENTER);
        dlg.add(btns, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // Finalizar OS
    private void finalizeSelectedOS() {
        int row = osTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Selecione uma OS para finalizar.", "Atenção", JOptionPane.WARNING_MESSAGE); return; }

        OS os = getOSByRow(row);
        if (os == null) return;

        int r = JOptionPane.showConfirmDialog(this, "Finalizar OS " + os.id + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            os.status = "FINALIZADA";
            os.dataConclusao = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
            refreshOSTable();
            refreshFinance();
            addLog("OS finalizada: " + os.id);
            showSuccessDialog("OS Finalizada!", os.id + " foi marcada como FINALIZADA.");
        }
    }

    // Deletar OS
    private void deleteSelectedOS() {
        int row = osTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Selecione uma OS para excluir.", "Atenção", JOptionPane.WARNING_MESSAGE); return; }

        OS os = getOSByRow(row);
        if (os == null) return;

        int r = JOptionPane.showConfirmDialog(this, "Excluir OS " + os.id + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            osList.remove(os);
            refreshOSTable();
            refreshFinance();
            detailsArea.setText("");
            addLog("OS excluída: " + os.id);
        }
    }

    // Imprimir OS
    private void printSelectedOS() {
        int row = osTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Selecione uma OS.", "Atenção", JOptionPane.WARNING_MESSAGE); return; }
        OS os = getOSByRow(row);
        if (os == null) return;

        String out = String.format(
            "┌──────────────────────────────────────┐\n" +
            "│   ORDEM DE SERVIÇO - TechSuite Pro   │\n" +
            "├──────────────────────────────────────┤\n" +
            "│  OS: %-33s│\n" +
            "│  Abertura: %-27s│\n" +
            "│  Conclusão: %-26s│\n" +
            "│  Cliente: %-28s│\n" +
            "│  Telefone: %-25s│\n" +
            "│  Email: %-30s│\n" +
            "│  Serviço: %-28s│\n" +
            "│  Técnico: %-28s│\n" +
            "│  Equipamento: %-22s│\n" +
            "│  Status: %-29s│\n" +
            "│  Prioridade: %-24s│\n" +
            "│  Valor: R$ %-26s│\n" +
            "│  Garantia: %-25s│\n" +
            "│  Descrição: %-24s│\n" +
            "└──────────────────────────────────────┘",
            os.id, os.dataAbertura, os.dataConclusao, os.cliente, os.telefone,
            os.email, os.servico, os.tecnico, os.equipamento, os.status,
            os.prioridade, os.valor, os.garantia, os.descricao.length() > 24 ? os.descricao.substring(0,24) : os.descricao
        );
        JOptionPane.showMessageDialog(this, out, "OS " + os.id, JOptionPane.INFORMATION_MESSAGE);
    }

    // ===== HELPERS DE OS =====
    private OS getOSByRow(int row) {
        int idx = osTable.convertRowIndexToModel(row);
        if (idx >= 0 && idx < osList.size()) return osList.get(idx);
        return null;
    }

    private void refreshOSTable() {
        osModel.setRowCount(0);
        for (OS os : osList) osModel.addRow(os.toRow());
    }

    private void filterOS() {
        String s = searchOS.getText().toLowerCase();
        String st = (String) filterStatus.getSelectedItem();
        String pr = (String) filterPriority.getSelectedItem();
        osModel.setRowCount(0);
        for (OS os : osList) {
            boolean mS = s.isEmpty();
            if (!mS) {
                String all = os.id + os.cliente + os.servico + os.tecnico + os.status;
                mS = all.toLowerCase().contains(s);
            }
            boolean mSt = st.equals("TODOS") || os.status.equals(st);
            boolean mPr = pr.equals("TODAS") || os.prioridade.equals(pr);
            if (mS && mSt && mPr) osModel.addRow(os.toRow());
        }
    }

    private void showDetails(int row) {
        int idx = osTable.convertRowIndexToModel(row);
        if (idx < 0 || idx >= osList.size()) return;
        OS os = osList.get(idx);
        String out = String.format(
            "══════════════════════════════════════════\n" +
            "  ORDEM DE SERVIÇO: %s\n" +
            "══════════════════════════════════════════\n\n" +
            "  Abertura   : %s\n" +
            "  Conclusão  : %s\n" +
            "  Cliente    : %s\n" +
            "  Telefone   : %s\n" +
            "  Email      : %s\n" +
            "  Serviço    : %s\n" +
            "  Técnico    : %s\n" +
            "  Equipamento: %s\n" +
            "  Status     : %s\n" +
            "  Prioridade : %s\n" +
            "  Valor      : R$ %s\n" +
            "  Garantia   : %s\n" +
            "  Descrição  : %s\n" +
            "\n══════════════════════════════════════════\n",
            os.id, os.dataAbertura, os.dataConclusao, os.cliente, os.telefone,
            os.email, os.servico, os.tecnico, os.equipamento, os.status,
            os.prioridade, os.valor, os.garantia, os.descricao
        );
        detailsArea.setText(out);
    }

    private int countOS(String status) {
        int c = 0;
        for (OS os : osList) if (os.status.equals(status)) c++;
        return c;
    }

    private String totalFaturamento() {
        double t = 0;
        for (OS os : osList) t += parseValor(os.valor);
        return "R$ " + String.format("%.2f", t);
    }

    private String totalPendente() {
        double t = 0;
        for (OS os : osList) if (!os.status.equals("FINALIZADA")) t += parseValor(os.valor);
        return "R$ " + String.format("%.2f", t);
    }

    private String totalRecebido() {
        double t = 0;
        for (OS os : osList) if (os.status.equals("FINALIZADA")) t += parseValor(os.valor);
        return "R$ " + String.format("%.2f", t);
    }

    private String ticketMedio() {
        if (osList.isEmpty()) return "R$ 0,00";
        double t = 0;
        for (OS os : osList) t += parseValor(os.valor);
        return "R$ " + String.format("%.2f", t / osList.size());
    }

    private double parseValor(String v) {
        try { return Double.parseDouble(v.replace(",", ".")); } catch (Exception e) { return 0; }
    }

    // ===== CLIENTES =====
    private void addClient() {
        JTextField n = inputField(), t = inputField(), e = inputField();
        JPanel p = formPanel(new Object[]{lbl("Nome:"), n, lbl("Telefone:"), t, lbl("Email:"), e}, 3);
        if (JOptionPane.showConfirmDialog(this, p, "Novo Cliente", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String id = String.format("CL-%03d", clientModel.getRowCount()+1);
            String data = dateOnly();
            clientModel.addRow(new Object[]{id, n.getText(), t.getText(), e.getText(), data});
            addLog("Cliente: " + n.getText());
        }
    }

    private void deleteClient() {
        int r = clientTable.getSelectedRow();
        if (r == -1) { JOptionPane.showMessageDialog(this, "Selecione um cliente.", "Atenção", JOptionPane.WARNING_MESSAGE); return; }
        if (JOptionPane.showConfirmDialog(this, "Excluir cliente?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            addLog("Cliente excluído: " + clientModel.getValueAt(r, 1));
            ((DefaultTableModel)clientTable.getModel()).removeRow(r);
        }
    }

    // ===== EQUIPE =====
    private void addTech() {
        JTextField n = inputField(), e = inputField();
        JPanel p = formPanel(new Object[]{lbl("Nome:"), n, lbl("Especialidade:"), e}, 2);
        if (JOptionPane.showConfirmDialog(this, p, "Novo Técnico", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String id = String.format("TEC-%03d", techModel.getRowCount()+1);
            techModel.addRow(new Object[]{id, n.getText(), e.getText(), 0, 0});
            addLog("Técnico: " + n.getText());
        }
    }

    private void deleteTech() {
        int r = techTable.getSelectedRow();
        if (r == -1) { JOptionPane.showMessageDialog(this, "Selecione um técnico.", "Atenção", JOptionPane.WARNING_MESSAGE); return; }
        if (JOptionPane.showConfirmDialog(this, "Excluir técnico?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            addLog("Técnico excluído");
            ((DefaultTableModel)techTable.getModel()).removeRow(r);
        }
    }

    // ===== FINANCEIRO =====
    private void refreshFinance() {
        if (finModel == null) return;
        finModel.setRowCount(0);
        for (OS os : osList) {
            String pg = os.status.equals("FINALIZADA") ? "PAGO" : "PENDENTE";
            finModel.addRow(new Object[]{os.id, os.cliente, os.servico, "R$ " + os.valor, pg, os.dataConclusao});
        }
    }

    // ===== RELATÓRIOS =====
    private void showFullReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("╔══════════════════════════════════════╗\n");
        sb.append("║   RELATÓRIO COMPLETO - TechSuite     ║\n");
        sb.append("╠══════════════════════════════════════╣\n");
        sb.append(String.format("║ Total OS : %26d ║\n", osList.size()));
        sb.append(String.format("║ Abertas  : %26d ║\n", countOS("ABERTA")));
        sb.append(String.format("║ Andamento: %26d ║\n", countOS("EM ANDAMENTO")));
        sb.append(String.format("║ Finaliz. : %26d ║\n", countOS("FINALIZADA")));
        sb.append(String.format("║ Receita  : %-19s ║\n", totalFaturamento()));
        sb.append(String.format("║ Clientes : %26d ║\n", clientModel.getRowCount()));
        sb.append(String.format("║ Técnicos : %26d ║\n", techModel.getRowCount()));
        sb.append("╚══════════════════════════════════════╝\n");
        showInfoDialog("Relatório Completo", sb.toString());
    }

    private void showClientReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("══════════════════════════════════════\n");
        sb.append("  RELATÓRIO DE CLIENTES\n");
        sb.append("══════════════════════════════════════\n\n");
        DefaultTableModel m = (DefaultTableModel)clientTable.getModel();
        for (int i = 0; i < m.getRowCount(); i++) {
            sb.append(String.format("  %s - %s | %s\n", m.getValueAt(i,0), m.getValueAt(i,1), m.getValueAt(i,2)));
        }
        showInfoDialog("Clientes", sb.toString());
    }

    private void showTechReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("══════════════════════════════════════\n");
        sb.append("  RELATÓRIO DA EQUIPE\n");
        sb.append("══════════════════════════════════════\n\n");
        DefaultTableModel m = (DefaultTableModel)techTable.getModel();
        for (int i = 0; i < m.getRowCount(); i++) {
            sb.append(String.format("  %s - %s | %s\n", m.getValueAt(i,0), m.getValueAt(i,1), m.getValueAt(i,2)));
        }
        showInfoDialog("Equipe", sb.toString());
    }

    private void showFinanceReport() {
        String out = String.format("RELATÓRIO FINANCEIRO\n\n" +
            "Receita Total : %s\n" +
            "A Receber     : %s\n" +
            "Recebido      : %s\n" +
            "Ticket Médio  : %s\n" +
            "Total OS      : %d",
            totalFaturamento(), totalPendente(), totalRecebido(), ticketMedio(), osList.size());
        showInfoDialog("Financeiro", out);
    }

    private void doBackup() {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String dest = fc.getSelectedFile().getPath() + "/backup_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".txt";
                FileWriter fw = new FileWriter(dest);
                fw.write("BACKUP TECHSUITE - " + now() + "\n\n");
                for (OS os : osList) {
                    fw.write(String.join("|", os.id, os.cliente, os.servico, os.status, os.valor) + "\n");
                }
                fw.close();
                addLog("Backup: " + dest);
                showSuccessDialog("Backup Realizado!", "Arquivo: " + dest);
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE); }
        }
    }

    private void showSystemInfo() {
        String info = String.format(
            "Sistema: %s %s\nJava: %s\nUsuário: %s\nMemória: %d MB livre\nProcessadores: %d",
            System.getProperty("os.name"), System.getProperty("os.version"),
            System.getProperty("java.version"), System.getProperty("user.name"),
            Runtime.getRuntime().freeMemory()/1024/1024, Runtime.getRuntime().availableProcessors());
        showInfoDialog("Info do Sistema", info);
    }

    // ===== ADMIN =====
    private void loadUsers() {
        if (userModel == null) return;
        userModel.setRowCount(0);
        userModel.addRow(new Object[]{"admin", "Administrador", "ADMIN", "ATIVO"});
        File uf = new File(System.getProperty("user.home") + "/.techsuite/users.dat");
        if (!uf.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(uf))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p.length >= 4) userModel.addRow(new Object[]{p[0], p[1], p[2], p[3]});
            }
        } catch (Exception ignored) {}
    }

    private void addUserDialog() {
        JTextField uId = inputField(), uName = inputField();
        JPasswordField uPass = new JPasswordField();
        uPass.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        uPass.setBackground(Color.WHITE);
        uPass.setBorder(BorderFactory.createLineBorder(BORDER));
        JComboBox<String> uRole = new JComboBox<>(new String[]{"USER", "ADMIN"});
        uRole.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        uRole.setBackground(Color.WHITE);

        JPanel p = formPanel(new Object[]{lbl("Usuário:"), uId, lbl("Senha:"), uPass, lbl("Nome:"), uName, lbl("Nível:"), uRole}, 4);
        if (JOptionPane.showConfirmDialog(this, p, "Novo Usuário", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try (FileWriter fw = new FileWriter(System.getProperty("user.home") + "/.techsuite/users.dat", true)) {
                fw.write(String.format("%s|%s|%s|ATIVO\n", uId.getText().trim(), uName.getText().trim(), uRole.getSelectedItem()));
                addLog("Usuário criado: " + uId.getText());
                loadUsers();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE); }
        }
    }

    // ===== DIALOGOS MODERNOS =====
    private void showSuccessDialog(String title, String message) {
        JDialog dlg = new JDialog(this, title, true);
        dlg.setSize(400, 250);
        dlg.setLocationRelativeTo(this);
        dlg.setUndecorated(true);
        dlg.getContentPane().setBackground(Color.WHITE);
        dlg.setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(SUCCESS);
        header.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel icon = new JLabel("✅", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        icon.setForeground(Color.WHITE);
        header.add(icon, BorderLayout.CENTER);

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 18));
        t.setForeground(Color.WHITE);
        t.setHorizontalAlignment(SwingConstants.CENTER);
        header.add(t, BorderLayout.SOUTH);
        dlg.add(header, BorderLayout.NORTH);

        // Message
        JTextArea msg = new JTextArea(message);
        msg.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        msg.setForeground(TEXT_SECONDARY);
        msg.setBackground(Color.WHITE);
        msg.setEditable(false);
        msg.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        msg.setLineWrap(true);
        dlg.add(msg, BorderLayout.CENTER);

        // Button
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 15));
        btns.setBackground(Color.WHITE);
        JButton ok = new JButton("OK");
        styleBtn(ok, SUCCESS);
        ok.setPreferredSize(new Dimension(120, 38));
        ok.addActionListener(e -> dlg.dispose());
        btns.add(ok);
        dlg.add(btns, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    private void showInfoDialog(String title, String message) {
        JDialog dlg = new JDialog(this, title, true);
        dlg.setSize(500, 380);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(Color.WHITE);
        dlg.setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(INFO);
        header.setBorder(BorderFactory.createEmptyBorder(18, 25, 18, 25));
        JLabel t = new JLabel("  " + title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 16));
        t.setForeground(Color.WHITE);
        header.add(t, BorderLayout.CENTER);
        dlg.add(header, BorderLayout.NORTH);

        JTextArea msg = new JTextArea(message);
        msg.setFont(new Font("Consolas", Font.PLAIN, 12));
        msg.setForeground(TEXT_PRIMARY);
        msg.setBackground(Color.WHITE);
        msg.setEditable(false);
        msg.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        msg.setLineWrap(true);
        dlg.add(new JScrollPane(msg), BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        btns.setBackground(Color.WHITE);
        JButton close = new JButton("Fechar");
        styleBtn(close, INFO);
        close.addActionListener(e -> dlg.dispose());
        btns.add(close);
        dlg.add(btns, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    // ===== UI HELPERS =====
    private JTable createTable(DefaultTableModel m) {
        JTable t = new JTable(m);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        t.setForeground(TEXT_PRIMARY);
        t.setBackground(Color.WHITE);
        t.setRowHeight(32);
        t.setGridColor(new Color(235, 235, 240));
        t.setSelectionBackground(new Color(79, 70, 229, 25));
        t.setSelectionForeground(TEXT_PRIMARY);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        t.getTableHeader().setBackground(new Color(248, 248, 252));
        t.getTableHeader().setForeground(TEXT_SECONDARY);
        t.getTableHeader().setPreferredSize(new Dimension(0, 36));
        return t;
    }

    private void setColWidths(JTable t, int[] w) {
        for (int i = 0; i < w.length && i < t.getColumnCount(); i++)
            t.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
    }

    private JTextField inputField() {
        JTextField f = new JTextField();
        f.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        f.setBackground(Color.WHITE);
        f.setForeground(TEXT_PRIMARY);
        f.setCaretColor(TEXT_PRIMARY);
        f.setBorder(BorderFactory.createLineBorder(BORDER));
        return f;
    }

    private JTextField inputField(String value) {
        JTextField f = inputField();
        f.setText(value != null ? value : "");
        return f;
    }

    private JButton btnPrimary(String text, Color bg) {
        JButton b = new JButton(text);
        styleBtn(b, bg);
        return b;
    }

    private JButton btnPrimary(String text, Color bg, ActionListener al) {
        JButton b = new JButton(text);
        styleBtn(b, bg);
        b.addActionListener(al);
        return b;
    }

    private JButton btnSecondary(String text, Color bg, ActionListener al) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 11));
        b.setForeground(Color.WHITE);
        b.setBackground(bg);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(140, 34));
        b.addActionListener(al);
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(bg.brighter()); }
            public void mouseExited(MouseEvent e) { b.setBackground(bg); }
        });
        return b;
    }

    private void styleBtn(JButton b, Color bg) {
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setForeground(Color.WHITE);
        b.setBackground(bg);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(140, 36));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(bg.brighter()); }
            public void mouseExited(MouseEvent e) { b.setBackground(bg); }
        });
    }

    private void addFormField(JPanel form, GridBagConstraints gbc, int row, String label, Component field) {
        gbc.gridy = row * 2;
        JLabel lb = new JLabel(label);
        lb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lb.setForeground(TEXT_SECONDARY);
        form.add(lb, gbc);

        gbc.gridy = row * 2 + 1;
        gbc.insets = new Insets(2, 0, 8, 0);
        form.add(field, gbc);
        gbc.insets = new Insets(4, 0, 4, 0);
    }

    private JLabel lbl(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(TEXT_SECONDARY);
        return l;
    }

    private JPanel formPanel(Object[] comps, int rows) {
        JPanel p = new JPanel(new GridLayout(rows, 2, 10, 8));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        for (Object c : comps) {
            if (c instanceof JLabel) ((JLabel)c).setForeground(TEXT_SECONDARY);
            if (c instanceof Component) p.add((Component)c);
        }
        return p;
    }

    private void exportData(DefaultTableModel model, String[] cols, String filename) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(filename));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                FileWriter w = new FileWriter(fc.getSelectedFile());
                w.write(String.join(",", cols) + "\n");
                for (int i = 0; i < model.getRowCount(); i++) {
                    for (int j = 0; j < model.getColumnCount(); j++) {
                        w.write("\"" + model.getValueAt(i, j).toString().replace("\"", "\"\"") + "\"");
                        if (j < model.getColumnCount()-1) w.write(",");
                    }
                    w.write("\n");
                }
                w.close();
                addLog("Exportado: " + filename);
                showSuccessDialog("Exportado!", "Arquivo: " + fc.getSelectedFile().getName());
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE); }
        }
    }

    private void addLog(String msg) {
        if (activityLog != null) {
            activityLog.append("[" + timeOnly() + "] " + msg + "\n");
            activityLog.setCaretPosition(activityLog.getDocument().getLength());
        }
    }

    private String now() { return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()); }
    private String dateOnly() { return new SimpleDateFormat("dd/MM/yyyy").format(new Date()); }
    private String timeOnly() { return new SimpleDateFormat("HH:mm:ss").format(new Date()); }

    // ===== CARD BORDER =====
    static class CardBorder extends AbstractBorder {
        private Color accent;
        CardBorder(Color a) { accent = a; }
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0, 0, 0, 12));
            g2.fillRoundRect(x + 2, y + 2, w - 2, h - 2, 12, 12);
            g2.setColor(BG_CARD);
            g2.fillRoundRect(x, y, w - 4, h - 4, 12, 12);
            g2.setColor(accent);
            g2.fillRect(x + 12, y, w - 24, 4);
            g2.setColor(BORDER);
            g2.drawRoundRect(x, y, w - 5, h - 5, 12, 12);
            g2.dispose();
        }
        public Insets getBorderInsets(Component c) { return new Insets(4, 4, 4, 4); }
    }

    // ===== DADOS SAMPLE =====
    private void loadSampleData() {
        String[][] data = {
            {"João Silva","(11) 98765-4321","joao@email.com","Formatação e backup","Carlos Tech","Notebook","PC lento, formatar Windows","FINALIZADA","BAIXA","150,00","90"},
            {"Maria Santos","(11) 91234-5678","maria@email.com","Troca de tela notebook","Ana Repair","Notebook","Tela quebrada Dell Inspiron","EM ANDAMENTO","ALTA","450,00","90"},
            {"Pedro Oliveira","(21) 99876-5432","pedro@email.com","Remoção de vírus","Carlos Tech","Desktop","PC com malware, remover vírus","ABERTA","MÉDIA","120,00","30"},
            {"Empresa ABC","(11) 3456-7890","contato@abc.com","Configuração de rede","Roberto Net","Servidor","Configurar rede com 10 PCs","AGUARDANDO PEÇA","URGENTE","800,00","180"},
            {"Lucas Ferreira","(31) 98888-7777","lucas@email.com","Upgrade SSD + RAM","Ana Repair","Notebook","Upgrade para SSD 480GB + 16GB RAM","EM ANDAMENTO","ALTA","650,00","90"},
            {"Fernanda Costa","(11) 97777-6666","fernanda@email.com","Recuperação de dados","Carlos Tech","Desktop","HD com defeito, recuperar arquivos","ABERTA","URGENTE","350,00","30"},
        };
        for (String[] d : data) {
            OS os = new OS(d[0], d[1], d[2], d[3], d[4], d[5], d[6], d[7], d[8], d[9], d[10]+"d");
            if (d[7].equals("FINALIZADA")) os.dataConclusao = "12/04/2026 14:00";
            osList.add(os);
        }

        Object[][] clients = {
            {"CL-001", "João Silva", "(11) 98765-4321", "joao@email.com", "01/03/2026"},
            {"CL-002", "Maria Santos", "(11) 91234-5678", "maria@email.com", "15/03/2026"},
            {"CL-003", "Pedro Oliveira", "(21) 99876-5432", "pedro@email.com", "20/03/2026"},
            {"CL-004", "Empresa ABC Ltda", "(11) 3456-7890", "contato@abc.com", "05/04/2026"},
            {"CL-005", "Lucas Ferreira", "(31) 98888-7777", "lucas@email.com", "10/04/2026"},
        };
        for (Object[] r : clients) clientModel.addRow(r);

        Object[][] techs = {
            {"TEC-001", "Carlos Tech", "Formatação e Software", 3, 45},
            {"TEC-002", "Ana Repair", "Hardware e Telas", 2, 38},
            {"TEC-003", "Roberto Net", "Redes e Servidores", 1, 27},
        };
        for (Object[] r : techs) techModel.addRow(r);

        refreshOSTable();
        refreshFinance();
        addLog("Sistema inicializado por " + currentUser);
        addLog("6 ordens | 5 clientes | 3 técnicos");
    }

    // ===== MAIN / LOGIN =====
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        new File(System.getProperty("user.home") + "/.techsuite").mkdirs();

        SwingUtilities.invokeLater(() -> new LoginDialog().showLogin());
    }

    // ===== TELA DE LOGIN MODERNA =====
    static class LoginDialog {
        private JDialog dialog;
        private JTextField userField;
        private JPasswordField passField;

        void showLogin() {
            dialog = new JDialog((Frame)null, "TechSuite Pro - Login", true);
            dialog.setSize(480, 520);
            dialog.setLocationRelativeTo(null);
            dialog.setUndecorated(true);
            dialog.setLayout(new BorderLayout());
            dialog.getContentPane().setBackground(Color.WHITE);
            dialog.setShape(new java.awt.geom.RoundRectangle2D.Double(0, 0, 480, 520, 20, 20));

            // Header gradient
            JPanel header = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    GradientPaint gp = new GradientPaint(0, 0, ACCENT, getWidth(), getHeight(), PURPLE);
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, getWidth(), 180, 20, 20);
                    g2.fillRect(0, 140, getWidth(), 40);
                    g2.dispose();
                }
            };
            header.setLayout(new BorderLayout());
            header.setBackground(ACCENT);
            header.setBorder(BorderFactory.createEmptyBorder(35, 35, 20, 35));

            JLabel titleLbl = new JLabel("TechSuite Pro");
            titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 30));
            titleLbl.setForeground(Color.WHITE);
            header.add(titleLbl, BorderLayout.CENTER);

            JLabel subLbl = new JLabel("Sistema de Gestão de Serviços v2.0");
            subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            subLbl.setForeground(new Color(200, 200, 255));
            header.add(subLbl, BorderLayout.SOUTH);
            dialog.add(header, BorderLayout.NORTH);

            // Form
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBackground(Color.WHITE);
            formPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 20, 40));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(5, 0, 5, 0);

            JLabel userIcon = new JLabel("  👤  Usuário:");
            userIcon.setFont(new Font("Segoe UI", Font.BOLD, 12));
            userIcon.setForeground(TEXT_SECONDARY);
            gbc.gridy = 0; formPanel.add(userIcon, gbc);

            userField = new JTextField();
            userField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            userField.setBackground(Color.WHITE);
            userField.setForeground(TEXT_PRIMARY);
            userField.setCaretColor(TEXT_PRIMARY);
            userField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(12, 15, 12, 15)));
            userField.setText("admin");
            gbc.gridy = 1; formPanel.add(userField, gbc);

            JLabel passIcon = new JLabel("  🔒  Senha:");
            passIcon.setFont(new Font("Segoe UI", Font.BOLD, 12));
            passIcon.setForeground(TEXT_SECONDARY);
            gbc.gridy = 2; formPanel.add(passIcon, gbc);

            passField = new JPasswordField();
            passField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            passField.setBackground(Color.WHITE);
            passField.setForeground(TEXT_PRIMARY);
            passField.setCaretColor(TEXT_PRIMARY);
            passField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(12, 15, 12, 15)));
            passField.setText("admin123");
            gbc.gridy = 3; formPanel.add(passField, gbc);

            // Login button
            JButton loginBtn = new JButton("ENTRAR");
            loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
            loginBtn.setForeground(Color.WHITE);
            loginBtn.setBackground(ACCENT);
            loginBtn.setFocusPainted(false);
            loginBtn.setBorderPainted(false);
            loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            loginBtn.setPreferredSize(new Dimension(0, 48));
            loginBtn.addActionListener(e -> doLogin());
            gbc.gridy = 4; gbc.insets = new Insets(15, 0, 0, 0);
            formPanel.add(loginBtn, gbc);

            passField.addActionListener(e -> doLogin());

            // Footer
            JPanel footer = new JPanel(new BorderLayout());
            footer.setBackground(Color.WHITE);
            footer.setBorder(BorderFactory.createEmptyBorder(10, 40, 25, 40));

            JLabel helpLbl = new JLabel("Acesso padrão: admin / admin123");
            helpLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            helpLbl.setForeground(TEXT_MUTED);
            helpLbl.setHorizontalAlignment(SwingConstants.CENTER);
            footer.add(helpLbl, BorderLayout.CENTER);

            // Close button
            JButton closeBtn = new JButton("✕");
            closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            closeBtn.setForeground(TEXT_MUTED);
            closeBtn.setBackground(new Color(245, 245, 250));
            closeBtn.setFocusPainted(false);
            closeBtn.setBorderPainted(false);
            closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            closeBtn.setPreferredSize(new Dimension(32, 32));
            closeBtn.addActionListener(e -> System.exit(0));
            footer.add(closeBtn, BorderLayout.EAST);

            dialog.add(formPanel, BorderLayout.CENTER);
            dialog.add(footer, BorderLayout.SOUTH);

            // Draggable
            final int[] drag = new int[2];
            header.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) { drag[0] = e.getX(); drag[1] = e.getY(); }
            });
            header.addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    dialog.setLocation(dialog.getLocation().x + e.getX() - drag[0], dialog.getLocation().y + e.getY() - drag[1]);
                }
            });

            dialog.setVisible(true);
        }

        private void doLogin() {
            String user = userField.getText().trim();
            String pass = new String(passField.getPassword());

            if (user.equals("admin") && pass.equals("admin123")) {
                dialog.dispose();
                HackPanel p = new HackPanel("admin", true);
                p.setVisible(true);
                return;
            }

            // Check file
            File uf = new File(System.getProperty("user.home") + "/.techsuite/users.dat");
            if (uf.exists()) {
                try (BufferedReader br = new BufferedReader(new FileReader(uf))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] p = line.split("\\|");
                        if (p.length >= 2 && p[0].equals(user)) {
                            dialog.dispose();
                            boolean admin = p.length > 2 && p[2].equals("ADMIN");
                            HackPanel hp = new HackPanel(user, admin);
                            hp.setVisible(true);
                            return;
                        }
                    }
                } catch (Exception ignored) {}
            }

            // Error
            JOptionPane.showMessageDialog(dialog, "Usuário ou senha incorretos!", "Erro de Login", JOptionPane.ERROR_MESSAGE);
            passField.setText("");
        }
    }
}
