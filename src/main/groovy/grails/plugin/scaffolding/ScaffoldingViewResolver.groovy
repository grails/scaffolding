package grails.plugin.scaffolding

import grails.codegen.model.ModelBuilder
import grails.io.IOUtils
import groovy.text.GStringTemplateEngine
import groovy.text.Template
import groovy.transform.CompileStatic
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.grails.web.servlet.view.GroovyPageView
import org.grails.web.servlet.view.GroovyPageViewResolver
import org.springframework.context.ResourceLoaderAware
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.UrlResource
import org.springframework.web.servlet.View

import java.util.concurrent.ConcurrentHashMap

/**
 * @author Graeme Rocher
 * @since 3.1
 */
@CompileStatic
class ScaffoldingViewResolver extends GroovyPageViewResolver implements ResourceLoaderAware, ModelBuilder {

    ResourceLoader resourceLoader
    protected Map<String, View> generatedViewCache = new ConcurrentHashMap<>()

    @Override
    protected View loadView(String viewName, Locale locale) throws Exception {
        def view = super.loadView(viewName, locale)
        if(view == null) {
            view = generatedViewCache[viewName]
            if(view != null) {
                return view
            }
            else {
                def webR = GrailsWebRequest.lookup()
                def controllerClass = webR.controllerClass

                def scaffoldValue = controllerClass?.getPropertyValue("scaffold")
                if(scaffoldValue instanceof Class) {
                    def shortViewName = viewName.substring(viewName.lastIndexOf('/') + 1)
                    def url = IOUtils.findResourceRelativeToClass(controllerClass.clazz, "META-INF/templates/scaffolding/${shortViewName}.gsp")
                    Resource res = url ? new UrlResource(url) : null
                    if(!res.exists()) {
                        res = resourceLoader.getResource("classpath:META-INF/templates/scaffolding/${shortViewName}.gsp")
                    }

                    if(res.exists()) {
                        def model = model((Class)scaffoldValue)
                        def viewGenerator = new GStringTemplateEngine()
                        Template t = viewGenerator.createTemplate(res.URL)

                        def contents = new StringWriter()
                        t.make(model.asMap()).writeTo(contents)
                        def template = templateEngine.createTemplate(new ByteArrayResource(contents.toString().bytes, "view:$viewName"))
                        view = new GroovyPageView()
                        view.setTemplate(template)
                        view.setTemplateEngine(templateEngine)
                        view.afterPropertiesSet()
                        return view
                    }
                    else {
                        return view
                    }

                }

            }
        }
        return view
    }
}