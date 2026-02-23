package com.rkonline.android.utils;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class BetEngine {

    public interface BetCallback {
        void onSuccess(int newWallet);
        void onFailure(String error);
    }

    public static void placeBet(
            FirebaseFirestore db,
            String mobile,
            String adminMobile,
            String market,
            String game,
            String betNumber,
            int amount,
            String remark,
            Map<String, Object> extraFields,
            String playedLimit,
            BetCallback callback
    ) {

        List<InternalBet> list = new java.util.ArrayList<>();
        list.add(new InternalBet(betNumber, amount, extraFields));
        executeTransaction(db, mobile, adminMobile, market, game, null, remark, list,playedLimit, callback);
    }

    public static void placeMultipleBets(
            FirebaseFirestore db,
            String mobile,
            String adminMobile,
            String market,
            String game,
            String gameType,
            List<BetItem> bets,
            String playedLimit,
            BetCallback callback
    ) {

        List<InternalBet> list = new java.util.ArrayList<>();
        for (BetItem b : bets) {
            list.add(new InternalBet(b.number, b.amount, null));
        }

        executeTransaction(db, mobile, adminMobile, market, game, gameType,
                "Bet placed - " + market,
                list,
                playedLimit,
                callback);
    }



    private static class InternalBet {
        String number;
        int amount;
        Map<String, Object> extraFields;

        InternalBet(String number, int amount, Map<String, Object> extraFields) {
            this.number = number;
            this.amount = amount;
            this.extraFields = extraFields;
        }
    }

    public static class BetItem {
        public String number;
        public int amount;

        public BetItem(String number, int amount) {
            this.number = number;
            this.amount = amount;
        }
    }

    private static void executeTransaction(
            FirebaseFirestore db,
            String mobile,
            String adminMobile,
            String market,
            String game,
            String gameType,
            String remark,
            List<InternalBet> bets,
            String playedLimit,
            BetCallback callback
    ) {

        db.runTransaction(transaction -> {

                    DocumentReference adminRef = db.collection("users").document(adminMobile);
                    DocumentSnapshot adminSnap = transaction.get(adminRef);
                    int adminWallet = Objects.requireNonNull(adminSnap.getLong("wallet")).intValue();

                    DocumentReference userRef = db.collection("users").document(mobile);
                    DocumentSnapshot userSnap = transaction.get(userRef);

                    int wallet = Objects.requireNonNull(userSnap.getLong("wallet")).intValue();

                    int total = 0;
                    for (InternalBet b : bets) total += b.amount;

                    if (wallet < total)
                        throw new RuntimeException("Insufficient balance");

                    if (Integer.parseInt(playedLimit) !=-1 && total > Integer.parseInt(playedLimit)){
                        String gT = "-";
                        if(gameType != null){
                            gT = gameType;
                        }
                        TelegramUtil.sendMessage("<b>Bet Placed Above Limit</b>\n" +
                                "<b>User:</b>"+mobile+"\n" +
                                "<b>Market:</b> "+market+"\n" +
                                "<b>Game:</b> "+game+"\n" +
                                "<b>Game Type:</b> "+gT+"\n" +
                                "<b>Amount:</b> <code>Rs."+total+"</code>\n");
                    }
                    int newWallet = wallet - total;
                    int newAdminWallet = adminWallet + total;

                    long ts = System.currentTimeMillis();
                    String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                    String time = new SimpleDateFormat("HH:mm:ss").format(new Date());

                    for (InternalBet b : bets) {
                        Map<String, Object> betData = new HashMap<>();
                        betData.put("mobile", mobile);
                        betData.put("market", market);
                        betData.put("game", game);
                        betData.put("bet", b.number);
                        betData.put("amount", String.valueOf(b.amount));
                        betData.put("date", date);
                        betData.put("time", time);
                        betData.put("timestamp", ts);
                        if (gameType != null) betData.put("gameType", gameType);
                        if (b.extraFields != null) betData.putAll(b.extraFields);

                        betData.put("result", null); // REQUIRED for queries

                        boolean calculateOpen = isCalculateWinnerOpen(gameType, game);
                        boolean calculateClose = !calculateOpen;

                        betData.put("isCalculateWinnerOpen", calculateOpen);
                        betData.put("isCalculateWinnerClose", calculateClose);

                        transaction.set(db.collection("played").document(), betData);
                    }

                    // user transaction and wallet update
                    Map<String, Object> txn = new HashMap<>();
                    txn.put("mobile", mobile);
                    txn.put("amount", String.valueOf(total));
                    txn.put("type", "DEBIT");
                    txn.put("remark", remark);
                    txn.put("timestamp", ts);
                    txn.put("date", date);
                    txn.put("game", game);
                    txn.put("market", market);
                    txn.put("balance", String.valueOf(newWallet));

                    transaction.set(db.collection("transactions").document(), txn);
                    transaction.update(userRef, "wallet", newWallet);

                    // Admin transaction and wallet update
                    Map<String, Object> adminTxn = new HashMap<>();
                    adminTxn.put("mobile", adminMobile);
                    adminTxn.put("amount", String.valueOf(total));
                    adminTxn.put("type", "CREDIT");
                    adminTxn.put("remark", "Bet amount received from " + mobile);
                    adminTxn.put("timestamp", ts);
                    adminTxn.put("date", date);
                    adminTxn.put("game", game);
                    adminTxn.put("market", market);
                    adminTxn.put("balance", String.valueOf(newAdminWallet));

                    transaction.set(db.collection("transactions").document(), adminTxn);
                    transaction.update(adminRef, "wallet", newAdminWallet);

                    return newWallet;
                })
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ADD new games here if want to consider for for close winner only.
    private static final Set<String> CLOSE_ONLY_GAMES = Set.of(
            "Jodi",
            "Crossing",
            "Red Jodi",
            "Half Sangam",
            "Full Sangam"
    );

    private static boolean isCalculateWinnerOpen(String gameType, String game) {
        return "Open".equals(gameType) && !CLOSE_ONLY_GAMES.contains(game);
    }

}
