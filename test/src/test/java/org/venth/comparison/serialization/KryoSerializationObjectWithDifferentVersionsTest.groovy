package org.venth.comparison.serialization

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import spock.lang.Specification

/**
 * @author Venth (last modified by $Author$).
 * @version $Revision$ $Date$
 */
class KryoSerializationObjectWithDifferentVersionsTest extends Specification {
    
    def "object deserialized to new version of class sets new fields to null"() {
        given:
            def (serializedModelClazz, serializedModelStream) = VersionedModelBuilder.a_model({
                    version1()
                            .with_string("string field value")
                            .with_primitive(100)
                }).to_inputStream()

        and:
            def (version2Clazz, _ ) = VersionedModelBuilder.a_model()
                    .version2().build()

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
                version1()
                        .with_string("string field value")
                        .with_primitive(100)
            }).to_inputStream()

        and:
            def (version2Clazz, _ ) = VersionedModelBuilder.a_model()
                    .version2().build()

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
                version2()
                        .with_string("string field value")
                        .with_primitive(100)
                        .with_new_string("new string field value")
                        .with_new_primitive(200)
            }).to_inputStream()

        and:
            def (version1Clazz, _ ) = VersionedModelBuilder.a_model()
                    .version1().build()

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



class VersionedModelBuilder {

    def modelBuilder

    static def a_model() {
        new VersionedModelBuilder()
    }

    static def a_model(Closure version)
    {
        def builder = new VersionedModelBuilder()
        version.delegate = builder
        builder.modelBuilder = version()

        builder
    }

    def version1() {
        new VersionedModel_Version1Builder()
    }

    def version2() {
        new VersionedModel_Version2Builder()
    }

    def to_inputStream() {
        def (clazz, model) = modelBuilder.build()
        def kryo = new Kryo()

        def outputStream = new ByteArrayOutputStream()
        def out = new Output(outputStream)

        kryo.writeObject(out, model)
        out.flush()

        [ clazz, new Input(new ByteArrayInputStream(outputStream.toByteArray())) ]
    }
}

class VersionedModel_Version1Builder {

    private String stringValue
    private int primitiveValue

    def with_string(String value) {
        this.stringValue = value
        this
    }

    def with_primitive(int value) {
        this.primitiveValue = value
        this
    }

    def build() {
        def (clazz, model) = createInstance()
        model.firstStringField = stringValue
        model.firstPrimitiveField = primitiveValue

        [ clazz, model ]
    }

    Object createInstance() {
       def modelClazz = loadClass(
                "/model-version1-1.0-SNAPSHOT.jar",
                "org.venth.comparison.serialization.model.VersionedModel"
        );

        [ modelClazz, modelClazz.newInstance() ]
    }

    Class<?> loadClass(String jarFile, String classSignature) {
        ClassLoader separateLoader = new URLClassLoader(
                [ getClass().getResource(jarFile) ] as URL[],
                Thread.currentThread().getContextClassLoader()
        );

        return separateLoader.loadClass(classSignature);
    }
}

class VersionedModel_Version2Builder extends VersionedModel_Version1Builder {
    private String newStringValue
    private int newPrimitiveValue

    def with_new_string(String value) {
        newStringValue = value
        this
    }

    def with_new_primitive(int value) {
        newPrimitiveValue = value
        this
    }

    Object createInstance() {
        def modelClazz = loadClass(
                "/model-version2-1.0-SNAPSHOT.jar",
                "org.venth.comparison.serialization.model.VersionedModel"
        );

        [ modelClazz, modelClazz.newInstance() ]
    }

    def build() {
        def (clazz, model) = super.build()
        model.secondStringField = newStringValue
        model.secondPrimitiveField = newPrimitiveValue

        [ clazz, model ]
    }
}