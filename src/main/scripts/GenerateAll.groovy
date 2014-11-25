description "Generates a controller and the associated views", "grails generate-all [DOMAIN CLASS]"

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

      render template: template('scaffolding/edit.gsp'), 
             destination: file("grails-app/views/${model.propertyName}/edit.gsp"),
             model: model           

      render template: template('scaffolding/create.gsp'), 
             destination: file("grails-app/views/${model.propertyName}/create.gsp"),
             model: model   

      render template: template('scaffolding/index.gsp'), 
             destination: file("grails-app/views/${model.propertyName}/index.gsp"),
             model: model           

      render template: template('scaffolding/show.gsp'), 
             destination: file("grails-app/views/${model.propertyName}/show.gsp"),
             model: model           


      addStatus "Scaffolding completed for $sourceClass"                                         
    }
    else {
      error "Domain class not found"
    }


}
else {
    error "No domain class specified"
}
