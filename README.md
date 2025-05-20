# Diário Cultural

**Desenvolvido por: Davi Baleeiro e Luis Felipe Carvalho**
**Projeto Acadêmico - Disciplina Algoritmos e Estruturas de Dados II**
**Departamento de Tecnologia - Engenharia de Computação**
**Universidade Estadual de Feira de Santana (UEFS)**
**Feira de Santana – BA – Brazil**
**Status do Projeto:** Etapa 02 - Funcionalidades com Persistência JSON

## 📝 Sumário

* [Sobre o Projeto](#sobre-o-projeto)
* [Funcionalidades](#funcionalidades)
* [Tecnologias Utilizadas](#tecnologias-utilizadas)
* [Estrutura do Projeto](#estrutura-do-projeto)
* [Pré-requisitos](#pré-requisitos)
* [Como Executar](#como-executar)
* [Manual de Uso Básico](#manual-de-uso-básico)
* [Estrutura de Dados JSON](#estrutura-de-dados-json)
* [Próximos Passos e Melhorias Futuras](#próximos-passos-e-melhorias-futuras)
* [Autores](#autores)

## 🌟 Sobre o Projeto

O "Diário Cultural" é uma aplicação Java desktop (atualmente com interface via linha de comando) projetada para auxiliar usuários a registrar, gerenciar e avaliar mídias consumidas, como livros, filmes e séries. O sistema permite catalogar obras, adicionar múltiplas avaliações (nota e comentário), registrar datas de consumo e manter um histórico organizado das experiências culturais.

Este projeto visa solucionar o problema da dispersão das memórias e impressões sobre o conteúdo cultural consumido, oferecendo um repositório pessoal e persistente.

## ✨ Funcionalidades

O sistema atualmente implementa as seguintes funcionalidades principais:

*   **Gerenciamento de Livros:**
    *   Cadastro (título, autor, editora, ISBN, ano, gênero, cópia física).
    *   Marcação como lido/não lido.
    *   Registro de data de leitura.
    *   Adição de múltiplas avaliações (nota 0-5, comentário).
    *   Atualização e Remoção.
    *   Busca (título, autor, gênero, ano, ISBN).
    *   Listagem com filtros (gênero, ano) e ordenação (título, autor, ano, avaliação).
*   **Gerenciamento de Filmes:**
    *   Cadastro (título, gênero, ano, duração, diretor, elenco, sinopse, onde assistir).
    *   Marcação como visto/não visto.
    *   Registro de data de visualização.
    *   Adição de múltiplas avaliações (nota 0-5, comentário).
    *   Atualização e Remoção.
    *   Busca (título, diretor, ator, gênero, ano).
    *   Listagem com filtros (gênero, ano) e ordenação (avaliação).
*   **Gerenciamento de Séries:**
    *   Cadastro de séries (título, gênero, ano de lançamento/término, elenco, onde assistir).
    *   Marcação como assistida/não assistida (série completa).
    *   Gerenciamento de Temporadas:
        *   Cadastro de temporadas individuais (número, ano, episódios, elenco adicional).
        *   Adição de múltiplas avaliações por temporada (nota 0-5, comentário).
    *   Atualização e Remoção de séries.
    *   Adição de novas temporadas a séries existentes.
    *   Busca (título, gênero, ano, ator).
    *   Listagem com filtros (gênero, ano) e ordenação (avaliação média das temporadas).
*   **Persistência de Dados:**
    *   Todos os dados de livros, filmes, séries e suas avaliações são salvos em arquivos JSON (`data/books.json`, `data/movies.json`, `data/series.json`).
    *   Os dados são carregados automaticamente ao iniciar a aplicação.
*   **Interface:**
    *   Interação via Linha de Comando (CLI) com menus e prompts.

## 💻 Tecnologias Utilizadas

*   **Linguagem:** Java (JDK 21 ou superior recomendado - ajuste conforme sua versão)
*   **Gerenciamento de Dependências:** Apache Maven
*   **Manipulação de JSON:** Jackson Databind, Jackson Core, Jackson Annotations, Jackson Datatype JSR310
*   **Testes Unitários:** JUnit 5 (para classes do Model)
*   **IDE Recomendada:** IntelliJ IDEA

## 📁 Estrutura do Projeto

O projeto segue o padrão arquitetural Model-View-Controller (MVC):

*   `src/main/java/org/diariocultural/`:
    *   **Model:** Classes que representam os dados e a lógica de negócio (`Media.java`, `Book.java`, `Movie.java`, `Series.java`, `Season.java`, `Review.java`, `ReviewInfo.java`).
    *   **View:** Classes responsáveis pela interface com o usuário via console (`MediaView.java`, `BookView.java`, `MovieView.java`, `SeriesView.java`).
    *   **Controller:** Classes que fazem a mediação entre Model e View (`BookController.java`, `MovieController.java`, `SeriesController.java`).
    *   `Main.java`: Ponto de entrada da aplicação, gerencia o menu principal.
*   `src/test/java/org/diariocultural/`: Contém os testes unitários para as classes do Model.
*   `pom.xml`: Arquivo de configuração do Maven, define as dependências e o build do projeto.
*   `data/`: Diretório (criado na primeira execução que salva dados) onde os arquivos JSON (`books.json`, `movies.json`, `series.json`) são armazenados. **Adicione esta pasta ao seu `.gitignore` se estiver usando Git.**

## ⚙️ Pré-requisitos

*   JDK (Java Development Kit) versão 21 ou superior instalado (ajuste para a versão utilizada).
*   Apache Maven instalado e configurado (necessário para compilar e gerenciar dependências, ou o IntelliJ IDEA pode lidar com isso se o projeto for importado como Maven).
*   (Opcional) IntelliJ IDEA ou outra IDE Java de sua preferência.

## 🚀 Como Executar

### 1. Compilando com Maven (Linha de Comando)

Abra um terminal ou prompt de comando na pasta raiz do projeto (onde o `pom.xml` está localizado) e execute:

```bash
mvn clean package
```

Isso compilará o projeto e criará um arquivo JAR em `target/Diario_cultural_Etapa_2-1.0-SNAPSHOT.jar` (o nome pode variar um pouco).

### 2. Executando via Linha de Comando (após compilar)

Navegue até a pasta `target/` e execute o JAR:

```bash
java -jar Diario_cultural_Etapa_2-1.0-SNAPSHOT.jar
```
(Se você configurou o `maven-assembly-plugin` para um "fat JAR", este comando deve funcionar. Caso contrário, você pode precisar adicionar o classpath manualmente ou rodar via IDE).

### 3. Executando via IntelliJ IDEA

*   Importe o projeto no IntelliJ IDEA como um projeto Maven (se ainda não o fez, clique com o botão direito no `pom.xml` > "Add as Maven Project").
*   Aguarde o IntelliJ sincronizar as dependências.
*   Localize a classe `Main.java` em `src/main/java/org/diariocultural/`.
*   Clique com o botão direito sobre `Main.java` e selecione "Run 'Main.main()'".

## 📖 Manual de Uso Básico

Ao executar a aplicação, você será apresentado a um menu principal:

```
=== MENU PRINCIPAL ===
1 - Gerenciar Filmes
2 - Gerenciar Livros
3 - Gerenciar Séries
0 - Sair
Escolha:
```

1.  Digite o número da opção desejada e pressione Enter.
2.  **Gerenciar Mídias (Filmes, Livros, Séries):**
    *   **Adicionar:** Siga os prompts para inserir os detalhes do item.
    *   **Listar/Filtrar/Ordenar:** Exibe a lista de itens, com opções para aplicar filtros (por gênero, ano) e ordenação (por título, autor, avaliação, etc.).
    *   **Buscar:** Permite buscar itens por critérios específicos (título, autor, diretor, gênero, etc.).
    *   **Atualizar:** Solicita o título do item e permite modificar seus dados e adicionar novas avaliações.
    *   **Remover:** Solicita o título do item e o remove da lista.
    *   (Séries) **Adicionar Temporada:** Permite adicionar novas temporadas a uma série existente.
3.  **Entrada de Dados:**
    *   Siga as instruções de cada prompt.
    *   Para listas (gêneros, elenco), separe os itens por vírgula.
    *   Para respostas Sim/Não, use 's' ou 'n'.
    *   Para datas, use o formato `dd/MM/yyyy`.
4.  **Sair:** Escolha '0' no menu principal e confirme. Seus dados são salvos automaticamente após cada operação de modificação.

## 💾 Estrutura de Dados JSON

Os dados são persistidos em arquivos JSON no diretório `data/` da aplicação. Cada tipo de mídia principal (livros, filmes, séries) tem seu próprio arquivo.

**Exemplo de um Livro em `books.json` (simplificado):**
```json
[
  {
    "title": "O Senhor dos Anéis",
    "genre": ["Fantasia", "Aventura"],
    "releaseYear": 1954,
    "bookId": 1,
    "originalTitle": "The Lord of the Rings",
    "author": "J.R.R. Tolkien",
    "publisher": "Allen & Unwin",
    "isbn": "978-0618640157",
    "hasCopy": true,
    "readStatus": true,
    "readDate": 1672531200000, // Exemplo de timestamp ou data ISO
    "reviewInfo": {
      "reviews": [
        {
          "rating": 5,
          "comment": "Obra prima!",
          "reviewDate": 1672617600000
        }
      ]
    }
  }
]
```
*(Nota: O formato real da data no JSON pode variar dependendo da configuração do Jackson. Timestamps numéricos ou strings no formato ISO 8601 são comuns.)*

## 🔮 Próximos Passos e Melhorias Futuras

*   **Refinamento da Interface:** Embora funcional, a CLI pode ser melhorada com mais clareza e validações.
*   **Edição de Avaliações/Temporadas:** Permitir editar uma avaliação ou temporada específica, em vez de apenas adicionar novas.
*   **Interface Gráfica (JavaFX):** O principal próximo passo é o desenvolvimento de uma interface gráfica utilizando JavaFX para uma experiência de usuário muito superior.
*   **Relatórios e Estatísticas:** Geração de estatísticas sobre mídias consumidas, gêneros preferidos, etc.
*   **Testes de Integração:** Adicionar testes que verifiquem a interação entre as camadas (Controller-Model, Controller-Persistência).
*   **Opções de Backup:** Implementar funcionalidade para exportar/importar os dados JSON (backup/restauração).

## 🧑‍💻 Autores

*   Davi Baleeiro
*   Luis Felipe Pereira de Carvalho
