import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.io.*;
import java.sql.*;

public class HackPanel extends JFrame {

    // ===== CORES =====
    private static final Color BG_MAIN = new Color(245, 247, 250);
    private static final Color BG_SIDEBAR = new Color(15, 15, 35);
    private static final Color BG_SIDEBAR_HOVER = new Color(30, 30, 55);
    private static final Color BG_CARD = Color.WHITE;
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

    // ===== MODELO OS =====
    static class OS {
        String id, dataAbertura, dataConclusao, cliente, telefone, email;
        String servico, tecnico, equipamento, descricao;
        String status, prioridade, valor, garantia;
        private boolean saved = false;

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

        boolean existsInDatabase() { return saved; }
        void setSaved(boolean saved) { this.saved = saved; }

        Object[] toRow() {
            return new Object[]{id, dataAbertura, dataConclusao, cliente, servico, tecnico, status, prioridade, valor, garantia};
        }
    }

    // ===== COMPONENTES =====
    private JPanel contentPanel, sidebar;
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

        // INICIALIZAR TODOS OS MODELS ANTES DE TUDO
        osModel = new DefaultTableModel(OS_COLS, 0) { public boolean isCellEditable(int r, int c) { return false; }};
        clientModel = new DefaultTableModel(CLIENT_COLS, 0) { public boolean isCellEditable(int r, int c) { return false; }};
        techModel = new DefaultTableModel(TECH_COLS, 0) { public boolean isCellEditable(int r, int c) { return false; }};
        finModel = new DefaultTableModel(FIN_COLS, 0) { public boolean isCellEditable(int r, int c) { return false; }};
        if (admin) userModel = new DefaultTableModel(USER_COLS, 0) { public boolean isCellEditable(int r, int c) { return false; }};

