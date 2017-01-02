package grails.plugin.scaffolding

import grails.codegen.model.ModelBuilder
import grails.core.GrailsControllerClass
import grails.core.support.GrailsApplicationAware
import grails.io.IOUtils
import grails.core.GrailsApplication
import grails.util.BuildSettings
import grails.util.Environment
import groovy.text.GStringTemplateEngine
import groovy.text.Template
import groovy.transform.CompileStatic
import org.grails.buffer.*
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.grails.web.servlet.view.GroovyPageView
import org.grails.web.servlet.view.GroovyPageViewResolver
import org.springframework.context.ResourceLoaderAware
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.FileSystemResource
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
class ScaffoldingViewResolver extends GroovyPageViewResolver implements ResourceLoaderAware, ModelBuilder, GrailsApplicationAware {

    GrailsApplication grailsApplication
    ResourceLoader resourceLoader
    protected Map<String, View> generatedViewCache = new ConcurrentHashMap<>()

    private Resource findViewResourceForTemplateSet(GrailsControllerClass controllerClass, String shortViewName, String templateSet) throws Exception {
        Resource res = null
        if(Environment.isDevelopmentMode()) {
            res = new FileSystemResource(new File(BuildSettings.BASE_DIR, "src/main/templates/${templateSet}/${shortViewName}.gsp"))
        }

        if(!res?.exists()) {
            def url = IOUtils.findResourceRelativeToClass(controllerClass.clazz, "/META-INF/templates/${templateSet}/${shortViewName}.gsp")
            res = url ? new UrlResource(url) : null
        }

        if(!res.exists()) {
            res = resourceLoader.getResource("classpath:META-INF/templates/${templateSet}/${shortViewName}.gsp")
        }

        return res
    }

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
                String scaffoldTemplatesValue = controllerClass?.getPropertyValue("scaffoldTemplates")
                if (!scaffoldTemplatesValue)
                    scaffoldTemplatesValue = grailsApplication.config.getProperty("grails.plugin.scaffolding.defaultTemplateSet")
                if(scaffoldValue instanceof Class) {
                    def shortViewName = viewName.substring(viewName.lastIndexOf('/') + 1)
                    Resource res = scaffoldTemplatesValue?findViewResourceForTemplateSet(controllerClass, shortViewName, scaffoldTemplatesValue):null
                    if (!res || !res.exists())
                        res = findViewResourceForTemplateSet(controllerClass, shortViewName, "scaffolding")

                    if(res.exists()) {
                        def model = model((Class)scaffoldValue)
                        def viewGenerator = new GStringTemplateEngine()
                        Template t = viewGenerator.createTemplate(res.URL)

                        def contents = new FastStringWriter()
                        t.make(model.asMap()).writeTo(contents)

                        def template = templateEngine.createTemplate(new ByteArrayResource(contents.toString().getBytes(templateEngine.gspEncoding), "view:$viewName"))
                        view = new GroovyPageView()
                        view.setServletContext(getServletContext())
                        view.setTemplate(template)
                        view.setApplicationContext(getApplicationContext())
                        view.setTemplateEngine(templateEngine)
                        view.afterPropertiesSet()
                        generatedViewCache[viewName] = view
                        return view
                    }
                    else
                        return null
                }
            }
        }
        return view
    }
}