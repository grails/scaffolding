description "Example description", "grails generate-controller [DOMAIN CLASS]"

if(args) {
    def sourceClass = source(args[0])

    if(sourceClass) {
      def model = model(sourceClass)

      render template: template('scaffolding/Controller.groovy'), 
             destination: file("grails-app/controllers/${model.packagePath}/${model.convention('Controller')}.groovy"),
             model: model

      render template: template('scaffolding/Spec.groovy'), 
             destination: file("src/test/groovy/${model.packagePath}/${model.convention('ControllerSpec')}.groovy"),
             model: model           

      addStatus "Scaffolding completed for $sourceClass"                                         

    }
    else {
      error "No domain class found"
    }

}
else {
    error "No domain class specified"
}
