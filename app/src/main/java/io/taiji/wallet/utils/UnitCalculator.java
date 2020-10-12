package io.taiji.wallet.utils;

import com.networknt.taiji.utility.Converter;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import io.taiji.wallet.data.UnitEntry;

public class UnitCalculator {

    private static UnitCalculator instance;
    private DecimalFormat formatterCrypt = new DecimalFormat("#,###,###.####");
    private DecimalFormat formatterCryptExact = new DecimalFormat("#,###,###.#######");

    private UnitCalculator() {
    }

    public static UnitCalculator getInstance() {
        if (instance == null)
            instance = new UnitCalculator();
        return instance;
    }

    private UnitEntry[] conversionNames = new UnitEntry[]{
            new UnitEntry("SHELL", Converter.Unit.SHELL, "S"),
            new UnitEntry("KSHELL", Converter.Unit.KSHELL, "KS"),
            new UnitEntry("MSHELL", Converter.Unit.MSHELL, "MS"),
            new UnitEntry("TAIJI", Converter.Unit.TAIJI, "T"),
            new UnitEntry("KTAIJI", Converter.Unit.KTAIJI, "KT"),
            new UnitEntry("MTAIJI", Converter.Unit.MTAIJI, "MT")
    };

    private int index = 0;

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public UnitEntry next() {
        index = (index + 1) % conversionNames.length;
        return conversionNames[index];
    }

    public UnitEntry getCurrent() {
        return conversionNames[index];
    }

    public UnitEntry previous() {
        index = index > 0 ? index - 1 : conversionNames.length - 1;
        return conversionNames[index];
    }

    public UnitEntry getMainUnit() {
        return conversionNames[3];
    }

    public UnitEntry getShellCurrency() {
        return conversionNames[0];
    }

    public String getCurrencyShort() {
        return conversionNames[index].getShorty();
    }

    public double convertUnit(long balance, Converter.Unit unit) {
        // convert from shell to the unit specified
        return Converter.fromShellToDouble(balance, unit);
    }


    public String displayBalanceNicely(double d) {
        return formatterCrypt.format(d);
    }

}
