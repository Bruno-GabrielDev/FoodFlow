# Relatório de Avaliação Técnica de Testes de UI - FoodFlow (Etapa 4)

Este documento detalha a classificação técnica de cada teste de UI do projeto FoodFlow, utilizando as técnicas de teste de caixa-preta: **Particionamento por Classe de Equivalência (CE)** e **Análise de Valor Limite (AVL)**.

---

## 1. Tela de Segurança e Controle de Acesso (`SecurityUiTest.java`)

| ID | Descrição do Teste | Técnica | Motivo da Classificação |
| :--- | :--- | :--- | :--- |
| **UI 01** | Redirecionar /dashboard sem autenticação | **CE** | Representa a classe de usuários "Não Autenticados" tentando acessar rotas privadas. |
| **UI 02** | Redirecionar /orders sem autenticação | **CE** | Valida a mesma classe de equivalência (acesso negado) para um endpoint diferente. |
| **UI 03** | Invalidação por remoção de token | **CE** | Testa a transição de estado da sessão de "Válida" para "Inexistente" no armazenamento local. |
| **UI 04** | Invalidação por token corrompido | **CE** | Representa a classe de "Tokens Malformados/Inválidos", garantindo que não sejam aceitos. |
| **UI 05** | Bloqueio de acesso pós-logout | **CE** | Valida a classe de usuários que encerraram a sessão voluntariamente. |
| **UI 06** | SQL Injection no Login | **CE** | Testa a classe de "Inputs Maliciosos" que visam quebrar a lógica da consulta no banco de dados. |
| **UI 07** | HTML Injection (Doom) nas observações | **CE** | Representa a classe de "Injeção de Script/Tag" para validar a sanitização do DOM pelo React. |
| **UI 08** | Perda de token durante ação ativa | **CE** | Valida o comportamento sistêmico na mudança repentina de estado de autorização durante um processo. |
| **UI 09** | Impede IDOR (Ataque Cross-Waiter) | **CE** | Testa a classe de "Acesso a Recursos de Outros Usuários" (Autorização em nível de objeto). |

---

## 2. Tela de Fluxos de Aceitação e Robustez (`AcceptanceUiTest.java`)

| ID | Descrição do Teste | Técnica | Motivo da Classificação |
| :--- | :--- | :--- | :--- |
| **UI 10** | Abrir nova comanda via Dashboard | **CE** | Representa o fluxo nominal (caminho feliz) da principal funcionalidade de abertura de mesa. |
| **UI 11** | Lançar item com adicionais | **CE** | Valida a classe de "Seleção de Múltiplas Opções" (adicionais e observações) no lançamento. |
| **UI 12** | Evitar duplicidade (Double Submission) | **CE** | Testa a robustez contra a classe de "Ações Simultâneas" disparadas pela interface. |
| **UI 13** | Limpar estado do modal ao reabrir | **CE** | Valida a integridade do estado da UI, garantindo que não haja vazamento de dados entre ações. |
| **UI 59** | Grande volume de itens (50+) | **AVL** | Testa o limite de renderização de listas extensas em um único componente. |
| **UI 60** | Valores monetários elevados | **AVL** | Testa o limite financeiro e a formatação de strings monetárias com muitos dígitos. |

---

## 3. Tela de Login (`LoginUiTest.java`)

| ID | Descrição do Teste | Técnica | Motivo da Classificação |
| :--- | :--- | :--- | :--- |
| **UI 14** | Login com credenciais válidas | **CE** | Representa a classe de "Usuários Válidos e Autorizados". |
| **UI 15** | Erro com senha incorreta | **CE** | Representa a classe de erro "Credenciais Inválidas (Senha)". |
| **UI 16** | Erro com usuário inexistente | **CE** | Representa a classe de erro "Credenciais Inválidas (Usuário)". |
| **UI 17** | Submissão com campos vazios | **AVL** | Valida o limite inferior de preenchimento obrigatório (zero caracteres). |
| **UI 18** | Navegação para Cadastro | **CE** | Testa a transição de estado da aplicação (Navegação de telas). |

