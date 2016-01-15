package nl.kadaster.pdok.bussiness.registryservices;

import nl.kadaster.pdok.bussiness.registryservices.bean.License;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Service
public class LicenseRegistryService extends AbstractInMemoryRegistryService implements RegistryService {

    public LicenseRegistryService() {
        addLicense("http://creativecommons.org/publicdomain/mark/1.0/deed.nl", "Public Domain");
        addLicense("http://creativecommons.org/publicdomain/zero/1.0/", "CC0 (Creative Commons)");
        addLicense("http://creativecommons.org/licenses/by/3.0/nl/", "CC-BY (Creative Commons Naamsvermelding)");
    }

    private void addLicense(String key, String label) {
        License license = new License();
        license.setKey(key);
        license.setTranslation(label);
        addItem(license);
    }
}





