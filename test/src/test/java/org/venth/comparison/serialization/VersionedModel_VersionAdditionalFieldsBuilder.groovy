package org.venth.comparison.serialization

/**
 * @author Venth on 29/05/2015
 */
class VersionedModel_VersionAdditionalFieldsBuilder extends VersionedModel_VersionBaseBuilder {
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
                "/model-version-additional-fields-1.0-SNAPSHOT.jar",
                "org.venth.comparison.serialization.model.VersionedModel"
        );

        [modelClazz, modelClazz.newInstance()]
    }

    def build() {
        def (clazz, model) = super.build()
        model.secondStringField = newStringValue
        model.secondPrimitiveField = newPrimitiveValue

        [clazz, model, separateLoader]
    }
}
