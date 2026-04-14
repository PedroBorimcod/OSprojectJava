import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DB_PATH = System.getProperty("user.home") + "/.techsuite/techsuite.db";
    private static Connection connection;

    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
                connection.setAutoCommit(true);
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver SQLite não encontrado. Certifique-se de que sqlite-jdbc.jar está no classpath.", e);
            }
        }
        return connection;
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Tabela de OS
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS ordens (
                    id TEXT PRIMARY KEY,
                    data_abertura TEXT,
                    data_conclusao TEXT,
                    cliente TEXT,
                    telefone TEXT,
                    email TEXT,
                    servico TEXT,
                    tecnico TEXT,
                    equipamento TEXT,
                    descricao TEXT,
                    status TEXT,
                    prioridade TEXT,
                    valor TEXT,
                    garantia TEXT
                )
            """);

            // Tabela de Clientes
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS clientes (
                    id TEXT PRIMARY KEY,
                    nome TEXT,
                    telefone TEXT,
                    email TEXT,
                    data_cadastro TEXT
                )
            """);

            // Tabela de Técnicos
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS tecnicos (
                    id TEXT PRIMARY KEY,
                    nome TEXT,
                    especialidade TEXT,
                    os_ativas INTEGER,
                    os_finalizadas INTEGER
                )
            """);

            // Tabela de Logs
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    timestamp TEXT,
                    mensagem TEXT
                )
            """);

        } catch (SQLException e) {
            System.err.println("Erro ao inicializar banco de dados: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ===== CRUD PARA ORDENS DE SERVIÇO =====
    public static void saveOS(HackPanel.OS os) {
        String sql = os.existsInDatabase() 
            ? "UPDATE ordens SET data_abertura=?, data_conclusao=?, cliente=?, telefone=?, email=?, servico=?, tecnico=?, equipamento=?, descricao=?, status=?, prioridade=?, valor=?, garantia=? WHERE id=?"
            : "INSERT INTO ordens (id, data_abertura, data_conclusao, cliente, telefone, email, servico, tecnico, equipamento, descricao, status, prioridade, valor, garantia) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, os.dataAbertura);
            pstmt.setString(2, os.dataConclusao);
            pstmt.setString(3, os.cliente);
            pstmt.setString(4, os.telefone);
            pstmt.setString(5, os.email);
            pstmt.setString(6, os.servico);
            pstmt.setString(7, os.tecnico);
            pstmt.setString(8, os.equipamento);
            pstmt.setString(9, os.descricao);
            pstmt.setString(10, os.status);
            pstmt.setString(11, os.prioridade);
            pstmt.setString(12, os.valor);
            pstmt.setString(13, os.garantia);
            
            if (os.existsInDatabase()) {
                pstmt.setString(14, os.id);
            } else {
                pstmt.setString(14, os.id);
            }
            
            pstmt.executeUpdate();
            os.setSaved(true);
        } catch (SQLException e) {
            System.err.println("Erro ao salvar OS: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void deleteOS(String osId) {
        String sql = "DELETE FROM ordens WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, osId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao excluir OS: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static List<HackPanel.OS> loadAllOS() {
        List<HackPanel.OS> osList = new ArrayList<>();
        String sql = "SELECT * FROM ordens ORDER BY data_abertura DESC";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                HackPanel.OS os = new HackPanel.OS(
                    rs.getString("cliente"),
                    rs.getString("telefone"),
                    rs.getString("email"),
                    rs.getString("servico"),
                    rs.getString("tecnico"),
                    rs.getString("equipamento"),
                    rs.getString("descricao"),
                    rs.getString("status"),
                    rs.getString("prioridade"),
                    rs.getString("valor"),
                    rs.getString("garantia")
                );
                os.id = rs.getString("id");
                os.dataAbertura = rs.getString("data_abertura");
                os.dataConclusao = rs.getString("data_conclusao");
                os.setSaved(true);
                osList.add(os);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao carregar OS: " + e.getMessage());
            e.printStackTrace();
        }
        return osList;
    }

    // ===== CRUD PARA CLIENTES =====
    public static void saveCliente(String id, String nome, String telefone, String email, String dataCadastro) {
        String sql = "INSERT OR REPLACE INTO clientes (id, nome, telefone, email, data_cadastro) VALUES (?,?,?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, nome);
            pstmt.setString(3, telefone);
            pstmt.setString(4, email);
            pstmt.setString(5, dataCadastro);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao salvar cliente: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void deleteCliente(String id) {
        String sql = "DELETE FROM clientes WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao excluir cliente: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static List<Object[]> loadAllClientes() {
        List<Object[]> clientes = new ArrayList<>();
        String sql = "SELECT * FROM clientes ORDER BY data_cadastro DESC";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                clientes.add(new Object[]{
                    rs.getString("id"),
                    rs.getString("nome"),
                    rs.getString("telefone"),
                    rs.getString("email"),
                    rs.getString("data_cadastro")
                });
            }
        } catch (SQLException e) {
            System.err.println("Erro ao carregar clientes: " + e.getMessage());
            e.printStackTrace();
        }
        return clientes;
    }

    // ===== CRUD PARA TÉCNICOS =====
    public static void saveTecnico(String id, String nome, String especialidade, int osAtivas, int osFinalizadas) {
        String sql = "INSERT OR REPLACE INTO tecnicos (id, nome, especialidade, os_ativas, os_finalizadas) VALUES (?,?,?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, nome);
            pstmt.setString(3, especialidade);
            pstmt.setInt(4, osAtivas);
            pstmt.setInt(5, osFinalizadas);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao salvar técnico: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void deleteTecnico(String id) {
        String sql = "DELETE FROM tecnicos WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao excluir técnico: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static List<Object[]> loadAllTecnicos() {
        List<Object[]> tecnicos = new ArrayList<>();
        String sql = "SELECT * FROM tecnicos ORDER BY nome";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                tecnicos.add(new Object[]{
                    rs.getString("id"),
                    rs.getString("nome"),
                    rs.getString("especialidade"),
                    rs.getInt("os_ativas"),
                    rs.getInt("os_finalizadas")
                });
            }
        } catch (SQLException e) {
            System.err.println("Erro ao carregar técnicos: " + e.getMessage());
            e.printStackTrace();
        }
        return tecnicos;
    }

    // ===== LOG DE ATIVIDADES =====
    public static void saveLog(String mensagem) {
        String sql = "INSERT INTO logs (timestamp, mensagem) VALUES (?,?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date()));
            pstmt.setString(2, mensagem);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao salvar log: " + e.getMessage());
        }
    }

    public static void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
