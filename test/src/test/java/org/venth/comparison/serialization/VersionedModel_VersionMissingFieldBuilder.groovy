package org.venth.comparison.serialization

/**
 * @author Venth on 29/05/2015
 */
class VersionedModel_VersionMissingFieldBuilder {

    private String stringValue
    private float primitiveValue

    ClassLoader separateLoader


    def with_primitive(float value) {
        this.primitiveValue = value
        this
    }

    def with_string(String value) {
        this.stringValue = value
        this
    }

    def build() {
        def (clazz, model) = createInstance()
        model.firstPrimitiveField = primitiveValue

        [clazz, model, separateLoader]
    }

    Object createInstance() {
        def modelClazz = loadClass(
                "/model-version-missing-field-1.0-SNAPSHOT.jar",
                "org.venth.comparison.serialization.model.VersionedModel"
        );

        [modelClazz, modelClazz.newInstance()]
    }

    Class<?> loadClass(String jarFile, String classSignature) {
        separateLoader = new URLClassLoader(
                [getClass().getResource(jarFile)] as URL[],
                Thread.currentThread().getContextClassLoader()
        );

        return separateLoader.loadClass(classSignature);
    }
}
