package com.rkonline.android.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class GameData {

    private GameData() {}

    private static final List<String> SINGLE_PANA = Arrays.asList(
            "128","137","146","236","245","290","380","470","489","560","678","579",
            "129","138","147","156","237","246","345","390","480","570","679","589",
            "120","139","148","157","238","247","256","346","490","580","670","689",
            "130","149","158","167","239","248","257","347","356","590","680","789",
            "140","159","168","230","249","258","267","348","357","456","690","780",
            "123","150","169","178","240","259","268","349","358","457","367","790",
            "124","160","179","250","269","278","340","359","368","458","467","890",
            "125","134","170","189","260","279","350","369","378","459","567","468",
            "126","135","180","234","270","289","360","379","450","469","478","568",
            "127","136","145","190","235","280","370","479","460","569","389","578"
    );

    private static final List<String> DOUBLE_PANA = Arrays.asList(
            "100","119","155","227","335","344","399","588","669",
            "200","110","228","255","336","499","660","688","778",
            "300","166","229","337","355","445","599","779","788",
            "400","112","220","266","338","446","455","699","770",
            "500","113","122","177","339","366","447","799","889",
            "600","114","277","330","448","466","556","880","899",
            "700","115","133","188","223","377","449","557","566",
            "800","116","224","233","288","440","477","558","990",
            "900","117","144","199","225","388","559","577","667",
            "550","668","244","299","226","488","677","118","334"
    );

    private static final List<String> TRIPLE_PANA = Arrays.asList(
            "000","111","222","333","444","555","666","777","888","999"
    );

    private static final List<String> RED_JODI = Arrays.asList(
            "11","22","33","44","55","66","77","88","99","00",
            "05","50","16","61","27","72","38","83","49","94"
    );

    public static ArrayList<String> getSinglePana() {
        return new ArrayList<>(SINGLE_PANA);
    }

    public static ArrayList<String> getDoublePana() {
        return new ArrayList<>(DOUBLE_PANA);
    }
    public static ArrayList<String> getJodi() {
        ArrayList<String> numbers = new ArrayList<>();
        for (int i = 0; i < 100; i++) numbers.add(String.format("%02d", i));
        return numbers;
    }
    public static ArrayList<String> getSingleAnk() {
        ArrayList<String> numbers = new ArrayList<>();
        for (int i = 0; i <= 9; i++) numbers.add(String.valueOf(i));
        return numbers;
    }

    public static ArrayList<String> getTriplePana() {
        return new ArrayList<>(TRIPLE_PANA);
    }

    public static ArrayList<String> getRedJodi() {
        return new ArrayList<>(RED_JODI);
    }

    public static ArrayList<String> getAllPatti() {
        ArrayList<String> list = new ArrayList<>();
        list.addAll(SINGLE_PANA);
        list.addAll(DOUBLE_PANA);
        list.addAll(TRIPLE_PANA);
        return list;
    }

    // -------- LINE GROUP GENERATION --------

    public static ArrayList<String> getPattiWithLines() {

        ArrayList<String> result = new ArrayList<>();

        ArrayList<String> all = getAllPatti();

        // 0..9 buckets
        ArrayList<String>[] buckets = new ArrayList[10];
        for (int i = 0; i < 10; i++) buckets[i] = new ArrayList<>();

        for (String p : all) {
            int line = calculateLine(p);
            buckets[line].add(p);
        }

        for (int i = 1; i <= 9; i++) {
            result.add("Line of " + i);
            result.addAll(buckets[i]);
        }

        result.add("Line of 0");
        result.addAll(buckets[0]);

        return result;
    }

    private static int calculateLine(String patti) {
        int sum = 0;
        for (char c : patti.toCharArray()) {
            sum += c - '0';
        }
        return sum % 10;
    }
    public static ArrayList<String> generateSPNumbers(String input) {
        ArrayList<String> numbers = new ArrayList<>();
        char[] chars = input.toCharArray();
        int len = chars.length;

        for (int i = 0; i < len - 2; i++) {
            for (int j = i + 1; j < len - 1; j++) {
                for (int k = j + 1; k < len; k++) {
                    numbers.add("" + chars[i] + chars[j] + chars[k]);
                }
            }
        }
        return numbers;
    }

    public static ArrayList<String> generateDPNumbers(String input) {
        ArrayList<String> numbers = new ArrayList<>();
        char[] chars = input.toCharArray();
        int len = chars.length;
        for (int i = 0; i < len; i++) {
            for (int j = i + 1; j < len; j++) {
                char a = input.charAt(i);
                char b = input.charAt(j);
                numbers.add("" + a + a + b);
                numbers.add("" + a + b + b);
            }
        }
        return numbers;
    }

    public static ArrayList<String> generateCrossingNumbers(String input) {
        ArrayList<String> numbers = new ArrayList<>();
        ArrayList<Character> unique = new ArrayList<>();
        for (char c : input.toCharArray()) {
            if (!unique.contains(c)) {
                unique.add(c);
            }
        }
        for (char a : unique) {
            for (char b : unique) {
                numbers.add("" + a + b);
            }
        }
        return numbers;
    }


}
