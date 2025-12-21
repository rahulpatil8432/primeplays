package com.rkonline.android;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import android.widget.ImageView;


class adapter_game extends RecyclerView.Adapter<adapter_game.ViewHolder> {

    Context context;
    ArrayList<String> name = new ArrayList<>();
    ArrayList<String> rate = new ArrayList<>();
    ArrayList<String> number = new ArrayList<>();
    String market;

    Boolean isMarketOpen;

    public adapter_game(Context context,  ArrayList<String> name, ArrayList<String> rate, String market, boolean isMarketOpen) {
        this.context = context;
        this.name = name;
        this.rate = rate;
        this.market = market;
        this.isMarketOpen = isMarketOpen;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.game_layout, parent, false);
        if(this.rate.isEmpty()){
            v.findViewById(R.id.rate).setVisibility(View.GONE);
        }
        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        String gameName = name.get(position);
        holder.name.setText(gameName);
        if (!rate.isEmpty()) {
            holder.rate.setText(rate.get(position));
            holder.rate.setVisibility(View.VISIBLE);
        } else {
            holder.rate.setVisibility(View.GONE);
        }
        switch (gameName) {

            case "Single Ank":
                holder.gameIcon.setImageResource(R.drawable.ic_single_digit);
                break;

            case "Single Ank Bulk":
                holder.gameIcon.setImageResource(R.drawable.ic_single_digit_bulk);
                break;

            case "Jodi":
                holder.gameIcon.setImageResource(R.drawable.ic_jodi);
                break;

            case "Jodi Bulk":
                holder.gameIcon.setImageResource(R.drawable.ic_jodi_bulk);
                break;

            case "Single Pana":
                holder.gameIcon.setImageResource(R.drawable.ic_single_pana);
                break;

            case "Double Pana":
                holder.gameIcon.setImageResource(R.drawable.ic_double_pana);
                break;

            case "Triple Pana":
                holder.gameIcon.setImageResource(R.drawable.ic_triple_pana);
                break;

            case "Half Sangam":
                holder.gameIcon.setImageResource(R.drawable.ic_half_sangam);
                break;

            case "Full Sangam":
                holder.gameIcon.setImageResource(R.drawable.ic_full_sangam);
                break;

            default:
                holder.gameIcon.setImageResource(R.drawable.ic_single_digit);
                break;
        }

        /* ---------------- CLICK LOGIC ONLY ---------------- */

