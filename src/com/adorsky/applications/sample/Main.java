package com.adorsky.applications.sample;

import com.adorsky.applications.sample.exception.InvalidNumberException;
import com.adorsky.applications.sample.exception.MalformedMenuSelectionException;

import java.io.Console;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Sample Command Line Bank Account
 * @author: adorsky
 */

public class Main {

    private static Account account;
    private static final String MENU_PROMPT =
            "Please enter in a command (Deposit, Withdraw, Balance, Exit) :";

    private static final Console console = System.console();

    private Main() {}

    public static void main(String[] args)
            throws InvalidNumberException, IOException, MalformedMenuSelectionException {
        try {
            account = new Account(Paths.get("./log.html"));

            displayMenu();
        } catch(final InvalidNumberException e) {
            handleInvalidNumber(e);
        } catch(final IOException e) {
            handleIOException();
        } catch(final MalformedMenuSelectionException e) {
            handleMalformedMenuSelectionException();
        }
    }

    /**
     * Main interface for the command line application, directing user to various bank account functions.
     * Exceptions below rise up the stack from various parts of the application and are handled from main().
     * @throws IOException see Account
     * @throws InvalidNumberException see Account
     * @throws MalformedMenuSelectionException An invalid command was entered into the console
     */
    private static void displayMenu() throws IOException, InvalidNumberException, MalformedMenuSelectionException {
        boolean hasExited = false;

        while(!hasExited) {
            System.out.println(MENU_PROMPT);

            String input = console.readLine().trim().toLowerCase();

            switch(input) {
                case "deposit":
                    System.out.println("Please enter an amount to deposit:");
                    input = console.readLine().trim();
                    account.handleDeposit(input);
                    break;
                case "withdraw":
                    System.out.println("Please enter an amount to withdraw:");
                    input = console.readLine().trim();
                    account.handleWithdraw(input);
                    break;
                case "balance":
                    System.out.println("The current balance is: $" + account.determineBalance());
                    break;
                case "exit":
                    hasExited = true;
                    break;
                default:
                    throw new MalformedMenuSelectionException
                            ("The following incorrect input was entered: " + input);
            }
        }
    }

    private static void handleInvalidNumber(final Exception exception) {
        try {
            System.out.println("Invalid number entered.  Please enter another value:");

            final String input = console.readLine().trim();

            if(exception.getMessage().equals("Deposit number entered is invalid.")) {
                account.handleDeposit(input);
            } else {
                account.handleWithdraw(input);
            }

            displayMenu();
        } catch(final InvalidNumberException e) {
            handleInvalidNumber(e);
        } catch(final IOException e) {
            handleIOException();
        } catch(final MalformedMenuSelectionException e) {
            handleMalformedMenuSelectionException();
        }
    }

    private static void handleIOException() {
        try {
            System.out.println("Unable to read or write to the log.html file, please specify a file path:");
            String path = console.readLine();

            account = new Account(Paths.get(path));

            displayMenu();
        } catch(final InvalidNumberException e) {
            handleInvalidNumber(e);
        } catch(final IOException e) {
            handleIOException();
        } catch(final MalformedMenuSelectionException e) {
            handleMalformedMenuSelectionException();
        }

    }

    private static void handleMalformedMenuSelectionException() {
        try {
            System.out.println("Input was invalid, please try again.");
            displayMenu();
        } catch(final IOException | InvalidNumberException | MalformedMenuSelectionException e) {
            handleMalformedMenuSelectionException();
        }
    }
}
