package org.grails.compiler.scaffolding

import grails.compiler.ast.AstTransformer
import grails.compiler.ast.GrailsArtefactClassInjector
import grails.plugin.scaffolding.annotation.ScaffoldController
import grails.rest.RestfulController
import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.classgen.GeneratorContext
import org.codehaus.groovy.control.SourceUnit
import org.grails.compiler.injection.GrailsASTUtils
import org.grails.compiler.web.ControllerActionTransformer
import org.grails.core.artefact.ControllerArtefactHandler
import org.grails.plugins.web.rest.transform.ResourceTransform

/**
 * Transformation that turns a controller into a scaffolding controller at compile time of 'static scaffold = Foo'
 * is specified
 *
 * @author Graeme Rocher
 * @since 3.1
 */
@AstTransformer
@CompileStatic
class ScaffoldingControllerInjector implements GrailsArtefactClassInjector {

    public static final String PROPERTY_SCAFFOLD = "scaffold"

    final String[] artefactTypes = [ControllerArtefactHandler.TYPE] as String[]

    @Override
    void performInjection(SourceUnit source, GeneratorContext context, ClassNode classNode) {
        performInjectionOnAnnotatedClass(source, classNode)
    }

    @Override
    void performInjection(SourceUnit source, ClassNode classNode) {
        performInjectionOnAnnotatedClass(source, classNode)
    }

    @Override
    void performInjectionOnAnnotatedClass(SourceUnit source, ClassNode classNode) {
        def propertyNode = classNode.getProperty(PROPERTY_SCAFFOLD)
        def annotationNode = classNode.getAnnotations(ClassHelper.make(ScaffoldController)).find()

        def expression = propertyNode?.getInitialExpression()
        if (expression instanceof ClassExpression || annotationNode) {
            ClassNode superClassNode = ClassHelper.make(annotationNode?.getMember("value")?.type?.getTypeClass()?:RestfulController).getPlainNodeReference()
            def currentSuperClass = classNode.getSuperClass()
            if (currentSuperClass.equals(GrailsASTUtils.OBJECT_CLASS_NODE)) {
                def domainClass = expression? ((ClassExpression) expression).getType() : null
                if (!domainClass) {
                    domainClass = annotationNode.getMember("domain")?.type
                    if (!domainClass) {
                        GrailsASTUtils.error(source, classNode, "Scaffolded controller (${classNode.name}) with @ScaffoldController does not have domain class set.", true)
                    }
                }
                classNode.setSuperClass(GrailsASTUtils.nonGeneric(superClassNode, domainClass))
                new ResourceTransform().addConstructor(classNode, domainClass, false)
            } else if (!currentSuperClass.isDerivedFrom(superClassNode)) {
               GrailsASTUtils.error(source, classNode, "Scaffolded controllers (${classNode.name}) cannot extend other classes: ${currentSuperClass.getName()}", true)
            }
        } else if (propertyNode != null) {
            GrailsASTUtils.error(source, propertyNode, "The 'scaffold' property must refer to a domain class.", true)
        }
    }

    @Override
    boolean shouldInject(URL url) {
        return url != null && ControllerActionTransformer.CONTROLLER_PATTERN.matcher(url.getFile()).find()
    }
}
