package nl.kadaster.pdok.bussiness;

import nl.kadaster.pdok.bussiness.registryservices.CodelistElement;
import nl.kadaster.pdok.bussiness.registryservices.bean.Denominator;
import nl.kadaster.pdok.bussiness.registryservices.bean.License;
import nl.kadaster.pdok.bussiness.registryservices.bean.Location;
import nl.kadaster.pdok.bussiness.registryservices.bean.TopicCategory;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.util.List;

/**
 * Created by JuanLuis on 15/01/2016.
 */
@XmlRootElement(name = "response")
@XmlSeeAlso({Denominator.class, License.class, TopicCategory.class, Location.class})
public class GenericArrayResponse {
    private List<? extends CodelistElement> list;

    public GenericArrayResponse(List<? extends CodelistElement> list) {
        this.list = list;
    }

    public GenericArrayResponse() {
    }

    @XmlAnyElement
    public List<? extends CodelistElement> getList() {
        return list;
    }

    public void setList(List<? extends CodelistElement> list) {
        this.list = list;
    }
}
