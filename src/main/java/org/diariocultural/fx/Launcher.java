package org.diariocultural.fx;

/**
 * Classe intermediária para iniciar a aplicação JavaFX a partir de um JAR executável.
 * Esta classe também define uma propriedade de sistema para garantir a compatibilidade
 * gráfica em diferentes computadores.
 */
public class Launcher {
    public static void main(String[] args) {
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.text", "t2k");
        System.setProperty("java.awt.headless", "false");
        MainApp.main(args);
    }
}