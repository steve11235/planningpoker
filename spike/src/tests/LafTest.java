package tests;

import javax.swing.UIManager;

public class LafTest {

	public static void main(String[] a) {
		UIManager.LookAndFeelInfo[] looks = UIManager.getInstalledLookAndFeels();
		for (UIManager.LookAndFeelInfo look : looks) {
			System.out.println(look.getClassName());
		}
	}
}
