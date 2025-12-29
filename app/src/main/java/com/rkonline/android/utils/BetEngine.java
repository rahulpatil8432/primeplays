package com.rkonline.android.utils;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BetEngine {

    public interface BetCallback {
        void onSuccess(int newWallet);
        void onFailure(String error);
    }

    public static void placeBet(
            FirebaseFirestore db,
            String mobile,
            String market,
            String game,
            String betNumber,
            int amount,
            String remark,
            Map<String, Object> extraFields,
            BetCallback callback
    ) {

        DocumentReference userRef = db.collection("users").document(mobile);

        db.runTransaction(transaction -> {

                    DocumentSnapshot snap = transaction.get(userRef);
                    int wallet = Objects.requireNonNull(snap.getLong("wallet")).intValue();

                    if (wallet < amount) throw new RuntimeException("Insufficient balance");

                    int newWallet = wallet - amount;

                    long ts = System.currentTimeMillis();
                    String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                    String time = new SimpleDateFormat("HH:mm:ss").format(new Date());

                    Map<String, Object> bet = new HashMap<>();
                    bet.put("mobile", mobile);
                    bet.put("market", market);
                    bet.put("game", game);
                    bet.put("bet", betNumber);
                    bet.put("amount", String.valueOf(amount));
                    bet.put("date", date);
                    bet.put("time", time);
                    bet.put("timestamp", ts);

                    if (extraFields != null) bet.putAll(extraFields);

                    transaction.set(db.collection("played").document(), bet);

                    Map<String, Object> txn = new HashMap<>();
                    txn.put("mobile", mobile);
                    txn.put("amount", String.valueOf(amount));
                    txn.put("type", "DEBIT");
                    txn.put("remark", remark);
                    txn.put("timestamp", ts);
                    txn.put("date", date);
                    txn.put("game", game);
                    txn.put("market", market);
                    txn.put("balance", String.valueOf(newWallet));

                    transaction.set(db.collection("transactions").document(), txn);
                    transaction.update(userRef, "wallet", newWallet);

                    return newWallet;

                }).addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

}
