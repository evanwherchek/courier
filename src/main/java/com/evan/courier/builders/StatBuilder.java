package com.evan.courier.builders;

public class StatBuilder {
    public StatBuilder() {

    }

    public String build() {
        return "    <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "        <tr>\n" +
                "            <td align=\"center\">\n" +
                "                <p style=\"margin: 10px 0; font-size: 16px; text-align: center;\">The current interest rate is</p>\n" +
                "                <p style=\"margin: 20px 0; font-size: 48px; font-weight: bold; text-align: center;\">3%</p>\n" +
                "                <p style=\"margin: 10px 0; font-size: 16px; text-align: center;\">The fed will meet again on 1/23.</p>\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "    </table>\n";
    }
}
