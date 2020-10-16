package io.taiji.wallet.utils.qr;

import org.junit.Assert;
import org.junit.Test;

public class AddressEncoderTest {
    @Test
    public void testEncoder() throws Exception {
        AddressEncoder encoder = new AddressEncoder("0000e5099B5aa298ea822dc05aB5999f8926677C", "5000");
        String encoded = AddressEncoder.encodeERC(encoder);
        System.out.println(encoded);
        AddressEncoder object = AddressEncoder.decode(encoded);
        Assert.assertTrue("0000e5099B5aa298ea822dc05aB5999f8926677C".equals(object.getAddress()));
        Assert.assertTrue("5000".equals(object.getAmount()));
    }

    @Test
    public void testEncoderWithData() throws Exception {
        AddressEncoder encoder = new AddressEncoder("0000e5099B5aa298ea822dc05aB5999f8926677C", "5000");
        encoder.setData("ABC");
        String encoded = AddressEncoder.encodeERC(encoder);
        System.out.println(encoded);
        AddressEncoder object = AddressEncoder.decode(encoded);
        Assert.assertTrue("0000e5099B5aa298ea822dc05aB5999f8926677C".equals(object.getAddress()));
        Assert.assertTrue("5000".equals(object.getAmount()));
        Assert.assertTrue("ABC".equals(object.getData()));
    }

}
