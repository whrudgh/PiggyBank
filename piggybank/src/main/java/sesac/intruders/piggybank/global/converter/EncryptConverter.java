package sesac.intruders.piggybank.global.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import sesac.intruders.piggybank.global.util.EncryptionUtil;

@Converter
public class EncryptConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null)
            return null;
        return EncryptionUtil.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null)
            return null;
        return EncryptionUtil.decrypt(dbData);
    }
}