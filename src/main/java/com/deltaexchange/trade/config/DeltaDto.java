package com.deltaexchange.trade.config;

import org.springframework.stereotype.Service;

@Service
public class DeltaDto {

    private double tpPrice = 0;

    private double avgPrice = 0;

    public double getTpPrice() {
        return tpPrice;
    }

    public void setTpPrice(double tpPrice) {
        this.tpPrice = tpPrice;
    }

    public double getAvgPrice() {
        return avgPrice;
    }

    public void setAvgPrice(double avgPrice) {
        this.avgPrice = avgPrice;
    }

    
    

}
