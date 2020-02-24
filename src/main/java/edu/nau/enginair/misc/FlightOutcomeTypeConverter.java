package edu.nau.enginair.misc;

import dev.morphia.converters.SimpleValueConverter;
import dev.morphia.converters.TypeConverter;
import dev.morphia.mapping.MappedField;
import edu.nau.enginair.models.FlightOutcome;

public class FlightOutcomeTypeConverter extends TypeConverter implements SimpleValueConverter {
    @Override
    protected boolean isSupported(Class c, MappedField
            optionalExtraInfo) {
        return c.equals(FlightOutcome.class);
    }
    @Override
    public Object decode(Class<?> aClass, Object o, MappedField mappedField) {
        return FlightOutcome.fromInt((Integer) o);
    }

    @Override
    public Object encode(Object value, MappedField optionalExtraInfo)
    {
        if (value == null) {
            return null;
        }

        return ((FlightOutcome) value).getOutcomeNum();
    }
}
