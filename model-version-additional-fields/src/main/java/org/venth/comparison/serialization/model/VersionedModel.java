package org.venth.comparison.serialization.model;

import com.esotericsoftware.kryo.serializers.FieldSerializer;

import java.io.Serializable;

/**
 * @author venth
 *
 */
public class VersionedModel implements Serializable {

    private static final long serialVersionUID = 8492539216375372328L;

    public String firstStringField;
    public int firstPrimitiveField;

    @FieldSerializer.Optional("secondStringField")
    public String secondStringField;
    @FieldSerializer.Optional("secondPrimitiveField")
    public int secondPrimitiveField;
}
