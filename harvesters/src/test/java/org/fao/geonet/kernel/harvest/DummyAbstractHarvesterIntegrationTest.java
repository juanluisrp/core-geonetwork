package org.fao.geonet.kernel.harvest;

import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.repository.UserRepository;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Jeroen Bloemscheer on 22-6-2015.
 * TODO: Check the CSW Harvester Integration tests by remove this dummy and ammending CSWHarvesterIntegrationTest
 */
public class DummyAbstractHarvesterIntegrationTest {
    public DummyAbstractHarvesterIntegrationTest(String harvester) {

    }
    @Autowired
    protected UserRepository _userRepo;
    protected Element fileStream(String s) throws Exception {
        throw new Exception();
    }

    protected int getExpectedAdded() {
        return 0;
    }

    protected int getExpectedTotalFound() {
        return 0;
    }
    protected void performExtraAssertions(AbstractHarvester harvester) {}
}
