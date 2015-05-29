package org.venth.comparison.serialization

import com.esotericsoftware.kryo.Kryo
import spock.lang.Specification

/**
 * @author Venth (last modified by $Author$).
 * @version $Revision$ $Date$
 */
class KryoSerializationObjectWithDifferentVersionsTest extends Specification {
    
    def "object deserialized to new version of class sets new fields to null"() {
        given:
            def (serializedModelClazz, serializedModelStream) = VersionedModelBuilder.a_model({
                    version_base()
                            .with_string("string field value")
                            .with_primitive(100)
                }).to_inputStream()

        and:
            def (version2Clazz, _ ) = VersionedModelBuilder.a_model()
                    .version_additional_fields().build()

        and:
            def kryo = new Kryo()
            kryo.register version2Clazz

        when:
            def deserializedObject = kryo.readObject serializedModelStream, version2Clazz

        then:
            deserializedObject.secondStringField == null
            deserializedObject.secondPrimitiveField == 0
    }
    
    def "deserialization to new class version sets old fields"() {
        given:
            def (serializedModelClazz, serializedModelStream) = VersionedModelBuilder.a_model({
                version_base()
                        .with_string("string field value")
                        .with_primitive(100)
            }).to_inputStream()

        and:
            def (version2Clazz, _ ) = VersionedModelBuilder.a_model()
                    .version_additional_fields().build()

        and:
            def kryo = new Kryo()
            kryo.register version2Clazz

        when:
            def deserializedObject = kryo.readObject serializedModelStream, version2Clazz

        then:
            deserializedObject.firstStringField == "string field value"
            deserializedObject.firstPrimitiveField == 100
    }

    def "deserialization object of new class to the object of old class sets only old fields"() {
        given:
            def (serializedModelClazz, serializedModelStream) = VersionedModelBuilder.a_model({
                version_additional_fields()
                        .with_string("string field value")
                        .with_primitive(100)
                        .with_new_string("new string field value")
                        .with_new_primitive(200)
            }).to_inputStream()

        and:
            def (version1Clazz, _ ) = VersionedModelBuilder.a_model()
                    .version_base().build()

        and:
            def kryo = new Kryo()
            kryo.register version1Clazz

        when:
            def deserializedObject = kryo.readObject serializedModelStream, version1Clazz

        then:
            deserializedObject.firstStringField == "string field value"
            deserializedObject.firstPrimitiveField == 100
    }
}





