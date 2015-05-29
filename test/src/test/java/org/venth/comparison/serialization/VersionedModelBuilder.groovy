package org.venth.comparison.serialization

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output

/**
 * @author Venth on 29/05/2015
 */
class VersionedModelBuilder {

    def modelBuilder

    static def a_model() {
        new VersionedModelBuilder()
    }

    static def a_model(Closure version) {
        def builder = new VersionedModelBuilder()
        version.delegate = builder
        builder.modelBuilder = version()

        builder
    }

    def version_base() {
        new VersionedModel_VersionBaseBuilder()
    }

    def version_additional_fields() {
        new VersionedModel_VersionAdditionalFieldsBuilder()
    }

    def version_missing_field() {
        new VersionedModel_VersionMissingFieldBuilder()
    }

    def version_different_sequence() {
        new VersionedModel_VersionDifferentSequenceBuilder()
    }

    def version_changed_field_type() {
        new VersionedModel_VersionChangedFieldTypeBuilder()
    }

    def to_inputStream() {
        def (clazz, model) = modelBuilder.build()
        def kryo = new Kryo()

        def outputStream = new ByteArrayOutputStream()
        def out = new Output(outputStream)

        kryo.writeObject(out, model)
        out.flush()

        [clazz, new Input(new ByteArrayInputStream(outputStream.toByteArray()))]
    }
    def to_object() {
        def (clazz, model, separateLoader) = modelBuilder.build()

        [clazz, model, separateLoader]
    }
}
