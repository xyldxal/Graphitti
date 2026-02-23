package graphtheory;

import javax.swing.UIManager;
import java.awt.Color;
import java.util.Date;

public class Main {

    public static void main(String[] args) throws Exception {

        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        Date date = new Date();
        new Canvas("Graphitti " + date.toString(), 680, 560, Color.WHITE);

    }
}
