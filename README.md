# TechSuite Pro - Sistema com Banco de Dados

## 🎉 Mudanças Implementadas

### Banco de Dados SQLite
O sistema agora usa um **banco de dados SQLite** para salvar todas as informações permanentemente:
- ✅ Ordens de Serviço (OS)
- ✅ Clientes
- ✅ Técnicos
- ✅ Logs de atividades

### O que mudou:

#### ❌ Antes (Problema):
- Toda vez que você fechava o sistema, **perdia todos os dados**
- Dados eram recriados do zero ao iniciar (dados de exemplo fixos)
- Não havia persistência de informações

#### ✅ Depois (Solução):
- **Todos os dados são salvos automaticamente** no banco de dados
- Ao fechar e abrir o sistema, **suas informações permanecem**
- OS, clientes e técnicos são **carregados do banco** ao iniciar
- Exclusões são **refletidas permanentemente**

## 📦 Arquivos do Projeto

### Arquivos Java:
- `HackPanel.java` - Sistema principal (interface e lógica)
- `DatabaseManager.java` - Gerenciamento do banco de dados SQLite

### Scripts:
- `compilar-executar.bat` - Compila e executa o sistema (recomendado para desenvolvimento)
- `criar-pacote.bat` - Cria um JAR único com tudo incluído
- `criar-jar.bat` - Versão antiga (apenas classes, sem SQLite)

### Driver:
- `sqlite-jdbc-3.44.1.0.jar` - Driver SQLite JDBC

### Banco de Dados:
- Local: `%USERPROFILE%\.techsuite\techsuite.db`
- Criado automaticamente na primeira execução

## 🚀 Como Usar

### Para Desenvolvimento (Recomendado):
```batch
compilar-executar.bat
```
Este script:
1. Baixa o driver SQLite se necessário
2. Compila todos os arquivos
3. Executa o sistema

### Para Distribuição:
```batch
criar-pacote.bat
```
Cria um arquivo `TechSuitePro.jar` que pode ser distribuído

### Executar JAR:
```batch
java -jar TechSuitePro.jar
```

## 🔧 Login Padrão

**Usuário:** `admin`  
**Senha:** `admin123`

## 📊 Estrutura do Banco de Dados

### Tabela: ordens
- id (PRIMARY KEY)
- data_abertura
- data_conclusao
- cliente
- telefone
- email
- servico
- tecnico
- equipamento
- descricao
- status
- prioridade
- valor
- garantia

### Tabela: clientes
- id (PRIMARY KEY)
- nome
- telefone
- email
- data_cadastro

### Tabela: tecnicos
- id (PRIMARY KEY)
- nome
- especialidade
- os_ativas
- os_finalizadas

### Tabela: logs
- id (AUTO INCREMENT)
- timestamp
- mensagem

## ✨ Funcionalidades

### Todas as operações agora são persistentes:

1. **Criar OS** → Salva no banco automaticamente
2. **Editar OS** → Atualiza no banco
3. **Finalizar OS** → Salva status e data de conclusão
4. **Excluir OS** → Remove permanentemente do banco
5. **Adicionar Cliente** → Salva no banco
6. **Excluir Cliente** → Remove do banco
7. **Adicionar Técnico** → Salva no banco
8. **Excluir Técnico** → Remove do banco

## 🔒 Backup

O banco de dados está localizado em:
```
%USERPROFILE%\.techsuite\techsuite.db
```

Para fazer backup, basta copiar este arquivo.

## 📝 Notas Importantes

⚠️ **IMPORTANTE:**
- Não delete o arquivo `techsuite.db` ou perderá todos os dados
- Faça backup regularmente copiando o arquivo do banco
- O sistema inicializa **vazio** (sem dados de exemplo)
- Dados de exemplo foram removidos para não poluir seu banco

## 🐛 Solução de Problemas

### Erro: "Driver SQLite não encontrado"
- Certifique-se de que `sqlite-jdbc-*.jar` está na mesma pasta
- Execute `compilar-executar.bat` que baixa automaticamente

### Erro de compilação
- Instale o JDK Java 17 ou superior
- Verifique se o Java está no PATH: `java -version`

### Sistema não inicia
- Verifique se há permissão na pasta `.techsuite`
- Delete o banco e reinicie para recriá-lo

## 📞 Suporte

Em caso de problemas, verifique:
1. Se o Java está instalado: `java -version`
2. Se os arquivos `.jar` estão na pasta
3. Se há permissão de escrita na pasta do usuário

---

**Versão:** 2.0 - Com Banco de Dados SQLite  
**Data:** Abril/2026
