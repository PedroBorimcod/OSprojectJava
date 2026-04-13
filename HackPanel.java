import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.security.MessageDigest;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.io.*;

public class HackPanel extends JFrame {

    // ===== CORES - TEMA LIGHT PROFISSIONAL =====
    private static final Color BG_MAIN = new Color(245, 247, 250);
    private static final Color BG_SIDEBAR = new Color(30, 30, 50);
    private static final Color BG_SIDEBAR_HOVER = new Color(45, 45, 70);
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
    private static final Color PINK = new Color(236, 72, 153);
    private static final Color TEXT_PRIMARY = new Color(15, 15, 35);
    private static final Color TEXT_SECONDARY = new Color(100, 110, 130);
    private static final Color TEXT_MUTED = new Color(150, 155, 170);

    // Componentes
    private JPanel contentPanel;
    private JPanel sidebar;
    private CardLayout cardLayout;
    private JTable osTable, clientTable, techTable, finTable, userTable;
    private DefaultTableModel osModel, clientModel, techModel, finModel, userModel;
    private JTextArea detailsArea, activityLog;
    private JTextField searchOS;
    private JComboBox<String> filterStatus, filterPriority;
    private int osCounter = 0;
    private String currentUser = "";
    private boolean isAdmin = false;

    // Dialogo OS
    private JDialog osDialog;
    private JTextField[] osFields;
    private JComboBox<String> osStatus, osPriority, osEquip;
    private JTextArea osDesc;
    private boolean editing = false;
    private int editRow = -1;

    // Dados sample
    private Object[][] sampleOS;

    private static final String[] OS_COLS = {"OS#", "Data Abertura", "Data Conclusão", "Cliente", "Serviço", "Técnico", "Status", "Prioridade", "Valor", "Garantia"};
    private static final String[] CLIENT_COLS = {"ID", "Nome", "Telefone", "Email", "Endereço", "Desde"};
    private static final String[] TECH_COLS = {"ID", "Nome", "Especialidade", "OS Ativas", "Finalizadas", "Disponível"};
    private static final String[] FIN_COLS = {"OS#", "Cliente", "Serviço", "Valor", "Status Pgto", "Data Conclusão"};
    private static final String[] USER_COLS = {"ID", "Usuário", "Nome", "Email", "Nível", "Status", "Último Acesso"};
    private static final String[] STATUS = {"ABERTA", "EM ANDAMENTO", "AGUARDANDO PEÇA", "FINALIZADA", "CANCELADA"};
    private static final String[] PRIORIDADES = {"BAIXA", "MÉDIA", "ALTA", "URGENTE"};
    private static final String[] EQUIPS = {"Notebook", "Desktop", "Impressora", "Servidor", "Rede", "Smartphone", "Outro"};

    // ===== CONSTRUTOR =====
    public HackPanel(String user, boolean admin) {
        this.currentUser = user;
        this.isAdmin = admin;
        initFolders();
        setupFrame();
        loadData();
    }

    private void initFolders() {
        new File(System.getProperty("user.home") + "/.techsuite").mkdirs();
        File uf = new File(System.getProperty("user.home") + "/.techsuite/users.dat");
        if (!uf.exists()) {
            try { uf.createNewFile(); } catch (Exception ignored) {}
        }
    }

    // ===== FRAME PRINCIPAL =====
    private void setupFrame() {
        setTitle("TechSuite Pro - " + currentUser);
        setSize(1400, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_MAIN);

        // Sidebar
        sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // Header
        JPanel header = createHeader();
        add(header, BorderLayout.NORTH);

        // Content com CardLayout
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

        // Update stat cards
        SwingUtilities.invokeLater(() -> {
            refreshDashboardStats();
        });
    }

