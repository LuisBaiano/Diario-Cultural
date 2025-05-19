package org.diariocultural;

import java.util.Scanner;

/**
 * Diário Cultural
 * Desenvolvido por: Davi Baleeiro e Luis Felipe Carvalho
 * Projeto Acadêmico - UEFS - Abril/2025
 */


/**
 * Classe principal que executa a interface de linha de comando (CLI)
 * para gerenciar um Diário Cultural de filmes, livros e séries.
 */
public class Main {

    /** Instâncias para entrada de dados e controle das entidades. */
    private static final Scanner scanner = new Scanner(System.in);
    private static final BookController bookController = new BookController();
    private static final MovieController movieController = new MovieController();
    private static final SeriesController seriesController = new SeriesController();

    /**
     * Ponto de entrada da aplicação. Exibe o menu principal e mostra os submenus
     * o loop de interação com o usuário até que ele escolha sair.
     *
     * @param args Argumentos da linha de comando (não utilizados).
     */
    public static void main(String[] args) {
        boolean exit = false;
        while (!exit) {
            displayMainMenu();
            int choice = getIntInput();
            switch (choice) {
                case 1 -> manageMovies();
                case 2 -> manageBooks();
                case 3 -> manageSeries();
                case 0 -> exit = confirmExit();
                default -> displayInvalidOptionError();
            }
        }
        System.out.println("Saindo do Diário Cultural. Até logo!");
        scanner.close();
    }

    // --- Métodos de Exibição de Menus ---

    /**
     * Exibe as opções do menu principal.
     */
    private static void displayMainMenu() {
        System.out.println("""
        === MENU PRINCIPAL ===
        1 - Gerenciar Filmes
        2 - Gerenciar Livros
        3 - Gerenciar Séries
        0 - Sair
        Escolha:""");
    }

    /**
     * Exibe o submenu de Gerenciamento de Filmes e delega as ações
     * ao MovieController com base na escolha do usuário.
     */
    private static void manageMovies() {
        boolean back = false;
        while (!back) {
            System.out.println("""
            === GERENCIAR FILMES ===
            1 - Adicionar Filme
            2 - Listar/Filtrar/Ordenar Filmes
            3 - Buscar Filme
            4 - Atualizar Filme
            5 - Remover Filme
            0 - Voltar
            Escolha:""");
            switch (getIntInput()) {
                case 1 -> movieController.addMovie();
                case 2 -> movieController.listAllMovies();
                case 3 -> movieController.searchMovie();
                case 4 -> updateMovie();
                case 5 -> removeMovie();
                case 0 -> back = true;
                default -> displayInvalidOptionError();
            }
        }
    }

    /**
     * Exibe o submenu de Gerenciamento de Livros e delega as ações
     * ao BookController com base na escolha do usuário.
     */
    private static void manageBooks() {
        boolean back = false;
        while (!back) {
            System.out.println("""
            === GERENCIAR LIVROS ===
            1 - Adicionar Livro
            2 - Listar/Filtrar/Ordenar Livros
            3 - Buscar Livro
            4 - Atualizar Livro
            5 - Remover Livro
            0 - Voltar
            Escolha:""");
            switch (getIntInput()) {
                case 1 -> bookController.addBook();
                case 2 -> bookController.listAllBooks();
                case 3 -> {System.out.print("Digite o critério de busca (Titulo, Autor, ISBN, Gênero e ano) para Livros: ");
                    String criteria = scanner.nextLine();
                    bookController.searchBooks(criteria);}
                case 4 -> updateBook();
                case 5 -> removeBook();
                case 0 -> back = true;
                default -> displayInvalidOptionError();
            }
        }
    }

    /**
     * Exibe o submenu de Gerenciamento de Séries e delega as ações
     * ao SeriesController com base na escolha do usuário.
     */
    private static void manageSeries() {
        boolean back = false;
        while (!back) {
            System.out.println("""
            === GERENCIAR SÉRIES ===
            1 - Adicionar Série
            2 - Listar/Filtrar/Ordenar Séries
            3 - Buscar Série
            4 - Atualizar Série
            5 - Adicionar Temporada a Série Existente
            6 - Remover Série
            0 - Voltar
            Escolha:""");
            switch (getIntInput()) {
                case 1 -> seriesController.addSeries();
                case 2 -> seriesController.listAllSeries();
                case 3 -> seriesController.searchSeries();
                case 4 -> updateSeries();
                case 5 -> seriesController.addSeasonFromMenu();
                case 6 -> removeSeries();
                case 0 -> back = true;
                default -> displayInvalidOptionError();
            }
        }
    }

    // --- Métodos Auxiliares de Interação com Usuário ---

    /**
     * Lê uma linha da entrada padrão e tenta convertê-la para um inteiro.
     * Retorna −1 se a entrada for vazia ou não for um número válido.
     *
     * @return O inteiro inserido pelo usuário ou −1 em caso de erro/entrada vazia.
     */
    private static int getIntInput() {
        try {
            String line = scanner.nextLine();
            if (line.isBlank()) return -1;
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Pergunta ao usuário se ele tem certeza que deseja sair.
     *
     * @return true se o usuário confirmar a saída (digitando 's' ou 'S'), false caso contrário.
     */
    private static boolean confirmExit() {
        System.out.println("Tem certeza que deseja sair? (s/n)");
        return scanner.nextLine().equalsIgnoreCase("s");
    }

    /**
     * Exibe uma mensagem padrão indicando que a opção escolhida é inválida.
     */
    private static void displayInvalidOptionError() {
        System.out.println("Opção inválida!");
    }

    // --- Métodos de Interface para Operações CRUD (Update/Remove) ---

    /**
     * Solicita o título do filme ao usuário e chama o método de atualização no controller.
     */
    private static void updateMovie() {
        System.out.print("Digite o título do filme para atualizar: ");
        movieController.updateMovie(scanner.nextLine());
    }

    /**
     * Solicita o título do filme ao usuário e chama o método de remoção no controller.
     */
    private static void removeMovie() {
        System.out.print("Digite o título do filme para remover: ");
        movieController.removeMovie(scanner.nextLine());
    }

    /**
     * Solicita o título do livro ao usuário e chama o método de atualização no controller.
     */
    private static void updateBook() {
        System.out.print("Digite o título do livro para atualizar: ");
        bookController.updateBook(scanner.nextLine());
    }

    /**
     * Solicita o título do livro ao usuário e chama o método de remoção no controller.
     */
    private static void removeBook() {
        System.out.print("Digite o título do livro para remover: ");
        bookController.removeBook(scanner.nextLine());
    }

    /**
     * Solicita o título da série ao usuário e chama o método de atualização no controller.
     */
    private static void updateSeries() {
        System.out.print("Digite o título da série para atualizar: ");
        seriesController.updateSeries(scanner.nextLine());
    }

    /**
     * Solicita o título da série ao usuário e chama o método de remoção no controller.
     */
    private static void removeSeries() {
        System.out.print("Digite o título da série para remover: ");
        seriesController.removeSeries(scanner.nextLine());
    }
}