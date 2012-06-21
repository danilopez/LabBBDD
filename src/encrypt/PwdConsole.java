/*
 * This file contains CryptoUtils. The author of this class is Réal Gagnon (you
 * can get this class from http://www.rgagnon.com), he allows the use, with no
 * restrictiction, of individual How-To in a development (compiled/source) but
 * he appreciated a mention.
 */
package encrypt;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class PwdConsole {

    public static void readPwd(String msg) throws Exception {
        ConsoleEraser consoleEraser = new ConsoleEraser();
        System.out.print(msg);
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        consoleEraser.start();
        String pass = stdin.readLine();
        consoleEraser.halt();
        System.out.print("\b");
    }
}

class ConsoleEraser extends Thread {

    private boolean running = true;

    @Override
    public void run() {
        while (running) {
            System.out.print("\b ");
        }
    }

    public synchronized void halt() {
        running = false;
    }
}
//
// The original version:
//
//public class PwdConsole {
//
//    public static void main(String[] args) throws Exception {
//        ConsoleEraser consoleEraser = new ConsoleEraser();
//        System.out.print("Password?  ");
//        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
//        consoleEraser.start();
//        String pass = stdin.readLine();
//        consoleEraser.halt();
//        System.out.print("\b");
//        System.out.println("Password: '" + pass + "'");
//    }
//}
//
//class ConsoleEraser extends Thread {
//
//    private boolean running = true;
//
//    @Override
//    public void run() {
//        while (running) {
//            System.out.print("\b ");
//        }
//    }
//
//    public synchronized void halt() {
//        running = false;
//    }
//}

