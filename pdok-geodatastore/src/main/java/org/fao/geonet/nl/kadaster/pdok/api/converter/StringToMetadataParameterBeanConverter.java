package org.fao.geonet.nl.kadaster.pdok.api.converter;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.kadaster.pdok.bussiness.MetadataParametersBean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Created by JuanLuis on 23/07/2015.
 */
@Component
public class StringToMetadataParameterBeanConverter implements Converter<String, MetadataParametersBean> {
    @Override
    public MetadataParametersBean convert(String source) {
        ObjectMapper mapper = new ObjectMapper();
        MetadataParametersBean mpb = null;

        try {
            mpb = mapper.readValue(source, MetadataParametersBean.class);
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mpb;
    }
}
