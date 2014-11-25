package grails.plugin.scaffolding

class ScaffoldingGrailsPlugin {

   // the version or versions of Grails the plugin is designed for
    def grailsVersion = "3.0.0.BUILD-SNAPSHOT > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    def title = "Scaffolding Plugin" // Headline display name of the plugin
    def author = "Graeme Rocher"
    def authorEmail = "grocher@pivotal.io"
    def description = '''\
Plugin that generates scaffolded controllers and views for a Grails application.
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/scaffolding"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
    def license = "APACHE"


    // Location of the plugin's issue tracker.
    def issueManagement = [ system: "Github", url: "https://github.com/grails3-plugins/scaffolding/issues" ]

    // Online location of the plugin's browseable source code.
    def scm = [ url: "https://github.com/grails3-plugins/scaffolding" ]

}
