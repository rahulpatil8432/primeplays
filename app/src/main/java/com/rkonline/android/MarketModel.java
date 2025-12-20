package com.rkonline.android;
import com.google.firebase.firestore.IgnoreExtraProperties;
@IgnoreExtraProperties
public class MarketModel {

    public String name;
    public String openResult;
    public String closeResult;
    public String marketId;

    public String jodi;

    public MarketModel() {
    }

    public String getName() { return name; }
    public String getOpenResult() { return openResult; }
    public String getCloseResult() { return closeResult; }
    public String getJodi() { return jodi; }

    public String getMarketId() { return marketId; }

    public void setName(String name) { this.name = name; }
    public void setOpenResult(String openResult) { this.openResult = openResult; }
    public void setCloseResult(String closeResult) { this.closeResult = closeResult; }
    public void setMarketId(String marketId) { this.marketId = marketId; }
    public void setJodi(String jodi) { this.jodi = jodi; }
}
