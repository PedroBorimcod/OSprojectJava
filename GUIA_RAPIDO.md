# 🚀 Guia Rápido - TechSuite Pro com Banco de Dados

## ✅ O que foi corrigido:

**PROBLEMA ANTERIOR:**
- Toda vez que você fechava o sistema, voltavam os dados de exemplo
- Mesmo depois de excluir tudo, ao reabrir o sistema os dados voltavam
- Não havia persistência real das informações

**SOLUÇÃO IMPLEMENTADA:**
- ✅ Banco de dados SQLite para salvar tudo permanentemente
- ✅ Dados salvos automaticamente ao criar/editar/excluir
- ✅ Ao abrir o sistema, carrega os dados reais do banco
- ✅ Dados excluídos não voltam mais

## 📍 Onde ficam os dados:

O banco de dados fica em:
```
C:\Users\WINDOWS\.techsuite\techsuite.db
```

## 🎯 Como usar:

### Opção 1 - Desenvolvimento (Recomendado):
```
Duplo clique em: compilar-executar.bat
```

### Opção 2 - Se já estiver compilado:
```
Duplo clique em: criar-pacote.bat
Depois: java -jar TechSuitePro.jar
```

## 🔐 Login:
- **Usuário:** admin
- **Senha:** admin123

## 💡 Fluxo de Trabalho:

1. **Abra o sistema** → Faz login
2. **Crie OS, Clientes, Técnicos** → Tudo salvo automaticamente
3. **Feche o sistema** → Dados persistem no banco
4. **Reabra o sistema** → Seus dados estão lá!
5. **Exclua o que não precisa** → Removido permanentemente

## ✨ Tudo é Automático:

Quando você:
- ✅ **Cria** uma OS → Salva no banco
- ✅ **Edita** uma OS → Atualiza no banco
- ✅ **Finaliza** uma OS → Salva no banco
- ✅ **Exclui** uma OS → Remove do banco
- ✅ **Adiciona** cliente → Salva no banco
- ✅ **Exclui** cliente → Remove do banco
- ✅ **Adiciona** técnico → Salva no banco
- ✅ **Exclui** técnico → Remove do banco

## 📦 Backup (Importante!):

Para fazer backup dos seus dados:
1. Feche o sistema
2. Copie o arquivo: `C:\Users\WINDOWS\.techsuite\techsuite.db`
3. Guarde em local seguro

## ⚠️ Importante:

- **NÃO delete** o arquivo `techsuite.db` ou perderá tudo
- O sistema inicia **vazio** (sem dados fictícios)
- Dados de exemplo foram **removidos permanentemente**

## 🎉 Resultado Final:

Agora você pode:
- ✅ Trabalhar normalmente
- ✅ Fechar o sistema quando quiser
- ✅ Reabrir e encontrar tudo como deixou
- ✅ Excluir dados indesejados (não voltam mais!)
- ✅ Ter todos os dados da sua conta salvos

---

**Pronto! Seu sistema agora tem persistência real de dados! 🎊**
