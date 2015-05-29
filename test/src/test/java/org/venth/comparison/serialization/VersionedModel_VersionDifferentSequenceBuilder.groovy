package org.venth.comparison.serialization

/**
 * @author Venth on 29/05/2015
 */
class VersionedModel_VersionDifferentSequenceBuilder extends VersionedModel_VersionBaseBuilder {

    Object createInstance() {
        def modelClazz = loadClass(
                "/model-version-different-sequence-1.0-SNAPSHOT.jar",
                "org.venth.comparison.serialization.model.VersionedModel"
        );

        [modelClazz, modelClazz.newInstance()]
    }
}