        initFolders();
        DatabaseManager.initializeDatabase();
        loadDataFromDatabase();
        setupFrame();
    }

    private void initFolders() {
        new File(System.getProperty("user.home") + "/.techsuite").mkdirs();
    }

    // ===== SETUP FRAME =====
    private void setupFrame() {
        setTitle("TechSuite Pro - " + currentUser);
        setSize(1450, 880);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_MAIN);

        sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        add(createHeader(), BorderLayout.NORTH);

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

        JPanel logo = new JPanel(new BorderLayout());
        logo.setBackground(BG_SIDEBAR);
        logo.setBorder(BorderFactory.createEmptyBorder(25, 20, 25, 20));
        JLabel t = new JLabel(" TechSuite Pro");
        t.setFont(new Font("Segoe UI", Font.BOLD, 20));
        t.setForeground(Color.WHITE);
        logo.add(t, BorderLayout.CENTER);
        JLabel s = new JLabel(" Gestão de Serviços v2.0");
        s.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        s.setForeground(new Color(140, 140, 190));
        logo.add(s, BorderLayout.SOUTH);
        panel.add(logo, BorderLayout.NORTH);

        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBackground(BG_SIDEBAR);
        menu.setBorder(BorderFactory.createEmptyBorder(15, 12, 15, 12));

        menu.add(menuBtn("📊  Dashboard", true, e -> showPage("dashboard")));
        menu.add(menuBtn("📋  Ordens de Serviço", false, e -> showPage("ordens")));
        menu.add(menuBtn("👥  Clientes", false, e -> showPage("clientes")));
        menu.add(menuBtn("🔧  Equipe Técnica", false, e -> showPage("equipe")));
        menu.add(menuBtn("💰  Financeiro", false, e -> showPage("financeiro")));
        menu.add(menuBtn("📈  Relatórios", false, e -> showPage("relatorios")));
        if (isAdmin) {
            menu.add(Box.createVerticalStrut(8));
            menu.add(menuBtn("⚙️  Administração", false, e -> showPage("admin")));
        }
        panel.add(menu, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(10, 10, 25));
        footer.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JLabel u = new JLabel("  " + currentUser);
        u.setFont(new Font("Segoe UI", Font.BOLD, 12));
        u.setForeground(Color.WHITE);
        footer.add(u, BorderLayout.CENTER);
        JLabel r = new JLabel("  " + (isAdmin ? "Administrador" : "Usuário"));
        r.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        r.setForeground(new Color(140, 140, 190));
        footer.add(r, BorderLayout.SOUTH);
        panel.add(footer, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel menuBtn(String text, boolean active, ActionListener al) {
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
        return item;
    }

    // ===== HEADER =====
    private JPanel createHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(Color.WHITE);
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        h.setPreferredSize(new Dimension(0, 60));

        headerTitle = new JLabel("  Dashboard");
        headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerTitle.setForeground(TEXT_PRIMARY);
        h.add(headerTitle, BorderLayout.CENTER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        right.setBackground(Color.WHITE);
        JLabel d = new JLabel(new SimpleDateFormat("dd/MM/yyyy  HH:mm").format(new Date()));
        d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        d.setForeground(TEXT_SECONDARY);
        right.add(d);

        JButton logout = new JButton("  Sair");
        styleBtn(logout, DANGER);
        logout.setPreferredSize(new Dimension(80, 32));
        logout.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Deseja sair?", "Logout", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
                System.exit(0);
        });
        right.add(logout);
        h.add(right, BorderLayout.EAST);
        return h;
    }

    private void showPage(String page) {
        cardLayout.show(contentPanel, page);
        if (page.equals("dashboard")) headerTitle.setText("  Dashboard");
        else if (page.equals("ordens")) headerTitle.setText("  Ordens de Serviço");
        else if (page.equals("clientes")) headerTitle.setText("  Clientes");
        else if (page.equals("equipe")) headerTitle.setText("  Equipe Técnica");
        else if (page.equals("financeiro")) headerTitle.setText("  Financeiro");
        else if (page.equals("relatorios")) headerTitle.setText("  Relatórios");
        else if (page.equals("admin")) headerTitle.setText("  Administração");
    }

    // ===== DASHBOARD =====
    private JPanel buildDashboard() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(BG_MAIN);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JPanel welcome = new JPanel(new BorderLayout(5, 5));
        welcome.setBackground(BG_MAIN);
        JLabel w = new JLabel("Bom dia, " + currentUser + "! 👋");
        w.setFont(new Font("Segoe UI", Font.BOLD, 24));
        w.setForeground(TEXT_PRIMARY);
        welcome.add(w, BorderLayout.CENTER);
        JLabel dl = new JLabel(new SimpleDateFormat("EEEE, dd 'de' MMMM 'de' yyyy").format(new Date()));
        dl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dl.setForeground(TEXT_SECONDARY);
        welcome.add(dl, BorderLayout.SOUTH);
        panel.add(welcome, BorderLayout.NORTH);

        JPanel stats = new JPanel(new GridLayout(1, 5, 15, 0));
        stats.setBackground(BG_MAIN);
        stats.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        stats.add(statCard("OS Abertas", String.valueOf(countOS("ABERTA")), WARNING, "📋"));
        stats.add(statCard("Em Andamento", String.valueOf(countOS("EM ANDAMENTO")), INFO, "🔧"));
        stats.add(statCard("Finalizadas", String.valueOf(countOS("FINALIZADA")), SUCCESS, "✅"));
        stats.add(statCard("Faturamento", totalFaturamento(), SUCCESS, "💰"));
        stats.add(statCard("Clientes", String.valueOf(clientModel.getRowCount()), PURPLE, "👥"));
        panel.add(stats, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new GridLayout(1, 2, 20, 0));
        bottom.setBackground(BG_MAIN);

        JPanel actions = card("Ações Rápidas", ACCENT);
        JPanel ag = new JPanel(new GridLayout(2, 2, 10, 10));
        ag.setBackground(BG_CARD);
        ag.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        ag.add(qBtn("➕ Nova OS", ACCENT, e -> { showPage("ordens"); openOSDialog(); }));
        ag.add(qBtn("👤 Novo Cliente", SUCCESS, e -> { showPage("clientes"); addClient(); }));
        ag.add(qBtn("🔧 Novo Técnico", PURPLE, e -> { showPage("equipe"); addTech(); }));
        ag.add(qBtn("📊 Relatório", CYAN, e -> showPage("relatorios")));
        actions.add(ag, BorderLayout.CENTER);

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

    // ===== OS PANEL =====
    private JPanel buildOSPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_MAIN);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("📋 Gerenciar Ordens de Serviço");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_PRIMARY);
        panel.add(title, BorderLayout.NORTH);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(BorderFactory.createCompoundBorder(new CardBorder(ACCENT), BorderFactory.createEmptyBorder(12, 15, 12, 15)));

        toolbar.add(lbl("🔍 Buscar:"));
        searchOS = new JTextField(15);
        searchOS.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchOS.setBackground(Color.WHITE);
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

        osTable = new JTable(osModel);
        styleTable(osTable);
        osTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && osTable.getSelectedRow() != -1) showDetails(osTable.getSelectedRow());
        });
        JScrollPane scroll = new JScrollPane(osTable);
        scroll.setBorder(BorderFactory.createCompoundBorder(new CardBorder(ACCENT), null));

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setBackground(BG_MAIN);

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

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBackground(BG_MAIN);
        actions.add(btnSecondary("✏️ Editar", WARNING, e -> editSelectedOS()));
        actions.add(btnSecondary("✅ Finalizar", SUCCESS, e -> finalizeSelectedOS()));
        actions.add(btnSecondary("🗑️ Excluir", DANGER, e -> deleteSelectedOS()));
        actions.add(btnSecondary("🖨️ Imprimir", INFO, e -> printSelectedOS()));
        actions.add(btnSecondary("📤 Exportar", PURPLE, e -> exportData(osModel, OS_COLS, "ordens.csv")));
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
        JButton add = btnPrimary("➕ Novo Cliente", SUCCESS);
        add.addActionListener(e -> addClient());
        topBar.add(add, BorderLayout.EAST);
        panel.add(topBar, BorderLayout.CENTER);

        clientTable = new JTable(clientModel);
        styleTable(clientTable);
        panel.add(new JScrollPane(clientTable), BorderLayout.CENTER);

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
        JButton add = btnPrimary("➕ Novo Técnico", PURPLE);
        add.addActionListener(e -> addTech());
        topBar.add(add, BorderLayout.EAST);
        panel.add(topBar, BorderLayout.CENTER);

        techTable = new JTable(techModel);
        styleTable(techTable);
        panel.add(new JScrollPane(techTable), BorderLayout.CENTER);

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

        finTable = new JTable(finModel);
        styleTable(finTable);
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

        panel.add(reportCard("📋 Relatório de Ordens", "Todas as ordens com detalhes", ACCENT, e -> showFullReport()));
        panel.add(reportCard("👥 Relatório de Clientes", "Lista completa de clientes", SUCCESS, e -> showClientReport()));
        panel.add(reportCard("🔧 Relatório da Equipe", "Desempenho dos técnicos", WARNING, e -> showTechReport()));
        panel.add(reportCard("💰 Relatório Financeiro", "Receitas e despesas", PURPLE, e -> showFinanceReport()));
        panel.add(reportCard("💾 Backup de Dados", "Backup completo", ORANGE, e -> doBackup()));
        panel.add(reportCard("ℹ️ Info do Sistema", "Dados do sistema", CYAN, e -> showSystemInfo()));

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

        userTable = new JTable(userModel);
        styleTable(userTable);
        panel.add(new JScrollPane(userTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBackground(BG_MAIN);
        actions.add(btnPrimary("➕ Novo Usuário", ACCENT, e -> addUserDialog()));
        actions.add(btnSecondary("🔄 Atualizar", INFO, e -> loadUsers()));
        panel.add(actions, BorderLayout.SOUTH);

        loadUsers();
        return panel;
    }

    // ===== CRUD OS =====
    private void openOSDialog() {
        JDialog dlg = new JDialog(this, "➕ Nova Ordem de Serviço", true);
        dlg.setSize(560, 660);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(Color.WHITE);
        dlg.setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ACCENT);
        header.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        JLabel h = new JLabel("➕ Nova Ordem de Serviço");
        h.setFont(new Font("Segoe UI", Font.BOLD, 17));
        h.setForeground(Color.WHITE);
        header.add(h, BorderLayout.CENTER);
        dlg.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 0, 3, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JTextField fCl = inputField(), fTel = inputField(), fEm = inputField();
        JTextField fSv = inputField(), fTc = inputField(), fVl = inputField();
        JComboBox<String> cEq = combo(EQUIPS);
        JComboBox<String> cSt = combo(new String[]{"ABERTA", "EM ANDAMENTO", "AGUARDANDO PEÇA"});
        JComboBox<String> cPr = combo(PRIORIDADES);
        JTextField fGr = inputField(); fGr.setText("90");
        JTextArea fDs = new JTextArea(3, 15);
        fDs.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        fDs.setLineWrap(true);
        fDs.setBackground(Color.WHITE);
        fDs.setBorder(BorderFactory.createLineBorder(BORDER));

        int r = 0;
        addField(form, gbc, r++, "Cliente:", fCl);
        addField(form, gbc, r++, "Telefone:", fTel);
        addField(form, gbc, r++, "Email:", fEm);
        addField(form, gbc, r++, "Serviço:", fSv);
        addField(form, gbc, r++, "Técnico:", fTc);
        addField(form, gbc, r++, "Equipamento:", cEq);
        addField(form, gbc, r++, "Descrição:", new JScrollPane(fDs));
        addField(form, gbc, r++, "Status:", cSt);
        addField(form, gbc, r++, "Prioridade:", cPr);
        addField(form, gbc, r++, "Valor (R$):", fVl);
        addField(form, gbc, r++, "Garantia (dias):", fGr);

        JPanel btns = new JPanel(new GridLayout(1, 2, 10, 0));
        btns.setBackground(Color.WHITE);
        btns.setBorder(BorderFactory.createEmptyBorder(8, 22, 18, 22));

        JButton save = new JButton("💾 Salvar OS");
        styleBtn(save, ACCENT);
        save.addActionListener(e -> {
            String cl = fCl.getText().trim(), sv = fSv.getText().trim(), tc = fTc.getText().trim();
            if (cl.isEmpty() || sv.isEmpty() || tc.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Preencha: Cliente, Serviço e Técnico", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String vl = fVl.getText().trim();
            if (vl.isEmpty()) vl = "0,00";

            OS os = new OS(cl, fTel.getText().trim(), fEm.getText().trim(), sv, tc,
                (String)cEq.getSelectedItem(), fDs.getText().trim(), (String)cSt.getSelectedItem(),
                (String)cPr.getSelectedItem(), vl, fGr.getText().trim() + "d");

            osList.add(os);
            DatabaseManager.saveOS(os);
            refreshOSTable();
            refreshFinance();
            addLog("Nova OS: " + os.id + " - " + cl);
            dlg.dispose();
            showSuccess("OS Criada!", os.id + " - " + cl);
        });
        btns.add(save);

        JButton cancel = new JButton("❌ Cancelar");
        styleBtn(cancel, DANGER);
        cancel.addActionListener(e -> dlg.dispose());
        btns.add(cancel);

        dlg.add(new JScrollPane(form), BorderLayout.CENTER);
        dlg.add(btns, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void editSelectedOS() {
        int row = osTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Selecione uma OS.", "Atenção", JOptionPane.WARNING_MESSAGE); return; }
        OS os = osList.get(row);
        if (os == null) return;

        JDialog dlg = new JDialog(this, "✏️ Editar " + os.id, true);
        dlg.setSize(560, 660);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(Color.WHITE);
        dlg.setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(WARNING);
        header.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        JLabel h = new JLabel("✏️ Editar " + os.id);
        h.setFont(new Font("Segoe UI", Font.BOLD, 17));
        h.setForeground(Color.WHITE);
        header.add(h, BorderLayout.CENTER);
        dlg.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 0, 3, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JTextField fCl = inputField(os.cliente), fTel = inputField(os.telefone), fEm = inputField(os.email);
        JTextField fSv = inputField(os.servico), fTc = inputField(os.tecnico), fVl = inputField(os.valor);
        JComboBox<String> cEq = combo(EQUIPS); cEq.setSelectedItem(os.equipamento);
        JComboBox<String> cSt = combo(STATUS); cSt.setSelectedItem(os.status);
        JComboBox<String> cPr = combo(PRIORIDADES); cPr.setSelectedItem(os.prioridade);
        JTextField fGr = inputField(os.garantia.replace("d",""));
        JTextArea fDs = new JTextArea(3, 15);
        fDs.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        fDs.setLineWrap(true); fDs.setBackground(Color.WHITE);
        fDs.setBorder(BorderFactory.createLineBorder(BORDER));
        fDs.setText(os.descricao);

        int ri = 0;
        addField(form, gbc, ri++, "Cliente:", fCl);
        addField(form, gbc, ri++, "Telefone:", fTel);
        addField(form, gbc, ri++, "Email:", fEm);
        addField(form, gbc, ri++, "Serviço:", fSv);
        addField(form, gbc, ri++, "Técnico:", fTc);
        addField(form, gbc, ri++, "Equipamento:", cEq);
        addField(form, gbc, ri++, "Descrição:", new JScrollPane(fDs));
        addField(form, gbc, ri++, "Status:", cSt);
        addField(form, gbc, ri++, "Prioridade:", cPr);
        addField(form, gbc, ri++, "Valor (R$):", fVl);
        addField(form, gbc, ri++, "Garantia (dias):", fGr);

        JPanel btns = new JPanel(new GridLayout(1, 2, 10, 0));
        btns.setBackground(Color.WHITE);
        btns.setBorder(BorderFactory.createEmptyBorder(8, 22, 18, 22));

        JButton save = new JButton("💾 Salvar");
        styleBtn(save, WARNING);
        save.addActionListener(e -> {
            os.cliente = fCl.getText().trim(); os.telefone = fTel.getText().trim();
            os.email = fEm.getText().trim(); os.servico = fSv.getText().trim();
            os.tecnico = fTc.getText().trim(); os.equipamento = (String)cEq.getSelectedItem();
            os.descricao = fDs.getText().trim(); os.status = (String)cSt.getSelectedItem();
            os.prioridade = (String)cPr.getSelectedItem();
            os.valor = fVl.getText().trim(); os.garantia = fGr.getText().trim() + "d";
            DatabaseManager.saveOS(os);
            refreshOSTable(); refreshFinance();
            addLog("OS editada: " + os.id);
            dlg.dispose();
            showSuccess("OS Atualizada!", os.id);
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

    private void finalizeSelectedOS() {
        int row = osTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Selecione uma OS.", "Atenção", JOptionPane.WARNING_MESSAGE); return; }
        OS os = osList.get(row);
        if (JOptionPane.showConfirmDialog(this, "Finalizar OS " + os.id + "?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            os.status = "FINALIZADA";
            os.dataConclusao = now();
            DatabaseManager.saveOS(os);
            refreshOSTable(); refreshFinance();
            addLog("OS finalizada: " + os.id);
            showSuccess("OS Finalizada!", os.id);
        }
    }

    private void deleteSelectedOS() {
        int row = osTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Selecione uma OS.", "Atenção", JOptionPane.WARNING_MESSAGE); return; }
        OS os = osList.get(row);
        if (JOptionPane.showConfirmDialog(this, "Excluir OS " + os.id + "?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            DatabaseManager.deleteOS(os.id);
            osList.remove(os);
            refreshOSTable(); refreshFinance();
            detailsArea.setText("");
            addLog("OS excluída: " + os.id);
        }
    }

    private void printSelectedOS() {
        int row = osTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Selecione uma OS.", "Atenção", JOptionPane.WARNING_MESSAGE); return; }
        OS os = osList.get(row);
        String out = String.format(
            "┌──────────────────────────────────────┐\n" +
            "│   ORDEM DE SERVIÇO - TechSuite Pro   │\n" +
            "├──────────────────────────────────────┤\n" +
            "│  OS: %-33s│\n│  Abertura: %-27s│\n│  Conclusão: %-26s│\n" +
            "│  Cliente: %-28s│\n│  Serviço: %-28s│\n│  Técnico: %-28s│\n" +
            "│  Status: %-29s│\n│  Prioridade: %-24s│\n│  Valor: R$ %-26s│\n" +
            "└──────────────────────────────────────┘",
            os.id, os.dataAbertura, os.dataConclusao, os.cliente, os.servico, os.tecnico, os.status, os.prioridade, os.valor);
        JOptionPane.showMessageDialog(this, out, "OS " + os.id, JOptionPane.INFORMATION_MESSAGE);
    }

    // ===== HELPERS OS =====
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
            boolean mS = s.isEmpty() || (os.id + os.cliente + os.servico + os.tecnico + os.status).toLowerCase().contains(s);
            boolean mSt = st.equals("TODOS") || os.status.equals(st);
            boolean mPr = pr.equals("TODAS") || os.prioridade.equals(pr);
            if (mS && mSt && mPr) osModel.addRow(os.toRow());
        }
    }

    private void showDetails(int row) {
        int idx = osTable.convertRowIndexToModel(row);
        if (idx < 0 || idx >= osList.size()) return;
        OS os = osList.get(idx);
        detailsArea.setText(String.format(
            "══════════════════════════════════════════\n" +
            "  ORDEM: %s | Status: %s\n" +
            "══════════════════════════════════════════\n\n" +
            "  Abertura   : %s\n  Conclusão  : %s\n  Cliente    : %s\n" +
            "  Telefone   : %s\n  Email      : %s\n  Serviço    : %s\n" +
            "  Técnico    : %s\n  Equipamento: %s\n  Prioridade : %s\n" +
            "  Valor      : R$ %s\n  Garantia   : %s\n  Descrição  : %s\n",
            os.id, os.status, os.dataAbertura, os.dataConclusao, os.cliente,
            os.telefone, os.email, os.servico, os.tecnico, os.equipamento,
            os.prioridade, os.valor, os.garantia, os.descricao));
    }

    private int countOS(String s) { int c=0; for(OS o:osList) if(o.status.equals(s)) c++; return c; }
    private double pv(String v) { try { return Double.parseDouble(v.replace(",",".")); } catch(Exception e){ return 0; } }
    private String totalFaturamento(){ double t=0; for(OS o:osList)t+=pv(o.valor); return "R$ "+String.format("%.2f",t); }
    private String totalPendente(){ double t=0; for(OS o:osList)if(!o.status.equals("FINALIZADA"))t+=pv(o.valor); return "R$ "+String.format("%.2f",t); }
    private String totalRecebido(){ double t=0; for(OS o:osList)if(o.status.equals("FINALIZADA"))t+=pv(o.valor); return "R$ "+String.format("%.2f",t); }
    private String ticketMedio(){ if(osList.isEmpty())return "R$ 0,00"; double t=0; for(OS o:osList)t+=pv(o.valor); return "R$ "+String.format("%.2f",t/osList.size()); }

    // ===== CRUD CLIENTES =====
    private void addClient() {
        JTextField n = inputField(), t = inputField(), e = inputField();
        JPanel p = formPanel(new Object[]{lbl("Nome:"), n, lbl("Telefone:"), t, lbl("Email:"), e}, 3);
        if (JOptionPane.showConfirmDialog(this, p, "Novo Cliente", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String id = String.format("CL-%03d", clientModel.getRowCount()+1);
            clientModel.addRow(new Object[]{id, n.getText(), t.getText(), e.getText(), dateOnly()});
            DatabaseManager.saveCliente(id, n.getText(), t.getText(), e.getText(), dateOnly());
            addLog("Cliente: " + n.getText());
        }
    }

    private void deleteClient() {
        int r = clientTable.getSelectedRow();
        if (r == -1) { JOptionPane.showMessageDialog(this, "Selecione um cliente.", "Atenção", JOptionPane.WARNING_MESSAGE); return; }
        if (JOptionPane.showConfirmDialog(this, "Excluir?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            String id = clientModel.getValueAt(r, 0).toString();
            DatabaseManager.deleteCliente(id);
            addLog("Cliente excluído: " + clientModel.getValueAt(r, 1));
            clientModel.removeRow(r);
        }
    }

    // ===== CRUD EQUIPE =====
    private void addTech() {
        JTextField n = inputField(), e = inputField();
        JPanel p = formPanel(new Object[]{lbl("Nome:"), n, lbl("Especialidade:"), e}, 2);
        if (JOptionPane.showConfirmDialog(this, p, "Novo Técnico", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String id = String.format("TEC-%03d", techModel.getRowCount()+1);
            techModel.addRow(new Object[]{id, n.getText(), e.getText(), 0, 0});
            DatabaseManager.saveTecnico(id, n.getText(), e.getText(), 0, 0);
            addLog("Técnico: " + n.getText());
        }
    }

    private void deleteTech() {
        int r = techTable.getSelectedRow();
        if (r == -1) { JOptionPane.showMessageDialog(this, "Selecione um técnico.", "Atenção", JOptionPane.WARNING_MESSAGE); return; }
        if (JOptionPane.showConfirmDialog(this, "Excluir?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            String id = techModel.getValueAt(r, 0).toString();
            DatabaseManager.deleteTecnico(id);
            addLog("Técnico excluído");
            techModel.removeRow(r);
        }
    }

    // ===== FINANCEIRO =====
    private void refreshFinance() {
        finModel.setRowCount(0);
        for (OS os : osList) {
            String pg = os.status.equals("FINALIZADA") ? "PAGO" : "PENDENTE";
            finModel.addRow(new Object[]{os.id, os.cliente, os.servico, "R$ " + os.valor, pg, os.dataConclusao});
        }
    }

    // ===== RELATÓRIOS =====
    private void showFullReport() {
        String s = String.format("╔══════════════════════════════════════╗\n║   RELATÓRIO COMPLETO - TechSuite     ║\n╠══════════════════════════════════════╣\n" +
            "║ Total OS : %26d ║\n║ Abertas  : %26d ║\n║ Andamento: %26d ║\n║ Finaliz. : %26d ║\n" +
            "║ Receita  : %-19s ║\n║ Clientes : %26d ║\n║ Técnicos : %26d ║\n" +
            "╚══════════════════════════════════════╝\n",
            osList.size(), countOS("ABERTA"), countOS("EM ANDAMENTO"), countOS("FINALIZADA"),
            totalFaturamento(), clientModel.getRowCount(), techModel.getRowCount());
        showInfo("Relatório Completo", s);
    }

    private void showClientReport() {
        StringBuilder sb = new StringBuilder("══════════════════════════════════════\n  RELATÓRIO DE CLIENTES\n══════════════════════════════════════\n\n");
        for (int i = 0; i < clientModel.getRowCount(); i++)
            sb.append(String.format("  %s - %s | %s\n", clientModel.getValueAt(i,0), clientModel.getValueAt(i,1), clientModel.getValueAt(i,2)));
        showInfo("Clientes", sb.toString());
    }

    private void showTechReport() {
        StringBuilder sb = new StringBuilder("══════════════════════════════════════\n  RELATÓRIO DA EQUIPE\n══════════════════════════════════════\n\n");
        for (int i = 0; i < techModel.getRowCount(); i++)
            sb.append(String.format("  %s - %s | %s\n", techModel.getValueAt(i,0), techModel.getValueAt(i,1), techModel.getValueAt(i,2)));
        showInfo("Equipe", sb.toString());
    }

    private void showFinanceReport() {
        showInfo("Financeiro", String.format("Receita Total : %s\nA Receber     : %s\nRecebido      : %s\nTicket Médio  : %s\nTotal OS      : %d",
            totalFaturamento(), totalPendente(), totalRecebido(), ticketMedio(), osList.size()));
    }

    private void doBackup() {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String dest = fc.getSelectedFile().getPath() + "/backup_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".txt";
                FileWriter fw = new FileWriter(dest);
                fw.write("BACKUP - " + now() + "\n\n");
                for (OS os : osList) fw.write(String.join("|", os.id, os.cliente, os.servico, os.status, os.valor) + "\n");
                fw.close();
                addLog("Backup: " + dest);
                showSuccess("Backup Realizado!", dest);
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE); }
        }
    }

    private void showSystemInfo() {
        showInfo("Info do Sistema", String.format("Sistema: %s %s\nJava: %s\nUsuário: %s\nMemória: %d MB\nProcessadores: %d",
            System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("java.version"),
            System.getProperty("user.name"), Runtime.getRuntime().freeMemory()/1024/1024, Runtime.getRuntime().availableProcessors()));
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
        JComboBox<String> uRole = combo(new String[]{"USER", "ADMIN"});
        JPanel p = formPanel(new Object[]{lbl("Usuário:"), uId, lbl("Senha:"), uPass, lbl("Nome:"), uName, lbl("Nível:"), uRole}, 4);
        if (JOptionPane.showConfirmDialog(this, p, "Novo Usuário", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try (FileWriter fw = new FileWriter(System.getProperty("user.home") + "/.techsuite/users.dat", true)) {
                fw.write(String.format("%s|%s|%s|ATIVO\n", uId.getText().trim(), uName.getText().trim(), uRole.getSelectedItem()));
                addLog("Usuário criado: " + uId.getText());
                loadUsers();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE); }
        }
    }

    // ===== DIALOGOS =====
    private void showSuccess(String title, String msg) {
        JDialog d = new JDialog(this, title, true);
        d.setSize(380, 230);
        d.setLocationRelativeTo(this);
        d.setUndecorated(true);
        d.getContentPane().setBackground(Color.WHITE);
        d.setLayout(new BorderLayout());
        JPanel hd = new JPanel(new BorderLayout());
        hd.setBackground(SUCCESS);
        hd.setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));
        JLabel ic = new JLabel("✅", SwingConstants.CENTER);
        ic.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 42));
        ic.setForeground(Color.WHITE);
        hd.add(ic, BorderLayout.CENTER);
        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 16));
        t.setForeground(Color.WHITE);
        t.setHorizontalAlignment(SwingConstants.CENTER);
        hd.add(t, BorderLayout.SOUTH);
        d.add(hd, BorderLayout.NORTH);
        JTextArea m = new JTextArea(msg);
        m.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        m.setForeground(TEXT_SECONDARY);
        m.setBackground(Color.WHITE);
        m.setEditable(false);
        m.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        m.setLineWrap(true);
        d.add(m, BorderLayout.CENTER);
        JPanel bn = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 12));
        bn.setBackground(Color.WHITE);
        JButton ok = new JButton("OK");
        styleBtn(ok, SUCCESS);
        ok.setPreferredSize(new Dimension(110, 36));
        ok.addActionListener(e -> d.dispose());
        bn.add(ok);
        d.add(bn, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    private void showInfo(String title, String msg) {
        JDialog d = new JDialog(this, title, true);
        d.setSize(480, 360);
        d.setLocationRelativeTo(this);
        d.getContentPane().setBackground(Color.WHITE);
        d.setLayout(new BorderLayout());
        JPanel hd = new JPanel(new BorderLayout());
        hd.setBackground(INFO);
        hd.setBorder(BorderFactory.createEmptyBorder(15, 22, 15, 22));
        JLabel t = new JLabel("  " + title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 15));
        t.setForeground(Color.WHITE);
        hd.add(t, BorderLayout.CENTER);
        d.add(hd, BorderLayout.NORTH);
        JTextArea m = new JTextArea(msg);
        m.setFont(new Font("Consolas", Font.PLAIN, 12));
        m.setForeground(TEXT_PRIMARY);
        m.setBackground(Color.WHITE);
        m.setEditable(false);
        m.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        m.setLineWrap(true);
        d.add(new JScrollPane(m), BorderLayout.CENTER);
        JPanel bn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        bn.setBackground(Color.WHITE);
        JButton cl = new JButton("Fechar");
        styleBtn(cl, INFO);
        cl.addActionListener(e -> d.dispose());
        bn.add(cl);
        d.add(bn, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    // ===== UI HELPERS =====
    private void styleTable(JTable t) {
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

    private JTextField inputField(String v) {
        JTextField f = inputField();
        if (v != null) f.setText(v);
        return f;
    }

    private JComboBox<String> combo(String[] items) {
        JComboBox<String> c = new JComboBox<>(items);
        c.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        c.setBackground(Color.WHITE);
        return c;
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
        b.setPreferredSize(new Dimension(135, 34));
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

    private void addField(JPanel form, GridBagConstraints gbc, int row, String label, Component field) {
        gbc.gridy = row * 2;
        JLabel lb = new JLabel(label);
        lb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lb.setForeground(TEXT_SECONDARY);
        form.add(lb, gbc);
        gbc.gridy = row * 2 + 1;
        gbc.insets = new Insets(2, 0, 7, 0);
        form.add(field, gbc);
        gbc.insets = new Insets(3, 0, 3, 0);
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
                showSuccess("Exportado!", fc.getSelectedFile().getName());
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE); }
        }
    }

    private void addLog(String msg) {
        if (activityLog != null) {
            activityLog.append("[" + timeOnly() + "] " + msg + "\n");
            activityLog.setCaretPosition(activityLog.getDocument().getLength());
        }
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
        JPanel c = new JPanel(new BorderLayout());
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

    private String now() { return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()); }
    private String dateOnly() { return new SimpleDateFormat("dd/MM/yyyy").format(new Date()); }
    private String timeOnly() { return new SimpleDateFormat("HH:mm:ss").format(new Date()); }

    // ===== CARREGAR DADOS DO BANCO =====
    private void loadDataFromDatabase() {
        // Carregar OS do banco
        osList = new ArrayList<>(DatabaseManager.loadAllOS());
        refreshOSTable();
        refreshFinance();
        
        // Carregar Clientes do banco
        clientModel.setRowCount(0);
        for (Object[] cliente : DatabaseManager.loadAllClientes()) {
            clientModel.addRow(cliente);
        }
        
        // Carregar Técnicos do banco
        techModel.setRowCount(0);
        for (Object[] tecnico : DatabaseManager.loadAllTecnicos()) {
            techModel.addRow(tecnico);
        }
        
        addLog("Sistema iniciado por " + currentUser);
        addLog(osList.size() + " ordens | " + clientModel.getRowCount() + " clientes | " + techModel.getRowCount() + " técnicos");
    }

    // ===== MAIN / LOGIN =====
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        new File(System.getProperty("user.home") + "/.techsuite").mkdirs();

        // Login direto sem EDT wrapper
        new LoginDialog().showLogin();
    }

    static class LoginDialog {
        private JDialog dialog;
        private JTextField userField;
        private JPasswordField passField;

        void showLogin() {
            dialog = new JDialog((Frame)null, "TechSuite Pro - Login", true);
            dialog.setSize(460, 500);
            dialog.setLocationRelativeTo(null);
            dialog.setUndecorated(true);
            dialog.setLayout(new BorderLayout());
            dialog.getContentPane().setBackground(Color.WHITE);
            dialog.setShape(new java.awt.geom.RoundRectangle2D.Double(0, 0, 460, 500, 18, 18));

            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(ACCENT);
            header.setBorder(BorderFactory.createEmptyBorder(30, 30, 18, 30));
            JLabel title = new JLabel("TechSuite Pro");
            title.setFont(new Font("Segoe UI", Font.BOLD, 28));
            title.setForeground(Color.WHITE);
            header.add(title, BorderLayout.CENTER);
            JLabel sub = new JLabel("Sistema de Gestão de Serviços");
            sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            sub.setForeground(new Color(200, 200, 255));
            header.add(sub, BorderLayout.SOUTH);
            dialog.add(header, BorderLayout.NORTH);

            JPanel form = new JPanel(new GridBagLayout());
            form.setBackground(Color.WHITE);
            form.setBorder(BorderFactory.createEmptyBorder(25, 35, 20, 35));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(4, 0, 4, 0);

            JLabel uI = new JLabel("  Usuário:");
            uI.setFont(new Font("Segoe UI", Font.BOLD, 12));
            uI.setForeground(TEXT_SECONDARY);
            gbc.gridy = 0; form.add(uI, gbc);

            userField = new JTextField();
            userField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            userField.setBackground(Color.WHITE);
            userField.setForeground(TEXT_PRIMARY);
            userField.setCaretColor(TEXT_PRIMARY);
            userField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(11, 14, 11, 14)));
            userField.setText("admin");
            gbc.gridy = 1; form.add(userField, gbc);

            JLabel pI = new JLabel("  Senha:");
            pI.setFont(new Font("Segoe UI", Font.BOLD, 12));
            pI.setForeground(TEXT_SECONDARY);
            gbc.gridy = 2; form.add(pI, gbc);

            passField = new JPasswordField();
            passField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            passField.setBackground(Color.WHITE);
            passField.setForeground(TEXT_PRIMARY);
            passField.setCaretColor(TEXT_PRIMARY);
            passField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(11, 14, 11, 14)));
            passField.setText("admin123");
            gbc.gridy = 3; form.add(passField, gbc);

            JButton loginBtn = new JButton("ENTRAR");
            loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
            loginBtn.setForeground(Color.WHITE);
            loginBtn.setBackground(ACCENT);
            loginBtn.setFocusPainted(false);
            loginBtn.setBorderPainted(false);
            loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            loginBtn.setPreferredSize(new Dimension(0, 46));
            loginBtn.addActionListener(e -> doLogin());
            gbc.gridy = 4; gbc.insets = new Insets(14, 0, 0, 0);
            form.add(loginBtn, gbc);

            passField.addActionListener(e -> doLogin());

            JPanel footer = new JPanel(new BorderLayout());
            footer.setBackground(Color.WHITE);
            footer.setBorder(BorderFactory.createEmptyBorder(10, 35, 22, 35));
            JLabel help = new JLabel("Acesso padrão: admin / admin123");
            help.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            help.setForeground(TEXT_MUTED);
            help.setHorizontalAlignment(SwingConstants.CENTER);
            footer.add(help, BorderLayout.CENTER);

            JButton closeBtn = new JButton("✕");
            closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
            closeBtn.setForeground(TEXT_MUTED);
            closeBtn.setBackground(new Color(245, 245, 250));
            closeBtn.setFocusPainted(false);
            closeBtn.setBorderPainted(false);
            closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            closeBtn.setPreferredSize(new Dimension(30, 30));
            closeBtn.addActionListener(e -> System.exit(0));
            footer.add(closeBtn, BorderLayout.EAST);

            dialog.add(form, BorderLayout.CENTER);
            dialog.add(footer, BorderLayout.SOUTH);

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

            JOptionPane.showMessageDialog(dialog, "Usuário ou senha incorretos!", "Erro de Login", JOptionPane.ERROR_MESSAGE);
            passField.setText("");
        }
    }
}
