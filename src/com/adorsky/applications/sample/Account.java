package com.adorsky.applications.sample;

import com.adorsky.applications.sample.exception.InvalidNumberException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository of all basic account information and operations as specified in the
 * command line application.
 * @author: adorsky
 */

class Account {

    private Path accountPath;
    private Document parsedAccountContent;
    private List<Double> accountDonations;
    private List<Double> accountTransactions;

    /**
     *
     * @param accountPath The path of the html source file for this account's information
     * @throws IOException Failure to load account information; handled in Main
     */
    Account(Path accountPath) throws IOException {
        this.accountPath = accountPath;

        loadAccountDonations();

        loadAccountTransactions();
    }

    /**
     *
     * @param depositAmount The amount to be deposited into the account, must be positive and
     *                      no more than 2 decimal places
     * @throws InvalidNumberException A number not fitting the description above was entered
     * @throws IOException Appending the html file failed
     */
    void handleDeposit(final String depositAmount) throws InvalidNumberException, IOException {
        if(isValidNumber(depositAmount)) {
            double depositValue = Double.valueOf(depositAmount);

            accountTransactions.add(depositValue);
            appendTransactions();
        } else {
            throw new InvalidNumberException("Deposit number entered is invalid.");
        }
    }

    /**
     *
     * @param withdrawAmount The amount to be withdrawn from the account, must be positive and
     *                      no more than 2 decimal places
     * @throws InvalidNumberException A number not fitting the description above was entered
     * @throws IOException Appending the html file failed
     */
    void handleWithdraw(final String withdrawAmount) throws InvalidNumberException, IOException {
        if(isValidNumber(withdrawAmount) && Double.valueOf(withdrawAmount) <= Double.valueOf(determineBalance())) {
            double withdrawValue = Double.valueOf(withdrawAmount) * -1;

            accountTransactions.add(withdrawValue);
            appendTransactions();
        } else {
            throw new InvalidNumberException("Withdraw number entered is invalid.");
        }
    }

    /**
     * Calculates the total balance of the account by adding recorded deposits and subtracting
     * recorded withdrawals
     * @return The calculated balance of the account
     */
    String determineBalance() {
        Double balance = 0.0;

        for( Double transaction : accountTransactions ) {
            balance += transaction;
        }

        DecimalFormat balanceFormat = new DecimalFormat("0.00");


        return balanceFormat.format(balance);
    }

    /**
     * Determines if a valid number was entered for a deposit or withdrawal.  Checks if
     * (a) the number can be a Double, (b) if the number is whole, (c) if not whole, if
     * the number has 2 or less decimal places
     * @param amount The deposit or withdrawal amount to be evaluated
     * @return Whether the number is valid
     */
    private boolean isValidNumber(final String amount) {
        try {
            double enteredValue = Double.valueOf(amount);

            final String[] decimalPlaces = amount.split("[.]");

            if(enteredValue < 0) {
                return false;
            } else if(decimalPlaces.length == 1 || decimalPlaces[1].length() <= 2) {
                return true;
            }

            return false;

        } catch(final NumberFormatException e) {
            return false;
        }
    }

    /**
     * Stores the values from the donation table of the source html file
     * @throws IOException Reading the html file failed
     */
    private void loadAccountDonations() throws IOException {
        accountDonations = new ArrayList<>();

        try(final BufferedReader reader = Files.newBufferedReader(accountPath)) {
            StringBuilder htmlAccountContent = new StringBuilder();
            String currLine;

            while((currLine = reader.readLine()) != null) {
                htmlAccountContent.append(currLine);
            }

            parsedAccountContent = Jsoup.parse(htmlAccountContent.toString());
            Elements transactions =  parsedAccountContent.select("table[id=\"donations\"] tbody tr td");

            Double transactionValue;

            for( final Element transaction : transactions ) {
                transactionValue = Double.parseDouble(transaction.text());
                accountDonations.add(transactionValue);
            }

        } catch(final IOException e) {
            throw new IOException("Unable to read log.html file to pull account information.");
        }
    }

    /**
     * Stores the values from the transactions table of the source html file
     * @throws IOException Reading the html file failed
     */
    private void loadAccountTransactions() throws IOException {
        accountTransactions = new ArrayList<>();

        try(final BufferedReader reader = Files.newBufferedReader(accountPath)) {
            StringBuilder htmlAccountContent = new StringBuilder();
            String currLine;

            while((currLine = reader.readLine()) != null) {
                htmlAccountContent.append(currLine);
            }

            parsedAccountContent = Jsoup.parse(htmlAccountContent.toString());
            Elements transactions =  parsedAccountContent.select("table[id=\"transactions\"] tbody tr td");

            Double transactionValue;

            for( final Element transaction : transactions ) {
                transactionValue = Double.parseDouble(transaction.text());
                accountTransactions.add(transactionValue);
            }

        } catch(final IOException e) {
            throw new IOException("Unable to read log.html file to pull account information.");
        }
    }


    /**
     * Appends a valid transaction to the existing html account source file.  The existing
     * source file is deleted and replaced with the new appended file.
     * @throws IOException Writing to the new file failed
     */
    private void appendTransactions() throws IOException {
        final Path tmpLogPath = Paths.get("./tmplog.html");

        final String htmlPreDonationsHeader = "<html>\n" +
                "<body>\n" +
                "    <div class=\"content\">\n" +
                "        <table id=\"donations\" class=\"table-bordered\">\n" +
                "            <thead>\n" +
                "                <th>Amount</th>\n" +
                "            </thead>\n" +
                "            <tbody>\n";

        final String htmlPreTransactionsHeader =
                "            </tbody>\n" +
                "        </table>\n" +
                "        <table id=\"transactions\" class=\"table-bordered\">\n" +
                "            <thead>\n" +
                "                <th>Amount</th>\n" +
                "            </thead>\n" +
                "            <tbody>\n";

        final String htmlFooter =
                "            </tbody>\n" +
                "        </table>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";

        try(final BufferedWriter writer = Files.newBufferedWriter(tmpLogPath)) {

            final StringBuilder html = new StringBuilder();

            html.append(htmlPreDonationsHeader);

            for (final Double currDonation : accountDonations ) {
                html.append("                <tr><td>" + currDonation + "</td></tr>\n");
            }

            html.append(htmlPreTransactionsHeader);

            for( final Double currTransaction : accountTransactions ) {
                html.append("                <tr><td>" + currTransaction + "</td></tr>\n");
            }

            html.append(htmlFooter);

            writer.write(html.toString());
        }

        Files.delete(accountPath);
        Files.move(tmpLogPath, accountPath);
    }
}