---

## 4. Tela de Casos de Borda (`EdgeCaseUiTest.java`)

| ID | Descrição do Teste | Técnica | Motivo da Classificação |
| :--- | :--- | :--- | :--- |
| **UI 19** | Número de pessoas excessivo (999) | **AVL** | Testa o limite superior arbitrário para o campo numérico de quantidade de pessoas. |
| **UI 20** | Rejeitar fechamento com 0 pessoas | **AVL** | Valida o limite exato onde o valor passa de inválido (0) para válido (1). |
| **UI 21** | Rejeitar número negativo de pessoas | **CE** | Representa a classe de "Valores Numéricos Inválidos/Fora de Domínio". |
| **UI 22** | Observação com 500+ caracteres | **AVL** | Testa o limite de armazenamento e renderização de strings longas na UI. |
| **UI 23** | Caracteres especiais e acentuação | **CE** | Representa a classe de "Dados Não-Alfanuméricos" e símbolos de escape. |
| **UI 24** | Observação vazia (campo opcional) | **AVL** | Valida o limite inferior de um campo que permite nulidade/vazio. |
| **UI 25** | Username com 200+ caracteres | **AVL** | Testa o limite máximo de caracteres permitido pelo esquema de dados/backend. |
| **UI 26** | Login com espaços em branco | **CE** | Representa a classe de inputs que possuem comprimento mas não conteúdo útil. |
| **UI 57** | Observação com exatos 255 caracteres | **AVL** | Valida o limite superior comum para campos de banco de dados (`VARCHAR(255)`). |
| **UI 58** | Observação com 256 caracteres | **AVL** | Testa o comportamento exato no estouro do limite comum de banco de dados. |

---

## 5. Tela de Comandas (`OrdersUiTest.java`)

| ID | Descrição do Teste | Técnica | Motivo da Classificação |
| :--- | :--- | :--- | :--- |
| **UI 27** | Exibição da tela de comandas | **CE** | Valida o estado de carregamento e renderização inicial da lista. |
| **UI 28** | Listagem de ao menos uma comanda | **CE** | Representa a classe de "Exibição de Dados Existentes". |
| **UI 29** | Modal de Adicionar Item | **CE** | Testa a interação de interface para abertura de sub-formulários. |
| **UI 30** | Fluxo de lançamento de item | **CE** | Fluxo nominal de criação de registros vinculados. |
| **UI 31 a 33** | Fechamento de comanda | **CE** | Representam os estados finais de um ciclo de negócio (Sucesso e Cancelamento). |
| **UI 34** | Visualização de detalhes | **CE** | Valida a profundidade dos dados exibidos para conferência. |

---

## 6. Tela do Dashboard (`DashboardUiTest.java`)

| ID | Descrição do Teste | Técnica | Motivo da Classificação |
| :--- | :--- | :--- | :--- |
| **UI 38 e 39** | Filtros de mesas (Livre/Ocupada) | **CE** | Cada filtro representa uma classe distinta de dados filtrados a serem exibidos na grade. |
| **UI 40 a 42** | Ações em mesas disponíveis | **CE** | Representa a transição de estado de uma mesa de "Disponível" para "Ocupada" via UI. |

---

## 7. Responsividade (`ResponsiveUiTest.java`)

| ID | Descrição do Teste | Técnica | Motivo da Classificação |
| :--- | :--- | :--- | :--- |
| **UI 45 a 49** | Viewports Mobile/Tablet/Desktop | **AVL** | Os breakpoints de design (375px, 768px, 1920px) são os valores limites onde a estrutura do layout deve se comportar de forma diferente. |
| **UI 50** | Consistência de dados entre viewports | **CE** | Garante que a classe de dados exibidos seja independente da classe de visualização (resolução). |