        holder.layout.setOnClickListener(v -> {

            number = new ArrayList<>();

            switch (gameName) {
                case "Single Ank":
                    single();
                    break;

                case "Jodi":
                case "crossing":
                    jodi();
                    break;

                case "Single Pana":
                    singlepatti();
                    break;

                case "Double Pana":
                    doublepatti();
                    break;

                case "Triple Pana":
                    triplepatti();
                    break;

                default:
                    triplepatti();
                    break;
            }

            Intent go;

            switch (gameName) {
                case "Half Sangam":
                    go = new Intent(context, halfsangam.class);
                    break;

                case "Full Sangam":
                    go = new Intent(context, fullsangam.class);
                    break;

                case "crossing":
                    go = new Intent(context, crossing.class);
                    break;

                default:
                    go = new Intent(context, betting.class);
                    break;
            }

            go.putExtra("list", number);
            go.putExtra("game", gameName);
            go.putExtra("market", market);
            go.putExtra("isMarketOpen", isMarketOpen);
            go.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if(!market.isEmpty()){
                context.startActivity(go);
            }
        });
    }


    @Override
    public int getItemCount() {
        return name.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView name, rate;
        LinearLayout layout;
        ImageView gameIcon;

        public ViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.gameName);
            rate = view.findViewById(R.id.rate);
            layout = view.findViewById(R.id.layout);
            gameIcon = view.findViewById(R.id.gameIcon);
        }
    }

    public void single() {
        for (int i = 0; i <= 9; i++) number.add("" + i);
    }
    public void jodi() {
        for (int i = 0; i < 100; i++) {
            String temp = String.format("%02d", i);
            number.add(temp);
        }
    }
    public void singlepatti() {
        number.add("128");
        number.add("137");
        number.add("146");
        number.add("236");
        number.add("245");
        number.add("290");
        number.add("380");
        number.add("470");
        number.add("489");
        number.add("560");
        number.add("678");
        number.add("579");
        number.add("129");
        number.add("138");
        number.add("147");
        number.add("156");
        number.add("237");
        number.add("246");
        number.add("345");
        number.add("390");
        number.add("480");
        number.add("570");
        number.add("679");
        number.add("120");
        number.add("139");
        number.add("148");
        number.add("157");
        number.add("238");
        number.add("247");
        number.add("256");
        number.add("346");
        number.add("490");
        number.add("580");
        number.add("670");
        number.add("689");
        number.add("130");
        number.add("149");
        number.add("158");
        number.add("167");
        number.add("239");
        number.add("248");
        number.add("257");
        number.add("347");
        number.add("356");
        number.add("590");
        number.add("680");
        number.add("789");
        number.add("140");
        number.add("159");
        number.add("168");
        number.add("230");
        number.add("249");
        number.add("258");
        number.add("267");
        number.add("348");
        number.add("357");
        number.add("456");
        number.add("690");
        number.add("780");
        number.add("123");
        number.add("150");
        number.add("169");
        number.add("178");
        number.add("240");
        number.add("259");
        number.add("268");
        number.add("349");
        number.add("358");
        number.add("457");
        number.add("367");
        number.add("790");
        number.add("124");
        number.add("160");
        number.add("179");
        number.add("250");
        number.add("269");
        number.add("278");
        number.add("340");
        number.add("359");
        number.add("368");
        number.add("458");
        number.add("467");
        number.add("890");
        number.add("125");
        number.add("134");
        number.add("170");
        number.add("189");
        number.add("260");
        number.add("279");
        number.add("350");
        number.add("369");
        number.add("378");
        number.add("459");
        number.add("567");
        number.add("468");
        number.add("126");
        number.add("135");
        number.add("180");
        number.add("234");
        number.add("270");
        number.add("289");
        number.add("360");
        number.add("379");
        number.add("450");
        number.add("469");
        number.add("478");
        number.add("568");
        number.add("127");
        number.add("136");
        number.add("145");
        number.add("190");
        number.add("235");
        number.add("280");
        number.add("370");
        number.add("479");
        number.add("460");
        number.add("569");
        number.add("389");
        number.add("578");
        number.add("589");
    }

    public void doublepatti() {
        number.add("100");
        number.add("119");
        number.add("155");
        number.add("227");
        number.add("335");
        number.add("344");
        number.add("399");
        number.add("588");
        number.add("669");
        number.add("200");
        number.add("110");
        number.add("228");
        number.add("255");
        number.add("336");
        number.add("499");
        number.add("660");
        number.add("688");
        number.add("778");
        number.add("300");
        number.add("166");
        number.add("229");
        number.add("337");
        number.add("355");
        number.add("445");
        number.add("599");
        number.add("779");
        number.add("788");
        number.add("400");
        number.add("112");
        number.add("220");
        number.add("266");
        number.add("338");
        number.add("446");
        number.add("455");
        number.add("699");
        number.add("770");
        number.add("500");
        number.add("113");
        number.add("122");
        number.add("177");
        number.add("339");
        number.add("366");
        number.add("447");
        number.add("799");
        number.add("889");
        number.add("600");
        number.add("114");
        number.add("277");
        number.add("330");
        number.add("448");
        number.add("466");
        number.add("556");
        number.add("880");
        number.add("899");
        number.add("700");
        number.add("115");
        number.add("133");
        number.add("188");
        number.add("223");
        number.add("377");
        number.add("449");
        number.add("557");
        number.add("566");
        number.add("800");
        number.add("116");
        number.add("224");
        number.add("233");
        number.add("288");
        number.add("440");
        number.add("477");
        number.add("558");
        number.add("990");
        number.add("900");
        number.add("117");
        number.add("144");
        number.add("199");
        number.add("225");
        number.add("388");
        number.add("559");
        number.add("577");
        number.add("667");
        number.add("550");
        number.add("668");
        number.add("244");
        number.add("299");
        number.add("226");
        number.add("488");
        number.add("677");
        number.add("118");
        number.add("334");
    }

    public void triplepatti() {
        number.add("000");
        number.add("111");
        number.add("222");
        number.add("333");
        number.add("444");
        number.add("555");
        number.add("666");
        number.add("777");
        number.add("888");
        number.add("999");
    }

}
