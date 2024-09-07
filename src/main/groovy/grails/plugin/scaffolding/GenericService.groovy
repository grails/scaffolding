package grails.plugin.scaffolding

import grails.artefact.Artefact
import grails.gorm.transactions.ReadOnly
import grails.util.GrailsNameUtils

@Artefact("Service")
@ReadOnly
class GenericService<T> {

    Class<T> resource
    String resourceName
    String resourceClassName
    boolean readOnly

    GenericService(Class<T> resource, boolean readOnly) {
        this.resource = resource
        this.readOnly = readOnly
        resourceClassName = resource.simpleName
        resourceName = GrailsNameUtils.getPropertyName(resource)
    }

    protected T queryForResource(Serializable id) {
        resource.get(id)
    }

    T get(Serializable id) {
        queryForResource(id)
    }

    List<T> list(Map args) {
        resource.list(args)
    }

    Long count() {
        resource.count()
    }

    void delete(Serializable id) {
        queryForResource(id).delete flush: true
    }

    T save(T instance) {
        instance.save flush: true
    }
}