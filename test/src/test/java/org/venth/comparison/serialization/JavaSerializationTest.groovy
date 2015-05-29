package org.venth.comparison.serialization

import spock.lang.Specification

/**
 * @author Venth on 29/05/2015
 *
 * Please note, that all the scenarios are based on
 * the same model class.
 *
 * The model class contains different sets of fields in different
 * versions.
 *
 * All the classes have the same serial version uuid.
 */
class JavaSerializationTest extends Specification {
    def "object deserialized to new version of class sets new fields to null"() {
        given: 'a objecvt made from the base model class'
        def (serializedModelClazz, serializedObjectFromBaseClass, baseCl) = VersionedModelBuilder.a_model({
            version_base()
                    .with_string("string field value")
                    .with_primitive(100)
        }).to_object()

        and: 'the model class with additional fields is loaded'
        def (version2Clazz, _, addCl ) = VersionedModelBuilder.a_model()
                .version_additional_fields().build()

        and: 'object is serialized'
        def output = new ByteArrayOutputStream()
        def serializedStream = new ObjectOutputStream(output)
        serializedStream.writeObject(serializedObjectFromBaseClass)

        when: 'object is deserialized to the model class with additional fields'
        def deserializationStream = deserializae_toStream(output, addCl)
        def deserializedObject = deserializationStream.readObject()

        then: "after deserialization new fields are empty"
        deserializedObject.secondStringField == null
        deserializedObject.secondPrimitiveField == 0
    }

    def "deserialization to new class version sets old fields"() {
        given: 'object mage from base class version'
        def (serializedModelClazz, serializedObjectFromBaseClass, baseCl) = VersionedModelBuilder.a_model({
            version_base()
                    .with_string("string field value")
                    .with_primitive(100)
        }).to_object()

        and: 'the model class with additional fields is loaded in a separate classloader'
        def (version2Clazz, _, addCl ) = VersionedModelBuilder.a_model()
                .version_additional_fields().build()

        and: 'object is serialized to the stream'
        def output = new ByteArrayOutputStream()
        def serializedStream = new ObjectOutputStream(output)
        serializedStream.writeObject(serializedObjectFromBaseClass)

        when: 'object is deserialized to a class with additional fields'
        def deserializationStream = deserializae_toStream(output, addCl)
        def deserializedObject = deserializationStream.readObject()

        then:
        deserializedObject.firstStringField == "string field value"
        deserializedObject.firstPrimitiveField == 100
    }

    def "deserialization to class version with missing fields sets only existing fields"() {
        given: 'an object made from the base class'
        def (serializedModelClazz, serializedObjectFromBaseClass, baseCl) = VersionedModelBuilder.a_model({
            version_base()
                    .with_string("string field value")
                    .with_primitive(100)
        }).to_object()

        and: 'model class with missing field is loaded to a separate classloader'
        def (version2Clazz, _, missingCl ) = VersionedModelBuilder.a_model()
                .version_missing_field().build()

        and: 'an object from base class is serialized to a stream'
        def output = new ByteArrayOutputStream()
        def serializedStream = new ObjectOutputStream(output)
        serializedStream.writeObject(serializedObjectFromBaseClass)

        when: 'object is deserialized to a class with missing string property'
        def deserializationStream = deserializae_toStream(output, missingCl)
        def deserializedObject = deserializationStream.readObject()

        then: 'only primitive field is filled'
        deserializedObject.firstPrimitiveField == 100
    }

    def "cannot deserialize to class version with different field type"() {
        given: 'an object made from the base class'
        def (serializedModelClazz, serializedObjectFromBaseClass, baseCl) = VersionedModelBuilder.a_model({
            version_base()
                    .with_string("string field value")
                    .with_primitive(100)
        }).to_object()

        and: 'model class with changed field type is loaded to a separate classloader'
        def (version2Clazz, _, changedFieldTypeCl ) = VersionedModelBuilder.a_model()
                .version_changed_field_type().build()

        and: 'an object from base class is serialized to a stream'
        def output = new ByteArrayOutputStream()
        def serializedStream = new ObjectOutputStream(output)
        serializedStream.writeObject(serializedObjectFromBaseClass)

        when: 'object is deserialized to the class with changed field type'
        def deserializationStream = deserializae_toStream(output, changedFieldTypeCl)
        def deserializedObject = deserializationStream.readObject()

        then: 'cannot deserialize because of changed field type'
        def e = thrown(InvalidClassException)
        e.message == "org.venth.comparison.serialization.model.VersionedModel; incompatible types for field firstPrimitiveField"
    }

    def "deserialization object of new class to the object of old class sets only old fields"() {
        given: 'an object made from the model class with additional fields'
        def (serializedModelClazz, serializedObjectFromClassWithAdditionalFields, addCl) = VersionedModelBuilder.a_model({
            version_additional_fields()
                    .with_string("string field value")
                    .with_primitive(100)
                    .with_new_string("new string field value")
                    .with_new_primitive(200)
        }).to_object()

        and: 'the base model class is loaded in a separate classloader'
        def (version1Clazz, _, baseCl ) = VersionedModelBuilder.a_model()
                .version_base().build()

        and: 'object is serialized'
        def output = new ByteArrayOutputStream()
        def serializedStream = new ObjectOutputStream(output)
        serializedStream.writeObject(serializedObjectFromClassWithAdditionalFields)

        when: 'object is deserialized'
        def deserializationStream = deserializae_toStream(output, baseCl)
        def deserializedObject = deserializationStream.readObject()

        then: 'original fields are set apropriate to the serialized object'
        deserializedObject.firstStringField == "string field value"
        deserializedObject.firstPrimitiveField == 100
    }

    def "deserialization object of class with additional field to the object of the old class with different field sequence sets only old fields"() {
        given: 'an object with additional fields'
        def (serializedModelClazz, serializedObjectFromClassWithAdditionalFields, addCl) = VersionedModelBuilder.a_model({
            version_additional_fields()
                    .with_string("string field value")
                    .with_primitive(100)
                    .with_new_string("new string field value")
                    .with_new_primitive(200)
        }).to_object()

        and: 'a model class with a different field sequence is loaded in a separate classloader'
        def (version1Clazz, _, differentSequenceCl ) = VersionedModelBuilder.a_model()
                .version_different_sequence().build()

        and: 'the object with additional fields is serialized'
        def output = new ByteArrayOutputStream()
        def serializedStream = new ObjectOutputStream(output)
        serializedStream.writeObject(serializedObjectFromClassWithAdditionalFields)

        when: 'the serialized object is deserialized to a class with different field sequence'
        def deserializationStream = deserializae_toStream(output, differentSequenceCl)
        def deserializedObject = deserializationStream.readObject()


        then: 'only first fields are filled - because they exists in the target class'
        deserializedObject.firstStringField == "string field value"
        deserializedObject.firstPrimitiveField == 100
    }

    private ObjectInputStream deserializae_toStream(output, classLoader) {
        return new ObjectInputStream(new ByteArrayInputStream(output.toByteArray())) {
            @Override
            protected Class<?> resolveClass(ObjectStreamClass desc)
                    throws IOException, ClassNotFoundException {
                String name = desc.getName();
                try {
                    return Class.forName(name, false, classLoader);
                } catch (ClassNotFoundException ex) {
                    Class cl = (Class) primClasses.get(name);
                    if (cl != null) {
                        return cl;
                    } else {
                        throw ex;
                    }
                }
            }
        }
    }


}