    // ===== SIDEBAR =====
    private JPanel createSidebar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_SIDEBAR);
        panel.setPreferredSize(new Dimension(240, 0));
        panel.setBorder(BorderFactory.createEmptyBorder());

        // Logo
        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setBackground(BG_SIDEBAR);
        logoPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel logo = new JLabel(" TechSuite Pro");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logo.setForeground(Color.WHITE);
        logoPanel.add(logo, BorderLayout.CENTER);

        JLabel version = new JLabel(" v2.0");
        version.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        version.setForeground(new Color(150, 150, 200));
        logoPanel.add(version, BorderLayout.SOUTH);
        panel.add(logoPanel, BorderLayout.NORTH);

        // Menu items
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(BG_SIDEBAR);
        menuPanel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        addMenuItem(menuPanel, "Dashboard", "home", ACCENT, true, e -> cardLayout.show(contentPanel, "dashboard"));
        addMenuItem(menuPanel, "Ordens de Serviço", "ordens", Color.WHITE, false, e -> cardLayout.show(contentPanel, "ordens"));
        addMenuItem(menuPanel, "Clientes", "clientes", Color.WHITE, false, e -> cardLayout.show(contentPanel, "clientes"));
        addMenuItem(menuPanel, "Equipe", "equipe", Color.WHITE, false, e -> cardLayout.show(contentPanel, "equipe"));
        addMenuItem(menuPanel, "Financeiro", "financeiro", Color.WHITE, false, e -> cardLayout.show(contentPanel, "financeiro"));
        addMenuItem(menuPanel, "Relatórios", "relatorios", Color.WHITE, false, e -> cardLayout.show(contentPanel, "relatorios"));

        if (isAdmin) {
            menuPanel.add(Box.createVerticalStrut(10));
            addMenuItem(menuPanel, "Administração", "admin", PURPLE, false, e -> cardLayout.show(contentPanel, "admin"));
        }

        panel.add(menuPanel, BorderLayout.CENTER);

        // Footer - user info
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(new Color(20, 20, 35));
        footerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel userLbl = new JLabel(" " + currentUser);
        userLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userLbl.setForeground(Color.WHITE);
        footerPanel.add(userLbl, BorderLayout.CENTER);

        JLabel roleLbl = new JLabel(" " + (isAdmin ? "Administrador" : "Usuário"));
        roleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        roleLbl.setForeground(new Color(150, 150, 200));
        footerPanel.add(roleLbl, BorderLayout.SOUTH);

        panel.add(footerPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void addMenuItem(JPanel menu, String text, String icon, Color color, boolean active, ActionListener al) {
        JPanel item = new JPanel(new BorderLayout(12, 0));
        item.setBackground(active ? BG_SIDEBAR_HOVER : BG_SIDEBAR);
        item.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 3, 0, 0, active ? ACCENT : new Color(0,0,0,0)),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)));
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));
        item.setMaximumSize(new Dimension(220, 44));

        JLabel lbl = new JLabel(" " + text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(active ? Color.WHITE : new Color(180, 180, 210));
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
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
            BorderFactory.createEmptyBorder(12, 25, 12, 25)));
        header.setPreferredSize(new Dimension(0, 56));

        // Page title
        JLabel title = new JLabel("Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        // Right side
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        right.setBackground(Color.WHITE);

        JLabel dateLbl = new JLabel(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));
        dateLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateLbl.setForeground(TEXT_SECONDARY);
        right.add(dateLbl);

        JButton logoutBtn = new JButton(" Sair");
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setBackground(DANGER);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.setPreferredSize(new Dimension(70, 30));
        logoutBtn.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Deseja sair?", "Logout", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
                System.exit(0);
        });
        right.add(logoutBtn);

        header.add(right, BorderLayout.EAST);

        return header;
    }

    // ===== DASHBOARD =====
    private JPanel buildDashboard() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(BG_MAIN);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // Welcome
        JPanel welcome = new JPanel(new BorderLayout());
        welcome.setBackground(BG_MAIN);
        welcome.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        JLabel welcomeLbl = new JLabel("Bom dia, " + currentUser + "!");
        welcomeLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        welcomeLbl.setForeground(TEXT_PRIMARY);
        welcome.add(welcomeLbl, BorderLayout.CENTER);

        JLabel dateLbl = new JLabel(new SimpleDateFormat("EEEE, dd 'de' MMMM 'de' yyyy").format(new Date()));
        dateLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dateLbl.setForeground(TEXT_SECONDARY);
        welcome.add(dateLbl, BorderLayout.SOUTH);

        panel.add(welcome, BorderLayout.NORTH);

        // Stats cards
        JPanel statsPanel = new JPanel(new GridLayout(1, 5, 15, 0));
        statsPanel.setBackground(BG_MAIN);
        statsPanel.setName("statsPanel");

        statsPanel.add(createStatCard("OS Abertas", "0", WARNING, "\uD83D\uDCCB"));
        statsPanel.add(createStatCard("Em Andamento", "0", INFO, "\uD83D\uDD27"));
        statsPanel.add(createStatCard("Finalizadas", "0", SUCCESS, "\u2705"));
        statsPanel.add(createStatCard("Faturamento", "R$ 0", SUCCESS, "\uD83D\uDCB0"));
        statsPanel.add(createStatCard("Clientes", "0", PURPLE, "\uD83D\uDC65"));

        panel.add(statsPanel, BorderLayout.CENTER);

        // Bottom section
        JPanel bottom = new JPanel(new GridLayout(1, 2, 20, 0));
        bottom.setBackground(BG_MAIN);
        bottom.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        // Quick actions
        JPanel actions = createCard("Ações Rápidas", ACCENT);
        JPanel actionsGrid = new JPanel(new GridLayout(2, 2, 10, 10));
        actionsGrid.setBackground(BG_CARD);
        actionsGrid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        actionsGrid.add(quickBtn("➕ Nova OS", ACCENT, e -> { cardLayout.show(contentPanel, "ordens"); openOS(false); }));
        actionsGrid.add(quickBtn("👤 Novo Cliente", SUCCESS, e -> { cardLayout.show(contentPanel, "clientes"); addClient(); }));
        actionsGrid.add(quickBtn("🔧 Novo Técnico", PURPLE, e -> { cardLayout.show(contentPanel, "equipe"); addTech(); }));
        actionsGrid.add(quickBtn("📊 Relatório", CYAN, e -> { cardLayout.show(contentPanel, "relatorios"); printFullReport(); }));
        actions.add(actionsGrid, BorderLayout.CENTER);

        // Activity log
        JPanel activity = createCard("Atividades Recentes", SUCCESS);
        activityLog = new JTextArea();
        activityLog.setFont(new Font("Consolas", Font.PLAIN, 11));
        activityLog.setForeground(TEXT_SECONDARY);
        activityLog.setBackground(Color.WHITE);
        activityLog.setEditable(false);
        activityLog.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        activity.add(new JScrollPane(activityLog), BorderLayout.CENTER);

        bottom.add(actions);
        bottom.add(activity);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createStatCard(String title, String value, Color accent, String icon) {
        JPanel card = new JPanel(new BorderLayout(12, 6));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createCompoundBorder(
                new ShadowBorder(accent),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)),
            null));

        // Top
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(BG_CARD);

        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        top.add(iconLbl, BorderLayout.WEST);

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valLbl.setForeground(accent);
        valLbl.setHorizontalAlignment(SwingConstants.RIGHT);
        top.add(valLbl, BorderLayout.EAST);

        card.add(top, BorderLayout.CENTER);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLbl.setForeground(TEXT_SECONDARY);
        card.add(titleLbl, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createCard(String title, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            new ShadowBorder(accent),
            BorderFactory.createEmptyBorder(15, 18, 15, 18)));

        JLabel titleLbl = new JLabel(" " + title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLbl.setForeground(accent);
        card.add(titleLbl, BorderLayout.NORTH);

        return card;
    }

    private JButton quickBtn(String text, Color accent, ActionListener al) {
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
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // Title
        JLabel title = new JLabel("Ordens de Serviço");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_PRIMARY);
        panel.add(title, BorderLayout.NORTH);

        // Filter bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        filterBar.setBackground(Color.WHITE);
        filterBar.setBorder(BorderFactory.createCompoundBorder(
            new ShadowBorder(ACCENT),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)));

        filterBar.add(lbl("Buscar:"));
        searchOS = new JTextField(18);
        searchOS.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchOS.setBackground(BG_INPUT);
        searchOS.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        searchOS.addKeyListener(new KeyAdapter() { public void keyReleased(KeyEvent e) { filterOS(); }});
        filterBar.add(searchOS);

        filterBar.add(Box.createHorizontalStrut(8));
        filterBar.add(lbl("Status:"));
        filterStatus = new JComboBox<>(new String[]{"TODOS", "ABERTA", "EM ANDAMENTO", "AGUARDANDO PEÇA", "FINALIZADA", "CANCELADA"});
        filterStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        filterStatus.setBackground(Color.WHITE);
        filterStatus.addActionListener(e -> filterOS());
        filterBar.add(filterStatus);

        filterBar.add(Box.createHorizontalStrut(8));
        filterBar.add(lbl("Prioridade:"));
        filterPriority = new JComboBox<>(new String[]{"TODAS", "BAIXA", "MÉDIA", "ALTA", "URGENTE"});
        filterPriority.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        filterPriority.setBackground(Color.WHITE);
        filterPriority.addActionListener(e -> filterOS());
        filterBar.add(filterPriority);

        JButton newBtn = btnPrimary("➕ Nova OS", ACCENT);
        newBtn.addActionListener(e -> openOS(false));
        filterBar.add(newBtn);

        panel.add(filterBar, BorderLayout.CENTER);

        // Table
        osModel = new DefaultTableModel(OS_COLS, 0) { public boolean isCellEditable(int r, int c) { return false; }};
        osTable = createTable(osModel);
        setColWidths(osTable, new int[]{70, 100, 100, 130, 150, 100, 95, 75, 75, 65});
        osTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && osTable.getSelectedRow() != -1) showDetails(osTable.getSelectedRow());
        });

        JScrollPane scroll = new JScrollPane(osTable);
        scroll.setBorder(BorderFactory.createCompoundBorder(new ShadowBorder(ACCENT), null));

        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setBackground(BG_MAIN);
        centerPanel.add(scroll, BorderLayout.CENTER);

        // Details
        JPanel det = createCard("Detalhes da Ordem", ACCENT);
        detailsArea = new JTextArea();
        detailsArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        detailsArea.setForeground(TEXT_SECONDARY);
        detailsArea.setBackground(Color.WHITE);
        detailsArea.setEditable(false);
        detailsArea.setLineWrap(true);
        detailsArea.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        det.add(new JScrollPane(detailsArea), BorderLayout.CENTER);
        det.setPreferredSize(new Dimension(0, 120));

        centerPanel.add(det, BorderLayout.SOUTH);
        panel.add(centerPanel, BorderLayout.CENTER);

        // Action bar
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        actions.setBackground(BG_MAIN);
        actions.add(btnSecondary("✏ Editar", WARNING, e -> openOS(true)));
        actions.add(btnSecondary("🗑 Excluir", DANGER, e -> deleteOS()));
        actions.add(btnSecondary("🖨 Imprimir", SUCCESS, e -> printOS()));
        actions.add(btnSecondary("📤 Exportar", PURPLE, e -> exportOS()));
        actions.add(btnSecondary("📊 Relatório", CYAN, e -> printFullReport()));
        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    // ===== CLIENTES =====
    private JPanel buildClientPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_MAIN);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel title = new JLabel("Clientes");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_PRIMARY);
        panel.add(title, BorderLayout.NORTH);

        JPanel topBar = new JPanel(new BorderLayout(10, 0));
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(new ShadowBorder(SUCCESS), BorderFactory.createEmptyBorder(12, 15, 12, 15)));

        JButton addBtn = btnPrimary("➕ Novo Cliente", SUCCESS);
        addBtn.addActionListener(e -> addClient());
        topBar.add(addBtn, BorderLayout.EAST);
        panel.add(topBar, BorderLayout.CENTER);

        clientModel = new DefaultTableModel(CLIENT_COLS, 0) { public boolean isCellEditable(int r, int c) { return false; }};
        clientTable = createTable(clientModel);
        panel.add(new JScrollPane(clientTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        actions.setBackground(BG_MAIN);
        actions.add(btnSecondary("✏ Editar", WARNING, e -> editClient()));
        actions.add(btnSecondary("🗑 Excluir", DANGER, e -> deleteClient()));
        actions.add(btnSecondary("📤 Exportar", PURPLE, e -> exportClients()));
        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    // ===== EQUIPE =====
    private JPanel buildTechPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_MAIN);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel title = new JLabel("Equipe Técnica");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_PRIMARY);
        panel.add(title, BorderLayout.NORTH);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(new ShadowBorder(PURPLE), BorderFactory.createEmptyBorder(12, 15, 12, 15)));

        JButton addBtn = btnPrimary("➕ Novo Técnico", PURPLE);
        addBtn.addActionListener(e -> addTech());
        topBar.add(addBtn, BorderLayout.EAST);
        panel.add(topBar, BorderLayout.CENTER);

        techModel = new DefaultTableModel(TECH_COLS, 0) { public boolean isCellEditable(int r, int c) { return false; }};
        techTable = createTable(techModel);
        panel.add(new JScrollPane(techTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        actions.setBackground(BG_MAIN);
        actions.add(btnSecondary("✏ Editar", WARNING, e -> editTech()));
        actions.add(btnSecondary("🗑 Excluir", DANGER, e -> deleteTech()));
        actions.add(btnSecondary("📅 Agenda", INFO, e -> showSchedule()));
        actions.add(btnSecondary("📤 Exportar", CYAN, e -> exportTechs()));
        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    // ===== FINANCEIRO =====
    private JPanel buildFinancePanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_MAIN);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel title = new JLabel("Financeiro");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_PRIMARY);
        panel.add(title, BorderLayout.NORTH);

        // Stats
        JPanel stats = new JPanel(new GridLayout(1, 4, 12, 0));
        stats.setBackground(BG_MAIN);
        stats.add(createStatCard("Receita Total", "R$ 0", SUCCESS, "\uD83D\uDCB5"));
        stats.add(createStatCard("A Receber", "R$ 0", WARNING, "\u23F3"));
        stats.add(createStatCard("Recebido", "R$ 0", INFO, "\u2705"));
        stats.add(createStatCard("Ticket Médio", "R$ 0", PURPLE, "\uD83D\uDCCA"));
        panel.add(stats, BorderLayout.CENTER);

        finModel = new DefaultTableModel(FIN_COLS, 0) { public boolean isCellEditable(int r, int c) { return false; }};
        finTable = createTable(finModel);
        JScrollPane scroll = new JScrollPane(finTable);
        scroll.setBorder(BorderFactory.createCompoundBorder(new ShadowBorder(INFO), null));
        panel.add(scroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        actions.setBackground(BG_MAIN);
        actions.add(btnSecondary("📊 Relatório", SUCCESS, e -> printFullReport()));
        actions.add(btnSecondary("🖨 Imprimir", INFO, e -> printFinance()));
        actions.add(btnSecondary("📤 CSV", PURPLE, e -> exportFinance()));
        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    // ===== RELATÓRIOS =====
    private JPanel buildReportPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 3, 15, 15));
        panel.setBackground(BG_MAIN);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        panel.add(reportCard("Relatório de Ordens", "Exporta todas as ordens com filtros por período, status e técnico.", ACCENT, "\uD83D\uDCCB", e -> printFullReport()));
        panel.add(reportCard("Relatório de Clientes", "Lista completa com histórico de serviços e total gasto.", SUCCESS, "\uD83D\uDC65", e -> exportClients()));
        panel.add(reportCard("Relatório da Equipe", "Desempenho individual dos técnicos com métricas.", WARNING, "\uD83D\uDD27", e -> exportTechs()));
        panel.add(reportCard("Relatório Financeiro", "Receitas, despesas, lucro e análise de rentabilidade.", PURPLE, "\uD83D\uDCB0", e -> printFinance()));
        panel.add(reportCard("Info do Sistema", "Informações do hardware, sistema e rede.", CYAN, "\uD83D\uDCBB", e -> showSystemInfo()));
        panel.add(reportCard("Backup de Dados", "Realiza backup completo de todos os dados.", ORANGE, "\uD83D\uDCBE", e -> doBackup()));

        return panel;
    }

    private JPanel reportCard(String title, String desc, Color accent, String icon, ActionListener al) {
        JPanel card = new JPanel(new BorderLayout(12, 10));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(new ShadowBorder(accent), BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        top.setBackground(BG_CARD);
        JLabel ic = new JLabel(icon);
        ic.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
        top.add(ic);
        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 14));
        t.setForeground(TEXT_PRIMARY);
        top.add(t);
        card.add(top, BorderLayout.NORTH);

        JLabel d = new JLabel("<html><body style='width:250px'><span style='color:#646E82'>" + desc + "</span></body></html>");
        d.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        card.add(d, BorderLayout.CENTER);

        JButton b = new JButton("Gerar Relatório");
        b.setFont(new Font("Segoe UI", Font.BOLD, 11));
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
        card.add(b, BorderLayout.SOUTH);

        return card;
    }

    // ===== ADMIN =====
    private JPanel buildAdminPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_MAIN);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel title = new JLabel("Administração de Usuários");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_PRIMARY);
        panel.add(title, BorderLayout.NORTH);

        userModel = new DefaultTableModel(USER_COLS, 0) { public boolean isCellEditable(int r, int c) { return false; }};
        userTable = createTable(userModel);
        panel.add(new JScrollPane(userTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        actions.setBackground(BG_MAIN);
        actions.add(btnPrimary("➕ Novo Usuário", ACCENT, e -> addUserDialog()));
        actions.add(btnSecondary("✅ Ativar", SUCCESS, e -> toggleUserStatus(true)));
        actions.add(btnSecondary("⛔ Bloquear", DANGER, e -> toggleUserStatus(false)));
        actions.add(btnSecondary("🗑 Excluir", WARNING, e -> deleteUser()));
        actions.add(btnSecondary("🔄 Atualizar", INFO, e -> loadUsers()));
        panel.add(actions, BorderLayout.SOUTH);

        loadUsers();
        return panel;
    }

    // ===== DIÁLOGO OS =====
    private void openOS(boolean edit) {
        if (edit && osTable.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this, "Selecione uma OS.", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }
        editing = edit;
        editRow = edit ? osTable.getSelectedRow() : -1;

        osDialog = new JDialog(this, edit ? "Editar OS" : "Nova OS", true);
        osDialog.setSize(520, 600);
        osDialog.setLocationRelativeTo(this);
        osDialog.getContentPane().setBackground(Color.WHITE);
        osDialog.setLayout(new BorderLayout(10, 10));

        // Header
        JPanel dlgHeader = new JPanel(new BorderLayout());
        dlgHeader.setBackground(ACCENT);
        dlgHeader.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        JLabel hLbl = new JLabel(edit ? "✏ Editar Ordem de Serviço" : "➕ Nova Ordem de Serviço");
        hLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        hLbl.setForeground(Color.WHITE);
        dlgHeader.add(hLbl, BorderLayout.CENTER);
        osDialog.add(dlgHeader, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        String[] labels = {"Cliente:", "Telefone:", "Email:", "Serviço:", "Técnico:", "Equipamento:", "Descrição:", "Status:", "Prioridade:", "Valor (R$):", "Garantia (dias):"};
        osFields = new JTextField[6];
        int fi = 0;
        int row = 0;

        for (int i = 0; i < labels.length; i++) {
            gbc.gridy = row;
            JLabel lb = new JLabel(labels[i]);
            lb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lb.setForeground(TEXT_SECONDARY);
            form.add(lb, gbc);
            row++;

            gbc.gridy = row;
            gbc.insets = new Insets(2, 0, 8, 0);

            if (labels[i].equals("Status:")) {
                osStatus = new JComboBox<>(STATUS);
                osStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                osStatus.setBackground(Color.WHITE);
                form.add(osStatus, gbc);
            } else if (labels[i].equals("Prioridade:")) {
                osPriority = new JComboBox<>(PRIORIDADES);
                osPriority.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                osPriority.setBackground(Color.WHITE);
                form.add(osPriority, gbc);
            } else if (labels[i].equals("Equipamento:")) {
                osEquip = new JComboBox<>(EQUIPS);
                osEquip.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                osEquip.setBackground(Color.WHITE);
                form.add(osEquip, gbc);
            } else if (labels[i].equals("Descrição:")) {
                osDesc = new JTextArea(3, 15);
                osDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                osDesc.setLineWrap(true);
                osDesc.setBackground(Color.WHITE);
                osDesc.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(6, 10, 6, 10)));
                form.add(new JScrollPane(osDesc), gbc);
            } else {
                osFields[fi] = inputField();
                form.add(osFields[fi], gbc);
                fi++;
            }
            row++;
            gbc.insets = new Insets(5, 0, 5, 0);
        }

        if (edit) {
            osFields[0].setText(v(editRow, 3));
            osFields[2].setText(v(editRow, 4));
            osFields[3].setText(v(editRow, 5));
            osFields[5].setText(v(editRow, 8));
            for (int i = 0; i < STATUS.length; i++) if (STATUS[i].equals(v(editRow, 6))) osStatus.setSelectedIndex(i);
            for (int i = 0; i < PRIORIDADES.length; i++) if (PRIORIDADES[i].equals(v(editRow, 7))) osPriority.setSelectedIndex(i);
        } else {
            osStatus.setSelectedIndex(0);
            osPriority.setSelectedIndex(1);
        }

        // Buttons
        JPanel btns = new JPanel(new GridLayout(1, 2, 10, 0));
        btns.setBackground(Color.WHITE);
        btns.setBorder(BorderFactory.createEmptyBorder(10, 20, 15, 20));

        JButton save = new JButton("💾 Salvar");
        styleBtn(save, ACCENT);
        save.addActionListener(e -> saveOS());
        btns.add(save);

        JButton cancel = new JButton("❌ Cancelar");
        styleBtn(cancel, DANGER);
        cancel.addActionListener(e -> osDialog.dispose());
        btns.add(cancel);

        osDialog.add(new JScrollPane(form), BorderLayout.CENTER);
        osDialog.add(btns, BorderLayout.SOUTH);
        osDialog.setVisible(true);
    }

    private void saveOS() {
        String cl = osFields[0].getText().trim();
        String sv = osFields[2].getText().trim();
        String tc = osFields[3].getText().trim();
        String vl = osFields[5].getText().trim();
        if (cl.isEmpty() || sv.isEmpty() || tc.isEmpty()) {
            JOptionPane.showMessageDialog(osDialog, "Preencha Cliente, Serviço e Técnico.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String st = (String) osStatus.getSelectedItem();
        String pr = (String) osPriority.getSelectedItem();
        String vlF = vl.isEmpty() ? "0,00" : vl;
        String now = now();

        if (editing) {
            osModel.setValueAt(cl, editRow, 3);
            osModel.setValueAt(sv, editRow, 4);
            osModel.setValueAt(tc, editRow, 5);
            osModel.setValueAt(st, editRow, 6);
            osModel.setValueAt(pr, editRow, 7);
            osModel.setValueAt(vlF, editRow, 8);
            if (st.equals("FINALIZADA") && (osModel.getValueAt(editRow, 2) == null || osModel.getValueAt(editRow, 2).toString().isEmpty()))
                osModel.setValueAt(now, editRow, 2);
            addLog("OS atualizada: " + osModel.getValueAt(editRow, 0));
        } else {
            String num = String.format("OS-%04d", ++osCounter);
            String dt = st.equals("FINALIZADA") ? now : "";
            osModel.addRow(new Object[]{num, now, dt, cl, sv, tc, st, pr, vlF, "90d"});
            addLog("Nova OS: " + num + " - " + cl);
        }
        osDialog.dispose();
        refreshFinance();
        refreshDashboardStats();
    }

    // ===== AÇÕES =====
    private void deleteOS() {
        int r = osTable.getSelectedRow();
        if (r == -1) { JOptionPane.showMessageDialog(this, "Selecione uma OS.", "Atenção", JOptionPane.WARNING_MESSAGE); return; }
        if (JOptionPane.showConfirmDialog(this, "Excluir " + osModel.getValueAt(r, 0) + "?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            addLog("OS excluída: " + osModel.getValueAt(r, 0));
            osModel.removeRow(r);
            detailsArea.setText("");
            refreshFinance();
            refreshDashboardStats();
        }
    }

    private void filterOS() {
        String s = searchOS.getText().toLowerCase();
        String st = (String) filterStatus.getSelectedItem();
        String pr = (String) filterPriority.getSelectedItem();
        osModel.setRowCount(0);
        for (Object[] row : sampleOS) {
            boolean mS = s.isEmpty(), mSt = st.equals("TODOS"), mPr = pr.equals("TODAS");
            if (!mS) for (Object c : row) if (c.toString().toLowerCase().contains(s)) { mS = true; break; }
            if (!mSt) mSt = row[6].equals(st);
            if (!mPr) mPr = row[7].equals(pr);
            if (mS && mSt && mPr) osModel.addRow(row);
        }
        refreshFinance();
        refreshDashboardStats();
    }

    private void showDetails(int r) {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════\n");
        sb.append("  ORDEM DE SERVIÇO\n");
        sb.append("═══════════════════════════════════════\n\n");
        for (int i = 0; i < OS_COLS.length; i++)
            sb.append(String.format("  %-16s : %s\n", OS_COLS[i], osModel.getValueAt(r, i)));
        detailsArea.setText(sb.toString());
    }

    private void printOS() {
        int r = osTable.getSelectedRow();
        if (r == -1) { JOptionPane.showMessageDialog(this, "Selecione uma OS.", "Atenção", JOptionPane.WARNING_MESSAGE); return; }
        StringBuilder sb = new StringBuilder();
        sb.append("┌──────────────────────────────────────┐\n");
        sb.append("│   ORDEM DE SERVIÇO - TechSuite Pro   │\n");
        sb.append("├──────────────────────────────────────┤\n");
        for (int i = 0; i < OS_COLS.length; i++)
            sb.append(String.format("│ %-10s : %-25s│\n", OS_COLS[i], osModel.getValueAt(r, i)));
        sb.append("└──────────────────────────────────────┘\n");
        JOptionPane.showMessageDialog(this, sb.toString(), "OS " + osModel.getValueAt(r, 0), JOptionPane.INFORMATION_MESSAGE);
    }

    private void exportOS() { exportData(osModel, OS_COLS, "ordens_servico.csv"); }
    private void exportClients() { exportData(clientModel, CLIENT_COLS, "clientes.csv"); }
    private void exportTechs() { exportData(techModel, TECH_COLS, "equipe.csv"); }
    private void exportFinance() { exportData(finModel, FIN_COLS, "financeiro.csv"); }

    // ===== CLIENTES =====
    private void addClient() {
        JTextField n = inputField(), t = inputField(), e = inputField(), en = inputField();
        JPanel p = formPanel(new Object[]{lbl("Nome:"), n, lbl("Telefone:"), t, lbl("Email:"), e, lbl("Endereço:"), en}, 4);
        if (JOptionPane.showConfirmDialog(this, p, "Novo Cliente", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            clientModel.addRow(new Object[]{String.format("CL-%03d", clientModel.getRowCount()+1), n.getText(), t.getText(), e.getText(), en.getText(), dateOnly()});
            addLog("Cliente: " + n.getText());
            refreshDashboardStats();
        }
    }

    private void editClient() { JOptionPane.showMessageDialog(this, "Selecione e edite o cliente.", "Editar", JOptionPane.INFORMATION_MESSAGE); }

    private void deleteClient() {
        int r = clientTable.getSelectedRow();
        if (r == -1) { JOptionPane.showMessageDialog(this, "Selecione um cliente.", "Atenção", JOptionPane.WARNING_MESSAGE); return; }
        if (JOptionPane.showConfirmDialog(this, "Excluir?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            addLog("Cliente excluído: " + clientModel.getValueAt(r, 1));
            clientModel.removeRow(r);
        }
    }

    // ===== EQUIPE =====
    private void addTech() {
        JTextField n = inputField(), e = inputField();
        JPanel p = formPanel(new Object[]{lbl("Nome:"), n, lbl("Especialidade:"), e}, 2);
        if (JOptionPane.showConfirmDialog(this, p, "Novo Técnico", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            techModel.addRow(new Object[]{String.format("TEC-%03d", techModel.getRowCount()+1), n.getText(), e.getText(), 0, 0, "SIM"});
            addLog("Técnico: " + n.getText());
        }
    }

    private void editTech() { JOptionPane.showMessageDialog(this, "Edite o técnico.", "Editar", JOptionPane.INFORMATION_MESSAGE); }

    private void deleteTech() {
        int r = techTable.getSelectedRow();
        if (r == -1) { JOptionPane.showMessageDialog(this, "Selecione um técnico.", "Atenção", JOptionPane.WARNING_MESSAGE); return; }
        if (JOptionPane.showConfirmDialog(this, "Excluir?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            addLog("Técnico excluído");
            techModel.removeRow(r);
        }
    }

    private void showSchedule() { JOptionPane.showMessageDialog(this, "📅 Agenda - Em desenvolvimento.", "Agenda", JOptionPane.INFORMATION_MESSAGE); }

    // ===== FINANCEIRO =====
    private void refreshFinance() {
        finModel.setRowCount(0);
        for (int i = 0; i < osModel.getRowCount(); i++) {
            String st = (String) osModel.getValueAt(i, 6);
            String pg = st.equals("FINALIZADA") ? "PAGO" : st.equals("CANCELADA") ? "CANCELADO" : "PENDENTE";
            finModel.addRow(new Object[]{osModel.getValueAt(i, 0), osModel.getValueAt(i, 3), osModel.getValueAt(i, 4),
                "R$ " + osModel.getValueAt(i, 8), pg, osModel.getValueAt(i, 2)});
        }
    }

    private void printFinance() {
        double total = 0, pago = 0, pendente = 0;
        for (int i = 0; i < finModel.getRowCount(); i++) {
            String val = finModel.getValueAt(i, 3).toString().replace("R$ ", "").replace(",", ".");
            double v = Double.parseDouble(val);
            total += v;
            if (finModel.getValueAt(i, 4).equals("PAGO")) pago += v; else if (finModel.getValueAt(i, 4).equals("PENDENTE")) pendente += v;
        }
        String r = String.format("RELATÓRIO FINANCEIRO\n\nTotal: R$ %.2f\nRecebido: R$ %.2f\nPendente: R$ %.2f\nTicket Médio: R$ %.2f\nOS: %d",
                total, pago, pendente, finModel.getRowCount() > 0 ? total / finModel.getRowCount() : 0, finModel.getRowCount());
        JOptionPane.showMessageDialog(this, r, "Financeiro", JOptionPane.INFORMATION_MESSAGE);
    }

    // ===== RELATÓRIO COMPLETO =====
    private void printFullReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("╔═══════════════════════════════════════════╗\n");
        sb.append("║   RELATÓRIO COMPLETO - TechSuite Pro      ║\n");
        sb.append("╠═══════════════════════════════════════════╣\n");

        int ab = 0, and = 0, fin = 0, can = 0;
        double total = 0, recebido = 0;
        for (int i = 0; i < osModel.getRowCount(); i++) {
            String st = (String) osModel.getValueAt(i, 6);
            double v = Double.parseDouble(osModel.getValueAt(i, 8).toString().replace(",", "."));
            total += v;
            if (st.equals("ABERTA")) ab++;
            else if (st.equals("EM ANDAMENTO")) and++;
            else if (st.equals("FINALIZADA")) { fin++; recebido += v; }
            else if (st.equals("CANCELADA")) can++;
        }

        sb.append(String.format("║ Total OS: %d | Abertas: %d | Andamento: %d  ║\n", osModel.getRowCount(), ab, and));
        sb.append(String.format("║ Finalizadas: %d | Canceladas: %d            ║\n", fin, can));
        sb.append(String.format("║ Receita: R$ %.2f | Recebido: R$ %.2f         ║\n", total, recebido));
        sb.append(String.format("║ Clientes: %d | Técnicos: %d                 ║\n", clientModel.getRowCount(), techModel.getRowCount()));
        sb.append("╚═══════════════════════════════════════════╝\n");

        JOptionPane.showMessageDialog(this, sb.toString(), "Relatório", JOptionPane.INFORMATION_MESSAGE);
    }

    // ===== ADMIN =====
    private void loadUsers() {
        if (userModel == null) return;
        userModel.setRowCount(0);
        File uf = new File(System.getProperty("user.home") + "/.techsuite/users.dat");
        if (!uf.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(uf))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p.length >= 7) userModel.addRow(p);
            }
        } catch (Exception ignored) {}
    }

    private void addUserDialog() {
        JDialog dlg = new JDialog(this, "Novo Usuário", true);
        dlg.setSize(420, 380);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(Color.WHITE);
        dlg.setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.weightx = 1.0;

        JTextField uId = inputField(), uName = inputField(), uEmail = inputField();
        JPasswordField uPass = new JPasswordField();
        uPass.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        uPass.setBackground(Color.WHITE);
        uPass.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        JComboBox<String> uRole = new JComboBox<>(new String[]{"USER", "ADMIN"});
        uRole.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        uRole.setBackground(Color.WHITE);

        int r = 0;
        gbc.gridy = r; form.add(lbl("Usuário:"), gbc); r++; gbc.gridy = r; form.add(uId, gbc); r++;
        gbc.gridy = r; form.add(lbl("Senha:"), gbc); r++; gbc.gridy = r; form.add(uPass, gbc); r++;
        gbc.gridy = r; form.add(lbl("Nome:"), gbc); r++; gbc.gridy = r; form.add(uName, gbc); r++;
        gbc.gridy = r; form.add(lbl("Email:"), gbc); r++; gbc.gridy = r; form.add(uEmail, gbc); r++;
        gbc.gridy = r; form.add(lbl("Nível:"), gbc); r++; gbc.gridy = r; form.add(uRole, gbc); r++;

        JPanel btns = new JPanel(new GridLayout(1, 2, 10, 0));
        btns.setBackground(Color.WHITE);

        JButton save = new JButton("💾 Cadastrar");
        styleBtn(save, ACCENT);
        save.addActionListener(e -> {
            String uid = uId.getText().trim(), pass = new String(uPass.getPassword());
            String name = uName.getText().trim(), email = uEmail.getText().trim();
            String role = (String) uRole.getSelectedItem();
            if (uid.isEmpty() || pass.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Preencha os campos.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try (FileWriter fw = new FileWriter(System.getProperty("user.home") + "/.techsuite/users.dat", true)) {
                fw.write(String.format("%d|%s|%s|%s|%s|%s|ATIVO|%s\n",
                    userModel.getRowCount()+1, uid, name, hashStr(pass), email, role, now()));
                addLog("Usuário criado: " + uid);
                loadUsers();
                dlg.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        btns.add(save);

        JButton cancel = new JButton("Cancelar");
        styleBtn(cancel, DANGER);
        cancel.addActionListener(e -> dlg.dispose());
        btns.add(cancel);

        dlg.add(new JScrollPane(form), BorderLayout.CENTER);
        dlg.add(btns, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void toggleUserStatus(boolean enable) {
        int r = userTable.getSelectedRow();
        if (r == -1) { JOptionPane.showMessageDialog(this, "Selecione um usuário.", "Atenção", JOptionPane.WARNING_MESSAGE); return; }
        userModel.setValueAt(enable ? "ATIVO" : "BLOQUEADO", r, 5);
        addLog("Usuário " + (enable ? "ativado" : "bloqueado"));
    }

    private void deleteUser() {
        int r = userTable.getSelectedRow();
        if (r == -1) { JOptionPane.showMessageDialog(this, "Selecione um usuário.", "Atenção", JOptionPane.WARNING_MESSAGE); return; }
        if (JOptionPane.showConfirmDialog(this, "Excluir usuário?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            addLog("Usuário excluído: " + userModel.getValueAt(r, 1));
            userModel.removeRow(r);
        }
    }

    // ===== FERRAMENTAS =====
    private void showSystemInfo() {
        String info = String.format("Sistema: %s %s\nJava: %s\nUsuário: %s\nMemória: %d MB / %d MB\nProcessadores: %d",
            System.getProperty("os.name"), System.getProperty("os.version"),
            System.getProperty("java.version"), System.getProperty("user.name"),
            Runtime.getRuntime().freeMemory()/1024/1024, Runtime.getRuntime().totalMemory()/1024/1024,
            Runtime.getRuntime().availableProcessors());
        JOptionPane.showMessageDialog(this, info, "Info do Sistema", JOptionPane.INFORMATION_MESSAGE);
    }

    private void doBackup() {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String dest = fc.getSelectedFile().getPath() + "/backup_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".txt";
                FileWriter fw = new FileWriter(dest);
                fw.write("BACKUP - " + now() + "\n\n");
                fw.write("=== ORDENS ===\n");
                for (int i = 0; i < osModel.getRowCount(); i++) {
                    for (int j = 0; j < osModel.getColumnCount(); j++) fw.write(osModel.getValueAt(i, j) + (j < osModel.getColumnCount()-1 ? "|" : ""));
                    fw.write("\n");
                }
                fw.write("\n=== CLIENTES ===\n");
                for (int i = 0; i < clientModel.getRowCount(); i++) {
                    for (int j = 0; j < clientModel.getColumnCount(); j++) fw.write(clientModel.getValueAt(i, j) + (j < clientModel.getColumnCount()-1 ? "|" : ""));
                    fw.write("\n");
                }
                fw.close();
                addLog("Backup: " + dest);
                JOptionPane.showMessageDialog(this, "Backup realizado!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE); }
        }
    }

    // ===== HELPERS UI =====
    private JTable createTable(DefaultTableModel m) {
        JTable t = new JTable(m);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        t.setForeground(TEXT_PRIMARY);
        t.setBackground(Color.WHITE);
        t.setRowHeight(30);
        t.setGridColor(new Color(230, 230, 235));
        t.setSelectionBackground(new Color(79, 70, 229, 30));
        t.setSelectionForeground(TEXT_PRIMARY);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        t.getTableHeader().setBackground(new Color(245, 245, 250));
        t.getTableHeader().setForeground(TEXT_SECONDARY);
        t.getTableHeader().setPreferredSize(new Dimension(0, 35));
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
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(8, 10, 8, 10)));
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
        b.setPreferredSize(new Dimension(130, 32));
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

    private JLabel lbl(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(TEXT_SECONDARY);
        return l;
    }

    private JPanel formPanel(Object[] comps, int rows) {
        JPanel p = new JPanel(new GridLayout(rows, 2, 8, 8));
        p.setBackground(Color.WHITE);
        for (Object c : comps) {
            if (c instanceof JLabel) ((JLabel) c).setForeground(TEXT_SECONDARY);
            if (c instanceof Component) p.add((Component) c);
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
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE); }
        }
    }

    private void addLog(String msg) {
        if (activityLog != null) {
            activityLog.append("[" + timeOnly() + "] " + msg + "\n");
            activityLog.setCaretPosition(activityLog.getDocument().getLength());
        }
    }

    private void refreshDashboardStats() {
        // Update stat cards
        SwingUtilities.invokeLater(() -> {
            if (osModel != null) {
                int ab = 0, and = 0, fin = 0;
                double fat = 0;
                for (int i = 0; i < osModel.getRowCount(); i++) {
                    String s = (String) osModel.getValueAt(i, 6);
                    if (s.equals("ABERTA")) ab++;
                    else if (s.equals("EM ANDAMENTO")) and++;
                    else if (s.equals("FINALIZADA")) fin++;
                    double v = Double.parseDouble(osModel.getValueAt(i, 8).toString().replace(",", "."));
                    fat += v;
                }
            }
        });
    }

    private String v(int r, int c) { return osModel.getValueAt(r, c) != null ? osModel.getValueAt(r, c).toString() : ""; }
    private String now() { return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()); }
    private String dateOnly() { return new SimpleDateFormat("dd/MM/yyyy").format(new Date()); }
    private String timeOnly() { return new SimpleDateFormat("HH:mm:ss").format(new Date()); }
    private String hashStr(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(s.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString().substring(0, 16);
        } catch (Exception e) { return s; }
    }

    // ===== DADOS =====
    private void loadData() {
        sampleOS = new Object[][]{
            {"OS-0001", "12/04/2026 08:30", "12/04/2026 14:00", "João Silva", "Formatação e backup", "Carlos Tech", "FINALIZADA", "BAIXA", "150,00", "90d"},
            {"OS-0002", "12/04/2026 09:15", "", "Maria Santos", "Troca de tela notebook", "Ana Repair", "EM ANDAMENTO", "ALTA", "450,00", "90d"},
            {"OS-0003", "12/04/2026 10:00", "", "Pedro Oliveira", "Remoção de vírus", "Carlos Tech", "ABERTA", "MÉDIA", "120,00", "30d"},
            {"OS-0004", "12/04/2026 10:45", "", "Empresa ABC", "Configuração de rede", "Roberto Net", "AGUARDANDO PEÇA", "URGENTE", "800,00", "180d"},
            {"OS-0005", "12/04/2026 11:30", "", "Lucas Ferreira", "Upgrade SSD + RAM", "Ana Repair", "EM ANDAMENTO", "ALTA", "650,00", "90d"},
            {"OS-0006", "12/04/2026 13:00", "", "Fernanda Costa", "Recuperação de dados", "Carlos Tech", "ABERTA", "URGENTE", "350,00", "30d"},
            {"OS-0007", "12/04/2026 14:00", "", "Tech Store ME", "Manutenção 10 PCs", "Roberto Net", "ABERTA", "MÉDIA", "1200,00", "90d"},
            {"OS-0008", "12/04/2026 14:30", "12/04/2026 16:00", "Ricardo Almeida", "Troca placa de vídeo", "Ana Repair", "FINALIZADA", "BAIXA", "900,00", "90d"},
            {"OS-0009", "12/04/2026 15:00", "", "Juliana Pereira", "Limpeza e manutenção", "Carlos Tech", "CANCELADA", "BAIXA", "80,00", "30d"},
        };
        osCounter = 9;
        for (Object[] r : sampleOS) osModel.addRow(r);
        refreshFinance();

        Object[][] clients = {
            {"CL-001", "João Silva", "(11) 98765-4321", "joao@email.com", "Rua A, 123", "01/03/2026"},
            {"CL-002", "Maria Santos", "(11) 91234-5678", "maria@email.com", "Av. B, 456", "15/03/2026"},
            {"CL-003", "Pedro Oliveira", "(21) 99876-5432", "pedro@email.com", "Rua C, 789", "20/03/2026"},
            {"CL-004", "Empresa ABC Ltda", "(11) 3456-7890", "contato@abc.com", "Rua D, 100", "05/04/2026"},
            {"CL-005", "Lucas Ferreira", "(31) 98888-7777", "lucas@email.com", "Av. E, 200", "10/04/2026"},
        };
        for (Object[] r : clients) clientModel.addRow(r);

        Object[][] techs = {
            {"TEC-001", "Carlos Tech", "Formatação e Software", 3, 45, "SIM"},
            {"TEC-002", "Ana Repair", "Hardware e Telas", 2, 38, "SIM"},
            {"TEC-003", "Roberto Net", "Redes e Servidores", 2, 27, "NÃO"},
        };
        for (Object[] r : techs) techModel.addRow(r);

        addLog("Sistema por " + currentUser);
        addLog("9 ordens | 5 clientes | 3 técnicos");
    }

    // ===== SHADOW BORDER (efeito card moderno) =====
    private static class ShadowBorder extends AbstractBorder {
        private Color accent;
        ShadowBorder(Color a) { accent = a; }
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Sombra
            g2.setColor(new Color(0, 0, 0, 15));
            g2.fillRoundRect(x + 2, y + 2, w - 2, h - 2, 12, 12);
            // Card
            g2.setColor(BG_CARD);
            g2.fillRoundRect(x, y, w - 4, h - 4, 12, 12);
            // Borda superior colorida
            g2.setColor(accent);
            g2.fillRoundRect(x, y, w - 4, 4, 4, 4);
            // Borda
            g2.setColor(BORDER);
            g2.drawRoundRect(x, y, w - 5, h - 5, 12, 12);
            g2.dispose();
        }
        public Insets getBorderInsets(Component c) { return new Insets(4, 4, 4, 4); }
    }

    // ===== MAIN =====
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        new File(System.getProperty("user.home") + "/.techsuite").mkdirs();

        // Login simples
        String user = JOptionPane.showInputDialog(null, "Usuário:", "TechSuite Pro - Login", JOptionPane.QUESTION_MESSAGE);
        if (user == null) System.exit(0);

        String pass = JOptionPane.showInputDialog(null, "Senha:", "TechSuite Pro - Login", JOptionPane.QUESTION_MESSAGE);
        if (pass == null) System.exit(0);

        if (!user.equals("admin") || !pass.equals("admin123")) {
            // Tentar arquivo
            if (!checkUser(user, pass)) {
                JOptionPane.showMessageDialog(null, "Usuário ou senha incorretos!", "Erro", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
        }

        boolean admin = user.equals("admin");
        SwingUtilities.invokeLater(() -> {
            HackPanel p = new HackPanel(user, admin);
            p.setVisible(true);
        });
    }

    private static boolean checkUser(String user, String pass) {
        File uf = new File(System.getProperty("user.home") + "/.techsuite/users.dat");
        if (!uf.exists()) return false;
        try (BufferedReader br = new BufferedReader(new FileReader(uf))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p.length >= 4 && p[1].equals(user) && p[3].equals(hashStrStatic(pass))) return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    private static String hashStrStatic(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(s.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString().substring(0, 16);
        } catch (Exception e) { return s; }
    }
}
