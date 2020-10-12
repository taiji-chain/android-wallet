package io.taiji.wallet;

import com.networknt.taiji.utility.Converter;

import org.junit.Test;

import io.taiji.wallet.utils.UnitCalculator;

public class UnitCalculatorTest {
    @Test
    public void testSmall() {
        long l = 50000l;
        double d = Converter.fromShellToDouble(l, Converter.Unit.TAIJI);
        System.out.printf("%f\n", d);
        String s = UnitCalculator.getInstance().displayBalanceNicely(d);
        System.out.println("s = " + s);
    }
}
