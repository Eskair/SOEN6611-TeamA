package ibank.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

/** The main menu after a correct PIN: one large button per use case. */
public class MenuScreen extends BaseScreen {

  private static final long serialVersionUID = 1L;
  private static final Dimension BUTTON_SIZE = new Dimension(300, 72);

  /** Builds the main menu for the logged-in cardholder. */
  public MenuScreen(AtmFrame frame) {
    super(frame, "menu.title", frame.session().getCardholder().getName());
    JPanel grid = new JPanel(new GridLayout(3, 2, 16, 16));
    grid.setOpaque(false);
    grid.add(menuButton("menu.balance", () -> frame.showScreen(new BalanceScreen(frame))));
    grid.add(menuButton("menu.withdraw", () -> frame.showScreen(AmountScreen.withdraw(frame))));
    grid.add(menuButton("menu.deposit", () -> frame.showScreen(AmountScreen.deposit(frame))));
    grid.add(menuButton("menu.transfer", () -> frame.showScreen(new TransferScreen(frame))));
    grid.add(menuButton("menu.changePin", () -> frame.showScreen(new ChangePinScreen(frame))));
    grid.add(menuButton("menu.end", () -> frame.endSession("session.ended")));
    addLine(grid);
  }

  private JButton menuButton(String key, Runnable onClick) {
    JButton button = new JButton(text().get(key));
    button.setFont(button.getFont().deriveFont(Font.BOLD, 22f));
    button.setPreferredSize(BUTTON_SIZE); // Large targets are easier to press.
    button.addActionListener(event -> onClick.run());
    return button;
  }
}
